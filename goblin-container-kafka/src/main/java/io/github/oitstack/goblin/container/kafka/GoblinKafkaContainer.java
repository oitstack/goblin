package io.github.oitstack.goblin.container.kafka;


import io.github.oitstack.goblin.core.Goblin;
import io.github.oitstack.goblin.core.GoblinContainer;
import io.github.oitstack.goblin.runtime.docker.container.DockerContainerAdapter;
import io.github.oitstack.goblin.spi.context.Image;

import java.util.HashMap;
import java.util.Map;

public class GoblinKafkaContainer <SELF extends GoblinKafkaContainer<SELF>> extends DockerContainerAdapter<SELF> implements GoblinContainer {

    private Map<String, String> placeholders = new HashMap<>();

    private final static String containerType = "KAFKA";

    @Override
    public void start(Goblin goblin, Object config, Image image) {
        this.image(image.getImageVersion());
        this.start();
        this.placeholders.put("BOOTSTRAPSERVERS", String.format("PLAINTEXT://%s:%s", this.getHost(), this.getPortByInnerPort(9093)));
        this.placeholders.put("HOST", this.getHost());
        this.placeholders.put("PORT", String.valueOf(this.getPortByInnerPort(9093)));
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
