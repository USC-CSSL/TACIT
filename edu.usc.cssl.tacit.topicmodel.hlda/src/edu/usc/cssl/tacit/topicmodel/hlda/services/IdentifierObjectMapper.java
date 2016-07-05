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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The IdentifierObjectMapper associates a numeric identifier with a
 * specific object. It also maintains the reverse mapping of an object
 * with a specific identifier.
 */
public class IdentifierObjectMapper<T extends Serializable> implements Serializable
{
    // Maps an object to a number
    private Map<T, Integer> objectIndexMap;

    // The inverse mapping of objectIndexMap
    private Map<Integer, T> indexObjectMap;

    // The next number to assign to the next object seen
    private int nextIndex;

    public IdentifierObjectMapper() {
        objectIndexMap = new HashMap<>();
        indexObjectMap = new HashMap<>();
    }

    /**
     * Adds an object to the mapper if it isn't already in it. Returns the
     * newly generated id for the object.
     *
     * @param object the object to add to the mapper
     * @return the id of the object
     */
    public int addObject(T object) {
        if (!objectIndexMap.containsKey(object)) {
            objectIndexMap.put(object, nextIndex);
            indexObjectMap.put(nextIndex, object);
            int result = nextIndex;
            incrementNextIndex();
            return result;
        }
        return objectIndexMap.get(object);
    }

    /**
     * Gets the object associated with the index number. If the index does
     * not appear in the mapping, will return null.
     *
     * @param index the index to get
     * @return the object at the specified index
     */
    public T getObjectFromIndex(int index) {
        if (indexObjectMap.containsKey(index)) {
            return indexObjectMap.get(index);
        }
        return null;
    }

    /**
     * Gets the index for the specified object. If the object is not in the
     * mapping, will return -1.
     *
     * @param object the object to check
     * @return the index of the object, or -1 if it does not exist
     */
    public int getIndexFromObject(T object) {
        if (objectIndexMap.containsKey(object)) {
            return objectIndexMap.get(object);
        }
        return -1;
    }

    /**
     * Returns <code>true</code> if the mapper contains the specified
     * object.
     *
     * @param object the object to check for
     * @return <code>true</code> if the object is in the mapper
     */
    public boolean contains(T object) {
        return objectIndexMap.containsKey(object);
    }

    /**
     * Returns <code>true</code> if the mapper contains the specified
     * index.
     *
     * @param index the index to check for
     * @return <code>true</code> if the index is in the mapper
     */
    public boolean containsIndex(int index) {
        return indexObjectMap.containsKey(index);
    }

    /**
     * Bumps the nextIndex counter up by 1.
     */
    private void incrementNextIndex() {
        nextIndex++;
    }

    /**
     * Returns the total size of the mapper. This is the number
     * of unique objects in the mapper.
     *
     * @return the number of objects in the mapper
     */
    public int size() {
        return indexObjectMap.size();
    }

    /**
     * Returns the set of keys for the mapper.
     *
     * @return the set of keys for the mapper
     */
    public Set<Integer> getIndexKeys() {
        return indexObjectMap.keySet();
    }

    /**
     * Removes an object based on its index.
     *
     * @param index the index of the object to remove
     */
    public void deleteIndex(int index) {
        if (indexObjectMap.containsKey(index)) {
            T objectToDelete = indexObjectMap.get(index);
            objectIndexMap.remove(objectToDelete);
            indexObjectMap.remove(index);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        IdentifierObjectMapper<?> that = (IdentifierObjectMapper<?>) o;

        return nextIndex == that.nextIndex && objectIndexMap.equals(that.objectIndexMap) && indexObjectMap.equals(that.indexObjectMap);

    }

    @Override
    public int hashCode() {
        int result = objectIndexMap.hashCode();
        result = 31 * result + indexObjectMap.hashCode();
        result = 31 * result + nextIndex;
        return result;
    }
}
