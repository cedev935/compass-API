package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.db.SelectBuilder;
import com.gncompass.serverfront.util.Currency;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public abstract class TransactionDetail extends AbstractObject {
  // Database name
  protected static final String TABLE_NAME = "TransactionDetails";

  // Database column names
  private static final String ID = "id";
  private static final String TYPE = "type";
  private static final String AMOUNT = "amount";

  // Transaction detail types enumerator (all inherited children)
  public static enum TransactionType {
    BANK_TRANSFER(1),
    INVESTMENT_RETURN(2),
    LOAN_PAYMENT(3),
    ADJUSTMENT(4),
    INVESTMENT_FUND(5),
    LOAN_FULFILLMENT(6);

    private final int value;

    private TransactionType(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }

  // Database parameters
  public long mId = 0;
  //public int mType = 0;
  public Currency mAmount = null;

  // Internals
  public Timestamp mPaidDate = null;

  protected TransactionDetail() {
  }

  public TransactionDetail(Currency amount) {
    mAmount = amount;
  }

  /*=============================================================
   * PROTECTED FUNCTIONS
   *============================================================*/

  /**
   * Build the select SQL for all properties related to the transaction detail
   * @param childIdColumn the child ID column for the join
   * @param childTypeColumn the child type column for the join
   * @return the SelectBuilder reference object
   */
  protected SelectBuilder buildSelectParentSql(String childIdColumn, String childTypeColumn) {
    return buildSelectParentSql(childIdColumn, childTypeColumn, null);
  }

  /**
   * Build the select SQL for all properties related to the transaction detail
   * @param childIdColumn the child ID column for the join
   * @param childTypeColumn the child type column for the join
   * @param idColumn the parent ID match column for extra join verification (optional).
   *                 For example, this could be the Transaction table column item ID
   * @return the SelectBuilder reference object
   */
  protected SelectBuilder buildSelectParentSql(String childIdColumn, String childTypeColumn,
                                               String idColumn) {
    SelectBuilder selectBuilder = new SelectBuilder()
        .join(getTableParent(),
              getColumnParent(ID) + "=" + childIdColumn + " AND " +
              getColumnParent(TYPE) + "=" + childTypeColumn +
              (idColumn != null ? " AND " + getColumnParent(ID) + "=" + idColumn : ""))
        .column(getColumnParent(ID))
        //.column(getColumnParent(TYPE))
        .column(getColumnParent(AMOUNT));

    // Add transaction join to determine if this item has been paid (only if this isn't a join)
    if (idColumn != null) {
      return selectBuilder;
    } else {
      return Transaction.joinOnItem(selectBuilder, getColumnParent(ID));
    }
  }

  /**
   * Returns the parent table name (this abstract class)
   * @return the object parent table name
   */
  @Override
  protected String getTableParent() {
    return TABLE_NAME;
  }

  /*=============================================================
   * PACKAGE-PRIVATE FUNCTIONS
   *============================================================*/

  /**
   * Updates the transaction detail info from the result set provided. This assumes it was fetched
   * appropriately by one of the child connected tables
   * @param resultSet the result set to pull the data from. This will not call .next()
   * @throws SQLException if the data is unexpected in the result set
   */
  @Override
  void updateFromFetch(ResultSet resultSet) throws SQLException {
    mId = resultSet.getLong(getColumnParent(ID));
    //mType = resultSet.getInt(getColumnParent(TYPE));
    mAmount = new Currency(resultSet.getDouble(getColumnParent(AMOUNT)));

    mPaidDate = Transaction.extractRegistered(resultSet);
  }

  /*=============================================================
   * PUBLIC FUNCTIONS
   *============================================================*/

  /**
   * Returns the transaction detail table ID
   * @return the ID
   */
  public long getId() {
    return mId;
  }

  /**
   * Returns the transaction type (abstract)
   * @return the transaction type enum
   */
  public abstract TransactionType getTransactionType();

  /**
   * Returns if the transaction detail item has been paid (associated transaction)
   * @return TRUE if paid. FALSE otherwise
   */
  public boolean isPaid() {
    return mPaidDate != null;
  }
}
