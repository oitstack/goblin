package io.github.oitstack.goblin.unit.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public interface MongoDBConnectionCallback {

    MongoDatabase db();
    MongoClient mongoClient();
}
