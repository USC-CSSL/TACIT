package edu.usc.cssl.tacit.topicmodel.onlinelda.services;

import java.util.*;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

public class Documents {

	private final Vocabulary vocab;
	
	//wordIds[i][j] gives the jth unique token present in document i
    private int[][] wordIds;
    
    //wordCts[i][j] is the number of times that the token given by wordIds[i][j] appears in document i.
    private int[][] wordCts;
    
	public Documents(List<String> docs, Vocabulary vocab, IProgressMonitor monitor) {
        this.vocab = vocab;
        parseDocs(docs, vocab,monitor);
    }
	
	public void parseDocs(List<String> docs, Vocabulary vocab,IProgressMonitor monitor){
	    /*
	    Parse a document into a list of word ids and a list of counts,
	    or parse a set of documents into two lists of lists of word ids
	    and counts.

	    Arguments: 
	    docs:  List of D documents. Each document must be represented as
	           a single string. (Word order is unimportant.) Any
	           words not in the vocabulary will be ignored.
	    vocab: Dictionary mapping from words to integer ids.
	    */
		
		int d = docs.size();
        this.wordIds = new int[d][];
        this.wordCts = new int[d][];
        
        monitor.subTask("Parsing Documents...");
        for(int id=0; id<d; id++){
            String doc = docs.get(id);
            
            doc = doc.toLowerCase()
                    .replaceAll("-", " ")
                    .replaceAll("[^a-z ]", "")
                    .replaceAll(" +", " ");
            
            ArrayList<String> all_words = getTokens(doc);
            Map<Integer,Integer> tokenCounts = new LinkedHashMap<Integer, Integer>();
            
            
            for(String word : all_words){
            	if(vocab.words.contains(word)){
            		
            		int tokenId = vocab.getId(word);
            		if(!tokenCounts.containsKey(tokenId))
            			tokenCounts.put(tokenId, 1);
            		else{
            			int c = tokenCounts.get(tokenId);
            			tokenCounts.put(tokenId, c+1);
            		}
            	}
            }
           // Map<Integer, Integer> results = sortByKey(tokenCounts);
       
            
            wordIds[id] = new int[tokenCounts.size()];
            wordCts[id] = new int[tokenCounts.size()];
            
            int i=0;
            
            //To insert into word dictionaries in sorted order
            SortedSet<Integer> keys = new TreeSet<Integer>(tokenCounts.keySet());
            for (int key : keys) {
               //int value = tokenCounts.get(key);
               wordIds[id][i] = key;
               wordCts[id][i] = tokenCounts.get(key);
               i++;
            }
            
            monitor.worked(1);
            if (monitor.isCanceled()){
            	throw new OperationCanceledException();
            }
        }
        
	}
	
	public int size() {
        return wordCts.length;
    }
	
	public ArrayList<String> getTokens(String d){
		String [] wordList = d.split(" ");
		ArrayList<String> words = new ArrayList<String>();
		
		for(String s:wordList){
			if(!s.contains(" ")){
				//System.out.println(s+":"+s.length());
				words.add(s);
			}
		}
		
		return words;
	}
	
	public String getToken(int i){
        return vocab.getToken(i);
    }
	
	public int getWordCount() {
        int count = 0;
        for(int [] w: wordCts){
            for(int c: w)
                count += c;
        }
        return count;
    }
	
	public int getIndexOf(int id, int[] array){
		int index=0;
		for(int i=0;i<array.length;i++){
			if(array[i] == id)
				index = i;
		}
		return index;
	}
	
	public int[][] getWordIds() {
		return wordIds;
	}

	public int[][] getWordCts() {
		return wordCts;
	}
}
