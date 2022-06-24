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
    public void imageNameParseTest2() {

        DockerImage dockerImageName = DockerImage.parseFrom("oitstack/damocles:v0.0.1",dockerClient);

        System.out.println(dockerImageName.getImageName().getVersion().getVersionDesc());
        System.out.println(dockerImageName.getImageName().getRegistry());
        System.out.println(dockerImageName.getImageName().getRepository());

        String repository = "docker-hub/mysql";
        final String mysqlTag = "mysql:5.7";



        DockerImage dockerImageName2 = DockerImage.parseFrom(mysqlTag,dockerClient);
        System.out.println(dockerImageName2.getImageName().getVersion().getVersionDesc());
        System.out.println(dockerImageName2.getImageName().getRegistry());
        System.out.println(dockerImageName2.getImageName().getRepository());
    }

    @Test
    public void imageNameParseTest() {

        String repository = "docker-hub/mysql";
        final String mysqlTag = "mysql:5.7";



        DockerImage dockerImageName = DockerImage.parseFrom(mysqlTag,dockerClient);
        System.out.println(dockerImageName.getImageName().getVersion().getVersionDesc());
        System.out.println(dockerImageName.getImageName().getRegistry());
        System.out.println(dockerImageName.getImageName().getRepository());

        Assert.assertEquals(dockerImageName.getImageName().getVersion().getVersionDesc(), "5.7");
        Assert.assertEquals(dockerImageName.getImageName().getRegistry(), REGISTRY);
        Assert.assertEquals(dockerImageName.getImageName().getRepository(), repository);
    }
}