package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.api.model.AuthResponse;
import com.gncompass.serverfront.db.DeleteBuilder;
import com.gncompass.serverfront.db.InsertBuilder;
import com.gncompass.serverfront.db.SelectBuilder;
import com.gncompass.serverfront.db.SQLManager;
import com.gncompass.serverfront.db.UpdateBuilder;
import com.gncompass.serverfront.db.model.User.UserType;
import com.gncompass.serverfront.util.StringHelper.AccessKey;
import com.gncompass.serverfront.util.UuidHelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public class UserSession extends AbstractObject {
  // Database name
  private static final String TABLE_NAME = "UserSessions";

  // Database column names
  private static final String ID = "id";
  private static final String USER_ID = "user_id";
  private static final String DEVICE_ID = "device_id";
  private static final String SESSION_KEY = "session_key";
  private static final String CREATED = "created";
  private static final String ACCESSED = "accessed";

  // Database parameters
  public long mId = 0;
  public long mUserId = 0;
  public byte[] mDeviceId = null;
  public byte[] mSessionKey = null;
  public Timestamp mCreated = null;
  public Timestamp mAccessed = null;

  // Internals
  public UUID mDeviceUuid = null;
  public UUID mSessionUuid = null;
  public User mUser = null;

  public UserSession() {
  }

  public UserSession(Cache sessionCache) {
    mId = sessionCache.mSessionId;
  }

  public UserSession(User user, UUID deviceUuid, UUID sessionUuid) {
    mDeviceUuid = deviceUuid;
    mSessionUuid = sessionUuid;
    mUser = user;
  }

  /*=============================================================
   * PRIVATE FUNCTIONS
   *============================================================*/

  /**
   * Updates the user session info from the result set provided and user type provided. This assumes
   * it was fetched appropriately by the SQL function
   * @param type the user type (borrower, investor)
   * @param resultSet the result set to pull the data from. This will not call .next()
   * @throws SQLException if the data is unexpected in the result set
   */
  private void updateFromFetch(UserType type, ResultSet resultSet) throws SQLException {
    // Determine the user type and fetch the user data
    mUser = null;
    switch (type) {
      case BORROWER:
        mUser = new Borrower();
        break;
      case INVESTOR:
        mUser = new Investor();
        break;
      default:
        throw new RuntimeException("User type not implemented for update from fetch in user session");
    }
    mUser.updateFromFetch(resultSet);

    // Fetch the internal data
    updateFromFetch(resultSet);
  }

  /*=============================================================
   * PROTECTED FUNCTIONS
   *============================================================*/

  /**
   * Build the select SQL for all properties related to the user session
   * @param type the type of user to fetch the session for (changes the join)
   * @param reference the user reference UUID string
   * @param deviceId the device ID reference UUID string
   * @param sessionKey the session key reference UUID string
   * @return the SelectBuilder reference object
   */
  protected SelectBuilder buildSelectSql(UserType type, String reference, String deviceId,
                                         String sessionKey) {
    // Determine the user join
    SelectBuilder selectBuilder = null;
    switch (type) {
      case BORROWER:
        selectBuilder = Borrower.buildSelectJoinSql(reference, getColumn(USER_ID));
        break;
      case INVESTOR:
        selectBuilder = Investor.buildSelectJoinSql(reference, getColumn(USER_ID));
        break;
      default:
        throw new RuntimeException("User type not implemented for build select sql in user session");
    }

    // This objects table properties
    return selectBuilder
        .column(getColumn(ID))
        .column(getColumn(USER_ID))
        .column(getColumn(DEVICE_ID))
        .column(getColumn(SESSION_KEY))
        .column(getColumn(CREATED))
        .column(getColumn(ACCESSED))
        .from(getTable())
        .where(getColumn(DEVICE_ID) + "=" + UuidHelper.getHexFromUUID(deviceId, true))
        .where(getColumn(SESSION_KEY) + "=" + UuidHelper.getHexFromUUID(sessionKey, true));
  }

  /**
   * Updates the user session info from the result set provided. This assumes it was fetched
   * appropriately by the SQL function
   * @param resultSet the result set to pull the data from. This will not call .next()
   * @throws SQLException if the data is unexpected in the result set
   */
  @Override
  protected void updateFromFetch(ResultSet resultSet) throws SQLException {
    mId = resultSet.getLong(getColumn(ID));
    mUserId = resultSet.getLong(getColumn(USER_ID));
    mDeviceId = resultSet.getBytes(getColumn(DEVICE_ID));
    mSessionKey = resultSet.getBytes(getColumn(SESSION_KEY));
    mCreated = resultSet.getTimestamp(getColumn(CREATED));
    mAccessed = resultSet.getTimestamp(getColumn(ACCESSED));
  }

  /*=============================================================
   * PUBLIC FUNCTIONS
   *============================================================*/

  /**
   * Adds this session to the database for an existing user
   * @return TRUE if success. FALSE otherwise
   */
  public boolean addToDatabase() {
    // Make sure the correct parameters were set
    if(mDeviceUuid != null && mSessionUuid != null && mUser != null) {
      // Create the session insert statement
      String insertSql = new InsertBuilder(getTable())
          .set(USER_ID, Long.toString(mUser.mId))
          .set(DEVICE_ID, UuidHelper.getHexFromUUID(mDeviceUuid, true))
          .set(SESSION_KEY, UuidHelper.getHexFromUUID(mSessionUuid, true))
          .toString();

      // Try to fetch a connection
      try (Connection conn = SQLManager.getConnection()) {
        return (conn.prepareStatement(insertSql).executeUpdate() == 1);
      } catch (SQLException e) {
        throw new RuntimeException("Unable to add the new session for an existing user", e);
      }
    }
    return false;
  }

  /**
   * Adds the user to the database first and then establishes this first session with the user.
   * This is all done within one transaction. If any call fails, the whole call is reverted
   * @return TRUE if successful and the entry exists. FALSE otherwise
   */
  public boolean addToDatabaseWithUser() {
    // Make sure the correct parameters were set
    if(mDeviceUuid != null && mSessionUuid != null && mUser != null) {
      // Create the session insert statement
      String insertSql = new InsertBuilder(getTable())
          .set(USER_ID, "LAST_INSERT_ID()")
          .set(DEVICE_ID, UuidHelper.getHexFromUUID(mDeviceUuid, true))
          .set(SESSION_KEY, UuidHelper.getHexFromUUID(mSessionUuid, true))
          .toString();

      // Try to fetch a connection
      try (Connection conn = SQLManager.getConnection()) {
        boolean success = false;

        // Start the transaction by ceasing auto commits
        conn.setAutoCommit(false);

        // Execute against the user first
        if (mUser.addToDatabase(conn)) {
          // Execute session insert statement (should return 1 row)
          if (conn.prepareStatement(insertSql).executeUpdate() == 1) {
            success = true;
          }
        }

        // Depending on the result, either commit or rollback
        if (success) {
          conn.commit();
          return true;
        } else {
          conn.rollback();
        }
      } catch (SQLException e) {
        throw new RuntimeException("Unable to add the new user with the new session", e);
      }
    }
    return false;
  }

  /**
   * Deletes all sessions that match the specific parameters of this session. This requires the
   * user ID and the device UUID to be set. Optionally, it will also enforce by session UUID
   * @return the number of rows deleted
   */
  public int deleteIfMatches() {
    if(mDeviceUuid != null && mUser != null) {
      // Create the session delete statement
      DeleteBuilder deleteSqlBuilder = new DeleteBuilder(getTable())
          .where(getColumn(USER_ID) + "=" + mUser.mId)
          .where(getColumn(DEVICE_ID) + "=" + UuidHelper.getHexFromUUID(mDeviceUuid, true));
      if(mSessionKey != null) {
        deleteSqlBuilder.where(
            getColumn(SESSION_KEY) + "=" + UuidHelper.getHexFromUUID(mSessionUuid, true));
      }
      String deleteSql = deleteSqlBuilder.toString();

      // Try to fetch a connection
      try (Connection conn = SQLManager.getConnection()) {
        // Execute session delete statement
        return conn.prepareStatement(deleteSql).executeUpdate();
      } catch (SQLException e) {
        throw new RuntimeException("Unable to delete old user sessions", e);
      }
    }
    return 0;
  }

  /**
   * Deletes the current session from the database
   * @return TRUE if successful. FALSE if nothing was deleted
   */
  public boolean deleteSession() {
    // Create the session delete statement
    String deleteSql = new DeleteBuilder(getTable())
        .where(getColumn(ID) + "=" + Long.toString(mId))
        .toString();

    // Try to fetch a connection
    try (Connection conn = SQLManager.getConnection()) {
      // Execute session delete statement
      return (conn.prepareStatement(deleteSql).executeUpdate() == 1);
    } catch (SQLException e) {
      throw new RuntimeException("Unable to delete selected user session", e);
    }
  }

  /**
   * Returns the auth response model for the API
   * @return the auth response model
   */
  public AuthResponse getAuthResponse() {
    return new AuthResponse(mSessionUuid.toString(), mUser.getUserReference().toString());
  }

  /**
   * Returns the user session cache object
   * @return the cache object
   */
  public Cache getCache() {
    if (mUser != null) {
      return new Cache(mUser.getUserId(), mUser.getUserReference(), mId);
    }
    return null;
  }

  /**
   * Fetches the user session information from the database
   * @param type the user type of the session (User.UserType)
   * @param reference the reference UUID of the user
   * @param accessKey the access key information for the session
   * @return the UserSession class object with the information fetched. If not found, return NULL
   */
  public UserSession getSession(UserType type, String reference, AccessKey accessKey) {
    // Build the query
    String selectSql =
        buildSelectSql(type, reference, accessKey.deviceId, accessKey.sessionKey).toString();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        if (rs.next()) {
          updateFromFetch(type, rs);
          return this;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Unable to fetch the borrower reference with SQL", e);
    }

    return null;
  }

  /*
   * Returns the table name of the class
   * @return the object table name
   */
  @Override
  public String getTable() {
    return TABLE_NAME;
  }

  /**
   * Identifies if the type and reference provided matches the session user
   * @param type the user type
   * @param userReference the user UUID reference
   * @return TRUE if matches. FALSE if doesn't
   */
  public boolean matches(UserType type, String userReference) {
    return (mUser != null && mUser.matches(type, userReference));
  }

  /**
   * Update the accessed time of the user session in the database
   */
  public void updateAccessed() {
    // Build the statement
    String updateSql = new UpdateBuilder(getTable())
        .set(getColumn(ACCESSED) + "=NOW()")
        .where(getColumn(ID) + "=" + mId)
        .toString();
    mAccessed = new Timestamp(new Date().getTime());

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      conn.prepareStatement(updateSql).executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Unable to update the session accessed time with SQL", e);
    }
  }

  /*=============================================================
   * STATIC FUNCTIONS
   *============================================================*/

  public static void deleteIfMatches(User user, UUID deviceUuid) {
    new UserSession(user, deviceUuid, null).deleteIfMatches();
  }

  public static void updateAccessed(Cache sessionCache) {
    new UserSession(sessionCache).updateAccessed();
  }

  /*=============================================================
   * INNER CLASSES
   *============================================================*/

  public static class Cache {
    private static final long EXPIRATION_HRS = 1;
    private static final long EXPIRATION_MS = EXPIRATION_HRS * 60 * 60 * 1000;

    public Date mAccessed;
    public long mId;
    public UUID mReference;
    public long mSessionId;

    public Cache(long id, UUID reference, long sessionId) {
      mId = id;
      mReference = reference;
      mSessionId = sessionId;

      updateAccessed();
    }

    public boolean isExpired() {
      long diffInMillies = new Date().getTime() - mAccessed.getTime();
      return (diffInMillies > EXPIRATION_MS);
    }

    public boolean matches(String userReference) {
      // TODO: Should it cache the string UUID to make comparisons faster?
      return matches(UUID.fromString(userReference));
    }

    public boolean matches(UUID userReference) {
      return (mReference != null && mReference.equals(userReference));
    }

    public void updateAccessed() {
      mAccessed = new Date();
    }
  }
}
