package io.github.oitstack.goblin.container.rocketmq;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oitstack.goblin.core.Goblin;
import io.github.oitstack.goblin.core.GoblinContainer;
import io.github.oitstack.goblin.runtime.docker.container.DockerContainerAdapter;
import io.github.oitstack.goblin.spi.context.Image;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class GoblinRocketMQContainer <SELF extends GoblinRocketMQContainer<SELF>> extends DockerContainerAdapter<SELF> implements GoblinContainer {

        private Map<String, String> placeholders = new HashMap<>();

    private final static String containerType = "ROCKETMQ";


    @Override
    public void start(Goblin goblin, Object config, Image image) {
        this.image(image.getImageVersion());
        Integer[] ports = new Integer[]{10909, 10911, 10912, 9876};
        String rocketMqHackFileName = "/home/rocketmq/GBL_BINDING_PORTS";
        this.exposedPorts(ports)
                .addEnv("NAMESRV_ADDR", "localhost:9876")
                .addEnv("HOST_IP", this.getHost())
                .addEnv("GBL_FILE_BINDING_PORTS", rocketMqHackFileName)
                .addCommandWhenStartup("/bin/sh")
                .addCommandWhenStartup("all.sh");

        this.start();

        Map<Integer, Integer> bindingPorts = Arrays.asList(ports).stream()
                .collect(Collectors.toMap(k -> k, k -> this.getPortByInnerPort(k)));

        String bindingPortsJsonStr = "";
        try {
            bindingPortsJsonStr = new ObjectMapper()
                    .configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false)
                    .writeValueAsString(bindingPorts);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.execInRuntime("sh", "-c", String.format("printf \"%s\" > %s", bindingPortsJsonStr, rocketMqHackFileName));


            if (null != config) {
                Config conf = (Config) config;
                if (null != conf.getRocketmq() && null != conf.getRocketmq().getAutoCreateTopics()) {
                    for (String topicName : conf.getRocketmq().getAutoCreateTopics()) {
                        this.execInRuntime("sh", "-c", String.format("./mqadmin updateTopic -c DefaultCluster -p 6 -t %s", topicName));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        this.placeholders.put("NAMESRVADDR", this.getHost() + ":" + this.getPortByInnerPort(9876));
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
        return Config.class;
    }
}
