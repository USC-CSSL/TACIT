package edu.usc.cssl.tacit.wordcount.standard.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;

import edu.usc.cssl.tacit.common.TacitUtility;
import edu.usc.cssl.tacit.common.snowballstemmer.PorterStemmer;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import edu.usc.cssl.tacit.wordcount.standard.Activator;

public class WordCountPlugin {

	private SentenceModel sentenceModel;
	private TokenizerModel tokenizerModel;
	private POSModel posModel;
	private SentenceDetectorME sentDetector;
	private TokenizerME tokenize;
	private POSTaggerME posTagger;

	protected boolean weighted;
	private boolean stemDictionary;
	private boolean doPennCounts;
	private boolean doWordDistribution;
	private boolean createDATFile;
	private boolean createPOSTags;
	protected Date dateObj;
	protected String outputPath;
	protected String wordDistributionDir;
	protected String posTagsDir;

	PorterStemmer stemmer = new PorterStemmer();

	// wordDictionary<word,<category,weight>>
	private HashMap<String, HashMap<Integer, Double>> wordDictionary = new HashMap<String, HashMap<Integer, Double>>();
	// counts<words,<category,current weight>>
	// File count to maintain word counts for an individual file
	// Overall count to maintain word counts for all the files
	// User maps have integer keys because we read them from dictionary
	// Penn maps use the Penn treebank POS tags as keys
	private HashMap<String, HashMap<Integer, Double>> userFileCount = new HashMap<String, HashMap<Integer, Double>>();
	private HashMap<String, HashMap<Integer, Double>> userOverallCount = new HashMap<String, HashMap<Integer, Double>>();
	private HashMap<String, HashMap<String, Double>> pennFileCount = new HashMap<String, HashMap<String, Double>>();
	private HashMap<String, HashMap<String, Double>> pennOverallCount = new HashMap<String, HashMap<String, Double>>();
	private HashMap<Integer, String> categoryID = new HashMap<Integer, String>();

	private BufferedWriter resultCSVbw = null;
	private BufferedWriter pennCSVbw = null;
	private BufferedWriter datbw = null;

	private HashMap<Integer, Integer> oldCategoryMap = new HashMap<Integer, Integer>();
	private int clashWordCount = 2147483647;
	// Variables to compute the overall counts
	private int numWords = 0;
	private int numDictWords = 0;
	private int numSentences = 0;
	private IProgressMonitor monitor;

	public WordCountPlugin(boolean weighted, Date dateObj,
			boolean stemDictionary, boolean doPennCounts,
			boolean doWordDistribution, boolean createDATFile,
			boolean createPOSTags, String outputPath, IProgressMonitor monitor) {
		this.weighted = weighted;
		this.dateObj = dateObj;
		this.stemDictionary = stemDictionary;
		this.doPennCounts = doPennCounts;
		this.doWordDistribution = doWordDistribution;
		this.createDATFile = createDATFile;
		this.outputPath = outputPath;
		this.monitor = monitor;
		this.createPOSTags = createPOSTags;

		// Create folder for word distribution files
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
		if (doWordDistribution) {
			createWordDistributionDir(df);
		}

		if (createPOSTags) {
			createPosTagsDir(df);
		}
	}
	protected void createPosTagsDir(DateFormat df){
		posTagsDir = outputPath + System.getProperty("file.separator")
		+ "TACIT-POS-Tags-" + df.format(dateObj);
		new File(posTagsDir).mkdir();
	}
	protected BufferedWriter createPosTagsFile(String inputFile) throws IOException{
		return new BufferedWriter(new FileWriter(posTagsDir
				+ System.getProperty("file.separator")
				+ inputFile.substring(inputFile.lastIndexOf(System
						.getProperty("file.separator")) + 1)));
	}
	protected void createWordDistributionDir(DateFormat df){
		wordDistributionDir = outputPath
				+ System.getProperty("file.separator")
				+ "TACIT-word-distribution-" + df.format(dateObj);
	}
	public void countWords(List<String> inputFiles, List<String> dictionaryFiles) {

		ConsoleView.printlInConsoleln("Loading models.");
		if (!setModels())
			return;

		try {
			ConsoleView.printlInConsoleln("Bulding dictionary.");
			buildMaps(dictionaryFiles);
		} catch (IOException e) {
			ConsoleView.printlInConsoleln("Error parsing dictionary.");
			e.printStackTrace();
			return;
		}

		ConsoleView.printlInConsoleln("Counting Words.");
		monitor.subTask("Counting Words");
		if (monitor.isCanceled())
			throw new OperationCanceledException();

		for (String iFile : inputFiles) {
			if (monitor.isCanceled())
				throw new OperationCanceledException();
			do_countWords(iFile);
			if (doWordDistribution)
				createWordDistribution(iFile);
			monitor.worked(2);
			refreshFileCounts();
		}

		ConsoleView.printlInConsoleln("Writing Results.");
		monitor.subTask("Computing Overall Results");
		// Add overall counts to csv output
		addToCSV("Overall Counts", this.numWords, this.numSentences,
				this.numDictWords, true);
		monitor.worked(5);
		closeWriters();

		generateRunReport();
		monitor.worked(1);
		return;
	}
	protected void generateRunReport(){
		if (weighted)
			TacitUtility.createRunReport(outputPath,
					"TACIT Weighted Word Count", dateObj,null);
		else
			TacitUtility.createRunReport(outputPath,
					"TACIT Standard Word Count", dateObj,null);
	}
	
	protected BufferedWriter createWordDistributionFile(String inputFile) throws IOException{
		return new BufferedWriter(new FileWriter(new File(
				wordDistributionDir
				+ System.getProperty("file.separator")
				+ inputFile.substring(inputFile.lastIndexOf(System
						.getProperty("file.separator")) + 1)
				+ "_word_distribution.csv")));
		
		
	}
	/**
	 * Generate word distribution file for the given input file. Make sure to
	 * call this before refreshing the file counts.
	 * 
	 * @param inputFile
	 */
	private void createWordDistribution(String inputFile) {

		try {

			if (!(new File(wordDistributionDir).exists())) {
				new File(wordDistributionDir).mkdir();
			}

			BufferedWriter bw = createWordDistributionFile(inputFile);

			List<Integer> keyList = new ArrayList<Integer>();
			keyList.addAll(categoryID.keySet());
			Collections.sort(keyList);

			HashMap<Integer, Double> categoryCount = new HashMap<Integer, Double>();

			for (Integer category : keyList) {
				categoryCount.put(category, 0.0);
			}

			// Add category headers
			StringBuilder toWrite = new StringBuilder();
			toWrite.append("Word,Count,");
			for (int i = 0; i < keyList.size(); i++) {
				toWrite.append(categoryID.get(keyList.get(i)) + ",");
			}
			bw.write(toWrite.toString());
			bw.newLine();

			List<String> dictWords = new ArrayList<String>();
			dictWords.addAll(userFileCount.keySet());

			// Find the overall weight of all the categories
			for (String word : dictWords) {
				List<Integer> wordCats = new ArrayList<Integer>();
				wordCats.addAll(userFileCount.get(word).keySet());

				for (Integer cat : wordCats) {
					categoryCount.put(cat, categoryCount.get(cat)
							+ userFileCount.get(word).get(cat));
				}
			}

			for (String word : dictWords) {
				toWrite = new StringBuilder();
				toWrite.append(word + ",");

				List<Integer> wordCats = new ArrayList<Integer>();
				wordCats.addAll(userFileCount.get(word).keySet());

				// All the complex logic below is to find the count of
				// individual word.
				// Accept it that the code works unless you change the data
				// structures
				// for storing the counts - Anurag Singh (7/17/2015)
				if (wordCats.isEmpty())
					toWrite.append("0,");
				else {
					int wordCount = 0;
					for (int category : wordCats) {
						if (userFileCount.get(word).get(category) != 0.0) {
							wordCount = (int) (userFileCount.get(word).get(
									wordCats.get(0)) / wordDictionary.get(word)
									.get(wordCats.get(0)));
							break;
						}
					}
					toWrite.append(wordCount + ",");
				}

				for (Integer cat : keyList) {
					if (userFileCount.get(word).containsKey(cat)) {
						double catContribution = 100
								* userFileCount.get(word).get(cat)
								/ categoryCount.get(cat);
						if (catContribution != 0.0)
							toWrite.append(catContribution + ",");
						else
							toWrite.append("0,");
					} else {
						toWrite.append("0,");
					}
				}

				bw.write(toWrite.toString());
				bw.newLine();
			}

			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Close the BufferedWriters for result files
	 */
	private void closeWriters() {
		try {
			if (resultCSVbw != null)
				resultCSVbw.close();

			if (pennCSVbw != null)
				pennCSVbw.close();

			if (datbw != null)
				datbw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		monitor.worked(1);
	}

	/**
	 * Count the words for the given file and adds a line in the result CSV
	 * corresponding to the file
	 * 
	 * @param inputFile
	 *            Path of input text file
	 */
	private void do_countWords(String inputFile) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(
					inputFile)));
			String currentLine;
			int numWords = 0;
			int numDictWords = 0;
			int numSentences = 0;

			BufferedWriter posBW = null;

			if (createPOSTags) {
				posBW = createPosTagsFile(inputFile);
			}

			while ((currentLine = br.readLine()) != null) {
				String[] sentences = sentDetector.sentDetect(currentLine);
				numSentences = numSentences + sentences.length;
				this.numSentences = this.numSentences + sentences.length;
				StringBuilder toWrite = new StringBuilder();

				for (int i = 0; i < sentences.length; i++) {
					String[] words = tokenize.tokenize(sentences[i]);
					String[] posTags = posTagger.tag(words);

					if (createPOSTags) {
						for (int k = 0; k < words.length; k++) {
							toWrite.append(words[k] + "/" + posTags[k] + " ");
						}
					}
					numWords = numWords + words.length;
					this.numWords = this.numWords + words.length;

					for (int j = 0; j < words.length; j++) {
						if (wordDictionary.containsKey(words[j])) {
							numDictWords++;
							this.numDictWords++;

							// Increment count for all user defined categories
							Set<Integer> wordCategories = wordDictionary.get(
									words[j]).keySet();
							for (Integer cat : wordCategories) {
								double wordWeight = wordDictionary
										.get(words[j]).get(cat);

								userFileCount.get(words[j]).put(
										cat,
										userFileCount.get(words[j]).get(cat)
												+ wordWeight);
								userOverallCount.get(words[j]).put(
										cat,
										userOverallCount.get(words[j]).get(cat)
												+ wordWeight);
							}

							// Increment count of Penn Treebank POS tags
							if (pennFileCount.get(words[j]).containsKey(
									posTags[j])) {
								pennFileCount.get(words[j]).put(
										posTags[j],
										pennFileCount.get(words[j]).get(
												posTags[j]) + 1);
							} else {
								pennFileCount.get(words[j])
										.put(posTags[j], 1.0);
							}
							if (pennOverallCount.get(words[j]).containsKey(
									posTags[j])) {
								pennOverallCount.get(words[j]).put(
										posTags[j],
										pennOverallCount.get(words[j]).get(
												posTags[j]) + 1);
							} else {
								pennOverallCount.get(words[j]).put(posTags[j],
										1.0);
							}
						}
					}

				}
				if (createPOSTags) {
					posBW.write(toWrite.toString());
					posBW.newLine();
				}
			}
			if (createPOSTags) {
				posBW.close();
			}
			br.close();
			monitor.worked(4);
			addToCSV(inputFile, numWords, numSentences, numDictWords, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	protected String createFileName(){
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
		String type = "";
		if (weighted)
			type = "TACIT-Weighted-Wordcount-UserTags-";
		else
			type = "TACIT-Standard-Wordcount-UserTags-";
		String filePath =  type
				+ df.format(dateObj) + ".csv";
		return filePath;
	}
	protected String createDATFilePath(){
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
		String type = "";
		if (weighted)
			type = "TACIT-Weighted-Wordcount-UserTags-";
		else
			type = "TACIT-Standard-Wordcount-UserTags-";		
		return outputPath + System.getProperty("file.separator") + type + df.format(dateObj) + ".dat";
		
	}
	/**
	 * Add a line to CSV with the counts for the given input file
	 * 
	 * @param inputFile
	 *            File foe word counts
	 * @param numWords
	 *            Total number of words in input file
	 * @param numSentences
	 *            Total number of sentences in input file
	 * @param numDictWords
	 *            Total number of dictionary words in input file
	 * @param isOverall
	 *            Flag to tell if the counts are for file or Overall
	 */
	private void addToCSV(String inputFile, int numWords, int numSentences,
			int numDictWords, boolean isOverall) {
		try {

			// Set up result CSV file when calling this function the first time
			if (resultCSVbw == null) {
				
				String type = "";
				if (weighted)
					type = "TACIT-Weighted-Wordcount-UserTags-";
				else
					type = "TACIT-Standard-Wordcount-UserTags-";
				
				DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
				String filePath = outputPath
						+ System.getProperty("file.separator") +createFileName();
				
				ConsoleView.printlInConsoleln("Created file " + outputPath
						+ System.getProperty("file.separator") + type
						+ df.format(dateObj)
						+ ".csv for storing counts for user tags.");
				resultCSVbw = new BufferedWriter(new FileWriter(filePath));

				if (createDATFile) {
					ConsoleView.printlInConsoleln("Created file " + outputPath
							+ System.getProperty("file.separator") + type
							+ df.format(dateObj) + ".dat.");
					datbw = new BufferedWriter(new FileWriter(createDATFilePath()));
				}

				resultCSVbw.write("Filename,WC,WPS,Dic,");

				List<Integer> keyList = new ArrayList<Integer>();
				keyList.addAll(categoryID.keySet());
				Collections.sort(keyList);

				// Add category headers
				StringBuilder toWrite = new StringBuilder();
				for (int i = 0; i < keyList.size(); i++) {
					toWrite.append(categoryID.get(keyList.get(i)) + ",");
				}
				resultCSVbw.write(toWrite.toString());
				resultCSVbw.newLine();

				if (createDATFile) {
					datbw.write("Filename WC WPS Dic ");
					datbw.write(toWrite.toString().replaceAll(",", " "));
					datbw.newLine();
				}
			}

			double totalWeight = 0;

			// Initialize map that will store the category count for the current
			// file
			HashMap<Integer, Double> categoryCount = new HashMap<Integer, Double>();
			List<Integer> keyList = new ArrayList<Integer>();
			keyList.addAll(categoryID.keySet());
			Collections.sort(keyList);
			for (Integer keyVal : keyList) {
				categoryCount.put(keyVal, 0.0);
			}

			List<String> dictWords = new ArrayList<String>();
			if (isOverall)
				dictWords.addAll(userOverallCount.keySet());
			else
				dictWords.addAll(userFileCount.keySet());

			// Find sum of all categories
			if (isOverall) {
				for (String word : dictWords) {
					List<Integer> wordCats = new ArrayList<Integer>();
					wordCats.addAll(userOverallCount.get(word).keySet());

					for (Integer cat : wordCats) {
						categoryCount.put(cat, categoryCount.get(cat)
								+ userOverallCount.get(word).get(cat));

						totalWeight = totalWeight
								+ userOverallCount.get(word).get(cat);
					}
				}
			} else {
				for (String word : dictWords) {
					List<Integer> wordCats = new ArrayList<Integer>();
					wordCats.addAll(userFileCount.get(word).keySet());

					for (Integer cat : wordCats) {
						categoryCount.put(cat, categoryCount.get(cat)
								+ userFileCount.get(word).get(cat));

						totalWeight = totalWeight
								+ userFileCount.get(word).get(cat);
					}
				}
			}

			// Write counts to output csv file
			// Note: The counts are the percentage contribution to the overall
			// weight
			// This is to keep it consistent with the LIWC results
			StringBuilder toWrite = new StringBuilder();
			toWrite.append("\"" + inputFile + "\"" + "," + numWords + ","
					+ Double.toString((double) numWords / numSentences) + ","
					+ Double.toString((double) 100 * numDictWords / numWords)
					+ ",");
			for (int i = 0; i < keyList.size(); i++) {
				// Avoid NaN values if totalWeight is 0
				if (totalWeight != 0)
					toWrite.append(Double.toString(100
							* categoryCount.get(keyList.get(i)) / totalWeight)
							+ ",");
				else
					toWrite.append(Double.toString(0) + ",");
			}
			resultCSVbw.write(toWrite.toString());
			resultCSVbw.newLine();

			if (createDATFile) {
				datbw.write(toWrite.toString().replaceAll(",", " "));
				datbw.newLine();
			}

			monitor.worked(4);

			// Pass numDictWords so that percentages of POS tags add up to 100.
			// Note: We are not considering words that are not part of the
			// dictionary even while counting Penn Treebank POS tags.
			if (doPennCounts)
				addPennToCSV(inputFile, isOverall);
			monitor.worked(4);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

	}

	/**
	 * Add counts for Penn Treebank POS tags. This function should be called
	 * from addToCSV only
	 * 
	 * @param inputFile
	 *            File for counting words
	 * @param isOverall
	 *            Flag to tell if the counts are for file or Overall
	 * @throws IOException
	 *             addToCSV handles the IOException so no need to handle here
	 */
	private void addPennToCSV(String inputFile, boolean isOverall)
			throws IOException {

		String pennPosTags = "CC<>CD<>DT<>EX<>FW<>IN<>JJ<>JJR<>JJS<>LS<>MD<>NN<>"
				+ "NNS<>NNP<>NNPS<>PDT<>POS<>PRP<>PRP$<>RB<>RBR<>RBS<>RP<>SYM<>TO<>"
				+ "UH<>VB<>VBD<>VBG<>VBN<>VBP<>VBZ<>WDT<>WP<>WP$<>WRB<>.<>,<>-LRB-<>-RRB-<>"
				+ "-RSB-<>-RSB-<>-LCB-<>-RCB-";

		String[] posTags;
		posTags = pennPosTags.split("<>");

		if (pennCSVbw == null) {
			String type = "";
			DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
			if (weighted)
				type = "TACIT-Weighted-Wordcount-DefaultTags-";
			else
				type = "TACIT-Standard-Wordcount-DefaultTags-";

			pennCSVbw = new BufferedWriter(new FileWriter(outputPath
					+ System.getProperty("file.separator") + type
					+ df.format(dateObj) + ".csv"));

			ConsoleView.printlInConsoleln("Created file " + outputPath
					+ System.getProperty("file.separator") + type
					+ df.format(dateObj)
					+ ".csv for storing counts for default TACIT tags.");

			pennCSVbw.write("Filename,");

			StringBuilder toWrite = new StringBuilder();
			for (int i = 0; i < posTags.length; i++) {
				if (posTags[i].compareTo(",") == 0)
					toWrite.append("\",\",");
				else
					toWrite.append(posTags[i] + ",");
			}
			pennCSVbw.write(toWrite.toString());
			pennCSVbw.newLine();
		}

		// Initialize map that will store the category count for the current
		// file
		HashMap<String, Double> categoryCount = new HashMap<String, Double>();
		for (String key : posTags) {
			categoryCount.put(key, 0.0);
		}

		List<String> dictWords = new ArrayList<String>();
		if (isOverall)
			dictWords.addAll(pennOverallCount.keySet());
		else
			dictWords.addAll(pennFileCount.keySet());

		double totalWeight = 0;

		// Find sum of all categories
		if (isOverall) {
			for (String word : dictWords) {
				List<String> wordCats = new ArrayList<String>();
				wordCats.addAll(pennOverallCount.get(word).keySet());

				for (String cat : wordCats) {
					// Use the try-catch block to catch any POS tag that was not
					// mentioned in the master list.
					try {
						categoryCount.put(cat, categoryCount.get(cat)
								+ pennOverallCount.get(word).get(cat));

						totalWeight = totalWeight
								+ pennOverallCount.get(word).get(cat);
					} catch (NullPointerException e) {
						ConsoleView.printlInConsoleln("Invalid Category: "
								+ cat);
					}
				}
			}
		} else {
			for (String word : dictWords) {
				List<String> wordCats = new ArrayList<String>();
				wordCats.addAll(pennFileCount.get(word).keySet());

				for (String cat : wordCats) {
					// Use the try-catch block to catch any POS tag that was not
					// mentioned in the master list.
					try {
						categoryCount.put(cat, categoryCount.get(cat)
								+ pennFileCount.get(word).get(cat));

						totalWeight = totalWeight
								+ pennFileCount.get(word).get(cat);
					} catch (NullPointerException e) {
						ConsoleView.printlInConsoleln("Invalid Category: "
								+ cat);
					}
				}
			}
		}

		// Write counts to output csv file
		// Note: The counts are the percentage contribution to the overall
		// weight
		// This is to keep it consistent with the LIWC results
		StringBuilder toWrite = new StringBuilder();
		toWrite.append(inputFile + ",");
		for (int i = 0; i < posTags.length; i++) {
			if (totalWeight != 0)
				toWrite.append(Double.toString(100
						* categoryCount.get(posTags[i]) / totalWeight)
						+ ",");
			else
				toWrite.append(Double.toString(0) + ",");
		}
		pennCSVbw.write(toWrite.toString());
		pennCSVbw.newLine();

	}

	/**
	 * Function to initialize all the HashMap's used to maintain word counts.
	 * 
	 * @param dictionaryFiles
	 *            List of dictionary Files
	 * @throws IOException
	 *             Catch error in caller. The errors might be due to bad
	 *             dictionary format.
	 */
	private void buildMaps(List<String> dictionaryFiles) throws IOException {

		monitor.subTask("Building Dictionary Maps");

		for (String dFile : dictionaryFiles) {
			clashWordCount = 2147483647;
			BufferedReader br = new BufferedReader(new FileReader(new File(
					dFile)));

			String currentLine = br.readLine().trim();
			
			if (currentLine == null) {
				ConsoleView.printlInConsoleln("The dictionary file " + dFile
						+ " is empty.");
			}

			if (currentLine.equals("%"))
				while ((currentLine = br.readLine().trim().toLowerCase()) != null
						&& !currentLine.equals("%")) {
					int key = Integer.parseInt(currentLine.split("\\s+")[0]
							.trim());
					String value = currentLine.split("\\s+")[1]
							.trim();

					if(categoryID.containsKey(key) && !((categoryID.get(key)).equals(value)))
					{

						addConflictingCategory(key, value);
						
					}
					else
					{
						categoryID.put(key, value);
					}
					
				}

			if (currentLine == null) {
				ConsoleView.printlInConsoleln("The dictionary file " + dFile
						+ " does not have any categorized words.");
			} else {
				while ((currentLine = br.readLine()) != null) {

					String[] words = currentLine.split("\\s+");

					// If word not in the maps, add it
					if (!wordDictionary.containsKey(words[0])) {

						// Handle stemming
						if (stemDictionary) {
							stemmer.setCurrent(words[0]);
							String stemmedWord = "";
							if (stemmer.stem())
								stemmedWord = stemmer.getCurrent();
							if (!stemmedWord.equals(""))
								words[0] = stemmedWord;
						}

						wordDictionary.put(words[0],
								new HashMap<Integer, Double>());
						userFileCount.put(words[0],
								new HashMap<Integer, Double>());
						userOverallCount.put(words[0],
								new HashMap<Integer, Double>());
						pennFileCount.put(words[0],
								new HashMap<String, Double>());
						pennOverallCount.put(words[0],
								new HashMap<String, Double>());
					}
					/*parsing the file to change category ID's
					 * 
					 */
					for (int i = 1; i < words.length; i = increment(i)) {
						
						if (words[i].contains(")") || words[i].contains("("))
							continue;
						// Add a category to the maps if it was not added
						// earlier
						if(oldCategoryMap.containsKey(Integer.parseInt(words[i]))){
							words[i] = oldCategoryMap.get(Integer.parseInt(words[i]))+"";
						}
						
					
						if (!wordDictionary.get(words[0]).containsKey(
								Integer.parseInt(words[i]))) {
							
							if (weighted) {
								wordDictionary.get(words[0]).put(
										Integer.parseInt(words[i]),
										Double.parseDouble(words[i + 1]));
							} else {
								
								wordDictionary.get(words[0]).put(
										Integer.parseInt(words[i]), 1.0);
							}
							userFileCount.get(words[0]).put(
									Integer.parseInt(words[i]), 0.0);
							userOverallCount.get(words[0]).put(
									Integer.parseInt(words[i]), 0.0);
						}
					}
				}
			}

			br.close();
		}

		monitor.worked(2);
	}
	
	private void addConflictingCategory(int key,String value){
		
		while(categoryID.containsKey(clashWordCount)){
				clashWordCount--;
		}
		categoryID.put(clashWordCount, value);
		oldCategoryMap.put(key, clashWordCount);
		clashWordCount--;
	}
	/**
	 * Call this function after completing the counts for individual files to
	 * set the file counts back to 0.
	 */
	private void refreshFileCounts() {
		Set<String> keySet = userFileCount.keySet();
		for (String key : keySet) {
			Set<Integer> subKeySet = userFileCount.get(key).keySet();

			for (Integer subKey : subKeySet) {
				userFileCount.get(key).put(subKey, 0.0);
			}
		}

		keySet = pennFileCount.keySet();
		for (String key : keySet) {
			Set<String> subKeySet = pennFileCount.get(key).keySet();

			for (String subKey : subKeySet) {
				pennFileCount.get(key).put(subKey, 0.0);
			}
		}
		monitor.worked(1);
	}

	/**
	 * Utility function to increment loops while handling weighted and non
	 * weighted dictionaries.
	 * 
	 * @param val
	 * @return
	 */
	private int increment(int val) {
		if (weighted) {
			return val + 2;
		} else
			return val + 1;
	}
	protected File getSetupFile(String bundleEntry) throws IOException{
		File setupFile = new File(FileLocator.toFileURL(
				Platform.getBundle(Activator.PLUGIN_ID).getEntry(
						bundleEntry)).getPath());
		return setupFile;
	}
	/**
	 * Sets all the models for OpenNLP
	 * 
	 * @return Returns true if no errors while loading models
	 */
	private boolean setModels() {
		monitor.subTask("Setting Models");
		InputStream sentenceIs = null, tokenIs = null, posIs = null;
		try {
			File setupFile = getSetupFile("en-sent.bin");
			
			sentenceIs = new FileInputStream(setupFile.toString());
			setupFile = getSetupFile("en-token.bin");
			tokenIs = new FileInputStream(setupFile.toString());
			setupFile = getSetupFile("en-pos-maxent.bin");
			posIs = new FileInputStream(setupFile.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Model file not found");
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		try {
			sentenceModel = new SentenceModel(sentenceIs);
			tokenizerModel = new TokenizerModel(tokenIs);
			posModel = new POSModel(posIs);
		} catch (InvalidFormatException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		sentDetector = new SentenceDetectorME(sentenceModel);
		tokenize = new TokenizerME(tokenizerModel);
		posTagger = new POSTaggerME(posModel);

		monitor.worked(1);

		return true;
	}
}