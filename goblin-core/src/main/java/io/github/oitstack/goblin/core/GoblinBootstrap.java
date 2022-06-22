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
package io.github.oitstack.goblin.core;

import io.github.oitstack.goblin.core.utils.ConfigParseUtils;
import io.github.oitstack.goblin.spi.context.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * Goblin startup class
 */
public class GoblinBootstrap {

    private final static Logger LOGGER = LoggerFactory.getLogger(GoblinBootstrap.class);

    private static final String GOBLIN_CONFIG_FILE = "goblin.yml";

    private GoblinBootstrap() {
        setThreadExceptionHandler();
        Goblin.getInstance().startup(buildCoreConfiguration(parsePropertiesFromFile()));
    }

    /**
     * Get a singleton GoblinBootstrap instance.
     * @return
     */
    public static GoblinBootstrap getInstance() {
        return GoblinBootStrapInstanceHolder.INSTANCE;
    }

    static class GoblinBootStrapInstanceHolder {
        public static GoblinBootstrap INSTANCE = new GoblinBootstrap();
    }

    /**
     * Parsing Goblin configuration files.
     * @return
     */
    private Map<String, Object> parsePropertiesFromFile() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(GOBLIN_CONFIG_FILE);
        Map<String, Object> properties = new Yaml().load(inputStream);

        return properties;
    }

    /**
     *
     * @param confMap
     * @return
     */
    private Configuration buildCoreConfiguration(Map<String, Object> confMap) {
        Configuration configuration = ConfigParseUtils.map2Pojo(confMap, Configuration.class);
        configuration.setConfMap(confMap);
        return configuration;
    }


    private void setThreadExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LOGGER.error(String.format("ThreadId: %s, error msg: %s", t.getName(), e.getMessage()));
            e.printStackTrace();
        });
    }

}
