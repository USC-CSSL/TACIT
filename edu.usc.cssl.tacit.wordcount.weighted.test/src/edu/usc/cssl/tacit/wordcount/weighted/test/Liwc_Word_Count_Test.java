package edu.usc.cssl.tacit.wordcount.weighted.test;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.uc.cssl.tacit.wordcount.weighted.services.WordCountApi;
import edu.usc.cssl.tacit.common.ui.utility.TacitUtil;

public class Liwc_Word_Count_Test{
	
	// TODO: Tacit Utility run report has been disabled for all the tests
	// NOTE: Only an empty string is passed to the doStopWords parameter from the UI which translates to the respective flag becoming false 
	// Hence no test cases have been written for the stopWordSetBuild method because it is never called.
	
	final boolean isLiwcStemming = false; // no other value possible
	final boolean isSnowBall = false;// no other value possible
	final boolean isStemDic = false; // no other value possible
	final String directoryPath = new File("TestData").getAbsolutePath();
	
	@Test
	public void standardLIWCWordCountPluginTest() throws IOException {
		
		WordCountApi wca = new WordCountApi(false){
			@Override
			protected void generateRunReport(String outputFile, Date dateObj){}
		};	
		List<File> inputDataFiles = new ArrayList<File>();
		inputDataFiles.add(new File(directoryPath + System.getProperty("file.separator") +"LIWCData.txt"));	
		List<String> inputDictionaryFiles = new ArrayList<String>();
		inputDictionaryFiles.add(directoryPath + System.getProperty("file.separator") +"LIWCStandardWordCountDictionary.dic");
		final File generatedOutputFile = new File(directoryPath + File.separator
				+ "LIWCStandardWordCountGeneratedOutput.csv");
		final File generatedDATFile = new File(directoryPath + File.separator
				+ "LIWCStandardWordCountGeneratedOutput.dat");
 
		TacitUtil tacitHelper = new TacitUtil();
		final Map<String, String[]> fileCorpusMap = tacitHelper
				.getFileCorpusMembership();
		
		wca.wordCount(new NullProgressMonitor(), inputDataFiles, inputDictionaryFiles,
				"", directoryPath, "", true, isLiwcStemming,
				isSnowBall, false, false, isStemDic,
				generatedOutputFile, generatedDATFile, Calendar.getInstance().getTime(), fileCorpusMap);
		
		File expectedOutputFile = new File(directoryPath + System.getProperty("file.separator") +"LIWCStandardWordCountExpectedOutput.csv");
		BufferedReader reader = new BufferedReader(new FileReader(generatedOutputFile));
		String generatedLine1 = reader.readLine();
		String generatedLine2 = reader.readLine();
		reader.close();
	
		reader = new BufferedReader(new FileReader(expectedOutputFile));
		String expectedLine1 = reader.readLine();
		String expectedLine2 = reader.readLine();
		reader.close();

		
		assertEquals("Comparing the output for dic", expectedLine1, generatedLine1);
		assertEquals("Comparing the count of story", expectedLine2, generatedLine2);	
	}
	@Test
	public void standardLIWCWordCountDATTest() throws IOException {
		final File generatedOutputFile = new File(directoryPath + File.separator
				+ "LIWCStandardWordCountGeneratedOutput.csv");
		final File generatedDATFile = new File(directoryPath + File.separator
				+ "LIWCStandardWordCountGeneratedOutput.dat");

		WordCountApi wca = new WordCountApi(false){
			@Override
			protected void generateRunReport(String outputFile, Date dateObj){}
		};
		
		List<File> inputDataFiles = new ArrayList<File>();
		inputDataFiles.add(new File(directoryPath + System.getProperty("file.separator") +"LIWCData.txt"));	
		List<String> inputDictionaryFiles = new ArrayList<String>();
		inputDictionaryFiles.add(directoryPath + System.getProperty("file.separator") +"LIWCStandardWordCountDictionary.dic");
		
		TacitUtil tacitHelper = new TacitUtil();
		final Map<String, String[]> fileCorpusMap = tacitHelper
				.getFileCorpusMembership();
		
		wca.wordCount(new NullProgressMonitor(), inputDataFiles, inputDictionaryFiles,
				"", directoryPath, "", true, isLiwcStemming,
				isSnowBall, true, false, isStemDic,
				generatedOutputFile, generatedDATFile, Calendar.getInstance().getTime(), fileCorpusMap);
		
		File expectedDATFile = new File(directoryPath + System.getProperty("file.separator") +"LIWCStandardWordCountExpectedOutput.dat");
		BufferedReader reader = new BufferedReader(new FileReader(generatedDATFile));
		reader.readLine();
		String generatedWC = reader.readLine();
		reader.close();
		reader = new BufferedReader(new FileReader(expectedDATFile));
		reader.readLine();
		String expectedWC = reader.readLine();
		reader.close();
		assertEquals("Comparing the output for word count", generatedWC, expectedWC);
	}
	@Test
	public void standardLIWCWordCountWordDistributionTest() throws IOException {
		final File generatedOutputFile = new File(directoryPath + File.separator
				+ "LIWCStandardWordCountGeneratedOutput.csv");
		final File generatedDATFile = new File(directoryPath + File.separator
				+ "LIWCStandardWordCountGeneratedOutput.dat");

		WordCountApi wca = new WordCountApi(false){
			@Override
			protected void generateRunReport(String outputFile, Date dateObj){}
			@Override
			protected BufferedWriter createWordDistributionFile(String inputFile, File oFile, String dateFormat) throws IOException{
				File wdFile = new File(directoryPath + System.getProperty("file.separator") + "LIWCStandardWordDistributionGeneratedOutput.csv");
				BufferedWriter bw = new BufferedWriter(new FileWriter(wdFile));
				return bw;
			}
		};
		
		List<File> inputDataFiles = new ArrayList<File>();
		inputDataFiles.add(new File(directoryPath + System.getProperty("file.separator") +"LIWCData.txt"));	
		List<String> inputDictionaryFiles = new ArrayList<String>();
		inputDictionaryFiles.add(directoryPath + System.getProperty("file.separator") +"LIWCStandardWordCountDictionary.dic");
		
		TacitUtil tacitHelper = new TacitUtil();
		final Map<String, String[]> fileCorpusMap = tacitHelper
				.getFileCorpusMembership();
		
		wca.wordCount(new NullProgressMonitor(), inputDataFiles, inputDictionaryFiles,
				"", directoryPath, "", true, isLiwcStemming,
				isSnowBall, false, true, isStemDic,
				generatedOutputFile, generatedDATFile, Calendar.getInstance().getTime(), fileCorpusMap);
		
		File generatedWordDistributionFile = new File(directoryPath + System.getProperty("file.separator") + "LIWCStandardWordDistributionGeneratedOutput.csv");
		File expectedWordDistributionFile = new File(directoryPath + System.getProperty("file.separator") + "LIWCStandardWordDistributionExpectedOutput.csv");
		BufferedReader reader = new BufferedReader(new FileReader(generatedWordDistributionFile));
		String line = "";
		String generatedWD = "";
		String expectedWD = "";
		while((line = reader.readLine())!= null)
			generatedWD += line;
		reader.close();
		reader = new BufferedReader(new FileReader(expectedWordDistributionFile));
		while((line = reader.readLine())!= null)
			expectedWD += line;
		reader.close();
		assertEquals("Comparing the output for word count", generatedWD, expectedWD);
	}
	@Test
	public void standardLIWCCountWordsTest() throws IOException {
		final File generatedOutputFile = new File(directoryPath + File.separator
				+ "LIWCStandardWordCountGeneratedOutput.csv");
		final File generatedDATFile = new File(directoryPath + File.separator
				+ "LIWCStandardWordCountGeneratedOutput.dat");

		WordCountApi wca = new WordCountApi(false){

			@Override
			protected void generateRunReport(String outputFile, Date dateObj){}
			@Override
			protected BufferedWriter createWordDistributionFile(String inputFile, File oFile, String dateFormat) throws IOException{
				File wdFile = new File(directoryPath + System.getProperty("file.separator") + "LIWCStandardWordDistributionGeneratedOutput.csv");
				BufferedWriter bw = new BufferedWriter(new FileWriter(wdFile));
				return bw;
			}
		};
		
		List<File> inputDataFiles = new ArrayList<File>();
		inputDataFiles.add(new File(directoryPath + System.getProperty("file.separator") +"LIWCData.txt"));	
		List<String> inputDictionaryFiles = new ArrayList<String>();
		inputDictionaryFiles.add(directoryPath + System.getProperty("file.separator") +"LIWCStandardWordCountDictionary.dic");	
		TacitUtil tacitHelper = new TacitUtil();
		final Map<String, String[]> fileCorpusMap = tacitHelper
				.getFileCorpusMembership();
		String absoluteFilePath = inputDataFiles.get(0).getAbsolutePath();
		if (absoluteFilePath.contains("DS_Store")) {
			System.out.println("absoluteFilePath.contains(DS_Store)");
			return;
		}
		String corpus = "NIL";
		if (fileCorpusMap.containsKey(absoluteFilePath))
			corpus = fileCorpusMap.get(absoluteFilePath)[0] + "\\" + fileCorpusMap.get(absoluteFilePath)[1];
		wca.countWords(inputDataFiles.get(0), generatedOutputFile, generatedDATFile, Calendar.getInstance().getTime(), corpus);
		File expectedOutputFile = new File(directoryPath + System.getProperty("file.separator") +"LIWCStandardWordCountExpectedOutput.csv");
		BufferedReader reader = new BufferedReader(new FileReader(generatedOutputFile));
		String generatedLine1 = reader.readLine();
		String generatedLine2 = reader.readLine();
		reader.close();
	
		reader = new BufferedReader(new FileReader(expectedOutputFile));
		String expectedLine1 = reader.readLine();
		String expectedLine2 = reader.readLine();
		reader.close();

		
		assertEquals("Comparing the output for dic", expectedLine1, generatedLine1);
		assertEquals("Comparing the count of story", expectedLine2, generatedLine2);
	}
	
	@Test
	public void standardLIWCProcessTest() throws IOException {
		String line = "> On Jan 19, 2016 11:28 AM, \"Morteza Dehghani\" <mdehghan@usc.edu> wrote:";
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		WordCountApi wca = new WordCountApi(false){
			@Override
			protected void generateRunReport(String outputFile, Date dateObj){}
			@Override
			protected BufferedWriter createWordDistributionFile(String inputFile, File oFile, String dateFormat) throws IOException{
				File wdFile = new File(directoryPath + System.getProperty("file.separator") + "LIWCStandardWordDistributionGeneratedOutput.csv");
				BufferedWriter bw = new BufferedWriter(new FileWriter(wdFile));
				return bw;
			}
		};
		
		int generatedOutputArray[] = wca.process(line, map);
		int expectedOutputArray[] = {10, 3, 2};
		assertEquals("Comparing the output array of process method", Arrays.toString(generatedOutputArray), Arrays.toString(expectedOutputArray));
	}
	
	@Test
	public void trimCharsTest() throws IOException{

		String output = WordCountApi.trimChars(">",">\", \".,;\"!-()[]{}:?'/\\`~$%#@&*_=+<>");
		assertEquals("comparing categories", "", output);
	}
	
	@Test
	public void standardLIWCWordCountPluginMultipleDictionariesTest() throws IOException {
		
		WordCountApi wca = new WordCountApi(false){
			@Override
			protected void generateRunReport(String outputFile, Date dateObj){}
		};	
		List<File> inputDataFiles = new ArrayList<File>();
		inputDataFiles.add(new File(directoryPath + System.getProperty("file.separator") +"LIWCData.txt"));	
		List<String> inputDictionaryFiles = new ArrayList<String>();
		inputDictionaryFiles.add(directoryPath + System.getProperty("file.separator") +"LIWCStandardWordCountDictionary.dic");
		inputDictionaryFiles.add(directoryPath + System.getProperty("file.separator") +"LIWCStandardWordCountDictionary2.dic");
		
		final File generatedOutputFile = new File(directoryPath + File.separator
				+ "LIWCStandardWordCountGeneratedOutputMultipleDictionariesTest.csv");
		final File generatedDATFile = new File(directoryPath + File.separator
				+ "LIWCStandardWordCountGeneratedOutputMultipleDictionariesTest.dat");
 
		TacitUtil tacitHelper = new TacitUtil();
		final Map<String, String[]> fileCorpusMap = tacitHelper
				.getFileCorpusMembership();
		
		wca.wordCount(new NullProgressMonitor(), inputDataFiles, inputDictionaryFiles,
				"", directoryPath, "", true, isLiwcStemming,
				isSnowBall, false, false, isStemDic,
				generatedOutputFile, generatedDATFile, Calendar.getInstance().getTime(), fileCorpusMap);
		
		File expectedOutputFile = new File(directoryPath + System.getProperty("file.separator") +"LIWCStandardWordCountExpectedOutputMultipleDictionariesTest.csv");
		BufferedReader reader = new BufferedReader(new FileReader(generatedOutputFile));
		String line = "";
		reader.readLine();
		line = reader.readLine();
		reader.close();
		String values[] = line.split(",");
		String generatedWC = values[1];
		String generatedWPS = values[2];
		String generatedDic = values[3];
		String generatedStoryCount = values[4];
	
		reader = new BufferedReader(new FileReader(expectedOutputFile));
		reader.readLine();
		line = reader.readLine();
		reader.close();
		values = line.split(",");
		String expectedWC = values[1];
		String expectedWPS = values[2];
		String expectedDic = values[3];
		String expectedStoryCount = values[4];
		
		assertEquals("Comparing the output for word count", generatedWC, expectedWC);
		assertEquals("Comparing the output for wps", generatedWPS, expectedWPS);
		assertEquals("Comparing the output for dic", generatedDic, expectedDic);
		assertEquals("Comparing the count of story", generatedStoryCount, expectedStoryCount);	
	}
	@Test
	public void weightedLIWCWordCountPluginTest() throws IOException {
		
		WordCountApi wca = new WordCountApi(true){
			@Override
			protected void generateRunReport(String outputFile, Date dateObj){}
		};	
		List<File> inputDataFiles = new ArrayList<File>();
		inputDataFiles.add(new File(directoryPath + System.getProperty("file.separator") +"LIWCData.txt"));	
		List<String> inputDictionaryFiles = new ArrayList<String>();
		inputDictionaryFiles.add(directoryPath + System.getProperty("file.separator") +"LIWCWeightedWordCountDictionary.dic");
		final File generatedOutputFile = new File(directoryPath + File.separator
				+ "LIWCWeightedWordCountGeneratedOutput.csv");
		final File generatedDATFile = new File(directoryPath + File.separator
				+ "LIWCWeightedWordCountGeneratedOutput.dat");
 
		TacitUtil tacitHelper = new TacitUtil();
		final Map<String, String[]> fileCorpusMap = tacitHelper
				.getFileCorpusMembership();
		
		wca.wordCount(new NullProgressMonitor(), inputDataFiles, inputDictionaryFiles,
				"", directoryPath, "", true, isLiwcStemming,
				isSnowBall, false, false, isStemDic,
				generatedOutputFile, generatedDATFile, Calendar.getInstance().getTime(), fileCorpusMap);
		
		File expectedOutputFile = new File(directoryPath + System.getProperty("file.separator") +"LIWCWeightedWordCountExpectedOutput.csv");
		BufferedReader reader = new BufferedReader(new FileReader(generatedOutputFile));
		String generatedLine1 = reader.readLine();
		String generatedLine2 = reader.readLine();
		reader.close();
	
		reader = new BufferedReader(new FileReader(expectedOutputFile));
		String expectedLine1 = reader.readLine();
		String expectedLine2 = reader.readLine();
		reader.close();

		assertEquals("Comparing the output for line 1 of output file", expectedLine1, generatedLine1);
		assertEquals("Comparing the output for line 2 of output file", expectedLine2, generatedLine2);	
	}
	@Test
	public void weightedLIWCWordCountDATTest() throws IOException {
		final File generatedOutputFile = new File(directoryPath + File.separator
				+ "LIWCWeightedWordCountGeneratedOutput.csv");
		final File generatedDATFile = new File(directoryPath + File.separator
				+ "LIWCWeightedWordCountGeneratedOutput.dat");

		WordCountApi wca = new WordCountApi(true){
			@Override
			protected void generateRunReport(String outputFile, Date dateObj){}
		};
		
		List<File> inputDataFiles = new ArrayList<File>();
		inputDataFiles.add(new File(directoryPath + System.getProperty("file.separator") +"LIWCData.txt"));	
		List<String> inputDictionaryFiles = new ArrayList<String>();
		inputDictionaryFiles.add(directoryPath + System.getProperty("file.separator") +"LIWCWeightedWordCountDictionary.dic");
		
		TacitUtil tacitHelper = new TacitUtil();
		final Map<String, String[]> fileCorpusMap = tacitHelper
				.getFileCorpusMembership();
		
		wca.wordCount(new NullProgressMonitor(), inputDataFiles, inputDictionaryFiles,
				"", directoryPath, "", true, isLiwcStemming,
				isSnowBall, true, false, isStemDic,
				generatedOutputFile, generatedDATFile, Calendar.getInstance().getTime(), fileCorpusMap);
		
		File expectedDATFile = new File(directoryPath + System.getProperty("file.separator") +"LIWCWeightedWordCountExpectedOutput.dat");
		BufferedReader reader = new BufferedReader(new FileReader(generatedDATFile));
		reader.readLine();
		String generatedWC = reader.readLine();
		reader.close();
		reader = new BufferedReader(new FileReader(expectedDATFile));
		reader.readLine();
		String expectedWC = reader.readLine();
		reader.close();
		assertEquals("Comparing the output for word count", generatedWC, expectedWC);
	}
	@Test
	public void weightedLIWCWordCountWordDistributionTest() throws IOException {
		final File generatedOutputFile = new File(directoryPath + File.separator
				+ "LIWCWeightedWordCountGeneratedOutput.csv");
		final File generatedDATFile = new File(directoryPath + File.separator
				+ "LIWCWeightedWordCountGeneratedOutput.dat");

		WordCountApi wca = new WordCountApi(true){
			@Override
			protected void generateRunReport(String outputFile, Date dateObj){}
			@Override
			protected BufferedWriter createWordDistributionFile(String inputFile, File oFile, String dateFormat) throws IOException{
				File wdFile = new File(directoryPath + System.getProperty("file.separator") + "LIWCWeightedWordDistributionGeneratedOutput.csv");
				BufferedWriter bw = new BufferedWriter(new FileWriter(wdFile));
				return bw;
			}
		};
		
		List<File> inputDataFiles = new ArrayList<File>();
		inputDataFiles.add(new File(directoryPath + System.getProperty("file.separator") +"LIWCData.txt"));	
		List<String> inputDictionaryFiles = new ArrayList<String>();
		inputDictionaryFiles.add(directoryPath + System.getProperty("file.separator") +"LIWCWeightedWordCountDictionary.dic");
		
		TacitUtil tacitHelper = new TacitUtil();
		final Map<String, String[]> fileCorpusMap = tacitHelper
				.getFileCorpusMembership();
		
		wca.wordCount(new NullProgressMonitor(), inputDataFiles, inputDictionaryFiles,
				"", directoryPath, "", true, isLiwcStemming,
				isSnowBall, false, true, isStemDic,
				generatedOutputFile, generatedDATFile, Calendar.getInstance().getTime(), fileCorpusMap);
		
		File generatedWordDistributionFile = new File(directoryPath + System.getProperty("file.separator") + "LIWCWeightedWordDistributionGeneratedOutput.csv");
		File expectedWordDistributionFile = new File(directoryPath + System.getProperty("file.separator") + "LIWCWeightedWordDistributionExpectedOutput.csv");
		BufferedReader reader = new BufferedReader(new FileReader(generatedWordDistributionFile));
		String line = "";
		String generatedWD = "";
		String expectedWD = "";
		while((line = reader.readLine())!= null)
			generatedWD += line;
		reader.close();
		reader = new BufferedReader(new FileReader(expectedWordDistributionFile));
		while((line = reader.readLine())!= null)
			expectedWD += line;
		reader.close();
		assertEquals("Comparing the output for word count", generatedWD, expectedWD);
	}
	@Test
	public void weightedLIWCCountWordsTest() throws IOException {
		final File generatedOutputFile = new File(directoryPath + File.separator
				+ "LIWCWeightedWordCountGeneratedOutput.csv");
		final File generatedDATFile = new File(directoryPath + File.separator
				+ "LIWCWeightedWordCountGeneratedOutput.dat");

		WordCountApi wca = new WordCountApi(true){
			@Override
			protected void generateRunReport(String outputFile, Date dateObj){}
			@Override
			protected BufferedWriter createWordDistributionFile(String inputFile, File oFile, String dateFormat) throws IOException{
				File wdFile = new File(directoryPath + System.getProperty("file.separator") + "LIWCWeightedWordDistributionGeneratedOutput.csv");
				BufferedWriter bw = new BufferedWriter(new FileWriter(wdFile));
				return bw;
			}
		};
		
		List<File> inputDataFiles = new ArrayList<File>();
		inputDataFiles.add(new File(directoryPath + System.getProperty("file.separator") +"LIWCData.txt"));	
		List<String> inputDictionaryFiles = new ArrayList<String>();
		inputDictionaryFiles.add(directoryPath + System.getProperty("file.separator") +"LIWCWeightedWordCountDictionary.dic");	
		TacitUtil tacitHelper = new TacitUtil();
		final Map<String, String[]> fileCorpusMap = tacitHelper
				.getFileCorpusMembership();
		String absoluteFilePath = inputDataFiles.get(0).getAbsolutePath();
		if (absoluteFilePath.contains("DS_Store")) {
			System.out.println("absoluteFilePath.contains(DS_Store)");
			return;
		}
		String corpus = "NIL";
		if (fileCorpusMap.containsKey(absoluteFilePath))
			corpus = fileCorpusMap.get(absoluteFilePath)[0] + "\\" + fileCorpusMap.get(absoluteFilePath)[1];
		wca.countWords(inputDataFiles.get(0), generatedOutputFile, generatedDATFile, Calendar.getInstance().getTime(), corpus);
		File expectedOutputFile = new File(directoryPath + System.getProperty("file.separator") +"LIWCWeightedWordCountExpectedOutput.csv");
		BufferedReader reader = new BufferedReader(new FileReader(generatedOutputFile));
		String generatedLine1 = reader.readLine();
		String generatedLine2 = reader.readLine();
		reader.close();
	
		reader = new BufferedReader(new FileReader(expectedOutputFile));
		String expectedLine1 = reader.readLine();
		String expectedLine2 = reader.readLine();
		reader.close();
		
		assertEquals("Comparing the output for dic", expectedLine1, generatedLine1);
		assertEquals("Comparing the count of story", expectedLine2, generatedLine2);
	}
	@Test
	public void weightedLIWCProcessTest() throws IOException {
		String line = "> On Jan 19, 2016 11:28 AM, \"Morteza Dehghani\" <mdehghan@usc.edu> wrote:";
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		WordCountApi wca = new WordCountApi(true){
			@Override
			protected void generateRunReport(String outputFile, Date dateObj){}
			@Override
			protected BufferedWriter createWordDistributionFile(String inputFile, File oFile, String dateFormat) throws IOException{
				File wdFile = new File(directoryPath + System.getProperty("file.separator") + "LIWCStandardWordDistributionGeneratedOutput.csv");
				BufferedWriter bw = new BufferedWriter(new FileWriter(wdFile));
				return bw;
			}
		};
		
		int generatedOutputArray[] = wca.process(line, map);
		int expectedOutputArray[] = {10, 3, 2};
		assertEquals("Comparing the output array of process method", Arrays.toString(generatedOutputArray), Arrays.toString(expectedOutputArray));
	}
}