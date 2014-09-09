/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */
package edu.usc.pil.nlputils.plugins.wordcount.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.widgets.Shell;

import snowballstemmer.PorterStemmer;
import edu.usc.pil.nlputils.plugins.wordcount.utilities.*;

public class WordCount {
	
	private Trie categorizer = new Trie();
	private TreeMap<Integer,String> categories = new TreeMap<Integer, String>();
	private String delimiters;
	private boolean doLower;
	private boolean doStopWords;
	private HashSet<String> stopWordSet = new HashSet<String>();
	private boolean doLiwcStemming = true;
	private boolean doSpss = true;
	private boolean doWordDistribution = true;
	private boolean doSnowballStemming = true;
	PorterStemmer stemmer = new PorterStemmer();
	
	// counting numbers - // 5.7, .7 , 5., 567, -25.9, +45
	Pattern pattern = Pattern.compile("^[+-]{0,1}[\\d]*[.]{0,1}[\\d]+[.]{0,1}$");
	
	// for calculating punctuation ratios
	int period, comma, colon, semiC, qMark, exclam, dash, quote, apostro, parenth, otherP, allPct;
	
	private static Logger logger = Logger.getLogger(WordCount.class.getName());
	
	// Updated function that can handle multiple input files
	public int wordCount(String[] inputFiles, String dictionaryFile, String stopWordsFile, String outputFile, String delimiters, boolean doLower, boolean doLiwcStemming, boolean doSnowBallStemming, boolean doSpss, boolean doWordDistribution) throws IOException{
		int returnCode = -1;
		if (delimiters==null || delimiters.equals(""))
			this.delimiters=" ";
		else
			this.delimiters = delimiters;
		this.doLower = doLower;
		this.doLiwcStemming = doLiwcStemming;
		this.doSpss = doSpss;
		this.doWordDistribution = doWordDistribution;
		this.doSnowballStemming = doSnowBallStemming;
		
		appendLog("Processing...");
		
		if (stopWordsFile.equals(null) || stopWordsFile.equals(""))
			this.doStopWords=false;
		else
			this.doStopWords=true;
		// An error flag to check the error conditions
		boolean error = false;
		
		if (inputFiles==null){
			logger.warning("Please select the input file(s).");
			error = true;
			return -2;
		}
		
		// Checking the dictionary
		File dFile = new File(dictionaryFile);
		if (!dFile.exists() || dFile.isDirectory()) {
			logger.warning("Please check the dictionary file path.");
			error = true;
			return -3;
		}
		
		// Checking the output path
		File oFile = new File(outputFile+".csv");
		if (outputFile=="" || oFile.isDirectory()) {
			logger.warning("The output file path is incorrect.");
			error = true;
			return -5;
		}
		
		
		// Checking the spss path
		File spssFile = new File(outputFile+".dat");
		if (doSpss) {
				if (outputFile=="" || spssFile.isDirectory()) {
					logger.warning("The SPSS output file path is incorrect.");
					error = true;
					return -6;
				}
		}
				
		
		// StopWords is optional
		File sFile = new File(stopWordsFile);
		if (doStopWords){
			if (!sFile.exists() || sFile.isDirectory()) {
				logger.warning("Please check the stop words file path.");
				error = true;
				return -4;
			}
		}
			
		if(error) {
			return returnCode;
		}
		
		// No errors with the output, dictionary and stop-words paths. Start processing.

		long startTime = System.currentTimeMillis();
		buildCategorizer(dFile);
		logger.info("Finished building the dictionary trie in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
		appendLog("Finished building the dictionary trie in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
		
		// Create Stop Words Set if doStopWords is true
		if (doStopWords){
			startTime = System.currentTimeMillis();
			stopWordSetBuild(sFile);
			logger.info("Finished building the Stop Words Set in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
			appendLog("Finished building the Stop Words Set in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
		}
		
		// Write the titles in the output file.
		buildOutputFile(oFile);
		
		// Write the SPSS file
		if (doSpss)
			buildSpssFile(spssFile);
		
		//categorizer.printTrie();
		//System.out.println(categories);
		//System.out.println(stopWordSet);

		// for each inputFile,
		for (String inputFile: inputFiles) {
			
			File iFile = new File(inputFile);
			if (!iFile.exists() || iFile.isDirectory()){
				logger.warning("Please check the input file path "+inputFile);
				error = true;
				returnCode = -2;
			}
			
			if(error) {
				return returnCode;
			}
			
			System.out.println(inputFile);
			countWords(inputFile, oFile, spssFile);
		}
		
		if (doSpss)
			finalizeSpssFile(spssFile);
		//No errors
		returnCode = 0;
		return returnCode;
	}
	
	
	public void countWords(String inputFile, File oFile, File spssFile) throws IOException{
		File iFile = new File(inputFile);
		logger.info("Current input file - "+inputFile);
		appendLog("Current input file - "+inputFile);
		// For calculating Category wise distribution of each word.
		HashMap<String,HashSet<String>> wordCategories = new HashMap<String, HashSet<String>>();
		
		
		// Build a hashmap of the words in the input file
		long startTime = System.currentTimeMillis();	
		BufferedReader br = new BufferedReader(new FileReader(iFile));
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		String currentLine;
		int totalWords = 0;
		int sixltr = 0;
		int noOfLines = 0;
		int numerals = 0;
		period = comma = colon = semiC = qMark  = exclam = dash = quote = apostro = parenth = otherP = allPct = 0;
		while ((currentLine = br.readLine()) != null) {
			noOfLines = noOfLines + StringUtils.countMatches(currentLine, ". ");
			noOfLines = noOfLines + StringUtils.countMatches(currentLine, "? ");
			noOfLines = noOfLines + StringUtils.countMatches(currentLine, "! ");
			noOfLines = noOfLines + 1; // For the final sentence
			
			period = period + StringUtils.countMatches(currentLine, ".");
			comma = comma + StringUtils.countMatches(currentLine, ",");
			colon = colon + StringUtils.countMatches(currentLine, ":");
			semiC = semiC + StringUtils.countMatches(currentLine, ";");
			qMark = qMark + StringUtils.countMatches(currentLine, "?");
			exclam = exclam + StringUtils.countMatches(currentLine, "!");
			dash = dash + StringUtils.countMatches(currentLine, "-");
			quote = quote + StringUtils.countMatches(currentLine, "\"");
			apostro = apostro + StringUtils.countMatches(currentLine, "'");
			parenth = parenth + StringUtils.countMatches(currentLine, "(");
			parenth = parenth + StringUtils.countMatches(currentLine, ")");
			
			for (char c:"!\"#$%&*+=/\\<>@_^`~|{}[]".toCharArray()){
				otherP = otherP + StringUtils.countMatches(currentLine, String.valueOf(c));
			}
			
			int[] i = process(currentLine, map);
			totalWords = totalWords + i[0];
			sixltr = sixltr + i[1];
			numerals = numerals + i[2];
		}
		allPct = allPct + period + comma + colon + semiC + qMark + exclam + dash + quote + apostro + parenth + otherP;
		
		br.close();
		logger.info("Total number of words - "+totalWords);
		logger.info("Finished building hashmap in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
		appendLog("Total number of words - "+totalWords);
		appendLog("Finished building hashmap in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
		
		// Calculate Category-wise count
		HashMap<String,Integer> catCount = new HashMap<String, Integer>();
		List<Integer> currCategories;
		int dicCount = 0;
		String currCategoryName = "";
		// Search each input word in the trie prefix tree categorizer (dictionary).
		for (String currWord : map.keySet()){
			
			currCategories = categorizer.query(currWord);
			
			// If the word is in the trie, update the dictionary words count and the per-category count
			if (currCategories!=null){
				//dicCount = dicCount+1;
				dicCount = dicCount+map.get(currWord); // add the count of the current word. we are not counting unique words here.
				for (int i : currCategories) {
					currCategoryName = categories.get(i);
					if (catCount.get(currCategoryName)!=null){
						//catCount.put(currCategoryName, catCount.get(currCategoryName)+1);
						// Add 1 to count the unique words in the category. 
						// Add map.get(currWord), i.e, the num of each word to count total number of words in the category
						catCount.put(currCategoryName, catCount.get(currCategoryName)+map.get(currWord));
					} else {
						catCount.put(currCategoryName, map.get(currWord));
					}
					
					// Populate the Category Set for each Word
					HashSet<String> currWordCategories = wordCategories.get(currWord);
					if (currWordCategories!=null){
						wordCategories.get(currWord).add(currCategoryName);
					} else {
						currWordCategories = new HashSet<String>();
						currWordCategories.add(currCategoryName);
						wordCategories.put(currWord, currWordCategories);
					}

				}
			}
		}
		// If Word Distribution output is enabled, calculate the values
		if (doWordDistribution)
			calculateWordDistribution(map, catCount, wordCategories, inputFile,oFile);
			
		writeToFile(oFile, iFile.getName(), totalWords, totalWords/(float)noOfLines, (sixltr*100)/(float)totalWords, (dicCount*100)/(float)totalWords, (numerals*100)/(float)totalWords, catCount);
		if (doSpss)
			writeToSpss(spssFile, iFile.getName(), totalWords, totalWords/(float)noOfLines, (sixltr*100)/(float)totalWords, (dicCount*100)/(float)totalWords, catCount);
	}
	
	public void calculateWordDistribution(HashMap<String,Integer> map, HashMap<String,Integer> catCount, HashMap<String,HashSet<String>> wordCategories, String inputFile, File oFile) throws IOException{
		File outputDir = oFile.getParentFile();
		String iFilename = inputFile.substring(inputFile.lastIndexOf(System.getProperty("file.separator")));
		File wdFile = new File(outputDir.getAbsolutePath()+System.getProperty("file.separator")+iFilename+"_wordDistribution.csv");
		BufferedWriter bw = new BufferedWriter(new FileWriter(wdFile));
		bw.write("Word,Count,");
		StringBuilder toWrite = new StringBuilder();
		
		for (String currCat : catCount.keySet()){
			toWrite.append(currCat+",");
		}
		bw.write(toWrite.toString());
		bw.newLine();
		
		// check for words in wordCategories instead of map because wordCategories has the words that are present in the dictionary
		for(String currWord : wordCategories.keySet()){
			StringBuilder row = new StringBuilder();
			int currWC = map.get(currWord);
			row.append(currWord+","+currWC+",");
			
			for (String currCat : catCount.keySet()){
				// multiplier is 0 if the current word does not belong to the current category
				int multiplier = 0;
				if (wordCategories.get(currWord).contains(currCat)){
					multiplier = 100;	// 100 instead of 1 because the output should be of the form 25%, not 0.25
				}
				row.append( (multiplier * map.get(currWord)) / (float)catCount.get(currCat) +",");
			}
			bw.write(row.toString());
			bw.newLine();
		}
		
		bw.close();
	}
	
	
	// Legacy single-inputfile function
	public int oldWordCount(String inputFile, String dictionaryFile, String stopWordsFile, String outputFile, String delimiters, boolean doLower) throws IOException{
		int returnCode = -1;
		this.delimiters = delimiters;
		this.doLower = doLower;
		File iFile = new File(inputFile);
		File dFile = new File(dictionaryFile);
		File sFile = new File(stopWordsFile);
		File oFile = new File(outputFile);

		boolean error = false;
		if (!iFile.exists() || iFile.isDirectory()) {
			logger.warning("Please check the input file path.");
			error = true;
			returnCode = -2;
		} else if (!dFile.exists() || dFile.isDirectory()) {
			logger.warning("Please check the dictionary file path.");
			error = true;
			returnCode = -3;
		} else if (!sFile.exists() || sFile.isDirectory()) {
			logger.warning("Please check the stop words file path.");
			error = true;
			returnCode = -4;
		} else if (outputFile=="" || oFile.exists() || oFile.isDirectory()) {
			logger.warning("The output file path is incorrect or the file already exists.");
			error = true;
			returnCode = -5;
		} 
		// All the files exist. Start analysis.
		if (!error) {
			long startTime = System.currentTimeMillis();	
			BufferedReader br = new BufferedReader(new FileReader(iFile));
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			String currentLine;
			Integer totalWords = new Integer(0);
			while ((currentLine = br.readLine()) != null) {
				//System.out.println(currentLine);
				int[] i = process(currentLine, map);
				totalWords = totalWords + i[0];
				//System.out.println(map);
			}
			br.close();
			//System.out.println(map);
			logger.info("Total number of words - "+totalWords);
			logger.info("Finished building hashmap in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
			startTime = System.currentTimeMillis();
			buildCategorizer(dFile);
			stopWordSetBuild(sFile);
			//System.out.println(categories);
			//System.out.println(categorizer);
			logger.info("Finished building the dictionary trie in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
			//categorizer.printTrie();
			/*
			Test Data
			 
			System.out.println(categorizer.query("pizza"));
			System.out.println(categorizer.query("pizzahut"));
			System.out.println(categorizer.query("piz"));
			System.out.println(categorizer.query("zero"));
			System.out.println(categorizer.query("yielding"));
			System.out.println(categorizer.query("abhorrence"));
			System.out.println(categorizer.query("determined"));
			System.out.println(categorizer.query("fellow"));
			System.out.println(categorizer.query("january"));
			System.out.println(categorizer.query("maps"));
			System.out.println(categorizer.query("nutrition"));
			System.out.println(categorizer.query("orchestra"));
			System.out.println(categorizer.query("perception"));
			System.out.println(categorizer.query("abomination"));
			System.out.println(categorizer.query("abomin"));
			System.out.println(categorizer.query("abominat"));
			System.out.println(categorizer.query("abominat*"));
			System.out.println(categorizer.query("accept"));
			System.out.println(categorizer.query("acceptr"));
			System.out.println(categorizer.query("acceptra"));
			System.out.println(categorizer.query("accep"));
			System.out.println(categorizer.query("accept*"));
			*/
			HashMap<String,Integer> catCount = new HashMap<String, Integer>();
			List<Integer> currCategories;
			int dicCount = 0;
			String currCategoryName = "";
			
			for (String currWord : map.keySet()){
				currCategories = categorizer.query(currWord);
				if (currCategories!=null){
					//dicCount = dicCount+1;
					dicCount = dicCount+map.get(currWord); // add the count of the current word. we are not counting unique words here.
					for (int i : currCategories) {
						currCategoryName = categories.get(i);
						if (catCount.get(currCategoryName)!=null){
							catCount.put(currCategoryName, catCount.get(currCategoryName)+1);
						} else {
							catCount.put(currCategoryName, 1);
						}
					}
				}
				//System.out.println(currWord);
			}
			writeToFile(oFile, iFile.getName(), totalWords, 0, 0, dicCount, 0, catCount);
			returnCode = 0;
		}
		return returnCode;
	}
	
	// Builds the Stop Word Set
	public void stopWordSetBuild(File sFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(sFile));
		String currentLine = null;
		while ((currentLine = br.readLine()) != null ) {
			stopWordSet.add(currentLine.trim().toLowerCase());
		}
		br.close();
	}
	
	public void buildOutputFile(File oFile) throws IOException{
		StringBuilder titles = new StringBuilder();
		titles.append("Filename,WC,WPS,Sixltr,Dic,Numerals,");
		for (String title : categories.values()){
			titles.append(title+",");
		}
		titles.append("Period, Comma, Colon, SemiC, QMark, Exclam, Dash, Quote, Apostro, Parenth, OtherP, AllPct");
		FileWriter fw = new FileWriter(oFile);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(titles.toString());
		bw.newLine();
		bw.close();
		logger.info("Building the output File.");
		appendLog("Building the output File.");
	}
	
	public void buildSpssFile(File spssFile) throws IOException{
		StringBuilder titles = new StringBuilder();
		titles.append("DATA LIST LIST\n/ Filename A(40) WC WPS Sixltr Dic ");
		for (String title : categories.values()){
			titles.append(title+" ");
		}
		titles.append(".\nBEGIN DATA.");
		FileWriter fw = new FileWriter(spssFile);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(titles.toString());
		bw.newLine();
		bw.close();
		//logger.info("Created the SPSS output File.");
	}
	
	public void finalizeSpssFile(File spssFile) throws IOException{
		String end = "END DATA.\n\nLIST.";
		FileWriter fw = new FileWriter(spssFile, true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(end);
		bw.newLine();
		bw.close();
		logger.info("Created the SPSS output File.");
		appendLog("Created the SPSS output File.");
	}

	public void writeToSpss(File spssFile, String docName, int totalCount, float wps, float sixltr, float dic, HashMap<String,Integer> catCount) throws IOException{
		StringBuilder row = new StringBuilder();
		row.append("\""+docName+"\""+" "+totalCount+" "+wps+" "+sixltr+" "+dic+" ");
		int currCatCount = 0;
		// Get the category-wise word count and create the comma-separated row string 
		for (String title : categories.values()){
			if (catCount.get(title) == null)
				currCatCount = 0;
			else
				currCatCount = catCount.get(title);
			row.append(((currCatCount*100)/(float)totalCount)+" ");
		}
		// Append mode because the titles are already written. Append a row corresponding to each input file
		FileWriter fw = new FileWriter(spssFile, true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(row.toString());
		bw.newLine();
		bw.close();
		logger.info("SPSS File Updated Successfully");
	}

	public void writeToFile(File oFile, String docName, int totalCount, float wps, float sixltr, float dic, float numerals, HashMap<String,Integer> catCount) throws IOException{
		StringBuilder row = new StringBuilder();
		row.append(docName+","+totalCount+","+wps+","+sixltr+","+dic+","+numerals+",");
		
		int currCatCount = 0;
		// Get the category-wise word count and create the comma-separated row string 
		for (String title : categories.values()){
			if (catCount.get(title) == null)
				currCatCount = 0;
			else
				currCatCount = catCount.get(title);
			row.append(((currCatCount*100)/(float)totalCount)+",");
		}
		
		//Period, Comma, Colon, SemiC, QMark, Exclam, Dash, Quote, Apostro, Parenth, OtherP, AllPct
		row.append(((period*100)/(float)totalCount)+",");
		row.append(((comma*100)/(float)totalCount)+",");
		row.append(((colon*100)/(float)totalCount)+",");
		row.append(((semiC*100)/(float)totalCount)+",");
		row.append(((qMark*100)/(float)totalCount)+",");
		row.append(((exclam*100)/(float)totalCount)+",");
		row.append(((dash*100)/(float)totalCount)+",");
		row.append(((quote*100)/(float)totalCount)+",");
		row.append(((apostro*100)/(float)totalCount)+",");
		row.append(((parenth*100)/(float)totalCount)+",");
		row.append(((otherP*100)/(float)totalCount)+",");
		row.append(((allPct*100)/(float)totalCount)+",");
		
		// Append mode because the titles are already written. Append a row corresponding to each input file
		FileWriter fw = new FileWriter(oFile, true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(row.toString());
		bw.newLine();
		bw.close();
		logger.info("CSV File Updated Successfully");
		appendLog("CSV File Updated Successfully");
	}

	public void buildCategorizer(File dFile) throws IOException {
		BufferedReader br= new BufferedReader(new FileReader(dFile));
		String currentLine=br.readLine().trim();
		if (currentLine == null) {
			logger.warning("The dictionary file is empty");
			appendLog("The dictionary file is empty");
		}
		if (currentLine.equals("%"))
			while ((currentLine=br.readLine().trim()) != null && !currentLine.equals("%"))
				categories.put(Integer.parseInt(currentLine.split("\\s+")[0].trim()), currentLine.split("\\s+")[1].trim());
		
		if (currentLine == null){
			logger.warning("The dictionary file does not have categorized words");
			appendLog("The dictionary file does not have categorized words");
		} else {
			while ((currentLine=br.readLine())!=null) {
				ArrayList<Integer> categories = new ArrayList<Integer>();
				currentLine = currentLine.trim();
				
				if (currentLine.equals(""))
					continue;
				String[] words = currentLine.split("\\s+");
				for (int i=1; i<words.length; i++){
					categories.add(Integer.parseInt(words[i]));
				}
				//System.out.println(words[0]+" "+categories);
				// do Stemming or not. if Stemming is disabled, remove * from the dictionary words
				if (doLiwcStemming)
					categorizer.insert(words[0], categories);
				else
					categorizer.insert(words[0].replace("*", ""), categories);
				//categorizer.printTrie();
			}
		}
		br.close();
	}
	
	// Adds words and their corresponding count to the hashmap. Returns total number of words.
	public int[] process(String line, HashMap<String, Integer> map) {
		int ret[] = new int[3];
		int numWords = 0;
		int sixltr = 0;
		int numerals = 0;
		Matcher matcher;
		//preprocess
		if (doLower)
			line = line.toLowerCase();
		StringTokenizer st = new StringTokenizer(line,delimiters);
		
		while (st.hasMoreTokens()){
			String currentWord = st.nextToken();
			
			matcher = pattern.matcher(currentWord);
			while (matcher.find()){
				numerals++;
				//System.out.println(currentWord);
			}
			
			//Do Porter2/Snowball Stemming if enabled
			if (doSnowballStemming){
				stemmer.setCurrent(currentWord);
				String stemmedWord = "";
				if(stemmer.stem())
					 stemmedWord = stemmer.getCurrent();
				if (!stemmedWord.equals(""))
					currentWord = stemmedWord;
			}
			
			// If stop word, ignore
			if (doStopWords)
				if (stopWordSet.contains(currentWord))
					continue;
			if (currentWord.length()>6){
				sixltr = sixltr + 1;
			}
			numWords = numWords + 1;
			// can use map.containsKey function. But avoiding two calls with the one below.
			Object value = map.get(currentWord);
			if (value!=null) {
				int i = (int) value;
				map.put(currentWord, i+1);
			} else {
				map.put(currentWord, 1);
			}
		}
		ret[0] = numWords;
		ret[1] = sixltr;
		ret[2] = numerals;
		return ret;
	}
	
	
	// This function updates the consoleMessage parameter of the context.
	@Inject IEclipseContext context;
	private void appendLog(String message){
		IEclipseContext parent = null;
		if (context==null)
			return;
		parent = context.getParent();
		parent.set("consoleMessage", message);
	}

}
