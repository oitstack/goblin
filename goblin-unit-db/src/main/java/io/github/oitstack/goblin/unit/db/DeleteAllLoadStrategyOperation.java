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
 * Delete data strategy.
 */
public class DeleteAllLoadStrategyOperation implements LoadStrategyOperation {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeleteAllLoadStrategyOperation.class);

	/**
	 * DataBase operations
	 */
	private DatabaseOperation databaseOperation;

	public DeleteAllLoadStrategyOperation(DatabaseOperation databaseOperation) {
		this.databaseOperation = databaseOperation;
	}

	/**
	 * execute script.
	 */
	@Override
	public void executeScripts(InputStream[] contentDataset) {
		
		LOGGER.debug("Calling Clean Load Strategy.");

		if (contentDataset.length > 0) {
			executeClean(contentDataset);
		}
	}

	/**
	 * Delete data.
	 * @param contentDataset Data to be deleted.
	 */
	private void executeClean(InputStream[] contentDataset) {
		for (InputStream dataScript : contentDataset) {
			this.databaseOperation.deleteAll(dataScript);
		}
	}

}
