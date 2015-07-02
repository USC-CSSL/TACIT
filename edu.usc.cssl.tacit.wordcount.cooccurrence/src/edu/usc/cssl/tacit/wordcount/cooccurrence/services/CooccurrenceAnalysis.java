package edu.usc.cssl.tacit.wordcount.cooccurrence.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;

import edu.usc.cssl.tacit.common.TacitUtility;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class CooccurrenceAnalysis {

	private static String delimiters = "";// " .,;\"!-()\\[\\]{}:?'/\\`~$%#@&*_=+<>";
	private boolean doPhrases;
	private Map<String, Integer> seedWords;
	private String outputPath;
	private Map<String, Map<String, Integer>> wordMat;

	/**
	 * This function populates the seedWords map with seed words mentioned in
	 * the input file.
	 * 
	 * @param seedFile
	 *            - absolute filepath of seedFile.
	 * @return boolean value indicating success or failure
	 * @throws IOException
	 */
	private boolean setSeedWords(String seedFile) throws IOException {
		String[] seeds = null;
		String currentLine = null;

		BufferedReader br = new BufferedReader(new FileReader(
				new File(seedFile)));
		while ((currentLine = br.readLine()) != null) {
			seeds = currentLine.split(" ");
			for (String seed : seeds) {
				if (!seedWords.containsKey(seed)) {
					seedWords.put(seed, 1);
				}
			}
		}
		br.close();
		return (seedWords.size() > 0) ? true : false;
	}

	public CooccurrenceAnalysis() {
		seedWords = new HashMap<String, Integer>();
		wordMat = new HashMap<String, Map<String, Integer>>();
		doPhrases = false;
	}

	private void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
		createIfMissing(outputPath);
	}

	public boolean calculateCooccurrences(List<String> selectedFiles,
			String seedFile, int windowSize, String outputPath, int threshold,
			boolean buildMatrix, IProgressMonitor monitor) {

		String currentLine = null;
		Queue<String> q = new LinkedList<String>();
		List<String> phrase = new ArrayList<String>();

		setOutputPath(outputPath);

		// build the seed word dictionary
		try {

			boolean ret = false;
			if (windowSize > 0) { // TODO : prevent from GUI
				ret = setSeedWords(seedFile);
			}
			if (ret) {
				doPhrases = true;
			}

			File[] listOfFiles = (File[]) selectedFiles
					.toArray(new File[selectedFiles.size()]);
			int seedWordCount = seedWords.size();
			int count;
			for (File f : listOfFiles) {
				monitor.subTask("Processing inout file "+f.getName());
				appendLog("Processing inout file "+f.getName());
				count = 0;
				if (f.getAbsolutePath().contains("DS_Store"))
					continue;

				List<String> words = new ArrayList<String>();
				if (!f.exists() || f.isDirectory())
					continue;
				BufferedReader br = new BufferedReader(new FileReader(f));

				int line_no = 0;
				while ((currentLine = br.readLine()) != null) {
					if (currentLine.isEmpty() || currentLine.equals(""))
						continue;
					line_no++;
					for (String word : currentLine.split(" ")) {
						if (word.isEmpty() || word.equals(""))
							continue;

						word.replaceAll(delimiters, "");
						if (buildMatrix)
							words.add(word);

						if (doPhrases) {

							if (count >= threshold || count >= seedWordCount) {
								StringBuilder match = new StringBuilder();
								for (String str : q) {
									if (seedWords.containsKey(str))
										match.append('*');
									match.append(str + ' ');
								}
								phrase.add(f.getName() + "  " + line_no + " "
										+ match.toString());
								q.clear();
								count = 0;
								for (String s : seedWords.keySet()) {
									seedWords.put(s, 1);
								}
							} else if (q.size() >= windowSize) {
								String first = q.remove();
								if (seedWords.containsKey(first)) {
									if (seedWords.get(first) == 0) {
										count--;
										seedWords.put(first, 1);
									}
								}
							}
							q.add(word);
							if (seedWords.containsKey(word)) {
								if (seedWords.get(word) != 0) {
									count++;
									seedWords.put(word, 0);
								}
							}

						}

						if (buildMatrix) {

							Map<String, Integer> vec = null;
							// ConsoleView.writeInConsole("Building word mat for " +
							// word);
							if (wordMat.containsKey(word)) {
								vec = wordMat.get(word);
							} else {
								vec = new HashMap<String, Integer>();
								wordMat.put(word, vec);
							}
							for (String second : words) {
								if (vec.containsKey(second)) {
									vec.put(second, vec.get(second) + 1);
								} else {
									vec.put(second, 1);
								}
								Map<String, Integer> temp = wordMat.get(second);
								if (temp.containsKey(word)) {
									temp.put(word, temp.get(word) + 1);
								} else {
									temp.put(word, 1);
								}

							}
						}

					}
				}
				br.close();
				monitor.worked(1);
			}

			if (buildMatrix) {
				monitor.subTask("Writing Word Matrix");
				writeWordMatrix();
				
			}
			monitor.worked(10);
			if (ret && phrase.size() > 0) {
				monitor.subTask("Writing Phrases");
				writePhrases(phrase);
			}
			monitor.worked(10);
			ConsoleView.printlInConsoleln(String.valueOf(phrase.size()));
			Date dateObj = new Date();
			TacitUtility.createRunReport(outputPath, "Cooccurrence Analysis",dateObj);
			return true;
		} catch (Exception e) {
			ConsoleView.printlInConsoleln("Exception occurred in Cooccurrence Analysis "
					+ e);
		}

		return false;
	}

	/**
	 * write the phrases into file phrases.txt
	 * 
	 * @param phrases
	 *            - phrases to be written
	 */
	private void writePhrases(List<String> phrases) {
		try {
			FileWriter fw = new FileWriter(new File(outputPath + File.separator
					+ "phrases.txt"));
			for (String p : phrases) {
				fw.write(p + "\n");
			}
			ConsoleView.printlInConsoleln("Writing phrases at "+outputPath + File.separator
					+ "phrases.txt");
			fw.close();
		} catch (IOException e) {
			ConsoleView.printlInConsoleln("Error writing output to file phrases.txt " + e);
		}
	}

	/**
	 * Creates a directory in the file system if it does not already exists
	 * 
	 * @param folder
	 *            : full path of the directory which has to be created.
	 */
	private void createIfMissing(String folder) {
		File path = new File(folder);
		if (!path.exists()) {
			path.mkdirs();
		}
	}

	/**
	 * write the word matrix into the file word-to-word-matrix.csv
	 */
	private void writeWordMatrix() {

		SortedSet<String> keys = new TreeSet<String>(wordMat.keySet());
		Map<String, Integer> vec = null;
        
		try {
			FileWriter fw = new FileWriter(new File(outputPath + File.separator
					+ "word-to-word-matrix.csv"));
			fw.write(" ,");
			for (String key : keys) {
				fw.write(key + ",");
			}
			fw.write("\n");

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
			appendLog("Writng Word Matrix into word-to-word-matrix.csv");
			fw.close();
		} catch (IOException e) {
			ConsoleView.printlInConsoleln("Error writing output to files" + e);
		}
	}

	public boolean invokeCooccurrence(List<String> selectedFiles,
			String seedFileLocation, String fOutputDir, String numTopics,
			String ftxtThreshold, boolean fOption, IProgressMonitor monitor) {

		int windowSize = 0;
		if (!numTopics.equals(""))
			windowSize = Integer.parseInt(numTopics);

		int threshold = 0;
		if (!ftxtThreshold.equals(""))
			threshold = Integer.parseInt(ftxtThreshold);

		boolean buildMatrix = false;
		if (fOption)
			buildMatrix = true;

		// ConsoleView.writeInConsole("Running Co-occurrence Analysis...");
		appendLog("Running Co-occurrence Analysis...");
		boolean isSuccess = calculateCooccurrences(selectedFiles,
				seedFileLocation, windowSize, fOutputDir, threshold,
				buildMatrix,monitor);
		if (isSuccess == false) {
			appendLog("Sorry. Something went wrong with Co-occurrence Analysis. Please check your input and try again.\n");
			return isSuccess;
		}

		appendLog("Output for Co-occurrence Analysis");

		appendLog("Word to word matrix stored in " + fOutputDir
				+ File.separator + "word-to-word-matrix.csv");
		if (seedFileLocation != "" && !seedFileLocation.isEmpty()
				&& windowSize != 0)
			appendLog("Phrases stored in " + fOutputDir + File.separator
					+ "phrases.txt");
		return true;

	}

	private void appendLog(String string) {
		ConsoleView.printlInConsoleln(string);

	}
}