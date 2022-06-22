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
package io.github.oitstack.goblin.runtime.docker.image.namereplacer;


import io.github.oitstack.goblin.runtime.config.RunTimeConfig;
import io.github.oitstack.goblin.runtime.docker.image.DockerImageName;

/**
 * Default replacer for the registry.
 * @Author CuttleFish
 * @Date 2022/3/3 下午9:19
 */
public class FillDefaultRegistryReplacer implements NameReplacer {

    /**
     * Replace registry
     * @param imageName image name to replace.
     * @return
     */
    @Override
    public DockerImageName doReplace(DockerImageName imageName) {
        final String configuredPrefix = RunTimeConfig.getInstance().get(RunTimeConfig.PREFIX_PROPERTY_KEY);
        if (null == configuredPrefix || "".equals(configuredPrefix)) {
            return imageName;
        }
        String oldRegistry = imageName.getRegistry();
        if (null != oldRegistry && !"".equals(oldRegistry)) {
            return imageName;
        }
        DockerImageName prefixAsImage = DockerImageName.parseFrom(configuredPrefix);
        return imageName.cloneWithRegistry(prefixAsImage.getRegistry()).cloneWithRepository(prefixAsImage.getRepository() + imageName.getRepository());
    }

}
