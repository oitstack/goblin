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


import java.lang.reflect.Constructor;

/**
 * Data operation strategy factory.
 */
public class ReflectLoadStrategyFactory implements LoadStrategyFactory {
    public ReflectLoadStrategyFactory() {
        super();
    }


    /**
     * Generate data operation strategy.
     */
    @Override
    public LoadStrategyOperation getLoadStrategyInstance(LoadStrategyEnum loadStrategyEnum, DatabaseOperation dbOperation) {

        try {
            Constructor<? extends LoadStrategyOperation> dataBaseConstructor = loadStrategyEnum.strategy().getConstructor(DatabaseOperation.class);
            dataBaseConstructor.setAccessible(true);

            return dataBaseConstructor.newInstance(dbOperation);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }
}
