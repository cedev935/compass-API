package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.db.SelectBuilder;
import com.gncompass.serverfront.db.SQLManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoanAmortization extends AbstractObject {
  // Database name
  private static final String TABLE_NAME = "LoanAmortizations";

  // Database column names
  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String MONTHS = "months";

  // Database parameters
  public long mId = 0;
  public String mName = null;
  public int mMonths = 0;

  public LoanAmortization() {
  }

  public LoanAmortization(ResultSet rs) throws SQLException {
    updateFromFetch(rs);
  }

  /*=============================================================
   * PRIVATE FUNCTIONS
   *============================================================*/

  /**
   * Build the select SQL for all properties related to all loan amortizations
   * @return the SelectBuilder reference object
   */
  private SelectBuilder buildSelectSql() {
    return new SelectBuilder(getTable())
        .column(getColumn(ID))
        .column(getColumn(NAME))
        .column(getColumn(MONTHS));
  }

  /*=============================================================
   * PROTECTED FUNCTIONS
   *============================================================*/

  /**
   * Updates the loan amortization info from the result set provided. This assumes it was fetched
   * appropriately by the SQL function
   * @param resultSet the result set to pull the data from. This will not call .next()
   * @throws SQLException if the data is unexpected in the result set
   */
  @Override
  protected void updateFromFetch(ResultSet resultSet) throws SQLException {
    mId = resultSet.getLong(getColumn(ID));
    mName = resultSet.getString(getColumn(NAME));
    mMonths = resultSet.getInt(getColumn(MONTHS));
  }

  /*=============================================================
   * PUBLIC FUNCTIONS
   *============================================================*/

  /**
   * Returns the API model relating to the database model
   * @return the API model for a loan amortization
   */
  public com.gncompass.serverfront.api.model.LoanAmortization getApiModel() {
    return new com.gncompass.serverfront.api.model.LoanAmortization(mId, mName, mMonths);
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
   * Returns the select all loan amortizations statement
   * @return the SelectBuilder reference object
   */
  private static SelectBuilder buildSelectAllSql() {
    return new LoanAmortization().buildSelectSql();
  }

  /**
   * Fetches all loan amortizations available for creating loans
   * @return the stack of loan amortizations available. Empty list if none found
   */
  public static List<LoanAmortization> getAll() {
    List<LoanAmortization> loanAmortizations = new ArrayList<>();

    // Build the query
    String selectSql = buildSelectAllSql().toString();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        while (rs.next()) {
          loanAmortizations.add(new LoanAmortization(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Unable to fetch all the loan amortizations with SQL", e);
    }

    return loanAmortizations;
  }
}
