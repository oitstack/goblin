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
package io.github.oitstack.goblin.runtime.docker.container;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import com.google.common.collect.ImmutableMap;
import io.github.oitstack.goblin.runtime.Runtime;
import io.github.oitstack.goblin.runtime.RuntimeAdapter;
import io.github.oitstack.goblin.runtime.config.RunTimeConfig;
import io.github.oitstack.goblin.runtime.constant.Constants;
import io.github.oitstack.goblin.runtime.docker.client.AutoRecycleDockerClient;
import io.github.oitstack.goblin.runtime.docker.client.GoblinDockerClient;
import io.github.oitstack.goblin.runtime.docker.image.DockerImage;
import io.github.oitstack.goblin.runtime.docker.utils.*;
import io.github.oitstack.goblin.runtime.transfer.MountableFile;
import io.github.oitstack.goblin.runtime.utils.PlatformUtils;
import io.github.oitstack.goblin.runtime.utils.Preconditions;
import io.github.oitstack.goblin.runtime.transfer.TransferFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The implementation of the basic operations of the docker container. If you want to implement a custom container,
 * you can directly inherit this class, and then configure the implementation class according to the SPI.
 *
 * @Author CuttleFish
 * @Date 2022/2/18 下午6:11
 */
public class DockerContainerAdapter<T extends DockerContainerAdapter<T>> extends RuntimeAdapter<T> {

    private final static Logger LOGGER = LoggerFactory.getLogger(DockerContainerAdapter.class);
    private GoblinDockerClient goblinDockerClient;
    /**
     * Environment variables that need to be injected into the container to be started.
     */
    private final Map<String, String> envs = new HashMap<>();

    /**
     * image to start the container
     */
    private DockerImage image;

    private final List<T> others = new ArrayList<>();

    /**
     * List of container operation commands
     */
    private final List<String> commands = new ArrayList<>();

    private boolean privilegedMode = false;

    /**
     * id of started container.
     */
    private String containerId;


    private final List<Bind> binds = new ArrayList<>();

    /**
     * List of ports exposed by the container
     */
    private Integer[] exposedPorts = null;

    /**
     * Files that need to be copied into the container
     */
    private Map<TransferFile, String> needCopyFiles = new HashMap<>();

    private static final String MD5_LABEL = "goblin_container_md5_label";

    public DockerContainerAdapter() {
        this(AutoRecycleDockerClient.getInstance());
    }

    public DockerContainerAdapter(GoblinDockerClient goblinDockerClient) {
        this.goblinDockerClient = goblinDockerClient;
    }

    @Override
    public T image(String image) {
        this.image = DockerImage.parseFrom(image,this.goblinDockerClient);
        return self();
    }

    @Override
    public T addLink(T other) {
        others.add(other);
        return self();
    }

    @Override
    public T addEnv(String key, String value) {
        this.envs.put(key, value);
        return self();
    }

    @Override
    public T addCommandWhenStartup(String command) {
        this.commands.add(command);
        return self();
    }

    @Override
    public T privilegedMode(boolean privilegedMode) {
        this.privilegedMode = privilegedMode;
        return self();
    }

    public T addBind(Bind bind) {
        this.binds.add(bind);
        return self();
    }

    @Override
    public T exposedPorts(Integer[] ports) {
        this.exposedPorts = ports;
        return self();
    }


    @Override
    public String getHost() {
        return this.goblinDockerClient.getDockerHostIpAddress();
    }

    @Override
    public Integer getPortByInnerPort(Integer innerPort) {
        InspectContainerResponse inspectContainerResponse
                = this.goblinDockerClient.inspectContainerCmd(this.containerId).exec();
        return DockerUtils.getFirstMappedPort(inspectContainerResponse, Arrays.asList(new ExposedPort(innerPort)));
    }


    @Override
    public String getImageName() {
        return image.getImageName().toIdentifyName();
    }

    @Override
    public String getEnv(String key) {
        return this.envs.get(key);
    }

    @Override
    public String getRuntimeId() {
        return this.containerId;
    }

    @Override
    public boolean getPrivilegedMode() {
        return privilegedMode;
    }

    @Override
    public List<Bind> getBinds() {
        return this.binds;
    }

    @Override
    public boolean isRunning() {
        InspectContainerResponse inspectContainerResponse
                = this.goblinDockerClient.inspectContainerCmd(this.containerId).exec();
        return inspectContainerResponse != null
                && inspectContainerResponse.getState() != null
                && inspectContainerResponse.getState().getRunning();
    }

    @Override
    protected void doInit() {
    }

    @Override
    public CompletableFuture<Runtime> doStart() {
        Preconditions.checkNotNull(this.image, "image cannot be null");
        CompletableFuture<Runtime> result = new CompletableFuture();

        image.getPulled().whenComplete((r, ex) -> {
            if (null != ex) {
                result.completeExceptionally(ex);
                return;
            }
            try {
                CreateContainerCmd createContainerCmd = buildCreateCommand();
                String label = encodeCommandExceptForGroup(createContainerCmd);
                if (RunTimeConfig.getInstance().getContainerEnableReuseFlag()) {
                    this.containerId = findContainerByLabel(label);
                }
                if (StringUtils.isBlank(this.containerId)) {
                    createContainerCmd.getLabels().put(MD5_LABEL, label);
                    this.containerId = createContainerCmd.exec().getId();
                    doCopyFileToContainerWhenStartup();
                    containerPreStart();
                    this.goblinDockerClient.startContainerCmd(this.containerId).exec();
                    blockUntilContainerStarted();
                    containerStarted();
                } else {
                    LOGGER.info("reuse container, imageInfo={}, containerId={}, cmd={}.",
                            image, containerId, JsonTool.toJSONString(createContainerCmd));
                }
                result.complete(this);
            } catch (Exception ex2) {
                result.completeExceptionally(ex2);
            }
        });
        return result;
    }


    private CreateContainerCmd buildCreateCommand() {
        List<String> envList = envs.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.toList());

        CreateContainerCmd createContainerCmd = this.goblinDockerClient
                .createContainerCmd(image.getImageName().toIdentifyName())
                .withEnv(envList)
                .withPrivileged(privilegedMode)
                .withPublishAllPorts(true)
                .withBinds(binds);

        if (this.exposedPorts != null) {
            createContainerCmd.withExposedPorts(Stream.of(this.exposedPorts).map(ExposedPort::new).collect(Collectors.toList()));
        }
        if (commands.size() > 0) {
            createContainerCmd.withCmd(commands);
        }
        return createContainerCmd;
    }

    /**
     * md5 of the command, except for label of group.
     *
     * @param createContainerCmd
     * @return
     */
    private String encodeCommandExceptForGroup(CreateContainerCmd createContainerCmd) {
        Map<String, String> labels = createContainerCmd.getLabels();
        String group = labels.get(Constants.LABEL_NAME_GROUP);
        labels.remove(Constants.LABEL_NAME_GROUP);

        String cmdJson = JsonTool.toJSONString(createContainerCmd);
        String cmdMd5 = UUID.randomUUID().toString();
        if (StringUtils.isNotBlank(cmdJson)) {
            cmdMd5 = MD5Util.signature(cmdJson);
        }

        labels.put(Constants.LABEL_NAME_GROUP, group);
        return cmdMd5;
    }

    /**
     * Find containers from host by label.
     *
     * @param label
     * @return
     */
    private String findContainerByLabel(String label) {

        return StringUtils.isNotBlank(label) ?
                this.goblinDockerClient.listContainersCmd()
                        .withLabelFilter(ImmutableMap.of(MD5_LABEL, label))
                        .withLimit(1)
                        .withStatusFilter(Arrays.asList("running"))
                        .exec()
                        .stream()
                        .findAny()
                        .map(it -> it.getId())
                        .orElse("") : "";
    }

    /**
     * The action performed before the container is started.
     * If necessary, you can override it to implement custom container services.
     */
    private void containerPreStart() {
    }

    /**
     * The action performed after the container is started,
     * If necessary, you can override it to implement custom container services.
     */
    protected void containerStarted() {
    }

    /**
     * If some actions of your custom container need to be executed after the container is started,
     * you can override this method to block until the container is started.
     */
    protected void blockUntilContainerStarted() {
    }


    public T addFileSystemBind(final String hostPath, final String containerPath) {
        if (PlatformUtils.IS_WINDOWS && hostPath.startsWith("/")) {
            binds.add(new Bind(hostPath, new Volume(containerPath), AccessMode.DEFAULT, SELContext.DEFAULT));

        } else {
            binds.add(new Bind(MountableFile.generateFromHostPath(hostPath).getResolvedPath(), new Volume(containerPath), AccessMode.DEFAULT, SELContext.DEFAULT));
        }
        return self();
    }

    @Override
    public T addCopyFileToRuntimeWhenStartup(TransferFile transferFile, String containerPath) {
        Preconditions.checkTrue(transferFile instanceof DockerTransferFile, "transferFile must instanceof DockerTransferFile");
        needCopyFiles.put(transferFile, containerPath);
        return self();
    }


    private void doCopyFileToContainerWhenStartup() {
        Preconditions.checkNotNull(this.containerId, "copyFileToContainer can only be used with created / running container");
        needCopyFiles.forEach((transferFile, containerPath) -> {
            transferFile.transferTo(this.containerId, containerPath);
        });

    }


    @Override
    public boolean shutdown() {
        return false;
    }

    @Override
    public boolean transferFileToRuntime(String filePath, String containerPath) {
        DockerTransferFile.forHostPath(this.goblinDockerClient,filePath).transferTo(this.containerId, containerPath);
        return true;
    }

    @Override
    public ExecResult execInRuntime(Charset outputCharset, String... commands) {
        return DockerUtils.runCommandInContainer(this.goblinDockerClient,this.containerId, commands);
    }

}

