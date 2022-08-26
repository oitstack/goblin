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
package io.github.oitstack.goblin.unit.db;

import org.springframework.test.context.TestContext;

import java.lang.reflect.Method;

/**
 * This class is used to get the default dataset configuration file path.
 */
public class DefaultDataSetLocationResolver {

    /**
     * Context information on test cases encapsulated by spring-test.
     */
    private TestContext testContext;

    public DefaultDataSetLocationResolver(TestContext testContext) {
        this.testContext = testContext;
    }

    /**
     * Get the test class for this test context.
     */
    public Class<?> getResourceBase() {
        return testContext.getTestClass();
    }

    /**
     * Get dataset configuration file path.
     * @param suffix suffix of Dataset configuration file
     * @return
     */
    public String resolveDefaultDataSetLocation(String suffix) {
        String defaultClassAnnotatedClasspath;

        String location;
        Method testMethod = null;

        try {
            testMethod = testContext.getTestMethod();
        } catch (Exception e) {

        }
        if (null == testMethod) {
            defaultClassAnnotatedClasspath = DefaultClassPathLocationBuilder.defaultClassAnnotatedClassPathLocation(getResourceBase());

            location = defaultClassAnnotatedClasspath + suffix;
        } else {
            defaultClassAnnotatedClasspath = DefaultClassPathLocationBuilder.defaultClassAnnotatedClassPathLocation(testMethod);
            location = DefaultClassPathLocationBuilder.defaultMethodAnnotatedClassPathLocation(testMethod, defaultClassAnnotatedClasspath, suffix);
        }

        return location;
    }
}
