package io.github.oitstack.goblin.core.utils;

import io.github.oitstack.goblin.spi.context.Configuration;
import io.github.oitstack.goblin.spi.context.Image;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ConfigParseUtilsTest {

    @Test
    public void testMap2Pojo() {
        Map<String, Object> properties = new HashMap<>();

        Image imageA = new Image();
        imageA.setId("mysql");
        imageA.setImageVersion("mysql:5.7");
        imageA.setType("MYSQL");
        Image[] images = new Image[] {imageA};
        properties.put("dockerImages", images);
        Map<String, String> envMap = new HashMap<>();
        envMap.put("testKey", "testValue");
        properties.put("env", envMap);

        Configuration config = ConfigParseUtils.map2Pojo(properties, Configuration.class);
        Assert.assertNotNull(config);
        Assert.assertEquals("testValue", config.getEnv().get("testKey"));
        Assert.assertEquals(1, config.getDockerImages().length);
        Assert.assertEquals(imageA, config.getDockerImages()[0]);
    }
}
