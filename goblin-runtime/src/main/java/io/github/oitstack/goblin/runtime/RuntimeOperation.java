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

import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.charset.Charset;

/**
 * An interface that defines the basic operations in a running container.
 * @Author CuttleFish
 * @Date 2022/2/18 下午9:09
 */
public interface RuntimeOperation<D extends RuntimeOperation> {

    /**
     * Copy the file to the container.
     * @param filePath file path to be uploaded
     * @param containerPath The location of the file in the container
     * @return
     */
    boolean transferFileToRuntime(String filePath, String containerPath);

    /**
     * execute container command
     * @param command container operation command
     * @return
     */
    default RuntimeOperation.ExecResult execInRuntime(String... command) {
        return execInRuntime(Charset.forName("UTF-8"), command);
    }

    /**
     * execute container command
     * @param outputCharset character set
     * @param command container operation command
     * @return
     */
    RuntimeOperation.ExecResult execInRuntime(Charset outputCharset, String... command);

    @Data
    @AllArgsConstructor
    class ExecResult {
        String exitCode;
        String stdout;
        String stderr;
    }
}
