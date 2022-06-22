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
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Version;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.RemoteApiVersion;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import io.github.oitstack.goblin.runtime.utils.PropertyAndEnvUtils;
import io.github.oitstack.goblin.runtime.config.RunTimeConfig;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Delegate;
import io.github.oitstack.goblin.runtime.docker.utils.DockerUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * A custom docker client with automatic container recycling.
 *
 * @Author CuttleFish
 * @Date 2022/2/18 下午7:01
 */
@Data
public class AutoRecycleDockerClient implements GoblinDockerClient {

    private static final String LABEL_NAME_GROUP = "group";

    /**
     * docker client.
     */
    @Delegate(excludes = ExcludeMethods.class)
    private DockerClient client;
    private DamoclesManager damoclesManager;
    private boolean needStartDamocles = "false".equals(PropertyAndEnvUtils.get("enable_damocles")) ? false : true;
    /**
     * docker client metadata.
     */
    private MetaData metaData;

    private AutoRecycleDockerClient() {

        initRightVersionClient(RemoteApiVersion.VERSION_1_20, 0, 3);
        if (needStartDamocles) {
            this.damoclesManager = new DamoclesManager(this.client);
            this.damoclesManager.startAndRegisterClient(this.metaData.getClientIdentify(), this.metaData.getDockerHost());
        }
    }


    /**
     * Initialize the docker client so that the client version is not lower than the server version.
     *
     * @param remoteApiVersion current version
     * @param retries          number of retries
     * @param maxRetries       maximum number of retries
     */
    private void initRightVersionClient(RemoteApiVersion remoteApiVersion, int retries, int maxRetries) {
        if (retries >= maxRetries) {
            return;
        }
        Properties p = getDockerProperties();
        GoblinDockerClientConfig.Builder dccb = new GoblinDockerClientConfig.Builder()
                .withProperties(p).withApiVersion(remoteApiVersion);
        GoblinDockerClientConfig dcc = dccb.build();

        DockerHttpClient dockerHttpClient = new ZerodepDockerHttpClient.Builder()
                .dockerHost(dcc.getDockerHost())
                .sslConfig(dcc.getSSLConfig())
                .build();

        this.client = DockerClientImpl.getInstance(dcc, dockerHttpClient);
        Info dockerInfo = client.infoCmd().exec();
        Version version = client.versionCmd().exec();

        RemoteApiVersion realVersion = RemoteApiVersion.parseConfig(version.getApiVersion());
        if (realVersion.isGreater(remoteApiVersion)) {
            initRightVersionClient(realVersion, retries++, maxRetries);
        }
        this.metaData = new MetaData(dcc.getDockerHost(), dockerInfo, version, this.client);
    }


    /**
     * Get docker client configuration.
     *
     * @return
     */
    private Properties getDockerProperties() {
        Properties p = new Properties();
        RunTimeConfig.getInstance().getAllProperties().forEach((k, v) -> {
            String key = (String) k;
            if (key.startsWith(RunTimeConfig.RUNTIME_CONFIG_PREFIX)) {
                p.put(key.substring(RunTimeConfig.RUNTIME_CONFIG_PREFIX.length()), v);
            }
        });
        return p;
    }

    /**
     * Get a AutoRecycleDockerClient instance.
     *
     * @return
     */
    public static AutoRecycleDockerClient getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Assemble the create container command with label,
     * All containers during goblin operation use this label,
     * which is convenient for destroying the container when goblin terminates
     *
     * @param image
     * @return
     */
    @Override
    public CreateContainerCmd createContainerCmd(String image) {
        Map<String, String> labels = new HashMap<>();
        labels.put(LABEL_NAME_GROUP, this.getMetaData().getClientIdentify());
        return client.createContainerCmd(image).withLabels(labels);
    }

    @Override
    public String getDockerHostIpAddress() {
        return this.getMetaData().getDockerHostIpAddress();
    }


    static class InstanceHolder {
        private static AutoRecycleDockerClient INSTANCE = new AutoRecycleDockerClient();
    }

    interface ExcludeMethods {
        CreateContainerCmd createContainerCmd(String image);
    }

    /**
     * Metadata that the docker client sees during runtime.
     */
    @Data
    public static class MetaData {
        private URI dockerHost;
        private Info dockerInfo;
        private Version version;
        private DockerClient refDockerClient;
        @Getter(lazy = true)
        private final String dockerHostIpAddress = resolve();

        public String resolve() {
            return DockerUtils.getDockerHostIpAddress(this.refDockerClient, this.dockerHost);
        }

        public MetaData(URI dockerHost, Info dockerInfo, Version version, DockerClient refDockerClient) {
            this.dockerHost = dockerHost;
            this.dockerInfo = dockerInfo;
            this.version = version;
            this.refDockerClient = refDockerClient;
        }

        /**
         * The unique identifier of the docker client, used to label the container.
         */
        private String clientIdentify = UUID.randomUUID().toString();

        public String getClientIdentify() {
            return clientIdentify;
        }
    }
}
