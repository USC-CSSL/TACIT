package edu.usc.cssl.nlputils.classify.naivebayes.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.runtime.Platform;

import bsh.EvalError;

public class NaiveBayesClassifier {
	private StringBuilder readMe = new StringBuilder();

	public void classify(ArrayList<String> trainingClasses,
			String classificationInputDir, String classificationOutputDir,
			boolean removeStopwords, boolean doLowercase)
			throws FileNotFoundException, IOException, EvalError {
		System.out.println("Classification starts ..");
		Calendar cal = Calendar.getInstance();
		String dateString = "" + (cal.get(Calendar.MONTH) + 1) + "-"
				+ cal.get(Calendar.DATE) + "-" + cal.get(Calendar.YEAR);

		String tempOutputPath = null;
		String tempSourceDir = null;
		// Create a output filename and comma separated source directories
		for (String classPath : trainingClasses) {
			tempOutputPath += classPath.substring(classPath.lastIndexOf(System
					.getProperty("file.separator")) + 1) + "_";
			tempSourceDir = classPath + ",";
		}
		String outputPath = classificationOutputDir
				+ System.getProperty("file.separator")
				+ tempOutputPath.substring(0, tempOutputPath.length() - 1)
				+ dateString + "-" + System.currentTimeMillis();

		String keepSeq = "FALSE", stopWords = "FALSE", preserveCase = "TRUE";
		if (removeStopwords) {
			stopWords = "TRUE";
		}
		if (doLowercase) {
			preserveCase = "FALSE";
		}

		// Set up the args
		String[] t2vArgs = { "--input",
				tempSourceDir.substring(0, tempSourceDir.length() - 1),
				"--output", outputPath + ".train", "--keep-sequence", keepSeq,
				"--remove-stopwords", stopWords, "--preserve-case",
				preserveCase };
		String[] t2vArgs_test = { "--input", classificationInputDir,
				"--output", outputPath + ".test", "--keep-sequence", keepSeq,
				"--remove-stopwords", stopWords, "--preserve-case",
				preserveCase, "--use-pipe-from", outputPath + ".train" };

		// Accuracy is irrelevant to validation. Just classify with raw report
		String[] v2cArgs = { "--training-file", outputPath + ".train",
				"--testing-file", outputPath + ".test", "--output-classifier",
				outputPath + ".out", "--report", "test:raw" };
		Text2Vectors.main(t2vArgs);
		System.out.println("Created training file " + outputPath + ".train");
		Text2Vectors.main(t2vArgs_test);
		System.out.println("Created validation file " + outputPath + ".test");
		ArrayList<String> result = Vectors2Classify.main(v2cArgs);
		System.out.println("Created classifier output file " + outputPath
				+ ".out");

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				outputPath + "_output.csv")));
		bw.write("File, Predicted Class, Other Classes\n");
		for (String s : result)
			bw.write(s + "\n");
		bw.close();

		System.out.println("Created prediction CSV file " + outputPath
				+ "_output.csv");
		writeReadMe(outputPath);
	}

	public void writeReadMe(String location) {
		File readme = new File(location + "_README.txt");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(readme));
			String plugV = Platform
					.getBundle("edu.usc.cssl.nlputils.classify.naivebayes")
					.getHeaders().get("Bundle-Version");
			String appV = Platform
					.getBundle("edu.usc.cssl.nlputils.classify.naivebayes")
					.getHeaders().get("Bundle-Version");
			Date date = new Date();
			bw.write("Naive Bayes Output\n------------------\n\nApplication Version: "
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
