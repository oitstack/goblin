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
package io.github.oitstack.goblin.spring.starter;


import io.github.oitstack.goblin.core.GoblinBootstrap;
import io.github.oitstack.goblin.spi.testrule.TestRuleManager;
import io.github.oitstack.goblin.spi.testrule.TestRule;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * The concrete implementation of TestExecutionListener in spring-test, used to start Goblin.
 */
public class GoblinTestExecutionListener extends AbstractTestExecutionListener {

    /**
     * Goblin is initialized before all test methods of the current test class are executed.
     * @param testContext context of test case
     */
    @Override
    public void beforeTestClass(TestContext testContext) {
        if (isGoblinTest(testContext)) {
            initGoblin(testContext);
        }
    }

    /**
     * Execute the test rules after all test methods of the current test class are executed.
     *
     * @param testContext context of test case
     */
    @Override
    public void afterTestClass(TestContext testContext) {
        for (TestRule testRule : TestRuleManager.getInstance().getTestRules()) {
            testRule.afterAll(testContext);
        }
    }

    /**
     * Execute the test rules before current test case is executed.
     *
     * @param testContext context of test case
     */
    @Override
    public void beforeTestMethod(TestContext testContext) {

        for (TestRule testRule : TestRuleManager.getInstance().getTestRules()) {
            testRule.beforeEach(testContext);
        }
    }

    /**
     * Execute the test rules after current test case is executed.
     *
     * @param testContext context of test case
     */
    @Override
    public void afterTestMethod(TestContext testContext) {

        for (TestRule testRule : TestRuleManager.getInstance().getTestRules()) {
            testRule.afterEach(testContext);
        }
    }

    /**
     * Determine whether the class under test is marked with GoblinTest annotation.
     *
     * @param testContext context of test case
     * @return
     */
    private static boolean isGoblinTest(TestContext testContext) {
        return AnnotatedElementUtils.findMergedAnnotation(testContext.getTestClass(), GoblinTest.class) != null;
    }

    /**
     * Initialize the Goblin runtime environment.
     *
     * @param testContext context of test case
     */
    private static void initGoblin(TestContext testContext) {
        GoblinBootstrap.getInstance();

        for (TestRule testRule : TestRuleManager.getInstance().getTestRules()) {
            testRule.beforeAll(testContext);
        }
    }
}
