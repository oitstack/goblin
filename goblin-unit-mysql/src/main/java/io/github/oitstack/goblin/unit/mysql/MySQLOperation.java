/*
 * Copyright 2022 OPPO Goblin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.oitstack.goblin.unit.mysql;


import io.github.oitstack.goblin.unit.db.AbstractCustomizableDatabaseOperation;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mysql.MySqlConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class defines some basic operations of MySQL,
 * such as getting a connection, adding, deleting, modifying and checking data.
 */
public class MySQLOperation extends AbstractCustomizableDatabaseOperation<MySQLConnectionCallback, MySqlConnection> {

    public final static Logger LOGGER = LoggerFactory.getLogger(MySQLOperation.class);

    /**
     * By locking the jdbc driver instantiation process, ensure that the jdbc instance is only initialized once.
     */
    private static final Object DRIVER_LOAD_MUTEX = new Object();

    /**
     * Database driver.
     */
    private Driver driver;

    /**
     * DataBase connection.
     */
    private static MySqlConnection conn;

    /**
     * DataBase configuration.
     */
    private MySQLConfiguration mySQLConfiguration;

    protected MySQLOperation(MySQLConfiguration mySQLConfiguration) {
        this.mySQLConfiguration = mySQLConfiguration;
        this.setInsertStrategy(new DefaultInsertionStrategy());
        this.setCompareStrategy(new DefaultComparisonStrategy());
    }

    protected MySQLOperation() {
        this.setInsertStrategy(new DefaultInsertionStrategy());
        this.setCompareStrategy(new DefaultComparisonStrategy());
    }

    /**
     * Insert data into database.
     *
     * @param contentStream data to be inserted.
     */
    @Override
    public void insert(InputStream contentStream) {
        final MySqlConnection conn = connectionManager();
        try {
            executeInsertion(() -> conn, contentStream);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Unexpected error reading data set file.", e);
        }
    }

    /**
     * Delete all data.
     *
     * @param contentStream data to be deleted.
     */
    @Override
    public void deleteAll(InputStream contentStream) {
        IDatabaseConnection conn = null;
        try {
            conn = connectionManager();
            FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
            builder.setCaseSensitiveTableNames(false);
            IDataSet dataSet = builder.build(contentStream);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Dropping tables. {}", dataSet.getTableNames());
            }
            org.dbunit.operation.DatabaseOperation.DELETE_ALL.execute(conn, dataSet);

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (DatabaseUnitException e) {
            e.printStackTrace();
        }
    }

    /**
     * Determine whether the data is the expected data.
     *
     * @param expectedData expected data
     * @return
     */
    @Override
    public boolean databaseIs(InputStream expectedData) {
        final MySqlConnection conn = connectionManager();
        try {

            executeComparison(() -> conn, expectedData);

            return true;
        } catch (Throwable e) {
            throw new IllegalArgumentException("Unexpected error reading expected data set file.", e);
        }
    }

    /**
     * Get MySQL connection.
     *
     * @return
     */
    @Override
    public MySqlConnection connectionManager() {
        if (conn == null) {
            try {
                final Driver jdbcDriverInstance = getJdbcDriverInstance();
                Class.forName("com.mysql.jdbc.Driver");
                Connection dbConn = DriverManager.getConnection(
                        mySQLConfiguration.getUrl(),
                        mySQLConfiguration.getUsername(),
                        mySQLConfiguration.getPassword()
                );

                conn = new MySqlConnection(dbConn, mySQLConfiguration.getDatabaseName());
                conn.getConfig().setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true);
                conn.getConfig().setProperty(DatabaseConfig.PROPERTY_ESCAPE_PATTERN, "`?`");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return conn;
    }

    /**
     * Load the JDBC driver and get the driver class name.
     */
    public String getDriverClassName() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return "com.mysql.cj.jdbc.Driver";
        } catch (ClassNotFoundException var2) {
            return "com.mysql.jdbc.Driver";
        }
    }

    /**
     * Generate MySQL driver instance.
     */
    public Driver getJdbcDriverInstance() throws RuntimeException {

        synchronized (DRIVER_LOAD_MUTEX) {
            if (driver == null) {
                try {
                    driver = (Driver) Class.forName(this.getDriverClassName()).newInstance();
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                    throw new RuntimeException("Could not get Driver", e);
                }
            }
        }

        return driver;
    }
}
