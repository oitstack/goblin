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
package io.github.oitstack.goblin.runtime.config;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import io.github.oitstack.goblin.runtime.docker.host.DockerHostSelector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Goblin runtime configuration.
 */
public final class RunTimeConfig {
    public final static String RUNTIME_CONFIG_PREFIX = "GOBLIN_RUNTIME_";
    private final static String CONFIG_FILE_PATH = "goblin_runtime.properties";
    public final static String AUTH_CONFIGS = RUNTIME_CONFIG_PREFIX + "DOCKER_AUTH_CONFIG";
    public final static String PREFIX_PROPERTY_KEY = RUNTIME_CONFIG_PREFIX + "default_docker_hub";
    public final static String DOCKER_SOCKET_OVERRIDE = RUNTIME_CONFIG_PREFIX + "_docker_socket_override";
    public final static String REUSE_CONTAINER_ENABLE = RUNTIME_CONFIG_PREFIX + "reuse_container_enable";
    public final static String DAMOCLES_TAG = RUNTIME_CONFIG_PREFIX + "DAMOCLES_TAG";
    public final static String DOCKER_HOST_PREFIX = "tcp://";

    /**
     * All configurations, including those loaded from configuration files,
     * obtained from environment variables, obtained from system properties, etc.
     */
    Properties properties;

    /**
     * Get a RunTimeConfig instance.
     * @return
     */
    public static RunTimeConfig getInstance() {
        return GlobalConfigHolder.INSTANCE;
    }

    private RunTimeConfig() {
        properties = loadConfig();
    }

    /**
     * Config loading.
     * @return
     */
    private Properties loadConfig() {
        Properties properties = new Properties();
        overrideFromEnv(properties);
        overrideFromSystemProperty(properties);
        overrideFromConfigFile(properties);
        overrideDockerHost(properties);

        return properties;
    }

    /**
     * Fill docker host config.
     * @param properties
     */
    private void overrideDockerHost(Properties properties) {
        String selectedDockerHost = DockerHostSelector.getInstance().getSelectedDockerHost();
        properties.put(RUNTIME_CONFIG_PREFIX + DefaultDockerClientConfig.DOCKER_HOST, DOCKER_HOST_PREFIX + selectedDockerHost);
    }


    /**
     * Fill config from config file.
     * @param properties
     */
    private void overrideFromConfigFile(Properties properties) {
        Properties prosFromConfigFile = new Properties();
        try (InputStream is = DefaultDockerClientConfig.class.getResourceAsStream("/" + CONFIG_FILE_PATH)) {
            if (is != null) {
                prosFromConfigFile.load(is);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        replaceProperties(prosFromConfigFile, properties);

        prosFromConfigFile.forEach((k, v) -> {
            if (!((k instanceof String) && (v instanceof String))) {
                return;
            }
            properties.put(k, v);
        });
    }

    /**
     * Fill config from system properties.
     * @param properties
     */
    private void overrideFromSystemProperty(Properties properties) {
        Properties systemProperties = System.getProperties();
        if (null != systemProperties) {
            systemProperties.forEach((k, v) -> {
                if (!((k instanceof String) && (v instanceof String))) {
                    return;
                }
                properties.put(k, v);
            });
        }
    }

    /**
     * Fill config from environment variables.
     * @param properties
     */
    private void overrideFromEnv(Properties properties) {
        Map<String, String> envMap = System.getenv();
        if (null != envMap) {
            envMap.entrySet().stream().filter(e -> e.getKey() != null).forEach(e -> {
                properties.put(e.getKey(), e.getValue());
            });
        }
    }

    private static void replaceProperties(Properties configProperties, Properties replacements) {
        for (Object objectKey : configProperties.keySet()) {
            String key = objectKey.toString();
            configProperties.setProperty(key, replaceProperties(configProperties.getProperty(key), replacements));
        }
    }

    /**
     * Replace placeholders, placeholders start with ${ and end with }.
     * @param s source to be replaced.
     * @param replacements
     * @return target to replace.
     */
    private static String replaceProperties(String s, Properties replacements) {
        for (Map.Entry<Object, Object> entry : replacements.entrySet()) {
            String key = "${" + entry.getKey() + "}";
            while (s.contains(key)) {
                s = s.replace(key, String.valueOf(entry.getValue()));
            }
        }
        return s;
    }


    /**
     * Get all configuration.
     * @return
     */
    public Properties getAllProperties() {
        return properties;
    }

    /**
     * Get the configuration of the specified key.
     * @param key  key of properties.
     * @return
     */
    public String get(String key) {
        return properties.getProperty(key);
    }

    /**
     * Get the configuration of the specified key, use default value when no value is available.
     * @param key key of properties.
     * @param def Use default value when no value is available
     * @return
     */
    public String getOrDefault(String key, String def) {
        return properties.getProperty(key) != null ? properties.getProperty(key) : def;
    }

    static class GlobalConfigHolder {
        static RunTimeConfig INSTANCE = new RunTimeConfig();
    }


    /**
     * Whether to allow reuse of existing containers.
     * If allowed, when a container already exists, it will not restart a new container, but reuse it.
     * @return
     */
    public boolean getContainerEnableReuseFlag() {
        return Boolean.parseBoolean(this.getOrDefault(RunTimeConfig.REUSE_CONTAINER_ENABLE, "false"));
    }

    /**
     * Get the tag of the damocles image.
     * @return
     */
    public String getDamoclesTag() {
        return this.get(RunTimeConfig.DAMOCLES_TAG);
    }

}
