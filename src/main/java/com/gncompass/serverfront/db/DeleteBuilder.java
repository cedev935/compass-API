package com.gncompass.serverfront.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for building SQL delete statements.
 * Modified from code developed by John Krasnay <john@krasnay.ca>
 */
public class DeleteBuilder extends AbstractBuilder implements Serializable {

  private static final long serialVersionUID = 1;
  private String table;
  private List<String> joins = new ArrayList<>();
  private List<String> wheres = new ArrayList<String>();

  public DeleteBuilder(String table) {
    this.table = table;
  }

  @Override
  public String toString() {
    if (wheres.size() == 0) {
      throw new RuntimeException(
                      "Empty where lists are not permitted for building delete statements");
    }

    // Proceed with build
    StringBuilder sql = new StringBuilder("DELETE ");
    if (joins.size() > 0) {
      sql.append(table).append(" FROM ").append(table);
    } else {
      sql.append("FROM ").append(table);
    }
    appendList(sql, joins, " JOIN ", " JOIN ");
    appendList(sql, wheres, " WHERE ", " AND ");
    return sql.toString();
  }

  public DeleteBuilder join(String table, String on) {
    joins.add(table + " ON " + on);
    return this;
  }

  public DeleteBuilder where(String expr) {
    wheres.add(expr);
    return this;
  }
}
