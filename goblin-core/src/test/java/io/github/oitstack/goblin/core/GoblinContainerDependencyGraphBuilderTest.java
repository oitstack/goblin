package io.github.oitstack.goblin.core;

import io.github.oitstack.goblin.spi.context.Image;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class GoblinContainerDependencyGraphBuilderTest {


    @Test
    public void testBuildDependencyGraph() {
        Image mysqlImage = new Image();
        mysqlImage.setType("MYSQL");
        mysqlImage.setId("mysql");
        Image redisImage = new Image();
        redisImage.setType("REDIS");
        redisImage.setId("redis");
        Image serviceAImage = new Image();
        serviceAImage.setType("SERVICE");
        serviceAImage.setId("serviceA");
        serviceAImage.setRequirement(new String[]{"mysql", "redis"});
        Image[] images = new Image[]{mysqlImage, redisImage, serviceAImage};

        GoblinGraph<Image> dependenciesGraph = GoblinContainerDependencyGraphBuilder.buildDependencyGraph(images);
        Assert.assertEquals(3, dependenciesGraph.size());
        Assert.assertEquals(2, dependenciesGraph.travel().size());

        List<GoblinGraph.Node<Image>> preBuildNodes = dependenciesGraph.travel().get(0);
        Assert.assertEquals(2, preBuildNodes.size());

        Assert.assertTrue(preBuildNodes.stream().anyMatch(node -> node.getValue().getId().equals("mysql")));
        Assert.assertTrue(preBuildNodes.stream().anyMatch(node -> node.getValue().getId().equals("redis")));
        Assert.assertEquals(1, dependenciesGraph.travel().get(1).size());
        Assert.assertEquals("serviceA", dependenciesGraph.travel().get(1).get(0).getValue().getId());
    }
}
