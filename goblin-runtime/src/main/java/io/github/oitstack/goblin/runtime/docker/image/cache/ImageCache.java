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
package io.github.oitstack.goblin.runtime.docker.image.cache;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import io.github.oitstack.goblin.runtime.docker.image.DockerImageName;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache the images that the current client has pulled.
 * If an image has already been pulled, it will not be pulled again.
 * @Author CuttleFish
 * @Date 2022/3/1 下午12:25
 */
public class ImageCache implements IImageCache{

    /**
     * The cache of the docker image, the key is DockerImageName,
     * and the value is the detailed information of the image
     */
    private Map<DockerImageName, ImageData> cache = new ConcurrentHashMap<>();

    private ImageCache() {
    }

    /**
     * Get a ImageCache instance.
     *
     * @return
     */
    public static ImageCache getInstance() {
        return ImageCacheHolder.INSTANCE;
    }

    /**
     * Refresh cache.
     * @param dockerImageName
     * @param dockerClient
     * @return
     */
    public ImageData refresh(DockerImageName dockerImageName, DockerClient dockerClient) {
        //TODO:
        InspectImageResponse response = null;
        try {
            response = dockerClient.inspectImageCmd(dockerImageName.toIdentifyName()).exec();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        if (response != null) {
            ImageData imageData = ImageData.from(response);
            cache.put(dockerImageName, imageData);
            return imageData;
        } else {
            cache.remove(dockerImageName);
            return null;
        }
    }


    public ImageData get(DockerImageName dockerImageName) {
        return cache.get(dockerImageName);
    }

    static class ImageCacheHolder {
        public static final ImageCache INSTANCE = new ImageCache();
    }


    /**
     * This class encapsulates the details of the docker image.
     */
    @Data
    @Builder
    public static class ImageData {
        public Long time;

        static ImageData from(InspectImageResponse inspectImageResponse) {
            return ImageData.builder()
                    .time(ZonedDateTime.parse(inspectImageResponse.getCreated()).toEpochSecond())
                    .build();
        }
    }

}
