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

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

import bsh.EvalError;
import edu.usc.cssl.nlputils.common.ui.views.ConsoleView;

public class NaiveBayesClassifier {
	private StringBuilder readMe = new StringBuilder();
	private String tmpLocation;
	private String outputDir; 
	
	public NaiveBayesClassifier() {
		this.tmpLocation = System.getProperty("user.dir") + File.separator + "NB_Classifier";
		this.outputDir = this.tmpLocation + File.separator + "Output";
		//this.tmpLocation = "F:\\NLP\\Naive Bayes Classifier\\2 Class Analysis\\preprocess\\NB_Classifier";
		
		String outputDir = this.outputDir;
		if(!new File(outputDir).exists()) {
			new File(outputDir).mkdirs();	
		}
	}
	
	public String getTmpLocation() {
		return this.tmpLocation;
	}
	public String predict(ArrayList<String> trainingClasses, ArrayList<String> testingClasses, boolean removeStopwords, boolean doLowercase) throws FileNotFoundException, IOException, EvalError {
		Calendar cal = Calendar.getInstance();
		String dateString = "" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DATE) + "-" + cal.get(Calendar.YEAR);

		if(trainingClasses.isEmpty() || testingClasses.isEmpty())
			return null;
		
		String outputDir = this.outputDir;
		
		String tempOutputPath = "";
		String tempTrainDirs = "";
		// Create a output filename and comma separated source directories
		for (String classPath : trainingClasses) {
			tempOutputPath += classPath.substring(classPath.lastIndexOf(System.getProperty("file.separator")) + 1) + "_";
			tempTrainDirs += classPath + ",";
		}
		String outputPath = outputDir + System.getProperty("file.separator")+ tempOutputPath.substring(0, tempOutputPath.length() - 1) + dateString + "-" + System.currentTimeMillis();

		String tempTestDirs = "";
		for (String classPath : testingClasses) {
			if (!classPath.isEmpty()) 
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

		ArrayList<String> tempT2vArgs = new ArrayList<String>(Arrays.asList("--input", "--output", outputPath + ".train","--keep-sequence", keepSeq, "--remove-stopwords", stopWords,"--preserve-case", preserveCase));
		// add all the class paths to the argument
		tempT2vArgs.addAll(1, Arrays.asList(trainDirs));
		// convert the object array to string, this feature is available in only java 1.6 or greater
		String[] t2vArgs = Arrays.copyOf(tempT2vArgs.toArray(), tempT2vArgs.toArray().length, String[].class);

		ArrayList<String> tempT2vArgsTest = new ArrayList<String>(Arrays.asList("--input", "--output", outputPath + ".test","--keep-sequence", keepSeq, "--remove-stopwords",stopWords, "--preserve-case", preserveCase,"--use-pipe-from", outputPath + ".train"));
		// add all the class paths to the argument
		tempT2vArgsTest.addAll(1, Arrays.asList(testDirs));
		String[] t2vArgs_test = Arrays.copyOf(tempT2vArgsTest.toArray(),tempT2vArgsTest.toArray().length, String[].class);
		
		String[] v2cArgs = { "--training-file", outputPath + ".train","--testing-file", outputPath + ".test", "--output-classifier",outputPath + ".out" };

		Text2Vectors.main(t2vArgs);
		System.out.println("Created training file " + outputPath + ".train");
		Text2Vectors.main(t2vArgs_test);
		System.out.println("Created test file " + outputPath + ".test");
		ArrayList<String> result = Vectors2Classify.main(v2cArgs);
		ConsoleView.printlInConsoleln("\nCreated classifier output file " + outputPath+ ".out");
		System.out.println(result.get(0));
		writeReadMe(outputPath);
		return result.get(0);
	}

	
	public void classify(ArrayList<String> trainingClasses, String classificationInputDir, String classificationOutputDir, boolean removeStopwords, boolean doLowercase) throws FileNotFoundException, IOException, EvalError {
		ConsoleView.printlInConsoleln("Classification starts ..");
		Calendar cal = Calendar.getInstance();
		String dateString = "" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DATE) + "-" + cal.get(Calendar.YEAR);
		if(trainingClasses.isEmpty() || classificationInputDir.isEmpty() || classificationOutputDir.isEmpty()) 
			return;
		String tempOutputPath = "";
		String tempSourceDir = "";
		// Create a output filename and comma separated source directories
		for (String classPath : trainingClasses) {
			tempOutputPath += classPath.substring(classPath.lastIndexOf(System.getProperty("file.separator")) + 1) + "_";
			tempSourceDir += classPath + ",";
		}
		String outputPath = classificationOutputDir + System.getProperty("file.separator") + tempOutputPath.substring(0, tempOutputPath.length() - 1) + dateString + "-" + System.currentTimeMillis();

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

		ArrayList<String> tempT2vArgs = new ArrayList<String>(Arrays.asList("--input", "--output", outputPath + ".train", "--keep-sequence", keepSeq, "--remove-stopwords", stopWords, "--preserve-case", preserveCase));
		// add all the class paths to the argument
		tempT2vArgs.addAll(1, Arrays.asList(sourceDirs));
		// convert the object array to string, this feature is available in only java 1.6 or greater
		String[] t2vArgs = Arrays.copyOf(tempT2vArgs.toArray(),tempT2vArgs.toArray().length, String[].class);
		String[] t2vArgs_test = { "--input", classificationInputDir, "--output", outputPath + ".test", "--keep-sequence", keepSeq, "--remove-stopwords", stopWords, "--preserve-case", preserveCase, "--use-pipe-from", outputPath + ".train" };
		String[] v2cArgs = { "--training-file", outputPath + ".train", "--testing-file", outputPath + ".test", "--output-classifier", outputPath + ".out", "--report", "test:raw"};

		System.out.println("Args :" + Arrays.toString(t2vArgs));
		System.out.println("Args test :" + Arrays.toString(t2vArgs_test));
		System.out.println("Command args :" + Arrays.toString(v2cArgs));

		Text2Vectors.main(t2vArgs);
		System.out.println("Created training file " + outputPath + ".train");
		Text2Vectors.main(t2vArgs_test);
		System.out.println("Created validation file " + outputPath + ".test");
		ArrayList<String> result = Vectors2Classify.main(v2cArgs);
		ConsoleView.printlInConsoleln("\nCreated classifier output file " + outputPath + ".out");

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPath + "_output.csv")));
		bw.write("File, Predicted Class, Other Classes\n");
		for (String s : result)
			bw.write(s + "\n");
		bw.close();

		ConsoleView.printlInConsoleln("Created prediction CSV file " + outputPath + "_output.csv");
		writeReadMe(outputPath);
	}
	
	public void purgeDirectory(File dir) {
		if(null == dir || !dir.exists() || !dir.isDirectory()) 
			return;
		if(dir.listFiles().length>0) {
		    for (File file: dir.listFiles()) {
		        if (file.isDirectory()) 
		        	purgeDirectory(file);
		        file.delete();
		    }
		}
	}
	
	public void createTempDirectories(HashMap<String, List<String>> classPaths, ArrayList<String> trainingDataPaths, IProgressMonitor monitor) throws IOException {
		String tmpLocation = this.tmpLocation;
		if(!new File(tmpLocation).exists()) {
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
			System.out.println("Training data dir :"+ tempTrainDir);
			if(new File(tempTrainDir).exists()) {
				purgeDirectory(new File(tempTrainDir)); 
			}
			new File(tempTrainDir).mkdirs();
			
			for (int num = 0; num<numFiles; num++) {
				files[num] = new File(classFiles.get(num));
				//new File(files[num].getAbsolutePath(), new File(tempTrainDir + File.separator + files[num].getName()).getAbsolutePath());
				//Files.copy(files[num].toPath(), new File(tempTrainDir+ File.separator + files[num].getName()).toPath(), new CopyOption[] { REPLACE_EXISTING });
				FileUtils.copyFileToDirectory(files[num], new File(tempTrainDir));
			}
			trainingDataPaths.add(new File(tempTrainDir).getAbsolutePath());	
			monitor.worked(1); // processing of each directory/class
		}
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

	public void deleteTempDirectories(ArrayList<String> trainingDataPaths) {
		for(String s : trainingDataPaths) {
			System.out.println("Cleaning directory "+ s);
			purgeDirectory(new File(s));
			new File(s).delete();
		}
		//purgeDirectory(new File(this.tmpLocation));
		//new File(this.tmpLocation).delete();
	}

	public void selectAllFiles(String DirPath, ArrayList<String> files) {
		File dir = new File(DirPath);
		for(File f : dir.listFiles()) {
			if(f.isDirectory()) {
				selectAllFiles(f.getAbsolutePath(), files);
			} else {
				files.add(f.getAbsolutePath());
			}
		}
	}
}
