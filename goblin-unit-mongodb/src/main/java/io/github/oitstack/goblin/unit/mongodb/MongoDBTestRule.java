package io.github.oitstack.goblin.unit.mongodb;

import io.github.oitstack.goblin.unit.db.AbstractTestRule;
import io.github.oitstack.goblin.unit.db.DatabaseOperation;

import java.util.Map;

public class MongoDBTestRule extends AbstractTestRule {
    private static final String NAME = "mongodb";
    private static final String EXTENSION = "json";

    public MongoDBTestRule(MongoDBConfiguration config) {
        databaseOperation = new MongoDBOperation(config);
    }

    public MongoDBTestRule() {

        if (System.getProperty("GOBLIN_MONGODB_HOST")!=null &&System.getProperty("GOBLIN_MONGODB_PORT")!=null) {
            MongoDBConfiguration config = new MongoDBConfiguration(
                    System.getProperty("GOBLIN_MONGODB_HOST"),
                    Integer.valueOf(System.getProperty("GOBLIN_MONGODB_PORT")),
                    System.getProperty("GOBLIN_MONGODB_DATABASENAME")
            );
            databaseOperation = new MongoDBOperation(config);
        } else {
            databaseOperation = new MongoDBOperation();
        }
    }

    public static class MongoDbTestRuleBuilder {

        private MongoDBConfiguration mongoDbConfiguration;

        private MongoDbTestRuleBuilder() {
        }

        public static MongoDbTestRuleBuilder newMongoDbTestRuleBuilder() {
            return new MongoDbTestRuleBuilder();
        }

        public MongoDbTestRuleBuilder configure(MongoDBConfiguration mongoDbConfiguration) {
            this.mongoDbConfiguration = mongoDbConfiguration;
            return this;
        }

        public MongoDBTestRule build() {
            if (null == mongoDbConfiguration) {
                throw new IllegalArgumentException("Configuration object should be provided.");
            }
            return new MongoDBTestRule(mongoDbConfiguration);
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
