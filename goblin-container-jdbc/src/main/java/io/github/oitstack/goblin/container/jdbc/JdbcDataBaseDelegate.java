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
package io.github.oitstack.goblin.container.jdbc;

import io.github.oitstack.goblin.container.jdbc.container.JdbcDatabaseContainer;
import io.github.oitstack.goblin.container.jdbc.util.ScriptUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Delegate for JDBC database.
 */
public class JdbcDataBaseDelegate extends AbstractDatabaseDelegate<Statement> {

    public final static Logger LOGGER = LoggerFactory.getLogger(JdbcDataBaseDelegate.class);

    private JdbcDatabaseContainer jdbcContainer;
    private String query;

    public JdbcDataBaseDelegate(JdbcDatabaseContainer jdbcContainer, String query) {
        this.query = query;
        this.jdbcContainer = jdbcContainer;
    }

    @Override
    protected Statement createConn() {
        try {
            Connection connection = jdbcContainer.createConnection(query);
            return connection.createStatement();
        } catch (SQLException e) {
            LOGGER.error("Could not get JDBC connection");
            throw new RuntimeException("Could not get JDBC connection", e);
        }
    }


    @Override
    public void execute(String statement, int lineNumber,
                        boolean continueOnError, boolean ignoreFailedDrops, String scriptPath) {
        if ((null==statement||"".equals(statement))
                ||(null==statement.trim()||"".equals(statement.trim()))) {
            return;
        }

        try {
            Statement conn = getConn();
            boolean affectedRows = conn.execute(statement);
            LOGGER.debug("{} data affected at SQL: {}", affectedRows, statement);
        } catch (SQLException ex) {
            boolean isDropStatement = statement.trim().toLowerCase().startsWith("drop");
            if (continueOnError || (ignoreFailedDrops && isDropStatement)) {
                LOGGER.debug("Execute SQL script failed at line {} of resource {}: {}", lineNumber, scriptPath, statement, ex);
            } else {
                throw new ScriptUtils.StatementFailedException(statement, lineNumber, scriptPath, ex);
            }
        }
    }

    @Override
    protected void closeConnection(Statement connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.error("Could not close JDBC connection, sqlException", e);
        } catch (Throwable e) {
            LOGGER.error("Could not close JDBC connection", e);
        }
    }
}
