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
package io.github.oitstack.goblin.runtime.docker.image;

import java.util.function.Predicate;

/**
 * @Author CuttleFish
 * @Date 2022/2/25 下午3:31
 */
public abstract class DockerImageVersion {
    DockerImageVersion(String version) {
        support(version);
    }

    public abstract String getSeparator();

    public abstract String getVersionDesc();

    public abstract boolean support(String version);

    public static final Predicate TAG_VERSION_PREDICATE = new Predicate<String>() {
        public static final String TAG_REGEX = "[\\w][\\w.\\-]{0,127}";

        @Override
        public boolean test(String o) {
            return o.matches(TAG_REGEX);
        }
    };

    public static final Predicate SHA256_VERSION_PREDICATE = new Predicate<String>() {
        public static final String HASH_REGEX = "[0-9a-fA-F]{32,}";

        @Override
        public boolean test(String o) {
            return o.matches(HASH_REGEX);
        }
    };

    public static class AnyVersion extends DockerImageVersion {
        static final TagVersion LATEST_VERSION = new TagVersion("latest");

        AnyVersion() {
            super(null);
        }

        @Override
        public String getSeparator() {
            return LATEST_VERSION.getSeparator();
        }

        @Override
        public String getVersionDesc() {
            return LATEST_VERSION.getVersionDesc();
        }

        @Override
        public boolean support(String version) {
            return true;
        }
    }

    public static class TagVersion extends DockerImageVersion {

        static final TagVersion LATEST = new TagVersion("latest");
        private final String version;

        TagVersion(String tag) {
            super(tag);
            this.version = tag;
        }

        @Override
        public String getSeparator() {
            return ":";
        }

        @Override
        public String getVersionDesc() {
            return version;
        }

        @Override
        public boolean support(String version) {
            return TAG_VERSION_PREDICATE.test(version);
        }
    }

    public static class Sha256Version extends DockerImageVersion {
        private final String version;

        Sha256Version(String hash) {
            super(hash);
            this.version = hash;
        }

        @Override
        public String getSeparator() {
            return "@";
        }

        @Override
        public String getVersionDesc() {
            return version;
        }

        @Override
        public boolean support(String version) {
            return SHA256_VERSION_PREDICATE.test(version);
        }
    }


}
