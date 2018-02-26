package com.gncompass.serverfront.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for building SQL insert statements.
 * Modified from code developed by John Krasnay <john@krasnay.ca>
 */
public class InsertBuilder extends AbstractBuilder implements Serializable {

  private static final long serialVersionUID = 1;
  private String table;
  private List<String> columns = new ArrayList<String>();
  private List<String> values = new ArrayList<String>();

  /**
   * Constructor.
   * @param table Name of the table into which we'll be inserting.
   */
  public InsertBuilder(String table) {
    this.table = table;
  }

  /**
   * Inserts a column name, value pair into the SQL.
   * @param column Name of the table column.
   * @param value Value to substitute in. InsertBuilder does *no* interpretation
   *              of this. If you want a string constant inserted, you must
   *              provide the single quotes and escape the internal quotes. It
   *              is more common to use a question mark or a token in the style
   *              of {@link ParameterizedPreparedStatementCreator}, e.g. ":foo".
   */
  public InsertBuilder set(String column, String value) {
    columns.add(column);
    values.add(value);
    return this;
  }

  /**
   * Inserts a column name, value string pair into the SQL. Adds a single quote wrapper to the value
   * @param column Name of the table column
   * @param value Value to substitute in that is a string
   * @return the insert builder reference
   */
  public InsertBuilder setString(String column, String value) {
    return set(column, "'" + value + "'");
  }

  @Override
  public String toString() {
    StringBuilder sql = new StringBuilder("INSERT INTO ").append(table).append(" (");
    appendList(sql, columns, "", ", ");
    sql.append(") VALUES (");
    appendList(sql, values, "", ", ");
    sql.append(")");
    return sql.toString();
  }
}
