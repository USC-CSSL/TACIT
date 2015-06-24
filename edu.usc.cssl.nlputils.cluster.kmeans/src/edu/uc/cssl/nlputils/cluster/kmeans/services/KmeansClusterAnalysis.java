package edu.uc.cssl.nlputils.cluster.kmeans.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;
import edu.usc.cssl.nlputils.common.ui.views.ConsoleView;
import edu.usc.nlputils.common.TacitUtility;

public class KmeansClusterAnalysis {
	private static int[] doClustering(List<File> inputFiles, int numOfClusters) {
		try {

			StringToWordVector filter = new StringToWordVector();
			SimpleKMeans kmeans = new SimpleKMeans();

			FastVector atts = new FastVector(1);
			atts.addElement(new Attribute("text", (FastVector) null));

			Instances docs = new Instances("text_files", atts, 0);

			for (int i = 0; i < inputFiles.size(); i++) {

				try {
					double[] newInst = new double[1];
					String content = new Scanner(inputFiles.get(i))
							.useDelimiter("\\Z").next();
					newInst[0] = (double) docs.attribute(0).addStringValue(
							content);
					docs.add(new Instance(1.0, newInst));
				} catch (Exception e) {
					ConsoleView.printlInConsoleln("Exception occurred in reading files"
							+ e);
					return null;
				}
			}

			filter.setInputFormat(docs);
			Instances filteredData = Filter.useFilter(docs, filter);

			kmeans.setPreserveInstancesOrder(true);
			kmeans.setNumClusters(numOfClusters);
			kmeans.buildClusterer(filteredData);
			int[] assignments = kmeans.getAssignments();

			int i = 0;
			for (int clusterNum : assignments) {
				System.out
						.printf("Instance %d -> Cluster %d \n", i, clusterNum);
				i++;
			}

			return assignments;
		} catch (Exception e) {
			ConsoleView.printlInConsoleln("Exception occurred in K means " + e);
		}
		return null;
	}

	public static void runClustering(int fNumClusters, List<File> listOfFiles,
			String fOutputDir) {

		List<File> inputFiles = new ArrayList<File>();
		for (File f : listOfFiles) {
			if (f.getAbsolutePath().contains("DS_Store"))
				continue;
			inputFiles.add(f);
		}

		ConsoleView.printlInConsoleln("Running KMeans Clustering...");

		int[] clusters = doClustering(inputFiles, fNumClusters);
		if (clusters == null) {
			ConsoleView.printlInConsoleln("Sorry. Something went wrong with KMeans Clustering. Please check your input and try again.\n");
			return;
		}
		int i = 0;
		ConsoleView.printlInConsoleln("Output for KMeans Clustering");
		ConsoleView.printlInConsoleln("Clusters formed: \n");

		Map<Integer, List<String>> outputClusters = new HashMap<Integer, List<String>>();
		for (i = 0; i < fNumClusters; i++) {
			outputClusters.put(i, new ArrayList<String>());
		}

		List<String> vec;
		i = 0;
		for (int clusterNum : clusters) {
			vec = outputClusters.get(clusterNum);
			vec.add(inputFiles.get(i).getName());
			outputClusters.put(clusterNum, vec);
			i++;
		}

		try {
			String op = fOutputDir + File.separator + "KMeansClusters.txt";
			ConsoleView.printlInConsoleln("Saving the output for Kmeans clustering in " + op);
			FileWriter fw = new FileWriter(new File(op));
			for (int c : outputClusters.keySet()) {
				System.out.printf("Cluster %d \n", c);
				ConsoleView.printlInConsoleln("Cluster " + c + ": \n");
				fw.write("Cluster " + c + ": \n");
				vec = outputClusters.get(c);
				for (String f : vec) {
					ConsoleView.printlInConsoleln("File " + f);
					fw.write("File" + f + "\n");
				}
				fw.write("\n");
				ConsoleView.printlInConsoleln("");
			}
			fw.close();
			TacitUtility.createReadMe(fOutputDir, "K Means Clustering");
		} catch (IOException e) {
			ConsoleView.printlInConsoleln("Error writing output to files" + e);
		}

	}
}
