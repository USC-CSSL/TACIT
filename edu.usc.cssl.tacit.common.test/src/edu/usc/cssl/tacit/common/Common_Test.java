package edu.usc.cssl.tacit.common;


import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import edu.usc.cssl.tacit.common.Preprocessor;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class Common_Test{

	final String directoryPath = new File("TestData").getAbsolutePath();
	
	@Test
	public void test() throws IOException {
		
		List<Object> selectedFiles = new ArrayList<Object>();
		selectedFiles.add(directoryPath + File.separator
				+ "KMeansClusteringData1.txt");
		
		Preprocessor ppObj = null;
		List<String> inFiles = null;
		try {
			ppObj = new Preprocessor("Kmeans", true){
				
				protected String generateProcessedFileName(String inFileBefore, String outName){
					String outFile = ppFilesLoc + System.getProperty("file.separator") + "GeneratedPreprocessedFile.txt";
					return outFile;
				}
				
				protected void createppDir(String caller) {
					ppDir = directoryPath+ File.separator + "temp";
					new File(ppDir).mkdir();
				}	
				
				protected void setupParams() throws IOException {
					if (doPreprocessing) {
						// Setup global parameters
						String stopwordsFile = directoryPath+ File.separator + "stopwords_eng.txt";
						delimiters = " .,;'\"!-()[]{}:?"; //Basically, keep the original initialized value. Potentially redundant statement.
						stemLang = "EN";
						doLowercase = true;
						doStemming = true;
						doStopWords = true;
						doCleanUp = true;
						
						SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
						Date now = new Date();
						this.currTime = sdfDate.format(now);

						// Setup stop words set
						if (doStopWords) {
							String currentLine;
							File sfile = new File(stopwordsFile);
							if (!sfile.exists() || sfile.isDirectory()) {
								ConsoleView.printlInConsoleln("Stop Words file is not valid. Please provide a correct file path");
								throw new IOException();
							}
							BufferedReader br = new BufferedReader(new FileReader(new File(stopwordsFile)));
							while ((currentLine = br.readLine()) != null) {
								stopWordsSet.add(currentLine.trim().toLowerCase());
							}
							br.close();
						}

						// Setup Stemmer
						if (doStemming) {
							if (!stemLang.equals("LATIN"))
								stemmer = stemSelect(stemLang);
						}
					}
				}
			};
			inFiles = ppObj.processData("tempdata",
					selectedFiles, null);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		File generatedPreprocessedOutput = new File(directoryPath + File.separator +"temp"+File.separator +"tempdata"+File.separator 
				+ "GeneratedPreprocessedFile.txt");
		File expectedPreprocessedOutput = new File(directoryPath + File.separator
				+ "ExpectedPreprocessedFile.txt");
		BufferedReader reader = new BufferedReader(new FileReader(generatedPreprocessedOutput));
		String line = "";
		String generatedOutput = "";
		String expectedOutput = "";
		while((line = reader.readLine())!= null)
			generatedOutput += line;
		reader.close();
		reader = new BufferedReader(new FileReader(expectedPreprocessedOutput));
		while((line = reader.readLine())!= null)
			expectedOutput += line;
		reader.close();
		assertEquals("Comparing preprocessed outputs", expectedOutput, generatedOutput);
		ppObj.clean();
	}

}
