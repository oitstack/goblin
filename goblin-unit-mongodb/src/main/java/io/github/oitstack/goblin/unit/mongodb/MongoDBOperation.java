package io.github.oitstack.goblin.unit.mongodb;


import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.oitstack.goblin.unit.db.AbstractCustomizableDatabaseOperation;
import io.github.oitstack.goblin.unit.db.IOUtils;
import io.github.oitstack.goblin.unit.db.XUnitAssertionError;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;


public class MongoDBOperation extends AbstractCustomizableDatabaseOperation<MongoDBConnectionCallback, MongoClient> {

    public final static Logger logger = LoggerFactory.getLogger(MongoDBOperation.class);

    private MongoDBConfiguration mongoDbConfiguration;

    private MongoClient mongoClient;

    protected MongoDBOperation(MongoDBConfiguration mongoDbConfiguration) {
        this.mongoDbConfiguration = mongoDbConfiguration;
        this.setInsertStrategy(new DefaultInsertionStrategy());
        this.setCompareStrategy(new DefaultComparisonStrategy());
    }

    protected MongoDBOperation() {
        this.setInsertStrategy(new DefaultInsertionStrategy());
        this.setCompareStrategy(new DefaultComparisonStrategy());
    }

    @Override
    public void insert(InputStream contentStream) {
        try {
            final MongoDatabase mongoDb = getMongoDb();
            executeInsertion(new MongoDBConnectionCallback() {
                @Override
                public MongoDatabase db() {
                    return mongoDb;
                }

                @Override
                public MongoClient mongoClient() {
                    return mongoClient;
                }
            }, contentStream);

        } catch (Throwable e) {
            throw new IllegalArgumentException("Unexpected error reading data set file.", e);
        }

    }

    @Override
    public void deleteAll(InputStream contentStream) {
        MongoDatabase mongoDb = getMongoDb();

        String jsonData = null;
        try {
            jsonData = IOUtils.readFullStream(contentStream);
            Document parsedData = parseData(jsonData);
            deleteAllElements(parsedData, mongoDb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean databaseIs(InputStream expectedData) {
        try {
            final MongoDatabase mongoDb = getMongoDb();
            executeComparison(new MongoDBConnectionCallback() {
                @Override
                public MongoDatabase db() {
                    return mongoDb;
                }

                @Override
                public MongoClient mongoClient() {
                    return mongoClient;
                }
            }, expectedData);
            return true;
        } catch (XUnitAssertionError e1) {
            throw e1;
        } catch (Throwable e) {
            throw new IllegalArgumentException("Unexpected error verifing expected data set file.", e);
        }
    }

    private void deleteAllElements(Document parsedData, MongoDatabase mongoDb) {
//        final MongoIterable<String> listCollectionNames = mongoDb.listCollectionNames();
        Set<String> collectionaNames = parsedData.keySet();
        for (String collectionName : collectionaNames) {

            if (isNotASystemCollection(collectionName)) {
                logger.debug("Dropping Collection {}.", collectionName);
                MongoCollection dbCollection = mongoDb.getCollection(collectionName);
                dbCollection.deleteMany(new Document());
            }
        }
    }

    private Document parseData(String jsonData) throws IOException {
        Document parsedData = Document.parse(jsonData);
        return parsedData;
    }

    private boolean isNotASystemCollection(String collectionName) {
        return !collectionName.startsWith("system.");
    }

    @Override
    public MongoClient connectionManager() {
        if (null == mongoClient) {
            mongoClient = new MongoClient(this.mongoDbConfiguration.getHost(), this.mongoDbConfiguration.getPort());
        }

        return mongoClient;
    }

    private MongoDatabase getMongoDb() {
        connectionManager();
        MongoDatabase db = mongoClient.getDatabase(this.mongoDbConfiguration.getDatabaseName());
        return db;
    }

}
