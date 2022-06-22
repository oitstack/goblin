package io.github.oitstack.goblin.core.container;

public class Config {

    private Mysql mysql;

    public Mysql getMysql() {
        return mysql;
    }

    public void setMysql(Mysql mysql) {
        this.mysql = mysql;
    }

    public static class Mysql {
        private String database;
        private String initScript;

        public Mysql() {
        }

        public Mysql(String database, String initScript) {
            this.database = database;
            this.initScript = initScript;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getInitScript() {
            return initScript;
        }

        public void setInitScript(String initScript) {
            this.initScript = initScript;
        }
    }
}
