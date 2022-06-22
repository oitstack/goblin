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

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * This class is used to manage testRule extension points,
 * such as loading user-defined extension points and providing access methods.
 * @author yangguang
 * @date 2022/5/19
 */
public class TestRuleManager {

    /**
     * Store user-defined TestRule.
     */
    private List<TestRule> testRules = new ArrayList<>();

    /**
     * Get user-defined TestRule.
     * @return
     */
    public List<TestRule> getTestRules() {
        return testRules;
    }


    private TestRuleManager() {
        load();
    }

    /**
     * Get a singleton TestRuleManager instance.
     * @return
     */
    public static TestRuleManager getInstance() {
        return PluginsManagerHolder.INSTANCE;

    }

    static class PluginsManagerHolder {
        public static TestRuleManager INSTANCE = new TestRuleManager();
    }

    private void load() {
        loadTestRules();
    }

    /**
     * Load user-defined testRule via SPI.
     */
    private void loadTestRules() {
        if (!testRules.isEmpty()) {
            return;
        }

        ServiceLoader<TestRule> serviceLoader = ServiceLoader.load(TestRule.class);
        for (TestRule testRule : serviceLoader) {
            testRules.add(testRule);
        }
    }


}
