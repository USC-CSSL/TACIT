/**
 * @author Yuvarani Shankar <yshankar@usc.edu>
 **/
package edu.usc.cssl.tacit.classify.naivebayes.weka;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.core.runtime.IProgressMonitor;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.NominalPrediction;
import weka.core.FastVector;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

/*
 * http://www.programcreek.com/2013/01/a-simple-machine-learning-example-in-java/
 */
public class NaiveBayesClassifierWeka {
	private Map<String,List<String>>clasPaths;
	private Classifier nbc;
	private Instances dataFiltered;
	private StringToWordVector filter;
	public NaiveBayesClassifierWeka(Map<String,List<String>>clasPaths) {
		this.clasPaths = clasPaths;
	}
	
	public  void initializeInstances() throws Exception{
		DirectoryToArff ref = new DirectoryToArff();
		ref.createTrainInstances(clasPaths);
		Instances dataRaw = ref.loadArff();
		filter = new StringToWordVector();
		filter.setInputFormat(dataRaw);
		dataFiltered = Filter.useFilter(dataRaw, filter);
		nbc = createClassifier(dataFiltered);
	}

	public boolean doCrossValidate(int k, IProgressMonitor monitor, Date dateObj)throws Exception  {
		crossValidate(nbc, dataFiltered, k);		
		return true;
	}

	public boolean  doClassify(String classificationInputDir, String classificationOutputDir,
			IProgressMonitor monitor,Date dateObj) throws Exception {
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
		ConsoleView.printlInConsoleln("Classification starts ..");
		Instances rawTestData = new DirectoryToArff().createTestInstances(classificationInputDir);
		Instances filteredTestData = Filter.useFilter(rawTestData, filter);
		Evaluation testEval = new Evaluation(dataFiltered);
		testEval.evaluateModel(nbc, filteredTestData);
		FastVector predictions = testEval.predictions();
		
		String outputPath = classificationOutputDir + System.getProperty("file.separator") +"Naive_Bayes_classification_results" + "-" + df.format(dateObj);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPath + "-output.csv")));
		bw.write("Filename,Predicted Class\n");
		for (int i = 0; i < predictions.size(); i++) {
			NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
			int pred = (int) np.predicted();
			bw.write(DirectoryToArff.instanceIdNameMap.get(i) + "," + getClassName(dataFiltered.classAttribute().value(pred)) +"\n");
		}
		bw.close();
		return true;
	}

	private static Classifier createClassifier(Instances dataFiltered)
			throws Exception {
		Classifier classifier = new NaiveBayes();
		classifier.buildClassifier(dataFiltered);
		return classifier;
	}

	private static void crossValidate(Classifier nbc, Instances dataFiltered, int k) throws Exception {
		ConsoleView.printlInConsoleln("Cross Validating...");
		Evaluation eval = new Evaluation(dataFiltered);
		eval.crossValidateModel(nbc, dataFiltered, k, new Random(1));
		ConsoleView.printlInConsoleln(eval.toSummaryString("\nK-fold Cross Validation Results\n", false));
		
		//printConfusionMatrix(eval.confusionMatrix()); //Not required
		
		String[] attributes = {"TP Rate", "FP Rate", "Precision", "Recall", "F-Measure", "ROC Area"};
		HashMap<String, HashMap<String, String>> detailedResults = new HashMap<String, HashMap<String, String>>();
		String[] temp = eval.toClassDetailsString().toString().split("\\n");
		for(int i = 3; i<temp.length-1; i++) {
			String[] tmp = temp[i].split("\\s+");
			String cName = getClassName(tmp[tmp.length-1]);
			HashMap<String, String> classDetails = new HashMap<String, String>();
			int index = 0;
			for(String val : tmp) {
				val = val.replaceAll("\\s", "");
				if(val.length() != 0) {
					classDetails.put(attributes[index], val);
					index++;
				}
				if(index == attributes.length) break;
			}
			detailedResults.put(cName, classDetails);
		}
		
		StringBuilder header = new StringBuilder();
		header.append("Class" + "\t");
		for(String attr : attributes) 
			header.append(attr + "\t");
		ConsoleView.printlInConsoleln(new String(header));
		
		for(String cName : detailedResults.keySet()) {
			StringBuilder cDetails = new StringBuilder();
			cDetails.append(cName + "\t");
			for(String attr : attributes) {
				cDetails.append(detailedResults.get(cName).get(attr) + "\t");
			}
			ConsoleView.printlInConsoleln(new String(cDetails));
		}
		ConsoleView.printlInConsoleln();
		ConsoleView.printlInConsoleln("\nAccuracy: " + calculateAccuracy(eval.predictions()));
	}

	private static void printConfusionMatrix(double[][] confusionMatrix) {		
		double[][] confusion = confusionMatrix;
		for (int i = 0; i < confusion.length; i++) {
			for (int j = 0; j < confusion[0].length; j++)
				ConsoleView.printlInConsole(confusion[i][j] + "\t");
			ConsoleView.printlInConsoleln();
		}		
	}

	private static String getClassName(String path) {
		String[] temp = path.split("\\\\");
		return temp[temp.length-1];
	}

	public static double calculateAccuracy(FastVector predictions) {
		double correct = 0;
		for (int i = 0; i < predictions.size(); i++) {
			NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
			if (np.predicted() == np.actual()) {
				correct++;
			}
		}
		return 100 * correct / predictions.size();
	}
}
