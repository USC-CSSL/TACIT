package edu.usc.cssl.nlputils.plugins.zlda.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
	
	private DTWC(List<File> docs){
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
	
	public DTWC(List<File> docs, File seedFile){
		this(docs);
		this.seedFile = seedFile; 
	}
	

	
	private void initializeSeeds(){
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
			vector = docVectors.get(i);
			try {
			
				
				PTBTokenizer ptbtk = new PTBTokenizer(new FileReader(documents.get(i)), 
						new CoreLabelTokenFactory(), "");
				
				while(ptbtk.hasNext()){
					//word = ptbtk.next().toString().toLowerCase();
					word = ptbtk.next().toString();
					word.replaceAll("[^a-zA-Z0-9-_]", " ");
					if(word.matches("^[a-zA-Z0-9]*$")){
						//word = stemmer.stem(word);
						if(termIndex.containsKey(word) == false){
							termIndex.put(word, vocabSize);
							indexTerm.put(vocabSize, word);
							termCount.put(word, 1);
							vector.add(vocabSize);
							vocabSize++;
						}
						else{
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
	
	public void writeVectorsToFile(FileWriter fw){
		
		try{
			for(int i=0; i<docVectors.size(); i++){
				for(int j=0; j<docVectors.get(i).size(); j++){
					fw.write(docVectors.get(i).get(j) + " ");
				}
				fw.write("\n");
				fw.flush();;
			}
			fw.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void writeZSetsToFile(FileWriter fw){
		
		Iterator it;
		Map.Entry<Integer, List<Integer>> pair;
		try{
			for(int i=0; i<topicSeeds.size(); i++){
				fw.write("%\n");
				it = topicSeeds.get(i).entrySet().iterator();
				while(it.hasNext()){
					pair = (Map.Entry)it.next(); 
					fw.write(pair.getKey() + " ");
					for(int j=0; j<pair.getValue().size(); j++){
						fw.write(pair.getValue().get(j) + " ");
					}
					fw.write("\n");
				}
				fw.flush();
			}
			fw.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void writeIndexTermToFile(FileWriter fw){
		
		Iterator it;
		Map.Entry<Integer, String> pair;
		try{
			it = indexTerm.entrySet().iterator();
			while(it.hasNext()){
				pair = (Map.Entry)it.next();
				fw.write(pair.getKey() + " " + pair.getValue() + "\n");
			}
			fw.flush();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void computeDocumentVectors(){
		calculateTermIndicesAndVectors();
		computeZSets();
		convertToPrimitiveDataTypes();
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
		
		constructTopicList();
		
		List<Integer> doc;
		Map<Integer, List<Integer>> docTopicSeeds;
		Integer word;
		List<Integer> wordTopicList;
		
 		for(int i=0; i<docVectors.size(); i++){
			doc = docVectors.get(i);
			docTopicSeeds = topicSeeds.get(i);
			for(int j=0; j<doc.size(); j++){
				word = doc.get((int)j);
				if(termCount.get(indexTerm.get(word)) > 5)
				{
					wordTopicList = docTopicSeeds.get(j);
					if(seedWords.containsKey(word)){
						if(wordTopicList == null){
							wordTopicList = new ArrayList<Integer>();
							docTopicSeeds.put(j, wordTopicList);
						}
						wordTopicList.add(seedWords.get(word));
					}
				}
			}
		}
		
	}
	
	private void convertToPrimitiveDataTypes(){
	
		docVectorsAsInt = new int[docVectors.size()][];
		for(int i=0; i<docVectors.size(); i++){
			docVectorsAsInt[i] = new int[docVectors.get(i).size()];
			for(int j=0; j<docVectors.get(i).size(); j++){
				docVectorsAsInt[i][j] = docVectors.get(i).get(j);
			}
 		}
		
		topicSeedsAsInt = new int[topicSeeds.size()][][];
		for(int i=0; i<topicSeeds.size(); i++){
			topicSeedsAsInt[i] = new int[docVectors.get(i).size()][];
			for(int j=0; j<docVectors.get(i).size(); j++){
				if(topicSeeds.get(i).get(j) != null){
					topicSeedsAsInt[i][j] = new int[topicSeeds.get(i).get(j).size()];
					for(int k=0; k<topicSeeds.get(i).get(j).size(); k++){
						topicSeedsAsInt[i][j][k] = topicSeeds.get(i).get(j).get(k);
					}
				}
			}
		}
		
	}
	
}
