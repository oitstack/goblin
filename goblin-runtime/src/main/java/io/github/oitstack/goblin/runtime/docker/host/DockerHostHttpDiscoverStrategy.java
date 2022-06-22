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
package io.github.oitstack.goblin.runtime.docker.host;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.classic.methods.HttpGet;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.config.RequestConfig;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.ParseException;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.io.entity.EntityUtils;
import io.github.oitstack.goblin.runtime.docker.utils.StringUtils;
import io.github.oitstack.goblin.runtime.utils.PropertyAndEnvUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Get docker host from remote via http request.
 * @author yangguang
 * @date 2022/5/23
 */
public class DockerHostHttpDiscoverStrategy implements DockerHostDiscoverStrategy {
    private final static Logger LOGGER = LoggerFactory.getLogger(DockerHostHttpDiscoverStrategy.class);

    @Override
    public List<String> discover() {
        String url = PropertyAndEnvUtils.get(DockerHostSelector.DOCKER_HOST_LIST_ENDPOINT);
        if (StringUtils.isBlank(url)) {
            LOGGER.error("DOCKER_HOST_LIST_ENDPOINT is not configured.");
            throw new RuntimeException("DOCKER_HOST_LIST_ENDPOINT is not configured.");
        }
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(100L, TimeUnit.MILLISECONDS)
                .setConnectionRequestTimeout(100L, TimeUnit.MILLISECONDS)
                .setResponseTimeout(100L, TimeUnit.MILLISECONDS)
                .build();

        List<String> dockerHostList = new ArrayList<>();
        try (CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build()) {
            HttpGet getMethod = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(getMethod)) {

                String content = EntityUtils.toString(response.getEntity());
                LOGGER.info("get dockerhost list from remote server. response was: {}, {}", response.getCode(), content);
                if (response.getCode() != 200) {
                    throw new RuntimeException(String.format("get dockerhost list from remote server failed. code:", response.getCode()));
                }

                Arrays.asList(content.split(",")).stream().forEach(hostpost -> dockerHostList.add(hostpost));
                EntityUtils.consume(response.getEntity());
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return dockerHostList;
    }
}
