package test.edu.usc.cssl.tacit.wordcount.weighted.services;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.uc.cssl.tacit.wordcount.weighted.services.WordCountApi;
import edu.usc.cssl.tacit.common.ui.utility.TacitUtil;

public class LIWCWeightedWordCountPluginTest{
	
	 // TODO: Tacit Utility run report has been disabled for all the tests

	@Test
	public void weightedliwcWordCountPluginTest() throws IOException {
		String directoryPath = new File("TestData").getAbsolutePath();
		
		WordCountApi wca = new WordCountApi(true);
		final boolean isLiwcStemming = false; // no other value possible
		final boolean isSnowBall = false;// no other value possible
		final boolean isSpss = false; // since create DAT file is unchecked
		final boolean isWdist = false; // since create word distribution files is unchecked
		final boolean isStemDic = false; // no other value possible
		
		final File generatedOutputFile = new File(directoryPath + File.separator
				+ "LIWC-weighted-wordcount.csv");
		final File generatedDATFile = new File(directoryPath + File.separator
				+ "LIWC-weighted-wordcount.dat");
		
		List<File> inputDataFiles = new ArrayList<File>();
		inputDataFiles.add(new File(directoryPath + System.getProperty("file.separator") +"LIWCData.txt"));
		
		List<String> inputDictionaryFiles = new ArrayList<String>();
		inputDictionaryFiles.add(directoryPath + System.getProperty("file.separator") +"LIWCStandardWordCountDictionary.dic");
		
		TacitUtil tacitHelper = new TacitUtil();
		final Map<String, String[]> fileCorpusMap = tacitHelper
				.getFileCorpusMembership();
		
		wca.wordCount(new NullProgressMonitor(), inputDataFiles, inputDictionaryFiles,
				"", directoryPath, "", true, isLiwcStemming,
				isSnowBall, isSpss, isWdist, isStemDic,
				generatedOutputFile, generatedDATFile, Calendar.getInstance().getTime(), fileCorpusMap);
		
		
		File expectedOutputFile = new File(directoryPath + System.getProperty("file.separator") +"LIWCWeightedWordCountExpectedOutput.csv");
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
	
}
