package edu.usc.cssl.nlputils.wordcount.standard.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Platform;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.usc.nlputils.common.snowballstemmer.PorterStemmer;

public class WordCountPlugin {
	private StringBuilder readMe = new StringBuilder();
	private boolean doStopWords;
	private HashSet<String> stopWordSet;
	Map<String, Map<String, Double>> wordMat;
	Set<String> dict;
	SortedSet<String> keys;
	String delimeters = " .,;\"!-()[]{}:?'/\\`~$%#@&*_=+<>";
	PorterStemmer stemmer = new PorterStemmer();
	private boolean doStemming;
	private boolean useDict;

	DecimalFormat df = new DecimalFormat("#.##");

	private static Logger logger = Logger.getLogger(WordCountPlugin.class
			.getName());

	public int invokeWordCount(List<String> inputFiles,
			List<String> dictionaryFiles, String stopWordsFile,
			String outputFile, boolean doStemming) throws IOException {
		long startTime = System.currentTimeMillis();
		useDict = true;
		this.doStemming = doStemming;
		wordMat = new HashMap<String, Map<String, Double>>();
		stopWordSet = new HashSet<String>();
		keys = new TreeSet<String>();

		// StopWords is optional
		if (stopWordsFile.equals(null) || stopWordsFile.equals(""))
			this.doStopWords = false;
		else {
			this.doStopWords = true;
			startTime = System.currentTimeMillis();
			stopWordSetBuild(stopWordsFile);
			logger.info("Finished building the Stop Words Set in "
					+ (System.currentTimeMillis() - startTime)
					+ " milliseconds.");
			appendLog("Finished building the Stop Words Set in "
					+ (System.currentTimeMillis() - startTime)
					+ " milliseconds.");
		}

		if (dictionaryFiles.isEmpty()) {
			useDict = false;
		}

		if (useDict) {
			dict = new HashSet<String>();
			for (String string : dictionaryFiles) {
				buildDictionary(new File(string));
			}
		}

		for (String inputFile : inputFiles) {
			String input = inputFile;

			// Mac cache file filtering
			if (inputFile.contains("DS_Store"))
				continue;
			if (new File(inputFile).isDirectory()) {
				continue;
			}
			Map<String, Double> words = new HashMap<String, Double>();
			countWords(inputFile, words);
			wordMat.put(input, words);
		}
		writeToOutput(outputFile);
		writeReadMe(outputFile);
		return 0;

	}

	/* Build the dictionary set if present */
	public void buildDictionary(File dictFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(dictFile));
		String currentLine = null;
		String[] words = null;
		while ((currentLine = br.readLine()) != null) {
			words = currentLine.split(" ");
			for (String word : words) {
				if (!word.equals("")) {
					word = word.trim().toLowerCase();
					if (doStemming) {

						word = word.replace("*", "");
						stemmer.setCurrent(word);
						String stemmedWord = "";
						if (stemmer.stem())
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
		while ((currentLine = br.readLine()) != null) {
			stopWordSet.add(currentLine.trim().toLowerCase());
		}
		br.close();
	}

	/* Does the actual word count for each document */
	public void countWords(String inputFile, Map<String, Double> words) {

		int totalWords = 0;

		try {
			String seed = null;

			@SuppressWarnings("unchecked")
			PTBTokenizer ptbt = new PTBTokenizer(new FileReader(inputFile),
					new CoreLabelTokenFactory(), "");
			for (CoreLabel label; ptbt.hasNext();) {
				label = (CoreLabel) ptbt.next();
				seed = label.toString().toLowerCase();
				if (seed.isEmpty())
					continue;
				if (doStopWords && stopWordSet.contains(seed))
					continue;
				if (delimeters.contains(seed))
					continue;
				if (doStemming) {

					seed = seed.replace("*", "");
					stemmer.setCurrent(seed);
					String stemmedWord = "";
					if (stemmer.stem())
						stemmedWord = stemmer.getCurrent();
					if (!stemmedWord.equals(""))
						seed = stemmedWord;

				}
				if (useDict && !dict.contains(seed))
					continue;
				totalWords++;
				if (words.containsKey(seed)) {
					words.put(seed, words.get(seed) + 1);
				} else {
					words.put(seed, (double) 1);
					if (!keys.contains(seed))
						keys.add(seed);
				}
			}

			for (String word : words.keySet()) {
				words.put(word, (100 * words.get(word)) / totalWords);
			}

		} catch (Exception e) {
			System.out.println("Error processing file " + inputFile
					+ " .Exception " + e);
		}
	}

	/* Writes the whole output to a output file */
	public void writeToOutput(String outputPath) {
		Map<String, Double> vec = null;

		System.out.println(keys.size());
		try {
			File file = new File(outputPath +File.separator
					+ "document-to-word-matrix.csv");
			
			if (!file.exists()) {
				file.createNewFile();
			}
			else {
				file.delete();
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file);
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

	// This function updates the consoleMessage parameter of the context.
	private void appendLog(String message) {
		readMe.append(message + "\n");
	}

	public void writeReadMe(String location) {
		File readme = new File(location + "_README.txt");

		if (!readme.exists()) {
			try {
				readme.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			readme.delete();
			try {
				readme.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(readme));
			String plugV = Platform
					.getBundle("edu.usc.cssl.nlputils.wordcount.standard")
					.getHeaders().get("Bundle-Version");
			String appV = Platform
					.getBundle("edu.usc.cssl.nlputils.repository").getHeaders()
					.get("Bundle-Version");
			Date date = new Date();
			bw.write("Basic Word Count Output\n-----------------------\n\nApplication Version: "
					+ appV
					+ "\nPlugin Version: "
					+ plugV
					+ "\nDate: "
					+ date.toString() + "\n\n");
			bw.write(readMe.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}