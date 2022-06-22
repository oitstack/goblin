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
 * Refresh cache decorator for image fetches.
 * @Author CuttleFish
 * @Date 2022/3/1 下午12:56
 */
public class RefreshCacheImageFetcherDecorator implements ImageFetcher {

    /**
     * The image fetcher that will be decorated.
     */
    private final ImageFetcher imageFetcher;

    public RefreshCacheImageFetcherDecorator(ImageFetcher fetcher) {
        this.imageFetcher = fetcher;
    }

    /**
     * Fetch docker image and refresh cache.
     */
    @Override
    public FetchContext fetch(DockerImageName dockerImageName, DockerClient dockerClient) {
        FetchContext context = imageFetcher.fetch(dockerImageName,dockerClient);
        if (context.getUpStreamResult()) {
            ImageCache.ImageData newImageData = ImageCache.getInstance().refresh(dockerImageName,dockerClient);
            return FetchContext.builder().pulledTimeStamp(newImageData.getTime()).upStreamResult(true).build();

        } else {
            return context.toBuilder().pulledTimeStamp(System.currentTimeMillis()).upStreamResult(false).build();
        }
    }
}
