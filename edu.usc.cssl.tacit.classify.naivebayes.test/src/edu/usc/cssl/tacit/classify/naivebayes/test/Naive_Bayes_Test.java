package edu.usc.cssl.tacit.classify.naivebayes.test;


import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.usc.cssl.tacit.classify.naivebayes.services.NaiveBayesClassifier;
import edu.usc.cssl.tacit.classify.naivebayes.weka.NaiveBayesClassifierWeka;

public class Naive_Bayes_Test {
	HashMap<String, Double> expectedHashMap;
	final String directoryPath = new File("TestData").getAbsolutePath();
	@Test
	public void naiveBayesCrossValidateTest() throws IOException {
		int kValue = 4;
		Date dateObj = Calendar.getInstance().getTime();
		final NaiveBayesClassifier nbc = new NaiveBayesClassifier(){
		};
		Map<String, List<String>> classPaths = new HashMap<String, List<String>>();
		ArrayList<String> list1 = new ArrayList<String>();
		list1.add(directoryPath+ File.separator +"Data1"+ File.separator +"File1.txt");
		list1.add(directoryPath+ File.separator +"Data1"+ File.separator +"File2.txt");
		ArrayList<String> list2 = new ArrayList<String>();
		list2.add(directoryPath+ File.separator +"Data2"+ File.separator +"File3.txt");
		list2.add(directoryPath+ File.separator +"Data2"+ File.separator +"File4.txt");
		classPaths.put(directoryPath+ File.separator +"Data1", list1);
		classPaths.put(directoryPath+ File.separator +"Data2", list2);
		ArrayList<String> trainingDataPaths = new ArrayList<String>();
		trainingDataPaths.add(directoryPath + File.separator +"Data1");
		trainingDataPaths.add(directoryPath + File.separator +"Data2");
		Exception exception = null;
		try {
			NaiveBayesClassifierWeka cv = new NaiveBayesClassifierWeka(classPaths);
			cv.initializeInstances();
			cv.doCrossValidate(kValue, new NullProgressMonitor(), dateObj);
		} catch (Exception e) {
			exception = e;
			System.out.println(e);
		}		

		System.out.println(exception);
		assertEquals("Checking if any exception occurred", null, exception);
	}
	@Test
	public void naiveBayesDoClassifyTest() throws IOException {
		int kValue = 2;
		Date dateObj = Calendar.getInstance().getTime();
		final NaiveBayesClassifier nbc = new NaiveBayesClassifier(){
		};
		Map<String, List<String>> classPaths = new HashMap<String, List<String>>();
		ArrayList<String> list1 = new ArrayList<String>();
		list1.add(directoryPath+ File.separator +"Data1"+ File.separator +"File1.txt");
		ArrayList<String> list2 = new ArrayList<String>();
		list2.add(directoryPath+ File.separator +"Data2"+ File.separator +"File3.txt");
		classPaths.put(directoryPath+ File.separator +"Data1", list1);
		classPaths.put(directoryPath+ File.separator +"Data2", list2);
		ArrayList<String> trainingDataPaths = new ArrayList<String>();
		trainingDataPaths.add(directoryPath + File.separator +"Data1");
		trainingDataPaths.add(directoryPath + File.separator +"Data2");
		try {
			NaiveBayesClassifierWeka cv = new NaiveBayesClassifierWeka(classPaths){
				protected String generateOutputFileName(String classificationOutputDir, Date dateObj){
					return directoryPath + File.separator + "GeneratedClassificationOutput.csv";
				}
			};
			cv.initializeInstances();
			cv.doCrossValidate(kValue, new NullProgressMonitor(), dateObj);
			cv.doClassify(directoryPath+ File.separator +"ClassificationInputData", directoryPath,  new NullProgressMonitor(), dateObj);
		} catch (Exception e) {
		}		
		File generatedNaiveBayesOutput = new File(directoryPath + File.separator
				+ "GeneratedClassificationOutput.csv");
		File expectedNaiveBayesOutput = new File(directoryPath + File.separator
				+ "ExpectedClassificationOutput.csv");
		BufferedReader reader = new BufferedReader(new FileReader(generatedNaiveBayesOutput));
		String line = "";
		String generatedOutput = "";
		String expectedOutput = "";
		while((line = reader.readLine())!= null){
			String temp[] = line.split(",");
			String x = temp[0].substring(temp[0].lastIndexOf("edu.usc.cssl.tacit.classify.naivebayes.test"));
			String y = temp[1].substring(temp[1].lastIndexOf("edu.usc.cssl.tacit.classify.naivebayes.test"));
			generatedOutput += (x+y);
		}
		reader.close();
		reader = new BufferedReader(new FileReader(expectedNaiveBayesOutput));
		while((line = reader.readLine())!= null){
			String temp[] = line.split(",");
			String x = temp[0].substring(temp[0].lastIndexOf("edu.usc.cssl.tacit.classify.naivebayes.test"));
			String y = temp[1].substring(temp[1].lastIndexOf("edu.usc.cssl.tacit.classify.naivebayes.test"));
			expectedOutput += (x+y);
		}
		reader.close();
		
		assertEquals("Comparing naive bayes output", expectedOutput, generatedOutput);
	}
}
