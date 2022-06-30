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
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.Ports;
import io.github.oitstack.goblin.runtime.RuntimeOperation;
import io.github.oitstack.goblin.runtime.docker.image.DockerImageName;
import io.github.oitstack.goblin.runtime.transfer.MountableFile;
import lombok.experimental.UtilityClass;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Docker operation tool class.
 *
 * @Author CuttleFish
 * @Date 2022/3/7 下午3:35
 */
@UtilityClass
public class DockerUtils {
    public final static Logger LOGGER = LoggerFactory.getLogger(DockerUtils.class);

    public static final boolean IN_A_CONTAINER = new File("/.dockerenv").exists();

    /**
     * Obtain the corresponding docker host port according to the docker container port.
     *
     * @param inspectContainerResponse docker container information
     * @param originalPort             Port exposed by docker container
     * @return
     */
    public static Integer getMappedPort(InspectContainerResponse inspectContainerResponse, int originalPort) {

        Ports.Binding[] binding = new Ports.Binding[0];
        final InspectContainerResponse containerInfo = inspectContainerResponse;
        if (containerInfo != null) {
            binding = containerInfo.getNetworkSettings().getPorts().getBindings().get(new ExposedPort(originalPort));
        }

        if (binding != null && binding.length > 0 && binding[0] != null) {
            return Integer.valueOf(binding[0].getHostPortSpec());
        } else {
            throw new IllegalArgumentException("Requested port (" + originalPort + ") is not mapped");
        }
    }

    public static Integer getFirstMappedPort(InspectContainerResponse inspectContainerResponse, List<ExposedPort> exposedPorts) {
        return exposedPorts
                .stream()
                .findFirst()
                .map(t -> getMappedPort(inspectContainerResponse, t.getPort()))
                .orElseThrow(() -> new IllegalStateException("Container doesn't expose any ports"));
    }

    public static String getDockerHostIpAddress(DockerClient client, URI dockerHost) {

        switch (dockerHost.getScheme()) {
            case "http":
            case "https":
            case "tcp":
                return dockerHost.getHost();
            case "unix":
            case "npipe":
                if (IN_A_CONTAINER) {
                    return client.inspectNetworkCmd()
                            .withNetworkId("bridge")
                            .exec()
                            .getIpam()
                            .getConfig()
                            .stream()
                            .filter(it -> it.getGateway() != null)
                            .findAny()
                            .map(Network.Ipam.Config::getGateway)
                            .orElseGet(() -> {
                                return "localhost";
                            });
                }
                return "localhost";
            default:
                return null;
        }
    }


    public static void transferFileToDocker(DockerClient dockerClient, String containerId, MountableFile hostFile, String containerFile) {
        try (
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                TarArchiveOutputStream tarArchive = new TarArchiveOutputStream(byteArrayOutputStream)
        ) {
            tarArchive.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);


            TarArchiveEntry tarEntry = new TarArchiveEntry(containerFile);
            tarEntry.setSize(hostFile.getSize());
            tarEntry.setMode(hostFile.getFileMode());

            try {
                tarArchive.putArchiveEntry(tarEntry);
                IOUtils.write(hostFile.getBytes(), tarArchive);
                tarArchive.closeArchiveEntry();
            } catch (IOException e) {
                throw new RuntimeException("Can't transfer " + containerFile, e);
            }

            tarArchive.finish();

            dockerClient
                    .copyArchiveToContainerCmd(containerId)
                    .withTarInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()))
                    .withRemotePath("/")
                    .exec();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static RuntimeOperation.ExecResult runCommandInContainer(DockerClient dockerClient, String containerId, String... commands) {
        final ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                .withAttachStdout(true).withAttachStderr(true).withCmd(commands).exec();


        ExecResultCallback callback = new ExecResultCallback();

        try {
            dockerClient.execStartCmd(execCreateCmdResponse.getId()).exec(callback).awaitCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        Integer exitCode = dockerClient.inspectExecCmd(execCreateCmdResponse.getId()).exec().getExitCode();

        final RuntimeOperation.ExecResult result = new RuntimeOperation.ExecResult(
                String.valueOf(exitCode),
                callback.getStdout(),
                callback.getStderr());
        return result;
    }

    /**
     *  Determine whether the current docker image exists on target docker host.
     */
    public static boolean imageExist(DockerClient client, DockerImageName imageName) {
        List<Image> imageList = client.listImagesCmd().withImageNameFilter(imageName.toIdentifyName()).exec();
        if (imageList == null || imageList.isEmpty()) {
            return false;
        }

        boolean exist = imageList.stream()
                .anyMatch(image -> {
                    if (image.getRepoTags() == null) {
                        return false;
                    }
                    for (String tag : image.getRepoTags()) {
                        if (imageName.getRawName().equals(tag)) {
                            return true;
                        }
                    }

                    return false;
                });

        LOGGER.debug("docker image exist:{} in target host, imageRawName={}.", exist, imageName.getRawName());
        return exist;
    }
}
