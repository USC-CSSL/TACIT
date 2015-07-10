/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */
package edu.usc.cssl.tacit.topicmodel.zlda.services;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import edu.usc.cssl.tacit.common.TacitUtility;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class ZlabelTopicModelAnalysis {

	private SubProgressMonitor monitor;

	public ZlabelTopicModelAnalysis(SubProgressMonitor monitor) {
		this.monitor = monitor;
	}

	private void runLDA(File dir, File preSeedFile, int numTopics,
			int noOfSamples, double alphaval, double betaval,
			double confidenceValue, String outputdir, Date dateObj) {

		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
		File[] listOfFiles = dir.listFiles();
		List<File> inputFiles = new ArrayList<File>();
		monitor.subTask("Collecting files from the directory...");
		for (File f : listOfFiles) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			if (f.getAbsolutePath().contains("DS_Store"))
				continue;
			inputFiles.add(f);
		}
		monitor.worked(5);


		ConsoleView.printlInConsoleln("running zlabel LDA...");
		DTWC dtwc = new DTWC(inputFiles, preSeedFile,this.monitor);
		dtwc.computeDocumentVectors();
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		int[][][] zlabels = dtwc.getTopicSeedsAsInt();
		int[][] docs = dtwc.getDocVectorsAsInt();


		int T = numTopics;
		int W = dtwc.getVocabSize();

		double[][] alpha = new double[1][T];
		for (int i = 0; i < T; i++) {
			alpha[0][i] = alphaval;
		}

		double[][] beta = new double[T][W];
		for (int i = 0; i < T; i++) {
			for (int j = 0; j < W; j++) {
				beta[i][j] = betaval;
			}
		}

		ZlabelLDA zelda = new ZlabelLDA(docs, zlabels, confidenceValue, alpha,
				beta, noOfSamples);
		this.monitor.subTask("Calculating Z label ...");
		boolean retVal = zelda.zLDA();
		if (!retVal) {
			System.out
					.println("Sorry, something is wrong with the input - please check format and try again");
			return;
		}
		this.monitor.worked(15);
		double[][] theta, phi;

		theta = zelda.getTheta();
		phi = zelda.getPhi();

		Map<String, Integer> dictionary = dtwc.getTermIndex();
		Map<Integer, String> revDict = dtwc.getIndexTerm();
		this.monitor.subTask("Processing Topic Words ...");
		List<List<Map.Entry<String, Double>>> topicWords = new ArrayList<List<Map.Entry<String, Double>>>();
		for (int i = 0; i < T; i++) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			topicWords.add(new ArrayList<Map.Entry<String, Double>>());
		}

		for (int i = 0; i < T; i++) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			for (int j = 0; j < W; j++) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				if (phi[i][j] > 0.001) {
					topicWords.get(i).add(
							new AbstractMap.SimpleEntry<String, Double>(revDict
									.get(j), new Double(phi[i][j])));
				}
			}
		}
		this.monitor.worked(15);
this.monitor.subTask("writing corresponding words and phi values in topicwords-"+df.format(dateObj)+".csv");
		System.out
				.println("\nTopic and its corresponding words and phi values stored in "
						+ outputdir + File.separator + "topicwords-"+df.format(dateObj)+".csv");
		try {
			FileWriter fw = new FileWriter(new File(outputdir + File.separator
					+ "topicwords-"+df.format(dateObj)+".csv"));
			for (int i = 0; i < T; i++) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				fw.write("Topic" + i + ",");
				Collections.sort(topicWords.get(i),
						new Comparator<Map.Entry<String, Double>>() {
							@Override
							public int compare(Entry<String, Double> arg0,
									Entry<String, Double> arg1) {
								return -(arg0.getValue()).compareTo(arg1
										.getValue());
							}
						});
				for (int j = 0; (j < topicWords.get(i).size() && j < 50); j++) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					fw.write(topicWords.get(i).get(j).getKey() + ","
							+ topicWords.get(i).get(j).getValue() + ",");
				}
				fw.write("\n");
				fw.flush();
			}
			fw.flush();
			fw.close();
			this.monitor.worked(15);
			this.monitor.subTask("writing Phi values for each stopic in phi-"+df.format(dateObj)+".csv");
			ConsoleView.printlInConsoleln("\nPhi values for each stopic stored in "
					+ outputdir + File.separator + "phi-"+df.format(dateObj)+".csv");
			fw = new FileWriter(
					new File(outputdir + File.separator + "phi-"+df.format(dateObj)+".csv"));
			for (int i = 0; i < T; i++) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				fw.write("Topic" + i + ",");
				for (int j = 0; j < phi[i].length; j++) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					if (phi[i][j] > 0.001) {
						fw.write(phi[i][j] + ",");
					}
				}
				fw.write("\n");
				fw.flush();
			}
			fw.flush();
			fw.close();
			this.monitor.worked(15);
			this.monitor.subTask("writing Theta values for each stopic in theta-"+df.format(dateObj)+".csv");
			ConsoleView.printlInConsoleln("\nTheta values for each document stored in "
					+ outputdir + File.separator + "theta-"+df.format(dateObj)+".csv");
			fw = new FileWriter(new File(outputdir + File.separator
					+ "theta-"+df.format(dateObj)+".csv"));
			for (int i = 0; i < docs.length; i++) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				fw.write("Document" + i + ",");
				for (int j = 0; j < theta[i].length; j++) {
					fw.write(theta[i][j] + ",");
				}
				fw.write("\n");
				fw.flush();
			}
			fw.flush();
			fw.close();
			TacitUtility.createRunReport(outputdir, "Z-Label LDA",dateObj);
		} catch (Exception e) {
			ConsoleView.printlInConsoleln("Error writing output to files " + e);
		}
		ConsoleView.printlInConsoleln("\nDone zlabel LDA...");
		this.monitor.worked(15);
		this.monitor.done();
	}

	public void invokeLDA(String inputDir, String seedFileName, int numTopics,
			String outputDir, Date dateObj) {
		File dir = new File(inputDir);

		File seedFile = new File(seedFileName);

		double alphaval = 0.5;
		double betaval = 0.1;
		int noOfSamples = 2000;
		double confidenceValue = 1;

		runLDA(dir, seedFile, numTopics, noOfSamples, alphaval, betaval,
				confidenceValue, outputDir, dateObj);

	}
}
