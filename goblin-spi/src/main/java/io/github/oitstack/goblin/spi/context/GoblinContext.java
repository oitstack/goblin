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
package io.github.oitstack.goblin.spi.context;

import java.util.Map;

/**
 * Goblin running context.
 * @author yangguang
 * @date 2022/5/27
 */
public class GoblinContext {

    /**
     * Placeholder, mainly set after the container is started.
     */
    private Map<String, String> placeHolder;

    /**
     * Mapping classes for Goblin configuration files.
     */
    private Configuration configuration;

    public GoblinContext(Map<String, String> placeHolder, Configuration configuration) {
        this.placeHolder = placeHolder;
        this.configuration = configuration;
    }

    /**
     * Get placeHolder.
     * @return
     */
    public Map<String, String> getPlaceHolder() {
        return placeHolder;
    }

    /**
     * Set placeHolder.
     * @param placeHolder
     */
    public void setPlaceHolder(Map<String, String> placeHolder) {
        this.placeHolder = placeHolder;
    }

    /**
     * Get Configuration.
     * @return
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Set Configuration.
     * @param configuration
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
