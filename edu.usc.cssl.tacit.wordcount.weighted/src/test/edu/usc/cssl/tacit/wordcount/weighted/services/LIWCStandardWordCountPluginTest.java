package test.edu.usc.cssl.tacit.wordcount.weighted.services;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.uc.cssl.tacit.wordcount.weighted.services.WordCountApi;
import edu.usc.cssl.tacit.common.ui.utility.TacitUtil;

public class LIWCStandardWordCountPluginTest{
	
	 // TODO: Tacit Utility run report has been disabled for all the tests
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
				System.out.println(wdFile.getAbsolutePath());
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
}
