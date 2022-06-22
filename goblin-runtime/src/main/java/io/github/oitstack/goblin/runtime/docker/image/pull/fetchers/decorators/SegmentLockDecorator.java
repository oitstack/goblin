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
package io.github.oitstack.goblin.runtime.docker.image.pull.fetchers.decorators;

import com.github.dockerjava.api.DockerClient;
import io.github.oitstack.goblin.runtime.docker.image.DockerImageName;
import io.github.oitstack.goblin.runtime.docker.image.pull.fetchers.FetchContext;
import io.github.oitstack.goblin.runtime.docker.image.pull.fetchers.ImageFetcher;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

/**
 * Segment lock decorator for image fetches
 * @Author CuttleFish
 * @Date 2022/3/4 下午5:27
 */
public class SegmentLockDecorator implements ImageFetcher {

    /**
     * The image fetcher that will be decorated.
     */
    ImageFetcher fetcher;
    private int length = 1024;
    private int mask = length - 1;

    /**
     * Locks for image fetcher.
     */
    private Lock[] locks = Stream.generate(ReentrantLock::new)
            .limit(length)
            .toArray(Lock[]::new);


    /**
     * Get a lock.
     * @param i
     * @return
     */
    private Lock getLock(int i) {
        return locks[i & mask];
    }

    public SegmentLockDecorator(ImageFetcher fetcher) {
        this.fetcher = fetcher;
    }

    /**
     * Lock when pulling images to ensure that the same image can only be pulled by
     * one thread at the same time.
     */
    @Override
    public FetchContext fetch(DockerImageName dockerImageName, DockerClient dockerClient) {
        int lockIndex = Math.abs(dockerImageName.hashCode());
        this.getLock(lockIndex).lock();
        try {
            return fetcher.fetch(dockerImageName,dockerClient);
        } finally {
            this.getLock(lockIndex).unlock();
        }

    }
}
