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


import io.github.oitstack.goblin.unit.db.AbstractJsr330Configuration;

/**
 * MySQL configuration class.
 */
public class MySQLConfiguration extends AbstractJsr330Configuration {

    /**
     * DataBase name.
     */
    private String databaseName;

    /**
     * DataBase username.
     */
    private String username;

    /**
     * DataBase password
     */
    private String password;

    /**
     * DataBase connection url
     */
    private String url;

    public MySQLConfiguration(String url, String databaseName, String username, String password) {
        this.url = url;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
    }

    /**
     * Get DataBase name.
     */
    public String getDatabaseName() {
        return this.databaseName;
    }

    /**
     * Get DataBase username.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Get DataBase password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Set DataBase name.
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * Set DataBase username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Set DataBase password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get DataBase connection url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set DataBase connection url
     */
    public void setUrl(String url) {
        this.url = url;
    }
}
