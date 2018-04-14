package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.db.SelectBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Transaction extends AbstractObject {
  // Database name
  private static final String TABLE_NAME = "Transactions";

  // Database column names
  //private static final String ID = "id";
  //private static final String REFERENCE = "reference";
  private static final String REGISTERED = "registered";
  //private static final String USER_ID = "user_id";
  //private static final String DESCRIPTION = "description";
  private static final String ITEM = "item";
  //private static final String BALANCE = "balance";

  // Database parameters
  //public long mId = 0;
  //public String mReference = null;
  public Timestamp mRegistered = null;
  //public long mUserId = 0L;
  //public String mDescription = null;
  //public long mItemId = 0L;
  //public Currency mBalance = null;

  public Transaction() {
  }

  /*=============================================================
   * PRIVATE FUNCTIONS
   *============================================================*/

  /**
   * Adds the columns to the select builder provided
   * @param selectBuilder the select builder to add to
   * @return the modified select builder
   */
  private SelectBuilder addColumns(SelectBuilder selectBuilder) {
    return selectBuilder.column(getColumn(REGISTERED));
  }

  /**
   * Extracts the registered date from the result set. If it is null, null is returned (Internal)
   * @param rs the result set to extract from
   * @return the timestamp. NULL if no tied transaction
   * @throws SQLException exception on failed to fetch (no column found likely)
   */
  private Timestamp extractRegisteredInternal(ResultSet rs) throws SQLException {
    return rs.getTimestamp(getColumn(REGISTERED));
  }

  /**
   * Adds a join of this table to an existing select statement on the column provided and attaches
   * any relevant column data into the select. This is an optional left join
   * @param selectBuilder the select builder to add to
   * @param ratingIdColumn the column in the main table select to join to
   */
  private SelectBuilder joinOnItemInternal(SelectBuilder selectBuilder, String itemIdColumn) {
    return addColumns(selectBuilder.leftJoin(getTable(), getColumn(ITEM) + "=" + itemIdColumn));
  }

  /*=============================================================
   * PROTECTED FUNCTIONS
   *============================================================*/

  /**
   * Updates the transaction info from the result set provided. This assumes it was fetched
   * appropriately by the SQL function
   * @param resultSet the result set to pull the data from. This will not call .next()
   * @throws SQLException if the data is unexpected in the result set
   */
  @Override
  protected void updateFromFetch(ResultSet resultSet) throws SQLException {
    throw new RuntimeException("Update from fetch for Transaction not implemented");
  }

  /*=============================================================
   * PUBLIC FUNCTIONS
   *============================================================*/

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
   * Extracts the registered date from the result set. If it is null, null is returned (External)
   * @param rs the result set to extract from
   * @return the timestamp. NULL if no tied transaction
   * @throws SQLException exception on failed to fetch (no column found likely)
   */
  static Timestamp extractRegistered(ResultSet rs) throws SQLException {
    return new Transaction().extractRegisteredInternal(rs);
  }

  /**
   * Adds a left join statement to the select builder provided connecting the transaction table
   * to the caller
   * @param selectBuilder the select builder to add the join information to
   * @param itemIdColumn the column in the main table that will tie to the item index column
   * @return the select builder returned with the modifications
   */
  static SelectBuilder joinOnItem(SelectBuilder selectBuilder, String itemIdColumn) {
    return new Transaction().joinOnItemInternal(selectBuilder, itemIdColumn);
  }
}
