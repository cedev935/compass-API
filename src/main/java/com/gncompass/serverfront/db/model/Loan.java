package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.api.model.LoanInfo;
import com.gncompass.serverfront.api.model.LoanSummary;
import com.gncompass.serverfront.db.InsertBuilder;
import com.gncompass.serverfront.db.SelectBuilder;
import com.gncompass.serverfront.db.SQLManager;
import com.gncompass.serverfront.util.Currency;
import com.gncompass.serverfront.util.PaymentHelper;
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
  private static final String BANK = "bank";
  private static final String PRINCIPAL = "principal";
  private static final String RATING = "rating";
  private static final String RATE = "rate";
  private static final String AMORTIZATION = "amortization";
  private static final String FREQUENCY = "frequency";
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
  public BankConnection mBankConnection = null;
  public LoanAmortization mLoanAmortization = null;
  public LoanFrequency mLoanFrequency = null;
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

  public Loan(Currency principal, int ratingId, double rate) {
    mPrincipal = principal;
    mRatingId = ratingId;
    mRate = rate;

    mReferenceUuid = UUID.randomUUID();
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
   * Adds this loan for the provided borrower to the database
   * @param borrower the borrower to tie this loan to
   * @return TRUE if successfully added. FALSE otherwise
   */
  public boolean addForBorrower(Borrower borrower) {
    if (mReferenceUuid != null && borrower != null && mBankConnection != null && mPrincipal != null
        && mRate > 0.0d && mLoanAmortization != null && mLoanFrequency != null) {
      // Create the loan insert statement
      // This also defines the start date before being fulfilled (TEMP)
      String insertSql = new InsertBuilder(getTable())
          .set(REFERENCE, UuidHelper.getHexFromUUID(mReferenceUuid, true))
          .set(BORROWER, Long.toString(borrower.mId))
          .set(BANK, Long.toString(mBankConnection.mId))
          .set(PRINCIPAL, Double.toString(mPrincipal.doubleValue()))
          .set(RATING, Integer.toString(mRatingId))
          .set(RATE, Double.toString(mRate))
          .set(AMORTIZATION, Long.toString(mLoanAmortization.mId))
          .set(FREQUENCY, Long.toString(mLoanFrequency.mId))
          .set(START_DATE, "CURDATE()")
          .toString();

      // Attempt the insert against a connection
      try (Connection conn = SQLManager.getConnection()) {
        if (conn.prepareStatement(insertSql).executeUpdate() == 1) {
          mStartDate = new Date(new java.util.Date().getTime()); // TEMP
          mCreated = new Timestamp(mStartDate.getTime());

          // TEMP. Only required due to special MVP additions (see LoanCreate functionality)
          try (ResultSet rs = conn.prepareStatement("SELECT LAST_INSERT_ID()").executeQuery()) {
            if (rs.next()) {
              mId = rs.getLong(1);
              return true;
            }
          }
        }
      } catch (SQLException e) {
        throw new RuntimeException("Unable to create a new loan with SQL", e);
      }
    }

    return false;
  }

  /**
   * Generates the next payment for the loan based on the loan properties and the previous payment
   * information available
   * @return TRUE if successful. FALSE otherwise
   */
  public boolean generateNextPayment() {
    // Determine the payment per period
    double totalYears = mLoanAmortization.getTotalYears();
    int periodsPerYear = mLoanFrequency.getPeriodsPerYear();
    double dailyInterestRate = mRate / PaymentHelper.DAYS_PER_YEAR;
    double ratePerPeriod = Math.pow(1 + dailyInterestRate,
                                    (double) PaymentHelper.DAYS_PER_YEAR / periodsPerYear) - 1;
    Currency paymentPerPeriod = PaymentHelper.paymentPerPeriod(
                                mPrincipal, ratePerPeriod, (int) (periodsPerYear * totalYears));

    // Calculate the total principal payments already active to determine a pending balance
    Date lastPaymentDate = mStartDate;
    Currency principalBalance = new Currency(mPrincipal);
    if (mLoanPayments != null) {
      for (LoanPayment lp : mLoanPayments) {
        principalBalance = principalBalance.subtract(lp.getPrincipal());
        lastPaymentDate = lp.mDueDate;
      }
    }

    // Determine the next date for a payment based on the previous payment
    Date nextPaymentDate = mLoanFrequency.getNextPaymentDate(
                                    mStartDate, mLoanPayments != null ? mLoanPayments.size() : 0);

    // With the current balance, determine what the next interest payment should be for
    long daysInPeriod = PaymentHelper.daysInPeriod(lastPaymentDate, nextPaymentDate);
    Currency interestAmount = new Currency(
                                daysInPeriod * dailyInterestRate * principalBalance.doubleValue());

    // With the balance, determine what the next payment should be for
    Currency interestWithPrincipal = principalBalance.add(interestAmount);
    Currency paymentAmount = null;
    if (interestWithPrincipal.lessThan(paymentPerPeriod)) {
      paymentAmount = interestWithPrincipal;
    } else {
      paymentAmount = paymentPerPeriod;
    }

    // Generate the payment
    LoanPayment payment = new LoanPayment(paymentAmount, interestAmount, nextPaymentDate);
    if (mLoanPayments == null) {
      mLoanPayments = new ArrayList<>();
    }
    mLoanPayments.add(payment);
    return payment.addToLoan(this);
  }

  /**
   * Returns the API info model relating to the database model
   * @return the API info for a loan
   */
  public LoanInfo getApiInfo() {
    LoanInfo loanInfo = new LoanInfo(mReferenceUuid.toString(), mCreated.getTime(),
                                     mBankConnection.getApiSummary(), mPrincipal.doubleValue(),
                                     mRatingId, mRate, mLoanAmortization.getApiModel(),
                                     mLoanFrequency.getApiModel());
    if (mStartDate != null) {
      loanInfo.mStartedTime = mStartDate.getTime();

      // Balance
      calculateBalance();
      if (mBalance != null) {
        loanInfo.mBalance = mBalance.doubleValue();
      }

      // Payments
      for (LoanPayment lp : mLoanPayments) {
        loanInfo.mPayments.add(lp.getApiModel());
      }

      // Next Payment
      if (mNextPayment != null) {
        loanInfo.mNextPayment = mNextPayment.getApiModel();
      }
    }

    return loanInfo;
  }

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
        loanSummary.mNextPayment = mNextPayment.getApiModel();
      }
    }
    return loanSummary;
  }

  /**
   * Fetches the loan information from the database
   * @param borrower the borrower object to fetch for
   * @param reference the reference UUID to the loan
   * @return the loan object with the information fetched. If not found, return NULL
   */
  public Loan getLoan(Borrower borrower, String reference) {
    // Build the query
    SelectBuilder selectBuilder = buildSelectSql(borrower, reference);
    BankConnection.join(selectBuilder, getColumn(BANK));
    LoanAmortization.join(selectBuilder, getColumn(AMORTIZATION));
    LoanFrequency.join(selectBuilder, getColumn(FREQUENCY));
    String selectSql = selectBuilder.toString();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        if (rs.next()) {
          updateFromFetch(rs);
          mBankConnection = new BankConnection(rs);
          mLoanAmortization = new LoanAmortization(rs);
          mLoanFrequency = new LoanFrequency(rs);
          fetchAllPayments(conn);
          return this;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Unable to fetch the loan reference with SQL", e);
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
