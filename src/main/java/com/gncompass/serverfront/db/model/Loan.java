package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.api.model.LoanSummary;
import com.gncompass.serverfront.db.SelectBuilder;
import com.gncompass.serverfront.db.SQLManager;
import com.gncompass.serverfront.util.Currency;
import com.gncompass.serverfront.util.UuidHelper;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Loan extends AbstractObject {
  // Database name
  private static final String TABLE_NAME = "Loans";

  // Database column names
  private static final String ID = "id";
  private static final String REFERENCE = "reference";
  private static final String BORROWER = "borrower";
  private static final String CREATED = "created";
  //private static final String BANK = "bank";
  private static final String PRINCIPAL = "principal";
  private static final String RATING = "rating";
  private static final String RATE = "rate";
  //private static final String AMORTIZATION = "amortization";
  //private static final String FREQUENCY = "frequency";
  private static final String START_DATE = "start_date";

  // Database parameters
  public long mId = 0;
  public byte[] mReference = null;
  //public long mBorrowerId = 0;
  public Timestamp mCreated = null;
  //public int mBankId = 0;
  public Currency mPrincipal = null;
  public int mRatingId = 0;
  public double mRate = 0.0d;
  //public int mAmortizationId = 0;
  //public int mFrequencyId = 0;
  public Date mStartDate = null;

  // Internals
  public List<LoanPayment> mLoanPayments = null;
  public UUID mReferenceUuid = null;

  // Calculated
  public Currency mBalance = null;
  public LoanPayment mNextPayment = null;

  public Loan() {
  }

  public Loan(ResultSet rs) throws SQLException {
    updateFromFetch(rs);
  }

  /*=============================================================
   * PRIVATE FUNCTIONS
   *============================================================*/

  /**
   * Build the select SQL for all properties related to the loan. Both parameters cannot be
   * NULL
   * @param borrower the borrower reference. Can be null
   * @param reference the bank connection UUID reference. Can be null
   * @return the SelectBuilder reference object
   */
  private SelectBuilder buildSelectSql(Borrower borrower, String reference) {
    if (borrower == null && reference == null) {
      throw new RuntimeException(
                "Both the borrower and the reference are null on select loan. Not permitted");
    }

    // Build the select statement
    SelectBuilder selectBuilder = buildSelectSql();
    if (borrower != null) {
      selectBuilder.where(getColumn(BORROWER) + "=" + Long.toString(borrower.mId));
    }
    if (reference != null) {
      selectBuilder.where(getColumn(REFERENCE) + "=" + UuidHelper.getHexFromUUID(reference, true));
    }
    return selectBuilder;
  }

  /**
   * Build the select SQL for all properties related to a loan from the direct table
   * @return the SelectBuilder reference object
   */
  private SelectBuilder buildSelectSql() {
    return new SelectBuilder(getTable())
        .column(getColumn(ID))
        .column(getColumn(REFERENCE))
        .column(getColumn(CREATED))
        .column(getColumn(PRINCIPAL))
        .column(getColumn(RATING))
        .column(getColumn(RATE))
        .column(getColumn(START_DATE));
  }

  /**
   * Calculates the pending balance information about the loan using the existing available
   * fetched info
   * TODO: This should add accumulated interest for overdo payments
   */
  private void calculateBalance() {
    if (mLoanPayments != null) {
      Currency principalPaid = new Currency();
      Currency amountDue = new Currency();
      Currency interestDue = new Currency();
      LoanPayment lastPayment = null;

      // Run through all the existing payments
      for (LoanPayment lp : mLoanPayments) {
        if (lp.isPaid()) {
          principalPaid = principalPaid.add(lp.getPrincipal());
        } else {
          amountDue = amountDue.add(lp.mAmount);
          interestDue = interestDue.add(lp.mInterest);
          lastPayment = lp;
        }
      }

      // Tally and cache
      mBalance = mPrincipal.subtract(principalPaid);
      if (mBalance.lessThanZero()) {
        mBalance.setToZero();
      }
      if (lastPayment != null) {
        mNextPayment = new LoanPayment(amountDue, interestDue, lastPayment.mDueDate);
      } else {
        mNextPayment = null;
      }
    }
  }

  /**
   * Fetches all payments tied to this particular loan and stores them in the class
   * @param conn the connection to fetch with
   * @throws SQLException exception on fetch failure
   */
  private void fetchAllPayments(Connection conn) throws SQLException {
    mLoanPayments = LoanPayment.getAllForLoan(conn, this);
  }

  /*=============================================================
   * PROTECTED FUNCTIONS
   *============================================================*/

  /**
   * Updates the loan info from the result set provided. This assumes it was fetched
   * appropriately by the SQL function
   * @param resultSet the result set to pull the data from. This will not call .next()
   * @throws SQLException if the data is unexpected in the result set
   */
  @Override
  protected void updateFromFetch(ResultSet resultSet) throws SQLException {
    mId = resultSet.getLong(getColumn(ID));
    mReference = resultSet.getBytes(getColumn(REFERENCE));
    mCreated = resultSet.getTimestamp(getColumn(CREATED));
    mPrincipal = new Currency(resultSet.getDouble(getColumn(PRINCIPAL)));
    mRatingId = resultSet.getInt(getColumn(RATING));
    mRate = resultSet.getDouble(getColumn(RATE));
    mStartDate = resultSet.getDate(getColumn(START_DATE));

    // Determine the reference
    mReferenceUuid = UuidHelper.getUUIDFromBytes(mReference);
  }

  /*=============================================================
   * PUBLIC FUNCTIONS
   *============================================================*/

  /**
   * Returns the API summary model relating to the database model
   * @return the API summary for a loan
   */
  public LoanSummary getApiSummary() {
    LoanSummary loanSummary = new LoanSummary(
                                      mReferenceUuid.toString(), mPrincipal.doubleValue(), mRate);
    if (mStartDate != null) {
      loanSummary.mStartedTime = mStartDate.getTime();

      // Balance
      calculateBalance();
      if (mBalance != null) {
        loanSummary.mBalance = mBalance.doubleValue();
      }

      // Next Payment
      if (mNextPayment != null) {
        // TODO!
      }
    }
    return loanSummary;
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
   * Fetches the list of all loans for the provided borrower
   * @param borrower the borrower object to fetch for
   * @return the stack of loans tied to the borrower. Empty list if none found
   */
  public static List<Loan> getAllForBorrower(Borrower borrower) {
    List<Loan> loans = new ArrayList<>();

    // Build the query
    String selectSql = new Loan().buildSelectSql(borrower, null).toString();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        while (rs.next()) {
          Loan loan = new Loan(rs);
          loan.fetchAllPayments(conn);
          loans.add(loan);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Unable to fetch the list of loans for the borrower with SQL", e);
    }

    return loans;
  }
}
