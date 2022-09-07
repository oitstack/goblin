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
package io.github.oitstack.goblin.runtime;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Container status and other information.
 * @Author CuttleFish
 * @Date 2022/2/18 下午9:13
 */
public interface RuntimeState<D extends RuntimeState> {
    default Set<Integer> getLivenessCheckPortNumbers() {

        final Set<Integer> result = getExposedPorts().stream().map(this::getPortByInnerPort).distinct().collect(Collectors.toSet());

//        result.addAll(getBoundPortNumbers());
        return result;
    }

    List<Integer> getExposedPorts();

    /**
     * The host where the container is located.
     * @return
     */
    String getHost();

    /**
     * Find the corresponding port exposed by the container on the host
     * according to the container exposed port.
     * @param innerPort
     * @return
     */
    Integer getPortByInnerPort(Integer innerPort);

    /**
     * Get image name
     * @return
     */
    String getImageName();

    /**
     * Get environment variables.
     * @param key
     * @return
     */
    String getEnv(String key);

    /**
     * Get container id.
     * @return
     */
    String getRuntimeId();


    /**
     * Get privileged mode.
     * @return
     */
    boolean getPrivilegedMode();

    /**
     *Get binds.
     * @return
     */
    List<Bind> getBinds();

    /**
     * Whether the container is running.
     * @return
     */
    boolean isRunning();

}
