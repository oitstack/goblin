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
package io.github.oitstack.goblin.runtime;

/**
 * Container interfaces.
 * @Author CuttleFish
 * @Date 2022/2/18 下午5:39
 */
public interface Runtime<T extends Runtime<T>> extends RuntimeLifeCycle<T>, RuntimeState<T>, RuntimeSetter<T>, RuntimeOperation<T> {


}
