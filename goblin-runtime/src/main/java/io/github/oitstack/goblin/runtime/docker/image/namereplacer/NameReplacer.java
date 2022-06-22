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


import io.github.oitstack.goblin.runtime.docker.image.DockerImageName;

/**
 * This interface defines how to replace the docker image name.
 * @Author CuttleFish
 * @Date 2022/3/3 下午9:07
 */
public interface NameReplacer {

    /**
     *
     * @param chain chain of responsibility
     * @param imageName docker image name
     * @param pos The location of the instance in the chain of responsibility
     * @return
     */
    default DockerImageName replace(NameReplacerChain chain, DockerImageName imageName, int pos) {
        return chain.next(this.doReplace(imageName), pos + 1);
    }

    /**
     * Defines a method to replace the docker image name.
     * @param imageName image name to replace.
     * @return
     */
    DockerImageName doReplace(DockerImageName imageName);
}
