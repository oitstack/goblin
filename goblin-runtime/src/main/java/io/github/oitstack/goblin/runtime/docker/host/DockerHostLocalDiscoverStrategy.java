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
package io.github.oitstack.goblin.runtime.docker.host;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import io.github.oitstack.goblin.runtime.config.RunTimeConfig;
import io.github.oitstack.goblin.runtime.docker.utils.StringUtils;
import io.github.oitstack.goblin.runtime.utils.PropertyAndEnvUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Get docker host from local config
 * @author yangguang
 * @date 2022/5/23
 */
public class DockerHostLocalDiscoverStrategy implements DockerHostDiscoverStrategy {

    private final static Logger LOGGER = LoggerFactory.getLogger(DockerHostLocalDiscoverStrategy.class);

    @Override
    public List<String> discover() {

        String dockerHosts = PropertyAndEnvUtils.get(RunTimeConfig.RUNTIME_CONFIG_PREFIX + DefaultDockerClientConfig.DOCKER_HOST);
        if (StringUtils.isBlank(dockerHosts)) {
            LOGGER.error("{} is not configured.", RunTimeConfig.RUNTIME_CONFIG_PREFIX + DefaultDockerClientConfig.DOCKER_HOST);
            throw new RuntimeException(String.format("%s is not configured",
                    RunTimeConfig.RUNTIME_CONFIG_PREFIX + DefaultDockerClientConfig.DOCKER_HOST));
        }
        return Arrays.asList(dockerHosts.split(","));
    }
}
