package edu.usc.cssl.nlputils.plugins.hierarchicalclustering.process;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import weka.clusterers.HierarchicalClusterer;
import weka.core.Attribute;
import weka.core.Drawable;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class HierarchicalClustering {

	public static void main(String[] args) {
		String dir = "/home/niki/Desktop/CSSL/Clustering/sampledocs/";
		File file = new File("/home/niki/Desktop/CSSL/Clustering/sampledocs/");
		List<File> files = new ArrayList<File>();
		for(String f : file.list()){
			files.add(new File(dir+f));
			System.out.println(f);
		}
		
		doClustering(files,"/home/niki/Desktop/CSSL/Clustering/sampledocs/");
	}

	public static String doClustering(List<File> inputFiles, String outputPath) {
		try {
			
			StringToWordVector filter = new StringToWordVector();
			HierarchicalClusterer aggHierarchical = new HierarchicalClusterer();

			FastVector atts = new FastVector(1);
			atts.addElement(new Attribute("text", (FastVector) null));

			Instances docs = new Instances("text_files", atts, 0);

			
			
	
			for (int i = 0; i < inputFiles.size(); i++) {

				try {
					double[] newInst = new double[1];
					String content = new Scanner(inputFiles.get(i)).useDelimiter("\\Z").next();
					newInst[0] = (double) docs.attribute(0).addStringValue(content);
					docs.add(new Instance(1.0, newInst));
				} catch (Exception e) {
					System.out.println("Exception occurred in reading files" + e);
				}
			}

			filter.setInputFormat(docs);
			Instances filteredData  = Filter.useFilter(docs, filter);
			
			
			aggHierarchical.setNumClusters(1);
			aggHierarchical.buildClusterer(filteredData);
			int numClusters = aggHierarchical.numberOfClusters();
			String g = aggHierarchical.printNewickTipText();
			System.out.println("Network " + aggHierarchical.toString());
			
			BufferedWriter buf = new BufferedWriter(new FileWriter (new File(outputPath+"cluster.txt")));
			buf.write(g);
			buf.close();
			
			return g;
		} catch (Exception e) {
			System.out.println("Exception occurred in K means " + e);
		}
		return null;
	}
}