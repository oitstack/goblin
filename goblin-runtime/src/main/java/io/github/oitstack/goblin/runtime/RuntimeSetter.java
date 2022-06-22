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


import io.github.oitstack.goblin.runtime.config.Configure;
import io.github.oitstack.goblin.runtime.transfer.TransferFile;

/**
 * Container property settings.
 * @Author CuttleFish
 * @Date 2022/3/1 下午7:42
 */
public interface RuntimeSetter<D extends RuntimeSetter> {

    /**
     * Set up container images
     * @param image
     * @return
     */
    D image(String image);

    /**
     * Add container link.
     * @param other
     * @return
     */
    D addLink(D other);

    /**
     * Add container environment variables.
     * @param key environment variable key
     * @param value environment variable value
     * @return
     */
    D addEnv(String key, String value);

    /**
     * Add the command when the container starts.
     * @param command command to add
     * @return
     */
    D addCommandWhenStartup(String command);

    /**
     * Set privileged mode.
     * @param privilegedMode
     * @return
     */
    D privilegedMode(boolean privilegedMode);

    /**
     * Set configuration.
     * @param configure configuration to be configured
     * @return
     */
    D config(Configure configure);

    /**
     * Set ports exposed by the container
     * @param ports
     * @return
     */
    D exposedPorts(Integer[] ports);

    /**
     * Add files to the container when the container starts.
     * @param transferFile file path to be transferred
     * @param containerPath The location of the file in the container
     * @return
     */
    D addCopyFileToRuntimeWhenStartup(TransferFile transferFile, String containerPath);
}
