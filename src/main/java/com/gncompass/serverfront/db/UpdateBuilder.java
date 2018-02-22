package com.gncompass.serverfront.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating SQL update statements.
 * Modified from code developed by John Krasnay <john@krasnay.ca>
 */
public class UpdateBuilder extends AbstractBuilder implements Serializable {

  private static final long serialVersionUID = 1L;
  private String table;
  private List<String> sets = new ArrayList<>();
  private List<String> wheres = new ArrayList<>();

  public UpdateBuilder(String table) {
    this.table = table;
  }

  public UpdateBuilder set(String expr) {
    sets.add(expr);
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sql = new StringBuilder("UPDATE ").append(table);
    appendList(sql, sets, " SET ", ", ");
    appendList(sql, wheres, " WHERE ", " AND ");
    return sql.toString();
  }

  public UpdateBuilder where(String expr) {
    wheres.add(expr);
    return this;
  }
}
