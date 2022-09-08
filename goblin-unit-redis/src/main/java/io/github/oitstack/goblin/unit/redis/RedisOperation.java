package io.github.oitstack.goblin.unit.redis;


import io.github.oitstack.goblin.unit.db.AbstractCustomizableDatabaseOperation;
import io.github.oitstack.goblin.unit.db.XUnitAssertionError;
import io.github.oitstack.goblin.unit.redis.parser.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;


public class RedisOperation extends AbstractCustomizableDatabaseOperation<RedisConnectionCallback, RedisClient> {

    public final static Logger logger = LoggerFactory.getLogger(RedisOperation.class);

    private RedisConfiguration redisConfiguration;

    private RedisClient redisClient;

    protected RedisOperation(RedisConfiguration redisConfiguration) {
        this.redisConfiguration = redisConfiguration;
        this.setInsertStrategy(new DefaultInsertionStrategy());
        this.setCompareStrategy(new DefaultComparisonStrategy());
    }

    protected RedisOperation() {
        this.setInsertStrategy(new DefaultInsertionStrategy());
        this.setCompareStrategy(new DefaultComparisonStrategy());
    }

    @Override
    public void insert(InputStream contentStream) {

        try {
            final RedisClient redisClient = connectionManager();
            executeInsertion(new RedisConnectionCallback() {
                @Override
                public RedisClient redisClient() {
                    return redisClient;
                }
            }, contentStream);

        } catch (Throwable e) {
            throw new IllegalArgumentException("Unexpected error reading data set file.", e);
        }
    }

    @Override
    public void deleteAll(InputStream contentStream) {
        JsonParser jsonParser = getJsonParser();

        jsonParser.delete(contentStream);
    }

    @Override
    public boolean databaseIs(InputStream expectedData) {
        try {
            final RedisClient redisClient = connectionManager();
            executeComparison(new RedisConnectionCallback() {
                @Override
                public RedisClient redisClient() {
                    return redisClient;
                }
            }, expectedData);

            return true;
        } catch (XUnitAssertionError e1) {
            throw e1;
        } catch (Throwable e2) {
            throw new IllegalArgumentException("Unexpected error verifing expected data set file.", e2);
        }
    }

    private JsonParser getJsonParser() {
        RedisClient redisClient = connectionManager();
        JsonParser jsonParser = new JsonParser(redisClient);

        return jsonParser;
    }

    @Override
    public RedisClient connectionManager() {
        if (null == redisClient) {
            redisClient = new RedisClient(this.redisConfiguration.getHost(), this.redisConfiguration.getPort());
        }

        return redisClient;
    }

}
