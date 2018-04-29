package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.db.SelectBuilder;
import com.gncompass.serverfront.db.SQLManager;
import com.gncompass.serverfront.util.PaymentHelper;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
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
   * Adds the columns to the select builder provided
   * @param selectBuilder the select builder to add to
   * @return the modified select builder
   */
  private SelectBuilder addColumns(SelectBuilder selectBuilder) {
    return selectBuilder.column(getColumn(ID))
        .column(getColumn(NAME))
        .column(getColumn(DAYS))
        .column(getColumn(PER_MONTH));
  }

  /**
   * Build the select SQL for all properties related to all loan frequencies
   * @return the SelectBuilder reference object
   */
  private SelectBuilder buildSelectSql() {
    return addColumns(new SelectBuilder(getTable()));
  }

  /**
   * Adds a join statement to the select builder provided connecting the frequency table to
   * the caller. This is the internal function
   * @param selectBuilder the select builder to add the join information to
   * @param frequencyIdColumn the column in the main table that will tie to the ID index column
   * @return the select builder returned with the modifications
   */
  private SelectBuilder joinToSelect(SelectBuilder selectBuilder, String frequencyIdColumn) {
    return addColumns(selectBuilder.join(getTable(), getColumn(ID) + "=" + frequencyIdColumn));
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

  /**
   * Fetches the loan frequency information from the database based on the ID
   * @param frequencyId the unique frequency ID
   * @return the loan frequency object with the information fetched. If not found, return NULL
   */
  public LoanFrequency getForId(int frequencyId) {
    // Build the query
    String selectSql = buildSelectSql()
        .where(getColumn(ID) + "=" + Integer.toString(frequencyId))
        .toString();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        if (rs.next()) {
          updateFromFetch(rs);
          return this;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Unable to fetch the loan frequency reference with SQL", e);
    }

    return null;
  }

  /**
   * Returns the next payment date based on the start date plus the number of payment periods
   * that have already elapsed or have been processed
   * @param startDate the start date of the loan payments
   * @param periodsElapsed the number of payment periods that have been already identified
   * @return the next payment date
   */
  public Date getNextPaymentDate(Date startDate, int periodsElapsed) {
    GregorianCalendar startCalendar = new GregorianCalendar();
    startCalendar.setTime(startDate);

    // Handle the result depending on what kind of frequency it is
    if (mDays > 0) {
      startCalendar.add(GregorianCalendar.DAY_OF_MONTH, (periodsElapsed + 1) * mDays);
    } else if (mPerMonth > 0) {
      startCalendar.add(GregorianCalendar.MONTH, (int) ((float) (periodsElapsed + 1) / mPerMonth));
      if (mPerMonth > 0) {
        if (mPerMonth == 2) {
          // Every other month payment period adds a 14 day offset
          if (periodsElapsed % 2 == 0) {
            startCalendar.add(GregorianCalendar.DAY_OF_MONTH, 14);
          }
        } else {
          throw new RuntimeException(
                          "Per month frequencies larger than 2 (semi-monthly) are not supported");
        }
      }
    }

    return new Date(startCalendar.getTimeInMillis());
  }

  /**
   * Returns the number of periods per year for the frequency
   * @return period count
   */
  public int getPeriodsPerYear() {
    if (mDays > 0) {
      return PaymentHelper.DAYS_PER_YEAR / mDays;
    } else if (mPerMonth > 0) {
      return PaymentHelper.MONTHS_PER_YEAR * mPerMonth;
    }
    return 0;
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
   * Fetches all loan frequencies available for creating loans
   * @return the stack of loan frequencies available. Empty list if none found
   */
  public static List<LoanFrequency> getAll() {
    List<LoanFrequency> loanFrequencies = new ArrayList<>();

    // Build the query
    String selectSql = new LoanFrequency().buildSelectSql().toString();

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

  /**
   * Adds a join statement to the select builder provided connecting the frequency table to
   * the caller
   * @param selectBuilder the select builder to add the join information to
   * @param frequencyIdColumn the column in the main table that will tie to the ID index column
   * @return the select builder returned with the modifications
   */
  static SelectBuilder join(SelectBuilder selectBuilder, String frequencyIdColumn) {
    return new LoanFrequency().joinToSelect(selectBuilder, frequencyIdColumn);
  }
}
