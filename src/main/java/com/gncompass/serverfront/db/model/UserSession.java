package com.gncompass.serverfront.db.model;

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
  public User mUser = null;

  public UserSession() {
  }

  public UserSession(Cache sessionCache) {
    mId = sessionCache.mSessionId;
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

  public static void updateAccessed(Cache sessionCache) {
    new UserSession(sessionCache).updateAccessed();
  }

  /*=============================================================
   * INNER CLASSES
   *============================================================*/

  public static class Cache {
    public long mId;
    public UUID mReference;
    public long mSessionId;

    public Cache(long id, UUID reference, long sessionId) {
      mId = id;
      mReference = reference;
      mSessionId = sessionId;
    }

    public boolean matches(String userReference) {
      // TODO: Should it cache the string UUID to make comparisons faster?
      return (mReference != null && mReference.equals(UUID.fromString(userReference)));
    }
  }
}
