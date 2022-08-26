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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Delete and insert data strategy.
 */
public class CleanInsertLoadStrategyOperation implements LoadStrategyOperation {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CleanInsertLoadStrategyOperation.class);

	/**
	 * DataBase operations
	 */
	private DatabaseOperation databaseOperation;

	public CleanInsertLoadStrategyOperation(DatabaseOperation databaseOperation) {
		this.databaseOperation = databaseOperation;
	}

	/**
	 * execute script.
	 */
	@Override
	public void executeScripts(InputStream[] contentDataset) {

		LOGGER.debug("Calling Clean and Insert Load Strategy.");

		InputStream[] toCleanDatasets = new InputStream[contentDataset.length];
		InputStream[] toInsertDatasets = new InputStream[contentDataset.length];
		for (int i = 0; i < contentDataset.length; i++) {

			try {
				String inputStr = IOUtils.readFullStream(contentDataset[i]);
				toCleanDatasets[i] = new ByteArrayInputStream(inputStr.getBytes("UTF-8"));
				toInsertDatasets[i] = new ByteArrayInputStream(inputStr.getBytes("UTF-8"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		executeClean(toCleanDatasets);
		executeInsert(toInsertDatasets);
	}

	/**
	 * Insert data.
	 * @param contentDataset Data to be inserted
	 */
	private void executeInsert(InputStream[] contentDataset) {
		for (InputStream dataScript : contentDataset) {
			this.databaseOperation.insert(dataScript);
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
