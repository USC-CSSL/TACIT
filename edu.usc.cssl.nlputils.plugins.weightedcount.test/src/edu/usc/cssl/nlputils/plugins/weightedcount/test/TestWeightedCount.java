package edu.usc.cssl.nlputils.plugins.weightedcount.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.usc.cssl.nlputils.plugins.weightedCount.process.WeightedCount;

public class TestWeightedCount {
	
	private WeightedCount counter;
	private String[] inputFiles;
	private String dictionaryFile;
	private String stopWordsFile;
	private String outputFile;
	private String delimiters;
	private boolean doLower;
	private boolean doLiwcStemming;
	private boolean doSnowBallStemming;
	private boolean doSpss;
	private boolean doWordDistribution;
	private boolean stemDictionary;
	private String outputDir;
	
	@Before
	public void setUp() throws Exception {	
		counter = new WeightedCount();	
		inputFiles[0] = "testfiles/doc.txt";
		dictionaryFile = "testfiles/TabbedDictionary.dic";
		stopWordsFile = "testfiles/stop.txt";
		outputFile = "outdir/out";
		delimiters = "";
		doLower = true;
		doSnowBallStemming = false;
		doLiwcStemming = false;
		stemDictionary = false;
		doWordDistribution = false;
		outputDir = "outdir";
	}
	
	@Test
	public void testWordCount_spss() throws IOException {
		doSpss = true;
		counter.wordCount(inputFiles, dictionaryFile, stopWordsFile, outputFile, delimiters, doLower, doLiwcStemming, doSnowBallStemming, doSpss, doWordDistribution, stemDictionary);
		assertTrue("Failed to write output File ; not stemmed", new File("out.csv").exists());
		assertTrue("Failed to write spss File ; not stemmed", new File("out.dat").exists());
	}
	
	@Test
	public void testWordCount_wordDist() throws IOException {
		doWordDistribution = true;
		counter.wordCount(inputFiles, dictionaryFile, stopWordsFile, outputFile, delimiters, doLower, doLiwcStemming, doSnowBallStemming, doSpss, doWordDistribution, stemDictionary);
		assertTrue("Failed to write output File ; not stemmed", new File("out.csv").exists());
		
		String iFilename = inputFiles[0].substring(inputFiles[0].lastIndexOf(System.getProperty("file.separator")));
		File wdFile = new File(outputDir+System.getProperty("file.separator")+iFilename+"_wordDistribution.csv");
		assertTrue("Failed to write word distribution File ; not stemmed", wdFile.exists());
	}
	
	@Test
	public void testWordCount_spss_snowstem() throws IOException {
		doSnowBallStemming = true;
		stemDictionary = true;
		doSpss = true;
		counter.wordCount(inputFiles, dictionaryFile, stopWordsFile, outputFile, delimiters, doLower, doLiwcStemming, doSnowBallStemming, doSpss, doWordDistribution, stemDictionary);
		assertTrue("Failed to write output File ; Snowball stemmed", new File("out.csv").exists());
		assertTrue("Failed to write spss File ; Snowball stemmed", new File("out.dat").exists());
	}
	
	@Test
	public void testWordCount_wordDist_snowstem() throws IOException {
		doSnowBallStemming = true;
		stemDictionary = true;
		doWordDistribution = true;
		counter.wordCount(inputFiles, dictionaryFile, stopWordsFile, outputFile, delimiters, doLower, doLiwcStemming, doSnowBallStemming, doSpss, doWordDistribution, stemDictionary);
		assertTrue("Failed to write output File ; liwc stemmed", new File("out.csv").exists());
		
		String iFilename = inputFiles[0].substring(inputFiles[0].lastIndexOf(System.getProperty("file.separator")));
		File wdFile = new File(outputDir+System.getProperty("file.separator")+iFilename+"_wordDistribution.csv");
		assertTrue("Failed to write word distribution File ; liwc stemmed", wdFile.exists());
	}
	
	@Test
	public void testWordCount_spss_liwc() throws IOException {
		doLiwcStemming = true;
		stemDictionary = true;
		doSpss = true;
		counter.wordCount(inputFiles, dictionaryFile, stopWordsFile, outputFile, delimiters, doLower, doLiwcStemming, doSnowBallStemming, doSpss, doWordDistribution, stemDictionary);
		assertTrue("Failed to write output File", new File("out.csv").exists());
		assertTrue("Failed to write spss File", new File("out.dat").exists());
	}
	
	@Test
	public void testWordCount_wordDist_liwc() throws IOException {
		doLiwcStemming = true;
		stemDictionary = true;
		doWordDistribution = true;
		counter.wordCount(inputFiles, dictionaryFile, stopWordsFile, outputFile, delimiters, doLower, doLiwcStemming, doSnowBallStemming, doSpss, doWordDistribution, stemDictionary);
		assertTrue("Failed to write output File", new File("out.csv").exists());
		
		String iFilename = inputFiles[0].substring(inputFiles[0].lastIndexOf(System.getProperty("file.separator")));
		File wdFile = new File(outputDir+System.getProperty("file.separator")+iFilename+"_wordDistribution.csv");
		assertTrue("Failed to write word distribution File", wdFile.exists());
	}
	
	@After
    public void cleanUp() {
	   File testFile = new File(outputDir);
	   File[] files = testFile.listFiles();
	   for (File file : files) {
		   file.delete();
	   }
    }

}
