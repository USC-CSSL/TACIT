package edu.usc.cssl.tacit.classify.naivebayes.weka;

import java.util.Random;

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
public class NaiveBayesClassifier {
	public static void main(String[] args) throws Exception {
		String[] classes = {"F:\\NLP\\Naive Bayes Classifier\\2 Class Analysis\\Train\\Ham", "F:\\NLP\\Naive Bayes Classifier\\2 Class Analysis\\Train\\Spam"};
		DirectoryToArff.createTrainInstances(classes);
		
		Instances dataRaw = DirectoryToArff.loadArff();	
		StringToWordVector filter = new StringToWordVector();
		filter.setInputFormat(dataRaw);
		Instances dataFiltered = Filter.useFilter(dataRaw, filter);	 
	        
	    Classifier nbc = createClassifier(dataFiltered);	    
		crossValidate(nbc, dataFiltered, 2);
		
		classify(nbc, "F:\\NLP\\Naive Bayes Classifier\\2 Class Analysis\\Classify\\Input", dataFiltered, filter);
	}

	private static void classify(Classifier nbc, String input, Instances train, StringToWordVector filter) throws Exception {
	    Instances rawTestData = DirectoryToArff.createTestInstances(input);
	    Instances filteredTestData = Filter.useFilter(rawTestData, filter);
	    Evaluation testEval = new Evaluation(train);
	    testEval.evaluateModel(nbc, filteredTestData);
	    FastVector predictions = testEval.predictions();
		for (int i = 0; i < predictions.size(); i++) {
			NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
			int pred = (int) np.predicted();
			System.out.println(DirectoryToArff.instanceIdNameMap.get(i) + "\t" + train.classAttribute().value(pred));
		}
	}

	private static Classifier createClassifier(Instances dataFiltered) throws Exception {	    
	    Classifier classifier = new NaiveBayes();
	    classifier.buildClassifier(dataFiltered);
	    return classifier;
	}

	private static void crossValidate(Classifier nbc, Instances dataFiltered, int k) throws Exception {
		Evaluation eval = new Evaluation(dataFiltered);
		eval.crossValidateModel(nbc, dataFiltered, k, new Random(1));
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		double[][] confusion = eval.confusionMatrix();
		for (int i = 0; i < confusion.length; i++) {
			for (int j = 0; j < confusion[0].length; j++)
				System.out.print(confusion[i][j] + "\t");
			System.out.println();
		}
		System.out.println("Accuracy:" + calculateAccuracy(eval.predictions()));
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
