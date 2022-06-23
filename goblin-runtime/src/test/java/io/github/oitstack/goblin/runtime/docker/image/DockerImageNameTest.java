package io.github.oitstack.goblin.runtime.docker.image;

import com.github.dockerjava.api.DockerClient;
import io.github.oitstack.goblin.runtime.RunTimeBaseTest;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author yangguang
 */
public class DockerImageNameTest extends RunTimeBaseTest {

    @Mock
    DockerClient dockerClient;

    @Test
    public void imageNameParseTest() {
        String repository = "docker-hub/mysql";
        final String mysqlTag = "mysql:5.7";

        DockerImage dockerImageName = DockerImage.parseFrom(mysqlTag,dockerClient);
        Assert.assertNotNull(dockerImageName);
        Assert.assertEquals(dockerImageName.getImageName().getVersion().getVersionDesc(), "5.7");
        Assert.assertEquals(dockerImageName.getImageName().getRegistry(), REGISTRY);
        Assert.assertEquals(dockerImageName.getImageName().getRepository(), repository);
    }
}