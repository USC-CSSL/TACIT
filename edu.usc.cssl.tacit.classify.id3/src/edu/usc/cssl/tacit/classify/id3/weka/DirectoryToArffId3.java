package edu.usc.cssl.tacit.classify.id3.weka;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;
import weka.core.converters.ArffSaver;

/**
 * Class that converts given directory to attribute file required for Weka
 */
public class DirectoryToArffId3 {
	// Attribute file
	static String arffFilename = System.getProperty("user.dir") + File.separator + "rawData.arff";
	
	// Training
	static FastVector trainAtts;
	static Instances trainData;
	
	// Testing
	static FastVector testAtts;
	static Instances testData;
	static public HashMap<Integer, String> instanceIdNameMap;
	static int instanceId;
	
	
	/**
	 * Initialize training instances/classes
	 * @param classes - set of training classes
	 */
	private  static void initailizeTrainInstances(Set<String> classes) {
		FastVector cNames = new FastVector(classes.size());
		for(String c: classes)
			cNames.addElement(c);
		trainAtts = new FastVector(2);		
		trainAtts.addElement(new Attribute("text", (FastVector) null));
		trainAtts.addElement(new Attribute("class", cNames));
		trainData = new Instances("text_files", trainAtts, 0);	
	}
	
	/**
	 * Initialize testing instances/classes
	 */
	private  void initializeTestInstances(Set<String> classes) {
		instanceId = 0;
		instanceIdNameMap = new HashMap<Integer, String>();
		FastVector cNames = new FastVector(classes.size());
		for(String c: classes)
			cNames.addElement(c);
		cNames.addElement("?");
		testAtts = new FastVector(2);
		testAtts.addElement(new Attribute("text", (FastVector) null));
		testAtts.addElement(new Attribute("class", cNames));
		testData = new Instances("text_files", testAtts, 0);
		
	}	

	/***
	 * Converts the file to instance format and add it to the given instance
	 * @param filename
	 * @param className
	 * @param data
	 * @param isTestData
	 * @throws IOException
	 */
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

	public  static void createDataset(List<String> files, String className, Instances data, boolean isTestData) throws Exception {
		for(String file : files) {
			createInstance(file, className, data, isTestData);
		}
	}
	
	/***
	 * Create instance from the given directory path
	 */
	public  void createDataset(String directoryPath, String className, Instances data, boolean isTestData) throws Exception {
		File dir = new File(directoryPath);
		String[] files = dir.list();
		for (int i = 0; i < files.length; i++) {
			//System.out.println(directoryPath + File.separator + files[i]);
			createInstance(directoryPath + File.separator + files[i], className, data, isTestData);
		}
	}
	
	/**
	 * Save the instance as an attribute file
	 * @throws IOException
	 */
	public static void saveInstance() throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(trainData);
		saver.setFile(new File(arffFilename));
		saver.writeBatch();	
	}
	
	/**
	 * Create an instance from given list of strings
	 * @param classes - maps the directory and its files 
	 * @throws Exception
	 */
	public void createTrainInstances(Map<String,List<String>> classes) throws Exception {
		initailizeTrainInstances(classes.keySet());
		for(String c : classes.keySet()) {
			System.out.println("Training dir :" + c);
			createDataset(classes.get(c), c, trainData, false);
		}
		saveInstance();
	}

	/**
	 * Creates an instance out of the given input directory
	 * @param input - input directory path
	 * @return - Instance
	 * @throws Exception
	 */
	public Instances createTestInstances(String input,Map<String,List<String>> classes) throws Exception {
		initializeTestInstances(classes.keySet());
		createDataset(input, "?", testData, true);
		//System.out.println("**************************TestData**********************");
		//System.out.println(testData);
		return testData;
	}

	/**
	 * Loads attribute file as an instance for Weka
	 * @return - Instance
	 * @throws IOException
	 */
	public  Instances loadArff() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(arffFilename));
		ArffReader arff = new ArffReader(reader);
		Instances data = arff.getData();
		data.setClassIndex(data.numAttributes() - 1);
		reader.close();
		//System.out.println("**************************TrainData**********************");
		//System.out.println(data);
		return data;
	}
}
