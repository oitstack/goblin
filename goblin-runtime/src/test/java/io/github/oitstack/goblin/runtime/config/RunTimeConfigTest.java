package io.github.oitstack.goblin.runtime.config;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import io.github.oitstack.goblin.runtime.RunTimeBaseTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author yangguang
 * @date 2022/5/26
 */
public class RunTimeConfigTest extends RunTimeBaseTest {

    @Test
    public void dockerHostInjectTest() {
        RunTimeConfig instance = RunTimeConfig.getInstance();
        Assert.assertEquals(instance.get(RunTimeConfig.RUNTIME_CONFIG_PREFIX + DefaultDockerClientConfig.DOCKER_HOST),
                "tcp://" + DOCKER_HOST);
    }

    @Test
    public void sysPropertiesInjectTest() {
        RunTimeConfig instance = RunTimeConfig.getInstance();
        Assert.assertEquals(instance.get(PROPERTY_KEY), PROPERTY_VALUE);
    }

    @Test
    public void localConfigPropertiesInjectTest() {
        final String configKey = "local_config_test_key";
        final String configValue = "local_config_test_value";
        RunTimeConfig instance = RunTimeConfig.getInstance();
        Assert.assertEquals(instance.get(configKey), configValue);
    }
}
