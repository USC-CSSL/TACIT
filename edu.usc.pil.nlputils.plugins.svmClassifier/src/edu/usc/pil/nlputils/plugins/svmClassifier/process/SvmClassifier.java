package edu.usc.pil.nlputils.plugins.svmClassifier.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;

import edu.usc.pil.nlputils.plugins.svmClassifier.utilities.Convertor;

public class SvmClassifier {
	private int featureMapIndex=0;		// Keeps track of num of features for calculating the index of the next word
	private HashMap<String,Integer> featureMap = new HashMap<String,Integer>();		// Stores Numerical ID for each word
	private HashMap<String,Integer> dfMap = new HashMap<String,Integer>();		// Number of documents that contains each word
	private HashSet<String> stopWordsSet = new HashSet<String>();
	private String delimiters = " .,;'\"!-()[]{}:?";
	private String dateString;
	private File modelFile;
	private boolean doLowercase = true;
	private boolean doStopWords = true;
	private String intermediatePath;
	private int noOfDocuments = 0;
	private boolean doTfidf = false;
	
	public SvmClassifier(boolean doLowercase, String delimiters, String stopwordsFile) throws IOException{
		this.delimiters = delimiters;
		this.doLowercase = doLowercase;
		
			if (stopwordsFile != null && !stopwordsFile.isEmpty()){
				// If stopwordsFile is not given, doStopWords is false by default. Check only if it's not empty
				File sFile = new File(stopwordsFile);
				if (!sFile.exists() || sFile.isDirectory()){
					System.out.println("Error in stopwords file path "+sFile.getAbsolutePath());
					appendLog("Error in stopwords file path "+sFile.getAbsolutePath());
				} else {
					doStopWords = true;
					String currentLine;
					BufferedReader br = new BufferedReader(new FileReader(sFile));
					while ((currentLine = br.readLine())!=null){
						stopWordsSet.add(currentLine.trim().toLowerCase());
					}
					br.close();
				}
				
		}
	}
	
	public void buildDfMap(File inputFile) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String currentLine;
		StringBuilder fullFile = new StringBuilder();
		while ((currentLine = br.readLine())!=null){
			if (doLowercase){
				currentLine = currentLine.toLowerCase();
			}
			fullFile.append(currentLine+' ');
		}
		String input = fullFile.toString();
		for (char c:delimiters.toCharArray())
			input = input.replace(c, ' ');
		if (doStopWords)
			input = removeStopWords(input);
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
	
	// Should convert each text file to wordcount (Bag of words) map (bag of words)
	public HashMap<String, Double> fileToBow (File inputFile) throws IOException{
		HashMap<String, Double> hashMap = new HashMap<String,Double>();
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String currentLine;
		
		// Converting the file to one string for faster processing
		StringBuilder fullFile = new StringBuilder();
		while((currentLine = br.readLine())!=null){
			if (doLowercase){
				currentLine = currentLine.toLowerCase();
			}
			fullFile.append(currentLine+' ');
		}
		//System.out.println(fullFile);
		String input = fullFile.toString();
		for (char c:delimiters.toCharArray())
			input = input.replace(c, ' ');
		
		if (doStopWords)
			input = removeStopWords(input);
		//System.out.println(input);
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
				//System.out.println(word+" - "+noOfDocuments+"/"+(double)docsContaining);
			} else {
				continue;		// If new word, none of the training documents will contain it. So, skip.
			}
			hashMap.put(word, tfidf);
		}
		}
		//System.out.println(hashMap);
		br.close();
		return hashMap;
	}
	
	public String removeStopWords(String currentLine){
		StringBuilder returnString = new StringBuilder();
		String[] wordArray = currentLine.split("\\s+");
		for (String word : wordArray){
			if (!stopWordsSet.contains(word.toLowerCase())){
				returnString.append(word);
				returnString.append(' ');
			}
		}
		return returnString.toString();
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
		//System.out.println(integerMap.toString());
		//System.out.println(bow.toString());
		StringBuilder sb = new StringBuilder();
		for (int i:integerMap.keySet()){
			sb.append(i+":"+integerMap.get(i)+" ");
		}
		//System.out.println(sb.toString().trim());
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

	public int train(String label1, String folderPath1, String label2, String folderPath2, boolean doTfidf, boolean doCrossVal, String kVal) throws IOException{
		int ret = 0;
		File folder1 = new File(folderPath1);
		File folder2 = new File(folderPath2);
		Calendar cal = Calendar.getInstance();
		dateString = ""+(cal.get(Calendar.MONTH)+1)+"-"+cal.get(Calendar.DATE)+"-"+cal.get(Calendar.YEAR);
		intermediatePath = System.getProperty("user.dir")+"\\"+label1+"_"+label2+"_"+dateString+"-"+System.currentTimeMillis();
		modelFile = new File(intermediatePath+".model");
		File trainFile = new File(intermediatePath+".train");
		this.doTfidf = doTfidf;
		

		// if TFIDF method, build df map
		dfMap.clear();
		noOfDocuments = 0;
		if (doTfidf){
		for (File file:folder1.listFiles()){
			noOfDocuments = noOfDocuments+1;	// Count the total no of documents
			buildDfMap(file);
		}
		for (File file:folder2.listFiles()){
			noOfDocuments = noOfDocuments+1;	// Count the total no of documents
			buildDfMap(file);
		}
		//System.out.println("dfmap -"+dfMap);
		System.out.println("Finished building document frequency map.");
		appendLog("Finished building document frequency map.");
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(trainFile));
		
		for (File file:folder1.listFiles()){
			System.out.println("Reading File "+file.toString());
			bw.write("+1 "+BowToString(fileToBow(file)));
			bw.newLine();
		}
		for (File file:folder2.listFiles()){
			System.out.println("Reading File "+file.toString());
			bw.write("-1 "+BowToString(fileToBow(file)));
			bw.newLine();
		}
		System.out.println("Total number of documents - "+noOfDocuments+". Total unique features - "+featureMapIndex);
		System.out.println("Finished building SVM-format training file - "+trainFile.getAbsolutePath());
		appendLog("Total number of documents - "+noOfDocuments+". Total unique features - "+featureMapIndex);
		appendLog("Finished building SVM-format training file - "+trainFile.getAbsolutePath());
		bw.close();
		
		String[] train_arguments;
		if (doCrossVal){
			train_arguments = new String[4];
			train_arguments[0] = "-v";
			train_arguments[1] = kVal;
			train_arguments[2] = trainFile.getAbsolutePath();
			train_arguments[3] = modelFile.getAbsolutePath();
		}else {
		train_arguments = new String[2];
		train_arguments[0] = trainFile.getAbsolutePath();
		train_arguments[1] = modelFile.getAbsolutePath();
		}
		System.out.println("Training the classifier...");
		appendLog("Training the classifier...");
		double crossValResult = svm_train.main(train_arguments);
		System.out.println("Model file created - "+modelFile.getAbsolutePath());
		appendLog("Model file created - "+modelFile.getAbsolutePath());
		
		// Saving the feature map
		File hashmap = new File(intermediatePath+".hashmap");
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(hashmap));
		oos.writeObject(featureMap);
		oos.flush();
		oos.close();
		System.out.println("Feature Map saved - "+hashmap.getAbsolutePath());
		appendLog("Feature Map saved - "+hashmap.getAbsolutePath());
		
		if (doCrossVal){
			System.out.println("Cross Validation Accuracy = "+crossValResult+"%");
			appendLog("Cross Validation Accuracy = "+crossValResult+"%");
		}
		return ret;
	}
	
	public int output(String label1, String folderPath1, String label2, String folderPath2, String outputFilePath) throws IOException{
		int ret = 0;
		File dir1 = new File (folderPath1);
		File dir2 = new File (folderPath2);
		String tempOut = intermediatePath+".out";
		BufferedReader brt = new BufferedReader(new FileReader(new File(tempOut)));
		File outputFile = new File(outputFilePath);
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		bw.write("File Name,Actual Class,Predicted Class\n");
		String actualLabel, predictedLabel;
		actualLabel = label1;
		for (File file:dir1.listFiles()){
			String currPrediction = brt.readLine();
			if (currPrediction.equals("1.0")){
				predictedLabel = label1;
			} else{
				predictedLabel = label2;
			}
			bw.write(file.getAbsolutePath()+","+actualLabel+","+predictedLabel+"\n");
		}
		actualLabel = label2;
		for (File file:dir2.listFiles()){
			String currPrediction = brt.readLine();
			if (currPrediction.equals("1.0")){
				predictedLabel = label1;
			} else{
				predictedLabel = label2;
			}
			bw.write(file.getAbsolutePath()+","+actualLabel+","+predictedLabel+"\n");
		}
		System.out.println("Created output file "+outputFilePath);
		appendLog("Created output file "+outputFilePath);
		bw.close();
		brt.close();
		return ret;
	}
	
	public int outputPredictedOnly(String label1, String label2, String inputPath, String outputFilePath) throws IOException{
		int ret = 0;
		File dir = new File (inputPath);
		String tempOut = intermediatePath+".out";
		BufferedReader brt = new BufferedReader(new FileReader(new File(tempOut)));
		File outputFile = new File(outputFilePath);
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		bw.write("File Name,Predicted Class\n");
		String predictedLabel;
		for (File file:dir.listFiles()){
			String currPrediction = brt.readLine();
			if (currPrediction.equals("1.0")){
				predictedLabel = label1;
			} else{
				predictedLabel = label2;
			}
			bw.write(file.getAbsolutePath()+","+predictedLabel+"\n");
		}
		System.out.println("Created output file "+outputFilePath);
		appendLog("Created output file "+outputFilePath);
		bw.close();
		brt.close();
		return ret;
	}
			
	public int predict(String label1, String folderPath1, String label2, String folderPath2) throws IOException{
		int ret = 0;
		File folder1 = new File(folderPath1);
		File folder2 = new File(folderPath2);
		
		
		// if TFIDF method, clear and rebuild df map
				dfMap.clear();
				noOfDocuments = 0;
				if (doTfidf){
				for (File file:folder1.listFiles()){
					noOfDocuments = noOfDocuments + 1;
					buildDfMap(file);
				}
				for (File file:folder2.listFiles()){
					noOfDocuments = noOfDocuments + 1;
					buildDfMap(file);
				}
				//System.out.println("dfmap -"+dfMap);
				System.out.println("Finished building document frequency map.");
				appendLog("Finished building document frequency map.");
				}
				
		// Create a test file just like the training file was created.
		// Use the existing featureMap, ignore new words.
		File testFile = new File(intermediatePath+".test");
		BufferedWriter bw = new BufferedWriter(new FileWriter(testFile));
		
		for (File file:folder1.listFiles()){
			System.out.println("Reading File "+file.toString());
			bw.write("+1 "+BowToTestString(fileToBow(file)));
			bw.newLine();
		}
		for (File file:folder2.listFiles()){
			System.out.println("Reading File "+file.toString());
			bw.write("-1 "+BowToTestString(fileToBow(file)));
			bw.newLine();
		}
		System.out.println("Finished building SVM-format test file - "+testFile.getAbsolutePath());
		appendLog("Finished building SVM-format test file - "+testFile.getAbsolutePath());
		bw.close();
		System.out.println("Model file loaded - "+modelFile.getAbsolutePath());
		appendLog("Model file loaded - "+modelFile.getAbsolutePath());
		String[] predict_arguments = new String[3];
		predict_arguments[0] = testFile.getAbsolutePath();
		predict_arguments[1] = modelFile.getAbsolutePath();
		predict_arguments[2] = intermediatePath+".out";
		int[] result = svm_predict.main(predict_arguments);
		int correct = result[0], total = result[1];
		System.out.println("Created SVM output file - "+intermediatePath+".out");
		appendLog("Created SVM output file - "+intermediatePath+".out");
		System.out.println("Accuracy = "+(double)correct/total*100+"% ("+correct+"/"+total+") (classification)\n");
		appendLog("Accuracy = "+(double)correct/total*100+"% ("+correct+"/"+total+") (classification)\n");
		//System.out.println(featureMap.toString());
		return ret;
	}
	
	public int loadPretrainedModel(String label1, String label2, String modelFilePath, String hashmapPath) throws FileNotFoundException, IOException, ClassNotFoundException{
		int ret = 0;
		System.out.println("Loading Model File "+modelFilePath);
		this.modelFile = new File(modelFilePath);
		//String fileName = modelFilePath.substring(0,modelFilePath.lastIndexOf('.'));
		//File hashmap = new File(fileName+".hashmap");
		File hashmap = new File(hashmapPath);
		System.out.println("Loading Feature Map "+hashmap.getAbsolutePath());
		
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(hashmap));
		this.featureMap = (HashMap<String,Integer>)ois.readObject();
		//System.out.println(featureMap);
		Calendar cal = Calendar.getInstance();
		dateString = ""+(cal.get(Calendar.MONTH)+1)+"-"+cal.get(Calendar.DATE)+"-"+cal.get(Calendar.YEAR);
		this.intermediatePath = System.getProperty("user.dir")+"\\"+label1+"_"+label2+"_"+dateString+"-"+System.currentTimeMillis();
		return ret;
	}
	
	public int classify(String label1, String label2, String inputPath) throws IOException{
		int ret = 0;
		File folder = new File(inputPath);
		
		// if TFIDF method, clear and rebuild df map
				dfMap.clear();
				noOfDocuments = 0;
				if (doTfidf){
				for (File file:folder.listFiles()){
					noOfDocuments = noOfDocuments + 1;
					buildDfMap(file);
				}
				//System.out.println("dfmap -"+dfMap);
				System.out.println("Finished building document frequency map.");
				appendLog("Finished building document frequency map.");
				}
				
		// Create a test file just like the training file was created.
		// Use the existing featureMap, ignore new words.
		File testFile = new File(intermediatePath+".test");
		BufferedWriter bw = new BufferedWriter(new FileWriter(testFile));
		
		for (File file:folder.listFiles()){
			System.out.println("Reading File "+file.toString());
			bw.write("0 "+BowToTestString(fileToBow(file)));
			bw.newLine();
		}

		System.out.println("Finished building SVM-format test file - "+testFile.getAbsolutePath());
		appendLog("Finished building SVM-format test file - "+testFile.getAbsolutePath());
		bw.close();
		System.out.println("Model file loaded - "+modelFile.getAbsolutePath());
		appendLog("Model file loaded - "+modelFile.getAbsolutePath());
		String[] predict_arguments = new String[3];
		predict_arguments[0] = testFile.getAbsolutePath();
		predict_arguments[1] = modelFile.getAbsolutePath();
		predict_arguments[2] = intermediatePath+".out";
		int[] result = svm_predict.main(predict_arguments);
		int correct = result[0], total = result[1];
		System.out.println("Created SVM output file - "+intermediatePath+".out");
		appendLog("Created SVM output file - "+intermediatePath+".out");		
		//System.out.println("Accuracy = "+(double)correct/total*100+"% ("+correct+"/"+total+") (classification)\n");
		//appendLog("Accuracy = "+(double)correct/total*100+"% ("+correct+"/"+total+") (classification)\n");
		//System.out.println(featureMap.toString());
		return ret;
	}
//	public void classify(String testFile, String modelFile, String outputFilePath) throws IOException{
//		String[] predict_arguments = new String[3];
//		predict_arguments[0] = testFile;
//		predict_arguments[1] = modelFile;
//		predict_arguments[2] = outputFilePath;
//		int[] result = svm_predict.main(predict_arguments);
//		int correct = result[0], total = result[1];
//		System.out.println("Accuracy = "+(double)correct/total*100+"% ("+correct+"/"+total+") (classification)\n");
//		appendLog("Accuracy = "+(double)correct/total*100+"% ("+correct+"/"+total+") (classification)\n");
//	}
	
	public static void main(String[] args) throws IOException {
		SvmClassifier svm = new SvmClassifier(true," .,;'\"!-()[]{}:?",null);
		File folder1 = new File("c:\\test\\svm\\ham");
		File folder2 = new File("c:\\test\\svm\\spam");
		File formatFile = new File("c:\\test\\svm\\formatted"+".train");
		String modelFilePath = "c:\\test\\svm\\formatted"+".model";
		String testFilePath = "c:\\test\\svm\\a1a.t"+".test";
		String outputFilePath = "c:\\test\\svm\\predict.out";
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(formatFile));
		
		for (File file:folder1.listFiles()){
			System.out.println("Reading File "+file.toString());
			//System.out.println("+1 "+svm.BowToString(svm.fileToBow(file)));
			bw.write("+1 "+svm.BowToString(svm.fileToBow(file)));
			bw.newLine();
		}
		for (File file:folder2.listFiles()){
			System.out.println("Reading File "+file.toString());
			//System.out.println("-1 "+svm.BowToString(svm.fileToBow(file)));
			bw.write("-1 "+svm.BowToString(svm.fileToBow(file)));
			bw.newLine();
		}
		System.out.println("Total unique features - "+svm.featureMapIndex);
		System.out.println("Finished building SVM formatted file - "+formatFile.getAbsolutePath());
		bw.close();
		
		String[] train_arguments = new String[2];
		//arguments[0] = "svm_train";
		train_arguments[0] = formatFile.getAbsolutePath();
		train_arguments[0] = "c:\\test\\svm\\a1a.train";
		train_arguments[1] = modelFilePath;
		svm_train.main(train_arguments);
		System.out.println("Model file created - "+modelFilePath);
		
		
		// Modify the above steps to create a test file.
		// Use the existing featureMap, ignore new words.
		
		String[] predict_arguments = new String[3];
		predict_arguments[0] = testFilePath;
		predict_arguments[1] = modelFilePath;
		predict_arguments[2] = outputFilePath;
		int[] result = svm_predict.main(predict_arguments);
		int correct = result[0], total = result[1];
		System.out.println("Accuracy = "+(double)correct/total*100+"% ("+correct+"/"+total+") (classification)\n");
	}
	
	// This function updates the consoleMessage parameter of the context.
	@Inject IEclipseContext context;
	private void appendLog(String message){
		if (!(context==null)){
		IEclipseContext parent = context.getParent();
		parent.set("consoleMessage", message);
		}
	}


}
