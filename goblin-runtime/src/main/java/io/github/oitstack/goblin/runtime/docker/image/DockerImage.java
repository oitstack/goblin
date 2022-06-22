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
package io.github.oitstack.goblin.runtime.docker.image;

import com.github.dockerjava.api.DockerClient;
import io.github.oitstack.goblin.runtime.docker.client.AutoRecycleDockerClient;
import io.github.oitstack.goblin.runtime.docker.image.pull.PullStrategyContext;
import io.github.oitstack.goblin.runtime.docker.image.pull.PullStrategyFactory;
import io.github.oitstack.goblin.runtime.docker.image.pull.strategies.PullStrategy;
import lombok.Data;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible for the management of docker images.
 * @Author CuttleFish
 * @Date 2022/2/25 下午3:05
 */
@Data
public class DockerImage {

    /**
     * Related names of docker.
     */
    private DockerImageName imageName;

    /**
     * Future of image pull status.
     * @see
     */
    protected CompletableFuture<Boolean> pulled;

    /**
     * Image pull strategy
     */
    private String pullStrategy = "AGE";

    /**
     * Docker client, used to operate on docker.
     */
    private DockerClient dockerClient;

    /**
     * Get image pull strategy.
     * @return
     */
    public String getPullStrategy() {
        return pullStrategy;
    }


    /**
     * Pull the docker image.
     * @return
     */
    private Boolean pull() {
        PullStrategy pullStrategy = PullStrategyFactory.getInstance().getPullStrategy(getPullStrategy());
        return new PullStrategyContext().pull(pullStrategy, this.imageName, this.dockerClient);

    }

    /**
     * Whether image has been pulled successfully.
     * @return pull status
     */
    private Boolean hasAlready() {
        try {
            return pulled.get(100, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private DockerImage(DockerClient dockerClient, DockerImageName imageName, String pullStrategy) {
        this.dockerClient = dockerClient;
        this.imageName = imageName;
        this.pullStrategy = pullStrategy;
        //希望定义image时就去pull，节省等待时间，而不是使用时才去pull
        this.pulled = CompletableFuture.supplyAsync(() -> pull());
    }

    public static DockerImage parseFrom(String o) {
        return parseFrom(o, PullStrategyFactory.CACHE);
    }
    public static DockerImage parseFrom(String o, String pullStrategy) {
        return parseFrom(o, pullStrategy, AutoRecycleDockerClient.getInstance());
    }

    public static DockerImage parseFrom(String o, DockerClient dockerClient) {
        return parseFrom(o, PullStrategyFactory.CACHE, dockerClient);
    }

    public static DockerImage parseFrom(String o, String pullStrategy, DockerClient dockerClient) {
        return new DockerImage(dockerClient, DockerImageName.parseFrom(o), pullStrategy);
    }
}
