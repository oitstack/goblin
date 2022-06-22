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
package io.github.oitstack.goblin.spi.context;

import java.util.Arrays;

/**
 * Mapping classes for docker images.
 */
public class Image {
    /**
     * image tag, such as mysql:5.7.
     */
    private String imageVersion;

    /**
     * type of image, such as MYSQL.
     */
    private String type;

    private String id;

    /**
     * The container that the current container depends on,
     * here you can configure the image type to be depended on.
     */
    private String[] requirement;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageVersion() {
        return imageVersion;
    }

    public void setImageVersion(String imageVersion) {
        this.imageVersion = imageVersion;
    }

    public String[] getRequirement() {
        return requirement;
    }

    public void setRequirement(String[] requirement) {
        this.requirement = requirement;
    }

    public String getId() {
        if (null == id) {
            return type;
        } else {
            return id;
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {

        return String.format("%s, %s, %s, %s", id, type, imageVersion, null != requirement ? Arrays.asList(requirement) : "[]");
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (! (o instanceof Image)) {
            return false;
        }

        Image other = (Image) o;
        boolean valueEquals = (this.type == null && other.type == null)
                || (this.type != null && this.type.equals(other.type));

        return valueEquals;
    }

    @Override
    public int hashCode() {
        int result = 1;

        if (type != null) {
            result = 31 * result + type.hashCode();
        }

        return result;
    }
}
