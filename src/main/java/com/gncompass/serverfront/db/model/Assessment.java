package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.api.model.AssessmentSummary;
import com.gncompass.serverfront.db.SelectBuilder;
import com.gncompass.serverfront.db.SQLManager;
import com.gncompass.serverfront.util.UuidHelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Assessment extends AbstractObject {
  // Database name
  private static final String TABLE_NAME = "Assessments";

  // Database column names
  private static final String ID = "id";
  private static final String REFERENCE = "reference";
  private static final String BORROWER = "borrower";
  private static final String REGISTERED = "registered";
  private static final String UPDATED = "updated";
  private static final String STATUS = "status";
  private static final String RATING = "rating";

  // Database parameters
  public long mId = 0;
  public byte[] mReference = null;
  //public long mBorrowerId = 0;
  public long mRegisteredTime = 0L;
  public long mUpdatedTime = 0L;
  public int mStatusId = 0;
  public int mRatingId = 0;

  // Internals
  public UUID mReferenceUuid = null;

  public Assessment() {
  }

  public Assessment(ResultSet rs) throws SQLException {
    updateFromFetch(rs);
  }

  /*=============================================================
   * PRIVATE FUNCTIONS
   *============================================================*/

  /**
   * Build the select SQL for all properties related to the assessment
   * @param borrower the borrower reference
   * @param reference the bank connection UUID reference
   * @return the SelectBuilder reference object
   */
  private SelectBuilder buildSelectSql(Borrower borrower, String reference) {
    SelectBuilder selectBuilder = buildSelectSql()
        .where(getColumn(BORROWER) + "=" + Long.toString(borrower.mId));
    if (reference != null) {
      selectBuilder.where(getColumn(REFERENCE) + "=" + UuidHelper.getHexFromUUID(reference, true));
    }
    return selectBuilder;
  }

  /**
   * Build the select SQL for all properties related to all assessments
   * @return the SelectBuilder reference object
   */
  private SelectBuilder buildSelectSql() {
    return new SelectBuilder(getTable())
        .column(getColumn(ID))
        .column(getColumn(REFERENCE))
        .column(getColumn(REGISTERED))
        .column(getColumn(UPDATED))
        .column(getColumn(STATUS))
        .column(getColumn(RATING));
  }

  /*=============================================================
   * PROTECTED FUNCTIONS
   *============================================================*/

  /**
   * Updates the assessment info from the result set provided. This assumes it was fetched
   * appropriately by the SQL function
   * @param resultSet the result set to pull the data from. This will not call .next()
   * @throws SQLException if the data is unexpected in the result set
   */
  @Override
  protected void updateFromFetch(ResultSet resultSet) throws SQLException {
    mId = resultSet.getLong(getColumn(ID));
    mReference = resultSet.getBytes(getColumn(REFERENCE));
    //mBorrowerId = resultSet.getLong(getColumn(BORROWER));
    mRegisteredTime = resultSet.getTimestamp(getColumn(REGISTERED)).getTime();
    mUpdatedTime = resultSet.getTimestamp(getColumn(UPDATED)).getTime();
    mStatusId = resultSet.getInt(getColumn(STATUS));
    mRatingId = resultSet.getInt(getColumn(RATING));

    mReferenceUuid = UuidHelper.getUUIDFromBytes(mReference);
  }

  /*=============================================================
   * PUBLIC FUNCTIONS
   *============================================================*/

  /**
   * Returns the API model for the assessment summary information
   * @return the API model for a assessment summary
   */
  public AssessmentSummary getApiSummary() {
    return new AssessmentSummary(mReferenceUuid.toString(), mUpdatedTime, mStatusId, mRatingId);
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
   * Fetches the list of all assessments for the provided user
   * @param user the user object to fetch for
   * @return the stack of assessments tied to the user. Empty list if none found
   */
  public static List<Assessment> getAllForBorrower(Borrower borrower) {
    List<Assessment> assessments = new ArrayList<>();

    // Build the query
    String selectSql = new Assessment().buildSelectSql(borrower, null).toString();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        while (rs.next()) {
          assessments.add(new Assessment(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(
                        "Unable to fetch the list of assessments for the borrower with SQL", e);
    }

    return assessments;
  }
}
