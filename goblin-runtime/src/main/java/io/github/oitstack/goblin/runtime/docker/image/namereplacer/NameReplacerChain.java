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

import java.util.ArrayList;
import java.util.List;

/**
 * Docker image name replaces chain of responsibility.
 * @Author CuttleFish
 * @Date 2022/3/3 下午9:08
 */
public class NameReplacerChain {

    /**
     * List of docker image name replacers.
     */
    List<NameReplacer> replacers = new ArrayList<>();

    public static DockerImageName apply(DockerImageName imageName) {
        return new NameReplacerChain()
                .addReplacer(new FillDefaultRegistryReplacer())
//                .addReplacer(new ChangeDefaultReposityReplacer())
                .start(imageName);
    }

    private NameReplacerChain addReplacer(NameReplacer replacer) {
        this.replacers.add(replacer);
        return this;
    }

    public DockerImageName start(DockerImageName imageName) {
        return this.next(imageName, 0);
    }

    public DockerImageName next(DockerImageName imageName, int pos) {
        if (pos >= replacers.size()) {
            return imageName;
        } else {
            return replacers.get(pos).replace(this, imageName, pos);
        }
    }
}
