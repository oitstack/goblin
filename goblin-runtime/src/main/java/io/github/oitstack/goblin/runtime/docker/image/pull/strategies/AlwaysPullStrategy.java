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
package io.github.oitstack.goblin.runtime.docker.image.pull.strategies;

import com.github.dockerjava.api.DockerClient;
import io.github.oitstack.goblin.runtime.docker.image.DockerImageName;
import io.github.oitstack.goblin.runtime.docker.image.pull.fetchers.DirectImageFetcher;
import io.github.oitstack.goblin.runtime.docker.image.pull.fetchers.ImageFetcher;

/**
 * Image pull strategy that pulls every time.
 * @Author CuttleFish
 * @Date 2022/2/28 下午2:23
 */
public class AlwaysPullStrategy implements PullStrategy {
    private ImageFetcher fetcher = new DirectImageFetcher();

    /**
     * Pull image by image name of docker.
     * @param imageName
     * @return
     */
    @Override
    public boolean pull(DockerImageName imageName, DockerClient dockerClient) {
        return fetcher.fetch(imageName,dockerClient).getUpStreamResult();
    }
}
