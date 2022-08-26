package io.github.oitstack.goblin.container.rediscluster;

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

public class GoblinRedisClusterContainer <SELF extends GoblinRedisClusterContainer<SELF>> extends DockerContainerAdapter<SELF> implements GoblinContainer {

    private Map<String, String> placeholders = new HashMap<>();

    private final static String containerType = "REDISCLUSTER";
    private final static String redisProxyImage = "redisproxy:1.0.1-alpine";
    private final static String hackInfoFile = "/tmp/goblin_hack_info.txt";


    public void start(Goblin goblin, Object o, Image image) {
        Integer[] redisClusterPortsArr = new Integer[]{7000, 7001, 7002, 7003, 7004, 7005};
        this.exposedPorts(redisClusterPortsArr).start();
        Map<Integer, Integer> redisClusterPortsBinding = Arrays.asList(redisClusterPortsArr).stream()
                .collect(Collectors.toMap(k -> k, k -> this.getPortByInnerPort(k)));
        String bindPortsJson = null;
        try {
            bindPortsJson = new ObjectMapper()
                    .configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false)
                    .writeValueAsString(redisClusterPortsBinding);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String redisClusterNodes = String.format(
                "redis-cluster://@%s",
                redisClusterPortsBinding
                        .entrySet()
                        .stream()
                        .map(e -> String.format("%s:%s", this.getHost(), e.getValue()))
                        .collect(Collectors.joining(","))

        );

        DockerContainerAdapter redisProxy = new DockerContainerAdapter().image(this.redisProxyImage);


        Integer[] redisProxyPortsArr = new Integer[]{6379, 6380};

        redisProxy.exposedPorts(redisProxyPortsArr)
                .addEnv("GOBLIN_REDISCLUSTER_IP", this.getHost())
                .addEnv("GOBLIN_REDISCLUSTER_PORTS", bindPortsJson)
                .addEnv("GOBLIN_REDISCLUSTER_NODES", redisClusterNodes)
                .addEnv("GOBLIN_HACK_FILE", hackInfoFile)
                .addCommandWhenStartup("/bin/sh")
                .addCommandWhenStartup("startup.sh")
                .start();

        Map<Integer, Integer> redisProxyPortsBinding = Arrays.asList(redisProxyPortsArr).stream()
                .collect(Collectors.toMap(k -> k, k -> redisProxy.getPortByInnerPort(k)));

        GoblinHackInfo hackInfo = new GoblinHackInfo();
        hackInfo.hostIP = redisProxy.getHost();
        hackInfo.hostExportedPorts = redisProxyPortsBinding;
        String hackInfoJsonStr = null;
        try {
            hackInfoJsonStr = new ObjectMapper().writeValueAsString(hackInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            redisProxy.execInRuntime("sh", "-c", String.format("printf '%s' > %s", hackInfoJsonStr, hackInfoFile));

        } catch (Exception e) {
            e.printStackTrace();
        }

        this.placeholders.put("GOBLIN_REDISCLUSTER_URL", String.format("redis://%s:%s", redisProxy.getHost(), redisProxy.getPortByInnerPort(6379)));
        this.placeholders.put("GOBLIN_REDISCLUSTER_HOST", redisProxy.getHost());
        this.placeholders.put("GOBLIN_REDISCLUSTER_PORT", String.valueOf(redisProxy.getPortByInnerPort(6379)));
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
