package edu.usc.cssl.tacit.classify.svm.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.math3.stat.inference.AlternativeHypothesis;
import org.apache.commons.math3.stat.inference.BinomialTest;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import edu.usc.cssl.tacit.common.TacitUtility;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class CrossValidator {

	public void doCross(SVMClassify svm, String class1Label,
			File[] class1Files, String class2Label, File[] class2Files,
			int kValue, boolean doPredictiveWeights, String outputPath,
			IProgressMonitor monitor, Date dateObj) throws IOException,OperationCanceledException {
		// File folder1 = new File(class1Folder);
		// File folder2 = new File(class2Folder);
		// File[] class1Files = folder1.listFiles();
		// File[] class2Files = folder2.listFiles();
		int numFiles1 = class1Files.length;
		int numFiles2 = class2Files.length;


		//Check  if kValue is 1
		if (kValue == 1){
			svm.cross_train("k"+1, class1Label, class1Files, class2Label,class2Files, doPredictiveWeights,dateObj);
			double tempResults[]= svm.cross_predict("k" + 1, class1Label,class1Files, class2Label, class2Files);
			double accuracy = tempResults[0];
			double pValue = tempResults[1];
			ConsoleView.printlInConsoleln("");
			ConsoleView.printlInConsoleln("Average accuracy over " + kValue + " folds = " + accuracy + "%");
			ConsoleView.printlInConsoleln("Average Binomial Test P value  = " + pValue);
			createRunReport(outputPath, dateObj);
			clearFiles(kValue, outputPath);
			writeToCSV(new double[]{accuracy},new double[]{pValue}, outputPath,dateObj);
			return;
		}

		int trains1 = (int) Math.floor(((kValue*1.0-1)/kValue)* numFiles1);
		int trains2 = (int) Math.floor(((kValue*1.0-1)/kValue) * numFiles2);
		

		double[] accuracies = new double[kValue];
		double[] pValues = new double[kValue];
		double[] totals = new double[kValue];
		double[] corrects = new double[kValue];

		int index1 = 0;
		int index2 = 0;
		
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();

		}

		for (int i = 1; i <= kValue; i++) {
			
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();

			}
			
			ConsoleView.printlInConsoleln("--- Fold " + i + " ---");
			File[] trainFiles1 = new File[trains1];
			File[] trainFiles2 = new File[trains2];
			File[] testFiles1 = new File[numFiles1 - trains1];
			File[] testFiles2 = new File[numFiles2 - trains2];

			int currIndex = index1;
			for (int num = 0; num < trains1; num++) {
				trainFiles1[num] = class1Files[currIndex];
				// ConsoleView.writeInConsole(files1[currIndex]);
				currIndex++;
				if (currIndex >= numFiles1)
					currIndex = 0;
			}
			
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();

			}
			
			for (int num = 0; num < numFiles1 - trains1; num++) {
				testFiles1[num] = class1Files[currIndex];
				// ConsoleView.writeInConsole(files1[currIndex]);
				currIndex++;
				if (currIndex >= numFiles1)
					currIndex = 0;
			}
			
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();

			}

			currIndex = index2;
			for (int num = 0; num < trains2; num++) {
				trainFiles2[num] = class2Files[currIndex];
				// ConsoleView.writeInConsole(files2[currIndex]);
				currIndex++;
				if (currIndex >= numFiles2)
					currIndex = 0;
			}
			
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();

			}
			
			for (int num = 0; num < numFiles2 - trains2; num++) {
				testFiles2[num] = class2Files[currIndex];
				// ConsoleView.writeInConsole(files1[currIndex]);
				currIndex++;
				if (currIndex >= numFiles2)
					currIndex = 0;
			}
			
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();

			}

			svm.cross_train("k" + i, class1Label, trainFiles1, class2Label,
					trainFiles2, doPredictiveWeights,dateObj);
			
			double tempResults[]= svm.cross_predict("k" + i, class1Label,
					testFiles1, class2Label, testFiles2);
			accuracies[i - 1] = tempResults[0];
			pValues[i-1] = tempResults[1];
			totals[i-1] = tempResults[2];
			corrects[i-1] = tempResults[3];
			
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();

			}

			// Clear required globals like dfmap?
			index1 = index1 + numFiles1 - trains1;
			if (index1 >= numFiles1) {
				index1 = index1 - numFiles1;
			}

			index2 = index2 + numFiles2 - trains2;
			if (index2 >= numFiles2) {
				index2 = index2 - numFiles2;
			}
			monitor.worked(1);
		}

		double averageAccuracy = 0;
		double cumulativeTotal = 0;
		double cumulativeCorrect = 0;

		for (int j = 0; j < accuracies.length; j++) {
			averageAccuracy = averageAccuracy + accuracies[j];
			cumulativeTotal = cumulativeTotal + totals[j];
			cumulativeCorrect = cumulativeCorrect + corrects[j];
		}
		
		//Calculate the average binomial p value 
		BinomialTest btest = new BinomialTest();
		double p = 0.5;
		double averagePvalue = btest.binomialTest((int)cumulativeTotal, (int)cumulativeCorrect, p,AlternativeHypothesis.TWO_SIDED);

		ConsoleView.printlInConsoleln("");
		ConsoleView.printlInConsoleln("Average accuracy over " + kValue + " folds = " + averageAccuracy / accuracies.length + "%");
		ConsoleView.printlInConsoleln("Average Binomial Test P value  = " + averagePvalue);
		createRunReport(outputPath, dateObj);
		clearFiles(kValue, outputPath);
		writeToCSV(accuracies,pValues, outputPath,dateObj);
	}
	protected void createRunReport(String outputPath, Date dateObj){

		TacitUtility.createRunReport(outputPath, "SVM Classification", dateObj,null);
	}
	private void clearFiles(int kValue, String outputPath) {
		ConsoleView.printlInConsoleln("Clearing temporary files");
		for (int i = 0; i < kValue; i++) {
			File toDelete = new File(outputPath
					+ System.getProperty("file.separator")
					+ "SVM-Classification_k" + Integer.toString(i + 1)
					+ ".hashmap");
			toDelete.delete();
			toDelete = new File(outputPath
					+ System.getProperty("file.separator")
					+ "SVM-Classification_k" + Integer.toString(i + 1)
					+ ".model");
			toDelete.delete();
			toDelete = new File(outputPath
					+ System.getProperty("file.separator")
					+ "SVM-Classification_k" + Integer.toString(i + 1) + ".out");
			toDelete.delete();
			toDelete = new File(outputPath
					+ System.getProperty("file.separator")
					+ "SVM-Classification_k" + Integer.toString(i + 1)
					+ ".test");
			toDelete.delete();
			toDelete = new File(outputPath
					+ System.getProperty("file.separator")
					+ "SVM-Classification_k" + Integer.toString(i + 1)
					+ ".train");
			toDelete.delete();
		}
	}
	protected String createOutputFileName(String output, Date dateObj){
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");

		return output + System.getProperty("file.separator")
				+ "SVM-Classification-" + df.format(dateObj) + ".csv";
	}
	
	/**
	 * This method writes the final report of accuracies and binomial p values over all the runs.
	 * @param accuracies
	 * @param output
	 * @param dateObj
	 */
	private void writeToCSV(double[] accuracies,double[] pValues, String output, Date dateObj) {
		double averageAccuracy = 0;

		for (int j = 0; j < accuracies.length; j++) {
			averageAccuracy = averageAccuracy + accuracies[j];
		}

		String outputPath = createOutputFileName(output, dateObj);
		File outFile = new File(outputPath);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
			bw.write("Run,Accuracy,pValue");
			bw.newLine();

			for (int i = 0; i < accuracies.length; i++) {
				bw.write(Integer.toString(i + 1) + ","
						+ Double.toString(accuracies[i]) + "," + Double.toString(pValues[i]));
				bw.newLine(); 
			}
			bw.write("Average accuracy,"
					+ Double.toString(averageAccuracy / accuracies.length));
			bw.close();
			ConsoleView.printlInConsoleln("Finished creating output File - "
					+ outputPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
