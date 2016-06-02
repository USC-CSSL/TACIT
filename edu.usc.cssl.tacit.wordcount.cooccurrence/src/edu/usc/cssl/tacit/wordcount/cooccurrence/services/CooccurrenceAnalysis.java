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

	private static Pattern delimiters = Pattern.compile("[.,\"]");

	private HashSet<String> seedWords;
	private String outputPath;
	private Map<String, Map<String, Integer>> wordMat;
	private int windowSize;
	private int threshold;

	public CooccurrenceAnalysis() {
		seedWords = new HashSet<String>();
		wordMat = new HashMap<String, Map<String, Integer>>();
	}

	/**
	 * This function populates the seedWords set with seed words mentioned in
	 * the input file.
	 * 
	 * @param seedFile-absolute filepath of seedFile.
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
				if (!seedWords.contains(seed)) {
					seedWords.add(seed);
				}
			}
		}
		br.close();
		return (seedWords.size() > 0) ? true : false;
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
		Date currTime = new Date();
		ArrayList<String> emptyRemovalString = new ArrayList<String>();
		emptyRemovalString.add("");
		setOutputPath(outputPath);
		setThreshold(threshold);
		setWindowSize(windowSize);

		try {
			FileWriter fw = new FileWriter(new File(outputPath + File.separator + generateWindowFileName(currTime)));
			fw.write("Cooccurrence window,Seed Occurrences,Filename\n");
			boolean ret = false;
			//TODO: Handle the case where it outputs only if the window size is greater than 0
			if (windowSize > 0) {
				ret = setSeedWords(seedFile); // build the seed word dictionary
			}else{
				appendLog("Window size is zero.Cannot be processed..");
				fw.close();
				return false;
			}


			String[] listOfFiles = (String[]) selectedFiles.toArray(new String[selectedFiles.size()]);
			for (String fname : listOfFiles) {
				File f = new File(fname);
				//monitor.subTask("Processing input file " + f.getName());
				//appendLog("Processing input file " + f.getName());
				if (f.getAbsolutePath().contains("DS_Store"))
					continue;
				if (!f.exists() || f.isDirectory())
					continue;

				BufferedReader br = new BufferedReader(new FileReader(f));
				List<String> window = null;
				//Finding the cooccurrence window in each line 
				try {

					while ((currentLine = br.readLine()) != null) {
						ArrayList<String> lineWords = new ArrayList<String>(Arrays.asList(delimiters.matcher(currentLine).replaceAll(" ").toLowerCase().trim().split("\\s+")));
						//ArrayList<String> lineWords = new ArrayList<String>(Arrays.asList(currentLine.split(" ")));
						//lineWords.removeAll(emptyRemovalString);
						boolean isFirstWindow = true;
						
						int windowSeedWordCount = 0;
						int windowstart = 0;
						int windowend = Math.min(windowSize, lineWords.size())-1;
						window = new ArrayList<String>(lineWords.subList(windowstart, windowend+1));
						
							
						while (windowend < lineWords.size()){
							//Check if the window contains at least as many as threshold number of seed words.
							//If yes then print the window else don't.
							
							if (isFirstWindow){
								for (String word: window){
									if (seedWords.contains(word)){
										windowSeedWordCount++;
									}
								}
								
								if(windowSeedWordCount >= threshold){
									fw.write(StringUtils.join(window, " ") + ","+windowSeedWordCount+","+f.getName()+"\n");
								}

								isFirstWindow = false;
								windowend++;
										
							}else{
								String oldWord = window.remove(0);
								String newWord = lineWords.get(windowend);
								window.add(newWord);
								
								if (seedWords.contains(oldWord)){
									windowSeedWordCount--;
								}
								if (seedWords.contains(newWord)){
									windowSeedWordCount++;
								}
								if (windowSeedWordCount >= threshold){
									fw.write(StringUtils.join(window, " ") + ","+windowSeedWordCount+","+f.getName()+"\n");
								}
								windowend++;
							}
							
							if (buildMatrix) {
								if (window.size() >= 2 ){
									String firstWord = window.get(0);
									Map<String, Integer> vec = wordMat.get(firstWord);
									if (vec == null) {
										vec = new HashMap<String, Integer>();
										wordMat.put(firstWord, vec);
									}
								
									for (int i=1;i<window.size();i++) {
										String nextWord =window.get(i);
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
										if (revVec.containsKey(firstWord)) {
											revVec.put(firstWord, revVec.get(firstWord) + 1);
										} else {
											revVec.put(firstWord, 1);
										}

									}	
								}
								
							}
							
							
						}//each window
		
					}//each line
					
					//Building the left out matrix after the last window  
					if (buildMatrix){
						if (window != null && window.size() >= 0){
							window.remove(0);
						}
						while (window != null && window.size() > 0){
							String firstWord = window.remove(0);
							
							Map<String, Integer> vec = wordMat.get(firstWord);
							if (vec == null) {
								vec = new HashMap<String, Integer>();
								wordMat.put(firstWord, vec);
							}
							
							for(String nextWord : window){
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
								if (revVec.containsKey(firstWord)) {
									revVec.put(firstWord, revVec.get(firstWord) + 1);
								} else {
									revVec.put(firstWord, 1);
								}
							}
							
						}
					}
					
					
				} catch (OutOfMemoryError e) {
					br.close();
					fw.close();
					appendLog("Exception occurred in Cooccurrence Analysis :");
					appendLog("Sorry the co-occurence matrix is so huge. Please run again without opting Build Matrix");
					return false;
				}
				br.close();
				monitor.worked(1);
				
				if (monitor.isCanceled()){
					fw.close();
					appendLog("operation cancelled by user..");
					return false;
				}
			
			}//each file
			
			
			
			try {
					if (buildMatrix) {
						monitor.subTask("Writing Word Matrix");
						writeWordMatrix(generateMatrixFileName(currTime));
					}
					monitor.worked(10);
					
			} 
			catch (OutOfMemoryError e) {
				fw.close();
				appendLog("Exception occurred in Cooccurrence Analysis :");
				appendLog("Sorry the co-occurence matrix is so huge. Please run again without opting Build Matrix");
				return false;
			}
			
			
			monitor.worked(10);
			generateRunReport();
			fw.close();
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
	
	protected String generateWindowFileName(Date currTime){
		String phraseFilename = new SimpleDateFormat("'co-occur_windows_'yyyyMMddhhmm'.csv'").format(currTime);
		return phraseFilename;
	}
	protected String generateMatrixFileName(Date currTime){
		return new SimpleDateFormat("'co-Date currTime'yyyyMMddhhmm'.csv'").format(currTime);
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
			appendLog("Writing Word Matrix into " + filename);
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