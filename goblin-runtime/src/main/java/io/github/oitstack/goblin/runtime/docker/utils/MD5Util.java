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
package io.github.oitstack.goblin.runtime.docker.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MD5 signature tool class.
 */
public class MD5Util {

    public final static Logger LOGGER = LoggerFactory.getLogger(MD5Util.class);

    private MD5Util() {
    }

    /**
     * Sign content.
     * @param content
     * @return
     */
    public static String signature(String content) {
        try {
            return DigestUtils.md5Hex(content);
        } catch (Exception e) {
            LOGGER.error("md5 sign failed. e", e);
            return "";
        }
    }

}
