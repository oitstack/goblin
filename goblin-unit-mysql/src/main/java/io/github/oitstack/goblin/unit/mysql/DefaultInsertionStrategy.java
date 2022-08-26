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

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

import java.io.InputStream;

/**
 * Default MySQL data insertion strategy.
 */
public class DefaultInsertionStrategy implements MySQLInsertionStrategy {

    /**
     * Insert data into database.
     * @param connection DataBase connection
     * @param dataset Data to be inserted into the database
     */
    @Override
    public void insert(MySQLConnectionCallback connection, InputStream dataset) throws Throwable {
        FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
        builder.setColumnSensing(true);
        IDataSet dataSet = builder.build(dataset);
        org.dbunit.operation.DatabaseOperation.INSERT.execute(connection.conn(), dataSet);
    }
}
