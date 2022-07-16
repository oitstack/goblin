package io.github.oitstack.goblin.runtime;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import io.github.oitstack.goblin.runtime.config.RunTimeConfig;
import io.github.oitstack.goblin.runtime.docker.host.DockerHostSelector;
import io.github.oitstack.goblin.runtime.utils.PropertyAndEnvUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

/**
 * @author yangguang
 * @date 2022/5/26
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class RunTimeBaseTest {

    public final static String DOCKER_HOST = "localhost:12538";

    public final static String PROPERTY_KEY = "property_key";
    public final static String PROPERTY_VALUE = "property_value";
    public final static String REGISTRY = "localhost";

    @Mock
    DockerHostSelector dockerHostSelector;

    MockedStatic<DockerHostSelector> dockerHostConfigMock;

    @BeforeClass
    public static void init() {
        PropertyAndEnvUtils.setProperty(RunTimeConfig.RUNTIME_CONFIG_PREFIX + DefaultDockerClientConfig.DOCKER_HOST, DOCKER_HOST);
        PropertyAndEnvUtils.setProperty(PROPERTY_KEY, PROPERTY_VALUE);
        System.setProperty("GOBLIN_RUNTIME_registry.url", REGISTRY);
        System.setProperty("GOBLIN_RUNTIME_default_docker_hub", REGISTRY + "/docker-hub/");
    }

    @Before
    public void initTest() {
        dockerHostConfigMock = Mockito.mockStatic(DockerHostSelector.class);
        dockerHostConfigMock.when(DockerHostSelector::getInstance).thenReturn(dockerHostSelector);
        when(dockerHostSelector.getSelectedDockerHost()).thenReturn(DOCKER_HOST);
    }

    @After
    public void afterTest() {
        dockerHostConfigMock.close();
    }

    @Test
    public void voidTest() {

    }
    public static<T> void  change(T ss){
        System.out.println(ss);
    }
    static interface Itest<T>{
        public void change(T ss);
    }
   static class TestImpl implements Itest<Long>{
        public void change(Long ss){
            System.out.println(ss);
        }

    }
    public static void main(String[] args) {
        String s="554669163767922689";
        Itest it=new TestImpl();
        it.change(s);
    }

}
