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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Goblin container dependency graph.
 * @param <T> types of graph nodes
 */
public class GoblinGraph<T> {

    /**
     * root Node of Goblin container dependency graph.
     */
    private Node root;

    /**
     * Number of Goblin container dependency graph nodes.
     */
    private int size = 0;

    public static class Node<T> {
        private List<Node<T>> next;
        private List<Node<T>> prev;
        private T value;
        private int depth;

        Node(T t) {
            value = t;
        }

        public List<Node<T>> getNext() {
            return next;
        }

        public void setNext(List<Node<T>> next) {
            this.next = next;
        }

        public void appendNext(Node<T> node) {
            if (null == next) {
                next = new ArrayList<>();
            }

            next.add(node);
        }

        public void appendPrev(Node<T> node) {
            if (null == prev) {
                prev = new ArrayList<>();
            }

            prev.add(node);
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public List<Node<T>> getPrev() {
            return prev;
        }

        public void setPrev(List<Node<T>> prev) {
            this.prev = prev;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public int getDepth() {
            return depth;
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }
    }

    public GoblinGraph() {
        root = new Node(null);
    }

    public int size() {
        return size;
    }

    /**
     * Add container node
     * @param node
     */
    public void addNode(Node node) {

        if (null == node) {
            return;
        }

        if (null == node.getPrev()) {
            node.setDepth(1);
            root.appendNext(node);
        } else {
            List<Node> prevNodes = node.getPrev();

            for (Node prevNode : prevNodes) {
                prevNode.appendNext(node);
                node.setDepth(prevNode.getDepth() + 1);
                break;
            }
        }
        size++;
    }

    /**
     * Find container node
     * @param t
     * @return
     */
    public Node<T> findNode(T t) {
        return findNode(null, t);
    }

    private Node<T> findNode(Node<T> rootNode, T t) {

        if (rootNode == null) {
            rootNode = root;
        }

        if (null != rootNode.getValue() && rootNode.getValue().equals(t)) {
            return rootNode;
        }

        List<Node<T>> nextNodes = rootNode.getNext();

        if (null != nextNodes) {
            for (Node nextNode : nextNodes) {
                if (t.equals(nextNode.getValue())) {
                    return nextNode;
                }
            }

            for (Node nextNode : nextNodes) {
                if (null != nextNode.getNext()) {
                    List<Node> nextRootNodes = nextNode.getNext();
                    for (Node nextRootNode : nextRootNodes) {
                        Node node = findNode(nextRootNode, t);
                        if (node != null) {
                            return node;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Traverse container nodes.
     * @return
     */
    public List<List<Node<T>>> travel() {
        return travel(null, null);
    }

    /**T
     * Traverse container nodes.
     * @return
     */
    private List<List<Node<T>>> travel(List<Node<T>> rootNodes, List<List<Node<T>>> nodes) {
        if (rootNodes == null) {
            rootNodes = new ArrayList<>();
            rootNodes.add(root);
        }

        if (null == nodes) {
            nodes = new ArrayList<>();
        }

        Set<Node<T>> childNodes = new HashSet<>();

        for (Node rootNode : rootNodes) {
            if (rootNode.getNext() != null) {
                childNodes.addAll(rootNode.getNext());
            }
        }

        if (childNodes.size() != 0) {
            nodes.add(new ArrayList<>(childNodes));
            travel(new ArrayList<>(childNodes), nodes);
        }

        return nodes;
    }
}
