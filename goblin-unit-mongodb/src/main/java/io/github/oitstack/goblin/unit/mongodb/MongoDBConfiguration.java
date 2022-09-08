package io.github.oitstack.goblin.unit.mongodb;


import io.github.oitstack.goblin.unit.db.AbstractJsr330Configuration;

public class MongoDBConfiguration extends AbstractJsr330Configuration {
    private String databaseName;
    private String host;
    private Integer port;

    public MongoDBConfiguration(String host, Integer port, String databaseName) {
        this.databaseName = databaseName;
        this.host = host;
        this.port = port;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
