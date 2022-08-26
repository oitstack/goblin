package io.github.oitstack.goblin.container.redis;


import io.github.oitstack.goblin.core.Goblin;
import io.github.oitstack.goblin.core.GoblinContainer;
import io.github.oitstack.goblin.runtime.docker.container.DockerContainerAdapter;
import io.github.oitstack.goblin.spi.context.Image;

import java.util.HashMap;
import java.util.Map;

public class GoblinRedisContainer  <SELF extends GoblinRedisContainer<SELF>> extends DockerContainerAdapter<SELF> implements GoblinContainer {

    private Map<String, String> placeholders = new HashMap<>();

    private final static String containerType = "REDIS";

    public void start(Goblin context, Object confObj, Image image) {
        this.image(image.getImageVersion());
        this.start();

        this.placeholders.put("HOST", this.getHost());
        this.placeholders.put("PORT", String.valueOf(this.getPortByInnerPort(6379)));
        this.placeholders.put("URL", String.format("redis://%s:%s", this.getHost(), this.getPortByInnerPort(6379)));
    }

    @Override
    public Map<String, String> getPlaceHolders() {
        return this.placeholders;
    }

    @Override
    public String getContainerType() {
        return containerType;
    }

    @Override
    public Class configClass() {
        return null;
    }
}
