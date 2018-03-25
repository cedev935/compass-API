package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.db.SelectBuilder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
// import java.util.UUID;

public class AssessmentFile extends AbstractObject {
  // Database name
  private static final String TABLE_NAME = "AssessmentFiles";

  // Database column names
  private static final String ID = "id";
  private static final String ASSESSMENT = "assessment";
  private static final String BUCKET = "bucket";
  private static final String FILENAME = "filename";
  private static final String TYPE = "type";
  private static final String UPLOADED = "uploaded";

  // Database parameters
  public long mId = 0;
  //public long mAssessmentId = 0;
  public String mBucket = null;
  public String mFileName = null;
  public String mType = null;
  public long mUploadedTime = 0L;

  public AssessmentFile() {
  }

  public AssessmentFile(ResultSet rs) throws SQLException {
    updateFromFetch(rs);
  }

  /*=============================================================
   * PRIVATE FUNCTIONS
   *============================================================*/

  /**
   * Build the select SQL for all properties related to the assessment file
   * @param assessment the assessment reference
   * @param specificFile is a specific file being looked for. Puts it as a parameter (?) at spot 1
   * @return the SelectBuilder reference object
   */
  private SelectBuilder buildSelectSql(Assessment assessment, boolean specificFile) {
    SelectBuilder selectBuilder = buildSelectSql()
        .where(getColumn(ASSESSMENT) + "=" + Long.toString(assessment.mId));
    if (specificFile) {
      selectBuilder.where(getColumn(FILENAME) + "=?");
    }
    return selectBuilder;
  }

  /**
   * Build the select SQL for all properties related to the assessment file row
   * @return the SelectBuilder reference object
   */
  private SelectBuilder buildSelectSql() {
    return new SelectBuilder(getTable())
        .column(getColumn(ID))
        .column(getColumn(BUCKET))
        .column(getColumn(FILENAME))
        .column(getColumn(TYPE))
        .column(getColumn(UPLOADED));
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
    //mAssessmentId = resultSet.getLong(getColumn(ASSESSMENT));
    mBucket = resultSet.getString(getColumn(BUCKET));
    mFileName = resultSet.getString(getColumn(FILENAME));
    mType = resultSet.getString(getColumn(TYPE));
    mUploadedTime = resultSet.getTimestamp(getColumn(UPLOADED)).getTime();
  }

  /*=============================================================
   * PUBLIC FUNCTIONS
   *============================================================*/

  /**
   * Returns the API model for the assessment file information
   * @return the API model for a assessment file
   */
  public com.gncompass.serverfront.api.model.AssessmentFile getApiModel() {
    return new com.gncompass.serverfront.api.model.AssessmentFile(mFileName, mType, mUploadedTime);
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
   * Fetches the list of all assessment files for the provided assessment
   * @param conn the SQL connection
   * @param assessment the assessment object to fetch for
   * @return the stack of assessment files tied to the assessment. Empty list if none found
   */
  public static List<AssessmentFile> getAllForAssessment(Connection conn, Assessment assessment)
      throws SQLException {
    List<AssessmentFile> assessmentFiles = new ArrayList<>();

    // Build the query
    String selectSql = new AssessmentFile().buildSelectSql(assessment, false).toString();

    // Try to execute against the connection
    try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
      while (rs.next()) {
        assessmentFiles.add(new AssessmentFile(rs));
      }
    }

    return assessmentFiles;
  }
}
