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


import io.github.oitstack.goblin.unit.db.AbstractTestRule;
import io.github.oitstack.goblin.unit.db.DatabaseOperation;

/**
 * MySQL test rule, defines the operation mode of test data, file name format, file suffix, etc.
 */
public class MySQLTestRule extends AbstractTestRule {
    private static final String NAME = "mysql";
    private static final String EXTENSION = "xml";

    public MySQLTestRule(MySQLConfiguration mySQLConfiguration) {
        databaseOperation = new MySQLOperation(mySQLConfiguration);
    }

    public MySQLTestRule() {

        if (System.getProperty("GOBLIN_MYSQL_URL") != null) {
            MySQLConfiguration mySQLConfiguration = new MySQLConfiguration(
                    System.getProperty("GOBLIN_MYSQL_URL"),
                    System.getProperty("GOBLIN_MYSQL_DATABASENAME"),
                    System.getProperty("GOBLIN_MYSQL_USERNAME"),
                    System.getProperty("GOBLIN_MYSQL_PASSWORD")
            );
            databaseOperation = new MySQLOperation(mySQLConfiguration);
        } else {
            databaseOperation = new MySQLOperation();
        }
    }

    /**
     * Template method to get the suffix of the test data file.
     */
    @Override
    public String getWorkingExtension() {
        return EXTENSION;
    }

    /**
     * Template method to get database operations.
     */
    @Override
    public DatabaseOperation getDatabaseOperation() {
        return this.databaseOperation;
    }

    /**
     * Template method to Get name of test rule.
     */
    @Override
    public String getName() {
        return NAME;
    }

}
