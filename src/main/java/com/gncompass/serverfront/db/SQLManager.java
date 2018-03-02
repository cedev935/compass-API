package com.gncompass.serverfront.db;

import com.gncompass.serverfront.util.StateHelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

public class SQLManager {
  // Driver class identity information for pooling
  private static final String DRIVER_CLASS_DEV = "com.mysql.jdbc.Driver";
  private static final String DRIVER_CLASS_PROD = "com.mysql.jdbc.GoogleDriver";

  // Connection pool sizing
  private static final int MIN_CONNECTION_POOL_SIZE = 2;
  private static final int MAX_CONNECTION_POOL_SIZE = 5; // The hard limit is 12 by google

  // Connection pool will start throwing errors when connections take more than
  // this amount to execute and there are no available spots for new connections.
  private static final long MAX_CONNECTION_WAIT_MS = 30000L;

  // This datasource lives as long as the instance lives.
  private static BasicDataSource sDataSource = null;
  private static String sDataSourceAddress = null;
  private static final Object sDataSourceLock = new Object();

  /**
   * Fetches a connection reference from the available pool (default option)
   * @return a connection reference
   * @throws SQLException for any SQL connection errors. a connection could not be established
   */
  public static Connection getConnection() throws SQLException {
    return getConnection(true);
  }

  /**
   * Fetches a connection reference from either the available pool or just attempts to create a new
   * one with the standard JDBC driver, depending on the input parameter
   * @param useConnectionPool TRUE to use connection pool to fetch connection. FALSE to use JDBC
   * @return a connection reference
   * @throws SQLException for any SQL connection errors. a connection could not be established
   */
  public static Connection getConnection(boolean useConnectionPool) throws SQLException {
    // Direct JDBC connection
    if (!useConnectionPool) {
      return resetAutoCommit(DriverManager.getConnection(sDataSourceAddress));
    }

    // Pooled connection
    synchronized (sDataSourceLock) {
      if (sDataSource == null) {
        sDataSource = new BasicDataSource();
        sDataSource.setDriverClassName(getDriverClassName());
        sDataSource.setUrl(sDataSourceAddress);

        sDataSource.setInitialSize(MIN_CONNECTION_POOL_SIZE);
        sDataSource.setMaxActive(MAX_CONNECTION_POOL_SIZE);
        sDataSource.setMaxIdle(MAX_CONNECTION_POOL_SIZE);
        sDataSource.setMaxWait(MAX_CONNECTION_WAIT_MS);

        sDataSource.setTestOnBorrow(true);
        sDataSource.setValidationQuery("SELECT 1");

        // From BasicDataSourceFactory.java, DBCP-215
        // Trick to make sure that initialSize connections are created
        if (sDataSource.getInitialSize() > 0) {
          sDataSource.getLogWriter();
        }
      }

      return resetAutoCommit(sDataSource.getConnection());
    } // synchronized dataSourceLock
  }

  /**
   * Fetches the driver class name based on the production or development status of the build
   * @return the driver class name
   */
  private static String getDriverClassName() {
    String driverClassName;
    if(StateHelper.isProduction()) {
      driverClassName = DRIVER_CLASS_PROD;
    } else {
      driverClassName = DRIVER_CLASS_DEV;
    }

    try {
      Class.forName(driverClassName);
      return driverClassName;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Cannot find the SQL driver in the classpath." +
          " driver=" + driverClassName, e);
    }
  }

  /**
   * Basic initialization required for the SQL Manager. Called once on servlet init
   */
  public static void init() {
    synchronized (sDataSourceLock) {
      sDataSourceAddress = StateHelper.getProperty("cloudsql", "cloudsql-local");
    }
  }

  /**
   * Reset the auto commit back to true. Required since on occasion auto commit is disabled by
   * instance usages
   * @param conn the Connection to update
   * @return the Connection reference
   */
  private static Connection resetAutoCommit(Connection conn) {
    try {
      conn.setAutoCommit(true);
    } catch (SQLException se) {
      // Ignore
    }
    return conn;
  }
}
