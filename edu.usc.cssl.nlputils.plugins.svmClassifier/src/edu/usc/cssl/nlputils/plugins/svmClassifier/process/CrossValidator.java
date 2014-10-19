package edu.usc.cssl.nlputils.plugins.svmClassifier.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;

public class CrossValidator {
	
	@Inject IEclipseContext context;
	
	public void doCross(SvmClassifier svm, String label1, String inputFolder1, String label2, String inputFolder2, int k, boolean doPredictiveWeights) throws IOException{
		File folder1 = new File(inputFolder1);
		File folder2 = new File(inputFolder2);
		File[] files1 = folder1.listFiles();
		File[] files2 = folder2.listFiles();
		int numFiles1 = files1.length;
		int numFiles2 = files2.length;
		
		/*
		int trains1 = (int)Math.floor((k-1)*(numFiles1/(double)(k)));
		int trains2 = (int)Math.floor((k-1)*(numFiles2/(double)(k)));
		*/
		
		int trains1 = (int)Math.floor(0.90 * numFiles1);
		int trains2 = (int)Math.floor(0.90 * numFiles2);
		
		double[] accuracies = new double[k];
		
		int index1 = 0;
		int index2 = 0;
				
		for (int i=1; i<=k;i++){
			File[] trainFiles1 = new File[trains1];
			File[] trainFiles2 = new File[trains2];
			//System.out.println("Start point 1 - "+index1);
			//System.out.println("Start point 2 - "+index2);
			//System.out.println("Train "+trains1+" files from "+inputFolder1+" and "+trains2+" files from "+inputFolder2);
			//System.out.println("Test on "+(numFiles1-trains1)+" files from "+inputFolder1+" and "+(numFiles2-trains2)+" files from "+inputFolder2);
			
			int currIndex = index1;
			for (int num =0; num<trains1;num++){
				trainFiles1[num]=files1[currIndex];
				//System.out.println(files1[currIndex]);
				currIndex++;
				if(currIndex >= numFiles1)
					currIndex=0;
			}
			
			currIndex = index2;
			for (int num =0; num<trains2;num++){
				trainFiles2[num]=files2[currIndex];
				//System.out.println(files2[currIndex]);
				currIndex++;
				if(currIndex >= numFiles2)
					currIndex=0;
			}
			
			svm.cross_train("k"+i,label1, trainFiles1, label2, trainFiles2, doPredictiveWeights);
			accuracies[i-1]=svm.cross_predict("k"+i,label1, trainFiles1, label2, trainFiles2);
			
			// Clear required globals like dfmap?
			index1 = index1 + numFiles1-trains1;
			if (index1 >= numFiles1){
				index1 = index1 - numFiles1;
			}
			
			index2 = index2 + numFiles2-trains2;
			if (index2 >= numFiles2){
				index2 = index2 - numFiles2;
			}
		}
		
		double total = 0;
		for(double i:accuracies){
			//System.out.println(i);
			total = total+i;
		}
		double mean = (total/(float)k);
		System.out.println("Mean accuracy = "+mean);
		appendLog("Mean accuracy = "+mean);
		int closest = 0;
		double distance = Math.abs(mean - accuracies[0]);
		for (int i = 0; i<accuracies.length;i++){
			double currDist = Math.abs(mean - accuracies[i]);
			if (currDist < distance){
				closest = i;
				distance = currDist;
			}
		}
		
		System.out.println("Closest accuracy to mean = "+accuracies[closest]+", for the run k"+(closest+1)+" with distance "+distance);
		appendLog("Closest accuracy to mean = "+accuracies[closest]+", for the run k"+(closest+1)+" with distance "+distance);
		
	}
	
	// This function updates the consoleMessage parameter of the context.
		private void appendLog(String message){
			if (!(context==null)){
			IEclipseContext parent = context.getParent();
			parent.set("consoleMessage", message);
			}
		}
}
