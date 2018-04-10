package com.gncompass.serverfront.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for programmatically constructing SQL select statements
 *
 * Modified from code developed by John Krasnay <john@krasnay.ca>
 *
 * <pre>
 * String sql = new SelectBuilder()
 * .column(&quot;e.id&quot;)
 * .column(&quot;e.name as empname&quot;)
 * .column(&quot;d.name as deptname&quot;)
 * .column(&quot;e.salary&quot;)
 * .from((&quot;Employee e&quot;)
 * .join(&quot;Department d on e.dept_id = d.id&quot;)
 * .where(&quot;e.salary &gt; 100000&quot;)
 * .orderBy(&quot;e.salary desc&quot;)
 * .toString();
 * </pre>
 *
 * <pre>
 * String sql = new SelectBuilder()
 * .column(&quot;d.id&quot;)
 * .column(&quot;d.name&quot;)
 * .column(&quot;sum(e.salary) as total&quot;)
 * .from(&quot;Department d&quot;)
 * .join(&quot;Employee e on e.dept_id = d.id&quot;)
 * .groupBy(&quot;d.id&quot;)
 * .groupBy(&quot;d.name&quot;)
 * .having(&quot;total &gt; 1000000&quot;).toString();
 * </pre>
 */
public class SelectBuilder extends AbstractBuilder {

  private static final long serialVersionUID = 1L;

  private boolean distinct;
  private List<Object> columns = new ArrayList<>();
  private List<String> tables = new ArrayList<>();
  private List<String> joins = new ArrayList<>();
  private List<String> leftJoins = new ArrayList<>();
  private List<String> wheres = new ArrayList<>();
  private List<String> groupBys = new ArrayList<>();
  private List<String> havings = new ArrayList<>();
  private List<SelectBuilder> unions = new ArrayList<>();
  private List<String> orderBys = new ArrayList<>();
  private int limit = 0;
  private int offset = 0;
  private boolean forUpdate;
  private boolean noWait;

  public SelectBuilder() {
  }

  public SelectBuilder(String table) {
    tables.add(table);
  }

  /**
   * Copy constructor. Used by {@link #clone()}.
   * @param other SelectBuilder being cloned.
   */
  protected SelectBuilder(SelectBuilder other) {
    this.distinct = other.distinct;
    this.forUpdate = other.forUpdate;
    this.noWait = other.noWait;

    for (Object column : other.columns) {
      if (column instanceof SubSelectBuilder) {
        this.columns.add(((SubSelectBuilder) column).clone());
      } else {
        this.columns.add(column);
      }
    }

    this.tables.addAll(other.tables);
    this.joins.addAll(other.joins);
    this.leftJoins.addAll(other.leftJoins);
    this.wheres.addAll(other.wheres);
    this.groupBys.addAll(other.groupBys);
    this.havings.addAll(other.havings);

    for (SelectBuilder sb : other.unions) {
      this.unions.add(sb.clone());
    }

    this.orderBys.addAll(other.orderBys);
  }

  /**
   * Alias for {@link #where(String)}.
   */
  public SelectBuilder and(String expr) {
    return where(expr);
  }

  public SelectBuilder column(String name) {
    columns.add(name);
    return this;
  }

  public SelectBuilder column(SubSelectBuilder subSelect) {
    columns.add(subSelect);
    return this;
  }

  public SelectBuilder column(String name, boolean groupBy) {
    columns.add(name);
    if (groupBy) {
      groupBys.add(name);
    }
    return this;
  }

  public SelectBuilder limit(int limit, int offset) {
    this.limit = limit;
    this.offset = offset;
    return this;
  }

  public SelectBuilder limit(int limit) {
    return limit(limit, 0);
  }

  @Override
  public SelectBuilder clone() {
    return new SelectBuilder(this);
  }

  public SelectBuilder distinct() {
    this.distinct = true;
    return this;
  }

  public SelectBuilder forUpdate() {
    forUpdate = true;
    return this;
  }

  public SelectBuilder from(String table) {
    tables.add(table);
    return this;
  }

  public List<SelectBuilder> getUnions() {
    return unions;
  }

  public SelectBuilder groupBy(String expr) {
    groupBys.add(expr);
    return this;
  }

  public SelectBuilder having(String expr) {
    havings.add(expr);
    return this;
  }

  public SelectBuilder join(String join, String on) {
    return join(join, on, false);
  }

  public SelectBuilder join(String join, String on, boolean front) {
    String onStatement = join + " ON " + on;
    if(front) {
      joins.add(0, onStatement);
    } else {
      joins.add(onStatement);
    }

    return this;
  }

  public SelectBuilder leftJoin(String join, String on) {
    leftJoins.add(join + " ON " + on);
    return this;
  }

  public SelectBuilder leftJoin(String join, String on, boolean front) {
    String onStatement = join + " ON " + on;
    if(front) {
      leftJoins.add(0, onStatement);
    } else {
      leftJoins.add(onStatement);
    }

    return this;
  }

  public SelectBuilder noWait() {
    if (!forUpdate) {
      throw new RuntimeException("noWait without forUpdate cannot be called");
    }
    noWait = true;
    return this;
  }

  public SelectBuilder orderBy(String name) {
    orderBys.add(name);
    return this;
  }

  /**
   * Adds an ORDER BY item with a direction indicator.
   * @param name Name of the column by which to sort.
   * @param ascending If true, specifies the direction "ASC", otherwise, specifies
                      the direction "DESC".
   */
  public SelectBuilder orderBy(String name, boolean ascending) {
    if (ascending) {
      orderBys.add(name + " ASC");
    } else {
      orderBys.add(name + " DESC");
    }
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sql = new StringBuilder("SELECT ");

    if (distinct) {
      sql.append("DISTINCT ");
    }

    if (columns.size() == 0) {
      sql.append("*");
    } else {
      appendList(sql, columns, "", ", ");
    }

    appendList(sql, tables, " FROM ", ", ");
    appendList(sql, joins, " JOIN ", " JOIN ");
    appendList(sql, leftJoins, " LEFT JOIN ", " LEFT JOIN ");
    appendList(sql, wheres, " WHERE ", " AND ");
    appendList(sql, groupBys, " GROUP BY ", ", ");
    appendList(sql, havings, " HAVING ", " AND ");
    appendList(sql, unions, " UNION ", " UNION ");
    appendList(sql, orderBys, " ORDER BY ", ", ");

    if (forUpdate) {
      sql.append(" FOR UPDATE");
      if (noWait) {
        sql.append(" NOWAIT");
      }
    }

    if(limit > 0)
      sql.append(" LIMIT " + limit);
    if(offset > 0)
      sql.append(", " + offset);

    return sql.toString();
  }

  /**
   * Adds a "union" select builder. The generated SQL will union this query
   * with the result of the main query. The provided builder must have the
   * same columns as the parent select builder and must not use "order by" or
   * "for update".
   */
  public SelectBuilder union(SelectBuilder unionBuilder) {
    unions.add(unionBuilder);
    return this;
  }

  public SelectBuilder where(String expr) {
    wheres.add(expr);
    return this;
  }
}
