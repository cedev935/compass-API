package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.db.InsertBuilder;
import com.gncompass.serverfront.db.SelectBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class User extends AbstractObject {
  // Database name
  protected static final String TABLE_NAME = "Users";

  // Database column names
  private static final String ID = "id";
  private static final String TYPE = "type";
  private static final String PASSWORD = "password";
  private static final String PASSWORD_DATE = "password_date";
  private static final String NAME = "name";
  private static final String ENABLED = "enabled";
  private static final String FLAGS = "flags";
  private static final String ADDRESS1 = "address1";
  private static final String ADDRESS2 = "address2";
  private static final String ADDRESS3 = "address3";
  private static final String CITY = "city";
  private static final String PROVINCE = "province";
  private static final String POST_CODE = "post_code";
  private static final String COUNTRY = "country";
  private static final String CREATED = "created";

  // User types enumerator (all inherited children)
  public static enum UserType {
    BORROWER(1),
    INVESTOR(2);

    private final int value;

    private UserType(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }

  // Database parameters
  public long mId = 0;
  //public int mType = 0;
  public String mPassword = null;
  public Timestamp mPasswordDate = null;
  public String mName = null;
  public boolean mEnabled = false;
  public int mFlags = 0;
  public String mAddress1 = null;
  public String mAddress2 = null;
  public String mAddress3 = null;
  public String mCity = null;
  public String mProvince = null;
  public String mPostCode = null;
  public long mCountryId = 0;
  public Timestamp mCreated = null;

  protected User() {
  }

  protected User(String name, String passwordHash, Country country) {
    mName = name;
    mEnabled = true;
    mAddress1 = "";
    mCity = "";
    mCountryId = country.mId;

    mPassword = passwordHash;
  }

  /*=============================================================
   * PROTECTED FUNCTIONS
   *============================================================*/

  /**
   * Build the select SQL for all properties related to the borrower
   * @param childIdColumn the child ID column for the join
   * @param childTypeColumn the child type column for the join
   * @return the SelectBuilder reference object
   */
  protected SelectBuilder buildSelectParentSql(String childIdColumn, String childTypeColumn) {
    return buildSelectParentSql(childIdColumn, childTypeColumn, null);
  }

  /**
   * Build the select SQL for all properties related to the borrower
   * @param childIdColumn the child ID column for the join
   * @param childTypeColumn the child type column for the join
   * @param idColumn the parent ID match column for the join (optional)
   * @return the SelectBuilder reference object
   */
  protected SelectBuilder buildSelectParentSql(String childIdColumn, String childTypeColumn,
                                               String idColumn) {
    SelectBuilder selectBuilder = new SelectBuilder();
    selectBuilder.join(getTableParent(),
                       getColumnParent(ID) + "=" + childIdColumn + " AND " +
                       getColumnParent(TYPE) + "=" + childTypeColumn +
                       (idColumn != null ? " AND " + getColumnParent(ID) + "=" + idColumn : ""))
        .column(getColumnParent(ID))
        //.column(getColumnParent(TYPE))
        .column(getColumnParent(PASSWORD))
        .column(getColumnParent(PASSWORD_DATE))
        .column(getColumnParent(NAME))
        .column(getColumnParent(ENABLED))
        .column(getColumnParent(FLAGS))
        .column(getColumnParent(ADDRESS1))
        .column(getColumnParent(ADDRESS2))
        .column(getColumnParent(ADDRESS3))
        .column(getColumnParent(CITY))
        .column(getColumnParent(PROVINCE))
        .column(getColumnParent(POST_CODE))
        .column(getColumnParent(COUNTRY))
        .column(getColumnParent(CREATED));
    return selectBuilder;
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
   * Updates the user info from the result set provided. This assumes it was fetched appropriately
   * by one of the child connected tables
   * @param resultSet the result set to pull the data from. This will not call .next()
   * @throws SQLException if the data is unexpected in the result set
   */
  @Override
  void updateFromFetch(ResultSet resultSet) throws SQLException {
    mId = resultSet.getLong(getColumnParent(ID));
    //mType = resultSet.getInt(getColumnParent(TYPE));
    mPassword = resultSet.getString(getColumnParent(PASSWORD));
    mPasswordDate = resultSet.getTimestamp(getColumnParent(PASSWORD_DATE));
    mName = resultSet.getString(getColumnParent(NAME));
    mEnabled = resultSet.getBoolean(getColumnParent(ENABLED));
    mFlags = resultSet.getInt(getColumnParent(FLAGS));
    mAddress1 = resultSet.getString(getColumnParent(ADDRESS1));
    mAddress2 = resultSet.getString(getColumnParent(ADDRESS2));
    mAddress3 = resultSet.getString(getColumnParent(ADDRESS3));
    mCity = resultSet.getString(getColumnParent(CITY));
    mProvince = resultSet.getString(getColumnParent(PROVINCE));
    mPostCode = resultSet.getString(getColumnParent(POST_CODE));
    mCountryId = resultSet.getLong(getColumnParent(COUNTRY));
    mCreated = resultSet.getTimestamp(getColumnParent(CREATED));
  }

  /*=============================================================
   * PUBLIC FUNCTIONS
   *============================================================*/

  /**
   * Adds the user to the database
   * @param conn the SQL connection
   * @return TRUE if successfully added. FALSE otherwise
   * @throws SQLException exception on insert
   */
  public boolean addToDatabase(Connection conn) throws SQLException {
    if (mPassword != null && mName != null && mAddress1 != null && mCity != null &&
        mCountryId > 0) {
      // Create the user insert statement
      String insertSql = new InsertBuilder(getTableParent())
          .set(TYPE, Integer.toString(getUserType().getValue()))
          .setString(PASSWORD, mPassword)
          .set(NAME, "?")
          .setString(ADDRESS1, mAddress1)
          .setString(CITY, mCity)
          .setString(COUNTRY, Long.toString(mCountryId))
          .toString();

      // Execute the insert
      try (PreparedStatement statement = conn.prepareStatement(insertSql)) {
        statement.setString(1, mName);
        if (statement.executeUpdate() == 1) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns the user table ID
   * @return the user child table ID
   */
  public long getUserId() {
    return mId;
  }

  /**
   * Returns the user reference (abstract)
   * @return the user reference UUID
   */
  public abstract UUID getUserReference();

  /**
   * Returns the user type (abstract)
   * @return the user type enum
   */
  public abstract UserType getUserType();

  /**
   * Returns if the user reference matches the string UUID provided (abstract)
   * @param userReference the user UUID reference
   * @return TRUE if matches. FALSE if doesn't
   */
  public abstract boolean matches(String userReference);

  /**
   * Identifies if the type and reference provided matches the user
   * @param type the user type
   * @param userReference the user UUID reference
   * @return TRUE if matches. FALSE if doesn't
   */
  public boolean matches(UserType type, String userReference) {
    return (getUserType() == type && matches(userReference));
  }
}
