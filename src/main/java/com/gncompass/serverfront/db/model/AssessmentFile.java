package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.db.InsertBuilder;
import com.gncompass.serverfront.db.SelectBuilder;
import com.gncompass.serverfront.db.SQLManager;
import com.gncompass.serverfront.db.UpdateBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

  public AssessmentFile(String bucket, String fileName, String type) {
    mBucket = bucket;
    mFileName = fileName;
    mType = type;
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
    * Adds the assessment file to the database
    * @param assessment the assessment that will own this assessment file
    * @return TRUE if successfully added. FALSE otherwise
    */
  public boolean addToDatabase(Assessment assessment) {
    // Create the assessment file insert statement
    String insertSql = new InsertBuilder(getTable())
        .set(ASSESSMENT, Long.toString(assessment.mId))
        .setString(BUCKET, mBucket)
        .setString(FILENAME, mFileName)
        .setString(TYPE, mType)
        .toString();

    // Execute the insert
    try (Connection conn = SQLManager.getConnection()) {
      if (conn.prepareStatement(insertSql).executeUpdate() == 1) {
        mUploadedTime = new Date().getTime();
        return true;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Unable to add the assessment file for an existing assessment", e);
    }

    return false;
  }

  /**
   * Returns the API model for the assessment file information
   * @return the API model for a assessment file
   */
  public com.gncompass.serverfront.api.model.AssessmentFile getApiModel() {
    return new com.gncompass.serverfront.api.model.AssessmentFile(mFileName, mType, mUploadedTime);
  }

  /**
   * Fetches a single assessment file from the database
   * @param borrower the borrower object to fetch for
   * @param reference the reference UUID to the assessment
   * @param fileName the specific file name (non case specific)
   * @return the assessment file object with the information fetched. If not found, return NULL
   */
  public AssessmentFile getFile(Borrower borrower, String reference, String fileName) {
    // Build the query
    SelectBuilder selectBuilder = buildSelectSql()
        .where(getColumn(FILENAME) + "=?");
    Assessment.addJoin(selectBuilder, getColumn(ASSESSMENT), reference, borrower);
    String selectSql = selectBuilder.toString();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
        ps.setString(1, fileName);
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            updateFromFetch(rs);
            return this;
          }
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException("Unable to fetch the assessment file reference with SQL", e);
    }

    return null;
  }

  /**
   * Returns the google storage path to the file
   * @param assessmentUuid the assessment UUID reference
   * @return the string to the google storage path
   */
  public String getGSPath(String assessmentUuid) {
    return "/gs/" + mBucket + "/" + getStoragePath(assessmentUuid, mFileName);
  }

  /**
   * Returns the table name of the class
   * @return the object table name
   */
  @Override
  public String getTable() {
    return TABLE_NAME;
  }

  /**
   * Checks if the assessment file matches this one
   * @param file the assessment file to check for a match
   * @return TRUE if it matches. FALSE otherwise
   */
  public boolean matches(AssessmentFile file) {
    return mBucket != null && mBucket.matches(file.mBucket)
        && mFileName != null && mFileName.matches(file.mFileName);
  }

  /**
   * Updates the uploaded date on a file overwrite
   */
  public void updateUploaded() {
    // Build the statement
    String updateSql = new UpdateBuilder(getTable())
        .set(getColumn(UPLOADED) + "=NOW()")
        .where(getColumn(ID) + "=" + mId)
        .toString();
    mUploadedTime = new Date().getTime();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      conn.prepareStatement(updateSql).executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Unable to update the assessment file uploaded time with SQL", e);
    }
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

  /**
   * Returns the storage path for the indicated assessment file
   * @param assessmentUuid the assessment UUID string
   * @param fileName the file name for the assessment package
   * @return the string full path in the storage bucket
   */
  public static String getStoragePath(String assessmentUuid, String fileName) {
    return "assessments/" + assessmentUuid.toLowerCase() + "/" + fileName;
  }
}
