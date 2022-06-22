package io.github.oitstack.goblin.core.spi;

import io.github.oitstack.goblin.spi.GoblinLifeCycleInterceptor;
import io.github.oitstack.goblin.spi.context.GoblinContext;

/**
 * @author yangguang
 * @date 2022/5/27
 */
public class OblinLifeCycleInterceptorOrder100 implements GoblinLifeCycleInterceptor {

    public static final String GOBLIN_LIFE_CYCLE_ORDER_100_PRE_PROCESSED_KEY = "GOBLIN_LIFE_CYCLE_ORDER_100_PRE_PROCESSED";
    public static final String GOBLIN_LIFE_CYCLE_ORDER_100_POST_PROCESSED_KEY = "GOBLIN_LIFE_CYCLE_ORDER_100_POST_PROCESSED";

    @Override
    public void preProcess(GoblinContext context) {
        System.getProperties().setProperty(GOBLIN_LIFE_CYCLE_ORDER_100_PRE_PROCESSED_KEY, "true");
    }

    @Override
    public void postProcess(GoblinContext context) {
        System.getProperties().setProperty(GOBLIN_LIFE_CYCLE_ORDER_100_POST_PROCESSED_KEY, "true");
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
