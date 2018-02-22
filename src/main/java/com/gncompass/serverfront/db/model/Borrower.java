package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.db.SelectBuilder;
import com.gncompass.serverfront.db.SQLManager;
import com.gncompass.serverfront.util.UuidHelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Borrower extends User {
  // Database name
  private static final String TABLE_NAME = "Borrowers";

  // Database column names
  private static final String ID = "id";
  private static final String REFERENCE = "reference";
  private static final String EMAIL = "email";
  private static final String TYPE = "type";
  private static final String PHONE = "phone";
  private static final String EMPLOYER = "employer";
  private static final String JOB_TITLE = "job_title";
  private static final String LOAN_CAP = "loan_cap";

  // Database parameters
  public int mId = 0;
  public byte[] mReference = null;
  public String mEmail = null;
  //public int mType = 0;
  public String mPhone = null;
  public String mEmployer = null;
  public String mJobTitle = null;
  public float mLoanCap = 0.0f;

  // Internals
  public UUID mReferenceUuid = null;

  /*=============================================================
   * PRIVATE FUNCTIONS
   *============================================================*/

  /**
   * Build the select SQL for all properties related to the borrower. Allows for choosing between
   * JOIN or FROM for how this table is connected
   * @param reference the borrower UUID reference
   * @param isJoin TRUE if is JOIN. FALSE if is FROM
   * @param userIdColumn if JOIN, a user id column defines the matching ON column to join for the
   *                     parent ID
   * @return the SelectBuilder reference object
   */
  private SelectBuilder buildSelectSql(String reference, boolean isJoin, String userIdColumn) {
    SelectBuilder selectBuilder =
        super.buildSelectParentSql(getColumn(ID), getColumn(TYPE), isJoin ? userIdColumn : null)
        .column(getColumn(ID))
        .column(getColumn(REFERENCE))
        .column(getColumn(EMAIL))
        //.column(getColumn(TYPE))
        .column(getColumn(PHONE))
        .column(getColumn(EMPLOYER))
        .column(getColumn(JOB_TITLE))
        .column(getColumn(LOAN_CAP));

    String refMatch = getColumn(REFERENCE) + "=" + UuidHelper.getHexFromUUID(reference, true);
    if (isJoin) {
      selectBuilder
          .join(getTable(), refMatch, true);
    } else {
      selectBuilder
          .from(getTable())
          .where(refMatch);
    }

    return selectBuilder;
  }

  /*=============================================================
   * PROTECTED FUNCTIONS
   *============================================================*/

  /**
   * Build the select SQL for all properties related to the borrower. This is the query when this
   * class is controlling (the query FROM table is this one)
   * @param reference the borrower UUID reference
   * @return the SelectBuilder reference object
   */
  protected SelectBuilder buildSelectSql(String reference) {
    return buildSelectSql(reference, false, null);
  }

  /**
   * Returns the table name of the class
   * @return the object table name
   */
  @Override
  protected String getTable() {
    return TABLE_NAME;
  }

  /*=============================================================
   * PACKAGE-PRIVATE FUNCTIONS
   *============================================================*/

  /**
   * Updates the borrower info from the result set provided. This assumes it was fetched
   * appropriately by the SQL function
   * @param resultSet the result set to pull the data from. This will not call .next()
   * @throws SQLException if the data is unexpected in the result set
   */
  @Override
  void updateFromFetch(ResultSet resultSet) throws SQLException {
    super.updateFromFetch(resultSet);

    mId = resultSet.getInt(getColumn(ID));
    mReference = resultSet.getBytes(getColumn(REFERENCE));
    mEmail = resultSet.getString(getColumn(EMAIL));
    //mType = resultSet.getInt(getColumn(TYPE));
    mPhone = resultSet.getString(getColumn(PHONE));
    mEmployer = resultSet.getString(getColumn(EMPLOYER));
    mJobTitle = resultSet.getString(getColumn(JOB_TITLE));
    mLoanCap = resultSet.getFloat(getColumn(LOAN_CAP));

    mReferenceUuid = UuidHelper.getUUIDFromBytes(mReference);
  }

  /*=============================================================
   * PUBLIC FUNCTIONS
   *============================================================*/

  /**
   * Fetches the borrower information from the database
   * @param reference the reference UUID to the borrower
   * @return the Borrower class object with the information fetched. If not found, return NULL
   */
  public Borrower getBorrower(String reference) {
    // Build the query
    String selectSql = buildSelectSql(reference).toString();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        if (rs.next()) {
          updateFromFetch(rs);
          return this;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Unable to fetch the borrower reference with SQL", e);
    }

    return null;
  }

  /*
   * Returns the user type
   * @return the user type enum
   */
  @Override
  public UserType getUserType() {
    return UserType.BORROWER;
  }

  /**
   * Returns if the user reference matches the string UUID provided
   * @param userReference the user UUID reference
   * @return TRUE if matches. FALSE if doesn't
   */
  @Override
  public boolean matches(String userReference) {
    // TODO: Should it cache the string UUID to make comparisons faster?
    return (mReferenceUuid != null && mReferenceUuid.equals(UUID.fromString(userReference)));
  }

  /*=============================================================
   * STATIC FUNCTIONS
   *============================================================*/

  /**
   * Build the select SQL for all properties related to the borrower. This is the query when this
   * class is just an extension (the query JOIN table is this one)
   * @param reference the borrower UUID reference
   * @param userIdColumn the column defines the matching ON column to join for the parent ID
   * @return the SelectBuilder reference object
   */
  static SelectBuilder buildSelectJoinSql(String reference, String userIdColumn) {
    return new Borrower().buildSelectSql(reference, true, userIdColumn);
  }
}
