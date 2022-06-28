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

import io.github.oitstack.goblin.runtime.docker.utils.StringUtils;
import io.github.oitstack.goblin.runtime.utils.PropertyAndEnvUtils;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * There are multiple docker hosts obtained, this class is used to select from multiple hosts.
 * @author yangguang
 * @date 2022/5/24
 */
public class DockerHostSelector {

    private final static Logger LOGGER = LoggerFactory.getLogger(DockerHostSelector.class);

    public final static String DOCKER_HOST_LIST_ENDPOINT = "DOCKER_HOST_LIST_ENDPOINT";
    public final static String DOCKER_HOST_DISCOVER_STRATEGY = "DOCKER_HOST_DISCOVER_STRATEGY";

    /**
     * Selected available docker host.
     */
    private String selectedDockerHost;

    private DockerHostSelector() {
        loadDockerHost();
    }

    /**
     * Get a DockerHostSelector instance.
     * @return
     */
    public static DockerHostSelector getInstance() {
        return DockerHostConfigHolder.INSTANCE;
    }

    static class DockerHostConfigHolder {
        public static DockerHostSelector INSTANCE = new DockerHostSelector();
    }

    private void loadDockerHost() {

        DockerHostDiscoverStrategy dockerHostDiscoverStrategy
                = DockerHostDiscoverFactory.getInstance().getDockerHostDiscoverStrategy(
                PropertyAndEnvUtils.get(DOCKER_HOST_DISCOVER_STRATEGY)
        );

        String selectedDockerHost = selectOneValidHost(dockerHostDiscoverStrategy.discover());
        if (StringUtils.isBlank(selectedDockerHost)) {
            throw new RuntimeException("no valid docker host.");
        }
        this.selectedDockerHost = selectedDockerHost;
    }

    /**
     * Select an available one from multiple docker hosts,
     * and the availability is judged by the telnet command.
     * @param dockerHostList
     * @return
     */
    public String selectOneValidHost(List<String> dockerHostList) {
        if (null == dockerHostList || dockerHostList.size() == 0) {
            LOGGER.error("docker host list is empty");
            return null;
        }

        List<String> shuffledList = new ArrayList<>();
        shuffledList.addAll(dockerHostList);
        Collections.shuffle(shuffledList);

        String dockerHost = shuffledList.stream().filter(
                e -> {
                    TelnetClient telnetClient = new TelnetClient();
                    String[] hostport = e.split(":");
                    boolean succ = false;
                    try {
                        telnetClient.connect(hostport[0], Integer.parseInt(hostport[1]));
                        succ = true;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        try {
                            telnetClient.disconnect();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    return succ;
                }
        ).findFirst().get();

        return dockerHost;
    }

    /**
     * Get selected docker host.
     * @return
     */
    public String getSelectedDockerHost() {
        return selectedDockerHost;
    }
}
