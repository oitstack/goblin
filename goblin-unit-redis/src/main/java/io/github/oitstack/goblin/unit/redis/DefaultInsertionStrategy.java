package io.github.oitstack.goblin.unit.redis;


import io.github.oitstack.goblin.unit.redis.parser.JsonParser;

import java.io.InputStream;

public class DefaultInsertionStrategy implements RedisInsertionStrategy {


    @Override
    public void insert(RedisConnectionCallback connection, InputStream dataset) throws Throwable {
        JsonParser jsonParser = new JsonParser(connection.redisClient());

        jsonParser.insert(dataset);
    }

}
