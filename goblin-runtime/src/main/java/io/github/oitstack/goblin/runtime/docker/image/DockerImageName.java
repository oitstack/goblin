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


import io.github.oitstack.goblin.runtime.docker.image.namereplacer.NameReplacerChain;
import lombok.Data;
import io.github.oitstack.goblin.runtime.utils.Preconditions;

/**
 * This class is used to manage related names of docker.
 * @Author CuttleFish
 * @Date 2022/2/25 下午3:18
 */
@Data
public class DockerImageName {

    /**
     * Docker image tag
     */
    private final String rawName;
    private final String registry;
    private final String repository;
    private final DockerImageVersion version;

    private final static String SLASH = "/";
    private final static String DOT = ".";
    private final static String COLON = ":";
    private final static String SHA_TAG = "@sha256:";
    private final static String LOCAL_HOST = "localhost";

    /**
     * Parse docker raw tag name and generate object.
     * @param originImageName original image name
     * @return
     */
    public static DockerImageName parseFrom(String originImageName) {
        return NameReplacerChain.apply(new DockerImageName(originImageName));
    }

    private DockerImageName(String rawName, String registry, String repository, DockerImageVersion version) {
        this.rawName = rawName;
        this.registry = registry;
        this.repository = repository;
        this.version = version;
    }


    /**
     * Metadata for docker hub.
     */
    class ImageHubMetaData {
        private String registry;
        private String remoteName;

        public ImageHubMetaData(String registry, String remoteName) {
            this.registry = registry;
            this.remoteName = remoteName;
        }

        public String getRegistry() {
            return registry;
        }

        public String getRemoteName() {
            return remoteName;
        }
    }

    /**
     * Metadata for docker images.
     */
    class ImageMetaData {
        private String repository;
        private DockerImageVersion version;

        public ImageMetaData(String repository, DockerImageVersion version) {
            this.repository = repository;
            this.version = version;
        }

        public String getRepository() {
            return repository;
        }

        public DockerImageVersion getVersion() {
            return version;
        }
    }

    /**
     * Generate metadata for docker hub from image tag.
     * @param fullImageName docker image tag
     * @return
     */
    private ImageHubMetaData calcImageHubMetaData(String fullImageName) {
        ImageHubMetaData imageHubMetaData;
        final int index = fullImageName.indexOf(SLASH);
        //------------

        if (index == -1) {
            imageHubMetaData = new ImageHubMetaData("", fullImageName);
        } else {
            String beforeSlashStr = fullImageName.substring(0, index);
            if (!beforeSlashStr.contains(DOT) &&
                    !beforeSlashStr.contains(COLON) &&
                    !beforeSlashStr.equals(LOCAL_HOST)) {

                imageHubMetaData = new ImageHubMetaData("", fullImageName);
            } else {

                imageHubMetaData = new ImageHubMetaData(fullImageName.substring(0, index), fullImageName.substring(index + 1));
            }
        }
        return imageHubMetaData;
    }

    /**
     * Generate metadata for docker hub from remote name.
     * @param remoteName
     * @return
     */
    private ImageMetaData calcImageMetaData(String remoteName) {
        ImageMetaData imageMetaData;
        final String repository;
        if (remoteName.contains(SHA_TAG)) {
            //------------
            imageMetaData = new ImageMetaData(remoteName.split(SHA_TAG)[0], new DockerImageVersion.Sha256Version(remoteName.split(SHA_TAG)[1]));
        } else if (remoteName.contains(COLON)) {

            imageMetaData = new ImageMetaData(remoteName.split(COLON)[0], new DockerImageVersion.TagVersion(remoteName.split(COLON)[1]));

        } else {

            imageMetaData = new ImageMetaData(remoteName, new DockerImageVersion.AnyVersion());

        }
        return imageMetaData;
    }

    private DockerImageName(String fullImageName) {
        Preconditions.checkNotBlank(fullImageName, "fullImageName cannot be blank.");
        this.rawName = fullImageName;
        ImageHubMetaData imageHubMetaData = calcImageHubMetaData(fullImageName);

        this.registry = imageHubMetaData.getRegistry();

        String remoteName = imageHubMetaData.getRemoteName();

        ImageMetaData imageMetaData = calcImageMetaData(remoteName);
        this.version = imageMetaData.getVersion();
        this.repository = imageMetaData.getRepository();
    }


    public String getUnversionedPart() {
        return (this.registry == null ? "" : this.registry + SLASH) + this.repository;
    }

    public DockerImageName cloneWithRegistry(String registry) {
        return new DockerImageName(this.rawName, registry, this.repository, this.version);
    }

    public DockerImageName cloneWithRepository(String repository) {
        return new DockerImageName(this.rawName, this.registry, repository, this.version);
    }

    public String toIdentifyName() {
        return getUnversionedPart()
                + version.getSeparator()
                + this.version.getVersionDesc();
    }
}
