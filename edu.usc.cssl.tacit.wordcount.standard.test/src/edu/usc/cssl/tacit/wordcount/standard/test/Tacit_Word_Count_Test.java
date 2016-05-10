package edu.usc.cssl.tacit.wordcount.standard.test;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.usc.cssl.tacit.wordcount.standard.services.WordCountPlugin;

public class Tacit_Word_Count_Test{
	
	 // NOTE: Tacit Utility run reports have been disabled for the tests

	@Test
	public void standardWordCountTest() throws IOException {
		String directoryPath = new File("TestData").getAbsolutePath();
		WordCountPlugin wc = new WordCountPlugin(false, Calendar.getInstance().getTime(), false, false, false,
						false, false, directoryPath, new NullProgressMonitor()){
			@Override
			protected File getSetupFile(String bundleEntry) throws IOException{
				File setupFile = new File(bundleEntry);
				return setupFile;
			}
			@Override
			protected void generateRunReport(){}
			@Override
			protected String createFileName(){
				return "WordCountOutput.csv";
			}
		};
		List<String> inputFiles = new ArrayList<String>();
		inputFiles.add(directoryPath + System.getProperty("file.separator") +"Data.txt");
		List<String> dictionary = new ArrayList<String>();
		dictionary.add(directoryPath + System.getProperty("file.separator") +"ValidStandardWordCountDictionary.txt");
		wc.countWords(inputFiles, dictionary);
		File generatedOutputFile = new File(directoryPath + System.getProperty("file.separator") +"WordCountOutput.csv");
		File expectedOutputFile = new File(directoryPath + System.getProperty("file.separator") +"StandardWordCountTestExpectedOutput.csv");
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
	public void standardWordCountPluginPOSTagsTest() throws IOException {
		String directoryPath = new File("TestData").getAbsolutePath();
		WordCountPlugin wc = new WordCountPlugin(false, Calendar.getInstance().getTime(), false, false, false,
						false, true, directoryPath, new NullProgressMonitor()){
			
			@Override
			protected void createPosTagsDir(DateFormat df){
				posTagsDir = outputPath;
			}
			
			@Override
			protected File getSetupFile(String bundleEntry) throws IOException{
				File setupFile = new File(bundleEntry);
				return setupFile;
			}
			@Override
			protected void generateRunReport(){}
			@Override
			protected String createFileName(){
				return "WordCountOutput.csv";
			}
			protected BufferedWriter createPosTagsFile(String inputFile) throws IOException{
				return new BufferedWriter(new FileWriter(posTagsDir
						+ System.getProperty("file.separator")
						+ "POS-Tags-Generated-Output.txt"));
			}
		};

		List<String> inputFiles = new ArrayList<String>();
		inputFiles.add(directoryPath + System.getProperty("file.separator") +"Data.txt");
		List<String> dictionary = new ArrayList<String>();
		dictionary.add(directoryPath + System.getProperty("file.separator") +"ValidStandardWordCountDictionary.txt");
		wc.countWords(inputFiles, dictionary);
		File generatedPOSTagsFile = new File(directoryPath + System.getProperty("file.separator") +"POS-Tags-Generated-Output.txt");
		File expectedPOSTagsFile = new File(directoryPath + System.getProperty("file.separator") +"POS-Tags-Expected-Output.txt");
		BufferedReader reader = new BufferedReader(new FileReader(generatedPOSTagsFile));
		String line = "";
		String generatedPOSTags = "";
		String expectedPOSTags = "";
		while((line = reader.readLine())!=null)
			generatedPOSTags = generatedPOSTags + line;
		reader.close();
		reader = new BufferedReader(new FileReader(expectedPOSTagsFile));
		while((line = reader.readLine())!=null)
			expectedPOSTags = expectedPOSTags + line;
		reader.readLine();
		line = reader.readLine();
		reader.close();
		assertEquals("Comparing the POS Tag outputs", generatedPOSTags, expectedPOSTags);
		
}
	//There is no way to set the word distribution flag on the UI of standard and weighted word count plugins

	@Test
	public void standardWordCountPluginWordDistributionTest() throws IOException {
		String directoryPath = new File("TestData").getAbsolutePath();
		WordCountPlugin wc = new WordCountPlugin(false, Calendar.getInstance().getTime(), false, false, true,
						false, false, directoryPath, new NullProgressMonitor()){
			@Override
			protected void createWordDistributionDir(DateFormat df){
				wordDistributionDir = outputPath;
			}
			@Override
			protected BufferedWriter createWordDistributionFile(String inputFile) throws IOException{
				return new BufferedWriter(new FileWriter(new File(
						outputPath+ System.getProperty("file.separator") +"WordDistributionOutput.csv")));
			}
			@Override
			protected File getSetupFile(String bundleEntry) throws IOException{
				File setupFile = new File(bundleEntry);
				return setupFile;
			}
			@Override
			protected void generateRunReport(){}
			@Override
			protected String createFileName(){
				return "WordCountOutput.csv";
			}
		};

		List<String> inputFiles = new ArrayList<String>();
		inputFiles.add(directoryPath + System.getProperty("file.separator") +"Data.txt");
		List<String> dictionary = new ArrayList<String>();
		dictionary.add(directoryPath + System.getProperty("file.separator") +"ValidStandardWordCountDictionary.txt");
		wc.countWords(inputFiles, dictionary);
		File generatedWordDistributionOutput = new File(directoryPath + System.getProperty("file.separator") +"WordDistributionOutput.csv");
		File expectedWordDistributionOutput = new File(directoryPath + System.getProperty("file.separator") +"WordDistributionExpectedOutput.csv");
		HashSet<String> temp = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new FileReader(generatedWordDistributionOutput));
		String line = "";
		while((line = reader.readLine())!=null)
			temp.add(line);
		reader.close();
		boolean flag = true;
		reader = new BufferedReader(new FileReader(expectedWordDistributionOutput));
		while((line = reader.readLine())!=null)
			if (temp.contains(line))
				temp.remove(line);
			else
				flag = false;
		reader.readLine();
		reader.close();
		if(!temp.isEmpty())
			flag = false;
		assertEquals("Comparing the word distribution outputs", flag, true);
		
}
	
	@Test
	public void standardWordCountPluginStemDictionaryTest() throws IOException {
			String directoryPath = new File("TestData").getAbsolutePath();
			WordCountPlugin wc = new WordCountPlugin(false, Calendar.getInstance().getTime(), true, false, false,
							false, false, directoryPath, new NullProgressMonitor()){
				
				@Override
				protected File getSetupFile(String bundleEntry) throws IOException{
					File setupFile = new File(bundleEntry);
					return setupFile;
				}
				@Override
				protected void generateRunReport(){}
				@Override
				protected String createFileName(){
					return "StandardWordCountStemmedGeneratedOutput.csv";
				}
			};

			List<String> inputFiles = new ArrayList<String>();
			inputFiles.add(directoryPath + System.getProperty("file.separator") +"Data.txt");
			List<String> dictionary = new ArrayList<String>();
			dictionary.add(directoryPath + System.getProperty("file.separator") +"ValidStandardWordCountDictionary.txt");
			wc.countWords(inputFiles, dictionary);
			File generatedOutputFile = new File(directoryPath + System.getProperty("file.separator") +"StandardWordCountStemmedGeneratedOutput.csv");
			File expectedOutputFile = new File(directoryPath + System.getProperty("file.separator") +"StandardWordCountStemmedExpectedOutput.csv");
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
			
			assertEquals("Comparing the stemmed output for word count", generatedWC, expectedWC);
			assertEquals("Comparing the stemmed output for wps", generatedWPS, expectedWPS);
			assertEquals("Comparing the stemmed output for dic", generatedDic, expectedDic);
			assertEquals("Comparing the stemmed count of story", generatedStoryCount, expectedStoryCount);
			
	}
	@Test
	public void standardWordCountPluginDATTest() throws IOException {
		String directoryPath = new File("TestData").getAbsolutePath();
		WordCountPlugin wc = new WordCountPlugin(false, Calendar.getInstance().getTime(), false, false, false,
						true, false, directoryPath, new NullProgressMonitor()){
			@Override
			protected File getSetupFile(String bundleEntry) throws IOException{
				File setupFile = new File(bundleEntry);
				return setupFile;
			}
			@Override
			protected void generateRunReport(){}
			@Override
			protected String createDATFilePath(){
				return outputPath + System.getProperty("file.separator") +"WordCountOutput.dat";
			}@Override
			protected String createFileName(){
				return "WordCountOutput.csv";
			}
		};
		List<String> inputFiles = new ArrayList<String>();
		inputFiles.add(directoryPath + System.getProperty("file.separator") +"Data.txt");
		List<String> dictionary = new ArrayList<String>();
		dictionary.add(directoryPath + System.getProperty("file.separator") +"ValidStandardWordCountDictionary.txt");
		wc.countWords(inputFiles, dictionary);

		File expectedDATOutput = new File(directoryPath + System.getProperty("file.separator") +"ExpectedWordCountOutput.dat");
		File generatedDATOutput = new File(directoryPath + System.getProperty("file.separator") +"WordCountOutput.dat");
		BufferedReader reader = new BufferedReader(new FileReader(generatedDATOutput));
		String line = "";
		reader.readLine();
		line = reader.readLine();
		reader.close();
		line = line.substring(line.lastIndexOf(System.getProperty("file.separator"))+2);
		String values[] = line.split(" ");
		System.out.println(line);
		System.out.println(values);
		String generatedWC = values[1];
		String generatedWPS = values[2];
		String generatedDic = values[3];
		String generatedStoryCount = values[4];
	
		reader = new BufferedReader(new FileReader(expectedDATOutput));
		reader.readLine();
		line = reader.readLine();
		reader.close();
		line = line.substring(line.lastIndexOf(System.getProperty("file.separator"))+2);
		values = line.split(" ");
		String expectedWC = values[1];
		String expectedWPS = values[2];
		String expectedDic = values[3];
		String expectedStoryCount = values[4];
		
		assertEquals("Comparing the DAT output for word count", generatedWC, expectedWC);
		assertEquals("Comparing the DAT output for wps", generatedWPS, expectedWPS);
		assertEquals("Comparing the DAT output for dic", generatedDic, expectedDic);
		assertEquals("Comparing the DAT count of story", generatedStoryCount, expectedStoryCount);

	}
	
	@Test
	public void weightedWordCountPluginTest() throws IOException {
		String directoryPath = new File("TestData").getAbsolutePath();
		WordCountPlugin wc = new WordCountPlugin(true, Calendar.getInstance().getTime(), false, false, false,
						false, false, directoryPath, new NullProgressMonitor()){
			@Override
			protected File getSetupFile(String bundleEntry) throws IOException{
				File setupFile = new File(bundleEntry);
				return setupFile;
			}
			@Override
			protected void generateRunReport(){}
			@Override
			protected String createFileName(){
				return "WeightedWordCountOutput.csv";
			}
		};
		List<String> inputFiles = new ArrayList<String>();
		inputFiles.add(directoryPath + System.getProperty("file.separator") +"Data.txt");
		List<String> dictionary = new ArrayList<String>();
		dictionary.add(directoryPath + System.getProperty("file.separator") +"ValidWeightedWordCountDictionary.txt");
		wc.countWords(inputFiles, dictionary);
		File generatedOutputFile = new File(directoryPath + System.getProperty("file.separator") +"WeightedWordCountOutput.csv");
		File expectedOutputFile = new File(directoryPath + System.getProperty("file.separator") +"WeightedWordCountTestExpectedOutput.csv");
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
	public void weightedWordCountPluginPOSTagsTest() throws IOException {
		String directoryPath = new File("TestData").getAbsolutePath();
		WordCountPlugin wc = new WordCountPlugin(true, Calendar.getInstance().getTime(), false, false, false,
						false, true, directoryPath, new NullProgressMonitor()){
			
			@Override
			protected void createPosTagsDir(DateFormat df){
				posTagsDir = outputPath;
			}
			
			@Override
			protected File getSetupFile(String bundleEntry) throws IOException{
				File setupFile = new File(bundleEntry);
				return setupFile;
			}
			@Override
			protected void generateRunReport(){}
			@Override
			protected String createFileName(){
				return "WordCountOutput.csv";
			}
			protected BufferedWriter createPosTagsFile(String inputFile) throws IOException{
				return new BufferedWriter(new FileWriter(posTagsDir
						+ System.getProperty("file.separator")
						+ "POS-Tags-Generated-Output.txt"));
			}
		};
		List<String> inputFiles = new ArrayList<String>();
		inputFiles.add(directoryPath + System.getProperty("file.separator") +"Data.txt");
		List<String> dictionary = new ArrayList<String>();
		dictionary.add(directoryPath + System.getProperty("file.separator") +"ValidWeightedWordCountDictionary.txt");
		wc.countWords(inputFiles, dictionary);
		File generatedPOSTagsFile = new File(directoryPath + System.getProperty("file.separator") +"POS-Tags-Generated-Output.txt");
		File expectedPOSTagsFile = new File(directoryPath + System.getProperty("file.separator") +"weighted-POS-Tags-Expected-Output.txt");
		BufferedReader reader = new BufferedReader(new FileReader(generatedPOSTagsFile));
		String line = "";
		String generatedPOSTags = "";
		String expectedPOSTags = "";
		while((line = reader.readLine())!=null)
			generatedPOSTags = generatedPOSTags + line;
		reader.close();
		reader = new BufferedReader(new FileReader(expectedPOSTagsFile));
		while((line = reader.readLine())!=null)
			expectedPOSTags = expectedPOSTags + line;
		reader.readLine();
		line = reader.readLine();
		reader.close();
		assertEquals("Comparing the POS Tag outputs", generatedPOSTags, expectedPOSTags);
		
}
	@Test
	public void weightedWordCountPluginWordDistributionTest() throws IOException {
		String directoryPath = new File("TestData").getAbsolutePath();
		WordCountPlugin wc = new WordCountPlugin(true, Calendar.getInstance().getTime(), false, false, true,
						false, false, directoryPath, new NullProgressMonitor()){
			@Override
			protected void createWordDistributionDir(DateFormat df){
				wordDistributionDir = outputPath;
			}
			@Override
			protected BufferedWriter createWordDistributionFile(String inputFile) throws IOException{
				return new BufferedWriter(new FileWriter(new File(
						outputPath + System.getProperty("file.separator") + "WeightedWordDistributionOutput.csv")));
			}
			@Override
			protected File getSetupFile(String bundleEntry) throws IOException{
				File setupFile = new File(bundleEntry);
				return setupFile;
			}
			@Override
			protected void generateRunReport(){}
			@Override
			protected String createFileName(){
				return "WordCountOutput.csv";
			}
		};
		List<String> inputFiles = new ArrayList<String>();
		inputFiles.add(directoryPath + System.getProperty("file.separator") + "Data.txt");
		List<String> dictionary = new ArrayList<String>();
		dictionary.add(directoryPath + System.getProperty("file.separator") + "ValidWeightedWordCountDictionary.txt");
		wc.countWords(inputFiles, dictionary);
		File generatedWordDistributionOutput = new File(directoryPath + System.getProperty("file.separator") + "WeightedWordDistributionOutput.csv");
		File expectedWordDistributionOutput = new File(directoryPath + System.getProperty("file.separator") + "WeightedWordDistributionExpectedOutput.csv");
		HashSet<String> temp = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new FileReader(generatedWordDistributionOutput));
		String line = "";
		while((line = reader.readLine())!=null)
			temp.add(line);
		reader.close();
		boolean flag = true;
		reader = new BufferedReader(new FileReader(expectedWordDistributionOutput));
		while((line = reader.readLine())!=null)
			if (temp.contains(line))
				temp.remove(line);
			else
				flag = false;
		reader.readLine();
		reader.close();
		if(!temp.isEmpty())
			flag = false;
		assertEquals("Comparing the word distribution outputs", flag, true);
		
}
	
	@Test
	public void weightedWordCountPluginStemDictionaryTest() throws IOException {

			String directoryPath = new File("TestData").getAbsolutePath();
			WordCountPlugin wc = new WordCountPlugin(true, Calendar.getInstance().getTime(), true, false, false,
							false, false, directoryPath, new NullProgressMonitor()){
				
				@Override
				protected File getSetupFile(String bundleEntry) throws IOException{
					File setupFile = new File(bundleEntry);
					return setupFile;
				}
				@Override
				protected void generateRunReport(){}
				@Override
				protected String createFileName(){
					return "WeightedWordCountStemmedGeneratedOutput.csv";
				}
			};
			List<String> inputFiles = new ArrayList<String>();
			inputFiles.add(directoryPath + System.getProperty("file.separator") + "Data.txt");
			List<String> dictionary = new ArrayList<String>();
			dictionary.add(directoryPath + System.getProperty("file.separator") + "ValidWeightedWordCountDictionary.txt");
			wc.countWords(inputFiles, dictionary);
			File generatedOutputFile = new File(directoryPath + System.getProperty("file.separator") +"WeightedWordCountStemmedGeneratedOutput.csv");
			File expectedOutputFile = new File(directoryPath + System.getProperty("file.separator") +"WeightedWordCountStemmedExpectedOutput.csv");
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
			
			assertEquals("Comparing the stemmed output for word count", generatedWC, expectedWC);
			assertEquals("Comparing the stemmed output for wps", generatedWPS, expectedWPS);
			assertEquals("Comparing the stemmed output for dic", generatedDic, expectedDic);
			assertEquals("Comparing the stemmed count of story", generatedStoryCount, expectedStoryCount);
			
	}
	@Test
	public void weightedWordCountPluginDATTest() throws IOException {
		String directoryPath = new File("TestData").getAbsolutePath();
		WordCountPlugin wc = new WordCountPlugin(true, Calendar.getInstance().getTime(), false, false, false,
						true, false, directoryPath, new NullProgressMonitor()){
			@Override
			protected File getSetupFile(String bundleEntry) throws IOException{
				File setupFile = new File(bundleEntry);
				return setupFile;
			}
			@Override
			protected void generateRunReport(){}
			@Override
			protected String createDATFilePath(){
				return outputPath + System.getProperty("file.separator") +"WeightedWordCountOutput.dat";
			}@Override
			protected String createFileName(){
				return "WeightedWordCountOutput.csv";
			}
		};
		List<String> inputFiles = new ArrayList<String>();
		inputFiles.add(directoryPath + System.getProperty("file.separator") +"Data.txt");
		List<String> dictionary = new ArrayList<String>();
		dictionary.add(directoryPath + System.getProperty("file.separator") +"ValidWeightedWordCountDictionary.txt");
		wc.countWords(inputFiles, dictionary);
		
		File expectedDATOutput = new File(directoryPath + System.getProperty("file.separator") +"ExpectedWeightedWordCountOutput.dat");
		File generatedDATOutput = new File(directoryPath + System.getProperty("file.separator") +"WeightedWordCountOutput.dat");
		BufferedReader reader = new BufferedReader(new FileReader(generatedDATOutput));
		String line = "";
		reader.readLine();
		line = reader.readLine();
		reader.close();
		line = line.substring(line.lastIndexOf(System.getProperty("file.separator"))+2);
		String values[] = line.split(" ");
		System.out.println(line);
		System.out.println(values);
		String generatedWC = values[1];
		String generatedWPS = values[2];
		String generatedDic = values[3];
		String generatedStoryCount = values[4];
	
		reader = new BufferedReader(new FileReader(expectedDATOutput));
		reader.readLine();
		line = reader.readLine();
		reader.close();
		line = line.substring(line.lastIndexOf(System.getProperty("file.separator"))+2);
		values = line.split(" ");
		String expectedWC = values[1];
		String expectedWPS = values[2];
		String expectedDic = values[3];
		String expectedStoryCount = values[4];
		
		assertEquals("Comparing the DAT output for word count", generatedWC, expectedWC);
		assertEquals("Comparing the DAT output for wps", generatedWPS, expectedWPS);
		assertEquals("Comparing the DAT output for dic", generatedDic, expectedDic);
		assertEquals("Comparing the DAT count of story", generatedStoryCount, expectedStoryCount);

	}

}
