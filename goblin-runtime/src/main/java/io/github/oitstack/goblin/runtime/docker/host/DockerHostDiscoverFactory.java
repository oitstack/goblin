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

import java.util.ServiceLoader;

/**
 * There are various strategies for acquiring docker hosts,
 * such as local configuration, network acquisition, etc.
 * In addition, developers can also define them by themselves.
 *
 * @author yangguang
 * @date 2022/5/23
 */
public class DockerHostDiscoverFactory {

    /**
     * Docker host acquisition strategies.
     */
    private ServiceLoader<DockerHostDiscoverStrategy> discoverStrategies;

    /**
     * The local acquisition strategy of the docker host, which is also the default strategy.
     */
    private static final DockerHostDiscoverStrategy DEFAULT_STRATEGY = new DockerHostLocalDiscoverStrategy();

    private DockerHostDiscoverFactory() {
        discoverStrategies = ServiceLoader.load(DockerHostDiscoverStrategy.class);
    }

    static class DockerHostDiscoverFactoryHolder {
        public static DockerHostDiscoverFactory INSTANCE = new DockerHostDiscoverFactory();
    }


    /**
     * Get a DockerHostDiscoverFactory instance.
     * @return
     */
    public static DockerHostDiscoverFactory getInstance() {
        return DockerHostDiscoverFactoryHolder.INSTANCE;
    }


    /**
     * Get docker host discovery policy based on class name.
     *
     * @param fullClassName
     * @return
     */
    public DockerHostDiscoverStrategy getDockerHostDiscoverStrategy(String fullClassName) {
        for (DockerHostDiscoverStrategy strategy : discoverStrategies) {
            if (strategy.getClass().getName().equalsIgnoreCase(fullClassName)) {
                return strategy;
            }
        }
        return DEFAULT_STRATEGY;
    }

}
