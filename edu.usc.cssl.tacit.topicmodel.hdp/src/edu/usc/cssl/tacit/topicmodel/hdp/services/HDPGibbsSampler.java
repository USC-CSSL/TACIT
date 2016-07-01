package edu.usc.cssl.tacit.topicmodel.hdp.services;
/*
 * Copyright 2011 Arnim Bleier, Andreas Niekler and Patrick Jaehnichen
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 */



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.eclipse.core.runtime.IProgressMonitor;

import edu.usc.cssl.tacit.topicmodel.hdp.utils.CLDACorpus;
import edu.usc.cssl.tacit.topicmodel.hdp.utils.TopicsWriter;
import edu.usc.cssl.tacit.topicmodel.hdp.utils.WordAssignmentsWriter;

/**
 * Hierarchical Dirichlet Processes  
 * Chinese Restaurant Franchise Sampler
 * 
 * For more information on the algorithm see:
 * Hierarchical Bayesian Nonparametric Models with Applications. 
 * Y.W. Teh and M.I. Jordan. Bayesian Nonparametrics, 2010. Cambridge University Press.
 * http://www.gatsby.ucl.ac.uk/~ywteh/research/npbayes/TehJor2010a.pdf
 * 
 * For other known implementations see README.txt
 * 
 * @author <a href="mailto:arnim.bleier+hdp@gmail.com">Arnim Bleier</a>
 */
public class HDPGibbsSampler { 


	public double beta  = 0.5; // default only
	public double gamma = 1.5;
	public double alpha = 1.0;
	
	private Random random = new Random();
	private double[] p;
	private double[] f;
	
	protected DOCState[] docStates;
	protected int[] numberOfTablesByTopic;
	protected int[] wordCountByTopic;
	protected int[][] wordCountByTopicAndTerm;
	
	
	protected int sizeOfVocabulary;
	protected int totalNumberOfWords;
	protected int numberOfTopics = 1;
	protected int totalNumberOfTables;
	

	/**
	 * Initially assign the words to tables and topics
	 * 
	 * @param corpus {@link CLDACorpus} on which to fit the model
	 */
	public void addInstances(int[][] documentsInput, int V) {
		sizeOfVocabulary = V;
		totalNumberOfWords = 0;
		docStates = new DOCState[documentsInput.length];
		for (int d = 0; d < documentsInput.length; d++) {
			docStates[d] = new DOCState(documentsInput[d], d);
			totalNumberOfWords += documentsInput[d].length;
		}
		int k, i, j;
		DOCState docState;
		p = new double[20]; 
		f = new double[20];
		numberOfTablesByTopic = new int[numberOfTopics+1];
		wordCountByTopic = new  int[numberOfTopics+1];
		wordCountByTopicAndTerm = new int[numberOfTopics+1][];
		for (k = 0; k <= numberOfTopics; k++) 	// var initialization done
			wordCountByTopicAndTerm[k] = new int[sizeOfVocabulary];
		for (k = 0; k < numberOfTopics; k++) { 
			docState = docStates[k];
			for (i = 0; i < docState.documentLength; i++) 
				addWord(docState.docID, i, 0, k);
		} // all topics have now one document
		for (j = numberOfTopics; j < docStates.length; j++) {
			docState = docStates[j]; 
			k = random.nextInt(numberOfTopics);
			for (i = 0; i < docState.documentLength; i++) 
				addWord(docState.docID, i, 0, k);
		} // the words in the remaining documents are now assigned too
	}

	
	/**
	 * Step one step ahead
	 * 
	 */
	protected void nextGibbsSweep() {
		int table;
		for (int d = 0; d < docStates.length; d++) {
			for (int i = 0; i < docStates[d].documentLength; i++) {
				removeWord(d, i); // remove the word i from the state
				table = sampleTable(d, i);
				if (table == docStates[d].numberOfTables) // new Table
					addWord(d, i, table, sampleTopic()); // sampling its Topic
				else
					addWord(d, i, table, docStates[d].tableToTopic[table]); // existing Table
			}
		}
		defragment();
	}

	
	/**
	 * Decide at which topic the table should be assigned to
	 * 
	 * @return the index of the topic
	 */
	private int sampleTopic() {
		double u, pSum = 0.0;
		int k;
		p = ensureCapacity(p, numberOfTopics);
		for (k = 0; k < numberOfTopics; k++) {
			pSum += numberOfTablesByTopic[k] * f[k];
			p[k] = pSum;
		}
		pSum += gamma / sizeOfVocabulary;
		p[numberOfTopics] = pSum;
		u = random.nextDouble() * pSum;
		for (k = 0; k <= numberOfTopics; k++)
			if (u < p[k])
				break;
		return k;
	}
	

	/**	 
	 * Decide at which table the word should be assigned to
	 * 
	 * @param docID the index of the document of the current word
	 * @param i the index of the current word
	 * @return the index of the table
	 */
	int sampleTable(int docID, int i) {	
		int k, j;
		double pSum = 0.0, vb = sizeOfVocabulary * beta, fNew, u;
		DOCState docState = docStates[docID];
		f = ensureCapacity(f, numberOfTopics);
		p = ensureCapacity(p, docState.numberOfTables);
		fNew = gamma / sizeOfVocabulary;
		for (k = 0; k < numberOfTopics; k++) {
			f[k] = (wordCountByTopicAndTerm[k][docState.words[i].termIndex] + beta) / 
					(wordCountByTopic[k] + vb);
			fNew += numberOfTablesByTopic[k] * f[k];
		}
		for (j = 0; j < docState.numberOfTables; j++) {
			if (docState.wordCountByTable[j] > 0) 
				pSum += docState.wordCountByTable[j] * f[docState.tableToTopic[j]];
			p[j] = pSum;
		}
		pSum += alpha * fNew / (totalNumberOfTables + gamma); // Probability for t = tNew
		p[docState.numberOfTables] = pSum;
		u = random.nextDouble() * pSum;
		for (j = 0; j <= docState.numberOfTables; j++)
			if (u < p[j]) 
				break;	// decided which table the word i is assigned to
		return j;
	}


	/**
	 * Method to call for fitting the model.
	 * 
	 * @param doShuffle
	 * @param shuffleLag
	 * @param maxIter number of iterations to run
	 * @param saveLag save interval 
	 * @param wordAssignmentsWriter {@link WordAssignmentsWriter}
	 * @param topicsWriter {@link TopicsWriter}
	 * @throws IOException 
	 */
	public void run(int shuffleLag, int maxIter, PrintStream log, IProgressMonitor monitor) 
	throws IOException {
		for (int iter = 0; iter < maxIter; iter++) {
			if ((shuffleLag > 0) && (iter > 0) && (iter % shuffleLag == 0))
				doShuffle();
			nextGibbsSweep();
			log.println("iter = " + iter + " #topics = " + numberOfTopics + ", #tables = "
					+ totalNumberOfTables );
			if(iter%50 ==0)
				monitor.worked(1);
		}
	}
		
	
	/**
	 * Removes a word from the bookkeeping
	 * 
	 * @param docID the id of the document the word belongs to 
	 * @param i the index of the word
	 */
	protected void removeWord(int docID, int i){
		DOCState docState = docStates[docID];
		int table = docState.words[i].tableAssignment;
		int k = docState.tableToTopic[table];
		docState.wordCountByTable[table]--; 
		wordCountByTopic[k]--; 		
		wordCountByTopicAndTerm[k][docState.words[i].termIndex] --;
		if (docState.wordCountByTable[table] == 0) { // table is removed
			totalNumberOfTables--; 
			numberOfTablesByTopic[k]--; 
			docState.tableToTopic[table] --; 
		}
	}
	
	
	
	/**
	 * Add a word to the bookkeeping
	 * 
	 * @param docID	docID the id of the document the word belongs to 
	 * @param i the index of the word
	 * @param table the table to which the word is assigned to
	 * @param k the topic to which the word is assigned to
	 */
	protected void addWord(int docID, int i, int table, int k) {
		DOCState docState = docStates[docID];
		docState.words[i].tableAssignment = table; 
		docState.wordCountByTable[table]++; 
		wordCountByTopic[k]++; 
		wordCountByTopicAndTerm[k][docState.words[i].termIndex] ++;
		if (docState.wordCountByTable[table] == 1) { // a new table is created
			docState.numberOfTables++;
			docState.tableToTopic[table] = k;
			totalNumberOfTables++;
			numberOfTablesByTopic[k]++; 
			docState.tableToTopic = ensureCapacity(docState.tableToTopic, docState.numberOfTables);
			docState.wordCountByTable = ensureCapacity(docState.wordCountByTable, docState.numberOfTables);
			if (k == numberOfTopics) { // a new topic is created
				numberOfTopics++; 
				numberOfTablesByTopic = ensureCapacity(numberOfTablesByTopic, numberOfTopics); 
				wordCountByTopic = ensureCapacity(wordCountByTopic, numberOfTopics);
				wordCountByTopicAndTerm = add(wordCountByTopicAndTerm, new int[sizeOfVocabulary], numberOfTopics);
			}
		}
	}

	
	/**
	 * Removes topics from the bookkeeping that have no words assigned to
	 */
	protected void defragment() {
		int[] kOldToKNew = new int[numberOfTopics];
		int k, newNumberOfTopics = 0;
		for (k = 0; k < numberOfTopics; k++) {
			if (wordCountByTopic[k] > 0) {
				kOldToKNew[k] = newNumberOfTopics;
				swap(wordCountByTopic, newNumberOfTopics, k);
				swap(numberOfTablesByTopic, newNumberOfTopics, k);
				swap(wordCountByTopicAndTerm, newNumberOfTopics, k);
				newNumberOfTopics++;
			} 
		}
		numberOfTopics = newNumberOfTopics;
		for (int j = 0; j < docStates.length; j++) 
			docStates[j].defragment(kOldToKNew);
	}
	
	
	/**
	 * Permute the ordering of documents and words in the bookkeeping
	 */
	protected void doShuffle(){
		List<DOCState> h = Arrays.asList(docStates);
		Collections.shuffle(h);
		docStates = h.toArray(new DOCState[h.size()]);
		for (int j = 0; j < docStates.length; j ++){
			List<WordState> h2 = Arrays.asList(docStates[j].words);
			Collections.shuffle(h2);
			docStates[j].words = h2.toArray(new WordState[h2.size()]);
		}
	}
	
	
	
	public static void swap(int[] arr, int arg1, int arg2){
		   int t = arr[arg1]; 
		   arr[arg1] = arr[arg2]; 
		   arr[arg2] = t; 
	}
	
	public static void swap(int[][] arr, int arg1, int arg2) {
		   int[] t = arr[arg1]; 
		   arr[arg1] = arr[arg2]; 
		   arr[arg2] = t; 
	}
	
	public static double[] ensureCapacity(double[] arr, int min){
		int length = arr.length;
		if (min < length)
			return arr;
		double[] arr2 = new double[min*2];
		for (int i = 0; i < length; i++) 
			arr2[i] = arr[i];
		return arr2;
	}

	public static int[] ensureCapacity(int[] arr, int min) {
		int length = arr.length;
		if (min < length)
			return arr;
		int[] arr2 = new int[min*2];
		for (int i = 0; i < length; i++) 
			arr2[i] = arr[i];
		return arr2;
	}

	public static int[][] add(int[][] arr, int[] newElement, int index) {
		int length = arr.length;
		if (length <= index){
			int[][] arr2 = new int[index*2][];
			for (int i = 0; i < length; i++) 
				arr2[i] = arr[i];
			arr = arr2;
		}
		arr[index] = newElement;
		return arr;
	}
	
	

	
	class DOCState {
		
		int docID, documentLength, numberOfTables;
		int[] tableToTopic; 
	    int[] wordCountByTable;
		WordState[] words;

		public DOCState(int[] instance, int docID) {
			this.docID = docID;
		    numberOfTables = 0;  
		    documentLength = instance.length;
		    words = new WordState[documentLength];	
		    wordCountByTable = new int[2];
		    tableToTopic = new int[2];
			for (int position = 0; position < documentLength; position++) 
				words[position] = new WordState(instance[position], -1);
		}


		public void defragment(int[] kOldToKNew) {
		    int[] tOldToTNew = new int[numberOfTables];
		    int t, newNumberOfTables = 0;
		    for (t = 0; t < numberOfTables; t++){
		        if (wordCountByTable[t] > 0){
		            tOldToTNew[t] = newNumberOfTables;
		            tableToTopic[newNumberOfTables] = kOldToKNew[tableToTopic[t]];
		            swap(wordCountByTable, newNumberOfTables, t);
		            newNumberOfTables ++;
		        } else 
		        	tableToTopic[t] = -1;
		    }
		    numberOfTables = newNumberOfTables;
		    for (int i = 0; i < documentLength; i++)
		        words[i].tableAssignment = tOldToTNew[words[i].tableAssignment];
		}

	}
	
	
	class WordState {   
	
		int termIndex;
		int tableAssignment;
		
		public WordState(int wordIndex, int tableAssignment){
			this.termIndex = wordIndex;
			this.tableAssignment = tableAssignment;
		}

	}
	
	
	public void hdp(String input, String output, IProgressMonitor monitor) throws IOException {

		HDPGibbsSampler hdp = new HDPGibbsSampler();
		CLDACorpus corpus = new CLDACorpus(new FileInputStream(input));
		hdp.addInstances(corpus.getDocuments(), corpus.getVocabularySize());

		
		PrintStream file1 = new PrintStream(new File(output+File.separator+"summary.txt"));
		file1.println("sizeOfVocabulary = "+hdp.sizeOfVocabulary);
		file1.println("totalNumberOfWords = "+hdp.totalNumberOfWords);
		file1.println("NumberOfDocs = "+hdp.docStates.length);
		hdp.run(0, 2000, file1, monitor);
		monitor.worked(10);
		file1.close();
		

		PrintStream file = new PrintStream(output+File.separator+"topics.dat");
		for (int k = 0; k < hdp.numberOfTopics; k++) {
			for (int w = 0; w < hdp.sizeOfVocabulary; w++)
				file.format("%05d ",hdp.wordCountByTopicAndTerm[k][w]);
			file.println();
		}
		monitor.worked(10);
		file.close();

		
		file = new PrintStream(output+File.separator+"word-assignments.dat");
		file.println("d w z t");
		int t, docID;
		for (int d = 0; d < hdp.docStates.length; d++) {
			DOCState docState = hdp.docStates[d];
			docID = docState.docID;
			for (int i = 0; i < docState.documentLength; i++) {
				t = docState.words[i].tableAssignment;
				file.println(docID + " " + docState.words[i].termIndex + " " + docState.tableToTopic[t] + " " + t); 
			}
		}
		monitor.worked(10);
		file.close();
		
	}
		
}