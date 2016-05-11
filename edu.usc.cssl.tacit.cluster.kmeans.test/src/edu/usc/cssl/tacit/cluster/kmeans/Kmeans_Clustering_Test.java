package edu.usc.cssl.tacit.cluster.kmeans;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.uc.cssl.tacit.cluster.kmeans.services.KmeansClusterAnalysis;


public class Kmeans_Clustering_Test {
	final String directoryPath = new File("TestData").getAbsolutePath();
	@Test
	public void runClusteringTest() throws IOException {

		List<File> inputFiles = new ArrayList<File>();
		inputFiles.add(new File(directoryPath + File.separator
				+ "KMeansClusteringData1.txt"));
		inputFiles.add(new File(directoryPath + File.separator
				+ "KMeansClusteringData2.txt"));
		inputFiles.add(new File(directoryPath + File.separator
				+ "KMeansClusteringData3.txt"));
		inputFiles.add(new File(directoryPath + File.separator
				+ "KMeansClusteringData4.txt"));
		int noOfClusters = 3;
		Date dateObj = Calendar.getInstance().getTime();
		boolean result = KmeansClusterAnalysis
				.runClustering(noOfClusters, inputFiles,
						directoryPath, dateObj, true);
		assertEquals("Checking if the test ran successfully", true, result);
		File generatedKMeansClustersOutput = new File(directoryPath + File.separator
				+ "GeneratedKMeansClustersOutput.txt");
		File expectedKMeansClustersOutput = new File(directoryPath + File.separator
				+ "ExpectedKMeansClustersOutput.txt");
		BufferedReader reader = new BufferedReader(new FileReader(generatedKMeansClustersOutput));
		String line = "";
		String generatedOutput = "";
		String expectedOutput = "";
		while((line = reader.readLine())!= null)
			generatedOutput += line;
		reader.close();
		reader = new BufferedReader(new FileReader(expectedKMeansClustersOutput));
		while((line = reader.readLine())!= null)
			expectedOutput += line;
		reader.close();
		assertEquals("Comparing cluster outputs", expectedOutput, generatedOutput);
	}
	@Test
	public void doClusteringTest() throws IOException {

		List<File> inputFiles = new ArrayList<File>();
		inputFiles.add(new File(directoryPath + File.separator
				+ "KMeansClusteringData1.txt"));
		inputFiles.add(new File(directoryPath + File.separator
				+ "KMeansClusteringData2.txt"));
		inputFiles.add(new File(directoryPath + File.separator
				+ "KMeansClusteringData3.txt"));
		inputFiles.add(new File(directoryPath + File.separator
				+ "KMeansClusteringData4.txt"));
		int noOfClusters = 3;
		int expectedOutput[] = {1, 1, 0, 2};
		int generatedOutput[] = KmeansClusterAnalysis
				.doClustering(inputFiles, noOfClusters);
		assertEquals("Comparing cluster outputs", expectedOutput[0], generatedOutput[0]);
		assertEquals("Comparing cluster outputs", expectedOutput[1], generatedOutput[1]);
		assertEquals("Comparing cluster outputs", expectedOutput[2], generatedOutput[2]);
		assertEquals("Comparing cluster outputs", expectedOutput[3], generatedOutput[3]);
	}
}