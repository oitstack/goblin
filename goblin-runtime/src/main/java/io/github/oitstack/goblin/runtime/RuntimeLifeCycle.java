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

import java.util.concurrent.CompletableFuture;

/**
 * Container Lifecycle Interface.
 * @Author CuttleFish
 * @Date 2022/2/18 下午9:09
 */
public interface RuntimeLifeCycle<D extends RuntimeLifeCycle> {

     /**
      * Container initialization operation, valid only once in the whole life cycle.
      * @return
      */
     D init();

     /**
      * Container start.
      * @return
      */
     D start();

     /**
      * Start container asynchronously
      * @return
      */
     CompletableFuture<D> startAsync();

     /**
      * Close the container.
      * @return
      */
     boolean shutdown();



}
