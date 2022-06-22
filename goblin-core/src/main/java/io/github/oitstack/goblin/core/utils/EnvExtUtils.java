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
package io.github.oitstack.goblin.core.utils;

import com.google.common.annotations.VisibleForTesting;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Environment variable tool class.
 * @Author CuttleFish
 * @Date 2021/8/18 下午2:57
 */
public class EnvExtUtils {

    private static final String JAVA_PROCESS_ENVIRONMENT_CLASS = "java.lang.ProcessEnvironment";
    private static final String CASE_INSENSITIVE_ENVIRONMENT_FILED_NAME = "theCaseInsensitiveEnvironment";
    private static final String UN_MODIFIED_ENVIRONMENT_FILED_NAME = "theUnmodifiableEnvironment";
    private static final String MODIFIERS_ENVIRONMENT_FILED_NAME = "modifiers";

    /**
     * Environment variables to be expanded.
     */
    @VisibleForTesting
    public static final Map<String, String> EXT_ENVS = new HashMap<>();

    /**
     * Add environment variable.
     * @param source
     */
    public static void addAll(Map<String, String> source) {
        if (null == source) {
            return;
        }
        EXT_ENVS.putAll(source);
    }

    /**
     * Set environment variable.
     * @param key
     * @param value
     */
    public static void put(String key, String value) {
        EXT_ENVS.put(key, value);
    }

    /**
     * Remove environment variable.
     * @param key
     */
    public static void remove(String key) {
        EXT_ENVS.remove(key);
    }

    /**
     * Get environment variable.
     * @param key
     * @return
     */
    public String get(String key) {
        return EXT_ENVS.get(key);
    }

    /**
     * Inject environment variables set by the user into the operating system.
     */
    public static void injectEnv2Os() {
        try {
            Class envCls = Class.forName(JAVA_PROCESS_ENVIRONMENT_CLASS);
            Field[] fields = envCls.getDeclaredFields();
            for (Field field : fields) {
                if (CASE_INSENSITIVE_ENVIRONMENT_FILED_NAME.equals(field.getName())) {
                    field.setAccessible(true);
                    Map<String, String> envMap = (Map<String, String>) field.get(null);

                    for (Map.Entry<String, String> entry : EXT_ENVS.entrySet()) {
                        envMap.put(entry.getKey(), entry.getValue());
                    }
                }
                if (UN_MODIFIED_ENVIRONMENT_FILED_NAME.equals(field.getName())) {
                    Field modifiersField = Field.class.getDeclaredField(MODIFIERS_ENVIRONMENT_FILED_NAME);
                    modifiersField.setAccessible(true);
                    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                    field.setAccessible(true);
                    Map<String, String> envMap = (Map<String, String>) field.get(null);
                    Map<String, String> envMapTmp = new HashMap<>(envMap);
                    for (Map.Entry<String, String> entry : EXT_ENVS.entrySet()) {
                        envMapTmp.put(entry.getKey(), entry.getValue());
                    }

                    Map<String, String> newEnvMap = Collections.unmodifiableMap(envMapTmp);

                    field.set(null, newEnvMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
