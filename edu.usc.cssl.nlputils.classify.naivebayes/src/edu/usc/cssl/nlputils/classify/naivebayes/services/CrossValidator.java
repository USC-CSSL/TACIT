package edu.usc.cssl.nlputils.classify.naivebayes.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;

import bsh.EvalError;
import edu.usc.cssl.nlputils.common.ui.views.ConsoleView;


public class CrossValidator {
	
	public HashMap<Integer, String> doCross(NaiveBayesClassifier nbc, HashMap<String, List<String>> classPaths, int kValue) throws IOException, EvalError{
		
		HashMap<Integer, String> performance = new HashMap<Integer,  String>();
		
		int[] index = new int[classPaths.size()];
		String tmpLocation = nbc.getTmpLocation();
		if(!new File(tmpLocation).exists()) {
			new File(tmpLocation).mkdir();	
		}
		
		String trainDir = tmpLocation + File.separator + "Train";
		if(!new File(trainDir).exists()) {
			new File(trainDir).mkdir();	
		}

		String testDir = tmpLocation + File.separator + "Test";
		if(!new File(testDir).exists()) {
			new File(testDir).mkdir();	
		}	
		
		ConsoleView.printlInConsoleln("---------- Cross Validation Starts ------------");
		for (int i=1; i<=kValue; i++) {
			int count = 0;
			ConsoleView.printlInConsoleln ("------ Trial "+ i +"------");
			ArrayList<String> trainingDataPaths = new ArrayList<String>();
			ArrayList<String> testingDataPaths = new ArrayList<String>();
			
			for(String path : classPaths.keySet()) {
				List<String> selectedFiles =  classPaths.get(path);
				int numFiles = classPaths.get(path).size();
				int trainingSetSize = (int)Math.floor(0.90 * numFiles);
				int testingSetSize = numFiles - trainingSetSize;
				
				File[] trainFiles = new File[trainingSetSize];
				File[] testFiles = new File[testingSetSize];
				
				int currIndex = index[count];
				
				String className = new File(path).getName();
				String tempTrainDir = trainDir + File.separator + className;
				System.out.println("Training data dir :"+ tempTrainDir);
				if(new File(tempTrainDir).exists()) {
					nbc.purgeDirectory(new File(tempTrainDir)); 
				}
				new File(tempTrainDir).mkdir();
				
				for (int num = 0; num < trainingSetSize; num++) {
					trainFiles[num]= new File(selectedFiles.get(currIndex));
					//new File(trainFiles[num].getAbsolutePath(), new File(tempTrainDir + File.separator + trainFiles[num].getName()).getAbsolutePath());
					FileUtils.copyFileToDirectory(trainFiles[num], new File(tempTrainDir));
					//Files.copy(trainFiles[num].toPath(), new File(tempTrainDir + File.separator + trainFiles[num].getName()).toPath(), new CopyOption[] { REPLACE_EXISTING });
					currIndex++;
					if(currIndex >= numFiles)
						currIndex = 0;
				}
				String tempTestDir = testDir + File.separator + className;
				System.out.println("Testing data dir :"+ tempTestDir);

				if(new File(tempTestDir).exists()) {
					nbc.purgeDirectory(new File(tempTestDir)); 
				}
				new File(tempTestDir).mkdir();
			
				for (int num = 0; num < testingSetSize; num++) {
					testFiles[num] = new File(selectedFiles.get(currIndex));
					//new File(testFiles[num].getAbsolutePath(), new File(tempTestDir + File.separator + testFiles[num].getName()).getAbsolutePath());
					FileUtils.copyFileToDirectory(testFiles[num], new File(tempTestDir));
					//Files.copy(testFiles[num].toPath(), new File(tempTestDir+ File.separator + testFiles[num].getName()).toPath(), new CopyOption[] { REPLACE_EXISTING });					
					currIndex++;
					if(currIndex >= numFiles)
						currIndex=0;
				}
				//set training and testing paths 
				trainingDataPaths.add(tempTrainDir);
				testingDataPaths.add(tempTestDir);
				
				// Clear required globals like dfmap?
				index[count] = index[count] + numFiles-trainingSetSize;
				if (index[count] >= numFiles){
					index[count] = index[count] - numFiles;
				}
				count++;
			}

			System.out.println("Training data paths ..");
			for(String s : trainingDataPaths)
				System.out.println(s);
			
			System.out.println("Testing data paths ..");
			for(String s : testingDataPaths)
				System.out.println(s);
			
			// Perform classification
			String result = nbc.predict(trainingDataPaths, testingDataPaths, false, false);
			performance.put(i, result);
		}
		ConsoleView.printlInConsoleln("---------- Cross Validation Finished ------------");
		nbc.purgeDirectory(new File(trainDir));
		nbc.purgeDirectory(new File(testDir));
		return performance;
	}
}
