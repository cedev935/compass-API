package com.gncompass.serverfront.db.model;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractObject {
  /**
   * Returns the column name with the appropriate table scope. Used when column requested are used
   * with joins
   * @param columnName the base column name to add the scope to
   * @return the column name with the table scope
   */
  protected String getColumn(String columnName) {
    return getColumn(getTable(), columnName);
  }

  /**
   * Returns the column with the provided table name
   * @param tableName the table name
   * @param columnName the column name
   * @return the column name with the table scope
   */
  private String getColumn(String tableName, String columnName) {
    return tableName + "." + columnName;
  }

  /**
   * Returns the column with the appropriate parent table scope. Used when parent column requests
   * are used with joins. This class only supports one level of parent abstraction
   * @param columnName the base column name to add the scope to
   * @return the column name with the table scope
   */
  protected String getColumnParent(String columnName) {
    return getColumn(getTableParent(), columnName);
  }

  /*
   * Returns the table name of the class (abstract)
   * @return the object table name
   */
  protected abstract String getTable();

  /**
   * Returns the parent table name. This will throw if called and not overriden. This should only
   * be overridden if there is an abstract parent (such as User > Borrower). It is used by
   * getColumnParent(String)
   * @return the object parent table name
   */
  protected String getTableParent() {
    throw new RuntimeException(
        "Either no parent class exists or the parent class did not override getParentTable() implementation");
  }

  /**
   * The abstract update fetch that all objects should implement to update from a result set
   * @param resultSet the result set to pull the data from
   * @throws SQLException sql exception on data not found in result set
   */
  abstract void updateFromFetch(ResultSet resultSet) throws SQLException;
}
