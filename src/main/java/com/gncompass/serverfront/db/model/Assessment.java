package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.api.model.AssessmentInfo;
import com.gncompass.serverfront.api.model.AssessmentSummary;
import com.gncompass.serverfront.db.InsertBuilder;
import com.gncompass.serverfront.db.SelectBuilder;
import com.gncompass.serverfront.db.SQLManager;
import com.gncompass.serverfront.db.UpdateBuilder;
import com.gncompass.serverfront.util.StateHelper;
import com.gncompass.serverfront.util.UuidHelper;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.UploadOptions;

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

  // General statics
  private static final int MIN_SUBMIT_FILES = 2;
  private static final String UPLOAD_BUCKET = "test-gnc-data";
  private static final String UPLOAD_CALLBACK = "/uploads/assessments/";

  // Status enumeration
  public enum Status {
    STARTED(1),
    PENDING(2),
    APPROVED(3),
    REJECTED(4);

    private final int value;

    Status(final int newValue) {
      value = newValue;
    }

    public int getValue() {
      return value;
    }
  }

  // Database parameters
  public long mId = 0;
  public byte[] mReference = null;
  //public long mBorrowerId = 0;
  public long mRegisteredTime = 0L;
  public long mUpdatedTime = 0L;
  public int mStatusId = 0;
  public int mRatingId = 0;

  // Internals
  private List<AssessmentFile> mAssessmentFiles = new ArrayList<>();
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
   * Adds a join of this table for the indicated borrower and tied to the id column for the on
   * statement. Internal version
   * @param selectBuilder the select builder to add to
   * @param idColumn the id column of the select statement to join with
   * @param reference the assessment reference UUID
   * @param borrower the borrower of the join
   * @return the select builder result
   */
  private SelectBuilder addJoinInternal(SelectBuilder selectBuilder, String idColumn,
                                        String reference, Borrower borrower) {
    String joinOn = getColumn(ID) + "=" + idColumn + " AND "
                  + getColumn(BORROWER) + "=" + Long.toString(borrower.mId) + " AND "
                  + getColumn(REFERENCE) + "=" + UuidHelper.getHexFromUUID(reference, true);
    return selectBuilder.join(getTable(), joinOn);
  }

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
    * Adds the assessment to the database
    * @param borrower the borrower that will own this assessment
    * @return TRUE if successfully added. FALSE otherwise
    */
  public boolean addToDatabase(Borrower borrower) {
    mReferenceUuid = UUID.randomUUID();

    // Create the assessment insert and select statement
    String insertSql = new InsertBuilder(getTable())
        .set(REFERENCE, UuidHelper.getHexFromUUID(mReferenceUuid, true))
        .set(BORROWER, Long.toString(borrower.mId))
        .set(STATUS, Integer.toString(Status.STARTED.getValue()))
        .toString();
    SelectBuilder selectBuilder = buildSelectSql(borrower, null);
    selectBuilder.where(getColumn(ID) + "=LAST_INSERT_ID()");
    String selectSql = selectBuilder.toString();

    // Execute the insert
    try (Connection conn = SQLManager.getConnection()) {
      if (conn.prepareStatement(insertSql).executeUpdate() == 1) {
        // Fetch the assessment that was just created
        try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
          if (rs.next()) {
            updateFromFetch(rs);
            return true;
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Unable to add the assessment for an existing borrower", e);
    }

    return false;
  }

  /**
   * Can this assessment be submitted. Checks for pre-validation requirements
   * @return TRUE if it can be submitted with submit(). FALSE otherwise
   */
  public boolean canBeSubmitted() {
    return (mStatusId == Status.STARTED.getValue() && mAssessmentFiles.size() >= MIN_SUBMIT_FILES);
  }

  /**
   * Returns the API model for the assessment info object
   * @return the API mode for the assessment info
   */
  public AssessmentInfo getApiInfo() {
    // Fetch the upload URL
    String uploadUrl = null;
    if (mStatusId == Status.STARTED.getValue()) {
      BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
      UploadOptions uploadOptions = null;
      String bucket = null;
      if (StateHelper.isProduction()) {
        bucket = UPLOAD_BUCKET;
      }
      if (bucket == null || bucket.isEmpty()) {
        uploadOptions = UploadOptions.Builder.withDefaults();
      } else {
        uploadOptions = UploadOptions.Builder.withGoogleStorageBucketName(bucket);
      }
      String callbackUrl = UPLOAD_CALLBACK + mReferenceUuid.toString();
      uploadUrl = blobstoreService.createUploadUrl(callbackUrl, uploadOptions);
    }

    // Generate the assessment info
    AssessmentInfo info = new AssessmentInfo(
                                    mRegisteredTime, mUpdatedTime, mStatusId, mRatingId,
                                    uploadUrl);
    for (AssessmentFile file : mAssessmentFiles) {
      info.addFile(file.getApiModel());
    }
    return info;
  }

  /**
   * Returns the API model for the assessment summary information
   * @return the API model for a assessment summary
   */
  public AssessmentSummary getApiSummary() {
    return new AssessmentSummary(mReferenceUuid.toString(), mUpdatedTime, mStatusId, mRatingId);
  }

  /**
   * Fetches the assessment information from the database
   * @param borrower the borrower object to fetch for
   * @param reference the reference UUID to the assessment
   * @return the assessment object with the information fetched. If not found, return NULL
   */
  public Assessment getAssessment(Borrower borrower, String reference) {
    // Build the query
    String selectSql = buildSelectSql(borrower, reference).toString();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        if (rs.next()) {
          // Update core data
          updateFromFetch(rs);

          // Attempt to fetch the files
          mAssessmentFiles.clear();
          mAssessmentFiles.addAll(AssessmentFile.getAllForAssessment(conn, this));

          return this;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Unable to fetch the assessment reference with SQL", e);
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

  /**
   * Submits the assessment. This updates the state in the database to add it to the pending list
   * for review
   * @return TRUE if the assessment was successfully submitted. FALSE otherwise
   */
  public boolean submit() {
    if (canBeSubmitted()) {
      // The update statement
      String updateSql = new UpdateBuilder(getTable())
          .set(getColumn(STATUS) + "=" + Integer.toString(Status.PENDING.getValue()))
          .set(getColumn(UPDATED) + "=NOW()")
          .where(getColumn(ID) + "=" + Long.toString(mId))
          .toString();

      // Execute the update statement
      try (Connection conn = SQLManager.getConnection()) {
        conn.prepareStatement(updateSql).executeUpdate();
        return true;
      } catch (SQLException e) {
        throw new RuntimeException("Unable to update the assessment to submit with SQL", e);
      }
    }
    return false;
  }

  /*=============================================================
   * STATIC FUNCTIONS
   *============================================================*/

  /**
   * Adds a join of this table for the indicated borrower and tied to the id column for the on
   * statement. Package private version (exposed)
   * @param selectBuilder the select builder to add to
   * @param idColumn the id column of the select statement to join with
   * @param reference the assessment reference UUID
   * @param borrower the borrower of the join
   * @return the select builder result
   */
  static SelectBuilder addJoin(SelectBuilder selectBuilder, String idColumn, String reference,
                               Borrower borrower) {
    return new Assessment().addJoinInternal(selectBuilder, idColumn, reference, borrower);
  }

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
