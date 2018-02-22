package com.gncompass.serverfront.db.model;

import com.gncompass.serverfront.db.SelectBuilder;

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
  private static final String PARENT_ID = "id";
  private static final String CHILD_ID = "child_id";
  private static final String TYPE = "type";
  //private static final String PASSWORD = "password";
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
    BORROWER,
    INVESTOR
  }

  // Database parameters
  public long mParentId = 0;
  //public long mChildId = 0;
  //public int mType = 0;
  //public byte[] mPassword = null;
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
  public String mCountry = null;
  public Timestamp mCreated = null;

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
    SelectBuilder selectBuilder = new SelectBuilder()
        .join(getTableParent(),
              getColumnParent(CHILD_ID) + "=" + childIdColumn + " AND " +
              getColumnParent(TYPE) + "=" + childTypeColumn +
              (idColumn != null ? " AND " + getColumnParent(PARENT_ID) + "=" + idColumn : ""))
        .column(getColumnParent(PARENT_ID))
        //.column(getColumnParent(CHILD_ID))
        //.column(getColumnParent(TYPE))
        //.column(getColumnParent(PASSWORD))
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
    mParentId = resultSet.getLong(getColumnParent(PARENT_ID));
    //mChildId = resultSet.getLong(getColumnParent(CHILD_ID));
    //mType = resultSet.getInt(getColumnParent(TYPE));
    //mPassword = resultSet.getBytes(getColumnParent(PASSWORD));
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
    mCountry = resultSet.getString(getColumnParent(COUNTRY));
    mCreated = resultSet.getTimestamp(getColumnParent(CREATED));
  }

  /*=============================================================
   * PUBLIC FUNCTIONS
   *============================================================*/

  /**
   * Returns the user child table ID (abstract)
   * @return the user child table ID
   */
  public abstract long getUserId();

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
