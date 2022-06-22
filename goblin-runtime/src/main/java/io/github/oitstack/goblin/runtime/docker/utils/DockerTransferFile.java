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
package io.github.oitstack.goblin.runtime.docker.utils;


import com.github.dockerjava.api.DockerClient;
import io.github.oitstack.goblin.runtime.transfer.MountableFile;
import io.github.oitstack.goblin.runtime.transfer.TransferFile;

/**
 * @Author CuttleFish
 * @Date 2022/3/11 下午6:13
 */
public class DockerTransferFile extends TransferFile {
    private DockerClient dockerClient;

    public DockerTransferFile(DockerClient dockerClient, MountableFile mf) {
        super(mf);
        this.dockerClient = dockerClient;
    }

    public static DockerTransferFile forHostPath(DockerClient dockerClient, String filePath) {
        return new DockerTransferFile(dockerClient, MountableFile.generateFromHostPath(filePath));
    }

    @Override
    public void transferTo(String runtimeId, String runtimePath) {
        DockerUtils.transferFileToDocker(this.dockerClient, runtimeId, this.mountableFile, runtimePath);
    }
}
