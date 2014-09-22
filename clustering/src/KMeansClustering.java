import java.io.File;
import java.util.Scanner;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class KMeansClustering {

	public static void main(String[] args) {
		doClustering(
				"C://Users//carlosg//Desktop//CSSL//zlab-0.1//sampledocs//",
				"5");
	}

	public static void doClustering(String inputDir, String clusterSize) {
		try {
			
			int numOfClusters = Integer.parseInt(clusterSize.toString());
			StringToWordVector filter = new StringToWordVector();
			SimpleKMeans kmeans = new SimpleKMeans();

			FastVector atts = new FastVector(1);
			atts.addElement(new Attribute("text", (FastVector) null));

			Instances docs = new Instances("text_files_in_" + inputDir, atts, 0);

			File dir = new File(inputDir);
			File[] files = dir.listFiles();
	
			for (int i = 0; i < files.length; i++) {

				try {
					double[] newInst = new double[1];
					String content = new Scanner(files[i]).useDelimiter("\\Z").next();
					System.out.println("Content is " + content);
					newInst[0] = (double) docs.attribute(0).addStringValue(content);
					docs.add(new Instance(1.0, newInst));
				} catch (Exception e) {
					System.out.println("Exception occurred in reading files" + e);
				}
			}

			filter.setInputFormat(docs);
			Instances filteredData  = Filter.useFilter(docs, filter);
			
			kmeans.setPreserveInstancesOrder(true);
			kmeans.setNumClusters(numOfClusters);
			kmeans.buildClusterer(filteredData);
			int[] assignments = kmeans.getAssignments();

			int i = 0;
			for (int clusterNum : assignments) {
				System.out.printf("Instance %d -> Cluster %d \n", i, clusterNum);
				i++;
			}
		} catch (Exception e) {
			System.out.println("Exception occurred in K means " + e);
		}
	}
}