package io.github.oitstack.goblin.unit.redis;

import io.github.oitstack.goblin.unit.db.AbstractTestRule;
import io.github.oitstack.goblin.unit.db.DatabaseOperation;


public class RedisTestRule extends AbstractTestRule {
    private static final String NAME = "redis";
    private static final String EXTENSION = "json";

    public RedisTestRule(RedisConfiguration redisConfiguration) {
        databaseOperation = new RedisOperation(redisConfiguration);
    }

    public RedisTestRule() {

        String host = System.getProperty("GOBLIN_REDIS_HOST")==null ?
             System.getProperty("GOBLIN_REDIS_HOST"):System.getProperty("GOBLIN_REDIS_HOST");
        String port =
                System.getProperty("GOBLIN_REDIS_PORT")==null ?
                        System.getProperty("GOBLIN_REDISCLUSTER_PORT"):System.getProperty("GOBLIN_REDIS_PORT");

        if (null != host && null != port) {
            RedisConfiguration redisConfiguration = new RedisConfiguration(host, Integer.valueOf(port));
            databaseOperation = new RedisOperation(redisConfiguration);
        } else {
            databaseOperation = new RedisOperation();
        }
    }

    public static class RedisTestRuleBuilder {

        private RedisConfiguration redisConfiguration;

        private RedisTestRuleBuilder() {
        }

        public static RedisTestRuleBuilder newRedisTestRuleBuilder() {
            return new RedisTestRuleBuilder();
        }

        public RedisTestRuleBuilder configure(RedisConfiguration redisConfiguration) {
            this.redisConfiguration = redisConfiguration;
            return this;
        }

        public RedisTestRule build() {
            if (null == redisConfiguration) {
                throw new IllegalArgumentException("Configuration object should be provided.");
            }
            return new RedisTestRule(redisConfiguration);
        }
    }

    public String getWorkingExtension() {
        return EXTENSION;
    }

    public DatabaseOperation getDatabaseOperation() {
        return this.databaseOperation;
    }

    public String getName() {
        return NAME;
    }

}
