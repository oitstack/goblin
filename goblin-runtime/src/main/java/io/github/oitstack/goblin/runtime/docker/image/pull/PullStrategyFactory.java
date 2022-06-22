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
package io.github.oitstack.goblin.runtime.docker.image.pull;

import io.github.oitstack.goblin.runtime.docker.image.pull.strategies.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Image Pull Policy Factory.
 * @Author CuttleFish
 * @Date 2022/2/28 下午4:17
 */
public class PullStrategyFactory {

    private PullStrategyFactory() {
    }

    /**
     * Get a PullStrategyFactory instance.
     * @return
     */
    public static PullStrategyFactory getInstance() {
        return PullStrategyFactoryHolder.INSTANCE;
    }

    static class PullStrategyFactoryHolder {
        public static PullStrategyFactory INSTANCE = new PullStrategyFactory();
    }

    public final static String ALWAYS = "ALWAYS";
    public final static String CACHE = "CACHE";
    public final static String AGE = "AGE";

    public final static String CACHE_WITHOUT_LOCK = "CACHE_NO_LOCK";
    public final static String AGE_WITHOUT_LOCK = "AGE_NO_LOCK";

    /**
     * A map of container acquisition strategies.
     */
    private static Map<String, PullStrategy> STRATEGY_MAP = new ConcurrentHashMap<>();

    /**
     * Default container pull policy.
     */
    private static PullStrategy DEFAULT_PULL_STRATEGY = new AlwaysPullStrategy();

    static {
        STRATEGY_MAP.put(ALWAYS, new AlwaysPullStrategy());
        STRATEGY_MAP.put(CACHE, new CachePullLockStrategy());
        STRATEGY_MAP.put(AGE, new TimePullLockStrategy());
        STRATEGY_MAP.put(CACHE_WITHOUT_LOCK, new CachePullStrategy());
        STRATEGY_MAP.put(AGE_WITHOUT_LOCK, new TimePullStrategy());
    }

    /**
     * Get the specified image pull policy.
     * @param strategyName
     * @return
     */
    public PullStrategy getPullStrategy(String strategyName) {
        return STRATEGY_MAP.getOrDefault(strategyName, DEFAULT_PULL_STRATEGY);
    }


}
