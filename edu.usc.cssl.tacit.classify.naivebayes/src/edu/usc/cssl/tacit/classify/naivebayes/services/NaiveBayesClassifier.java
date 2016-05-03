package edu.usc.cssl.tacit.classify.naivebayes.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;

import bsh.EvalError;
import edu.usc.cssl.tacit.common.TacitUtility;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class NaiveBayesClassifier {
	private String tmpLocation;
	private String outputDir;
	private String tempoutputDir;
	private long currTime;

	public NaiveBayesClassifier() {
		this.tmpLocation = System.getProperty("user.dir") + System.getProperty("file.separator") + "NB_Classifier";
		this.outputDir = this.tmpLocation + System.getProperty("file.separator") + "Output";
		this.tempoutputDir = System.getProperty("user.dir") + System.getProperty("file.separator") + "tacit_temp_files";
		if (!new File(tempoutputDir).exists()) {
			new File(tempoutputDir).mkdir();
		}
		this.currTime = System.currentTimeMillis();

		String outputDir = this.outputDir;
		if (!new File(outputDir).exists()) {
			new File(outputDir).mkdirs();
		}
	}

	public String getTmpLocation() {
		return this.tmpLocation;
	}




	public void purgeDirectory(File dir) {
		if (null == dir || !dir.exists() || !dir.isDirectory())
			return;
		if (dir.listFiles().length > 0) {
			for (File file : dir.listFiles()) {
				if (file.isDirectory())
					purgeDirectory(file);
				file.delete();
			}
		}
	}

	public void createTempDirectories(Map<String, List<String>> classPaths, ArrayList<String> trainingDataPaths,
			IProgressMonitor monitor) throws IOException {
		String tmpLocation = this.tmpLocation;
		if (!new File(tmpLocation).exists()) {
			new File(tmpLocation).mkdirs();
		}

		String tempTrainDir = new String();
		for (String key : classPaths.keySet()) {
			List<String> classFiles = classPaths.get(key);
			int numFiles = classPaths.get(key).size();
			File[] files = new File[numFiles];

			// Create respective class directories
			String className = new File(key).getName();
			tempTrainDir = tmpLocation + System.getProperty("file.separator") + className;
			System.out.println("Training data dir :" + tempTrainDir);
			if (new File(tempTrainDir).exists()) {
				purgeDirectory(new File(tempTrainDir));
			}
			new File(tempTrainDir).mkdirs();

			for (int num = 0; num < numFiles; num++) {
				files[num] = new File(classFiles.get(num));
				FileUtils.copyFileToDirectory(files[num], new File(tempTrainDir));
			}
			trainingDataPaths.add(new File(tempTrainDir).getAbsolutePath());
			monitor.worked(1); // processing of each directory/class
		}
	}

	public void deleteTempDirectories(ArrayList<String> trainingDataPaths) {
		for (String s : trainingDataPaths) {
			System.out.println("Cleaning directory " + s);
			purgeDirectory(new File(s));
			new File(s).delete();
		}
		purgeDirectory(new File(this.tmpLocation));
		new File(this.tmpLocation).delete();
	}

	/**
	 * Adds all the files recursively into 'files' list
	 * @param DirPath
	 * @param files
	 */
	public void selectAllFiles(String DirPath, List<Object> files) {
		File dir = new File(DirPath);
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				selectAllFiles(f.getAbsolutePath(), files);
			} else {
				files.add(f.getAbsolutePath());
			}
		}
	}
}
