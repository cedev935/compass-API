package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.db.SelectBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Rating extends AbstractObject {
  // Database name
  private static final String TABLE_NAME = "Ratings";

  // Database column names
  private static final String ID = "id";
  //private static final String CODE = "code";
  //private static final String NAME = "name";
  private static final String LOAN_RATE = "loan_rate";
  //private static final String RETURN_RATE = "return_rate";
  //private static final String INITIAL_CAP = "initial_cap";

  // Database parameters
  //public long mId = 0;
  //public String mCode = null;
  //public String mName = null;
  public double mLoanRate = 0.0d;
  //public float mReturnRate = 0.0f;
  //public float mInitialCap = 0.0f;

  public Rating() {
  }

  public Rating(ResultSet rs) throws SQLException {
    updateFromFetch(rs);
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
    return selectBuilder.column(getColumn(LOAN_RATE));
  }

  /**
   * Adds a join of this table to an existing select statement on the column provided and attaches
   * any relevant column data into the select. This is an optional left join
   * @param selectBuilder the select builder to add to
   * @param ratingIdColumn the column in the main table select to join to
   */
  private SelectBuilder joinToSelectSql(SelectBuilder selectBuilder, String ratingIdColumn) {
    return addColumns(selectBuilder.leftJoin(getTable(), getColumn(ID) + "=" + ratingIdColumn));
  }

  /*=============================================================
   * PROTECTED FUNCTIONS
   *============================================================*/

  /**
   * Updates the rating info from the result set provided. This assumes it was fetched
   * appropriately by the SQL function
   * @param resultSet the result set to pull the data from. This will not call .next()
   * @throws SQLException if the data is unexpected in the result set
   */
  @Override
  protected void updateFromFetch(ResultSet resultSet) throws SQLException {
    mLoanRate = resultSet.getDouble(getColumn(LOAN_RATE));
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
   * Adds a join statement to the select builder provided connecting the rating table to the caller
   * @param selectBuilder the select builder to add the join information to
   * @param ratingIdColumn the column in the main table that will tie to the ID index column
   * @return the select builder returned with the modifications
   */
  static SelectBuilder join(SelectBuilder selectBuilder, String ratingIdColumn) {
    return new Rating().joinToSelectSql(selectBuilder, ratingIdColumn);
  }
}
