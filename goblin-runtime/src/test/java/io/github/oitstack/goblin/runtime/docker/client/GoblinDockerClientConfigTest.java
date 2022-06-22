package io.github.oitstack.goblin.runtime.docker.client;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.RemoteApiVersion;
import io.github.oitstack.goblin.runtime.RunTimeBaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

/**
 * @author yangguang
 * @date 2022/5/30
 */
public class GoblinDockerClientConfigTest extends RunTimeBaseTest {

    @Test
    public void testDockerClientConfig() {
        RemoteApiVersion remoteApiVersion = RemoteApiVersion.VERSION_1_7;
        String certPath = "/certpath";
        Properties p = new Properties();
        p.put(DefaultDockerClientConfig.DOCKER_TLS_VERIFY, "0");
        p.put(DefaultDockerClientConfig.DOCKER_CERT_PATH, certPath);
        p.put(DefaultDockerClientConfig.DOCKER_HOST, "tcp://" + DOCKER_HOST);

        GoblinDockerClientConfig goblinDockerClientConfig = new GoblinDockerClientConfig.Builder()
                .withProperties(p).withApiVersion(remoteApiVersion).build();

        Assert.assertTrue(goblinDockerClientConfig.getApiVersion().getVersion().equals(remoteApiVersion.getVersion()));
        Assert.assertEquals(goblinDockerClientConfig.getDockerHost().toString(), "tcp://" + DOCKER_HOST);


    }
}
