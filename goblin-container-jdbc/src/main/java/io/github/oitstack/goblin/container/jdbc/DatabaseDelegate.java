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
 * Delegate for Database.
 */
public interface DatabaseDelegate extends AutoCloseable {

    /**
     * Execute statement by the implementation of the delegate
     */
    void execute(String statement, int lineNumber, boolean continueOnError, boolean ignoreFailedDrops, String scriptPath);

    /**
     * Execute collection of statements
     */
    void execute(Collection<String> statements, String scriptPath, boolean continueOnError, boolean ignoreFailedDrops);

    /**
     * Close connection to the database
     *
     * Overridden to suppress throwing Exception
     */
    @Override
    void close();
}
