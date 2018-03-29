package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.db.SelectBuilder;
import com.gncompass.serverfront.db.SQLManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Country extends AbstractObject {
  // Database name
  private static final String TABLE_NAME = "Countries";

  // Database column names
  private static final String ID = "id";
  private static final String CODE = "code";
  private static final String NAME = "name";
  private static final String REGION_ID = "region";
  private static final String ENABLED = "enabled";

  // Database parameters
  public long mId = 0;
  public String mCode = null;
  public String mName = null;
  public long mRegionId = 0;
  public boolean mEnabled = false;

  public Country() {
  }

  public Country(ResultSet rs) throws SQLException {
    updateFromFetch(rs);
  }

  /*=============================================================
   * PRIVATE FUNCTIONS
   *============================================================*/

  /**
   * Adds a join of this table to an existing select statement on the column provided
   * @param selectBuilder the select builder to add to
   * @param countryIdColumn the column in the main table select to join to
   * @param countryCode the two digit country code of the required country
   * @return the SelectBuilder with the modifications
   */
  private SelectBuilder joinToSelectSql(SelectBuilder selectBuilder, String countryIdColumn, String countryCode) {
    String onStatement = getColumn(ID) + "=" + countryIdColumn
             + " AND " + getColumn(CODE) + "='" + countryCode + "'";
    return selectBuilder.join(getTable(), onStatement);
  }

  /*=============================================================
   * PROTECTED FUNCTIONS
   *============================================================*/

  /**
   * Build the select SQL for all properties related to the country
   * @param id the country ID
   * @return the SelectBuilder reference object
   */
  protected SelectBuilder buildSelectSql(long id) {
    SelectBuilder selectBuilder = buildSelectSql()
        .where(getColumn(ID) + "=" + Long.toString(id));
    return selectBuilder;
  }

  /**
   * Build the select SQL for all properties related to the country
   * @param code the country code (E.g, 'CA')
   * @return the SelectBuilder reference object
   */
  protected SelectBuilder buildSelectSql(String code) {
    SelectBuilder selectBuilder = buildSelectSql();
    if (code != null) {
      selectBuilder.where(getColumn(CODE) + "='" + code + "'");
    }
    selectBuilder.where(getColumn(ENABLED) + "=1");

    return selectBuilder;
  }

  /**
   * Build the select SQL for all properties related to all countries
   * @return the SelectBuilder reference object
   */
  protected SelectBuilder buildSelectSql() {
    return new SelectBuilder(getTable())
        .column(getColumn(ID))
        .column(getColumn(CODE))
        .column(getColumn(NAME))
        .column(getColumn(REGION_ID))
        .column(getColumn(ENABLED));
  }

  /**
   * Updates the country info from the result set provided. This assumes it was fetched
   * appropriately by the SQL function
   * @param resultSet the result set to pull the data from. This will not call .next()
   * @throws SQLException if the data is unexpected in the result set
   */
  @Override
  protected void updateFromFetch(ResultSet resultSet) throws SQLException {
    mId = resultSet.getLong(getColumn(ID));
    mCode = resultSet.getString(getColumn(CODE));
    mName = resultSet.getString(getColumn(NAME));
    mRegionId = resultSet.getLong(getColumn(REGION_ID));
    mEnabled = resultSet.getBoolean(getColumn(ENABLED));
  }

  /*=============================================================
   * PUBLIC FUNCTIONS
   *============================================================*/

  /**
   * Returns the API model relating to the database model
   * @return the API model for a country
   */
  public com.gncompass.serverfront.api.model.Country getApiModel() {
    return new com.gncompass.serverfront.api.model.Country(mCode, mName);
  }

  /**
   * Returns the country that matches the ID provided
   * @param id the country ID
   * @return the country reference. NULL if not found
   */
  public Country getCountry(long id) {
    // Build the query
    String selectSql = buildSelectSql(id).toString();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        if (rs.next()) {
          updateFromFetch(rs);
          return this;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Unable to fetch the country by ID with SQL", e);
    }

    return null;
  }

  /**
   * Returns the country that matches the code provided (assuming it's enabled and exists)
   * @param code the country code (Eg, 'CA')
   * @return the country reference. NULL if not found
   */
  public Country getCountry(String code) {
    // Build the query
    String selectSql = buildSelectSql(code).toString();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        if (rs.next()) {
          updateFromFetch(rs);
          return this;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Unable to fetch the country by code with SQL", e);
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
   * Returns the select all available countries statement
   * @return the SelectBuilder reference object
   */
  protected static SelectBuilder buildSelectAvailableSql() {
    return new Country().buildSelectSql(null);
  }

  /**
   * Fetches all countries available for creating accounts
   * @return the stack of countries available. Empty list if none found
   */
  public static List<Country> getAvailable() {
    List<Country> countries = new ArrayList<>();

    // Build the query
    String selectSql = buildSelectAvailableSql().toString();

    // Try to execute against the connection
    try (Connection conn = SQLManager.getConnection()) {
      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        while (rs.next()) {
          countries.add(new Country(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Unable to fetch the available countries with SQL", e);
    }

    return countries;
  }

  /**
   * Adds a join statement to the select builder provided connecting the country table to the caller
   * @param selectBuilder the select builder to add the join information to
   * @param countryIdColumn the column in the main table that will tie to the ID index column
   * @param countryCode the two digit country code to identify the chosen country on join
   * @return the select builder returned with the modifications
   */
  static SelectBuilder join(SelectBuilder selectBuilder, String countryIdColumn,
                            String countryCode) {
    return new Country().joinToSelectSql(selectBuilder, countryIdColumn, countryCode);
  }
}
