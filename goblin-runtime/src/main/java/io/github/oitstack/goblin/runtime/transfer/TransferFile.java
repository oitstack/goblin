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
package io.github.oitstack.goblin.runtime.transfer;

import lombok.experimental.Delegate;

/**
 * Template class for transferring files.
 * @Author CuttleFish
 * @Date 2022/3/11 下午5:39
 */
public abstract class TransferFile {
    @Delegate
    protected MountableFile mountableFile;

    public TransferFile(MountableFile mf) {
        this.mountableFile = mf;
    }

    /**
     * Transfer the file to the specified location in the container.
     * @param runtimeId container id
     * @param runtimePath file path in the container
     */
    public abstract void transferTo(String runtimeId, String runtimePath);
}
