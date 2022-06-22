package io.github.oitstack.goblin.runtime.docker.client;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InfoCmd;
import com.github.dockerjava.api.command.VersionCmd;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Version;
import com.github.dockerjava.core.DockerClientImpl;
import io.github.oitstack.goblin.runtime.RunTimeBaseTest;
import io.github.oitstack.goblin.runtime.docker.utils.JsonTool;
import io.github.oitstack.goblin.runtime.utils.PropertyAndEnvUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * @author yangguang
 * @date 2022/5/26
 */
public class AutoRecycleDockerClientTest extends RunTimeBaseTest {

    @Mock
    DockerClient dockerClient;

    @Mock
    InfoCmd infoCmd;

    @Mock
    VersionCmd versionCmd;
    @Mock
    DamoclesManager damoclesManager;


    @Test
    public void testDockerClient() {

        when(dockerClient.infoCmd()).thenReturn(infoCmd);
        when(infoCmd.exec()).thenReturn(new Info());


        when(dockerClient.versionCmd()).thenReturn(versionCmd);
        Version version = JsonTool.readValue("{\"ApiVersion\":\"1.10\"}", Version.class);
        when(versionCmd.exec()).thenReturn(version);


        MockedStatic<DockerClientImpl> dockerClientMock = Mockito.mockStatic(DockerClientImpl.class);
        dockerClientMock.when(() -> DockerClientImpl.getInstance(anyObject(), anyObject())).thenReturn(dockerClient);

//        MockedStatic<DamoclesManager> damoclesManagerMock = Mockito.mockStatic(DamoclesManager.class);
        doNothing().when(damoclesManager).startAndRegisterClient(anyString(), anyObject());
//        damoclesManagerMock
//                .when(() -> new DamoclesManager(anyObject()).startAndRegisterClient(anyString(), anyObject()))
//                .thenAnswer(invocation -> null);

        PropertyAndEnvUtils.setProperty("enable_damocles", "false");
        AutoRecycleDockerClient autoRecycleDockerClient = AutoRecycleDockerClient.getInstance();

        Assert.assertNotNull(autoRecycleDockerClient.getMetaData());
        Assert.assertTrue(autoRecycleDockerClient.getMetaData().getVersion() == version);
        Assert.assertEquals(autoRecycleDockerClient.getMetaData().getDockerHost().getHost() + ":"
                + autoRecycleDockerClient.getMetaData().getDockerHost().getPort(), DOCKER_HOST);

    }

}
