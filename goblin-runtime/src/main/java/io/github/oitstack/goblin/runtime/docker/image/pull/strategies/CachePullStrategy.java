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
import io.github.oitstack.goblin.runtime.docker.image.pull.fetchers.decorators.CacheExistsImageFetcherDecorator;
import io.github.oitstack.goblin.runtime.docker.image.pull.fetchers.decorators.RefreshCacheImageFetcherDecorator;

/**
 * Image pull strategy that use the cache first, then flush the cache.
 * @Author CuttleFish
 * @Date 2022/2/28 下午2:24
 */
public class CachePullStrategy implements PullStrategy {

    /**
     * Image fetch strategy.
     */
    private ImageFetcher fetcher =
            new CacheExistsImageFetcherDecorator(new RefreshCacheImageFetcherDecorator(new DirectImageFetcher()));

    @Override
    public boolean pull(DockerImageName imageName, DockerClient dockerClient) {
        return fetcher.fetch(imageName,dockerClient).getUpStreamResult();
    }
}
