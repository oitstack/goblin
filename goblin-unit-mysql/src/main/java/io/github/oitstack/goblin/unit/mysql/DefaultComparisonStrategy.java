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

import io.github.oitstack.goblin.unit.db.FailureHandler;
import io.github.oitstack.goblin.unit.db.XUnitAssertionError;
import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;

/**
 * Default data comparison strategy.
 */
public class DefaultComparisonStrategy implements MySQLComparisonStrategy {

    public final static Logger LOGGER = LoggerFactory.getLogger(DefaultComparisonStrategy.class);

    /**
     * Attributes ignored during data comparison.
     */
    private String[] ignoreProperties;

    /**
     * Compare whether the data obtained through the connection is consistent with the dataset.
     * @param connection DataBase connection
     * @param dataset data to be compared
     */
    @Override
    public boolean compare(MySQLConnectionCallback connection, InputStream dataset) throws XUnitAssertionError, Throwable {
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().setColumnSensing(true).build(dataset);
        IDataSet actualDataSet = connection.conn().createDataSet(expectedDataSet.getTableNames());

        try {
            if (null == ignoreProperties) {
                Assertion.assertEquals(expectedDataSet, actualDataSet);
            } else {
                Map<String, Set<String>> columnsToIgnore;
                Set<String> tablesNames = new HashSet<>(Arrays.asList(expectedDataSet.getTableNames()));
                columnsToIgnore = parseIgnoreProperties(tablesNames, ignoreProperties);

                for (ITable iTable : expectedDataSet.getTables()) {
                    Set<String> toIgnoreColumns = columnsToIgnore.get(iTable.getTableMetaData().getTableName());
                    ITable expectedTable = DefaultColumnFilter.excludedColumnsTable(
                            iTable,
                            toIgnoreColumns.stream().toArray(String[]::new)
                    );
                    ITable actualTable = DefaultColumnFilter.excludedColumnsTable(
                            actualDataSet.getTable(iTable.getTableMetaData().getTableName()),
                            toIgnoreColumns.stream().toArray(String[]::new)
                    );
                    Assertion.assertEquals(expectedTable, actualTable);
                }
            }
        } catch (DatabaseUnitException e) {
            e.printStackTrace();
            throw FailureHandler.createFailure(e.getMessage());
        }

        return true;
    }

    /**
     * Set properties that need to be ignored during data comparison.
     * @param ignoreProperties Attributes ignored during data comparison
     */
    @Override
    public void setIgnoreProperties(String[] ignoreProperties) {
        this.ignoreProperties = ignoreProperties;
    }

    private Map<String, Set<String>> parseIgnoreProperties(Set<String> tableNames, String[] ignoreProperties) {
        Map<String, Set<String>> columnsToIgnore = new HashMap<>();
        for (String column : ignoreProperties) {
            String[] tableAndColumn = column.split("\\.");

            if (tableAndColumn.length == 1) {
                for (String tableName : tableNames) {
                    Set<String> columns = columnsToIgnore.get(tableName);
                    if (null == columns) {
                        columns = new HashSet<>();
                        columnsToIgnore.put(tableName, columns);
                    }
                    columns.add(tableAndColumn[0]);
                }
            } else if (tableAndColumn.length == 2) {
                Set<String> columns = columnsToIgnore.get(tableAndColumn[0]);
                if (null == columns) {
                    columns = new HashSet<>();
                    columnsToIgnore.put(tableAndColumn[0], columns);
                }
                columns.add(tableAndColumn[1]);
            } else {
                LOGGER.warn(String.format("column %s has an invalid table.column value. It won't be used for ignoring columns", column));
            }
        }

        return columnsToIgnore;
    }
}
