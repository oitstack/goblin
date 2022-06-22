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

import org.springframework.test.context.TestContext;

/**
 * Test case SPI extension point, you can implement this interface to customize your own test rules,
 * and Goblin is responsible for loading your customized rules
 */
public interface TestRule {

    /**
     * This method is executed before all use cases are executed
     * @param context Context information on test cases encapsulated by spring-test.
     */
    void beforeAll(TestContext context);

    /**
     * This method is executed before current use cases are executed
     * @param context Context information on test cases encapsulated by spring-test.
     */
    void beforeEach(TestContext context);

    /**
     * This method is executed after current use cases are executed
     * @param context Context information on test cases encapsulated by spring-test.
     */
    void afterEach(TestContext context);

    /**
     * This method is executed after all use cases are executed
     * @param context Context information on test cases encapsulated by spring-test.
     */
    void afterAll(TestContext context);
}
