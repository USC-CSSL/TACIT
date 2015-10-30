package edu.usc.cssl.tacit.classify.naivebayes.weka;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;
import weka.core.converters.ArffSaver;

public class DirectoryToArff {
	static String resultsDirs = "F:\\Weka_temp";
	static String arffFilename = resultsDirs + File.separator + "rawData.arff";
	
	// Training
	static FastVector trainAtts;
	static Instances trainData;
	
	// Testing
	static FastVector testAtts;
	static Instances testData;
	static public HashMap<Integer, String> instanceIdNameMap;
	static int instanceId;
	
	private static void initailizeTrainInstances(ArrayList<String> classes) {
		FastVector cNames = new FastVector(classes.size());
		for(String c: classes)
			cNames.addElement(c);
		trainAtts = new FastVector(2);		
		trainAtts.addElement(new Attribute("text", (FastVector) null));
		trainAtts.addElement(new Attribute("class", cNames));
		trainData = new Instances("text_files", trainAtts, 0);	
	}
	
	private static void initializeTestInstances() {
		instanceId = 0;
		instanceIdNameMap = new HashMap<Integer, String>();
		FastVector cNames = new FastVector(1);
		cNames.addElement("?");
		testAtts = new FastVector(2);
		testAtts.addElement(new Attribute("text", (FastVector) null));
		testAtts.addElement(new Attribute("class", cNames));
		testData = new Instances("text_files", testAtts, 0);	
	}	
	
	private static String getClassName(String directoryPath) {
		String[] segments = directoryPath.split("\\\\");
		return segments[segments.length - 1];
	}

	private static void createInstance(String filename, String className, Instances data, boolean isTestData) throws IOException {
		File txt = new File(filename);
		if (txt.isDirectory()) { // process sub folders
			for (File fn : txt.listFiles())
				createInstance(fn.getAbsolutePath(), className, data, isTestData);
		} else { // if its  f
			double[] newInst = new double[data.numAttributes()];			
			InputStreamReader is = new InputStreamReader(new FileInputStream(txt));
			StringBuffer txtStr = new StringBuffer();
			int c;
			while ((c = is.read()) != -1) {
				txtStr.append((char) c);
			}
			newInst[0] = data.attribute(0).addStringValue(txtStr.toString());
			newInst[1] = data.attribute(1).indexOfValue(className);
			Instance inst = new Instance(1.0, newInst);			
			if(isTestData)
				instanceIdNameMap.put(instanceId++, filename);
			data.add(inst);
		}
	}

	public static void createDataset(String directoryPath, String className, Instances data, boolean isTestData) throws Exception {
		File dir = new File(directoryPath);
		String[] files = dir.list();
		for (int i = 0; i < files.length; i++) {
			createInstance(directoryPath + File.separator + files[i], className, data, isTestData);
		}
	}
	
	public static void saveInstance() throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(trainData);
		saver.setFile(new File(arffFilename));
		saver.writeBatch();	
	}
	
	
	public static void createTrainInstances(String[] classes) throws Exception {
		initailizeTrainInstances(getClassNames(classes));
		for(String c : classes) {
			createDataset(c, getClassName(c), trainData, false);
		}
		saveInstance();
	}

	public static Instances createTestInstances(String input) throws Exception {
		initializeTestInstances();
		createDataset(input, "?", testData, true);
		return testData;
	}
	
	private static ArrayList<String> getClassNames(String[] classes) {
		ArrayList<String> classNames = new ArrayList<String>();
		for(String s: classes)
			classNames.add(getClassName(s));
		return classNames;
	}

	public static Instances loadArff() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(arffFilename));
		ArffReader arff = new ArffReader(reader);
		Instances data = arff.getData();
		data.setClassIndex(data.numAttributes() - 1);
		reader.close();
		return data;
	}

	
	/* Testing //
	public static void main(String[] args) throws Exception {
		String dir = "F:\\NLP\\Naive Bayes Classifier\\2 Class Analysis\\Test\\Ham";
		DirectoryToArff tdta = new DirectoryToArff();
		tdta.createDataset(dir);
		System.out.println("Done!");
	}
	*/
}
