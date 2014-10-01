package edu.usc.pil.nlputils.plugins.CooccurrenceAnalysis.process;
import java.io.File;
import java.util.List;
import java.util.Scanner;


public class CooccurrenceAnalysis {

	public static void main(String[] args) {
		//doClustering(
		//		"C://Users//carlosg//Desktop//CSSL//zlab-0.1//sampledocs//",
		//		"5");
	}

	public static int[] calculateCooccurrences(List<File> inputFiles) {
		
		try{
			for (int i = 0; i < inputFiles.size(); i++) {

				try {
					double[] newInst = new double[2];
					
					String content = new Scanner(inputFiles.get(i)).useDelimiter("\\Z").next();
									
				} catch (Exception e) {
					System.out.println("Exception occurred in reading files" + e);
				}
			}
			
		
			
			
		
			return null;
		} catch (Exception e) {
			System.out.println("Exception occurred in Hierarchical Clustering " + e);
		}
		return null;
	}
}