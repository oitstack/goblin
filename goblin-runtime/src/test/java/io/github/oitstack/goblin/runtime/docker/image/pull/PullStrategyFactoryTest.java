package io.github.oitstack.goblin.runtime.docker.image.pull;

import io.github.oitstack.goblin.runtime.docker.image.pull.strategies.*;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author yangguang
 * @date 2022/5/30
 */
public class PullStrategyFactoryTest {

    @Test
    public void pullStrategyTest() {

        PullStrategy pullStrategy = PullStrategyFactory.getInstance().getPullStrategy(PullStrategyFactory.AGE);
        Assert.assertTrue(pullStrategy instanceof TimePullLockStrategy);

        PullStrategy cachePullStrategy = PullStrategyFactory.getInstance().getPullStrategy(PullStrategyFactory.CACHE);
        Assert.assertTrue(cachePullStrategy instanceof CachePullLockStrategy);

        PullStrategy alwaysPullStrategy = PullStrategyFactory.getInstance().getPullStrategy(PullStrategyFactory.ALWAYS);
        Assert.assertTrue(alwaysPullStrategy instanceof AlwaysPullStrategy);

        PullStrategy cachePullWithoutLockStrategy = PullStrategyFactory.getInstance().getPullStrategy(PullStrategyFactory.CACHE_WITHOUT_LOCK);
        Assert.assertTrue(cachePullWithoutLockStrategy instanceof CachePullStrategy);

        PullStrategy timePullStrategy = PullStrategyFactory.getInstance().getPullStrategy(PullStrategyFactory.AGE_WITHOUT_LOCK);
        Assert.assertTrue(timePullStrategy instanceof TimePullStrategy);
    }

}
