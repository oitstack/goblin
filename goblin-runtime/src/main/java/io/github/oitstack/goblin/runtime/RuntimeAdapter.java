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

import io.github.oitstack.goblin.runtime.config.Configure;
import io.github.oitstack.goblin.runtime.utils.MixAll;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Container template class, which defines the basic process of container operation
 * and provides related hook methods for specific container implementation.
 * @Author CuttleFish
 * @Date 2022/3/1 下午8:30
 */
public abstract class RuntimeAdapter<T extends RuntimeAdapter<T>> implements Runtime<T> {
    protected Configure configure;

    /**
     * Flag indicating whether the container has been initialized.
     */
    private AtomicBoolean initialized = new AtomicBoolean(false);

    public T self() {
        return (T) this;
    }

    private Function<Supplier, Object> ensureInit = supplier -> {
        init();
        return supplier.get();
    };

    @Override
    public T config(Configure configure) {
        this.configure = configure;
        return self();
    }

    @Override
    public T init() {
        if (initialized.compareAndSet(false, true)) {
            if (null == configure) {
                this.configure = MixAll.getMixConfigureBuilder().build();
            }
            doInit();
        }
        return self();
    }

    /**
     * Container initialization operation, if the container you define needs to be initialized,
     * you can implement this hook method
     */
    protected abstract void doInit();

    /**
     * Start container operation
     * @return
     */
    protected abstract CompletableFuture<Runtime> doStart();

    @Override
    public T start() {

        int i = 0;
         T result=(T) ensureInit.apply(() -> {
            try {
                return startAsync().get(configure.getCreateRuntimeTimeout(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        onStarted();
        return result;

    }

    @Override
    public CompletableFuture<T> startAsync() {
        return (CompletableFuture<T>) ensureInit.apply(() -> {
            return doStart();
        });
    }
    protected void onStarted(){

    }
}
