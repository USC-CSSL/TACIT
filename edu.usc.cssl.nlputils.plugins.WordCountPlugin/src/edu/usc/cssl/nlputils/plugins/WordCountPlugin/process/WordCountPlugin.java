package edu.usc.cssl.nlputils.plugins.WordCountPlugin.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;

import snowballstemmer.PorterStemmer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.usc.cssl.nlputils.utilities.Log;


public class WordCountPlugin {
	private StringBuilder readMe = new StringBuilder();
	private boolean doStopWords;
	private HashSet<String> stopWordSet;
	Map<String, Map<String, Double>> wordMat;
	Set<String> dict;
	SortedSet<String> keys;
	String delimeters = " .,;\"!-()[]{}:?'/\\`~$%#@&*_=+<>";
	PorterStemmer stemmer = new PorterStemmer();
	private boolean doStemming = true;
	private boolean useDict = false;
	
	DecimalFormat df = new DecimalFormat("#.##");
	
	
	private static Logger logger = Logger.getLogger(WordCountPlugin.class.getName());
	public static void main(String[] args) throws IOException {
		WordCountPlugin wc = new WordCountPlugin();
		String dir = "C://Users//carlosg//Desktop//CSSL//svm//testham//";
		File dirList = new File(dir);
		String[] inputFiles = dirList.list();
		
		wc.invokeWordCount(inputFiles,"C://Users//carlosg//Desktop//CSSL//Clusering//seed.txt" , "C://Users//carlosg//Desktop//CSSL//zlab-0.1/stop.txt", "C://Users//carlosg//Desktop//CSSL//Clusering//",  true);

	}

	public int invokeWordCount(String[] inputFiles, String dictionaryFile, String stopWordsFile, String outputFile, boolean doStemming) throws IOException{
		long startTime = System.currentTimeMillis();
		File dFile = null;
		useDict = false;
		doStemming = true;
		wordMat = new HashMap<String, Map<String, Double>>();
		stopWordSet = new HashSet<String>();
		keys = new TreeSet<String>();
		
		
		if (inputFiles==null){
			logger.warning("Please select the input file(s).");
			return -2;
		}
		
		if(!dictionaryFile.isEmpty() && !dictionaryFile.equals("")){
			// Checking the dictionary
			useDict = true;
			dFile = new File(dictionaryFile);
			if (!dFile.exists() || dFile.isDirectory()) {
				logger.warning("Please check the dictionary file path.");
				return -3;
			}
		}
				
		
		// StopWords is optional
		if (stopWordsFile.equals(null) || stopWordsFile.equals(""))
			this.doStopWords=false;
		else{
			this.doStopWords=true;
			File sFile = new File(stopWordsFile);
	
			if (!sFile.exists() || sFile.isDirectory()) {
				logger.warning("Please check the stop words file path.");
				return -4;
			}else {
				startTime = System.currentTimeMillis();
				stopWordSetBuild(stopWordsFile);
				logger.info("Finished building the Stop Words Set in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
				appendLog("Finished building the Stop Words Set in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
			}
		}
		
		// Checking the output path
		File oFile = new File(outputFile+".csv");
		if (outputFile=="" || oFile.isDirectory()) {
			logger.warning("The output file path is incorrect.");
			return -5;
		}
		
		if(useDict){
			dict = new HashSet<String>();
			buildDictionary(dFile);
		}
				
		for (String inputFile: inputFiles) {
			String input = inputFile;
			
			// Mac cache file filtering
			if (inputFile.contains("DS_Store"))
				continue;
			
			Map<String, Double> words = new HashMap<String, Double>();
			File iFile = new File(inputFile);
			if (!iFile.exists() || iFile.isDirectory()){
				logger.warning("Please check the input file path "+inputFile);
				return -2;
			}	
			
			countWords(inputFile, words);
			wordMat.put(input, words);
		}
		
		writeToOutput(outputFile);
		writeReadMe(outputFile);
		return 0;
		
	}
	
	/* Build the dictionary set if present*/
	public void buildDictionary(File dictFile) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(dictFile));
		String currentLine = null;
		String[] words = null;
		while ((currentLine = br.readLine()) != null ) {
			words = currentLine.split(" ");
			for(String word: words)
			{
				if(!word.equals("")){
					word = word.trim().toLowerCase();
					if(doStemming){

						word = word.replace("*", "");
						stemmer.setCurrent(word);
						String stemmedWord = "";
						if(stemmer.stem())
							stemmedWord = stemmer.getCurrent();
						if (!stemmedWord.equals(""))
							word = stemmedWord;

					}
					dict.add(word);
				}
			}

		}
		br.close();
	}
	
	// Builds the Stop Word Set
		public void stopWordSetBuild(String stopWordsFile) throws IOException {
			File sFile = new File(stopWordsFile);
			BufferedReader br = new BufferedReader(new FileReader(sFile));
			String currentLine = null;
			while ((currentLine = br.readLine()) != null ) {
				stopWordSet.add(currentLine.trim().toLowerCase());
			}
			br.close();
		}
		
		/* Does the actual word count for each document */
		public void countWords(String inputFile,  Map<String, Double> words){
			
			int totalWords  = 0;
			
			try {
				String seed = null;
		
				 @SuppressWarnings("unchecked")
				PTBTokenizer ptbt = new PTBTokenizer(new FileReader(inputFile), new CoreLabelTokenFactory(), "");
			      for (CoreLabel label; ptbt.hasNext(); ) {
			    	  	label = (CoreLabel) ptbt.next();
			        	seed = label.toString();
			        	if(seed.isEmpty())
			        		continue;
						if(doStopWords && stopWordSet.contains(seed))
							continue;
						if(delimeters.contains(seed))
							continue;
						if(doStemming){
						
								seed = seed.replace("*", "");
								stemmer.setCurrent(seed);
								String stemmedWord = "";
								if(stemmer.stem())
									 stemmedWord = stemmer.getCurrent();
								if (!stemmedWord.equals(""))
									seed = stemmedWord;
							
						}
						if(useDict && !dict.contains(seed))
								continue;
						totalWords++;
						if (words.containsKey(seed)) {
							words.put(seed, words.get(seed) + 1);
						}else{
							words.put(seed, (double) 1);
							if(!keys.contains(seed))
								keys.add(seed);
						}
						//System.out.println(seed + " " + words.get(seed));
					}

				for(String word: words.keySet()){
					words.put(word, (100*words.get(word))/totalWords);
				}
				
			}catch(Exception e){
				System.out.println("Error processing file " + inputFile + " .Exception " + e);
			}
		}
		
		/* Writes the whole output to a output file */
		public void writeToOutput(String outputPath){
			Map<String, Double> vec = null;
			
			System.out.println(keys.size());
			try {
				FileWriter fw = new FileWriter(new File(outputPath	+ "\\document-to-word-matrix.csv"));
				fw.write("start,");
				for (String key : keys) {
					fw.write(key + ",");
				}
				fw.write("\n");

				for (String files : wordMat.keySet()) {
					fw.write(files + ",");
					vec = wordMat.get(files);
					for (String value : keys) {
						if (vec.containsKey(value)) {
							fw.write(vec.get(value) + ",");
						} else {
							fw.write("0,");
						}
					}
					fw.write("\n");
				}

				fw.close();
			} catch (IOException e) {
				logger.info("Error writing output to files. ");
				appendLog("Error writing output to files. ");
			}
			logger.info("CSV File Updated Successfully. ");
			appendLog("CSV File Updated Successfully. ");
		}

	/*public static boolean calculateCooccurrences(String inputDir,
			String seedFile, int windowSize, String outputPath) {
		String currentLine = null;
		Map<String, Integer> seedWords = new HashMap<String, Integer>();
		Queue<String> q = new LinkedList<String>();
		Map<String, Map<String, Integer>> wordMat = new HashMap<String, Map<String, Integer>>();
		List<String> phrase = new ArrayList<String>();

		try {
			String[] seeds = null;
			BufferedReader br = new BufferedReader(new FileReader(new File(
					seedFile)));
			while ((currentLine = br.readLine()) != null) {
				seeds = currentLine.split(" ");
				for (String seed : seeds) {
					if (!seedWords.containsKey(seed)) {
						
						seedWords.put(seed, 1);
					}
				}

			}
			br.close();
			

			File dir = new File(inputDir);
			File[] listOfFiles = dir.listFiles();

			Map<String, Integer> vec = null;
			int i, len, count;
			String first;
			StringBuilder match;
			int size = seedWords.size();
			for (File f : listOfFiles) {
				List<String> words = new ArrayList<String>();
				br = new BufferedReader(new FileReader(f));
				while ((currentLine = br.readLine()) != null) {
					for (String s : currentLine.split(" "))
						if(!s.isEmpty())
						words.add(s);
				}
				System.out.println(words);
				len = count = 0;
				for (String word : words) {
					if(len<windowSize){
						q.add(word);
						if(seedWords.containsKey(word)){
							if(seedWords.get(word) != 0){
								count++;
								seedWords.put(word, 0);
							}
						}
						len++;
					}
					else{
						if(count ==  size){
							match = new StringBuilder();
							for(String s: q)
								match.append(s+' '); 
							phrase.add(match.toString());
						}
						first = q.remove();
						if(seedWords.containsKey(first)){
							if(seedWords.get(first) == 0){
								count--;
								seedWords.put(first, 1);
							}
						}
						q.add(word);
						if(seedWords.containsKey(word)){
							if(seedWords.get(word) != 0){
								count++;
								seedWords.put(word, 0);
							}
						}
					}
					
					if (wordMat.containsKey(word)) {
						vec = wordMat.get(word);
					} else {
						vec = new HashMap<String, Integer>();
					}
					for (String second : words) {
						if (second == word)
							continue;
						if (vec.containsKey(second)) {
							vec.put(second, vec.get(second) + 1);
						} else {
							vec.put(second, 1);
						}
					}
					wordMat.put(word, vec);
				}

			}

			SortedSet<String> keys = new TreeSet<String>(wordMat.keySet());
			System.out.println(keys.size());
			try {
				FileWriter fw = new FileWriter(new File(outputPath	+ "\\word-to-word-matrix.csv"));
				fw.write("start,");
				for (String key : keys) {
					fw.write(key + ",");
				}
				fw.write("\n\n");

				for (String key : keys) {
					fw.write(key + ",");
					vec = wordMat.get(key);
					for (String value : keys) {
						if (vec.containsKey(value)) {
							fw.write(vec.get(value) + ",");
						} else {
							fw.write("0,");
						}
					}
					fw.write("\n");
				}

				fw.close();
			} catch (IOException e) {
				System.out.println("Error writing output to files" + e);
			}
			
			try {
				FileWriter fw = new FileWriter(new File(outputPath	+ "\\phrases.csv"));
				for(String p:phrase)
					fw.write(p+"\n");
				fw.close();
			} catch (IOException e) {
				System.out.println("Error writing output to files" + e);
			}
			
			System.out.println(phrase.size());
			for(String s:phrase){
				System.out.println(s);
			}

			return true;
		} catch (Exception e) {
			System.out.println("Exception occurred in Cooccurrence Analysis " + e);
		}
		return false;
	}*/
	
	// This function updates the consoleMessage parameter of the context.
		@Inject IEclipseContext context;
		private void appendLog(String message){
			Log.append(context,message);
			readMe.append(message+"\n");
		}
		
		public void writeReadMe(String location){
			File readme = new File(location+"_README.txt");
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(readme));
				String plugV = Platform.getBundle("edu.usc.cssl.nlputils.plugins.WordCountPlugin").getHeaders().get("Bundle-Version");
				String appV = Platform.getBundle("edu.usc.cssl.nlputils.application").getHeaders().get("Bundle-Version");
				Date date = new Date();
				bw.write("Basic Word Count Output\n-----------------------\n\nApplication Version: "+appV+"\nPlugin Version: "+plugV+"\nDate: "+date.toString()+"\n\n");
				bw.write(readMe.toString());
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
}