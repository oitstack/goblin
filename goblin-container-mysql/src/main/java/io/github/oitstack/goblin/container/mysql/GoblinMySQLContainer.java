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


import io.github.oitstack.goblin.core.Goblin;
import io.github.oitstack.goblin.container.jdbc.container.JdbcDatabaseContainer;
import io.github.oitstack.goblin.spi.context.Image;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to start the mysql container.
 */
public class GoblinMySQLContainer extends JdbcDatabaseContainer<GoblinMySQLContainer> {

    public static final String CONTAINER_TYPE = "MYSQL";
    public static final String DEFAULT_IMAGE = "goblin.container.mysql:5.7";

    public static final Integer MYSQL_PORT = 3306;
    public static final String DEFAULT_USER = "test";
    public static final String DEFAULT_PASSWORD = "test";
    public static final String DEFAULT_DATA_BASE = "test";
    public static final String MYSQL_ROOT_USER = "root";

    /**
     * DataBase name.
     */
    private String databaseName = DEFAULT_DATA_BASE;

    /**
     * DataBase username.
     */
    private String username = DEFAULT_USER;

    /**
     * DataBase password.
     */
    private String password = DEFAULT_PASSWORD;

    /**
     * Store placeholders related to the mysql container.
     */
    private Map<String, String> placeholders = new HashMap<>();

    @Override
    public void start(Goblin context, Object confObj, Image image) {
        if (image == null || (image.getImageVersion() == null || "".equals(image.getImageVersion()))) {
            this.image(DEFAULT_IMAGE);
        } else {
            this.image(image.getImageVersion());
        }

        if (null != confObj) {
            Config conf = (Config) confObj;

            if (null != conf.getMysql()) {
                if (null != conf.getMysql().getDatabase()) {
                    this.setDatabaseName(conf.getMysql().getDatabase());
                    this.placeholders.put("DATABASE", conf.getMysql().getDatabase());
                }
                if (null != conf.getMysql().getInitScript()) {
                    this.setInitScript(conf.getMysql().getInitScript());
                }
            }

        }

        this.setUsername(MYSQL_ROOT_USER);
        this.setPassword(DEFAULT_PASSWORD);
        this.setExposedPort(MYSQL_PORT);
        this.setJdbcUrlParam("useUnicode", "true");
        this.setJdbcUrlParam("characterEncoding", "utf8");
        this.setJdbcUrlParam("useSSL", "false");
        this.setJdbcUrlParam("autoReconnect", "true");

        this.start();

        this.placeholders.put("URL", this.getJdbcUrl());
        this.placeholders.put("HOST", this.getHost());
        this.placeholders.put("PORT", String.valueOf(this.getPortByInnerPort(MYSQL_PORT)));
        this.placeholders.put("USERNAME", this.getUsername());
        this.placeholders.put("PASSWORD", this.getPassword());
    }

    @Override
    public Map<String, String> getPlaceHolders() {
        return this.placeholders;
    }

    @Override
    public String getContainerType() {
        return CONTAINER_TYPE;
    }

    @Override
    public Class configClass() {
        return Config.class;
    }

    @Override
    public String getDriverClassName() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return "com.mysql.cj.jdbc.Driver";
        } catch (ClassNotFoundException e) {
            return "com.mysql.jdbc.Driver";
        }
    }

    @Override
    public String getJdbcUrl() {
        String additionalUrlParams = buildUrlParameters("?", "&");
        return "jdbc:mysql://" + getHost() + ":" + getPortByInnerPort(MYSQL_PORT) +
                "/" + databaseName + additionalUrlParams;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    protected String getTestQueryCmd() {
        return "SELECT 1";
    }

    @Override
    public ExecResult execInRuntime(String... command) {
        return null;
    }

    @Override
    public GoblinMySQLContainer setDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
        return self();
    }

    @Override
    public GoblinMySQLContainer setUsername(final String username) {
        this.username = username;
        return self();
    }

    @Override
    public GoblinMySQLContainer setPassword(final String password) {
        this.password = password;
        return self();
    }

    @Override
    public GoblinMySQLContainer setExposedPort(Integer port) {
        super.setExposedPort(port);
        return self();
    }

    @Override
    public GoblinMySQLContainer setStartupTimeoutInSeconds(int startupTimeoutInSeconds) {
        super.setStartupTimeoutInSeconds(startupTimeoutInSeconds);
        return self();
    }

    @Override
    public GoblinMySQLContainer setConnectTimeoutInSeconds(int connectTimeoutInSeconds) {
        super.setConnectTimeoutInSeconds(connectTimeoutInSeconds);
        return self();
    }

    /**
     * Executed before the mysql container is initialized,
     * used for initialization of environment variables, etc.
     */
    @Override
    protected void doInit() {
        addEnv("MYSQL_DATABASE", databaseName);
        if (!MYSQL_ROOT_USER.equalsIgnoreCase(username)) {
            addEnv("MYSQL_USER", username);
        }
        if (password != null && !"".equals(password)) {
            addEnv("MYSQL_PASSWORD", password);
            addEnv("MYSQL_ROOT_PASSWORD", password);
        } else if (MYSQL_ROOT_USER.equalsIgnoreCase(username)) {
            addEnv("MYSQL_ALLOW_EMPTY_PASSWORD", "yes");
        } else {
            throw new RuntimeException("Empty password can only be used by root user");
        }
    }
}
