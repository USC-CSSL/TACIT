/*
 * Copyright 2015 Craig Thomas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usc.cssl.tacit.topicmodel.hlda.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a path through a Hierarchical LDA Tree.
 */
public class HierarchicalLDAPath implements Serializable
{
    // The nodes in the path
    private HierarchicalLDANode [] nodes;

    // The maximum depth of the path to be generated
    private int maxDepth;

    // The current depth of the path
    private int currentDepth;

    public HierarchicalLDAPath(HierarchicalLDANode rootNode, int maxDepth) {
        if (maxDepth < 1) {
            throw new IllegalArgumentException("maxDepth must be > 0");
        }
        if (rootNode == null) {
            throw new IllegalArgumentException("rootNode cannot be null");
        }
        nodes = new HierarchicalLDANode[maxDepth];
        nodes[0] = rootNode;
        this.maxDepth = maxDepth;
        currentDepth = 1;
    }

    /**
     * Adds a node to the path.
     *
     * @param node the node to add to the path
     */
    public void addNode(HierarchicalLDANode node) {
        if (currentDepth < maxDepth) {
            nodes[currentDepth] = node;
            currentDepth++;
        }
    }

    /**
     * Returns the last node added (the highest level node in the path).
     *
     * @return the last node added
     */
    public HierarchicalLDANode getCurrentNode() {
        return nodes[currentDepth - 1];
    }

    /**
     * Gets the node in the path on the specified level.
     *
     * @param level the level of the node to fetch
     * @return the node at the specified level
     */
    public HierarchicalLDANode getNode(int level) {
        if (level < 0 || level >= maxDepth) {
            throw new IllegalArgumentException("level must be >= 0 and < maxDepth");
        }
        return nodes[level];
    }

    /**
     * Returns an array containing all the nodes in the path.
     *
     * @return the array of nodes in the path
     */
    public HierarchicalLDANode [] getNodes() {
        return nodes;
    }

    /**
     * Clears out everything except the root node from the array of nodes.
     */
    public void clear() {
        for (int level = 0; level < maxDepth; level++) {
            nodes[level] = null;
        }
        currentDepth = 0;
    }

    /**
     * Returns <code>true</code> if the maximum depth of the path has been
     * reached.
     *
     * @return <code>true</code> if the path has reached its maximum depth
     */
    public boolean atMaxDepth() {
        return currentDepth == maxDepth;
    }

    /**
     * Removes the document from the path. Note: does not remove the words in
     * the document from the path.
     *
     * @param documentIndex the index of the document to remove
     */
    public void removeDocument(int documentIndex) {
        for (int level = 0; level < currentDepth; level++) {
            HierarchicalLDANode currentNode = getNode(level);
            currentNode.removeVisited(documentIndex);
        }
    }

    /**
     * Adds a document to the path.
     *
     * @param documentIndex the index of the document to add
     */
    public void addDocument(int documentIndex) {
        for (int level = 0; level < currentDepth; level++) {
            HierarchicalLDANode currentNode = getNode(level);
            currentNode.setVisited(documentIndex);
        }
    }

    /**
     * Given the root node of the tree, enumerate all possible paths that can
     * exist through the tree. Returns a list of all paths. The list of paths uses
     * the node ids to refer to the path elements (e.g. [[0, 4, 9], [0, 2, 8]]).
     *
     * @param rootNode the root node of the tree
     * @param maxDepth the maximum depth of the paths to generate
     * @return the list of paths through the tree
     */
    public static List<List<Integer>> enumeratePaths(HierarchicalLDANode rootNode, int maxDepth) {
        return enumeratePathComponents(rootNode, 0, maxDepth);
    }

    /**
     * Helper function for the <code>enumeratePaths</code> function that will recursively
     * build a list of paths.
     *
     * @param node the current node to check
     * @param depth the current depth in the list of paths
     * @param maxDepth the maximum depth to descend to
     * @return the list of paths through the tree
     */
    protected static List<List<Integer>> enumeratePathComponents(HierarchicalLDANode node, int depth, int maxDepth) {
        List<List<Integer>> result = new ArrayList<>();
        if (depth == maxDepth - 1) {
            List<Integer> thisNode = new ArrayList<>();
            thisNode.add(node.getId());
            result.add(thisNode);
            return result;
        }

        for (HierarchicalLDANode child : node.getChildren()) {
            List<List<Integer>> childPaths = enumeratePathComponents(child, depth + 1, maxDepth);
            for (List<Integer> childPath : childPaths) {
                childPath.add(0, node.getId());
                result.add(childPath);
            }
        }

        List<Integer> emptyChildPath = new ArrayList<>();
        emptyChildPath.add(0, node.getId());
        for (int i = depth; i < maxDepth - 1; i++) {
            emptyChildPath.add(-1);
        }
        result.add(emptyChildPath);
        return result;
    }

    /**
     * Given a list of node ids, add those nodes to the current path. Partial paths
     * are allowed
     *
     * @param path the list of node ids to add
     * @param nodeMapper the object responsible for mapping node ids to nodes
     */
    public void addPath(List<Integer> path, IdentifierObjectMapper<HierarchicalLDANode> nodeMapper) {
        clear();
        int length = path.size() < maxDepth ? path.size() : maxDepth;
        for (int level = 0; level < length; level++) {
            int nodeId = path.get(level);
            if (nodeId != -1) {
                addNode(nodeMapper.getObjectFromIndex(nodeId));
            } else {
                HierarchicalLDANode newNode = getCurrentNode().spawnChild(level);
                int newNodeId = nodeMapper.addObject(newNode);
                newNode.setId(newNodeId);
                addNode(newNode);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        HierarchicalLDAPath that = (HierarchicalLDAPath) o;

        if (maxDepth != that.maxDepth) { return false; }
        if (currentDepth != that.currentDepth) { return false; }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(getNodes(), that.getNodes());

    }

    @Override
    public int hashCode() {
        int result = (getNodes() != null) ? Arrays.hashCode(getNodes()) : 0;
        result = 31 * result + maxDepth;
        result = 31 * result + currentDepth;
        return result;
    }
}
