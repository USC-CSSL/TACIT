package edu.usc.cssl.tacit.topicmodel.lda.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import edu.usc.cssl.tacit.common.TacitUtility;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class LdaAnalysis {
	private StringBuilder readMe = new StringBuilder();
	private String sourceDir;
	private int numTopics;
	private String outputDir;
	private boolean wordWeights;

	public void initialize(String sourceDir, int numTopics, String outputDir, boolean wordWeights) {
		this.sourceDir = sourceDir;
		this.numTopics = numTopics;
		this.outputDir = outputDir;
		this.wordWeights = wordWeights;
	}

	public void doLDA(IProgressMonitor monitor, Date dateObj)
			throws FileNotFoundException, IOException {
		
		String outputPath = outputDir + System.getProperty("file.separator");

		String keepSeq = "TRUE", stopWords = "FALSE", preserveCase = "TRUE";

		/*
		 * if (removeStopwords){ stopWords = "TRUE"; } if (doLowercase){
		 * preserveCase = "FALSE"; }
		 */

		String[] t2vArgs = { "--input", sourceDir, "--output",
				outputPath + ".mallet", "--keep-sequence", keepSeq,
				"--remove-stopwords", stopWords, "--preserve-case",
				preserveCase };
		String[] v2tArgs = { "--input", outputPath + ".mallet", "--num-topics",
				String.valueOf(numTopics), "--optimize-interval", "20",
				"--output-state", outputPath + ".topic-state.gz",
				"--output-topic-keys", outputPath + ".topic-keys.txt",
				"--output-doc-topics", outputPath + ".topic-composition.txt",
				"--topic-word-weights-file", outputPath + ".word-weights.txt",
				"--word-topic-counts-file", outputPath + ".word-counts.txt" };
		monitor.subTask("Performing text to vector conversion");
		// --input pathway\to\the\directory\with\the\files --output
		// tutorial.mallet --keep-sequence --remove-stopwords
		Text2Vectors.main(t2vArgs);
		monitor.worked(15);
		monitor.subTask("Performing vector to topics conversion");
		// --input tutorial.mallet --num-topics 20 --output-state topic-state.gz
		// --output-topic-keys tutorial_keys.txt --output-doc-topics
		// tutorial_compostion.txt
		Vectors2Topics.main(v2tArgs);
		monitor.worked(5);
		monitor.subTask("Created complete state file " + outputPath
				+ ".topic-state.gz");
		// ConsoleView.printlInConsoleln("Created complete state file "+outputPath+".topic-state.gz");
		// ConsoleView.printlInConsoleln("Created topic keys file "+outputPath+".topic_keys.txt");
		// ConsoleView.printlInConsoleln("Created topic composition file "+outputPath+".topic_composition.txt");
		// ConsoleView.printlInConsoleln("Created topic word counts file "+outputPath+".word_counts.txt");

		monitor.subTask("Convert " + outputPath + ".topic-keys to csv");
		convertKeys2csv(outputPath + ".topic-keys", dateObj);
		monitor.worked(5);

		monitor.subTask("Convert " + outputPath + ".topic-composition to csv");
		convertComposition2csv(outputPath + ".topic-composition", dateObj);
		monitor.worked(5);

		monitor.subTask("Convert " + outputPath + ".word-counts to csv");
		if (wordWeights) {
			convertWeights2csv(outputPath + ".word-weights", dateObj);
		}

		monitor.worked(5);

		deleteFiles(outputPath);
		createRunReport(dateObj);
		monitor.worked(5);
	}
	protected void createRunReport(Date dateObj) {
		TacitUtility.createRunReport(outputDir, "LDA Analysis", dateObj,null);
	}
	private void deleteFiles(String outputPath) {
		File toDel = new File(outputPath + ".topic-state.gz");
		toDel.delete();
		toDel = new File(outputPath + ".word_counts.txt");
		toDel.delete();
		toDel = new File(outputPath + ".topic_keys.txt");
		toDel.delete();
		toDel = new File(outputPath + ".topic_composition.txt");
		toDel.delete();
		toDel = new File(outputPath + ".word_weights.txt");
		toDel.delete();
		toDel = new File(outputPath + ".mallet");
		toDel.delete();
	}
	protected String generateFileName(String fileName, Date dateObj){
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
		return fileName + "-"
				+ df.format(dateObj) + ".csv";
	}
	private void convertWeights2csv(String fileName, Date dateObj) {
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");

		BufferedReader br;
		BufferedWriter bw;
		try {
			br = new BufferedReader(new FileReader(new File(fileName + ".txt")));
			bw = new BufferedWriter(new FileWriter(new File(generateFileName(fileName, dateObj))));

			String currentLine = "Topic,Word,Weight";
			bw.write(currentLine);
			bw.newLine();
			while ((currentLine = br.readLine()) != null) {
				currentLine = currentLine.replace('\t', ',');
				List<String> wordList = Arrays.asList(currentLine.split(","));
				bw.write(wordList.get(0) + "," + wordList.get(1) + ","
						+ wordList.get(2));
				bw.newLine();
			}
			br.close();
			bw.close();

			ConsoleView.printlInConsoleln(fileName + "-" + df.format(dateObj)
					+ ".csv");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	protected String generateKeysFileName(String fileName, Date dateObj){
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
		return fileName + "-"
				+ df.format(dateObj) + ".csv";
	}
	private void convertKeys2csv(String fileName, Date dateObj) {
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");

		BufferedReader br;
		BufferedWriter bw;
		try {
			br = new BufferedReader(new FileReader(new File(fileName + ".txt")));
			bw = new BufferedWriter(new FileWriter(new File(generateKeysFileName(fileName, dateObj))));

			String currentLine = "Topic,Keywords";
			bw.write(currentLine);
			bw.newLine();
			while ((currentLine = br.readLine()) != null) {
				currentLine = currentLine.replace('\t', ',');
				List<String> wordList = Arrays.asList(currentLine.split(","));
				bw.write(wordList.get(0) + "," + wordList.get(2));
				bw.newLine();
			}
			br.close();
			bw.close();
			ConsoleView.printlInConsoleln("Created topic keys file " + fileName
					+ "-" + df.format(dateObj) + ".csv");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	protected String generateCompositionFileName(String fileName, Date dateObj){
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
		return fileName + "-"
				+ df.format(dateObj) + ".csv";
	}
	private void convertComposition2csv(String fileName, Date dateObj) {
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");

		BufferedReader br;
		BufferedWriter bw;
		try {
			br = new BufferedReader(new FileReader(new File(fileName + ".txt")));
			bw = new BufferedWriter(new FileWriter(new File(generateCompositionFileName(fileName, dateObj))));

			String currentLine = br.readLine();
			currentLine = "Number,File Name";
			for (int i = 0; i < numTopics; i++) {
				currentLine = currentLine + "," + "Topic " + i + " Probability";
			}
			bw.write(currentLine);
			bw.newLine();
			while ((currentLine = br.readLine()) != null) {
				currentLine = currentLine.replace('\t', ',');
				List<String> wordList = Arrays.asList(currentLine.split(","));

				HashMap<String, String> probabilities = new HashMap<String, String>();

				for (int i = 2; i < wordList.size(); i = i + 2) {
					probabilities.put(wordList.get(i), wordList.get(i + 1));
				}
				currentLine = wordList.get(0) + "," + wordList.get(1);
				// bw.write(wordList.get(0)+","+wordList.get(1));
				for (int i = 0; i < numTopics; i++) {
					String keyVal = probabilities.get(Integer.toString(i));
					currentLine = currentLine + "," + keyVal;
				}
				bw.write(currentLine);
				bw.newLine();

				ConsoleView.printlInConsoleln(fileName + "-"
						+ df.format(dateObj) + ".csv");
			}
			br.close();
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
