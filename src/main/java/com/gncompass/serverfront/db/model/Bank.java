package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.db.SelectBuilder;
import com.gncompass.serverfront.db.SQLManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Bank extends AbstractObject {
  // Database name
  private static final String TABLE_NAME = "Banks";

  // Database column names
  private static final String ID = "id";
  private static final String CODE = "code";
  private static final String NAME = "name";
  private static final String COUNTRY_ID = "country";
  private static final String ENABLED = "enabled";

  // Database parameters
  public long mId = 0;
  public int mCode = 0;
  public String mName = null;
  public long mCountryId = 0;
  public boolean mEnabled = false;

  public Bank() {
  }

  public Bank(ResultSet rs) throws SQLException {
    updateFromFetch(rs);
  }

  /*=============================================================
   * PRIVATE FUNCTIONS
   *============================================================*/

  /**
   * Build the select SQL for all properties related to a bank
   * @return the SelectBuilder reference object
   */
  private SelectBuilder buildSelectSql() {
    return new SelectBuilder(getTable())
        .column(getColumn(ID))
        .column(getColumn(CODE))
        .column(getColumn(NAME))
        .column(getColumn(COUNTRY_ID))
        .column(getColumn(ENABLED));
  }

  /**
   * Builds a select SQL for all properties related to a bank and fetches all banks tied to the
   * given country code
   * @param countryCode the two digit country code to fetch the banks for
   * @return the SelectBuilder reference object
   */
  private SelectBuilder buildSelectSqlForCountry(String countryCode) {
    return Country.join(buildSelectSql(), getColumn(COUNTRY_ID), countryCode);
  }

  /*=============================================================
   * PROTECTED FUNCTIONS
   *============================================================*/

  /**
   * Updates the bank info from the result set provided. This assumes it was fetched
   * appropriately by the SQL function
   * @param resultSet the result set to pull the data from. This will not call .next()
   * @throws SQLException if the data is unexpected in the result set
   */
  @Override
  protected void updateFromFetch(ResultSet resultSet) throws SQLException {
    mId = resultSet.getLong(getColumn(ID));
    mCode = resultSet.getInt(getColumn(CODE));
    mName = resultSet.getString(getColumn(NAME));
    mCountryId = resultSet.getLong(getColumn(COUNTRY_ID));
    mEnabled = resultSet.getBoolean(getColumn(ENABLED));
  }

  /*=============================================================
   * PUBLIC FUNCTIONS
   *============================================================*/

  /**
   * Returns the API model relating to the database model
   * @return the API model for a bank
   */
  public com.gncompass.serverfront.api.model.Bank getApiModel() {
    return new com.gncompass.serverfront.api.model.Bank(mId, mCode, mName);
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
   * Returns the select all banks tied to the country code statement
   * @return the SelectBuilder reference object
   */
  private static SelectBuilder buildSelectForCountry(String countryCode) {
    return new Bank().buildSelectSqlForCountry(countryCode);
  }

  /**
   * Returns a list of all banks for the given country code
   * @param countryCode the two digit ISO2 country code to fetch for
   * @return the list of all banks for the country. Blank if country not found
   */
  public static List<Bank> getAllForCountry(String countryCode) {
    List<Bank> banks = new ArrayList<>();

    // Build the query
    String selectSql = buildSelectForCountry(countryCode).toString();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        while (rs.next()) {
          banks.add(new Bank(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Unable to fetch the banks tied to the country with SQL", e);
    }

    return banks;
  }
}
