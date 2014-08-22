/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */

package edu.usc.pil.nlputils.plugins.svmClassifier.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class PredictiveWeights {

	public HashMap<Integer, Double> computePredictiveWeights(File modelFile) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(modelFile));
		HashMap<Integer, Double> weights = new HashMap<Integer,Double>();
		String currentLine;
		while ((currentLine = br.readLine())!=null){
			if (currentLine.equals("SV")){
				break;
			}
		}
		while ((currentLine = br.readLine())!=null){
			String[] items = currentLine.split("\\s+");
			double alpha = Double.parseDouble(items[0]);
			//System.out.println(alpha);
			for (int i = 1;i<items.length;i++) {
				String[] pair = items[i].split(":");
				int featureID = Integer.parseInt(pair[0]);
				double weight = Double.parseDouble(pair[1]);
				//System.out.println(pair[0]+" "+pair[1]);
				if (weights.containsKey(featureID)){
					weights.put(featureID, weights.get(featureID)+ (alpha*weight));
				} else {
					weights.put(featureID, alpha * weight);
				}
			}
		}
		//System.out.println(weights);
		br.close();
		return weights;
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		File modelFile = new File ("C:\\svm2weights\\linear.model");
		File weightsFile = new File("C:\\svm2weights\\weights.out");
		PredictiveWeights pw = new PredictiveWeights();
		pw.computePredictiveWeights(modelFile);
	}

}
