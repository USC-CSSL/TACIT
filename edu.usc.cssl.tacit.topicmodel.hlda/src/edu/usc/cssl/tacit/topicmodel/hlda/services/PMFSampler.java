/**
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

import static java.lang.Math.exp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Sample a probability mass function. Each sample has a weight associated
 * with it that represents the mass of the sample. The sampler works by
 * collecting all of the weights. When <code>sample()</code> is called,
 * it chooses a random number between 0 and the total sum of all the
 * weights. It returns the index of the sample that the randomly chosen
 * number falls between.
 *
 * For example, assume that the sampler was given the weights [1.2, 0.5, 0.1].
 * The total sum of all the weights is 1.8. It would then draw a random number
 * between 0 and 1.8 and compare it to the following values:
 *
 * Sample 1 - index 0 - must be greater than 0.0 and less than or equal to 1.2
 * Sample 2 - index 1 - must be greater 1.2 and less than or equal to 1.7
 * Sample 3 - index 2 - must be greater 1.7 and less than or equal to 1.8
 *
 * If the random value drawn was 1.543, then index 1 would be returned.
 */
public class PMFSampler implements Serializable
{
    private double [] weights;
    private double total;
    private int size;
    private int currentSample;
    private Random random;

    public PMFSampler(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("size must be >= 1");
        }
        this.size = size + 1;
        weights = new double[this.size];
        weights[0] = 0.0;
        total = 0.0;
        currentSample = 1;
        random = new Random();
    }

    /**
     * Returns the total number of samples.
     *
     * @return the total number of samples
     */
    public int getSize() {
        return size - 1;
    }

    /**
     * Adds a sample to the sampler. If adding a sample would exceed the
     * total size allocated, the sample is silently dropped.
     *
     * @param weight the weight of the sample to add
     */
    public void add(double weight) {
        if (weight < 0.0) {
            throw new IllegalArgumentException("weight must be >= 0.0");
        }
        if (currentSample == size) {
            return;
        }
        weights[currentSample] = weight + total;
        total += weight;
        currentSample++;
    }

    /**
     * Choose a random number between 0 and the total sum of all the
     * weights. Return the index of the sample that the randomly chosen
     * number falls between. If all the weights are 0, choose an index
     * randomly.
     *
     * @return the index of the sample corresponding to the random choice
     */
    public int sample() {
        if (total == 0.0) {
            return random.nextInt(size - 1);
        }
        double value = total * random.nextDouble();
        for (int i = 1; i < size - 1; i++) {
            if (value > weights[i - 1] && value <= weights[i]) {
                return i - 1;
            }
        }
        return size - 2;
    }

    /**
     * Clear out the samples.
     */
    public void clear() {
        weights = new double[this.size];
        weights[0] = 0.0;
        total = 0.0;
        currentSample = 1;
    }

    /**
     * Returns an array containing the probabilities for each
     * item in the PMF. The probabilities are scaled such that
     * they range from 0 to 1.
     *
     * @return the array of probabilities
     */
    public double [] getProbabilities() {
        double [] result = new double[size - 1];
        for (int i = 1; i < size; i++) {
            result[i - 1] = (weights[i] - weights[i - 1]) / total;
        }
        return result;
    }

    /**
     * Given an array of log likelihoods, will normalize them and add them to a
     * PMFSampler.
     *
     * @param logLikelihoods the log likelihoods to normalize
     * @return a PMFSampler containing the normalized log likelihoods
     */
    public static PMFSampler normalizeLogLikelihoods(double [] logLikelihoods) {
    	
        Double biggestInArray = logLikelihoods[0];
        for (double num :logLikelihoods){
        	if (num>biggestInArray)
        		biggestInArray = num;
        }
        Double biggest = (biggestInArray.equals(Double.NaN)) ? Double.NEGATIVE_INFINITY : biggestInArray;
        List<Double> likelihoods = new ArrayList<Double>();
        for(double value:logLikelihoods){
        	likelihoods.add(exp(value-biggest));
        }
        double sum = 0.0;
        for (double value :likelihoods){
        	sum+=value;
        }
        
        PMFSampler sampler = new PMFSampler(likelihoods.size());
        for(double value:likelihoods){
        	sampler.add(value/sum);
        }
        return sampler;
    }
}
