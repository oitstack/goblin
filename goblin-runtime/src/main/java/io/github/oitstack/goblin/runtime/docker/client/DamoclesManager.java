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
package io.github.oitstack.goblin.runtime.docker.client;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Volume;
import io.github.oitstack.goblin.runtime.config.RunTimeConfig;
import io.github.oitstack.goblin.runtime.docker.image.DockerImageName;
import io.github.oitstack.goblin.runtime.docker.image.cache.ImageCache;
import io.github.oitstack.goblin.runtime.docker.utils.DockerUtils;
import io.github.oitstack.goblin.runtime.docker.utils.StringUtils;
import io.github.oitstack.goblin.runtime.utils.MixAll;
import io.github.oitstack.goblin.runtime.utils.PlatformUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Damocles is a container for reliable cleaning of containers.
 * The Goblin client starts the Damocles container first when it starts up.
 * The Damocles container and the docker client and containers started by this client have the same label.
 * When the docker client shuts down, Damocles container closes other containers with this label.
 *
 * @Author CuttleFish
 * @Date 2022/3/7 上午10:22
 */
public class DamoclesManager {
    private final static Logger LOGGER = LoggerFactory.getLogger(DamoclesManager.class);
    private DockerClient client;
    private ImageCache imageCache = ImageCache.getInstance();

    public DamoclesManager(DockerClient client) {
        this.client = client;

    }

    /**
     * The port exposed by the Damocles container
     */
    private static final int EXPOSED_PORT = 8080;

    private static final String DEFAULT_DAMOCLES_TAG = "oitstack/damocles:v0.0.2";
    private AtomicBoolean damoclesStarted = new AtomicBoolean(false);

    /**
     * The docker host where containers is located
     *
     * @param clientIdentify label that identifies the client
     * @param dockerHost
     */
    public void startAndRegisterClient(String clientIdentify, URI dockerHost) {
        if (damoclesStarted.compareAndSet(false, true)) {
            String damoclesTag = RunTimeConfig.getInstance().getDamoclesTag();
            DockerImageName imageName = DockerImageName.parseFrom(StringUtils.isBlank(damoclesTag) ? DEFAULT_DAMOCLES_TAG : damoclesTag);

            try {
                pullDamoclesImage(this.client, imageName);

                String containerId = MixAll.retry(3, () -> startUpDamocles(clientIdentify, dockerHost, imageName));
                if (null != containerId) {
                    connectToDamocles(this.client, dockerHost, containerId);
                } else {
                    LOGGER.error("Damocles StartUp Failed.");
                }

            } catch (InterruptedException e) {
                LOGGER.error("Damocles StartUp Failed", e);
            }
        }

    }

    /**
     * Pull the container image to the docker host, if it already exists, do not pull it.
     *
     * @param client    docker client
     * @param imageName docker image name
     * @throws InterruptedException
     */
    private synchronized void pullDamoclesImage(DockerClient client, DockerImageName imageName) throws InterruptedException {
        ImageCache.ImageData imageData = this.imageCache.get(imageName);
        if (null != imageData) {
            return;
        }

        if (!DockerUtils.imageExist(client, imageName)) {
            client.pullImageCmd(imageName.toIdentifyName())
                    .withTag(imageName.getVersion().getVersionDesc())
                    .start()
                    .awaitCompletion();
        }


        this.imageCache.refresh(imageName, client);
    }

    /**
     * The docker client establishes a connection with Damocles and sends a heartbeat.
     * When the connection between the docker client and Damocles is closed or the heartbeat times out,
     * Damocles will close the container with the same label as Damocles
     *
     * @param client      docker client
     * @param dockerHost  container host
     * @param containerId container id
     */
    private static void connectToDamocles(DockerClient client, URI dockerHost, String containerId) {
        InspectContainerResponse inspectContainerResponse = client.inspectContainerCmd(containerId).exec();
        Integer port = DockerUtils.getFirstMappedPort(inspectContainerResponse, Arrays.asList(new ExposedPort(EXPOSED_PORT)));
        String host = DockerUtils.getDockerHostIpAddress(client, dockerHost);

        Thread connThread = new Thread(() -> {
            Socket clientSocket = new Socket();
            try {
                clientSocket.connect(new InetSocketAddress(host, port), 5 * 1000);
                OutputStream output = clientSocket.getOutputStream();
                Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                    try {
                        PrintWriter writer = new PrintWriter(output, true);
                        writer.println("PING");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 3, 1, TimeUnit.SECONDS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        connThread.setDaemon(true);
        connThread.setName("damocles client");
        connThread.start();

    }

    /**
     * Start the Damocles container.
     */
    private String startUpDamocles(String clientIdentify, URI dockerHost, DockerImageName imageName) {
        CreateContainerCmd createContainerCmd = this.client.createContainerCmd(imageName.toIdentifyName());
        List<Bind> binds = new ArrayList<>();
        binds.add(new Bind(getRemoteDockerUnixSocketPath(dockerHost), new Volume("/var/run/docker.sock")));
        String containerId = createContainerCmd
                .withBinds(binds)
                .withExposedPorts(new ExposedPort(EXPOSED_PORT))
                .withName("goblin-damocles-" + clientIdentify)
                .withPublishAllPorts(true)
                .withPrivileged(true)
                .withEnv("targetName=group=" + clientIdentify).exec().getId();


        client.startContainerCmd(containerId).exec();
        return containerId;
    }

    public String getRemoteDockerUnixSocketPath(URI dockerHost) {
        String dockerSocketOverride = RunTimeConfig.getInstance().get(RunTimeConfig.DOCKER_SOCKET_OVERRIDE);
        if (!StringUtils.isBlank(dockerSocketOverride)) {
            return dockerSocketOverride;
        }

        String path = "unix".equals(dockerHost.getScheme())
                ? dockerHost.getRawPath()
                : "/var/run/docker.sock";
        return PlatformUtils.IS_WINDOWS
                ? "/" + path
                : path;
    }

    public ImageCache getImageCache() {
        return imageCache;
    }

    public void setImageCache(ImageCache imageCache) {
        this.imageCache = imageCache;
    }
}
