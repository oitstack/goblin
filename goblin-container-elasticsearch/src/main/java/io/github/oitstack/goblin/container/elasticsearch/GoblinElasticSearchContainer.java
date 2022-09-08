package io.github.oitstack.goblin.container.elasticsearch;


import io.github.oitstack.goblin.core.Goblin;
import io.github.oitstack.goblin.core.GoblinContainer;
import io.github.oitstack.goblin.runtime.docker.container.DockerContainerAdapter;
import io.github.oitstack.goblin.runtime.wait.HttpWaitStrategy;
import io.github.oitstack.goblin.spi.context.Image;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class GoblinElasticSearchContainer <SELF extends GoblinElasticSearchContainer<SELF>> extends DockerContainerAdapter<SELF> implements GoblinContainer {

    private Map<String, String> placeholders = new HashMap<>();

    private final static String containerType = "ELASTICSEARCH";


    @Override
    public void start(Goblin goblin, Object config, Image image) {

        this.image(image.getImageVersion())
                .addEnv("discovery.type", "single-node")
                .exposedPorts(new Integer[]{9200, 9300})

                .start();

        this.placeholders.put("HTTPSRV", this.getHttpHostAddress());
        this.placeholders.put("HOST", this.getHost());
        this.placeholders.put("PORT", String.valueOf(this.getPortByInnerPort(9200)));
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

    public String getHttpHostAddress() {
        return this.getHost() + ":" + this.getPortByInnerPort(9200);
    }

    /** @deprecated */
    @Deprecated
    public InetSocketAddress getTcpHost() {
        return new InetSocketAddress(this.getHost(), this.getPortByInnerPort(9300));
    }

    @Override
    protected void blockUntilContainerStarted() {
        super.blockUntilContainerStarted();
        new HttpWaitStrategy().forPort(9200).forStatusCodeMatching((response) -> {
            return response == 200 || response == 401;
        }).withStartupTimeout(Duration.ofMinutes(2L));
    }
}
