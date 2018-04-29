package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.db.InsertBuilder;
import com.gncompass.serverfront.db.SelectBuilder;
import com.gncompass.serverfront.db.SQLManager;
import com.gncompass.serverfront.util.Currency;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoanPayment extends TransactionDetail {
  // Database name
  private static final String TABLE_NAME = "LoanPayments";

  // Database column names
  private static final String ID = "id";
  private static final String TYPE = "type";
  private static final String LOAN = "loan";
  private static final String INTEREST = "interest";
  private static final String DUE_DATE = "due_date";

  // Database parameters
  //public long mId = 0L;
  //public int mType = 0;
  //public long mLoanId = 0L;
  public Currency mInterest = null;
  public Date mDueDate = null;

  public LoanPayment() {
  }

  public LoanPayment(ResultSet rs) throws SQLException {
    updateFromFetch(rs);
  }

  public LoanPayment(Currency amount, Currency interest, Date dueDate) {
    super(amount);
    mInterest = interest;
    mDueDate = dueDate;
  }

  /*=============================================================
   * PRIVATE FUNCTIONS
   *============================================================*/

   /**
    * Build the select SQL for all properties related to the loan payment for a tied loan
    * @param loan the loan that determines which payments to display
    * @return the SelectBuilder reference object
    */
  private SelectBuilder buildSelectSql(Loan loan) {
    return buildSelectSql(loan, false, null);
  }

  /**
   * Build the select SQL for all properties related to the loan payment. Allows for choosing
   * between JOIN or FROM for how this table is connected
   * @param loan the loan that determines which payments to display
   * @param isJoin TRUE if is JOIN. FALSE if is FROM
   * @param joinIdColumn if JOIN, a join id column defines the matching ON column to join for the
   *                     parent ID
   * @return the SelectBuilder reference object
   */
  private SelectBuilder buildSelectSql(Loan loan, boolean isJoin, String joinIdColumn) {
    SelectBuilder selectBuilder =
        super.buildSelectParentSql(getColumn(ID), getColumn(TYPE), isJoin ? joinIdColumn : null)
        //.column(getColumn(ID))
        //.column(getColumn(TYPE))
        //.column(getColumn(LOAN))
        .column(getColumn(INTEREST))
        .column(getColumn(DUE_DATE));

    // Where and core join/from
    String primaryWhere = getColumn(LOAN) + "=" + Long.toString(loan.mId);
    if (isJoin) {
      selectBuilder
          .join(getTable(), primaryWhere, true);
    } else {
      selectBuilder
          .from(getTable())
          .where(primaryWhere);
    }

    // Ordering and return
    return selectBuilder.orderBy(getColumn(DUE_DATE));
  }

  /*=============================================================
   * PROTECTED FUNCTIONS
   *============================================================*/

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
   * Updates the loan payment info from the result set provided. This assumes it was fetched
   * appropriately by the SQL function
   * @param resultSet the result set to pull the data from. This will not call .next()
   * @throws SQLException if the data is unexpected in the result set
   */
  @Override
  void updateFromFetch(ResultSet resultSet) throws SQLException {
    super.updateFromFetch(resultSet);

    //mId = resultSet.getLong(getColumn(ID));
    //mType = resultSet.getInt(getColumn(TYPE));
    //mLoanId = resultSet.getLong(getColumn(LOAN));
    mInterest = new Currency(resultSet.getDouble(getColumn(INTEREST)));
    mDueDate = resultSet.getDate(getColumn(DUE_DATE));
  }

  /*=============================================================
   * PUBLIC FUNCTIONS
   *============================================================*/

  /**
   * Adds this loan payment for the provided loan to the database
   * @param loan the loan to tie the payment to
   * @return TRUE if the loan payment was successfully added. FALSE otherwise
   */
  public boolean addToLoan(Loan loan) {
    if (mInterest != null && mDueDate != null) {
      // Create the loan payment insert statement (done before it is needed to prevent holding
      // the connection for longer than needed)
      String insertSql = new InsertBuilder(getTable())
          .set(ID, "LAST_INSERT_ID()")
          .set(LOAN, Long.toString(loan.mId))
          .set(INTEREST, Double.toString(mInterest.doubleValue()))
          .set(DUE_DATE, "FROM_UNIXTIME(" + TimeUnit.MILLISECONDS.toSeconds(mDueDate.getTime()) + ")")
          .toString();

      // Try to fetch a connection
      try (Connection conn = SQLManager.getConnection()) {
        boolean success = false;
        conn.setAutoCommit(false);

        try {
          // Insert the transaction detail (parent) first
          if (super.addToDatabase(conn)) {
            // Insert the connected loan payment portion
            if (conn.prepareStatement(insertSql).executeUpdate() == 1) {
              success = true;
            }
          }
        } catch (SQLException e) {
          throw new RuntimeException("Unable to add the new loan payment for the loan", e);
        }

        // Depending on the result, either commit or rollback
        if (success) {
          conn.commit();
          return true;
        } else {
          conn.rollback();
        }
      } catch (SQLException e) {
        throw new RuntimeException("Unable to transact the new loan payment for the loan", e);
      }
    }
    return false;
  }

  /**
   * Returns the API model relating to the database model
   * @return the API model for a loan payment
   */
  public com.gncompass.serverfront.api.model.LoanPayment getApiModel() {
    com.gncompass.serverfront.api.model.LoanPayment loanPayment =
            new com.gncompass.serverfront.api.model.LoanPayment(mInterest.doubleValue(),
                                                                mDueDate.getTime());
    updateModelFromParent(loanPayment);
    return loanPayment;
  }

  /**
   * Returns the principal portion of this payment
   * @return the principal portion
   */
  public Currency getPrincipal() {
    if (mAmount != null && mInterest != null) {
      Currency principal = mAmount.subtract(mInterest);
      if (!principal.lessThanZero()) {
        return principal;
      }
    }
    return new Currency();
  }

  /**
   * Returns the transaction type of this loan payment
   * @return the transaction type enum
   */
  @Override
  public TransactionType getTransactionType() {
    return TransactionType.LOAN_PAYMENT;
  }

  /*=============================================================
   * STATIC FUNCTIONS
   *============================================================*/

  /**
   * Fetches the list of all loan payments for the provided loan. It is ordered by the due date
   * @param conn the connection to fetch the payment info through
   * @param loan the loan object to fetch for
   * @return the stack of loan payments tied to the loan. Empty list if none found
   * @throws SQLException exception on failed fetch
   */
  public static List<LoanPayment> getAllForLoan(Connection conn, Loan loan) throws SQLException {
    List<LoanPayment> loanPayments = new ArrayList<>();

    // Build the query
    String selectSql = new LoanPayment().buildSelectSql(loan).toString();

    // Try to execute against the connection
    try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
      while (rs.next()) {
        loanPayments.add(new LoanPayment(rs));
      }
    }

    return loanPayments;
  }
}
