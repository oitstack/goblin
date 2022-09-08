package io.github.oitstack.goblin.container.zookeeper;



import io.github.oitstack.goblin.core.Goblin;
import io.github.oitstack.goblin.core.GoblinContainer;
import io.github.oitstack.goblin.runtime.docker.container.DockerContainerAdapter;
import io.github.oitstack.goblin.spi.context.Image;

import java.util.HashMap;
import java.util.Map;

public class GoblinZooKeeperContainer <SELF extends GoblinZooKeeperContainer<SELF>> extends DockerContainerAdapter<SELF> implements GoblinContainer {

    private Map<String, String> placeholders = new HashMap<>();

    private final static String containerType = "ZOOKEEPER";


    @Override
    public void start(Goblin goblin, Object config, Image image) {
        this.image(image.getImageVersion());
        this.start();

        this.placeholders.put("URL", String.format("%s:%s", this.getHost(), this.getPortByInnerPort(2181)));
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
