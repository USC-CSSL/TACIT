package edu.usc.cssl.nlputils.topicmodel.zlda.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

public class DTWC {

	private List<File> documents;
	
	private List<List<Integer>> docVectors;
	private int[][] docVectorsAsInt;
	
	private Map<String, Integer> termIndex;
	private Map<Integer, String> indexTerm;
	private Integer vocabSize;
	private File seedFile;
	private Map<String, Integer> termCount;
	private List<Map<Integer, List<Integer>>> topicSeeds;
	private int[][][] topicSeedsAsInt;
	
	private Map<Integer, Integer> seedWords;

	private SubProgressMonitor monitor;
	
	
	public Integer getVocabSize() {
		return vocabSize;
	}
	
	public Map<Integer, String> getIndexTerm(){
		return indexTerm;
	}

	public File getSeedFile() {
		return seedFile;
	}

	public void setSeedFile(File seedFile) {
		this.seedFile = seedFile;
	}

	public List<Map<Integer, List<Integer>>> getTopicSeeds() {
		return topicSeeds;
	}
	
	public List<List<Integer>> getDocVectors(){
		return docVectors;
	}
	
	public Map<String, Integer> getTermIndex() {
		return termIndex;
	} 
	
	public int[][] getDocVectorsAsInt() {
		return docVectorsAsInt;
	}

	public int[][][] getTopicSeedsAsInt() {
		return topicSeedsAsInt;
	}	
	
	private DTWC(List<File> docs, SubProgressMonitor monitor){
		this.monitor = monitor;
		documents = docs;
		vocabSize = 0;
		docVectors = new ArrayList<List<Integer>>();
		for(int i=0; i<docs.size(); i++){
			docVectors.add(new ArrayList<Integer>());
		}
		termIndex = new HashMap<String, Integer>();
		termCount = new HashMap<String, Integer>();
		initializeSeeds();
		
		indexTerm = new HashMap<Integer, String>();
	}
	
	public DTWC(List<File> docs, File seedFile, SubProgressMonitor monitor){
		this(docs,monitor);
		 
		this.seedFile = seedFile;
	}
	

	
	private void initializeSeeds(){
		monitor.subTask("Initializing Seeds...");
		topicSeeds = new ArrayList<Map<Integer, List<Integer>>>();
		for(int i=0; i<docVectors.size(); i++){
			topicSeeds.add(new HashMap<Integer, List<Integer>>());
		}
		seedWords = new HashMap<Integer, Integer>();
	}
	
	@SuppressWarnings({ "rawtypes"})
	private void calculateTermIndicesAndVectors(){
		
		String word;
		List<Integer> vector;
		for(int i=0; i<documents.size(); i++){
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			vector = docVectors.get(i);
			try {
			
				
				PTBTokenizer ptbtk = new PTBTokenizer(new FileReader(documents.get(i)), 
						new CoreLabelTokenFactory(), "");
				monitor.subTask("Tokenizing words...");
				while(ptbtk.hasNext()){
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					//word = ptbtk.next().toString().toLowerCase();
					word = ptbtk.next().toString();
					word.replaceAll("[^a-zA-Z0-9-_]", " ");
					if(word.matches("^[a-zA-Z0-9]*$")){
						//word = stemmer.stem(word);
						if(termIndex.containsKey(word) == false){
							if (monitor.isCanceled()) {
								throw new OperationCanceledException();
							}
							termIndex.put(word, vocabSize);
							indexTerm.put(vocabSize, word);
							termCount.put(word, 1);
							vector.add(vocabSize);
							vocabSize++;
						}
						else{
							if (monitor.isCanceled()) {
								throw new OperationCanceledException();
							}
							vector.add(termIndex.get(word));
							termCount.put(word, termCount.get(word) + 1);
						}
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
	

	

	

	
	public void computeDocumentVectors(){
		monitor.subTask("Calculating Term Indices and Vectors");
		calculateTermIndicesAndVectors();
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		monitor.worked(15);
		computeZSets();
		monitor.subTask("Converting to Primitive Data Types");
		convertToPrimitiveDataTypes();
		monitor.worked(20);
	}
	
	/* Computing z-sets for these documents */
	

	/* Read the seed words from the seed file */
	private void constructTopicList(){
		
		String line;
		String[] words;
		int topicNo = 0;
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(seedFile));
			while((line = br.readLine()) != null){
				words = line.split(" ");
				for(int i=0; i<words.length; i++){
					if(termIndex.containsKey(words[i])){
						seedWords.put(termIndex.get(words[i]), topicNo);
					}
				}
				topicNo++;
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void computeZSets(){
		monitor.subTask("Constructing Topic list...");
		constructTopicList();
		monitor.worked(15);
		monitor.subTask("Computing Z Sets Topic list...");
		List<Integer> doc;
		Map<Integer, List<Integer>> docTopicSeeds;
		Integer word;
		List<Integer> wordTopicList;
		
 		for(int i=0; i<docVectors.size(); i++){
 			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			doc = docVectors.get(i);
			docTopicSeeds = topicSeeds.get(i);
			for(int j=0; j<doc.size(); j++){
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				word = doc.get((int)j);
				if(termCount.get(indexTerm.get(word)) > 5)
				{
					wordTopicList = docTopicSeeds.get(j);
					if(seedWords.containsKey(word)){
						if (monitor.isCanceled()) {
							throw new OperationCanceledException();
						}
						if(wordTopicList == null){
							wordTopicList = new ArrayList<Integer>();
							docTopicSeeds.put(j, wordTopicList);
						}
						wordTopicList.add(seedWords.get(word));
					}
				}
			}
		}
 		monitor.worked(15);
		
	}
	
	private void convertToPrimitiveDataTypes(){
	
		docVectorsAsInt = new int[docVectors.size()][];
		for(int i=0; i<docVectors.size(); i++){
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			docVectorsAsInt[i] = new int[docVectors.get(i).size()];
			for(int j=0; j<docVectors.get(i).size(); j++){
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				docVectorsAsInt[i][j] = docVectors.get(i).get(j);
			}
 		}
		
		topicSeedsAsInt = new int[topicSeeds.size()][][];
		for(int i=0; i<topicSeeds.size(); i++){
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			topicSeedsAsInt[i] = new int[docVectors.get(i).size()][];
			for(int j=0; j<docVectors.get(i).size(); j++){
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				if(topicSeeds.get(i).get(j) != null){
					topicSeedsAsInt[i][j] = new int[topicSeeds.get(i).get(j).size()];
					for(int k=0; k<topicSeeds.get(i).get(j).size(); k++){
						if (monitor.isCanceled()) {
							throw new OperationCanceledException();
						}
						topicSeedsAsInt[i][j][k] = topicSeeds.get(i).get(j).get(k);
					}
				}
			}
		}
		
	}
	
}
