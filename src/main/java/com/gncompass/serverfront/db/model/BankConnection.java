package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.api.model.BankSummary;
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
  public int mInstitution = 0;
  public int mTransit = 0;
  public int mAccount = 0;

  // Internals
  public UUID mReferenceUuid = null;

  public BankConnection() {
  }

  public BankConnection(ResultSet rs) throws SQLException {
    updateFromFetch(rs);
  }

  /*=============================================================
   * PRIVATE FUNCTIONS
   *============================================================*/

   /**
    * Build the select SQL for all properties related to the bank connection
    * @param userId the user ID
    * @return the SelectBuilder reference object
    */
   private SelectBuilder buildSelectSql(long userId) {
     SelectBuilder selectBuilder = buildSelectSql()
         .where(getColumn(USER_ID) + "=" + Long.toString(userId));
     return selectBuilder;
   }

   /**
    * Build the select SQL for all properties related to all bank connections
    * @return the SelectBuilder reference object
    */
   private SelectBuilder buildSelectSql() {
     return new SelectBuilder(getTable())
         .column(getColumn(ID))
         .column(getColumn(REFERENCE))
         .column(getColumn(LOGIN_ID))
         .column(getColumn(ENABLED))
         .column(getColumn(INSTITUTION))
         .column(getColumn(TRANSIT))
         .column(getColumn(ACCOUNT));
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
    mInstitution = resultSet.getInt(getColumn(INSTITUTION));
    mTransit = resultSet.getInt(getColumn(TRANSIT));
    mAccount = resultSet.getInt(getColumn(ACCOUNT));

    mReferenceUuid = UuidHelper.getUUIDFromBytes(mReference);
  }

  /*=============================================================
   * PUBLIC FUNCTIONS
   *============================================================*/

  /**
   * Returns the API model for the bank summary information
   * @return the API model for a bank summary
   */
  public BankSummary getApiSummary() {
    return new BankSummary(mReferenceUuid.toString(), mInstitution);
  }

  /*
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
    String selectSql = new BankConnection().buildSelectSql(user.mId).toString();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        while (rs.next()) {
          bankConnections.add(new BankConnection(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(
                        "Unable to fetch the list of banks for the borrower with SQL", e);
    }

    return bankConnections;
  }
}
