/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */
package edu.uc.cssl.tacit.wordcount.weighted.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import edu.usc.cssl.tacit.common.TacitUtility;
import edu.usc.cssl.tacit.common.snowballstemmer.PorterStemmer;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class WordCountApi {

	private Trie categorizer = new Trie();
	// private Trie phrazer = new Trie();
	private boolean phraseDetect = false;
	private HashMap<String, HashMap<String, Double>> weightMap = new HashMap<String, HashMap<String, Double>>();
	private HashMap<String, List<Integer>> phraseLookup = new HashMap<String, List<Integer>>();
	private HashMap<String, List<Integer>> conditionalCategory = new HashMap<String, List<Integer>>();
	private TreeMap<Integer, String> categories = new TreeMap<Integer, String>();
	private String delimiters;
	private boolean doLower;
	private boolean doStopWords;
	private int clashWordCount = 2147483647;
	private HashMap<Integer, Integer> oldCategoryMap = new HashMap<Integer, Integer>();
	// private boolean noDictionary = false;
	private HashSet<String> stopWordSet = new HashSet<String>();
	private boolean doLiwcStemming = true;
	private boolean doSpss = true;
	private boolean doWordDistribution = true;
	private boolean doSnowballStemming = true;
	private boolean stemDictionary = false;
	PorterStemmer stemmer = new PorterStemmer();
	private int weirdDashCount = 0;
	private String punctuations = " .,;\"!-()[]{}:?'/\\`~$%#@&*_=+<>";
	// counting numbers - // 5.7, .7 , 5., 567, -25.9, +45
	Pattern pattern = Pattern.compile("^[+-]{0,1}[\\d]*[.]{0,1}[\\d]+[.]{0,1}$");
	// Pattern pattern =
	// Pattern.compile("\\s+[+-]{0,1}[\\d]*[.]{0,1}[\\d]+[.,\\s]+");

	// end of line detection
	Pattern eol = Pattern.compile("\\w+\\s*[.?!]+\\B");

	// compound word detection
	Pattern compoundPattern = Pattern.compile("[\\w\\d]+[\\p{Punct}&&[^-]]*[-]{1}[\\p{Punct}&&[^-]]*[\\w\\d]+");

	Pattern doubleHyphenPattern = Pattern.compile("[\\w\\d]+[\\p{Punct}&&[^-]]*[-]{2}[\\p{Punct}&&[^-]]*[\\w\\d]+");

	// Regular word
	Pattern regularPattern = Pattern.compile("[\\w\\d]+");

	// for calculating punctuation ratios
	int period, comma, colon, semiC, qMark, exclam, dash, quote, apostro, parenth, otherP, allPct;
	private boolean weighted;

	private static Logger logger = Logger.getLogger(WordCountApi.class.getName());

	public WordCountApi(boolean weighted) {
		this.weighted = weighted;
	}

	// Updated function that can handle multiple input files
	public void wordCount(IProgressMonitor monitor, List<File> inputFiles, List<String> dictionaryFile,
			String stopWordsFile, String outputFile, String delimiters, boolean doLower, boolean doLiwcStemming,
			boolean doSnowBallStemming, boolean doSpss, boolean doWordDistribution, boolean stemDictionary, File oFile,
			File sFile, Date dateObj, Map<String, String[]> fileCorpuses) throws IOException {
		if (delimiters == null || delimiters.equals(""))
			this.delimiters = " ";
		else
			this.delimiters = delimiters;
		this.doLower = doLower;
		this.doLiwcStemming = doLiwcStemming;
		this.doSpss = doSpss;
		this.doWordDistribution = doWordDistribution;
		this.doSnowballStemming = doSnowBallStemming;
		this.stemDictionary = stemDictionary;

		if (!this.weighted)
			this.doLiwcStemming = true;
		appendLog("Processing...");

		if (stopWordsFile.equals(null) || stopWordsFile.equals("") || !this.weighted)
			this.doStopWords = false;
		else
			this.doStopWords = true;
		// An error flag to check the error conditions

		// No errors with the output, dictionary and stop-words paths. Start
		// processing.
		long startTime = System.currentTimeMillis();
		monitor.subTask("Counting Words");
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();

		}
		monitor.subTask("Building Category...");
		buildCategorizer(dictionaryFile);
		logger.info("Finished building the dictionary trie in " + (System.currentTimeMillis() - startTime)
				+ " milliseconds.");
		appendLog("Finished building the dictionary trie in " + (System.currentTimeMillis() - startTime)
				+ " milliseconds.");
		monitor.worked(10);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();

		}
		// Create Stop Words Set if doStopWords is true
		if (doStopWords) {
			startTime = System.currentTimeMillis();
			monitor.subTask("Removing Stop words...");
			stopWordSetBuild(new File(stopWordsFile));
			logger.info("Finished building the Stop Words Set in " + (System.currentTimeMillis() - startTime)
					+ " milliseconds.");
			appendLog("Finished building the Stop Words Set in " + (System.currentTimeMillis() - startTime)
					+ " milliseconds.");
		}
		monitor.worked(10);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();

		}
		// Write the titles in the output file.
		monitor.subTask("Writing Header...");
		buildOutputFile(oFile);
		monitor.worked(2);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();

		}
		// Write the SPSS file
		if (doSpss) {
			monitor.subTask("Writing File " + sFile.getName() + "...");
			buildSpssFile(sFile);
		}
		monitor.worked(10);

		if (monitor.isCanceled()) {
			throw new OperationCanceledException();

		}
		// for each inputFile,
		monitor.subTask("Counting Words...");
		for (File inputFile : inputFiles) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();

			}
			
			// Mac cache file filtering
			String absoluteFilePath = inputFile.getAbsolutePath();
			if (absoluteFilePath.contains("DS_Store"))
				continue;
			//monitor.subTask("Counting Words at " + inputFile);
			String corpus = "NIL";
			if (fileCorpuses.containsKey(absoluteFilePath))
				corpus = fileCorpuses.get(absoluteFilePath)[0] + "\\" + fileCorpuses.get(absoluteFilePath)[1];
			countWords(inputFile, oFile, sFile, dateObj, corpus);
			monitor.worked(1);
		}
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();

		}

		monitor.worked(10);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();

		}
		// No errors
		monitor.subTask("Writing Read Me File...");
		if (this.weighted)
			TacitUtility.createRunReport(outputFile, "Weighted Word Count", dateObj);
		else
			TacitUtility.createRunReport(outputFile, "LIWC Word Count", dateObj);

		monitor.worked(5);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();

		}
	}

	public void countWords(File iFile, File oFile, File spssFile, Date dateObj, String corpus) throws IOException {

		if (iFile.isDirectory()) {
			return;
		}
		//logger.info("Current input file - " + iFile.getName());
		//appendLog("Current input file - " + iFile.getAbsolutePath());
		// For calculating Category wise distribution of each word.
		HashMap<String, HashSet<String>> wordCategories = new HashMap<String, HashSet<String>>();

		// Build a hashmap of the words in the input file
		long startTime = System.currentTimeMillis();
		BufferedReader br = new BufferedReader(new FileReader(iFile));
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		String currentLine;
		int totalWords = 0;
		int sixltr = 0;
		int noOfLines = 0;
		int numerals = 0;
		weirdDashCount = 0;
		period = comma = colon = semiC = qMark = exclam = dash = quote = apostro = parenth = otherP = allPct = 0;
		while ((currentLine = br.readLine()) != null) {

			Matcher eolMatcher = eol.matcher(currentLine);
			while (eolMatcher.find())
				noOfLines++;

			period = period + StringUtils.countMatches(currentLine, ".");
			comma = comma + StringUtils.countMatches(currentLine, ",");
			colon = colon + StringUtils.countMatches(currentLine, ":");
			semiC = semiC + StringUtils.countMatches(currentLine, ";");
			qMark = qMark + StringUtils.countMatches(currentLine, "?");
			exclam = exclam + StringUtils.countMatches(currentLine, "!");
			dash = dash + StringUtils.countMatches(currentLine, "-");
			quote = quote + StringUtils.countMatches(currentLine, "\"");
			quote = quote + StringUtils.countMatches(currentLine, "�");
			quote = quote + StringUtils.countMatches(currentLine, "�");
			apostro = apostro + StringUtils.countMatches(currentLine, "'");
			parenth = parenth + StringUtils.countMatches(currentLine, "(");
			parenth = parenth + StringUtils.countMatches(currentLine, ")");
			parenth = parenth + StringUtils.countMatches(currentLine, "{");
			parenth = parenth + StringUtils.countMatches(currentLine, "}");
			parenth = parenth + StringUtils.countMatches(currentLine, "[");
			parenth = parenth + StringUtils.countMatches(currentLine, "]");

			for (char c : "#$%&*+=/\\<>@_^`~|".toCharArray()) {
				otherP = otherP + StringUtils.countMatches(currentLine, String.valueOf(c));
			}

			System.out.println();

			int[] i = process(currentLine, map);
			totalWords = totalWords + i[0];
			sixltr = sixltr + i[1];
			numerals = numerals + i[2];
		}
		allPct = allPct + period + comma + colon + semiC + qMark + exclam + dash + quote + apostro + parenth + otherP;

		br.close();
		//logger.info("Total number of words - " + totalWords);
		//logger.info("Finished building hashmap in " + (System.currentTimeMillis() - startTime) + " milliseconds.");
		//appendLog("Total number of words - " + totalWords);
		//appendLog("Finished building hashmap in " + (System.currentTimeMillis() - startTime) + " milliseconds.");

		// Calculate Category-wise count
		HashMap<String, Double> catCount = new HashMap<String, Double>();
		List<Integer> currCategories;
		int dicCount = 0;
		String currCategoryName = "";
		// Search each input word in the trie prefix tree categorizer
		// (dictionary).
		for (String currWord : map.keySet()) {

			if (currWord == null || currWord.equals(""))
				continue;

			currCategories = categorizer.query(currWord.toLowerCase());

			// If the word is in the trie, update the dictionary words count and
			// the per-category count
			if (currCategories != null) {
				// dicCount = dicCount+1;
				dicCount = dicCount + map.get(currWord); // add the count of the
															// current word. we
															// are not counting
															// unique words
															// here.
				for (int i : currCategories) {
					currCategoryName = categories.get(i);
					// ConsoleView.writeInConsole(currCategoryName+"->"+currWord);
					if (catCount.get(currCategoryName) != null) {
						// catCount.put(currCategoryName,
						// catCount.get(currCategoryName)+1);
						// Add 1 to count the unique words in the category.
						// Add map.get(currWord), i.e, the num of each word to
						// count total number of words in the category
						double currWordWeight = 1.0;
						if (null != this.weightMap.get(currWord)) {
							currWordWeight = this.weightMap.get(currWord).get(currCategoryName);
						}
						if (this.weighted)
							catCount.put(currCategoryName,
									catCount.get(currCategoryName) + (map.get(currWord) * currWordWeight));
						else
							catCount.put(currCategoryName, catCount.get(currCategoryName) + map.get(currWord));
					} else {
						double currWordWeight = 1.0;
						if (null != this.weightMap.get(currWord)) {
							currWordWeight = this.weightMap.get(currWord).get(currCategoryName);
						}

						if (this.weighted)
							catCount.put(currCategoryName, (map.get(currWord) * currWordWeight));
						else
							catCount.put(currCategoryName, (double) map.get(currWord));
					}

					// Populate the Category Set for each Word
					HashSet<String> currWordCategories = wordCategories.get(currWord);
					if (currWordCategories != null) {
						wordCategories.get(currWord).add(currCategoryName);
					} else {
						currWordCategories = new HashSet<String>();
						currWordCategories.add(currCategoryName);
						wordCategories.put(currWord, currWordCategories);
					}

				}
			} else {
				// ConsoleView.writeInConsole("No category -> "+currWord);
			}
		}
		// If Word Distribution output is enabled, calculate the values
		if (doWordDistribution) {

			calculateWordDistribution(map, catCount, wordCategories, iFile.getAbsolutePath(), oFile, dateObj);
		}

		// If there are no punctuation marks, minimum number of lines = 1
		if (noOfLines == 0)
			noOfLines = 1;

		writeToFile(oFile, iFile.getName(), corpus, totalWords, totalWords / (double) noOfLines,
				(sixltr * 100) / (double) totalWords, (dicCount * 100) / (float) totalWords,
				(numerals * 100) / (double) totalWords, catCount);
		if (doSpss)
			writeToSpss(spssFile, iFile.getName(), corpus, totalWords, totalWords / (float) noOfLines,
					(sixltr * 100) / (float) totalWords, (dicCount * 100) / (double) totalWords, catCount);
	}

	public void calculateWordDistribution(HashMap<String, Integer> map, HashMap<String, Double> catCount,
			HashMap<String, HashSet<String>> wordCategories, String inputFile, File oFile, Date dateobj)
					throws IOException {
		File outputDir = oFile.getParentFile();
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");

		// Create output directory for word distributions
		String wordDistributionDir = outputDir + System.getProperty("file.separator");
		if (this.weighted)
			wordDistributionDir = wordDistributionDir + "weighted-word-distribution-" + df.format(dateobj);
		else
			wordDistributionDir = wordDistributionDir + "LIWC-word-distribution-" + df.format(dateobj);
		if (!(new File(wordDistributionDir).exists()))
			new File(wordDistributionDir).mkdir();

		String iFilename = inputFile.substring(inputFile.lastIndexOf(System.getProperty("file.separator")));
		String outputPath = wordDistributionDir + System.getProperty("file.separator") + iFilename
				+ "-wordDistribution.csv";

		File wdFile = new File(outputPath);
		BufferedWriter bw = new BufferedWriter(new FileWriter(wdFile));
		bw.write("Word,Count,");
		StringBuilder toWrite = new StringBuilder();

		for (String currCat : catCount.keySet()) {
			toWrite.append(currCat + ",");
		}
		bw.write(toWrite.toString());
		bw.newLine();

		// check for words in wordCategories instead of map because
		// wordCategories has the words that are present in the dictionary
		for (String currWord : wordCategories.keySet()) {
			StringBuilder row = new StringBuilder();
			int currWC = map.get(currWord);
			row.append(currWord + "," + currWC + ",");

			for (String currCat : catCount.keySet()) {
				// multiplier is 0 if the current word does not belong to the
				// current category
				int multiplier = 0;
				if (wordCategories.get(currWord).contains(currCat))
					multiplier = 100; // 100 instead of 1 because the output
										// should be of the form 25%, not 0.25
				if (multiplier == 0)
					row.append("0,");
				else {
					// Find the root word as that's what's stored in the
					// dictionary and weight map
					String rootWord = categorizer.root(currWord);
					if (this.weighted) {
						row.append((multiplier * map.get(currWord) * weightMap.get(rootWord).get(currCat)
								* weightMap.get(rootWord).get(currCat)) / catCount.get(currCat) + ",");
					} else {
						row.append(multiplier * map.get(currWord) / catCount.get(currCat) + ",");
					}
				}
			}
			bw.write(row.toString());
			bw.newLine();
		}

		bw.close();
	}

	// Builds the Stop Word Set
	public void stopWordSetBuild(File sFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(sFile));
		String currentLine = null;
		while ((currentLine = br.readLine()) != null) {
			stopWordSet.add(currentLine.trim().toLowerCase());
		}
		br.close();
	}

	public void buildOutputFile(File oFile) throws IOException {
		StringBuilder titles = new StringBuilder();
		titles.append("Filename, Parent Corpus, Seg,WC,WPS,Sixltr,Dic,Numerals,");
		for (String title : categories.values()) {
			titles.append(title + ",");
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

	public void buildSpssFile(File spssFile) throws IOException {
		StringBuilder titles = new StringBuilder();
		titles.append("Filename ParentCorpus WC WPS Sixltr Dic ");
		for (String title : categories.values()) {
			titles.append(title + " ");
		}
		FileWriter fw = new FileWriter(spssFile);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(titles.toString());
		bw.newLine();
		bw.close();
		// logger.info("Created the SPSS output File.");
	}

	public void writeToSpss(File spssFile, String docName, String docCorpus, int totalCount, float wps, float sixltr,
			double d, HashMap<String, Double> catCount) throws IOException {
		StringBuilder row = new StringBuilder();
		row.append("\"" + docName + "\"" + " " + "\"" + docCorpus + "\"" + " " + totalCount + " " + wps + " " + sixltr
				+ " " + d + " ");
		double currCatCount = 0;
		// Get the category-wise word count and create the comma-separated row
		// string
		for (String title : categories.values()) {
			if (catCount.get(title) == null)
				currCatCount = 0;
			else
				currCatCount = catCount.get(title);
			row.append(((currCatCount * 100) / totalCount) + " ");
		}
		// Append mode because the titles are already written. Append a row
		// corresponding to each input file
		FileWriter fw = new FileWriter(spssFile, true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(row.toString());
		bw.newLine();
		bw.close();
		ConsoleView.printlInConsole("DAT File Updated Successfully");
	}

	public void writeToFile(File oFile, String docName, String docCorpus, int totalCount, double wps, double d,
			double dic, double numerals, HashMap<String, Double> catCount) throws IOException {
		StringBuilder row = new StringBuilder();
		row.append(docName + "," + docCorpus + ",1," + totalCount + "," + wps + "," + d + "," + dic + "," + numerals
				+ ",");

		double currCatCount = 0;
		// Get the category-wise word count and create the comma-separated row
		// string
		for (String title : categories.values()) {
			if (catCount.get(title) == null)
				currCatCount = 0;
			else
				currCatCount = catCount.get(title);
			row.append((((currCatCount * 100) / totalCount)) + ",");
		}

		// Period, Comma, Colon, SemiC, QMark, Exclam, Dash, Quote, Apostro,
		// Parenth, OtherP, AllPct
		row.append((((period * 100) / (float) totalCount)) + ",");
		row.append((((comma * 100) / (float) totalCount)) + ",");
		row.append((((colon * 100) / (float) totalCount)) + ",");
		row.append((((semiC * 100) / (float) totalCount)) + ",");
		row.append((((qMark * 100) / (float) totalCount)) + ",");
		row.append((((exclam * 100) / (float) totalCount)) + ",");
		// row.append(df.format(((dash*100)/(float)totalCount))+","); correct
		// way
		dash = (dash * 2) - weirdDashCount;
		row.append((((dash * 100) / (float) totalCount)) + ",");
		row.append((((quote * 100) / (float) totalCount)) + ",");
		row.append((((apostro * 100) / (float) totalCount)) + ",");
		row.append((((parenth * 50) / (float) totalCount)) + ","); // multiply
																	// by
																	// 50
																	// =
																	// dividing
																	// by
																	// two.
																	// parantheses
																	// are
																	// counted
																	// as
																	// pairs
		row.append((((otherP * 100) / (float) totalCount)) + ",");
		row.append((((allPct * 100) / (float) totalCount)) + ",");

		// Append mode because the titles are already written. Append a row
		// corresponding to each input file
		FileWriter fw = new FileWriter(oFile, true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(row.toString());
		bw.newLine();
		bw.close();
		//logger.info("CSV File Updated Successfully");
		//appendLog("CSV File Updated Successfully");
	}
	private void addConflictingCategory(int key, String value){
		while(categories.containsKey(clashWordCount)){
				clashWordCount--;
		}
		categories.put(clashWordCount, value);
		oldCategoryMap.put(key, clashWordCount);
		clashWordCount--;
	}
	public void buildCategorizer(List<String> dictFiles) throws IOException {

		for (String dFile : dictFiles) {
			BufferedReader br = new BufferedReader(new FileReader(new File(dFile)));
			String currentLine = br.readLine().trim();
			if (currentLine == null) {
				logger.warning("The dictionary file " + dFile + " is empty");
				appendLog("The dictionary file " + dFile + " is empty");
			}

			if (currentLine.equals("%"))
				while ((currentLine = br.readLine().trim().toLowerCase()) != null && !currentLine.equals("%")) {
					int categoryId = Integer.parseInt(currentLine.split("\\s+")[0].trim());
					String categoryName = currentLine.split("\\s+")[1].trim();
					if (categories.containsKey(categoryId) && !((categories.get(categoryId)).equals(categoryName))) {
						addConflictingCategory(categoryId, categoryName);
					} else {
						categories.put(categoryId, categoryName);
					}
				}

			if (currentLine == null) {
				logger.warning("The dictionary file " + dFile + " does not have categorized words");
				appendLog("The dictionary file " + dFile + " does not have categorized words");
			} else {
				while ((currentLine = br.readLine()) != null) {
					ArrayList<Integer> categories = new ArrayList<Integer>();
					ArrayList<Integer> condCategories = new ArrayList<Integer>();
					HashMap<String, Double> weights = new HashMap<String, Double>();
					currentLine = currentLine.trim().toLowerCase(); // Dictionary
																	// is stored
																	// in
																	// lowercase
																	// in LIWC

					if (currentLine.equals(""))
						continue;
					boolean conditional = false;
					String[] words = currentLine.split("\\s+");
					String currPhrase = words[0];
					String condPhrase = words[0];
					for (int i = initialize(); i < words.length; i = increment(i)) {

						
						if (!words[i].matches("\\d+")) {
							if (words[i].contains("/")) {
								String[] splits = words[i].split("/");
								categories.add(Integer.parseInt(splits[1]));
								if (splits[0].contains(">")) {
									conditional = true;
									condCategories.add(Integer.parseInt(splits[0].split(">")[1]));
									condPhrase = words[0] + " " + splits[0].split(">")[0].replace("<", "");
								}
							} else if (words[i].contains(")") || words[i].contains("("))
								continue;
							else {
								currPhrase = currPhrase + " " + words[i];
								phraseDetect = true;
							}
							continue;
						}
						try {
							if (this.weighted) {
								weights.put(this.categories.get(Integer.parseInt(words[i])),
										Double.parseDouble(words[i + 1]));
							}
						} catch (Exception e) {
							logger.warning("The dictionary file " + dFile + " is not suitable for weighted wordcount");
							appendLog("The dictionary file " + dFile + " is not suitable for weighted wordcount");
						}
						// Add a category to the maps if it was not added
						// earlier
						if(i >0 && oldCategoryMap.containsKey(Integer.parseInt(words[i]))){
							words[i] = oldCategoryMap.get(Integer.parseInt(words[i]))+"";
						}
						categories.add(Integer.parseInt(words[i]));
					}

					String currentWord = words[0];
					if (phraseDetect)
						currentWord = currPhrase;

					if (stemDictionary && !doLiwcStemming) {
						currentWord = currentWord.replace("*", "");
						stemmer.setCurrent(currentWord);
						String stemmedWord = "";
						if (stemmer.stem())
							stemmedWord = stemmer.getCurrent();
						if (!stemmedWord.equals(""))
							currentWord = stemmedWord;
					}

					// ConsoleView.writeInConsole(currentWord);
					// ConsoleView.writeInConsole(words[0]+" "+categories);

					// do Stemming or not. if Stemming is disabled, remove *
					// from the dictionary words
					if (!doLiwcStemming)
						currentWord = currentWord.replace("*", "");
					categorizer.insert(currentWord, categories);

					if (phraseDetect)
						phraseLookup.put(currentWord, categories);

					if (conditional)
						conditionalCategory.put(condPhrase, condCategories);
					// categorizer.printTrie();

					if (this.weighted)
						weightMap.put(currentWord, weights);
				}
			}
			br.close();
		}
	}

	private int initialize() {
		return 1;
		/*
		 * if (this.weighted) return 2; else return 1;
		 */
	}

	private int increment(int val) {
		if (this.weighted) {
			return val + 2;
		} else
			return val + 1;
	}

	// Adds words and their corresponding count to the hashmap. Returns total
	// number of words.
	public int[] process(String line, HashMap<String, Integer> map) {
		int ret[] = new int[3];
		int numWords = 0;
		int sixltr = 0;
		int numerals = 0;
		Matcher matcher = pattern.matcher(line);

		/*
		 * //LIWC checks the numerals before stripping off the hyphens
		 * StringTokenizer tokens = new StringTokenizer(line," ");
		 * while(tokens.hasMoreTokens()){ String currentWord =
		 * tokens.nextToken(); matcher = pattern.matcher(currentWord); while
		 * (matcher.find()){ numerals++; } }
		 */

		// preprocess
		if (doLower)
			line = line.toLowerCase();

		// phrase check
		// increment total words and sixltr words. put the word count in the
		// map.
		// add phrase to lookup and its categories to categorizer
		// phrases ending with *
		// phrase only. single pattern for space or beginning of file?
		// phrases with numerals?
		// use string builder?
		// right now, putting key in the map rather than the match
		if (phraseDetect) {
			for (String key : phraseLookup.keySet()) {
				Pattern p, justp;
				if (key.endsWith("*")) {
					p = Pattern.compile("[\\s\"\\.,()]{1}" + key + "[\\w\\d]*"); // if
																					// key
																					// is
																					// 'string*'
					justp = Pattern.compile(key + "[\\w\\d]*");
					key = key.substring(0, key.length() - 1);
				} else {
					p = Pattern.compile("[\\s\"\\.,()]{1}" + key + "\\b");
					justp = Pattern.compile(key + "\\b");
				}

				int wordsInPhrase = key.split("\\s+").length;

				Matcher m = p.matcher(line);
				ArrayList<Integer> indexes = new ArrayList<Integer>();
				while (m.find()) {
					// ConsoleView.writeInConsole(m.group());
					// ConsoleView.writeInConsole(m.start()+1);
					// ConsoleView.writeInConsole(m.end());
					String match = m.group();
					indexes.add(m.start() + 1);
					indexes.add(m.end());

					numWords = numWords + wordsInPhrase;
					sixltr = sixltr + bigWords(match);

					Object value = map.get(key);
					if (value != null) {
						int i = ((Integer) value).intValue();
						map.put(key, i + wordsInPhrase);
					} else {
						map.put(key, wordsInPhrase);
					}
				}

				for (int i = 0; i < indexes.size(); i = i + 2) {
					// phrases and replacements would be rare. No need for a
					// StringBuilder
					line = line.substring(0, indexes.get(i)) + line.substring(indexes.get(i + 1));
					int diff = indexes.get(i + 1) - indexes.get(i);
					for (int j = 0; j < indexes.size(); j++)
						indexes.set(j, indexes.get(j) - diff);
					// ConsoleView.writeInConsole(line);
				}
				indexes.clear();

				// in case the file just has that one phrase or begins with it
				Matcher m2 = justp.matcher(line);
				while (m2.find()) {
					String match = m2.group();
					indexes.add(m2.start());
					indexes.add(m2.end());

					numWords = numWords + wordsInPhrase;
					sixltr = sixltr + bigWords(match);

					Object value = map.get(key);
					if (value != null) {
						int i = ((Integer) value).intValue();
						map.put(key, i + wordsInPhrase);
					} else {
						map.put(key, wordsInPhrase);
					}
				}

				for (int i = 0; i < indexes.size(); i = i + 2) {
					// phrases and replacements would be rare. No need for a
					// StringBuilder
					line = line.substring(0, indexes.get(i)) + line.substring(indexes.get(i + 1));
					int diff = indexes.get(i + 1) - indexes.get(i);
					for (int j = 0; j < indexes.size(); j++)
						indexes.set(j, indexes.get(j) - diff);
					// ConsoleView.writeInConsole(line);
				}
				// ConsoleView.writeInConsole("Phrase removed - "+line);

			}
		}

		StringTokenizer st = new StringTokenizer(line, delimiters);
		String currentWord = null;
		if (st.hasMoreTokens())
			currentWord = trimChars(st.nextToken(), punctuations);
		do {
			String nextWord = null;
			// take the next word too
			if (st.hasMoreTokens())
				nextWord = trimChars(st.nextToken(), punctuations);
			// String currentWord = st.nextToken();

			if (currentWord == null || currentWord.equals("")) {
				currentWord = nextWord;
				continue;
			}

			// Checking numerals
			matcher = pattern.matcher(currentWord);
			while (matcher.find()) {
				numerals++;
			}

			// Do Porter2/Snowball Stemming if enabled
			if (doSnowballStemming) {
				stemmer.setCurrent(currentWord);
				String stemmedWord = "";
				if (stemmer.stem())
					stemmedWord = stemmer.getCurrent();
				if (!stemmedWord.equals(""))
					currentWord = stemmedWord;
			}

			// If stop word, ignore
			if (doStopWords)
				if (stopWordSet.contains(currentWord)) {
					currentWord = nextWord;
					continue;
				}

			Matcher word = regularPattern.matcher(currentWord);
			if (word.find()) {
				numWords = numWords + 1;
				if (currentWord.length() > 6) {
					sixltr = sixltr + 1;
					// ConsoleView.writeInConsole(currentWord+" "+sixltr);
				}
			}

			/*
			 * numWords = numWords + 1; if (currentWord.length()>6){ sixltr =
			 * sixltr + 1; //ConsoleView.writeInConsole(currentWord+" "+sixltr);
			 * }
			 */

			boolean treatAsOne = true;

			Matcher dh = doubleHyphenPattern.matcher(currentWord);
			// if double quotes, convert to single quotes and treat as a single
			// word in the lookup
			if (dh.find()) {
				currentWord = currentWord.replaceFirst("--", "-").toLowerCase();
				if (categorizer.query(currentWord) != null && !categorizer.checkHyphen(currentWord)) {
					// treat as one word.
					// numWords = numWords; already 1 added above
					// ConsoleView.writeInConsole("Treating as one -
					// "+currentWord);
					Object value = map.get(currentWord);
					if (value != null) {
						int i = ((Integer) value).intValue();
						map.put(currentWord, i + 1);
					} else {
						map.put(currentWord, 1);
					}
					String[] words = currentWord.split("-", 2);
					int hyphened = words.length;
					// boolean allFound = true;

					weirdDashCount = weirdDashCount + hyphened; // twice if two
																// dashes
				} else {
					String[] words = currentWord.split("-", 2);
					int hyphened = words.length - 1;
					// boolean allFound = true;

					if (categorizer.query(currentWord) != null) {
						Object value = map.get(currentWord);
						if (value != null) {
							int i = ((Integer) value).intValue();
							map.put(words[0], i + 1);
						} else {
							map.put(words[0], 1);
						}
						numWords = numWords + hyphened;
						treatAsOne = true;
					} else {
						numWords = numWords + hyphened;
						if (categorizer.query(words[0]) != null) {
							Object value = map.get(words[0]);
							if (value != null) {
								int i = ((Integer) value).intValue();
								map.put(words[0], i + 1);
							} else {
								map.put(words[0], 1);
							}
						}
						treatAsOne = false;
					}
					if (treatAsOne)
						weirdDashCount++;
					else
						weirdDashCount = weirdDashCount + hyphened; // twice if
																	// two
																	// dashes
				}
			} else {
				Matcher cm = compoundPattern.matcher(currentWord);
				if (cm.find()) {
					String[] words = currentWord.split("-");
					int hyphened = -1;
					for (String s : words) {
						if (s == null || s.equals(""))
							continue;
						hyphened++;
					}
					// int hyphens = StringUtils.countMatches(currentWord, "-");
					// -- breaks on double hyphens

					// If the word is not in the dictionary, consider as
					// separate words.
					if (categorizer.query(currentWord.toLowerCase()) == null) {
						numWords = numWords + hyphened; // no need to add +1 as
														// the count was
														// increased by 1 above.
						treatAsOne = false;
					} else
						// Add hyphencount to the weird dash count to subtract
						// from the final value.
						weirdDashCount = weirdDashCount + 1;
				}

				if (treatAsOne) {
					// can use map.containsKey function. But avoiding two calls
					// with the one below.
					Object value = map.get(currentWord);
					if (value != null) {
						int i = ((Integer) value).intValue();
						map.put(currentWord, i + 1);
					} else {
						map.put(currentWord, 1);
					}
				} else {
					// if the compound word doesnt exist in the dictionary,
					// treat as separate words.
					String[] parts = currentWord.split("-");
					for (String part : parts) {
						if (part == null || part.equals(""))
							continue;
						Object value = map.get(part);
						if (value != null) {
							int i = ((Integer) value).intValue();
							map.put(part, i + 1);
						} else {
							map.put(part, 1);
						}
					}
				}
			}
			currentWord = nextWord;
		} while (st.hasMoreTokens() || null != currentWord); // null
																// !=currentWord
																// : Otherwise
																// it will not
																// process the
																// last word

		ret[0] = numWords;
		ret[1] = sixltr;
		ret[2] = numerals;
		return ret;
	}

	private int bigWords(String group) {
		int bigs = 0;
		for (String word : group.split("\\s+"))
			if (word.length() > 6)
				bigs = bigs + 1;
		return bigs;
	}

	private void appendLog(String message) {
		ConsoleView.printlInConsoleln(message);
	}

	public static String trimChars(String source, String trimChars) {
		char[] chars = source.toCharArray();
		int length = chars.length;
		int start = 0;

		while (start < length && trimChars.indexOf(chars[start]) > -1) {
			start++;
		}

		while (start < length && trimChars.indexOf(chars[length - 1]) > -1) {
			length--;
		}

		if (start > 0 || length < chars.length) {
			return source.substring(start, length);
		} else {
			return source;
		}
	}
}
