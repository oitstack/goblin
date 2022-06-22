package io.github.oitstack.goblin.core.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class EnvExtUtilsTest {

    @Test
    public void testAddAll() {
        Map<String, String> sourceMap = new HashMap<>();
        sourceMap.put("testKeyA", "testValueA");
        sourceMap.put("testKeyB", "testValueB");

        EnvExtUtils.addAll(sourceMap);

        Assert.assertEquals("testValueA", EnvExtUtils.EXT_ENVS.get("testKeyA"));
        Assert.assertEquals("testValueB", EnvExtUtils.EXT_ENVS.get("testKeyB"));
    }

    @Test
    public void testPut() {
        Assert.assertEquals(false, EnvExtUtils.EXT_ENVS.containsKey("testKeyC"));
        EnvExtUtils.put("testKeyC", "testValueC");

        Assert.assertEquals("testValueC", EnvExtUtils.EXT_ENVS.get("testKeyC"));
    }

    @Test
    public void testRemove() {
        EnvExtUtils.put("testKeyD", "testValueD");
        Assert.assertEquals(true, EnvExtUtils.EXT_ENVS.containsKey("testKeyD"));
        EnvExtUtils.remove("testKeyD");

        Assert.assertEquals(false, EnvExtUtils.EXT_ENVS.containsKey("testKeyD"));
    }

    @Test
    public void testGet() {
        EnvExtUtils.put("testKeyE", "testValueE");

        Assert.assertEquals("testValueE", EnvExtUtils.EXT_ENVS.get("testKeyE"));
    }

    @Test
    public void testInjectEnv2Os() {
        EnvExtUtils.put("testKeyF", "testValueF");
        Assert.assertEquals("testValueF", EnvExtUtils.EXT_ENVS.get("testKeyF"));
        EnvExtUtils.injectEnv2Os();

        Assert.assertNotNull(System.getenv("testKeyF"));
        Assert.assertEquals("testValueF", System.getenv("testKeyF"));
    }
}
