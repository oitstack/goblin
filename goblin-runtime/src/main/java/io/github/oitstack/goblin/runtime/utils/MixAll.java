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


import io.github.oitstack.goblin.runtime.config.Configure;

import java.security.SecureRandom;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * A tool class that provides various functions.
 * @Author CuttleFish
 * @Date 2022/3/1 下午8:24
 */
public class MixAll {
    public static final ThreadFactory NAMED_THREAD_FACTORY = new ThreadFactory() {
        private String NAME_PREFIX = "goblin-thread-";
        private AtomicLong threadSeqNum = new AtomicLong(0L);

        @Override
        public Thread newThread(Runnable runnable) {
            Thread t = new Thread(runnable);
            t.setName(NAME_PREFIX + threadSeqNum.getAndIncrement());
            return t;
        }
    };

    public static Configure.ConfigureBuilder getPureConfigureBuilder() {
        return Configure.builder();
    }

    public static Configure.ConfigureBuilder getMixConfigureBuilder() {
        return Configure.builder();
    }

    /**
     * Attempt to execute the specified method until it succeeds or reaches the maximum times.
     *
     * @param retries number of attempts
     * @param supplier method to be executed
     *
     * @param <T> The result returned by the method to be executed.
     * @return
     */
    public static <T> T retry(int retries, Supplier<T> supplier) {
        int i = 0;
        while (i < retries) {
            T result = supplier.get();
            i++;
            if (result == null) {
                continue;
            } else {
                return result;
            }
        }
        return null;
    }

    private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
            .toCharArray();

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Get random string of specified length.
     * @param length length of target string
     * @return
     */
    public static String randomString(int length) {
        char[] result = new char[length];

        for (int i = 0; i < length; i++) {
            char pick = ALPHABET[RANDOM.nextInt(ALPHABET.length)];
            result[i] = pick;
        }

        return new String(result);
    }

}
