package io.github.oitstack.goblin.unit.redis;


import io.github.oitstack.goblin.unit.db.AbstractJsr330Configuration;

public class RedisConfiguration extends AbstractJsr330Configuration {
    private String host;
    private Integer port;

    public RedisConfiguration(String host, Integer port) {
        this.host = host;
        this.port = port;
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
