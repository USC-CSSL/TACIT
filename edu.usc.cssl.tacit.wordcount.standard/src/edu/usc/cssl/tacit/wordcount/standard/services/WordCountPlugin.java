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
import org.eclipse.core.runtime.Platform;

import edu.usc.cssl.tacit.common.TacitUtility;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import edu.usc.cssl.tacit.wordcount.standard.Activator;

public class WordCountPlugin {

	private SentenceModel sentenceModel;
	private TokenizerModel tokenizerModel;
	private POSModel posModel;
	private SentenceDetectorME sentDetector;
	private TokenizerME tokenize;
	private POSTaggerME posTagger;

	private boolean weighted;
	private Date dateObj;

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

	public WordCountPlugin(boolean weighted, Date dateObj) {
		this.weighted = weighted;
		this.dateObj = dateObj;
	}

	public void countWords(List<String> inputFiles,
			List<String> dictionaryFiles, String outputPath) {
		if (!setModels())
			return;

		try {
			buildMaps(dictionaryFiles);
		} catch (IOException e) {
			ConsoleView.printlInConsoleln("Error parsing dictionary.");
			e.printStackTrace();
			return;
		}

		System.out.println(wordDictionary.toString());
		System.out.println(userFileCount.toString());

		for (String iFile : inputFiles) {
			do_countWords(iFile, outputPath);
			refreshFileCounts();
		}

		if (weighted)
			TacitUtility.createRunReport(outputPath, "Weighted Word Count",
					dateObj);
		else
			TacitUtility.createRunReport(outputPath, "Standard Word Count",
					dateObj);
		return;
	}

	private void do_countWords(String inputFile, String outputPath) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(
					inputFile)));
			String currentLine;
			int numWords = 0;
			int numDictWords = 0;
			double totalWeight = 0;
			int numSentences = 0;

			while ((currentLine = br.readLine()) != null) {
				String[] sentences = sentDetector.sentDetect(currentLine);
				numSentences = numSentences + sentences.length;

				for (int i = 0; i < sentences.length; i++) {
					String[] words = tokenize.tokenize(sentences[i]);
					String[] posTags = posTagger.tag(words);
					numWords = numWords + words.length;

					for (int j = 0; j < words.length; j++) {
						if (wordDictionary.containsKey(words[j])) {
							numDictWords++;

							// Increment count for all user defined categories
							Set<Integer> wordCategories = wordDictionary.get(
									words[j]).keySet();
							for (Integer cat : wordCategories) {
								double wordWeight = wordDictionary
										.get(words[j]).get(cat);
								totalWeight = totalWeight + wordWeight;

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
			}
			br.close();
			addToCSV(inputFile, outputPath, numWords, numSentences,
					numDictWords, totalWeight, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addToCSV(String inputFile, String outputPath, int numWords,
			int numSentences, int numDictWords, double totalWeight,
			boolean isOverall) {
		try {
			
			//Set up result CSV file when calling this function the first time
			if (resultCSVbw == null) {
				String type = "";
				DateFormat df = new SimpleDateFormat("dd-MM-yy-HH-mm-ss");
				if (weighted)
					type = "TACIT-Weighted-Wordcount-";
				else
					type = "TACIT-Standard-Wordcount-";

				resultCSVbw = new BufferedWriter(new FileWriter(outputPath
						+ System.getProperty("file.separator") + type
						+ df.format(dateObj) + ".csv"));

				resultCSVbw.write("Filename,WC,WPS,Dic,");
				
				List<Integer> keyList = new ArrayList<Integer>(); 
				keyList.addAll(categoryID.keySet()); 
				Collections.sort(keyList);
				
				//
				StringBuilder toWrite = new StringBuilder();
				for (int i=0; i<keyList.size(); i++) {
					toWrite.append(keyList.get(i) + ",");
				}
				resultCSVbw.write(toWrite.toString());
				resultCSVbw.newLine();
			}

			// Initialize map that will store the category count for the current
			// file
			HashMap<Integer, Double> categoryCount = new HashMap<Integer, Double>();
			List<Integer> keyList = new ArrayList<Integer>();
			keyList.addAll(categoryID.keySet()); 
			Collections.sort(keyList);
			for (Integer keyVal : keyList) {
				categoryCount.put(keyVal, 0.0);
			}
			
			

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

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

		for (String dFile : dictionaryFiles) {
			BufferedReader br = new BufferedReader(new FileReader(new File(
					dFile)));

			String currentLine = br.readLine().trim();
			if (currentLine == null) {
				ConsoleView.printlInConsoleln("The dictionary file " + dFile
						+ " is empty.");
			}

			if (currentLine.equals("%"))
				while ((currentLine = br.readLine().trim().toLowerCase()) != null
						&& !currentLine.equals("%"))
					categoryID.put(
							Integer.parseInt(currentLine.split("\\s+")[0]
									.trim()), currentLine.split("\\s+")[1]
									.trim());

			if (currentLine == null) {
				ConsoleView.printlInConsoleln("The dictionary file " + dFile
						+ " does not have any categorized words.");
			} else {
				while ((currentLine = br.readLine()) != null) {
					String[] words = currentLine.split("\\s+");

					// If word not in the maps, add it
					if (!wordDictionary.containsKey(words[0])) {
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

					for (int i = 1; i < words.length; i = increment(i)) {
						// Add a category to the maps if it was not added
						// earlier
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

	/**
	 * Sets all the models for OpenNLP
	 * 
	 * @return Returns true if no errors while loading models
	 */
	private boolean setModels() {
		InputStream sentenceIs = null, tokenIs = null, posIs = null;
		try {
			File setupFile = new File(FileLocator.toFileURL(
					Platform.getBundle(Activator.PLUGIN_ID).getEntry(
							"en-sent.bin")).getPath());
			sentenceIs = new FileInputStream(setupFile.toString());
			setupFile = new File(FileLocator.toFileURL(
					Platform.getBundle(Activator.PLUGIN_ID).getEntry(
							"en-token.bin")).getPath());
			tokenIs = new FileInputStream(setupFile.toString());
			setupFile = new File(FileLocator.toFileURL(
					Platform.getBundle(Activator.PLUGIN_ID).getEntry(
							"en-pos-maxent.bin")).getPath());
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

		return true;
	}
}