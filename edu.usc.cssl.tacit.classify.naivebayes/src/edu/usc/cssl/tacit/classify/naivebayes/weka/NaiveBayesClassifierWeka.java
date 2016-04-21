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
	private Map<String, List<String>> classPaths;
	private Classifier nbc;
	private Instances dataFiltered;
	private StringToWordVector filter;

	public NaiveBayesClassifierWeka(Map<String, List<String>> classPaths) {
		this.classPaths = classPaths;
	}
	
	/**
	 * Initializes instances
	 * @throws Exception
	 */
	public void initializeInstances() throws Exception {
		DirectoryToArff ref = new DirectoryToArff();
		ref.createTrainInstances(classPaths);
		Instances dataRaw = ref.loadArff();
		filter = new StringToWordVector();
		filter.setInputFormat(dataRaw);
		dataFiltered = Filter.useFilter(dataRaw, filter);
		nbc = createClassifier(dataFiltered);
	}

	/**
	 * Cross validation helper function
	 * @param k
	 * @param monitor
	 * @param dateObj
	 * @return
	 * @throws Exception
	 */
	public boolean doCrossValidate(int k, IProgressMonitor monitor, Date dateObj) throws Exception {
		crossValidate(nbc, dataFiltered, k);
		return true;
	}

	/**
	 * Classifies the given directory and stores the result in given output directory
	 * @param classificationInputDir
	 * @param classificationOutputDir
	 * @param monitor - progress montior
	 * @param dateObj
	 * @return
	 * @throws Exception
	 */
	public boolean doClassify(String classificationInputDir, String classificationOutputDir, IProgressMonitor monitor,
			Date dateObj) throws Exception {
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
		ConsoleView.printlInConsoleln("Classification starts ..");
		String outputPath = classificationOutputDir + System.getProperty("file.separator") + "Naive_Bayes_classification_results" + "-" + df.format(dateObj);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPath + "-output.csv")));
		Instances rawTestData = new DirectoryToArff().createTestInstances(classificationInputDir);
		Instances filteredTestData = Filter.useFilter(rawTestData, filter);
		Evaluation testEval = new Evaluation(dataFiltered);
		testEval.evaluateModel(nbc, filteredTestData);
		FastVector predictions = testEval.predictions();
		for (int i = 0; i < predictions.size(); i++) {
			NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
			int pred = (int) np.predicted();
			String fileName = DirectoryToArff.instanceIdNameMap.get(i).replaceAll("[,:*?\"<>|]+", ""); 
			String predictedClass = dataFiltered.classAttribute().value(pred).replaceAll("[,:*?\"<>|]+", ""); 
			bw.write(fileName + "," + predictedClass + "\n");
		}
		bw.close();
		return true;
	}

	/**
	 * Creates a classifier using the given data
	 * @param dataFiltered
	 * @return Classifier (Naive bayes)
	 * @throws Exception
	 */
	private static Classifier createClassifier(Instances dataFiltered) throws Exception {
		Classifier classifier = new NaiveBayes();
		classifier.buildClassifier(dataFiltered);
		return classifier;
	}

	/**
	 * Performs cross validation using Weka
	 * @param nbc - Classifier (naive bayes)
	 * @param dataFiltered - Data in required format (instance)
	 * @param k - K value for cross validation
	 * @throws Exception
	 */
	private static void crossValidate(Classifier nbc, Instances dataFiltered, int k) throws Exception {
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

	/**
	 * Calculates accuracy based on the predictions
	 * @param predictions
	 * @return accuracy
	 */
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
