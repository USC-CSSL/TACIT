package test.edu.usc.tacit.cluster.hierarchical;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.junit.Test;

import edu.usc.cssl.tacit.cluster.hierarchical.services.HierarchicalClusterAnalysis;

public class Hierarchical_Clustering_Test {
	final String directoryPath = new File("TestData").getAbsolutePath();
	@Test
	public void invokeCooccurrenceTest() throws IOException {

		List<File> inputFiles = new ArrayList<File>();
		inputFiles.add(new File(directoryPath + File.separator
				+ "HierarchicalClusteringData1.txt"));
		inputFiles.add(new File(directoryPath + File.separator
				+ "HierarchicalClusteringData2.txt"));
		inputFiles.add(new File(directoryPath + File.separator
				+ "HierarchicalClusteringData3.txt"));
		inputFiles.add(new File(directoryPath + File.separator
				+ "HierarchicalClusteringData4.txt"));
		boolean isSaveImage = false;
		
		boolean isSuccessful = HierarchicalClusterAnalysis
				.runClustering(inputFiles, directoryPath,
						isSaveImage, new SubProgressMonitor(
								new NullProgressMonitor(), 50), Calendar.getInstance().getTime(), true);
		
		assertEquals("Checking if the test ran successfully", true, isSuccessful);
		
		File generatedHierarchicalClustersOutput = new File(directoryPath + File.separator
				+ "GeneratedHierarchicalClustersOutput.txt");
		File expectedHierarchicalClustersOutput = new File(directoryPath + File.separator
				+ "ExpectedHierarchicalClustersOutput.txt");
		BufferedReader reader = new BufferedReader(new FileReader(generatedHierarchicalClustersOutput));
		String line = "";
		String generatedOutput = "";
		String expectedOutput = "";
		while((line = reader.readLine())!= null)
			generatedOutput += line;
		reader.close();
		reader = new BufferedReader(new FileReader(expectedHierarchicalClustersOutput));
		while((line = reader.readLine())!= null)
			expectedOutput += line;
		reader.close();
		assertEquals("Comparing cluster outputs", expectedOutput, generatedOutput);
	}
	@Test
	public void doClusteringTest() throws IOException {

		List<File> inputFiles = new ArrayList<File>();
		inputFiles.add(new File(directoryPath + File.separator
				+ "HierarchicalClusteringData1.txt"));
		inputFiles.add(new File(directoryPath + File.separator
				+ "HierarchicalClusteringData2.txt"));
		inputFiles.add(new File(directoryPath + File.separator
				+ "HierarchicalClusteringData3.txt"));
		inputFiles.add(new File(directoryPath + File.separator
				+ "HierarchicalClusteringData4.txt"));
		boolean isSaveImage = false;
		
		String clusterOutput = HierarchicalClusterAnalysis.doClustering(inputFiles, directoryPath,
				isSaveImage, new SubProgressMonitor(
						new NullProgressMonitor(), 50), Calendar.getInstance().getTime(), true);
		assertEquals("Checking cluster output", "Newick:(((1:17.80449,2:17.80449):2.22049,3:20.02498):1.18822,4:21.2132)", clusterOutput);
		
		File generatedHierarchicalClustersOutput = new File(directoryPath + File.separator
				+ "GeneratedHierarchicalClustersOutput.txt");
		File expectedHierarchicalClustersOutput = new File(directoryPath + File.separator
				+ "ExpectedHierarchicalClustersOutput.txt");
		BufferedReader reader = new BufferedReader(new FileReader(generatedHierarchicalClustersOutput));
		String line = "";
		String generatedOutput = "";
		String expectedOutput = "";
		while((line = reader.readLine())!= null)
			generatedOutput += line;
		reader.close();
		reader = new BufferedReader(new FileReader(expectedHierarchicalClustersOutput));
		while((line = reader.readLine())!= null)
			expectedOutput += line;
		reader.close();
		assertEquals("Comparing file outputs", expectedOutput, generatedOutput);
	}
	
	@Test
	public void formatGraphTest(){
		List<File> inputFiles = new ArrayList<File>();
		inputFiles.add(new File(directoryPath + File.separator
				+ "HierarchicalClusteringData1.txt"));
		inputFiles.add(new File(directoryPath + File.separator
				+ "HierarchicalClusteringData2.txt"));
		inputFiles.add(new File(directoryPath + File.separator
				+ "HierarchicalClusteringData3.txt"));
		inputFiles.add(new File(directoryPath + File.separator
				+ "HierarchicalClusteringData4.txt"));
		String generatedOutput = HierarchicalClusterAnalysis.formatGraph("Newick:(((0.0:17.80449,1.0:17.80449):2.22049,0.0:20.02498):1.18822,0.0:21.2132)", inputFiles);
		String expectedOutput = "Newick:(((1:17.80449,2:17.80449):2.22049,3:20.02498):1.18822,4:21.2132)";
		assertEquals("Comparing graph outputs", expectedOutput, generatedOutput);
	}
}