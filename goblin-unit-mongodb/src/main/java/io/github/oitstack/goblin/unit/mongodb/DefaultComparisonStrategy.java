package io.github.oitstack.goblin.unit.mongodb;

import io.github.oitstack.goblin.unit.db.IOUtils;
import io.github.oitstack.goblin.unit.db.XUnitAssertionError;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class DefaultComparisonStrategy implements MongoDBComparisonStrategy {

    public final static Logger logger = LoggerFactory.getLogger(DefaultComparisonStrategy.class);

    private String[] ignoreProperties;


    @Override
    public boolean compare(MongoDBConnectionCallback connection, InputStream dataset) throws XUnitAssertionError, Throwable {
        Document parsedData = parseData(IOUtils.readFullStream(dataset));
        if (null == ignoreProperties) {
            MongoDbAssertion.strictAssertEquals(parsedData, connection.db());
        } else {
            MongoDbAssertion.flexibleAssertEquals(parsedData, ignoreProperties, connection.db());
        }


        return true;
    }

    @Override
    public void setIgnoreProperties(String[] ignoreProperties) {
        this.ignoreProperties = ignoreProperties;
    }


    private Document parseData(String jsonData) throws IOException {
        Document parsedData = Document.parse(jsonData);
        return parsedData;
    }
}
