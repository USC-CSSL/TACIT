package test.edu.usc.cssl.tacit.wordcount.cooccurrence.services;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.usc.cssl.tacit.wordcount.cooccurrence.services.CooccurrenceAnalysis;

public class Cooccurence_Test {
	final String directoryPath = new File("TestData").getAbsolutePath();
	@Test
	public void invokeCooccurrenceTest() throws IOException {
		
		//Files to be tested on
		List<String> files = new ArrayList<String>();
		files.add(directoryPath + File.separator + "CooccurenceInputFile1.txt");
		files.add(directoryPath + File.separator + "CooccurenceInputFile2.txt");
		
		//Seed File
		List<String> seedList = new ArrayList<String>();
		seedList.add(directoryPath + File.separator + "CooccurenceSeedFileInput.txt");
		
		//Threshold and window size and build matrix flag
		String windowSizeStr = "5";
		String thresholdLimit = "2";
		boolean isBuildMatrix = true;
		
	
		CooccurrenceAnalysis cooccurrenceAnalysis  = new CooccurrenceAnalysis() {
			
			@Override
			protected void generateRunReport(){}
			
			@Override
			protected String generateWindowFileName(Date currTime){
				return "GeneratedWindowOutput.csv";
			}

			@Override
			protected String generateMatrixFileName(Date currTime){
				return "GeneratedCooccurenceMatrixOutput.csv";
			}
		};
		boolean result = cooccurrenceAnalysis.invokeCooccurrence(files, seedList.get(0),directoryPath, windowSizeStr,
						thresholdLimit, isBuildMatrix, new NullProgressMonitor());
		//Test Case 1
		assertEquals("Checking if the test ran successfully", true, result);
		
		//Checking generated and expected output files.
		File generatedWindowOutput = new File(directoryPath + File.separator + "GeneratedWindowOutput.csv");
		File generatedCooccurenceMatrixOutput = new File(directoryPath + File.separator + "GeneratedCooccurenceMatrixOutput.csv");
		File expectedWindowOutput = new File(directoryPath + File.separator + "ExpectedWindowOutput.csv");
		File expectedCooccurenceMatrixOutput = new File(directoryPath + File.separator + "ExpectedCooccurenceMatrixOutput.csv");
		
		BufferedReader reader = new BufferedReader(new FileReader(generatedWindowOutput));
		String line = "";
		String generatedOutput = "";
		String expectedOutput = "";
		while((line = reader.readLine())!= null)
			generatedOutput += line;
		reader.close();
		reader = new BufferedReader(new FileReader(expectedWindowOutput));
		while((line = reader.readLine())!= null)
			expectedOutput += line;
		reader.close();

		//Test case 2
		assertEquals("Comparing Window Output", expectedOutput, generatedOutput);

		
		reader = new BufferedReader(new FileReader(generatedCooccurenceMatrixOutput));
		line = "";
		generatedOutput = "";
		expectedOutput = "";
		while((line = reader.readLine())!= null)
			generatedOutput += line;
		reader.close();
		reader = new BufferedReader(new FileReader(expectedCooccurenceMatrixOutput));
		while((line = reader.readLine())!= null)
			expectedOutput += line;
		reader.close();
		
		//Test case 3
		assertEquals("Comparing cooccurence matrix", expectedOutput, generatedOutput);
	}
}