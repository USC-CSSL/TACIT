package edu.usc.cssl.tacit.wordcount.cooccurrence.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;

import edu.usc.cssl.tacit.common.TacitUtility;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class CooccurrenceAnalysis {

	private static Pattern delimiters = Pattern.compile("[\\\\.,;\"!-\\)\\(\\]\\[\\{\\}:\\?'/\\`~$%#@&*_=\\+<>]");
	private boolean doPhrases;
	private Map<String, ArrayList<Integer>> seedWords;
	private HashMap<Set<String>, Integer> seedCombos;
	private String outputPath;
	private Map<String, Map<String, Integer>> wordMat;
	private int windowSize;
	private int threshold;

	public CooccurrenceAnalysis() {
		seedWords = new HashMap<String, ArrayList<Integer>>();
		seedCombos = new HashMap<Set<String>, Integer>();
		wordMat = new HashMap<String, Map<String, Integer>>();
		doPhrases = false;
	}

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

		BufferedReader br = new BufferedReader(new FileReader(new File(seedFile)));
		while ((currentLine = br.readLine()) != null) {
			seeds = currentLine.trim().toLowerCase().split(" ");
			for (String seed : seeds) {
				seed = seed.trim();
				if (seed.isEmpty() || seed.equals(""))
					continue;
				if (!seedWords.containsKey(seed)) {
					seedWords.put(seed, new ArrayList<Integer>());
				}
			}
		}
		br.close();
		return (seedWords.size() > 0) ? true : false;
	}

	/**
	 * This function populates creates all possible combinations of seedwords
	 * starting from size - threshold
	 */
	private boolean buildSeedCombos() {
		ArrayList<String> seedList = new ArrayList<String>(seedWords.keySet());
		Set<Set<String>> t_prevCombos = new HashSet<Set<String>>();

		for (String seed : seedList) {
			Set<String> singleton = new HashSet<String>(Arrays.asList(seed));
			t_prevCombos.add(singleton);
			if (threshold == 1)
				seedCombos.put(singleton, 0);
		}

		int size = 2;
		while (true) {
			if (size > windowSize || size > seedList.size())
				break;

			Set<Set<String>> t_currCombos = new HashSet<Set<String>>();
			for (Set<String> combo : t_prevCombos) {
				for (String seed : seedList) {
					Set<String> newCombo = new HashSet<String>(combo);
					if (!combo.contains(seed)) {
						newCombo.add(seed);
						t_currCombos.add(newCombo);
					}
				}
			}

			if (size >= threshold) {
				for (Set<String> combo : t_currCombos) {
					seedCombos.put(combo, 0);
				}
			}
			size++;
			t_prevCombos = t_currCombos;
		}
		return true;
	}

	private void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
		createIfMissing(outputPath);
	}

	private void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	private void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public boolean calculateCooccurrences(List<String> selectedFiles, String seedFile, int windowSize,
			String outputPath, int threshold, boolean buildMatrix, IProgressMonitor monitor) {
		String currentLine = null;
		List<String> phrase = new ArrayList<String>();
		Date currTime = new Date();
		setOutputPath(outputPath);
		setThreshold(threshold);
		setWindowSize(windowSize);

		try {
			boolean ret = false;
			if (windowSize > 0) {
				ret = setSeedWords(seedFile); // build the seed word dictionary
			}
			if (ret) {
				doPhrases = true;
				buildSeedCombos();
			}

			String[] listOfFiles = (String[]) selectedFiles.toArray(new String[selectedFiles.size()]);
			for (String fname : listOfFiles) {
				File f = new File(fname);

				monitor.subTask("Processing input file " + f.getName());
				appendLog("Processing input file " + f.getName());
				if (f.getAbsolutePath().contains("DS_Store"))
					continue;
				if (!f.exists() || f.isDirectory())
					continue;

				BufferedReader br = new BufferedReader(new FileReader(f));
				int line_no = 0;
				try {
					while ((currentLine = br.readLine()) != null) {
						ArrayList<String> words = new ArrayList<String>(Arrays.asList(
								delimiters.matcher(currentLine).replaceAll(" ").toLowerCase().trim().split("\\s+")));
						line_no++;
						int windowend = Math.min(windowSize, words.size()) - 1;
						List<String> window = new ArrayList<String>(words.subList(0, windowend));

						String pprev_word = null;
						String prev_word = null;
						for (int wi = 0; wi < words.size(); wi++) {
							String word = words.get(wi).trim();
							if (word.isEmpty() || word.equals(""))
								continue;

							if (window.size() > 0)
								window.remove(0);
							if (windowend < words.size() && windowSize > 1)
								window.add(words.get(windowend++).trim());

							if (buildMatrix) {
								Map<String, Integer> vec = wordMat.get(word);
								if (vec == null) {
									vec = new HashMap<String, Integer>();
									wordMat.put(word, vec);
								}
								for (String nextWord : window) {
									if (vec.containsKey(nextWord)) {
										vec.put(nextWord, vec.get(nextWord) + 1);
									} else {
										vec.put(nextWord, 1);
									}
									Map<String, Integer> revVec = wordMat.get(nextWord);
									if (revVec == null) {
										revVec = new HashMap<String, Integer>();
										wordMat.put(nextWord, revVec);
									}
									if (revVec.containsKey(word)) {
										revVec.put(word, revVec.get(word) + 1);
									} else {
										revVec.put(word, 1);
									}

								}
							}

							if (doPhrases) {
								if (seedWords.containsKey(word)) {
									for (Set<String> combo : seedCombos.keySet()) {

										boolean flag = true;
										String comboStr = StringUtils.join(combo, ' ');
										if (!comboStr.contains(word))
											continue;
										for (String seedWord : combo) {
											if (!window.contains(seedWord) && !word.equals(seedWord)) {
												flag = false;
												break;
											}
										}
										if (flag == true) {
											ArrayList<String> context = new ArrayList<String>();

											context.addAll(Arrays.asList(pprev_word, prev_word, word));
											context.addAll(window);
											if (wi + window.size() + 1 < words.size())
												context.add(words.get(wi + window.size() + 1));

											phrase.add(StringUtils.join(combo, ' ') + "," + f.getName() + "," + line_no
													+ "," + StringUtils.join(context, ' '));

											int phrase_count = seedCombos.get(combo) + 1;
											seedCombos.put(combo, phrase_count);
										}
									}
								}
							}
							pprev_word = prev_word;
							prev_word = word;
						}
					}
				} catch (OutOfMemoryError e) {
					br.close();
					appendLog("Exception occurred in Cooccurrence Analysis :");
					appendLog("Sorry the co-occurence matrix is so huge. Please run again without opting Build Matrix");
					return false;
				}
				br.close();
				monitor.worked(1);
			}
			try {
				if (buildMatrix) {
					monitor.subTask("Writing Word Matrix");
					writeWordMatrix(generateMatrixFileName(currTime));
				}
				monitor.worked(10);
				if (ret && phrase.size() > 0) {
					monitor.subTask("Writing Phrases");
					writePhrases(phrase, generatePhrasesFileName(currTime));
					writeSeedComboStats(generateSeedComboStatsFileName(currTime));
				} else {
					appendLog(
							"None of the seed combinations occured in the input files for the given window size and threshold");
					appendLog("Phrase and frequency files not created");
				}
			} catch (OutOfMemoryError e) {
				appendLog("Exception occurred in Cooccurrence Analysis :");
				appendLog("Sorry the co-occurence matrix is so huge. Please run again without opting Build Matrix");
				return false;
			}
			monitor.worked(10);
			appendLog(String.valueOf(phrase.size()));
			generateRunReport();
			return true;
		} catch (Exception e) {
			appendLog("Exception occurred in Cooccurrence Analysis " + e);
		}

		return false;
	}

	protected void generateRunReport(){
		Date dateObj = new Date();
		TacitUtility.createRunReport(outputPath, "Cooccurrence Analysis", dateObj, null);
	}
	protected String generateSeedComboStatsFileName(Date currTime){
		String freqFilename = new SimpleDateFormat("'co-occur_seedfrequencies_'yyyyMMddhhmm'.csv'")
				.format(currTime);
		return freqFilename;
	}
	protected String generatePhrasesFileName(Date currTime){
		String phraseFilename = new SimpleDateFormat("'co-occur_phrases_'yyyyMMddhhmm'.csv'")
				.format(currTime);
		return phraseFilename;
	}
	protected String generateMatrixFileName(Date currTime){
		return new SimpleDateFormat("'co-Date currTime'yyyyMMddhhmm'.csv'").format(currTime);
	}
	private void writeSeedComboStats(String filename) {
		try {
			FileWriter fw = new FileWriter(new File(outputPath + File.separator + filename));
			fw.write("seed combination, count\n");
			for (Set<String> combo : seedCombos.keySet()) {
				fw.write(StringUtils.join(combo, ' ') + "," + seedCombos.get(combo) + "\n");
			}
			appendLog("Writing frequencies of seed combinations at " + outputPath + File.separator + filename);
			fw.close();
			appendLog("Seed combination frequencies stored in " + outputPath + File.separator + filename);

		} catch (IOException e) {
			appendLog("Error writing seed frequencies to file " + filename + e);
		}
	}

	/**
	 * write the phrases into file phrases.txt
	 * 
	 * @param phrases
	 *            - phrases to be written
	 */
	private void writePhrases(List<String> phrases, String filename) {
		try {
			FileWriter fw = new FileWriter(new File(outputPath + File.separator + filename));
			fw.write("seed combination, file name, line no., phrase\n");
			for (String p : phrases) {
				fw.write(p + "\n");
			}
			appendLog("Writing phrases at " + outputPath + File.separator + filename);
			fw.close();
			appendLog("Phrases stored in " + outputPath + File.separator + filename);

		} catch (IOException e) {
			appendLog("Error writing phrases to file " + filename + e);
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
	private void writeWordMatrix(String filename) {

		SortedSet<String> keys = new TreeSet<String>(wordMat.keySet());
		Map<String, Integer> vec = null;

		try {
			FileWriter fw = new FileWriter(new File(outputPath + File.separator + filename));
			fw.write(" ,");
			for (String key : keys) {
				fw.write(key + ",");
			}
			fw.write("\n");
			for (String key : keys) {
				StringBuilder rowStr = new StringBuilder();
				rowStr.append(key + ",");
				vec = wordMat.get(key);
				for (String value : keys) {
					if (vec.containsKey(value)) {
						rowStr.append(vec.get(value) + ",");
					} else {
						rowStr.append("0,");
					}
				}
				fw.write(rowStr + "\n");
			}
			appendLog("Writng Word Matrix into " + filename);
			fw.close();
			appendLog("Word to word matrix stored in " + outputPath + File.separator + filename);

		} catch (IOException e) {
			appendLog("Error writing word matrix " + e);
		}
	}

	public boolean invokeCooccurrence(List<String> selectedFiles, String seedFileLocation, String fOutputDir,
			String numTopics, String ftxtThreshold, boolean fOption, IProgressMonitor monitor) {

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
		boolean isSuccess = calculateCooccurrences(selectedFiles, seedFileLocation, windowSize, fOutputDir, threshold,
				buildMatrix, monitor);
		if (isSuccess == false) {
			appendLog(
					"Sorry. Something went wrong with Co-occurrence Analysis. Please check your input and try again.\n");
			return isSuccess;
		}

		return true;

	}

	private void appendLog(String string) {
		ConsoleView.printlInConsoleln(string);

	}
}