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
package io.github.oitstack.goblin.unit.db;


import java.io.InputStream;

/**
 * Template class for database operations.
 */
public abstract class AbstractCustomizableDatabaseOperation<S,T> implements DatabaseOperation<T>  {

    /**
     * Data comparison strategy for comparing and asserting the accuracy of data.
     */
    protected ComparisonStrategy<S> compareStrategy;

    /**
     * Data insertion strategy for inserting data.
     */
    protected InsertionStrategy<S> insertStrategy;


    /**
     * Set Data comparison strategy
     * @param compareStrategy
     */
    public void setCompareStrategy(ComparisonStrategy<S> compareStrategy) {
        this.compareStrategy = compareStrategy;
    }

    /**
     * Get Data comparison strategy
     */
    public ComparisonStrategy<S> getCompareStrategy() {
        return this.compareStrategy;
    }

    /**
     * Compare data.
     */
    public boolean executeComparison(S conn, InputStream dataSet) throws Throwable {
        return compareStrategy.compare(conn, dataSet);
    }

    /**
     * Set data insertion strategy.
     * @param insertStrategy insertion strategy
     */
    public void setInsertStrategy(InsertionStrategy<S> insertStrategy) {
        this.insertStrategy = insertStrategy;
    }

    /**
     * Get data insertion strategy.
     */
    public InsertionStrategy<S> getInsertStrategy() {
        return this.insertStrategy;
    }

    /**
     * execute data insertion.
     */
    public void executeInsertion(S conn, InputStream dataSet) throws Throwable {
        insertStrategy.insert(conn, dataSet);
    }
}
