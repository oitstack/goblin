package io.github.oitstack.goblin.unit.redis;

import io.github.oitstack.goblin.unit.db.XUnitAssertionError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class DefaultComparisonStrategy implements RedisComparisonStrategy {

    public final static Logger logger = LoggerFactory.getLogger(DefaultComparisonStrategy.class);

    private String[] ignoreProperties;


    @Override
    public boolean compare(RedisConnectionCallback connection, InputStream dataset) throws XUnitAssertionError, Throwable {
        RedisAssertion.strictAssertEquals(connection, dataset);

        return true;
    }

    @Override
    public void setIgnoreProperties(String[] ignoreProperties) {
        this.ignoreProperties = ignoreProperties;
    }

}
