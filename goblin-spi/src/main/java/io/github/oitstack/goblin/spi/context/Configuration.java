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
 * Mapping classes for Goblin configuration files.
 */
public class Configuration {

    /**
     * User-configured docker image.
     */
    private Image[] dockerImages;

    /**
     * User-configured environment variables.
     */
    private Map<String, String> env;

    /**
     * User-configured system properties.
     */
    private Map<String, String> props;

    private Map<String, Object> confMap;

    public Image[] getDockerImages() {
        return dockerImages;
    }

    public void setDockerImages(Image[] dockerImages) {
        this.dockerImages = dockerImages;
    }


    public Map<String, String> getEnv() {
        return env;
    }

    public void setEnv(Map<String, String> env) {
        this.env = env;
    }

    public Map<String, String> getProps() {
        return props;
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
    }

    public Map<String, Object> getConfMap() {
        return confMap;
    }

    public void setConfMap(Map<String, Object> confMap) {
        this.confMap = confMap;
    }
}
