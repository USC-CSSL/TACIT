package edu.usc.cssl.nlputils.plugins.cooccurrenceanalysis.test;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.usc.cssl.nlputils.plugins.CooccurrenceAnalysis.process.CooccurrenceAnalysis;

public class TestCooccurenceAnalysis {

	CooccurrenceAnalysis analyser;
	private String inputDir;
	private String seedFile;
	private int windowSize;
	private String outputPath;
	private int threshold;
	private boolean buildMatrix;
	
	@Before
	public void setUp() throws Exception {
		
		analyser = new CooccurrenceAnalysis();
		buildMatrix = true;
		threshold = 3;
		inputDir = "inputdir";
		seedFile = "inputdir/seed/seeds.txt";
		windowSize = 4;
		outputPath = "outputdir";
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCalculateCooccurrencesBMatrixTrue() {
		
		boolean buildMatrix = true; 
		analyser.calculateCooccurrences(inputDir, seedFile, windowSize, outputPath, threshold, buildMatrix);
		assertTrue("Failed to write Phrase File", new File("outputdir/phrases.txt").exists());
		assertTrue("Failed to write Word Matrix File", new File("outputdir/word-to-word-matrix.csv").exists());
		
	}
	
	@Test
	public void testCalculateCooccurrencesBMatrixFalse() {
		
		boolean buildMatrix = false; 
		analyser.calculateCooccurrences(inputDir, seedFile, windowSize, outputPath, threshold, buildMatrix);
		assertTrue("Failed to write Phrase File", new File("outputdir/phrases.txt").exists());
		//assertTrue("Failed to write Word Matrix File", new File("outputdir/word-word.csv").exists());
	}
	
	@Test
	public void testCalculateCooccurrencesNoSeed() {
		
		seedFile = "C:\\Seed";
		analyser.calculateCooccurrences(inputDir, seedFile, windowSize, outputPath, threshold, buildMatrix);
		//assertTrue("Failed to write Phrase File", new File("outputdir/phrases.txt").exists());
		assertTrue("Failed to write Word Matrix File", new File("outputdir/word-to-word-matrix.csv").exists());
	}
	
	@After
    public void cleanUp() {
	   File testFile = new File("outputdir");
	   File[] files = testFile.listFiles();
	   for (File file : files) {
		   file.delete();
	   }
	}

}
