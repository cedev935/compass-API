package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.api.model.BorrowerEditable;
import com.gncompass.serverfront.api.model.BorrowerViewable;
import com.gncompass.serverfront.api.model.UserViewable;
import com.gncompass.serverfront.db.InsertBuilder;
import com.gncompass.serverfront.db.SelectBuilder;
import com.gncompass.serverfront.db.SQLManager;
import com.gncompass.serverfront.db.UpdateBuilder;
import com.gncompass.serverfront.util.UuidHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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
  //public long mId = 0;
  public byte[] mReference = null;
  public String mEmail = null;
  //public int mType = 0;
  public String mPhone = null;
  public String mEmployer = null;
  public String mJobTitle = null;
  public float mLoanCap = 0.0f;

  // Internals
  private List<Assessment> mAssessments = null;
  public UUID mReferenceUuid = null;

  public Borrower() {
  }

  public Borrower(UUID referenceUuid, String email, String name,
                  String passwordHash, Country country) {
    super(name, passwordHash, country);

    mEmail = email;
    mPhone = "";
    mEmployer = "";
    mJobTitle = "";

    mReferenceUuid = referenceUuid;
  }

  /*=============================================================
   * PRIVATE FUNCTIONS
   *============================================================*/

  /**
   * Build the select SQL for all properties related to the borrower. Allows for choosing between
   * JOIN or FROM for how this table is connected
   * @param primaryWhere the borrower primary where line (either for top where or join)
   * @param isJoin TRUE if is JOIN. FALSE if is FROM
   * @param userIdColumn if JOIN, a user id column defines the matching ON column to join for the
   *                     parent ID
   * @return the SelectBuilder reference object
   */
  private SelectBuilder buildSelectSql(String primaryWhere, boolean isJoin, String userIdColumn) {
    SelectBuilder selectBuilder =
        super.buildSelectParentSql(getColumn(ID), getColumn(TYPE), isJoin ? userIdColumn : null)
        //.column(getColumn(ID))
        .column(getColumn(REFERENCE))
        .column(getColumn(EMAIL))
        //.column(getColumn(TYPE))
        .column(getColumn(PHONE))
        .column(getColumn(EMPLOYER))
        .column(getColumn(JOB_TITLE))
        .column(getColumn(LOAN_CAP));

    if (isJoin) {
      selectBuilder
          .join(getTable(), primaryWhere, true);
    } else {
      selectBuilder
          .from(getTable())
          .where(primaryWhere);
    }

    return selectBuilder;
  }

  /**
   * Build the select SQL for all properties related to the borrower. Allows for choosing between
   * JOIN or FROM for how this table is connected
   * @param reference the borrower UUID reference
   * @param isJoin TRUE if is JOIN. FALSE if is FROM
   * @param userIdColumn if JOIN, a user id column defines the matching ON column to join for the
   *                     parent ID
   * @return the SelectBuilder reference object
   */
  private SelectBuilder buildSelectRefSql(String reference, boolean isJoin, String userIdColumn) {
    String refMatch = getColumn(REFERENCE) + "=" + UuidHelper.getHexFromUUID(reference, true);
    return buildSelectSql(refMatch, isJoin, userIdColumn);
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
    return buildSelectRefSql(reference, false, null);
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

    //mId = resultSet.getLong(getColumn(ID));
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
   * Adds the borrower to the database
   * @param conn the SQL connection
   * @return TRUE if successfully added. FALSE otherwise
   * @throws SQLException exception on insert
   */
  @Override
  public boolean addToDatabase(Connection conn) throws SQLException {
   if (mReferenceUuid != null && mEmail != null && mPhone != null && mEmployer != null &&
       mJobTitle != null) {
     // Add the user portion first
     if(super.addToDatabase(conn)) {
       // Create the borrower insert statement
       String insertSql = new InsertBuilder(getTable())
           .set(ID, "LAST_INSERT_ID()")
           .set(REFERENCE, UuidHelper.getHexFromUUID(mReferenceUuid, true))
           .setString(EMAIL, mEmail)
           .setString(PHONE, mPhone)
           .setString(EMPLOYER, mEmployer)
           .setString(JOB_TITLE, mJobTitle)
           .toString();

       // Execute the insert
       if (conn.prepareStatement(insertSql).executeUpdate() == 1) {
         return true;
       }
     }
   }
   return false;
  }

  /**
   * Fetches a subset of connected information, such as bank connections and assessments that
   * will be stored within this object
   */
  @Override
  public void fetchConnectedInfo() {
    super.fetchConnectedInfo();
    mAssessments = Assessment.getAllForBorrower(this);
  }

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

  /**
   * Fetches the borrower information, using the borrower email, from the database
   * @param email the email to find the borrower for
   * @return the Borrower class object with the information fetched. If not found, returns NULL
   */
  public Borrower getBorrowerByEmail(String email) {
    // Build the query
    String where = getColumn(EMAIL) + "='" + email + "'";
    String selectSql = buildSelectSql(where, false, null).toString();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        if (rs.next()) {
          updateFromFetch(rs);
          return this;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Unable to fetch the borrower by email with SQL", e);
    }

    return null;
  }

  /**
   * Returns the user reference
   * @return the user reference UUID
   */
  @Override
  public UUID getUserReference() {
    return mReferenceUuid;
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
   * Returns the viewable API JSON container
   * @param withConnectedInfo also include the connected info (assessments, banks, etc)
   * @return the user viewable API object
   */
  @Override
  public UserViewable getViewable(boolean withConnectedInfo) {
    BorrowerViewable viewable =
                      new BorrowerViewable(mEmail, mPhone, mEmployer, mJobTitle, mLoanCap);
    if (withConnectedInfo) {
      viewable.setAssessmentData(mAssessments);
    }
    super.updateViewable(viewable, withConnectedInfo);
    return viewable;
  }

  /**
   * Checks if a borrower exists for the given email
   * @param email the email to check if one exists
   * @return TRUE if a borrower already has that email. FALSE otherwise
   */
  public boolean isEmailExisting(String email) {
    String selectSql =
        new SelectBuilder(getTable())
        .column(getColumn(ID))
        .where(getColumn(EMAIL) + "='" + email + "'")
          .toString();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        if (rs.next()) {
          return true;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Unable to check if the borrower email exists with SQL", e);
    }

    return false;
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

  /**
   * Randomly assigns a loan cap to the borrower if one has not been provided already. This is only
   * temporary until production release with manual assessment approvals
   * TODO: REMOVE! In Future
   */
  public void randomLoanCap() {
    if (mLoanCap <= 0.0f) {
      // Determine the random rating
      int loanMin = 1;
      int loanMax = 5;
      int loanCap = ThreadLocalRandom.current().nextInt(loanMin, loanMax + 1) * 10000;

      // The update statement
      String updateSql = new UpdateBuilder(getTable())
          .set(getColumn(LOAN_CAP) + "=" + Integer.toString(loanCap))
          .where(getColumn(ID) + "=" + Long.toString(mId))
          .toString();

      // Execute the update statement
      try (Connection conn = SQLManager.getConnection()) {
        conn.prepareStatement(updateSql).executeUpdate();
      } catch (SQLException e) {
        throw new RuntimeException("Unable to update the borrower to randomly provide a loan cap with SQL", e);
      }
    }
  }

  /**
   * Updates this borrower information in the database. Requires that this borrower is valid with
   * a valid reference ID
   * @return TRUE if updated. FALSE otherwise
   */
  public boolean updateDatabase() {
    if(mEmployer != null && mJobTitle != null && mPhone != null) {
      // Build the statement
      String updateSql = new UpdateBuilder(getTable())
          .set(getColumn(EMPLOYER) + "=?")
          .set(getColumn(JOB_TITLE) + "=?")
          .set(getColumn(PHONE) + "=?")
          .where(getColumn(ID) + "=" + mId)
            .toString();

      // Try to execute against the connection
      try (Connection conn = SQLManager.getConnection()) {
        boolean success = false;
        conn.setAutoCommit(false);

        try {
          // Update the parent
          if (updateDatabase(conn)) {
            // Borrower specific detail statement
            try (PreparedStatement statement = conn.prepareStatement(updateSql)) {
              statement.setString(1, mEmployer);
              statement.setString(2, mJobTitle);
              statement.setString(3, mPhone);

              // Update the borrower
              if (statement.executeUpdate() == 1) {
                success = true;
              }
            }
          }
        } catch (SQLException e) {
          throw new RuntimeException("Unable to update the borrower info with SQL", e);
        }

        // Depending on the result, either commit or rollback
        if (success) {
          conn.commit();
          return true;
        } else {
          conn.rollback();
        }
      } catch (SQLException e) {
        throw new RuntimeException("Unable to transact the borrower info with SQL", e);
      }
    }
    return false;
  }

  /**
   * Takes an editable borrower object received through the API and updates this class with the new
   * information
   * @param editable the editable data to match
   */
  public void updateFromEditable(BorrowerEditable editable) {
    super.updateFromEditable(editable);

    mPhone = editable.mPhone;
    mEmployer = editable.mEmployer;
    mJobTitle = editable.mJobTitle;
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
    return new Borrower().buildSelectRefSql(reference, true, userIdColumn);
  }
}
