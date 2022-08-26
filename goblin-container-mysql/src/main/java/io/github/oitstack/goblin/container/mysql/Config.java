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
package io.github.oitstack.goblin.container.mysql;

/**
 *  Mapping class for goblin configuration.
 */
public class Config {

    /**
     * Mapping class for mysql configuration in goblin configuration file.
     */
    private Mysql mysql;

    public Mysql getMysql() {
        return mysql;
    }

    public void setMysql(Mysql mysql) {
        this.mysql = mysql;
    }

    /**
     * Mapping class for mysql configuration in goblin configuration file.
     */
    public static class Mysql {
        /**
         * DataBase name
         */
        private String database;

        /**
         * database initialization script.
         */
        private String initScript;

        public Mysql() {
        }

        public Mysql(String database, String initScript) {
            this.database = database;
            this.initScript = initScript;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getInitScript() {
            return initScript;
        }

        public void setInitScript(String initScript) {
            this.initScript = initScript;
        }
    }
}
