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
import java.util.*;

/**
 * A SparseDocument contains only the count of the unique words in a document,
 * as well as the words themselves.
 */
public class SparseDocument implements Serializable
{
    // Maps the ID of a word to a Word object
    private Map<Integer, Word> wordMap;

    // The Vocabulary that provides the mapping from word
    // Strings to numbers
    private IdentifierObjectMapper<String> vocabulary;

    public SparseDocument(IdentifierObjectMapper<String> vocabulary) {
        if (vocabulary == null) {
            throw new IllegalArgumentException("vocabulary cannot be null");
        }
        this.vocabulary = vocabulary;
        wordMap = new HashMap<>();
    }

    /**
     * Each document is a List of Strings that represent the
     * words in the document. For each word, add it to the vocabulary,
     * and then add the vocabulary number assigned to the word to
     * the wordArray for the document. If <code>addWordsToVocabulary</code>
     * is <code>true</code>, then previously unseen vocabulary words will be
     * added to the vocabulary.
     *
     * @param words the list of words in the document
     * @param addWordsToVocabulary if true, then previously unseen vocabulary words will be added to the vocabulary
     */
    public void readDocument(List<String> words, boolean addWordsToVocabulary) {
    	
        Map<String, Long> wordCounts = new HashMap<String, Long>();
    	for(String word:words){
    		if(wordCounts.containsKey(word)){
    			long temp = wordCounts.get(word)+ 1;
    			wordCounts.put(word, temp);
    		}
    		else{
    			wordCounts.put(word, (long) 1);
    		}
    	}
    	
        
        for (String word : wordCounts.keySet()) {
            boolean vocabularyContainsWord = vocabulary.contains(word);
            if ((vocabularyContainsWord) || (!vocabularyContainsWord && addWordsToVocabulary)) {
                Word newWord = new Word(word, vocabulary.addObject(word));
                newWord.setTotalCount(wordCounts.get(word).intValue());
                wordMap.put(newWord.getVocabularyId(), newWord);
            }
        }
    }

    /**
     * Sets topic for the vocabulary word.
     *
     * @param vocabularyWord the word to set
     * @param topic the topic number to set
     */
    public void setTopicForWord(int vocabularyWord, int topic) {
        if (wordMap.containsKey(vocabularyWord)) {
            Word word = wordMap.get(vocabularyWord);
            word.setTopic(topic);
        }
    }

    /**
     * Gets the topic for the vocabulary word. Will return -1 if the word is not
     * found, or if a topic is not set.
     *
     * @param vocabularyWord the vocabulary word to look up
     * @return the topic for the word or -1 if not set or not found
     */
    public int getTopicForWord(int vocabularyWord) {
        return (wordMap.containsKey(vocabularyWord)) ? wordMap.get(vocabularyWord).getTopic() : -1;
    }

    /**
     * Gets the count of this vocabulary word in this document. Will return 0
     * if the word does not occur in this document.
     *
     * @param vocabularyWord the word count to get
     * @return the count of the number of times this word appears
     */
    public int getWordCount(int vocabularyWord) {
        return (wordMap.containsKey(vocabularyWord)) ? wordMap.get(vocabularyWord).getTotalCount() : 0;
    }

    /**
     * Returns the Set of Word objects that appears in the document.
     *
     * @return the Set of Word objects in the document
     */
    public Set<Word> getWordSet() {
    	return new HashSet<>(wordMap.values());
    }



    /**
     * Returns a count of the number of words in each topic. The result is a map
     * with the topic number being the key, and the topic count being the value.
     *
     * @return a Map of topic numbers to number of words
     */
    public Map<Integer, Integer> getTopicCounts() {
    	Map<Integer, Integer> map = new HashMap<Integer, Integer>();
    	for(Word word:wordMap.values()){
    		int key = word.getTopic();
    		if(map.containsKey(key)){
    			int temp = map.get(key)+ word.getTotalCount();
    			map.put(key, temp);
    		}
    		else{
    			map.put(key, word.getTotalCount());
    		}
    	}
        return map;
    }


    /**
     * Returns a count of each of the words in the specified topic. The Map returned
     * maps a word vocabulary id to a count of the number of times it appears in the
     * document.
     *
     * @param topic the topic number to count
     * @return a map
     */
    public Map<Integer, Integer> getWordCountsByTopic(int topic) {
    	Map<Integer, Integer> map = new HashMap<Integer, Integer>();
    	for(Word word:wordMap.values()){
    		if(word.getTopic()==topic){
    			map.put(word.getVocabularyId(),word.getTotalCount());
    		}
    	}
    	return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        SparseDocument that = (SparseDocument) o;

        return wordMap.equals(that.wordMap) && vocabulary.equals(that.vocabulary);
    }

    @Override
    public int hashCode() {
        int result = wordMap.hashCode();
        result = 31 * result + vocabulary.hashCode();
        return result;
    }
}
