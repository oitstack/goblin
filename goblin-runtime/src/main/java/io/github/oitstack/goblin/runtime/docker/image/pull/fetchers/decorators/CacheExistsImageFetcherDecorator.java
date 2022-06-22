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
package io.github.oitstack.goblin.runtime.docker.image.pull.fetchers.decorators;

import com.github.dockerjava.api.DockerClient;
import io.github.oitstack.goblin.runtime.docker.image.cache.ImageCache;
import io.github.oitstack.goblin.runtime.docker.image.DockerImageName;
import io.github.oitstack.goblin.runtime.docker.image.pull.fetchers.FetchContext;
import io.github.oitstack.goblin.runtime.docker.image.pull.fetchers.ImageFetcher;

/**
 * Use cache decorator for image fetches.
 * @Author CuttleFish
 * @Date 2022/2/28 下午5:10
 */
public class CacheExistsImageFetcherDecorator implements ImageFetcher {

    /**
     * The image fetcher that will be decorated.
     */
    private final ImageFetcher imageFetcher;

    public CacheExistsImageFetcherDecorator(ImageFetcher fetcher) {
        this.imageFetcher = fetcher;
    }

    /**
     * Before fetching the image, determine whether there is a cache. If there is a cache, use it.
     */
    @Override
    public FetchContext fetch(DockerImageName dockerImageName, DockerClient dockerClient) {
        ImageCache.ImageData imageData = ImageCache.getInstance().get(dockerImageName);
        if (null != imageData) {
            return FetchContext.builder().pulledTimeStamp(System.currentTimeMillis()).upStreamResult(true).build();
        } else {
            FetchContext context = imageFetcher.fetch(dockerImageName,dockerClient);
            return context;
        }
    }
}
