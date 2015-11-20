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
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.core.runtime.IProgressMonitor;

import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.NominalPrediction;
import weka.core.FastVector;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

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
//	public static void main(String[] args) throws Exception {
//		String[] classes = {
//				"F:\\NLP\\Naive Bayes Classifier\\2 Class Analysis\\Train\\Ham",
//				"F:\\NLP\\Naive Bayes Classifier\\2 Class Analysis\\Train\\Spam" };
//	//	DirectoryToArff.createTrainInstances(classes);
//
//		Instances dataRaw =null;// DirectoryToArff.loadArff();
//		StringToWordVector filter = new StringToWordVector();
//		filter.setInputFormat(dataRaw);
//		Instances dataFiltered = Filter.useFilter(dataRaw, filter);
//
//		final Classifier nbc = createClassifier(dataFiltered);
//		crossValidate(nbc, dataFiltered, 2);
//
////		classify(
////				nbc,
////				"F:\\NLP\\Naive Bayes Classifier\\2 Class Analysis\\Classify\\Input",
////				dataFiltered, filter);
//	}

	public boolean doCrossValidate(int k, IProgressMonitor monitor, Date dateObj)throws Exception  {
		
		crossValidate(nbc, dataFiltered, k);		
		return true;
	}

	public boolean  doClassify(String classificationInputDir, String classificationOutputDir,
			IProgressMonitor monitor,Date dateObj) throws Exception {
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
		ConsoleView.printlInConsoleln("Classification starts ..");
		String outputPath = classificationOutputDir
				+ System.getProperty("file.separator") +"Naive_Bayes_classification_results"
				+ "-" + df.format(dateObj);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				outputPath + "-output.csv")));
		Instances rawTestData = new DirectoryToArff().createTestInstances(classificationInputDir);
		Instances filteredTestData = Filter.useFilter(rawTestData, filter);
		Evaluation testEval = new Evaluation(dataFiltered);
		testEval.evaluateModel(nbc, filteredTestData);
		FastVector predictions = testEval.predictions();
		for (int i = 0; i < predictions.size(); i++) {
			NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
			int pred = (int) np.predicted();
			bw.write(DirectoryToArff.instanceIdNameMap.get(i) + "\t"
					+ dataFiltered.classAttribute().value(pred) +"\n");
		}
		return true;
	}

	private static Classifier createClassifier(Instances dataFiltered)
			throws Exception {
		Classifier classifier = new NaiveBayes();
		classifier.buildClassifier(dataFiltered);
		return classifier;
	}

	private static void crossValidate(Classifier nbc, Instances dataFiltered,
			int k) throws Exception {
		Evaluation eval = new Evaluation(dataFiltered);
		eval.crossValidateModel(nbc, dataFiltered, k, new Random(1));
		ConsoleView.printlInConsoleln(eval.toSummaryString("\nResults\n======\n", false));
		double[][] confusion = eval.confusionMatrix();
		for (int i = 0; i < confusion.length; i++) {
			for (int j = 0; j < confusion[0].length; j++)
				ConsoleView.printlInConsole(confusion[i][j] + "\t");
			ConsoleView.printlInConsoleln();
		}
		ConsoleView.printlInConsoleln("Accuracy:" + calculateAccuracy(eval.predictions()));
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
