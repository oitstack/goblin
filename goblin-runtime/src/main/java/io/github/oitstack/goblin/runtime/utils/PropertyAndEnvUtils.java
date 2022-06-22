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
package io.github.oitstack.goblin.runtime.utils;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * A collection of utility methods to retrieve and
 * parse the values of the Java system properties.
 */
public final class PropertyAndEnvUtils {


    private PropertyAndEnvUtils() {
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(final String key, String def) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("key must not be empty.");
        }


        String value = null;
        value = getFromEnv(key, value);

        //get from property
        if (value == null) {
            value = System.getProperty(key);
        }


        if (value == null) {
            return def;
        } else {
            return value;
        }
    }

    private static String getFromEnv(String key, String value) {
        try {
            if (System.getSecurityManager() != null) {
                value = AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(key));

            } else {
                value = System.getProperty(key);
            }
        } catch (Exception e) {
        }
        return value;
    }


    /**
     * Sets the value of the Java system property with the
     * specified {@code key}
     */
    public static Object setProperty(String key, String value) {
        return System.getProperties().setProperty(key, value);
    }


}
