package edu.usc.cssl.nlputils.classify.svm.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.apache.commons.math3.stat.inference.AlternativeHypothesis;
import org.apache.commons.math3.stat.inference.BinomialTest;

import edu.usc.cssl.nlputils.common.ui.views.ConsoleView;


public class SVMClassify {
	private String dateString;
	private String intermediatePath;
	private File modelFile;
	private boolean doTfidf;
	private int featureMapIndex;
	private HashMap<String, Integer> featureMap = new HashMap<String,Integer>();
	private HashMap<String,Integer> dfMap = new HashMap<String,Integer>();
	private String delimiters = " .,;'\"!-()[]{}:?";
	private int noOfDocuments = 0;
	
	public SVMClassify(String class1Name, String class2Name, String outputFolder){
		Calendar cal = Calendar.getInstance();
		this.dateString = ""+(cal.get(Calendar.MONTH)+1)+"-"+cal.get(Calendar.DATE)+"-"+cal.get(Calendar.YEAR);
		this.intermediatePath = outputFolder+System.getProperty("file.separator")+class1Name+"_"+class2Name+"_"+dateString+"-"+System.currentTimeMillis();
	}
	
	public void buildDfMap(File inputFile) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		
		//ConsoleView.writeInConsole("Building map for: "+inputFile.getAbsolutePath());
		String currentLine;
		StringBuilder fullFile = new StringBuilder();
		while ((currentLine = br.readLine())!=null){
			fullFile.append(currentLine+' ');
		}
		String input = fullFile.toString();
		for (char c:delimiters.toCharArray())
			input = input.replace(c, ' ');
		HashSet<String> wordSet = new HashSet<String>();
		for (String word:input.split("\\s+")){
			wordSet.add(word);
		}
		for (String word:wordSet){
			if (!(dfMap.containsKey(word))){
				dfMap.put(word, 1);
			} else {
				dfMap.put(word, dfMap.get(word)+1);
			}
		}
		br.close();
	}
	
	public HashMap<String, Double> fileToBow (File inputFile) throws IOException{
		HashMap<String, Double> hashMap = new HashMap<String,Double>();
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String currentLine;
		
		// Converting the file to one string for faster processing
		StringBuilder fullFile = new StringBuilder();
		while((currentLine = br.readLine())!=null){
			fullFile.append(currentLine+' ');
		}
		//ConsoleView.writeInConsole(fullFile);
		String input = fullFile.toString();
		for (char c:delimiters.toCharArray())
			input = input.replace(c, ' ');
		
		//ConsoleView.writeInConsole(input);
		for (String word: input.split("\\s+")){
			if(!hashMap.containsKey(word))
				hashMap.put(word, (double)1);
			else{
				hashMap.put(word, hashMap.get(word)+1);
			}
		}
		
		// If TF.IDF method, multiply each hashMap value with IDF. IDF = log10( noOfDocuments / no of documents containing the current word)
		if (doTfidf){
		double tfidf=0;
		for (String word:hashMap.keySet()){
			Integer docsContaining;
			if ((docsContaining = dfMap.get(word))!=null){
				tfidf = hashMap.get(word) * (Math.log10(noOfDocuments / (double)docsContaining));
				//ConsoleView.writeInConsole(word+" - "+noOfDocuments+"/"+(double)docsContaining);
			} else {
				continue;		// If new word, none of the training documents will contain it. So, skip.
			}
			hashMap.put(word, tfidf);
		}
		}
		//ConsoleView.writeInConsole(hashMap);
		br.close();
		return hashMap;
	}
	
	public String BowToString(HashMap<String,Double> bow){
		TreeMap<Integer,Double> integerMap = new TreeMap<Integer,Double>();
		for (String word:bow.keySet()){
			if (featureMap.containsKey(word)){
				integerMap.put(featureMap.get(word), bow.get(word));
			} else {
				featureMapIndex = featureMapIndex+1;
				featureMap.put(word, featureMapIndex);
				integerMap.put(featureMapIndex, bow.get(word));
			}
		}
		//ConsoleView.writeInConsole(integerMap.toString());
		//ConsoleView.writeInConsole(bow.toString());
		StringBuilder sb = new StringBuilder();
		for (int i:integerMap.keySet()){
			sb.append(i+":"+integerMap.get(i)+" ");
		}
		//ConsoleView.writeInConsole(sb.toString().trim());
		return sb.toString().trim();
	}
	
	public String BowToTestString(HashMap<String,Double> bow){
		TreeMap<Integer,Double> integerMap = new TreeMap<Integer,Double>();
		for (String word:bow.keySet()){
			if (featureMap.containsKey(word)){
				integerMap.put(featureMap.get(word), bow.get(word));
			} else {
				//Ignore new words
			}
		}
		StringBuilder sb = new StringBuilder();
		for (int i:integerMap.keySet()){
			sb.append(i+":"+integerMap.get(i)+" ");
		}
		return sb.toString().trim();
	}
	
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
			//ConsoleView.writeInConsole(alpha);
			for (int i = 1;i<items.length;i++) {
				String[] pair = items[i].split(":");
				int featureID = Integer.parseInt(pair[0]);
				double weight = Double.parseDouble(pair[1]);
				//ConsoleView.writeInConsole(pair[0]+" "+pair[1]);
				if (weights.containsKey(featureID)){
					weights.put(featureID, weights.get(featureID)+ (alpha*weight));
				} else {
					weights.put(featureID, alpha * weight);
				}
			}
		}
		//ConsoleView.writeInConsole(weights);
		br.close();
		return weights;
	}
	
	public int cross_train(String kVal, String label1, File[] trainFiles1, String label2, File[] trainFiles2, boolean doPredictiveWeights) throws IOException{
		int ret = 0;
		modelFile = new File(intermediatePath+"_"+kVal+".model");
		File trainFile = new File(intermediatePath+"_"+kVal+".train");
		this.doTfidf = true;
		featureMapIndex = 0;
		featureMap.clear();
		dfMap.clear();
		noOfDocuments = 0;

		if (doTfidf){
			for (File file:trainFiles1){
				if (file.getAbsolutePath().contains("DS_Store"))
					continue;
				noOfDocuments = noOfDocuments+1;	// Count the total no of documents
				buildDfMap(file);
			}
			for (File file:trainFiles2){
				if (file.getAbsolutePath().contains("DS_Store"))
					continue;
				noOfDocuments = noOfDocuments+1;	// Count the total no of documents
				buildDfMap(file);
			}
			//ConsoleView.writeInConsole("dfmap -"+dfMap);
			ConsoleView.writeInConsole("Finished building document frequency map.");
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(trainFile));
		
		for (File file:trainFiles1){
			if (file.getAbsolutePath().contains("DS_Store"))
				continue;
			//ConsoleView.writeInConsole("Reading File "+file.toString());
			bw.write("+1 "+BowToString(fileToBow(file)));
			bw.newLine();
		}
		for (File file:trainFiles2){
			if (file.getAbsolutePath().contains("DS_Store"))
				continue;
			//ConsoleView.writeInConsole("Reading File "+file.toString());
			bw.write("-1 "+BowToString(fileToBow(file)));
			bw.newLine();
		}
		ConsoleView.writeInConsole("Total number of documents - "+noOfDocuments+". Total unique features - "+featureMapIndex);
		ConsoleView.writeInConsole("Finished building SVM-format training file - "+trainFile.getAbsolutePath());
		bw.close();
		
		String[] train_arguments;
		
		ConsoleView.writeInConsole("Linear Kernel selected");
		train_arguments = new String[4];
		train_arguments[0] = "-t";
		train_arguments[1] = "0";
		train_arguments[2] = trainFile.getAbsolutePath();
		train_arguments[3] = modelFile.getAbsolutePath();
		
		ConsoleView.writeInConsole("Training the classifier...");
		double[] result  = SVMTrain.main(train_arguments);
		double crossValResult = result[0];
		double pvalue = result[1];
		ConsoleView.writeInConsole("Model file created - "+modelFile.getAbsolutePath());
		
		// Saving the feature map
		File hashmap = new File(intermediatePath+"_"+kVal+".hashmap");
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(hashmap));
		oos.writeObject(featureMap);
		oos.flush();
		oos.close();
		ConsoleView.writeInConsole("Feature Map saved - "+hashmap.getAbsolutePath());
		
		
		HashMap<Integer,String> reverseMap = new HashMap<Integer,String>();
		for (String k:featureMap.keySet()){
			reverseMap.put(featureMap.get(k), k);
		}
		
		if (doPredictiveWeights){
		//PredictiveWeights pw = new PredictiveWeights();
		File weightsFile = new File(intermediatePath+"_weights"+"_"+kVal+".csv");
		BufferedWriter weightsWriter = new BufferedWriter(new FileWriter(weightsFile));
		//HashMap<Integer,Double> weightsMap = pw.computePredictiveWeights(modelFile);
		HashMap<Integer,Double> weightsMap = computePredictiveWeights(modelFile);
		weightsWriter.write("Word,ID,Weight\n");
		for (Integer i:weightsMap.keySet()){
			//System.out.print(i+" ");
			weightsWriter.write(reverseMap.get(i)+","+i+","+weightsMap.get(i)+"\n");
		}
		ConsoleView.writeInConsole("Created Predictive Weights file - "+weightsFile.getAbsolutePath());
		weightsWriter.close();
		}
		
		return ret;
	}
	
	public double cross_predict(String kVal, String label1, File[] testFiles1, String label2, File[] testFiles2) throws IOException{
		
		// if TFIDF method, clear and rebuild df map
		dfMap.clear();
		noOfDocuments = 0;
		if (doTfidf){
			for (File file:testFiles1){
				if (file.getAbsolutePath().contains("DS_Store"))
					continue;
				noOfDocuments = noOfDocuments + 1;
				buildDfMap(file);
			}
			for (File file:testFiles2){
				if (file.getAbsolutePath().contains("DS_Store"))
					continue;
				noOfDocuments = noOfDocuments + 1;
				buildDfMap(file);
			}
			//ConsoleView.writeInConsole("dfmap -"+dfMap);
			ConsoleView.writeInConsole("Finished building document frequency map.");
		}
				
		// Create a test file just like the training file was created.
		// Use the existing featureMap, ignore new words.
		File testFile = new File(intermediatePath+"_"+kVal+".test");
		BufferedWriter bw = new BufferedWriter(new FileWriter(testFile));
		
		for (File file:testFiles1){
			if (file.getAbsolutePath().contains("DS_Store"))
				continue;
			//ConsoleView.writeInConsole("Reading File "+file.toString());
			bw.write("+1 "+BowToTestString(fileToBow(file)));
			bw.newLine();
		}
		for (File file:testFiles2){
			if (file.getAbsolutePath().contains("DS_Store"))
				continue;
			//ConsoleView.writeInConsole("Reading File "+file.toString());
			bw.write("-1 "+BowToTestString(fileToBow(file)));
			bw.newLine();
		}
		ConsoleView.writeInConsole("Finished building SVM-format test file - "+testFile.getAbsolutePath());
		bw.close();
		ConsoleView.writeInConsole("Model file loaded - "+modelFile.getAbsolutePath());
		String[] predict_arguments = new String[3];
		predict_arguments[0] = testFile.getAbsolutePath();
		predict_arguments[1] = modelFile.getAbsolutePath();
		predict_arguments[2] = intermediatePath+"_"+kVal+".out";
		double[] result = SVMPredict.main(predict_arguments);
		int correct = (int) result[0], total = (int) result[1];
	//	double pvalue = result[2];
		BinomialTest btest = new BinomialTest();
		double p =0.5;
		double pvalue = btest.binomialTest(total, correct, p, AlternativeHypothesis.TWO_SIDED);
		ConsoleView.writeInConsole("Created SVM output file - "+intermediatePath+"_"+kVal+".out");
		ConsoleView.writeInConsole("Accuracy = "+(double)correct/total*100+"% ("+correct+"/"+total+") (classification)\n");
		ConsoleView.writeInConsole("P value  = " + pvalue);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(Integer.MAX_VALUE);
		//ConsoleView.writeInConsole(nf.format(pvalue));
		if(pvalue != 0){
			if(pvalue > 0.5)
				pvalue = Math.abs(pvalue -1);
		}
		
		return (double)correct/total*100;
	}

}
