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

		List<String> files = new ArrayList<String>();
		files.add(directoryPath + File.separator
				+ "CooccurenceInputFile1.txt");
		files.add(directoryPath + File.separator + "CooccurenceInputFile2.txt");
		List<String> seedList = new ArrayList<String>();
		seedList.add(directoryPath + File.separator
				+ "CooccurenceSeedFileInput.txt");
		String windowSizeStr = "5";
		String thresholdLimit = "1";
		boolean isBuildMatrix = true;
		boolean result = new CooccurrenceAnalysis() {
			@Override
			protected void generateRunReport(){}
			/*@Override
			protected String generateSeedComboStatsFileName(Date currTime){
				return "GeneratedCooccurenceSeedFrequenciesOutput.csv";
			}
			@Override
			protected String generatePhrasesFileName(Date currTime){
				return "GeneratedCooccurencePhrasesOutput.csv";
			}*/
			@Override
			protected String generateMatrixFileName(Date currTime){
				return "GeneratedCooccurenceMatrixOutput.csv";
			}
		}
				.invokeCooccurrence(files, seedList.get(0),
						directoryPath, windowSizeStr,
						thresholdLimit, isBuildMatrix, new NullProgressMonitor());

		assertEquals("Checking if the test ran successfully", true, result);
		File generatedCooccurenceSeedFrequenciesOutput = new File(directoryPath + File.separator
				+ "GeneratedCooccurenceSeedFrequenciesOutput.csv");
		File generatedCooccurencePhrasesOutput = new File(directoryPath + File.separator
				+ "GeneratedCooccurencePhrasesOutput.csv");
		File generatedCooccurenceMatrixOutput = new File(directoryPath + File.separator
				+ "GeneratedCooccurenceMatrixOutput.csv");
		File expectedCooccurenceSeedFrequenciesOutput = new File(directoryPath + File.separator
				+ "ExpectedCooccurenceSeedFrequenciesOutput.csv");
		File expectedCooccurencePhrasesOutput = new File(directoryPath + File.separator
				+ "ExpectedCooccurencePhrasesOutput.csv");
		File expectedCooccurenceMatrixOutput = new File(directoryPath + File.separator
				+ "ExpectedCooccurenceMatrixOutput.csv");
		BufferedReader reader = new BufferedReader(new FileReader(generatedCooccurenceSeedFrequenciesOutput));
		String line = "";
		String generatedOutput = "";
		String expectedOutput = "";
		while((line = reader.readLine())!= null)
			generatedOutput += line;
		reader.close();
		reader = new BufferedReader(new FileReader(expectedCooccurenceSeedFrequenciesOutput));
		while((line = reader.readLine())!= null)
			expectedOutput += line;
		reader.close();

		assertEquals("Comparing frequencies", expectedOutput, generatedOutput);
		reader = new BufferedReader(new FileReader(generatedCooccurencePhrasesOutput));
		line = "";
		generatedOutput = "";
		expectedOutput = "";
		while((line = reader.readLine())!= null)
			generatedOutput += line;
		reader.close();
		reader = new BufferedReader(new FileReader(expectedCooccurencePhrasesOutput));
		while((line = reader.readLine())!= null)
			expectedOutput += line;
		reader.close();

		assertEquals("Comparing cooccurence phrases", expectedOutput, generatedOutput);
		
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

		assertEquals("Comparing cooccurence matrix", expectedOutput, generatedOutput);
	}
}