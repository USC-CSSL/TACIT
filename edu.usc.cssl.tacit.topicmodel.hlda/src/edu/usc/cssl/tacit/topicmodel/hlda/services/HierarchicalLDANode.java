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

import static java.lang.Math.log;

import java.io.Serializable;
import java.util.*;

/**
 * Implements a node on the tree formed by the Nested Chinese Restaurant Problem.
 */
public class HierarchicalLDANode implements Serializable
{
    // Stores the node's internal identifier
    private UUID internalId;

    // The identifier for the node - becomes the topic number
    private int id;

    // Stores the parent to this node
    private HierarchicalLDANode parent;

    // Stores the list of children spawned from this node
    private List<HierarchicalLDANode> children;

    // The number of children spawned by this node (saves calling children.size())
    private int numChildren;

    // The set of documents that have visited this node in a path
    protected Set<Integer> documentsVisitingNode;

    // The total number of documents that have visited the node
    private int numDocumentsVisitingNode;

    // A count of the number of words of the specified vocabulary index
    // wordCounts[wordIndexInVocab]
    private int [] wordCounts;

    // The total number of words in the node
    private int totalWordCount;

    // The level of this node in the tree
    private int level;

    private Set<Integer> wordsInNode;

    private double pathWeight;

    /**
     * Alternate constructor used to create a node with no parent. Nodes
     * without parents are considered to be root nodes.
     *
     * @param parent the parent of this node
     * @param vocabularySize the size of the vocabulary
     */
    public HierarchicalLDANode(HierarchicalLDANode parent, int vocabularySize) {
        this.parent = parent;
        internalId = UUID.randomUUID();
        children = new ArrayList<>();
        documentsVisitingNode = new HashSet<>();
        numChildren = 0;
        numDocumentsVisitingNode = 0;
        level = 0;
        wordCounts = new int[vocabularySize];
        wordsInNode = new HashSet<>();
        pathWeight = 0;
    }

    /**
     * Sets the level for this node.
     *
     * @param level the new level for the node
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Returns the level for this node.
     *
     * @return the level number of this node
     */
    public int getLevel() {
        return level;
    }

    /**
     * Spawns a new child on for this node, and returns the spawned child.
     *
     * @param level the new level for the node (between 0 and maxDepth)
     * @return the newly spawned node
     */
    public HierarchicalLDANode spawnChild(int level) {
        HierarchicalLDANode child = new HierarchicalLDANode(this, wordCounts.length);
        child.setLevel(level);
        children.add(child);
        numChildren++;
        return child;
    }

    /**
     * Returns the list of children of this node.
     *
     * @return the children of this node
     */
    public List<HierarchicalLDANode> getChildren() {
        return children;
    }

    /**
     * Returns the number of children spawned by this node.
     *
     * @return the number of children spawned by this node
     */
    public int getNumChildren() {
        return numChildren;
    }

    /**
     * Returns the parent of this node. A null parent indicates that this
     * node is the root node.
     *
     * @return the parent node of this node
     */
    public HierarchicalLDANode getParent() {
        return parent;
    }

    /**
     * Returns true if this node is the root node, false otherwise.
     *
     * @return true if this is the root node
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * Marks the fact that the specified document has visited the node.
     *
     * @param documentIndex the index of the document visiting the node
     */
    public void setVisited(int documentIndex) {
        if (!documentsVisitingNode.contains(documentIndex)) {
            numDocumentsVisitingNode++;
            documentsVisitingNode.add(documentIndex);
        }
    }

    /**
     * Removes the document from having visited the node.
     *
     * @param documentIndex the index of the document to remove
     */
    public void removeVisited(int documentIndex) {
        if (documentsVisitingNode.contains(documentIndex)) {
            numDocumentsVisitingNode--;
            documentsVisitingNode.remove(documentIndex);
        }
    }

    /**
     * Returns the Id of this node (which is basically the topic number).
     *
     * @return the topic number of the node
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the Id of this node (which is basically the topic number).
     *
     * @param id the topic number for the node
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the set of documents that have visited this node.
     *
     * @return the set of document indices that have visited the node
     */
    public Set<Integer> getDocumentsVisitingNode() {
        return documentsVisitingNode;
    }

    /**
     * Returns the total number of documents that have visited the node.
     *
     * @return the total number of documents visiting the node
     */
    public int getNumDocumentsVisitingNode() {
        return numDocumentsVisitingNode;
    }

    /**
     * Removes this node from the parent's list of children.
     */
    public void removeFromParent() {
        if (parent != null) {
            parent.children.remove(this);
            parent.numChildren--;
            parent = null;
        }
    }

    /**
     * Adds the specified word to the node.
     *
     * @param word the word to add to the node
     */
    public void addWord(Word word) {
        wordCounts[word.getVocabularyId()] += word.getTotalCount();
        totalWordCount += word.getTotalCount();
        wordsInNode.add(word.getVocabularyId());
    }

    /**
     * Removes the specified word from the node.
     *
     * @param word the word to remove
     */
    public void removeWord(Word word) {
        wordCounts[word.getVocabularyId()] -= word.getTotalCount();
        totalWordCount -= word.getTotalCount();
        if (wordCounts[word.getVocabularyId()] <= 0) {
            wordsInNode.remove(word.getVocabularyId());
            wordCounts[word.getVocabularyId()] = 0;
        }
    }

    /**
     * Returns the set of all vocabulary ids that exist within the node.
     *
     * @return the set of all vocabulary ids in the node
     */
    public Set<Integer> getWordsInNode() {
        return wordsInNode;
    }

    /**
     * Returns the total number of times the specified word appears in the node.
     *
     * @param wordIndexInVocab the index of the vocabulary word
     * @return the count of the number of times this word appears
     */
    public int getWordCount(int wordIndexInVocab) {
        return wordCounts[wordIndexInVocab];
    }

    /**
     * Returns the total number of words in the node.
     *
     * @return the total number of words in the node
     */
    public int getTotalWordCount() {
        return totalWordCount;
    }

    /**
     * Scans the specified <code>nodeMapper</code> for nodes that have 0 documents and deletes them.
     *
     * @param nodeMapper the IdentifierObjectMapper responsible for mapping nodes to ids
     */
    public static void deleteEmptyNodes(IdentifierObjectMapper<HierarchicalLDANode> nodeMapper) {
        List<Integer> nodesToDelete = new ArrayList<>();
        for (int nodeIndex : nodeMapper.getIndexKeys()) {
            HierarchicalLDANode node = nodeMapper.getObjectFromIndex(nodeIndex);
            if (node.getNumDocumentsVisitingNode() == 0 || node.wordsInNode.isEmpty()) {
                node.removeFromParent();
                nodesToDelete.add(nodeIndex);
            }
        }
        for(int i:nodesToDelete)
        	nodeMapper.deleteIndex(i);
    }

    /**
     * Returns the top <code>numWords</code> that best describe the topic. If <code>numWords</code>
     * is greater than the number of words in the node, returns the set of all words in the
     * node arranged by their frequency.
     *
     * @param numWords the number of words to return
     * @param vocabulary the global vocabulary
     * @return an List of Strings that are the words that describe the topic
     */
    public List<String> getTopWords(int numWords, IdentifierObjectMapper<String> vocabulary) {
        if (numWords <= 0) {
            throw new IllegalArgumentException("numWords must be > 0");
        }
        BoundedPriorityQueue<Integer> priorityQueue = new BoundedPriorityQueue<>(numWords);
        for (int wordIndex = 0; wordIndex < vocabulary.size(); wordIndex++) {
            int wordCount = getWordCount(wordIndex);
            if (wordCount > 0) {
                priorityQueue.add(getWordCount(wordIndex), wordIndex);
            }
        }
        List<String> objects = new ArrayList<String>();
        for(int index: priorityQueue.getElements())
        	objects.add(vocabulary.getObjectFromIndex(index));
        return objects;
    }

    /**
     * Returns a mapping of parent to children for each node. For example, if
     * node 0 has children 1 and 2, and node 2 has child 3, the resultant map
     * would look like:
     *
     * {
     *     0: [1, 2],
     *     1: [],
     *     2: [3],
     *     3: []
     * }
     *
     * @param nodeMapper the IdentifierObjectMapper responsible for mapping nodes to ids
     * @return a Map containing each node and a list of its children
     */
    public static Map<Integer, List<Integer>> generateMap(IdentifierObjectMapper<HierarchicalLDANode> nodeMapper) {
        Map<Integer, List<Integer>> result = new HashMap<>();
        for (int nodeId : nodeMapper.getIndexKeys()) {
            HierarchicalLDANode parent = nodeMapper.getObjectFromIndex(nodeId);
            List<Integer> children = new ArrayList<Integer>();
			for(HierarchicalLDANode node: parent.getChildren())
				children.add(node.getId());
            result.put(nodeId, children);
        }
        return result;
    }
    

    /**
     * Used for path selection probabilities. Confers the weight specified onto the node,
     * calculates what the child weights should be, and passes the sum of the weights down
     * to the children. If the node is an internal node, adds the weight of generating a new
     * child node to this node. The weight is calculated in log space.
     *
     * @param weight the weight to apply to this node (plus any weight of generating new nodes)
     * @param gamma the gamma hyper-parameter
     * @param maxDepth the maximum depth of the tree
     */
    public void propagatePathWeight(double weight, double gamma, int maxDepth) {
        pathWeight = weight;
        for (HierarchicalLDANode child : children) {
            child.propagatePathWeight(weight + log(child.getNumDocumentsVisitingNode() / (numDocumentsVisitingNode - 1 + gamma)), gamma, maxDepth);
        }

        if (level != (maxDepth - 1)) {
            pathWeight += log(gamma / (numDocumentsVisitingNode - 1 + gamma));
        }
    }

    /**
     * Gets the weight of the path at this point in the tree.
     *
     * @return the weight of the path
     */
    public double getPathWeight() {
        return pathWeight;
    }

    /**
     * The uniqueness of a HierarchicalLDANode is guaranteed only by its
     * internal identifier. Two HierarchicalLDANodes may have the exact same
     * state, but will be considered different.
     *
     * @param o the other object to test
     * @return true if the two nodes have the same UUID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        HierarchicalLDANode that = (HierarchicalLDANode) o;

        return internalId.equals(that.internalId);

    }

    @Override
    public int hashCode() {
        return internalId.hashCode();
    }
}
