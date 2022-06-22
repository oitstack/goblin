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
package io.github.oitstack.goblin.spi.testrule;

import io.github.oitstack.goblin.spi.GoblinLifeCycleInterceptor;
import io.github.oitstack.goblin.spi.context.GoblinContext;

/**
 * TestRule interceptor, responsible for the initialization of TestRule extension points.
 * @author yangguang
 * @date 2022/5/7
 */
public class TestRulePluginInterceptor implements GoblinLifeCycleInterceptor {

    /**
     * This method is executed before Goblin starts.
     * @param context Goblin running context.
     */
    @Override
    public void preProcess(GoblinContext context) {

    }

    /**
     * This method is executed after Goblin starts.
     * @param context Goblin running context.
     */
    @Override
    public void postProcess(GoblinContext context) {
        TestRuleManager.getInstance();
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
