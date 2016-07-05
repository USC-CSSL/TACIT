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

/**
 * The Word class keeps track of a word within a document. Each word contains
 * a vocabulary ID associated with it, the raw string used to store the word,
 * a topic associated with the word, and the total number of times the word
 * appears in a document (or corpus).
 */
public class Word implements Serializable
{
    // The vocabulary identifier for the word
    private int vocabularyId;

    // The String representation of the word
    private String rawWord;

    // The total number of times this word appears in a document
    private int totalCount;

    // The topic number assigned to the word
    private int topic;

    public Word(String rawWord, int vocabularyId) {
        if (rawWord == null) {
            throw new IllegalArgumentException("rawWord cannot be null");
        }
        this.vocabularyId = vocabularyId;
        this.rawWord = rawWord;
        totalCount = 1;
        topic = -1;
    }

    /**
     * Sets the total number of times this word appears.
     *
     * @param newCount the total number of times the word appears
     */
    public void setTotalCount(int newCount) {
        totalCount = newCount;
    }

    /**
     * Gets the total number of times this word appears.
     *
     * @return the total number of times this word appears
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Sets the topic for the word.
     *
     * @param newTopic the topic for the word
     */
    public void setTopic(int newTopic) {
        topic = newTopic;
    }

    /**
     * Gets the topic for the word.
     *
     * @return the topic for the word
     */
    public int getTopic() {
        return topic;
    }

    /**
     * Returns the String representation of the word.
     *
     * @return the String representation of the word
     */
    public String getRawWord() {
        return rawWord;
    }

    /**
     * Returns the vocabulary number for the word.
     *
     * @return the vocabulary id number for the word
     */
    public int getVocabularyId() {
        return vocabularyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if ((o == null) || (getClass() != o.getClass())) { return false; }

        Word word = (Word) o;

        if (getVocabularyId() != word.getVocabularyId()) { return false; }
        if (getTotalCount() != word.getTotalCount()) { return false; }
        return getTopic() == word.getTopic() && getRawWord().equals(word.getRawWord());

    }

    @Override
    public int hashCode() {
        int result = getVocabularyId();
        result = 31 * result + getRawWord().hashCode();
        result = 31 * result + getTotalCount();
        result = 31 * result + getTopic();
        return result;
    }
}
