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
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.Platform;

import bsh.EvalError;

public class NaiveBayesClassifier {
	private StringBuilder readMe = new StringBuilder();
	private String tmpLocation;
	
	public NaiveBayesClassifier() {
		//this.tmpLocation = System.getProperty("user.dir") + File.separator + "NB_Classifier";
		this.tmpLocation = "F:\\NLP\\Naive Bayes Classifier\\2 Class Analysis\\preprocess\\NB_Classifier";
	}
	public void classify(ArrayList<String> trainingClasses, String classificationInputDir, String classificationOutputDir, boolean removeStopwords, boolean doLowercase) throws FileNotFoundException, IOException, EvalError {
		ConsoleView.writeInConsole("Classification starts ..");
		Calendar cal = Calendar.getInstance();
		String dateString = "" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DATE) + "-" + cal.get(Calendar.YEAR);

		String tempOutputPath = "";
		String tempSourceDir = "";
		// Create a output filename and comma separated source directories
		for (String classPath : trainingClasses) {
			tempOutputPath += classPath.substring(classPath.lastIndexOf(System .getProperty("file.separator")) + 1) + "_";
			tempSourceDir += classPath + ",";
		}
		String outputPath = classificationOutputDir + System.getProperty("file.separator") + tempOutputPath.substring(0, tempOutputPath.length() - 1) + dateString + "-" + System.currentTimeMillis();

		String keepSeq = "FALSE", stopWords = "FALSE", preserveCase = "TRUE";
		if (removeStopwords) 
			stopWords = "TRUE";
		if (doLowercase)
			preserveCase = "FALSE";

		// Set up the args
		tempSourceDir = tempSourceDir.substring(0, tempSourceDir.length() - 1);
		String sourceDirs[] = tempSourceDir.split(",");

		ArrayList<String> tempT2vArgs = new ArrayList<String>(Arrays.asList( "--input", "--output", outputPath + ".train", "--keep-sequence", keepSeq, "--remove-stopwords", stopWords,"--preserve-case", preserveCase));
		tempT2vArgs.addAll(1, Arrays.asList(sourceDirs)); // add all the class paths to the argument
		// convert the object array to string, this feature is available in only java 1.6 or greater
		String[] t2vArgs = Arrays.copyOf(tempT2vArgs.toArray(), tempT2vArgs.toArray().length, String[].class);
		String[] t2vArgs_test = { "--input", classificationInputDir, "--output", outputPath + ".test", "--keep-sequence", keepSeq, "--remove-stopwords", stopWords, "--preserve-case", preserveCase, "--use-pipe-from", outputPath + ".train" };
		String[] v2cArgs = { "--training-file", outputPath + ".train", "--testing-file", outputPath + ".test", "--output-classifier", outputPath + ".out", "--report", "test:raw"};

		ConsoleView.writeInConsole("Command args :" + Arrays.toString(v2cArgs));

		Text2Vectors.main(t2vArgs);
		ConsoleView.writeInConsole("Created training file " + outputPath + ".train");
		Text2Vectors.main(t2vArgs_test);
		ConsoleView.writeInConsole("Created validation file " + outputPath + ".test");
		ArrayList<String> result = Vectors2Classify.main(v2cArgs);
		ConsoleView.writeInConsole("Created classifier output file " + outputPath + ".out");
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPath + "_output.csv")));
		bw.write("File, Predicted Class, Other Classes\n");
		for (String s : result) 
			bw.write(s + "\n");
		bw.close();

		ConsoleView.writeInConsole("Created prediction CSV file " + outputPath + "_output.csv");
		writeReadMe(outputPath);
	}
	
	public void crossValidate(ArrayList<String> trainingClasses, String outputDir, boolean removeStopwords, boolean doLowercase, int kValue) throws FileNotFoundException, IOException, EvalError {
		ConsoleView.writeInConsole("Classification starts ..");
		Calendar cal = Calendar.getInstance();
		String dateString = "" + (cal.get(Calendar.MONTH) + 1) + "-"+ cal.get(Calendar.DATE) + "-" + cal.get(Calendar.YEAR);

		String tempOutputPath = "";
		String tempSourceDir = "";
		
		// Create a output filename and comma separated source directories
		for (String classPath : trainingClasses) {
			tempOutputPath += classPath.substring(classPath.lastIndexOf(System.getProperty("file.separator")) + 1) + "_";
			tempSourceDir += classPath + ",";
		}
		
		String keepSeq = "FALSE", stopWords = "FALSE", preserveCase = "TRUE";
		if (removeStopwords) 
			stopWords = "TRUE";
		if (doLowercase)
			preserveCase = "FALSE";
		
		String outputPath = outputDir + System.getProperty("file.separator") + tempOutputPath.substring(0, tempOutputPath.length() - 1) + dateString + "-" + System.currentTimeMillis();

		tempSourceDir = tempSourceDir.substring(0, tempSourceDir.length() - 1);
		String sourceDirs[] = tempSourceDir.split(",");

		ArrayList<String> tempT2vArgs = new ArrayList<String>(Arrays.asList("--input", "--output", outputPath + ".train", "--keep-sequence", keepSeq, "--remove-stopwords", stopWords, "--preserve-case", preserveCase));
		// add all the class paths to the argument
		tempT2vArgs.addAll(1, Arrays.asList(sourceDirs));
		// convert the object array to string, this feature is available in only java 1.6 or greater
		String[] t2vArgs = Arrays.copyOf(tempT2vArgs.toArray(), tempT2vArgs.toArray().length, String[].class);
		
		Text2Vectors.main(t2vArgs);
		ConsoleView.writeInConsole("Command args :" + Arrays.toString(t2vArgs));
		ConsoleView.writeInConsole("Created training file " + outputPath + ".train");
		
		ArrayList<String> tempT2vArgsTest = new ArrayList<String>(Arrays.asList("--input", "--output", outputPath + ".test", "--keep-sequence", keepSeq, "--remove-stopwords", stopWords, "--preserve-case", preserveCase, "--use-pipe-from", outputPath + ".train"));
		// add all the class paths to the argument
		tempT2vArgsTest.addAll(1, Arrays.asList(sourceDirs));
		// convert the object array to string, this feature is available in only java 1.6 or greater
		String[] t2vArgs_test = Arrays.copyOf(tempT2vArgsTest.toArray(), tempT2vArgsTest.toArray().length, String[].class);
				
		Text2Vectors.main(t2vArgs_test);
		ConsoleView.writeInConsole("Command args :" + Arrays.toString(t2vArgs_test));
		ConsoleView.writeInConsole("Created test file " + outputPath + ".test");
				
		String[] v2cArgs = { "--input", outputPath + ".train", "--training-portion", String.valueOf(0.6), "--cross-validation", String.valueOf(kValue)};
		ConsoleView.writeInConsole("Command args :" + Arrays.toString(v2cArgs));
		
		Vectors2Classify.main(v2cArgs);
		return;
	}
	
	private void purgeDirectory(File dir) {
		if(dir.listFiles().length>0) {
		    for (File file: dir.listFiles()) {
		        if (file.isDirectory()) 
		        	purgeDirectory(file);
		        file.delete();
		    }
		}
	}
	
	public void createTempDirectories(HashMap<String, List<String>> classPaths, ArrayList<String> trainingDataPaths) throws IOException {
		String tmpLocation = this.tmpLocation;
		if(new File(tmpLocation).exists()) {
			purgeDirectory(new File(tmpLocation)); 
		} else {
			new File(tmpLocation).mkdirs();	
		}
		
		String tempTrainDir = new String();
		for (String key : classPaths.keySet()) {
			List<String> classFiles = classPaths.get(key);
			int numFiles = classPaths.get(key).size();
			File[] files = new File[numFiles];	
			
			// Create respective class directories
			String className = new File(key).getName();
			tempTrainDir = tmpLocation + File.separator + className;
			ConsoleView.writeInConsole("Training data dir :"+ tempTrainDir);
			if(new File(tempTrainDir).exists()) {
				purgeDirectory(new File(tempTrainDir)); 
			}
			new File(tempTrainDir).mkdirs();
			
			for (int num = 0; num<numFiles; num++) {
				files[num] = new File(classFiles.get(num));
				Files.copy(files[num].toPath(), new File(tempTrainDir+ File.separator + files[num].getName()).toPath(), new CopyOption[] { REPLACE_EXISTING });
			}
			trainingDataPaths.add(new File(tempTrainDir).getAbsolutePath());	
		}
	}
	
	public void doCross(ArrayList<String> trainingDataPaths, String outputPath, boolean removeStopwords, boolean doLowercase, int kValue) throws FileNotFoundException, IOException, EvalError {
		crossValidate(trainingDataPaths, outputPath, false, false, kValue);
		//delete the temp files
		/*String tmpLocation = this.tmpLocation;
		if(new File(tmpLocation).exists()) {
			purgeDirectory(new File(tmpLocation));
			Files.delete(new File(tmpLocation).toPath());
		}*/
	}
	
	public void writeReadMe(String location) {
		// TODO: check whether this is actually needed
		File readme = new File(location + "_README.txt");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(readme));
			String plugV = Platform .getBundle("edu.usc.cssl.nlputils.classify.naivebayes").getHeaders().get("Bundle-Version");
			String appV = Platform .getBundle("edu.usc.cssl.nlputils.classify.naivebayes").getHeaders().get("Bundle-Version");
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
