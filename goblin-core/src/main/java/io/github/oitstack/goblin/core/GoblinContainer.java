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

import io.github.oitstack.goblin.spi.context.Image;

import java.util.Map;

/**
 * The interface for Goblin Container. If you need to define a container,
 * you must implement this interface
 */
public interface GoblinContainer {

    /**
     * Start the specified container
     * @param goblin goblin instance
     * @param config config for custom container
     * @param image Configured image information
     */
    void start(Goblin goblin, Object config, Image image);

    /**
     * Get the placeholder set after the current container is started.
     * @return
     */
    Map<String, String> getPlaceHolders();

    /**
     * Get the type of container, such as MYSQL, etc.
     * @return
     */
    String getContainerType();

    /**
     * class of corresponding config.
     * @return
     */
    Class configClass();
}
