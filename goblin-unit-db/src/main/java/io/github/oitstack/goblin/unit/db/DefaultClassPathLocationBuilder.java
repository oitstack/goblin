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

import java.lang.reflect.Method;

public class DefaultClassPathLocationBuilder {
    private static final String METHOD_SEPARATOR = "#";

    public static final String defaultClassAnnotatedClassPathLocation(Method testMethod) {
        String testClassName = testMethod.getDeclaringClass().getName();
        return "/" + testClassName.replace('.', '/');

    }

    public static final String defaultClassAnnotatedClassPathLocation(Class testClass) {
        String className = testClass.getName();
        return  "/" + className.replace('.', '/');
    }

    public static String defaultMethodAnnotatedClassPathLocation(
            Method testMethod,
            String defaultClassAnnotatedClasspath, String suffix) {
        String methodName = testMethod.getName();

        return defaultClassAnnotatedClasspath
                + METHOD_SEPARATOR
                + methodName
                + suffix;
    }
}
