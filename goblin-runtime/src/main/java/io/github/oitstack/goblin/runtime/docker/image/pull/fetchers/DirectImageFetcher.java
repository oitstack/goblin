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
package io.github.oitstack.goblin.runtime.docker.image.pull.fetchers;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import io.github.oitstack.goblin.runtime.docker.image.DockerImageName;

import java.util.List;

/**
 * Implementation of pull images, in a straightforward way.
 * @Author CuttleFish
 * @Date 2022/2/28 下午5:10
 */
public class DirectImageFetcher implements ImageFetcher {

    /**
     * Get the image directly.
     * @param dockerImageName
     * @return
     */
    @Override
    public FetchContext fetch(DockerImageName dockerImageName, DockerClient dockerClient) {
        FetchContext fetchContext = FetchContext.builder().upStreamResult(false).pulledTimeStamp(System.currentTimeMillis()).build();
        try {
            List<Image> imageList = dockerClient
                    .listImagesCmd()
                    .withImageNameFilter(dockerImageName.toIdentifyName())
                    .exec();

            if (imageList == null || imageList.isEmpty()) {
                dockerClient.pullImageCmd(dockerImageName.toIdentifyName())
                        .withTag(dockerImageName.getVersion().getVersionDesc())
                        .start()
                        .awaitCompletion();
            }


            fetchContext = fetchContext.toBuilder()
                    .pulledTimeStamp(System.currentTimeMillis())
                    .upStreamResult(true)
                    .build();
        } catch (InterruptedException e) {
            e.printStackTrace();
            fetchContext = fetchContext.toBuilder()
                    .exception(e.getMessage())
                    .pulledTimeStamp(System.currentTimeMillis())
                    .upStreamResult(false)
                    .build();
        }
        return fetchContext;
    }
}
