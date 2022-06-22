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

import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.core.*;
import lombok.experimental.Delegate;

import java.io.Serializable;
import java.util.Properties;


/**
 * This class manage goblin docker client config.
 * @Author CuttleFish
 * @Date 2022/3/3 下午2:55
 */
public class GoblinDockerClientConfig implements Serializable, DockerClientConfig {
    private static final long serialVersionUID = 1L;
    @Delegate(excludes = ExcludeMethods.class)
    DockerClientConfig dockerClientConfig;


    public GoblinDockerClientConfig(DockerClientConfig dockerClientConfig) {
        this.dockerClientConfig = dockerClientConfig;
    }

    /**
     * Get docker authentication configuration.
     * @param imageName docker image name
     * @return
     */
    @Override
    public AuthConfig effectiveAuthConfig(String imageName) {

        NameParser.ReposTag reposTag = NameParser.parseRepositoryTag(imageName);
        NameParser.HostnameReposName hostnameReposName = NameParser.resolveRepositoryName(reposTag.repos);

        AuthConfig authConfig = AuthConfigLocator.getInstance().locale(hostnameReposName, null);
        if (authConfig != null) {
            return authConfig;
        }
        authConfig = dockerClientConfig.effectiveAuthConfig(imageName);


        if (authConfig != null) {
            return authConfig;
        }
        return getAuthConfig();
    }


    private AuthConfig getAuthConfig() {
        AuthConfig authConfig = null;
        if (getRegistryUsername() != null && getRegistryPassword() != null && getRegistryUrl() != null) {
            authConfig = new AuthConfig()
                    .withUsername(getRegistryUsername())
                    .withPassword(getRegistryPassword())
                    .withEmail(getRegistryEmail())
                    .withRegistryAddress(getRegistryUrl());
        }
        return authConfig;
    }


    interface ExcludeMethods {
        public AuthConfig effectiveAuthConfig(String imageName);

    }


    public static class Builder {

        private Boolean dockerTlsVerify;

        private String dockerCerPath;

        private Properties properties;

        private RemoteApiVersion apiVersion;

        /**
         * This will set all fields in the builder to those contained in the Properties object. The Properties object should contain the
         * following docker-java configuration keys: DOCKER_HOST, DOCKER_TLS_VERIFY, api.version, registry.username, registry.password,
         * registry.email, DOCKER_CERT_PATH, and DOCKER_CONFIG.
         */
        public GoblinDockerClientConfig.Builder withProperties(Properties p) {
            this.properties = p;
            withDockerTlsVerify(p.getProperty(DefaultDockerClientConfig.DOCKER_TLS_VERIFY));
            withDockerCertPath(p.getProperty(DefaultDockerClientConfig.DOCKER_CERT_PATH));
            return this;
        }

        public GoblinDockerClientConfig.Builder withApiVersion(RemoteApiVersion version) {
            this.apiVersion = version;
            return this;
        }

        public final GoblinDockerClientConfig.Builder withDockerTlsVerify(String dockerTlsVerify) {
            if (dockerTlsVerify != null) {
                String trimmed = dockerTlsVerify.trim();
                this.dockerTlsVerify = "true".equalsIgnoreCase(trimmed) || "1".equals(trimmed);
            } else {
                this.dockerTlsVerify = false;
            }
            return this;
        }

        private void withDockerCertPath(String dockerCerPath) {
            this.dockerCerPath = dockerCerPath;
        }

        public GoblinDockerClientConfig build() {
            DefaultDockerClientConfig.Builder defaultBuilder = DefaultDockerClientConfig.createDefaultConfigBuilder();
            if (null != properties) {
                defaultBuilder.withProperties(properties);
            }
            if (null != this.apiVersion) {
                defaultBuilder.withApiVersion(this.apiVersion);
            }

            if (dockerTlsVerify) {
                defaultBuilder.withCustomSslConfig(new GoblinSSLConfig(dockerCerPath));
            }

            return new GoblinDockerClientConfig(defaultBuilder.build());
        }

    }
}
