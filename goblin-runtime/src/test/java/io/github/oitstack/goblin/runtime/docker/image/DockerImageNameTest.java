package io.github.oitstack.goblin.runtime.docker.image;

import io.github.oitstack.goblin.runtime.RunTimeBaseTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author yangguang
 * @date 2022/5/26
 */
public class DockerImageNameTest extends RunTimeBaseTest {

    @Test
    public void imageNameParseTest() {
        String repository = "docker-hub/mysql";
        final String mysqlTag = "mysql:5.7";

        DockerImage dockerImageName = DockerImage.parseFrom(mysqlTag);
        Assert.assertNotNull(dockerImageName);
        Assert.assertEquals(dockerImageName.getImageName().getVersion().getVersionDesc(), "5.7");
        Assert.assertEquals(dockerImageName.getImageName().getRegistry(), REGISTRY);
        Assert.assertEquals(dockerImageName.getImageName().getRepository(), repository);
    }
}
