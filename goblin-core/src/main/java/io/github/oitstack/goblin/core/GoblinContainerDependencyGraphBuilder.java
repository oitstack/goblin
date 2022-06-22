/*
 * Copyright 2022 OPPO Goblin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.oitstack.goblin.core;

import io.github.oitstack.goblin.spi.context.Image;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The sequence of containers constitutes a dependency graph, and this class is
 * used to build this graph.
 */
public class GoblinContainerDependencyGraphBuilder<T> {

    /**
     * Build the container node.
     * @param graph
     * @param imagesMap
     * @param nodesMap
     * @param imageName
     * @return
     */
    public static GoblinGraph.Node buildNode(GoblinGraph graph, Map<String, Image> imagesMap, HashMap<String, GoblinGraph.Node<Image>> nodesMap, String imageName) {

        if (!imagesMap.containsKey(imageName)) {
            return null;
        }
        if (nodesMap.containsKey(imageName)) {
            return nodesMap.get(imageName);
        }

        Image image = imagesMap.get(imageName);
        GoblinGraph.Node<Image> node = new GoblinGraph.Node<>(image);

        if (null != image.getRequirement()) {
            List<GoblinGraph.Node<Image>> prevNodes = new ArrayList<>();
            int maxDepth = 0;
            for (String requireImage : image.getRequirement()) {
                if (requireImage.equals(image.getId())) {
                    continue;
                }

                GoblinGraph.Node requireNode;
                if (nodesMap.containsKey(requireImage)) {
                    requireNode = nodesMap.get(requireImage);
                } else {
                    requireNode = buildNode(graph, imagesMap, nodesMap, requireImage);
                    if (null == requireNode) {
                        throw new RuntimeException(
                                String.format(
                                        "require image not defined. base: %s, require: %s",
                                        image.getId(),
                                        requireImage
                                )
                        );
                    }
                }
                maxDepth = Math.max(requireNode.getDepth(), maxDepth);
                prevNodes.add(requireNode);
            }

            int finalMaxDepth = maxDepth;
            prevNodes = prevNodes.stream().filter(n -> n.getDepth() >= finalMaxDepth).collect(Collectors.toList());
            node.setPrev(prevNodes);
        }

        nodesMap.put(imageName, node);
        graph.addNode(node);
        return node;
    }


    /**
     * Build a container dependency graph.
     * @param images
     * @return
     */
    public static GoblinGraph<Image> buildDependencyGraph(Image[] images) {
        GoblinGraph graph = new GoblinGraph<Image>();

        if (null != images) {
            final Map<String, Image> imagesMap
                    = Arrays.asList(images).stream()
                    .collect(Collectors.toMap(k -> k.getId(), k -> k, (o1, o2) -> o1));
            HashMap<String, GoblinGraph.Node<Image>> nodesMap = new HashMap<>();
            for (Image image : images) {
                buildNode(graph, imagesMap, nodesMap, image.getId());
            }
        }

        return graph;
    }
}
