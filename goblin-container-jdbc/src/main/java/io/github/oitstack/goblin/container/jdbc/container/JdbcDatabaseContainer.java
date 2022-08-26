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
package io.github.oitstack.goblin.container.jdbc.container;

import io.github.oitstack.goblin.container.jdbc.DatabaseDelegate;
import io.github.oitstack.goblin.container.jdbc.JdbcDataBaseDelegate;
import io.github.oitstack.goblin.container.jdbc.util.ScriptUtils;
import io.github.oitstack.goblin.core.GoblinContainer;
import io.github.oitstack.goblin.runtime.docker.container.DockerContainerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.util.stream.Collectors.joining;

/**
 * Abstract class for database container, you can customize your own database container
 * by inheriting from this class.
 *
 * @param <SELF> Subclass of JdbcDatabaseContainer
 */
public abstract class JdbcDatabaseContainer<SELF extends JdbcDatabaseContainer<SELF>> extends DockerContainerAdapter<SELF> implements GoblinContainer {

    public final static Logger LOGGER = LoggerFactory.getLogger(JdbcDatabaseContainer.class);

    /**
     * By locking the jdbc driver instantiation process, ensure that the jdbc instance is only initialized once.
     */
    private static final Object DRIVER_LOAD_LOCK = new Object();

    /**
     * Database driver.
     */
    private Driver driver;

    /**
     * Database initialization script.
     */
    private String initScriptPath;

    /**
     * Parameter of jdbc url.
     */
    protected Map<String, String> urlParameterMap = new HashMap<>();

    private static final int TIME_UNIT = 1000;
    private int startupTimeoutInSeconds = 90;
    private int connectTimeoutInSeconds = 90;

    private static final String QUESTION_MASK = "?";
    private static final String QUERY_PARAM_SEPARATOR = "&";


    /**
     * Get driver class name.
     *
     * @return
     */
    public abstract String getDriverClassName();

    /**
     * Get jdbc url.
     *
     * @return
     */
    public abstract String getJdbcUrl();

    /**
     * Get name of database.
     *
     * @return
     */
    public String getDatabaseName() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get username.
     *
     * @return
     */
    public abstract String getUsername();

    /**
     * Get password.
     *
     * @return
     */
    public abstract String getPassword();

    /**
     * Get test command, used to test the health of the database.
     *
     * @return
     */
    protected abstract String getTestQueryCmd();

    /**
     * Set username.
     *
     * @param username database username.
     * @return
     */
    public SELF setUsername(String username) {
        throw new UnsupportedOperationException();
    }

    /**
     * Set password.
     *
     * @param password database password.
     * @return
     */
    public SELF setPassword(String password) {
        throw new UnsupportedOperationException();
    }

    /**
     * Set database name.
     *
     * @param dbName database name
     * @return
     */
    public SELF setDatabaseName(String dbName) {
        throw new UnsupportedOperationException();
    }

    /**
     * Set parameter of jdbc url.
     *
     * @param paramName  Parameter name of jdbc url
     * @param paramValue Parameter value of jdbc url
     * @return
     */
    public SELF setJdbcUrlParam(String paramName, String paramValue) {
        urlParameterMap.put(paramName, paramValue);
        return self();
    }

    /**
     * Set the external port of the container.
     *
     * @param port container external port
     * @return
     */
    public SELF setExposedPort(Integer port) {
        this.exposedPorts(new Integer[]{port});
        return self();
    }


    /**
     * Set the container startup timeout.
     *
     * @param startupTimeoutInSeconds Container startup timeout
     * @return
     */
    public SELF setStartupTimeoutInSeconds(int startupTimeoutInSeconds) {
        this.startupTimeoutInSeconds = startupTimeoutInSeconds;
        return self();
    }


    /**
     * Set client and container connection timeouts.
     *
     * @param connectTimeoutInSeconds Client and container connection timeout
     * @return
     */
    public SELF setConnectTimeoutInSeconds(int connectTimeoutInSeconds) {
        this.connectTimeoutInSeconds = connectTimeoutInSeconds;
        return self();
    }

    /**
     * Setup database initialization script.
     *
     * @param scriptPath database initialization script
     * @return
     */
    public SELF setInitScript(String scriptPath) {
        this.initScriptPath = scriptPath;
        return self();
    }


    @Override
    protected void containerStarted() {
        runInitScript();
    }

    @Override
    protected void blockUntilContainerStarted() {
        long containerStartMills = System.currentTimeMillis();
        try {
            while (System.currentTimeMillis() < containerStartMills + (TIME_UNIT * startupTimeoutInSeconds)) {
                try {
                    if (!isRunning()) {
                        Thread.sleep(90L);
                        continue;
                    }

                    try (Connection connection = createConnection("")) {

                        boolean testQueryResult = connection.createStatement().execute(getTestQueryCmd());
                        if (testQueryResult) {
                            break;
                        }

                    }
                } catch (DriverNotFoundException e) {
                    LOGGER.info("no driver found.", e);
                    throw e;
                } catch (Exception e) {
                    LOGGER.info("Failure when trying test query", e);
                    e.printStackTrace();
                    Thread.sleep(90L);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Container wait was interrupted", e);
        }
    }


    /**
     * Get the jdbc driver instance.
     *
     * @return
     * @throws DriverNotFoundException
     */
    public Driver getJdbcDriverInstance() throws DriverNotFoundException {

        synchronized (DRIVER_LOAD_LOCK) {
            if (driver == null) {
                try {
                    Class<?> driverClass = Class.forName(getDriverClassName());
                    driver = (Driver) driverClass.newInstance();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    throw new DriverNotFoundException("Could not get Jdbc Driver", e);
                }
            }
        }

        return driver;
    }

    /**
     * Create database connection until timeout.
     */
    public Connection createConnection(String queryString) throws SQLException, DriverNotFoundException {
        final Properties propertiesInfo = getPropertiesInfo();

        final String url = buildConnectionUrl(queryString);

        final Driver jdbcDriverInstance = getJdbcDriverInstance();

        long connectionStartMs = System.currentTimeMillis();
        SQLException exception = null;
        try {
            while (System.currentTimeMillis() < connectionStartMs + (TIME_UNIT * connectTimeoutInSeconds) && isRunning()) {
                try {
                    LOGGER.info("Trying to create JDBC connection using {} to {} with properties: {}", driver.getClass().getName(), url, propertiesInfo);
                    return jdbcDriverInstance.connect(url, propertiesInfo);
                } catch (SQLException e) {
                    exception = e;
                    Thread.sleep(90L);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        throw new SQLException("Could not create new connection", exception);
    }

    private Properties getPropertiesInfo() {
        final Properties info = new Properties();
        info.put("user", this.getUsername());
        info.put("password", this.getPassword());
        return info;
    }

    /**
     * Assemble jdbc url.
     */
    protected String buildConnectionUrl(String query) {
        String baseUrl = getJdbcUrl();

        if (null==query||"".equals(query)) {
            return baseUrl;
        }

        if (!query.startsWith(QUESTION_MASK)) {
            throw new IllegalArgumentException("The '?' character must be included");
        }

        return baseUrl.contains(QUESTION_MASK)
                ? baseUrl + QUERY_PARAM_SEPARATOR + query.substring(1)
                : baseUrl + query;
    }

    /**
     * Assemble jdbc parameters into strings.
     */
    protected String buildUrlParameters(String startCharacter, String delimiter) {
        String urlParameters = "";
        String endCharacter = "";
        if (!this.urlParameterMap.isEmpty()) {
            String additionalParameters = this.urlParameterMap.entrySet().stream()
                    .map(Object::toString)
                    .collect(joining(delimiter));
            StringBuilder sb = new StringBuilder();
            sb.append(startCharacter);
            sb.append(additionalParameters);
            sb.append(endCharacter);
            urlParameters = sb.toString();
        }
        return urlParameters;
    }


    /**
     * Run the database initialization script.
     */
    protected void runInitScript() {
        if (initScriptPath != null) {
            ScriptUtils.runScript(initScriptPath, getDatabaseDelegate());
        }
    }


    /**
     * Get database startup timeout.
     *
     * @return
     */
    protected int getStartupTimeoutInSeconds() {
        return startupTimeoutInSeconds;
    }


    /**
     * Get database connection timeout.
     *
     * @return
     */
    protected int getConnectTimeoutInSeconds() {
        return connectTimeoutInSeconds;
    }

    /**
     * Get the proxy for the database container.
     *
     * @return
     */
    protected DatabaseDelegate getDatabaseDelegate() {
        return new JdbcDataBaseDelegate(this, "");
    }

    public static class DriverNotFoundException extends RuntimeException {
        public DriverNotFoundException(String message, Throwable e) {
            super(message, e);
        }
    }
}
