package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.db.SelectBuilder;
import com.gncompass.serverfront.db.SQLManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoanFrequency extends AbstractObject {
  // Database name
  private static final String TABLE_NAME = "LoanFrequencies";

  // Database column names
  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String DAYS = "days";
  private static final String PER_MONTH = "per_month";

  // Database parameters
  public long mId = 0;
  public String mName = null;
  public int mDays = 0;
  public int mPerMonth = 0;

  public LoanFrequency() {
  }

  public LoanFrequency(ResultSet rs) throws SQLException {
    updateFromFetch(rs);
  }

  /*=============================================================
   * PRIVATE FUNCTIONS
   *============================================================*/

  /**
   * Build the select SQL for all properties related to all loan frequencies
   * @return the SelectBuilder reference object
   */
  private SelectBuilder buildSelectSql() {
    return new SelectBuilder(getTable())
        .column(getColumn(ID))
        .column(getColumn(NAME))
        .column(getColumn(DAYS))
        .column(getColumn(PER_MONTH));
  }

  /*=============================================================
   * PROTECTED FUNCTIONS
   *============================================================*/

  /**
   * Updates the loan frequency info from the result set provided. This assumes it was fetched
   * appropriately by the SQL function
   * @param resultSet the result set to pull the data from. This will not call .next()
   * @throws SQLException if the data is unexpected in the result set
   */
  @Override
  protected void updateFromFetch(ResultSet resultSet) throws SQLException {
    mId = resultSet.getLong(getColumn(ID));
    mName = resultSet.getString(getColumn(NAME));
    mDays = resultSet.getInt(getColumn(DAYS));
    mPerMonth = resultSet.getInt(getColumn(PER_MONTH));
  }

  /*=============================================================
   * PUBLIC FUNCTIONS
   *============================================================*/

  /**
   * Returns the API model relating to the database model
   * @return the API model for a loan frequency
   */
  public com.gncompass.serverfront.api.model.LoanFrequency getApiModel() {
    return new com.gncompass.serverfront.api.model.LoanFrequency(mId, mName, mDays, mPerMonth);
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
   * Returns the select all loan frequencies statement
   * @return the SelectBuilder reference object
   */
  private static SelectBuilder buildSelectAllSql() {
    return new LoanFrequency().buildSelectSql();
  }

  /**
   * Fetches all loan frequencies available for creating loans
   * @return the stack of loan frequencies available. Empty list if none found
   */
  public static List<LoanFrequency> getAll() {
    List<LoanFrequency> loanFrequencies = new ArrayList<>();

    // Build the query
    String selectSql = buildSelectAllSql().toString();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        while (rs.next()) {
          loanFrequencies.add(new LoanFrequency(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Unable to fetch all the loan frequencies with SQL", e);
    }

    return loanFrequencies;
  }
}
