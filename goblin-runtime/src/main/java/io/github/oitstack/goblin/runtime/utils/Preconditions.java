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
package io.github.oitstack.goblin.runtime.utils;

import java.util.function.Predicate;

/**
 * Tool class for validating parameters.
 * @Author CuttleFish
 * @Date 2022/2/18 下午9:48
 */
public interface Preconditions {
    Predicate NOT_NULL = o -> null != o;

    Predicate NOT_EMPTY = o -> !"".equals(o);

    Predicate TRUE = (Predicate<Boolean>) o -> o;

    static void checkNotBlank(String s, String msg) {
        if (!NOT_NULL.and(NOT_EMPTY).test(s)) {
            throw new IllegalArgumentException(msg);
        }
    }

    static void checkNotNull(Object s, String msg) {
        if (!NOT_NULL.test(s)) {
            throw new IllegalArgumentException(msg);
        }
    }

    static void checkTrue(Object s, String msg) {
        if (!TRUE.test(s)) {
            throw new IllegalArgumentException(msg);
        }
    }
}
