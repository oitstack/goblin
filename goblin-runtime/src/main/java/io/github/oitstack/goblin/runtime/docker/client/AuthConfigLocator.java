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
import com.github.dockerjava.core.NameParser;
import io.github.oitstack.goblin.runtime.config.RunTimeConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to load authentication configuration information.
 * @Author CuttleFish
 * @Date 2022/3/3 下午3:18
 */
public class AuthConfigLocator {
    private Map<NameParser.HostnameReposName, AuthConfig>
            cache = new ConcurrentHashMap<>();

    private AuthConfigLocator() {
        loadFromGlobalConfig();
    }

    private void loadFromGlobalConfig() {
        String authConfigStr = RunTimeConfig.getInstance().get(RunTimeConfig.AUTH_CONFIGS);
        if (null == authConfigStr || "".equals(authConfigStr)) {
            return;
        }
        //TODO:
//        cache= ObjectMapper
    }

    public static AuthConfigLocator getInstance() {
        return AuthConfigLocatorHolder.INSTANCE;
    }

    static class AuthConfigLocatorHolder {
        public static AuthConfigLocator INSTANCE = new AuthConfigLocator();
    }

    public AuthConfig locale(NameParser.HostnameReposName hostReposName, AuthConfig defaultAuthConfig) {
        return cache.getOrDefault(hostReposName, defaultAuthConfig);
    }
}
