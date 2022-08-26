package io.github.oitstack.goblin.container.mongodb;

public class Config {

    private Mongodb mongodb;

    public Mongodb getMongodb() {
        return mongodb;
    }

    public void setMongodb(Mongodb mongodb) {
        this.mongodb = mongodb;
    }

    public static class Mongodb {
        private String database;

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }
    }
}
