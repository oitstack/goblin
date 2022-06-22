package io.github.oitstack.goblin.core;

import io.github.oitstack.goblin.spi.context.Configuration;
import io.github.oitstack.goblin.spi.context.Image;
import io.github.oitstack.goblin.core.container.MySQLTestContainer;
import io.github.oitstack.goblin.core.spi.GoblinLifeCycleInterceptorOrder10;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yangguang
 * @date 2022/5/27
 */
@RunWith(MockitoJUnitRunner.class)
public class GoblinBootstrapTest {

    @Test
    public void testBootstrap() {
        GoblinBootstrap.getInstance();
        Goblin goblin = Goblin.getInstance();

        Configuration configuration = goblin.getConfiguration();
        Assert.assertEquals(configuration.getEnv().get("TEST_ENV"), "env");
        Assert.assertEquals(System.getenv().get("TEST_ENV"), "env");
        Assert.assertEquals(configuration.getProps().get("test_prop"), "prop");
        Assert.assertEquals(System.getProperties().get("test_prop"), "prop");


        Assert.assertNotNull(configuration.getDockerImages());
        Image mysqlImage = getImageByType(configuration.getDockerImages(), MySQLTestContainer.CONTAINER_TYPE);
        Assert.assertNotNull(mysqlImage);
        Assert.assertEquals(mysqlImage.getImageVersion(), "mysql:5.7");
        Assert.assertEquals(mysqlImage.getId(), MySQLTestContainer.CONTAINER_TYPE);

        Map<String, String> placeHolders = goblin.getPlaceHolders();
        String userNamePlaceHolderKey = String.format(Goblin.PLACE_HOLDER_TPL, mysqlImage.getId().toUpperCase(), "USERNAME");
        Assert.assertEquals(placeHolders.get(userNamePlaceHolderKey), MySQLTestContainer.MYSQL_ROOT_USER);
        Assert.assertEquals(System.getProperties().get(userNamePlaceHolderKey), MySQLTestContainer.MYSQL_ROOT_USER);

        String passWordPlaceHolderKey = String.format(Goblin.PLACE_HOLDER_TPL, mysqlImage.getId().toUpperCase(), "PASSWORD");
        Assert.assertEquals(placeHolders.get(passWordPlaceHolderKey), MySQLTestContainer.DEFAULT_PASSWORD);
        Assert.assertEquals(System.getProperties().get(passWordPlaceHolderKey), MySQLTestContainer.DEFAULT_PASSWORD);

        GoblinContainer mysqlContainer = goblin.getContainerInstance(MySQLTestContainer.CONTAINER_TYPE);
        Assert.assertNotNull(mysqlContainer);
        Assert.assertTrue(mysqlContainer instanceof  MySQLTestContainer);


        Assert.assertEquals(System.getProperty(GoblinLifeCycleInterceptorOrder10.GOBLIN_LIFE_CYCLE_PRE_PROCESSED_KEY), "true");
        Assert.assertEquals(System.getProperty(GoblinLifeCycleInterceptorOrder10.GOBLIN_LIFE_CYCLE_POST_PROCESSED_KEY), "true");
    }

    private Image getImageByType(Image[] dockerImages, String type) {
        if (dockerImages != null) {
            for (Image image : dockerImages) {
                if (image.getType().equalsIgnoreCase(type)) {
                    return image;
                }
            }
        }

        return null;
    }

    @Test
    public void testBuildCoreConfiguration() {
        GoblinBootstrap bootstrap = GoblinBootstrap.getInstance();
        Class cls = bootstrap.getClass();
        Method testMethod = null;
        Map<String, Object> data = new HashMap<>();

        Image imageA = new Image();
        imageA.setId("mysql");
        imageA.setImageVersion("mysql:5.7");
        imageA.setType("MYSQL");
        Image[] images = new Image[] {imageA};
        data.put("dockerImages", images);
        Map<String, String> envMap = new HashMap<>();
        envMap.put("testKey", "testValue");
        data.put("env", envMap);

        try {
            testMethod = cls.getDeclaredMethod("buildCoreConfiguration", Map.class);
            testMethod.setAccessible(true);
            Configuration configuration = (Configuration) (testMethod.invoke(bootstrap, data));

            Assert.assertNotNull(configuration);
            Assert.assertEquals(imageA, configuration.getDockerImages()[0]);
            Assert.assertEquals("testValue", configuration.getEnv().get("testKey"));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


    }
}
