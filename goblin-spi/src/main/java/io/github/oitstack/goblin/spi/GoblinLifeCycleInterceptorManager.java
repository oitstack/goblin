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
package io.github.oitstack.goblin.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * This class is used to manage extension points of the Goblin life cycle,
 * such as loading custom extension points and providing access methods.
 * @author yangguang
 * @date 2022/5/17
 */
public class GoblinLifeCycleInterceptorManager {
    public static List<GoblinLifeCycleInterceptor> exts = new ArrayList<>();


    /**
     * Load user-defined GoblinLifeCycleInterceptor via SPI.
     */
    public static synchronized void load() {
        if (!exts.isEmpty()) {
            return;
        }

        ServiceLoader<GoblinLifeCycleInterceptor> serviceLoader = ServiceLoader.load(GoblinLifeCycleInterceptor.class);

        for (GoblinLifeCycleInterceptor interceptor : serviceLoader) {
            exts.add(interceptor);
        }

        exts.sort((o1, o2) -> o2.getOrder() -o1.getOrder());
    }

    /**
     * Get user-defined GoblinLifeCycleInterceptor.
     */
    public static List<GoblinLifeCycleInterceptor> getExts() {
        if (!exts.isEmpty()) {
            return exts;
        }

        load();
        return exts;
    }
}
