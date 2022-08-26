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

import java.util.Collection;

/**
 * Database proxy abstract class, you can inherit from it and implement to create connections,
 * close connections, execute scripts, etc.
 * @param <CONNECTION> Database connection
 */
public abstract class AbstractDatabaseDelegate<CONNECTION> implements DatabaseDelegate {

    /**
     * Database connection.
     */
    private CONNECTION connection;

    /**
     * Whether the client has established a connection to the database.
     */
    private boolean isConnected = false;

    /**
     * Get database connection.
     * @return
     */
    protected CONNECTION getConn() {
        if (!isConnected) {
            connection = createConn();
            isConnected = true;
        }
        return connection;
    }

    @Override
    public void execute(Collection<String> statements, String scriptPath, boolean continueOnError, boolean ignoreFailedDrops) {
        int lineNumber = 0;
        for (String statement : statements) {
            lineNumber++;
            execute(statement, lineNumber, continueOnError, ignoreFailedDrops, scriptPath);
        }
    }

    @Override
    public void close() {
        if (isConnected) {
            closeConnection(connection);
            isConnected = false;
        }
    }


    /**
     * Close the database connection, and requires the user to implement.
     * @param connection Database connection.
     */
    protected abstract void closeConnection(CONNECTION connection);

    /**
     * Create the database connection, and requires the user to implement.
     */
    protected abstract CONNECTION createConn();
}
