package edu.usc.cssl.nlputils.classify.naivebayes.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.runtime.Platform;

import bsh.EvalError;

public class NaiveBayesClassifier {
	private StringBuilder readMe = new StringBuilder();

	public void train_Test(ArrayList<String> trainingClasses,
			ArrayList<String> testingClasses, String outputDir,
			boolean removeStopwords, boolean doLowercase)
			throws FileNotFoundException, IOException, EvalError {
		Calendar cal = Calendar.getInstance();
		String dateString = "" + (cal.get(Calendar.MONTH) + 1) + "-"
				+ cal.get(Calendar.DATE) + "-" + cal.get(Calendar.YEAR);

		String tempOutputPath = "";
		String tempTrainDirs = "";
		// Create a output filename and comma separated source directories
		for (String classPath : trainingClasses) {
			tempOutputPath += classPath.substring(classPath.lastIndexOf(System
					.getProperty("file.separator")) + 1) + "_";
			tempTrainDirs += classPath + ",";
		}
		String outputPath = outputDir + System.getProperty("file.separator")
				+ tempOutputPath.substring(0, tempOutputPath.length() - 1)
				+ dateString + "-" + System.currentTimeMillis();

		String tempTestDirs = "";
		for (String classPath : testingClasses) {
			tempTestDirs += classPath + ",";
		}

		String keepSeq = "FALSE", stopWords = "FALSE", preserveCase = "TRUE";

		if (removeStopwords) {
			stopWords = "TRUE";
		}
		if (doLowercase) {
			preserveCase = "FALSE";
		}

		// Set up the args
		tempTrainDirs = tempTrainDirs.substring(0, tempTrainDirs.length() - 1);
		String trainDirs[] = tempTrainDirs.split(",");
		tempTestDirs = tempTestDirs.substring(0, tempTestDirs.length() - 1);
		String testDirs[] = tempTestDirs.split(",");

		ArrayList<String> tempT2vArgs = new ArrayList<String>(Arrays.asList(
				"--input", "--output", outputPath + ".train",
				"--keep-sequence", keepSeq, "--remove-stopwords", stopWords,
				"--preserve-case", preserveCase));
		// add all the class paths to the argument
		tempT2vArgs.addAll(1, Arrays.asList(trainDirs));
		// convert the object array to string, this feature is available in only
		// java 1.6 or greater
		String[] t2vArgs = Arrays.copyOf(tempT2vArgs.toArray(),
				tempT2vArgs.toArray().length, String[].class);

		ArrayList<String> tempT2vArgsTest = new ArrayList<String>(Arrays.asList(
				"--input", "--output", outputPath + ".test", "--keep-sequence",
				keepSeq, "--remove-stopwords", stopWords, "--preserve-case",
				preserveCase, "--use-pipe-from", outputPath + ".train"));
		// add all the class paths to the argument
		tempT2vArgsTest.addAll(1, Arrays.asList(testDirs));
		// convert the object array to string, this feature is available in only
		// java 1.6 or greater
		String[] t2vArgs_test = Arrays.copyOf(tempT2vArgsTest.toArray(),
				tempT2vArgsTest.toArray().length, String[].class);

		String[] v2cArgs = { "--training-file", outputPath + ".train",
				"--testing-file", outputPath + ".test", "--output-classifier",
				outputPath + ".out" };

		Text2Vectors.main(t2vArgs);
		System.out.println("Created training file " + outputPath + ".train");
		Text2Vectors.main(t2vArgs_test);
		System.out.println("Created test file " + outputPath + ".test");
		ArrayList<String> result = Vectors2Classify.main(v2cArgs);
		System.out.println("Created classifier output file " + outputPath
				+ ".out");
		System.out.println(result.get(0));
		writeReadMe(outputPath);
	}

	public void classify(ArrayList<String> trainingClasses,
			String classificationInputDir, String classificationOutputDir,
			boolean removeStopwords, boolean doLowercase)
			throws FileNotFoundException, IOException, EvalError {
		System.out.println("Classification starts ..");
		Calendar cal = Calendar.getInstance();
		String dateString = "" + (cal.get(Calendar.MONTH) + 1) + "-"
				+ cal.get(Calendar.DATE) + "-" + cal.get(Calendar.YEAR);

		String tempOutputPath = "";
		String tempSourceDir = "";
		// Create a output filename and comma separated source directories
		for (String classPath : trainingClasses) {
			tempOutputPath += classPath.substring(classPath.lastIndexOf(System
					.getProperty("file.separator")) + 1) + "_";
			tempSourceDir += classPath + ",";
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
		tempSourceDir = tempSourceDir.substring(0, tempSourceDir.length() - 1);
		String sourceDirs[] = tempSourceDir.split(",");

		ArrayList<String> tempT2vArgs = new ArrayList<String>(Arrays.asList(
				"--input", "--output", outputPath + ".train",
				"--keep-sequence", keepSeq, "--remove-stopwords", stopWords,
				"--preserve-case", preserveCase));
		// add all the class paths to the argument
		tempT2vArgs.addAll(1, Arrays.asList(sourceDirs));
		// convert the object array to string, this feature is available in only
		// java 1.6 or greater
		String[] t2vArgs = Arrays.copyOf(tempT2vArgs.toArray(),
				tempT2vArgs.toArray().length, String[].class);

		String[] t2vArgs_test = { "--input", classificationInputDir,
				"--output", outputPath + ".test", "--keep-sequence", keepSeq,
				"--remove-stopwords", stopWords, "--preserve-case",
				preserveCase, "--use-pipe-from", outputPath + ".train" };

		System.out.println("Args :" + Arrays.toString(t2vArgs));
		System.out.println("Args test :" + Arrays.toString(t2vArgs_test));

		// Accuracy is irrelevant to validation. Just classify with raw report
		String[] v2cArgs = { "--training-file", outputPath + ".train",
				"--testing-file", outputPath + ".test", "--output-classifier",
				outputPath + ".out", "--report", "test:raw" };

		System.out.println("Command args :" + Arrays.toString(v2cArgs));

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
		// TODO: check whether this is actually needed
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
