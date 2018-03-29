package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.api.model.BankConnectionInfo;
import com.gncompass.serverfront.api.model.BankConnectionNew;
import com.gncompass.serverfront.api.model.BankConnectionSummary;
import com.gncompass.serverfront.db.InsertBuilder;
import com.gncompass.serverfront.db.SelectBuilder;
import com.gncompass.serverfront.db.SQLManager;
import com.gncompass.serverfront.util.UuidHelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BankConnection extends AbstractObject {
  // Database name
  private static final String TABLE_NAME = "BankConnections";

  // Database column names
  private static final String ID = "id";
  private static final String REFERENCE = "reference";
  private static final String USER_ID = "user_id";
  private static final String LOGIN_ID = "login_id";
  private static final String ENABLED = "enabled";
  private static final String INSTITUTION = "institution";
  private static final String TRANSIT = "transit";
  private static final String ACCOUNT = "account";

  // Database parameters
  public long mId = 0;
  public byte[] mReference = null;
  //public long mUserId = 0;
  public byte[] mLoginId = null;
  public boolean mEnabled = false;
  public long mInstitution = 0;
  public int mTransit = 0;
  public int mAccount = 0;

  // Internals
  public Bank mBank = null;
  public UUID mLoginUuid = null;
  public UUID mReferenceUuid = null;

  public BankConnection() {
  }

  public BankConnection(BankConnectionNew bankInfo) {
    mAccount = bankInfo.mAccount;
    mInstitution = bankInfo.mBankId;
    mLoginUuid = UUID.fromString(bankInfo.mLoginId);
    mTransit = bankInfo.mTransit;

    mReferenceUuid = UUID.randomUUID();
  }

  public BankConnection(ResultSet rs) throws SQLException {
    updateFromFetch(rs);
  }

  /*=============================================================
   * PRIVATE FUNCTIONS
   *============================================================*/

  /**
   * Build the select SQL for all properties related to the bank connection
   * @param user the user reference
   * @param reference the bank connection UUID reference
   * @return the SelectBuilder reference object
   */
  private SelectBuilder buildSelectSql(User user, String reference) {
    SelectBuilder selectBuilder = buildSelectSql()
        .where(getColumn(USER_ID) + "=" + Long.toString(user.mId));
    if (reference != null) {
      selectBuilder.where(getColumn(REFERENCE) + "=" + UuidHelper.getHexFromUUID(reference, true));
    }
    return selectBuilder;
  }

  /**
   * Build the select SQL for all properties related to all bank connections
   * @return the SelectBuilder reference object
   */
  private SelectBuilder buildSelectSql() {
    SelectBuilder selectBuilder = new SelectBuilder(getTable())
        .column(getColumn(ID))
        .column(getColumn(REFERENCE))
        .column(getColumn(LOGIN_ID))
        .column(getColumn(ENABLED))
        .column(getColumn(INSTITUTION))
        .column(getColumn(TRANSIT))
        .column(getColumn(ACCOUNT));
    return Bank.join(selectBuilder, getColumn(INSTITUTION));
  }

  /*=============================================================
   * PROTECTED FUNCTIONS
   *============================================================*/

  /**
   * Updates the bank connection info from the result set provided. This assumes it was fetched
   * appropriately by the SQL function
   * @param resultSet the result set to pull the data from. This will not call .next()
   * @throws SQLException if the data is unexpected in the result set
   */
  @Override
  protected void updateFromFetch(ResultSet resultSet) throws SQLException {
    mId = resultSet.getLong(getColumn(ID));
    mReference = resultSet.getBytes(getColumn(REFERENCE));
    //mUserId = resultSet.getLong(getColumn(USER));
    mLoginId = resultSet.getBytes(getColumn(LOGIN_ID));
    mEnabled = resultSet.getBoolean(getColumn(ENABLED));
    mInstitution = resultSet.getLong(getColumn(INSTITUTION));
    mTransit = resultSet.getInt(getColumn(TRANSIT));
    mAccount = resultSet.getInt(getColumn(ACCOUNT));

    mBank = new Bank(resultSet);
    mLoginUuid = UuidHelper.getUUIDFromBytes(mLoginId);
    mReferenceUuid = UuidHelper.getUUIDFromBytes(mReference);
  }

  /*=============================================================
   * PUBLIC FUNCTIONS
   *============================================================*/

  /**
    * Adds the bank connection to the database
    * @param user the user that will own this bank connection
    * @return TRUE if successfully added. FALSE otherwise
    */
  public boolean addToDatabase(User user) {
    if (mAccount > 0 && mInstitution > 0 && mLoginUuid != null && mReferenceUuid != null
        && mTransit > 0 && user != null) {
      // Create the bank connection insert statement
      String insertSql = new InsertBuilder(getTable())
          .set(REFERENCE, UuidHelper.getHexFromUUID(mReferenceUuid, true))
          .set(USER_ID, Long.toString(user.mId))
          .set(LOGIN_ID, UuidHelper.getHexFromUUID(mLoginUuid, true))
          .set(INSTITUTION, Long.toString(mInstitution))
          .set(TRANSIT, Integer.toString(mTransit))
          .set(ACCOUNT, Integer.toString(mAccount))
          .toString();

      // Execute the insert
      try (Connection conn = SQLManager.getConnection()) {
        return (conn.prepareStatement(insertSql).executeUpdate() == 1);
      } catch (SQLException e) {
        throw new RuntimeException("Unable to add the bank connection for an existing user", e);
      }
    }
    return false;
  }

  /**
   * Returns the API model for the bank info object
   * @return the API mode for the bank info
   */
  public BankConnectionInfo getApiInfo() {
    return new BankConnectionInfo(mLoginUuid.toString(), mBank.mCode, mTransit, mAccount);
  }

  /**
   * Returns the API model for the bank summary information
   * @return the API model for a bank summary
   */
  public BankConnectionSummary getApiSummary() {
    return new BankConnectionSummary(mReferenceUuid.toString(), mBank.mCode, mBank.mName);
  }

  /**
   * Fetches the bank connection information from the database
   * @param user the user object to fetch for
   * @param reference the reference UUID to the bank connection
   * @return the bank connection object with the information fetched. If not found, return NULL
   */
  public BankConnection getBankConnection(User user, String reference) {
    // Build the query
    String selectSql = buildSelectSql(user, reference).toString();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        if (rs.next()) {
          updateFromFetch(rs);
          return this;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Unable to fetch the bank connection reference with SQL", e);
    }

    return null;
  }

  /**
   * Returns the table name of the class
   * @return the object table name
   */
  @Override
  public String getTable() {
    return TABLE_NAME;
  }

  /*=============================================================
   * STATIC FUNCTIONS
   *============================================================*/

  /**
   * Fetches the list of all banks for the provided user
   * @param user the user object to fetch for
   * @return the stack of bank connections tied to the user. Empty list if none found
   */
  public static List<BankConnection> getAllForUser(User user) {
    List<BankConnection> bankConnections = new ArrayList<>();

    // Build the query
    String selectSql = new BankConnection().buildSelectSql(user, null).toString();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        while (rs.next()) {
          bankConnections.add(new BankConnection(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(
                        "Unable to fetch the list of banks for the user with SQL", e);
    }

    return bankConnections;
  }
}
