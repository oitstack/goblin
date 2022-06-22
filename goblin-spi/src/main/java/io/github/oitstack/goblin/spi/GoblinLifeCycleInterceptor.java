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

import io.github.oitstack.goblin.spi.context.GoblinContext;

/**
 * Goblin life cycle interceptor interface. By implementing this interface,
 * you can perform related business before or after Goblin starts,
 * and specify the order of multiple interceptors.
 * Goblin automatically loads the implementation of this interface through SPI.
 * @author yangguang
 * @date 2022/5/16
 */
public interface GoblinLifeCycleInterceptor {

    /**
     * This method is executed before Goblin starts.
     * @param context Goblin running context.
     */
    void preProcess(GoblinContext context);

    /**
     * This method is executed after Goblin starts.
     * @param context
     */
    void postProcess(GoblinContext context);

    /**
     * Specify the execution order of multiple interceptors by returning a value.
     * The larger the return value, the earlier the execution.
     * @return
     */
    int getOrder();
}
