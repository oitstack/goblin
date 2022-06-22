package io.github.oitstack.goblin.core;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class GoblinGraphTest {


    @Test
    public void testAddNullNode() {
        GoblinGraph graph = new GoblinGraph();
        graph.addNode(null);

        Assert.assertEquals(0, graph.size());
    }

    @Test
    public void testAddNoDependenciesNode() {
        GoblinGraph graph = new GoblinGraph();

        GoblinGraph.Node nodeA = new GoblinGraph.Node("A");
        GoblinGraph.Node nodeB = new GoblinGraph.Node("B");
        Assert.assertEquals(0, graph.size());
        graph.addNode(nodeA);
        Assert.assertEquals(1, graph.size());
        GoblinGraph.Node foundNodeA = graph.findNode("A");
        Assert.assertEquals("A", foundNodeA.getValue());
        Assert.assertEquals(1, foundNodeA.getDepth());
        graph.addNode(nodeB);
        GoblinGraph.Node foundNodeB = graph.findNode("B");
        Assert.assertEquals(2, graph.size());
        Assert.assertEquals("B", foundNodeB.getValue());
        Assert.assertEquals(1, foundNodeB.getDepth());
    }

    @Test
    public void testAddHasDependenciesNode() {
        GoblinGraph<String> graph = new GoblinGraph();
        GoblinGraph.Node<String> nodeA = new GoblinGraph.Node("A");
        graph.addNode(nodeA);
        GoblinGraph.Node<String> nodeB = new GoblinGraph.Node("B");
        nodeB.appendPrev(nodeA);
        graph.addNode(nodeB);

        Assert.assertEquals(2, graph.size());
        GoblinGraph.Node<String> foundNodeA = graph.findNode("A");
        Assert.assertEquals("A",  foundNodeA.getValue());
        Assert.assertEquals(1,  foundNodeA.getDepth());
        Assert.assertEquals(1,  foundNodeA.getNext().size());
        Assert.assertEquals("B",  foundNodeA.getNext().get(0).getValue());

        GoblinGraph.Node<String> foundNodeB = graph.findNode("B");
        Assert.assertEquals("B", foundNodeB.getValue());
        Assert.assertEquals(2, foundNodeB.getDepth());
        Assert.assertEquals(1, foundNodeB.getPrev().size());
        Assert.assertEquals("A", foundNodeB.getPrev().get(0).getValue());
    }

    @Test
    public void testTravelGraph() {
        GoblinGraph graph = new GoblinGraph();

        List<List<GoblinGraph.Node>> nodes = graph.travel();
        Assert.assertEquals(0, nodes.size());

        GoblinGraph.Node nodeA = new GoblinGraph.Node("A");
        GoblinGraph.Node nodeB = new GoblinGraph.Node("B");
        GoblinGraph.Node nodeC = new GoblinGraph.Node("C");
        nodeB.appendPrev(nodeA);
        nodeC.appendPrev(nodeB);
        graph.addNode(nodeA);
        graph.addNode(nodeB);
        graph.addNode(nodeC);

        nodes = graph.travel();
        Assert.assertEquals(3, nodes.size());
        Assert.assertEquals("A", nodes.get(0).get(0).getValue());
        Assert.assertEquals(1, nodes.get(0).get(0).getDepth());
        Assert.assertEquals("B", nodes.get(1).get(0).getValue());
        Assert.assertEquals(2, nodes.get(1).get(0).getDepth());
        Assert.assertEquals("C", nodes.get(2).get(0).getValue());
        Assert.assertEquals(3, nodes.get(2).get(0).getDepth());
    }
}
