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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Insert data strategy.
 */
public class InsertLoadStrategyOperation implements LoadStrategyOperation {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(InsertLoadStrategyOperation.class);

	/**
	 * DataBase operations
	 */
	private DatabaseOperation dbOperation;

	public InsertLoadStrategyOperation(DatabaseOperation dbOperation) {
		this.dbOperation = dbOperation;
	}

	/**
	 * execute script.
	 */
	@Override
	public void executeScripts(InputStream[] dataSet) {

		LOGGER.debug("Calling Insert Load Strategy.");
		if (dataSet.length > 0) {
			executeInsert(dataSet);
		} 
	}

	/**
	 * Insert data.
	 * @param dataSet Data to be inserted
	 */
	private void executeInsert(InputStream[] dataSet) {
		for (InputStream dataStream : dataSet) {
			this.dbOperation.insert(dataStream);
		}
	}

}
