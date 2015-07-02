package edu.usc.cssl.tacit.classify.naivebayes.services;

/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
 This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
 http://www.cs.umass.edu/~mccallum/mallet
 This software is provided under the terms of the Common Public License,
 version 1.0, as published by http://www.opensource.org.  For further
 information, see the file `LICENSE' included with this distribution. */

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.apache.commons.math3.stat.inference.AlternativeHypothesis;
import org.apache.commons.math3.stat.inference.BinomialTest;

import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.Trial;
import cc.mallet.classify.evaluate.ConfusionMatrix;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Labeling;
import cc.mallet.types.MatrixOps;
import cc.mallet.util.BshInterpreter;
import cc.mallet.util.MalletLogger;
import cc.mallet.util.MalletProgressMessageLogger;
import cc.mallet.util.ProgressMessageLogFormatter;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

/**
 * Classify documents, run trials, print statistics from a vector file.
 * 
 * @author Andrew McCallum <a
 *         href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

public abstract class Vectors2Classify {
	static BshInterpreter interpreter = new BshInterpreter();
	static ArrayList<String> result = new ArrayList<String>();

	private static Logger logger = MalletLogger
			.getLogger(Vectors2Classify.class.getName());
	private static Logger progressLogger = MalletProgressMessageLogger
			.getLogger(Vectors2Classify.class.getName() + "-pl");
	private static ArrayList<String> classifierTrainerStrings = new ArrayList<String>();
	private static boolean[][] ReportOptions = new boolean[3][4];
	private static String[][] ReportOptionArgs = new String[3][4]; // arg in
																	// dataset:reportOption=arg

	// Essentially an enum mapping string names to enums to ints.
	private static class ReportOption {
		static final String[] dataOptions = { "train", "test", "validation" };
		static final String[] reportOptions = { "accuracy", "f1", "confusion",
				"raw" };
		static final int train = 0;
		static final int test = 1;
		static final int validation = 2;
		static final int accuracy = 0;
		static final int f1 = 1;
		static final int confusion = 2;
		static final int raw = 3;
	}

	static CommandOption.SpacedStrings report = new CommandOption.SpacedStrings(
			Vectors2Classify.class,
			"report",
			"[train|test|validation]:[accuracy|f1:label|confusion|raw]",
			true,
			new String[] { "test:accuracy", "test:confusion", "train:accuracy" },
			"", null) {
		@Override
		public void postParsing(CommandOption.List list) {
			java.lang.String defaultRawFormatting = "siw";

			for (int argi = 0; argi < this.value.length; argi++) {
				// convert options like --report train:accuracy --report
				// test:f1=labelA to
				// boolean array of options.

				// first, split the argument at semicolon.
				// System.out.println(argi + " " + this.value[argi]);
				java.lang.String arg = this.value[argi];
				java.lang.String fields[] = arg.split("[:=]");
				java.lang.String dataSet = fields[0];
				java.lang.String reportOption = fields[1];
				java.lang.String reportOptionArg = null;

				if (fields.length >= 3) {
					reportOptionArg = fields[2];
				}
				// System.out.println("Report option arg " + reportOptionArg);

				// find the datasource (test,train,validation)
				boolean foundDataSource = false;
				int i = 0;
				for (; i < ReportOption.dataOptions.length; i++) {
					if (dataSet.equals(ReportOption.dataOptions[i])) {
						foundDataSource = true;
						break;
					}
				}
				if (!foundDataSource) {
					throw new IllegalArgumentException("Unknown argument = "
							+ dataSet + " in --report " + this.value[argi]);
				}

				// find the report option (accuracy, f1, confusion, raw)
				boolean foundReportOption = false;
				int j = 0;
				for (; j < ReportOption.reportOptions.length; j++) {
					if (reportOption.equals(ReportOption.reportOptions[j])) {
						foundReportOption = true;
						break;
					}
				}
				if (!foundReportOption) {
					throw new IllegalArgumentException("Unknown argument = "
							+ reportOption + " in --report " + this.value[argi]);
				}

				// Mark the (dataSet,reportionOption) pair as selected
				ReportOptions[i][j] = true;

				if (j == ReportOption.f1) {
					// make sure a label was specified for f1
					if (reportOptionArg == null) {
						throw new IllegalArgumentException(
								"F1 must have label argument in --report "
										+ this.value[argi]);
					}
					// Pass through the string argument
					ReportOptionArgs[i][j] = reportOptionArg;

				} else if (reportOptionArg != null) {
					throw new IllegalArgumentException(
							"No arguments after = allowed in --report "
									+ this.value[argi]);
				}
			}
		}
	};

	static CommandOption.String trainerConstructor = new CommandOption.String(
			Vectors2Classify.class,
			"trainer",
			"ClassifierTrainer constructor",
			true,
			"new NaiveBayesTrainer()",
			"Java code for the constructor used to create a ClassifierTrainer.  "
					+ "If no '(' appears, then \"new \" will be prepended and \"Trainer()\" will be appended."
					+ "You may use this option mutiple times to compare multiple classifiers.",
			null) {
		@Override
		public void postParsing(CommandOption.List list) {
			classifierTrainerStrings.add(this.value);
		}
	};

	static CommandOption.String outputFile = new CommandOption.String(
			Vectors2Classify.class,
			"output-classifier",
			"FILENAME",
			true,
			"classifier.mallet",
			"The filename in which to write the classifier after it has been trained.",
			null);

	/*
	 * static CommandOption.String pipeFile = new CommandOption.String
	 * (Vectors2Classify.class, "output-pipe", "FILENAME", true,
	 * "classifier_pipe.mallet",
	 * "The filename in which to write the classifier's instancePipe after it has been trained."
	 * , null);
	 */

	static CommandOption.String inputFile = new CommandOption.String(
			Vectors2Classify.class,
			"input",
			"FILENAME",
			true,
			"text.vectors",
			"The filename from which to read the list of training instances.  Use - for stdin.",
			null);

	static CommandOption.String trainingFile = new CommandOption.String(
			Vectors2Classify.class,
			"training-file",
			"FILENAME",
			true,
			"text.vectors",
			"Read the training set instance list from this file. "
					+ "If this is specified, the input file parameter is ignored",
			null);

	static CommandOption.String testFile = new CommandOption.String(
			Vectors2Classify.class,
			"testing-file",
			"FILENAME",
			true,
			"text.vectors",
			"Read the test set instance list to this file. "
					+ "If this option is specified, the training-file parameter must be specified and "
					+ " the input-file parameter is ignored", null);

	static CommandOption.String validationFile = new CommandOption.String(
			Vectors2Classify.class,
			"validation-file",
			"FILENAME",
			true,
			"text.vectors",
			"Read the validation set instance list to this file."
					+ "If this option is specified, the training-file parameter must be specified and "
					+ "the input-file parameter is ignored", null);

	static CommandOption.Double trainingProportionOption = new CommandOption.Double(
			Vectors2Classify.class, "training-portion", "DECIMAL", true, 1.0,
			"The fraction of the instances that should be used for training.",
			null);

	static CommandOption.Double validationProportionOption = new CommandOption.Double(
			Vectors2Classify.class,
			"validation-portion",
			"DECIMAL",
			true,
			0.0,
			"The fraction of the instances that should be used for validation.",
			null);

	static CommandOption.Double unlabeledProportionOption = new CommandOption.Double(
			Vectors2Classify.class,
			"unlabeled-portion",
			"DECIMAL",
			true,
			0.0,
			"The fraction of the training instances that should have their labels hidden.  "
					+ "Note that these are taken out of the training-portion, not allocated separately.",
			null);

	static CommandOption.Integer randomSeedOption = new CommandOption.Integer(
			Vectors2Classify.class,
			"random-seed",
			"INTEGER",
			true,
			0,
			"The random seed for randomly selecting a proportion of the instance list for training",
			null);

	static CommandOption.Integer numTrialsOption = new CommandOption.Integer(
			Vectors2Classify.class, "num-trials", "INTEGER", true, 1,
			"The number of random train/test splits to perform", null);

	static CommandOption.Object classifierEvaluatorOption = new CommandOption.Object(
			Vectors2Classify.class, "classifier-evaluator", "CONSTRUCTOR",
			true, null,
			"Java code for constructing a ClassifierEvaluating object", null);

	// static CommandOption.Boolean printTrainAccuracyOption = new
	// CommandOption.Boolean
	// (Vectors2Classify.class, "print-train-accuracy", "true|false", true,
	// true,
	// "After training, run the resulting classifier on the instances included in training, "
	// +"and print the accuracy", null);
	//
	// static CommandOption.Boolean printTestAccuracyOption = new
	// CommandOption.Boolean
	// (Vectors2Classify.class, "print-test-accuracy", "true|false", true, true,
	// "After training, run the resulting classifier on the instances not included in training, "
	// +"and print the accuracy", null);

	static CommandOption.Integer verbosityOption = new CommandOption.Integer(
			Vectors2Classify.class,
			"verbosity",
			"INTEGER",
			true,
			-1,
			"The level of messages to print: 0 is silent, 8 is most verbose. "
					+ "Levels 0-8 correspond to the java.logger predefined levels "
					+ "off, severe, warning, info, config, fine, finer, finest, all. "
					+ "The default value is taken from the mallet logging.properties file,"
					+ " which currently defaults to INFO level (3)", null);

	static CommandOption.Boolean noOverwriteProgressMessagesOption = new CommandOption.Boolean(
			Vectors2Classify.class,
			"noOverwriteProgressMessages",
			"true|false",
			false,
			false,
			"Suppress writing-in-place on terminal for progess messages - repetitive messages "
					+ "of which only the latest is generally of interest", null);

	static CommandOption.Integer crossValidation = new CommandOption.Integer(
			Vectors2Classify.class, "cross-validation", "INT", true, 0,
			"The number of folds for cross-validation (DEFAULT=0).", null);

	public static ArrayList<String> main(String[] args) throws bsh.EvalError,
			java.io.IOException {
		result.clear();
		classifierTrainerStrings = new ArrayList<String>();
		ReportOptions = new boolean[][]{{false, false, false, false}, {false, false, false, false}, {false, false, false, false}};
			
		double pvalue = 0;
		// Process the command-line options
		CommandOption
				.setSummary(
						Vectors2Classify.class,
						"A tool for training, saving and printing diagnostics from a classifier on vectors.");
		CommandOption.process(Vectors2Classify.class, args);

		// handle default trainer here for now; default argument processing
		// doesn't work
		if (!trainerConstructor.wasInvoked()) {
			classifierTrainerStrings.add("new NaiveBayesTrainer()");
		}

		if (!report.wasInvoked()) {
			ReportOptions = new boolean[][]{{true, false, false, false}, {true, false, true, false}, {false, false, false, false}};
			//report.postParsing(null); // force postprocessing of default value
			
		}

		int verbosity = verbosityOption.value;

		Logger rootLogger = ((MalletLogger) progressLogger).getRootLogger();

		if (verbosityOption.wasInvoked()) {
			rootLogger.setLevel(MalletLogger.LoggingLevels[verbosity]);
		}

		if (noOverwriteProgressMessagesOption.value == false) {
			// install special formatting for progress messages
			// find console handler on root logger; change formatter to one
			// that knows about progress messages
			Handler[] handlers = rootLogger.getHandlers();
			for (int i = 0; i < handlers.length; i++) {
				if (handlers[i] instanceof ConsoleHandler) {
					handlers[i].setFormatter(new ProgressMessageLogFormatter());
				}
			}
		}

		boolean separateIlists = testFile.wasInvoked()
				|| trainingFile.wasInvoked() || validationFile.wasInvoked();
		InstanceList ilist = null;
		InstanceList testFileIlist = null;
		InstanceList trainingFileIlist = null;
		InstanceList validationFileIlist = null;

		if (!separateIlists) { // normal case, --input-file specified
			// Read in the InstanceList, from stdin if the input filename is
			// "-".
			ilist = InstanceList.load(new File(inputFile.value));
			//ilist = new InstanceList(ilist.getAlphabet(), ilist.getAlphabet());
		} else { // user specified separate files for testing and training sets.
			trainingFileIlist = InstanceList.load(new File(trainingFile.value));
			logger.info("Training vectors loaded from " + trainingFile.value);

			if (testFile.wasInvoked()) {
				testFileIlist = InstanceList.load(new File(testFile.value));
				logger.info("Testing vectors loaded from " + testFile.value);

				if (!testFileIlist.getPipe().alphabetsMatch(
						trainingFileIlist.getPipe())) {
					throw new RuntimeException(trainingFileIlist.getPipe()
							.getDataAlphabet()
							+ "\n"
							+ testFileIlist.getPipe().getDataAlphabet()
							+ "\n"
							+ trainingFileIlist.getPipe().getTargetAlphabet()
							+ "\n"
							+ testFileIlist.getPipe().getTargetAlphabet()
							+ "\n"
							+ "Training and testing alphabets don't match!\n");
				}
			}

			if (validationFile.wasInvoked()) {
				validationFileIlist = InstanceList.load(new File(
						validationFile.value));
				logger.info("validation vectors loaded from "
						+ validationFile.value);
				if (!validationFileIlist.getPipe().alphabetsMatch(
						trainingFileIlist.getPipe())) {
					throw new RuntimeException(
							trainingFileIlist.getPipe().getDataAlphabet()
									+ "\n"
									+ validationFileIlist.getPipe()
											.getDataAlphabet()
									+ "\n"
									+ trainingFileIlist.getPipe()
											.getTargetAlphabet()
									+ "\n"
									+ validationFileIlist.getPipe()
											.getTargetAlphabet()
									+ "\n"
									+ "Training and validation alphabets don't match!\n");
				}
			} else {
				validationFileIlist = new InstanceList(
						new cc.mallet.pipe.Noop());
			}

		}

		if (crossValidation.wasInvoked()
				&& trainingProportionOption.wasInvoked()) {
			logger.warning("Both --cross-validation and --training-portion were invoked.  Using cross validation with "
					+ crossValidation.value + " folds.");
		}
		if (crossValidation.wasInvoked()
				&& validationProportionOption.wasInvoked()) {
			logger.warning("Both --cross-validation and --validation-portion were invoked.  Using cross validation with "
					+ crossValidation.value + " folds.");
		}
		if (crossValidation.wasInvoked() && numTrialsOption.wasInvoked()) {
			logger.warning("Both --cross-validation and --num-trials were invoked.  Using cross validation with "
					+ crossValidation.value + " folds.");
		}

		int numTrials;
		if (crossValidation.wasInvoked()) {
			numTrials = crossValidation.value;
		} else {
			numTrials = numTrialsOption.value;
		}

		Random r = randomSeedOption.wasInvoked() ? new Random(
				randomSeedOption.value) : new Random();

		int numTrainers = classifierTrainerStrings.size();

		double trainAccuracy[][] = new double[numTrainers][numTrials];
		double testAccuracy[][] = new double[numTrainers][numTrials];
		double validationAccuracy[][] = new double[numTrainers][numTrials];

		String trainConfusionMatrix[][] = new String[numTrainers][numTrials];
		String testConfusionMatrix[][] = new String[numTrainers][numTrials];
		String validationConfusionMatrix[][] = new String[numTrainers][numTrials];

		double t = trainingProportionOption.value;
		double v = validationProportionOption.value;

		if (!separateIlists) {
			if (crossValidation.wasInvoked()) {
				logger.info("Cross-validation folds = " + crossValidation.value);
			} else {
				logger.info("Training portion = " + t);
				logger.info(" Unlabeled training sub-portion = "
						+ unlabeledProportionOption.value);
				logger.info("Validation portion = " + v);
				logger.info("Testing portion = " + (1 - v - t));
			}
		}

		// for (int i=0; i<3; i++){
		// for (int j=0; j<4; j++){
		// System.out.print(" " + ReportOptions[i][j]);
		// }
		// System.out.println();
		// }

		CrossValidationIterator cvIter;
		if (crossValidation.wasInvoked()) {
			if (crossValidation.value < 2) {
				throw new RuntimeException(
						"At least two folds (set with --cross-validation) are required for cross validation");
			}
			//System.out.println("Alphabets : "+ ilist.getDataAlphabet() +":"+ ilist.getTargetAlphabet());
			cvIter = new CrossValidationIterator(ilist, crossValidation.value,
					r);
		} else {
			cvIter = null;
		}

		String[] trainerNames = new String[numTrainers];
		for (int trialIndex = 0; trialIndex < numTrials; trialIndex++) {
			System.out.println("\n-------------------- Trial " + trialIndex
					+ "  --------------------\n");
			InstanceList[] ilists;
			BitSet unlabeledIndices = null;
			if (!separateIlists) {
				if (crossValidation.wasInvoked()) {
					InstanceList[] cvSplit = cvIter.next();
					ilists = new InstanceList[3];
					ilists[0] = cvSplit[0];
					ilists[1] = cvSplit[1];
					ilists[2] = cvSplit[0].cloneEmpty();
				} else {
					ilists = ilist.split(r, new double[] { t, 1 - t - v, v });
				}
			} else {
				ilists = new InstanceList[3];
				ilists[0] = trainingFileIlist;
				ilists[1] = testFileIlist;
				ilists[2] = validationFileIlist;
			}

			if (unlabeledProportionOption.value > 0)
				unlabeledIndices = new cc.mallet.util.Randoms(r.nextInt())
						.nextBitSet(ilists[0].size(),
								unlabeledProportionOption.value);

			// InfoGain ig = new InfoGain (ilists[0]);
			// int igl = Math.min (10, ig.numLocations());
			// for (int i = 0; i < igl; i++)
			// System.out.println
			// ("InfoGain["+ig.getObjectAtRank(i)+"]="+ig.getValueAtRank(i));
			// ig.print();

			// FeatureSelection selectedFeatures = new FeatureSelection (ig,
			// 8000);
			// ilists[0].setFeatureSelection (selectedFeatures);
			// OddsRatioFeatureInducer orfi = new OddsRatioFeatureInducer
			// (ilists[0]);
			// orfi.induceFeatures (ilists[0], false, true);

			// System.out.println
			// ("Training with "+ilists[0].size()+" instances");
			long time[] = new long[numTrainers];
			for (int c = 0; c < numTrainers; c++) {
				time[c] = System.currentTimeMillis();
				ClassifierTrainer trainer = getTrainer(classifierTrainerStrings
						.get(c));
				trainer.setValidationInstances(ilists[2]);
				// ConsoleView.writeInConsole("Trial " + trialIndex + " Training " + trainer + " with " + ilists[0].size() + " instances");
				ConsoleView.printlInConsoleln("Training " + trainer + " with " + ilists[0].size() + " instances");
				if (unlabeledProportionOption.value > 0)
					ilists[0].hideSomeLabels(unlabeledIndices);
				Classifier classifier = trainer.train(ilists[0]);
				if (unlabeledProportionOption.value > 0)
					ilists[0].unhideAllLabels();

				//ConsoleView.writeInConsole("Trial " + trialIndex + " Training " + trainer.toString() + " finished");
				ConsoleView.printlInConsoleln("Training " + trainer.toString() + " finished");
				time[c] = System.currentTimeMillis() - time[c];
				Trial trainTrial = new Trial(classifier, ilists[0]);
				// assert (ilists[1].size() > 0);
				Trial testTrial = new Trial(classifier, ilists[1]);
				Trial validationTrial = new Trial(classifier, ilists[2]);				

				// gdruck - only perform evaluation if requested in report
				// options
				if (ReportOptions[ReportOption.train][ReportOption.confusion]
						&& ilists[0].size() > 0)
					trainConfusionMatrix[c][trialIndex] = new ConfusionMatrix(
							trainTrial).toString();
				if (ReportOptions[ReportOption.test][ReportOption.confusion]
						&& ilists[1].size() > 0)
					testConfusionMatrix[c][trialIndex] = new ConfusionMatrix(
							testTrial).toString();
				if (ReportOptions[ReportOption.validation][ReportOption.confusion]
						&& ilists[2].size() > 0)
					validationConfusionMatrix[c][trialIndex] = new ConfusionMatrix(
							validationTrial).toString();

				// gdruck - only perform evaluation if requested in report
				// options
				if (ReportOptions[ReportOption.train][ReportOption.accuracy])
					trainAccuracy[c][trialIndex] = trainTrial.getAccuracy();
				if (ReportOptions[ReportOption.test][ReportOption.accuracy])
					testAccuracy[c][trialIndex] = testTrial.getAccuracy();
				if (ReportOptions[ReportOption.validation][ReportOption.accuracy])
					validationAccuracy[c][trialIndex] = validationTrial
							.getAccuracy();

				if (outputFile.wasInvoked()) {
					String filename = outputFile.value;
					if (numTrainers > 1)
						filename = filename + trainer.toString();
					if (numTrials > 1)
						filename = filename + ".trial" + trialIndex;
					try {
						ObjectOutputStream oos = new ObjectOutputStream(
								new FileOutputStream(filename));
						oos.writeObject(classifier);
						oos.close();
					} catch (Exception e) {
						e.printStackTrace();
						throw new IllegalArgumentException(
								"Couldn't write classifier to filename "
										+ filename);
					}
				}

				// New Reporting

				// raw output
				if (ReportOptions[ReportOption.train][ReportOption.raw]) {
					System.out.println("Trial " + trialIndex + " Trainer " + trainer.toString());
					System.out.println(" Raw Training Data");
					printTrialClassification(trainTrial);
				}

				if (ReportOptions[ReportOption.test][ReportOption.raw]) {
					System.out.println("Trial " + trialIndex + " Trainer " + trainer.toString());
					System.out.println(" Raw Testing Data");
					printTrialClassification(testTrial);
					//System.out.println("Report Option :"+(ReportOptions[ReportOption.test][ReportOption.raw]));
				}

				if (ReportOptions[ReportOption.validation][ReportOption.raw]) {
					System.out.println("Trial " + trialIndex + " Trainer " + trainer.toString());
					System.out.println(" Raw Validation Data");
					printTrialClassification(validationTrial);
				}
				System.out.println("Bino test vars size " + ilists[1].size()
						+ "and accuracy + " + testTrial.getAccuracy()
						+ " then success " + (int) testTrial.getAccuracy()
						* ilists[1].size());
				BinomialTest binomtest = new BinomialTest();
				double p = 0.5;

				// train
				if (ReportOptions[ReportOption.train][ReportOption.confusion]) {
					//ConsoleView.writeInConsole("Trial " + trialIndex + " Trainer " 	+ trainer.toString() + " Training Data Confusion Matrix");
					ConsoleView.printlInConsoleln(trainer.toString() + " Training Data Confusion Matrix");
					if (ilists[0].size() > 0)
						ConsoleView.printlInConsoleln(trainConfusionMatrix[c][trialIndex]);
				}

				if (ReportOptions[ReportOption.train][ReportOption.accuracy]) {
					pvalue = binomtest
							.binomialTest(ilists[0].size(), (int) (trainTrial
									.getAccuracy() * ilists[0].size()), p,
									AlternativeHypothesis.TWO_SIDED);
					if (pvalue != 0) {
						if (pvalue > 0.5)
							pvalue = Math.abs(pvalue - 1);
						ConsoleView.printlInConsoleln("Binomial 2-Sided P value = " + pvalue + "\n");
					}

					//ConsoleView.writeInConsole("Trial " + trialIndex + " Trainer " + trainer.toString() + " training data accuracy= " + trainAccuracy[c][trialIndex]);
					ConsoleView.printlInConsoleln(trainer.toString() + " training data accuracy= " + trainAccuracy[c][trialIndex]);
				}

				if (ReportOptions[ReportOption.train][ReportOption.f1]) {
					String label = ReportOptionArgs[ReportOption.train][ReportOption.f1];
					//ConsoleView.writeInConsole("Trial " + trialIndex + " Trainer "+ trainer.toString() + " training data F1(" + label + ") = " + trainTrial.getF1(label));
					ConsoleView.printlInConsoleln(trainer.toString() + " training data F1(" + label + ") = " + trainTrial.getF1(label));
				}

				// validation
				if (ReportOptions[ReportOption.validation][ReportOption.confusion]) {
				//	ConsoleView.writeInConsole("Trial " + trialIndex + " Trainer " + trainer.toString() + " Validation Data Confusion Matrix");
					ConsoleView.printlInConsoleln(trainer.toString() + " Validation Data Confusion Matrix");
					if (ilists[2].size() > 0)
						ConsoleView.printlInConsoleln(validationConfusionMatrix[c][trialIndex]);
				}

				if (ReportOptions[ReportOption.validation][ReportOption.accuracy]) {
					//ConsoleView.writeInConsole("Trial " + trialIndex + " Trainer " + trainer.toString() + " validation data accuracy= " + validationAccuracy[c][trialIndex]);
					ConsoleView.printlInConsoleln(trainer.toString() + " validation data accuracy= " + validationAccuracy[c][trialIndex]);
				}

				if (ReportOptions[ReportOption.validation][ReportOption.f1]) {
					String label = ReportOptionArgs[ReportOption.validation][ReportOption.f1];
					//ConsoleView.writeInConsole("Trial " + trialIndex + " Trainer " + trainer.toString() + " validation data F1(" + label + ") = " + validationTrial.getF1(label));
					ConsoleView.printlInConsoleln(trainer.toString() + " validation data F1(" + label + ") = " + validationTrial.getF1(label));
				}

				// test
				if (ReportOptions[ReportOption.test][ReportOption.confusion]) {
					//ConsoleView.writeInConsole("Trial " + trialIndex + " Trainer " + trainer.toString() + " Test Data Confusion Matrix");
					ConsoleView.printlInConsoleln(trainer.toString() + " Test Data Confusion Matrix");
					if (ilists[1].size() > 0)
						ConsoleView.printlInConsoleln(testConfusionMatrix[c][trialIndex]);
				}

				if (ReportOptions[ReportOption.test][ReportOption.accuracy]) {
					pvalue = binomtest.binomialTest(ilists[1].size(),
							(int) (testTrial.getAccuracy() * ilists[1].size()),
							0.5, AlternativeHypothesis.TWO_SIDED);
					if (pvalue != 0) {
						if (pvalue > 0.5)
							pvalue = Math.abs(pvalue - 1);
						ConsoleView.printlInConsoleln("Binomial 2-Sided P value = " + pvalue + " \n");
					}

					//ConsoleView.writeInConsole("Trial " + trialIndex + " Trainer " + trainer.toString() + " test data accuracy= " + testAccuracy[c][trialIndex]);
					ConsoleView.printlInConsoleln(trainer.toString() + " test data accuracy= " + testAccuracy[c][trialIndex]);
				}

				if (ReportOptions[ReportOption.test][ReportOption.f1]) {
					String label = ReportOptionArgs[ReportOption.test][ReportOption.f1];
					//ConsoleView.writeInConsole("Trial " + trialIndex + " Trainer " + trainer.toString() + " test data F1(" + label + ") = " + testTrial.getF1(label));
					ConsoleView.printlInConsoleln(trainer.toString() + " test data F1(" + label + ") = " + testTrial.getF1(label));
				}

				if (trialIndex == 0)
					trainerNames[c] = trainer.toString();

			} // end for each trainer
		} // end for each trial

		// New reporting
		// "[train|test|validation]:[accuracy|f1|confusion|raw]"
		for (int c = 0; c < numTrainers; c++) {
			ConsoleView.printlInConsole("\n" + trainerNames[c].toString()+ "\n");
			if (ReportOptions[ReportOption.train][ReportOption.accuracy]) {
				/*ConsoleView.printlInConsoleln("Summary. train accuracy mean = "
						+ MatrixOps.mean(trainAccuracy[c]) + " stddev = "
						+ MatrixOps.stddev(trainAccuracy[c]) + " stderr = "
						+ MatrixOps.stderr(trainAccuracy[c])); */
				
				String trainResult = "";
				if (pvalue != 0)
					trainResult+="Summary. train accuracy = " + MatrixOps.mean(trainAccuracy[c]);
				else
					trainResult+="Summary. train accuracy = " + MatrixOps.mean(trainAccuracy[c]);

				if(numTrials > 1) {
					trainResult+=" stddev = " + MatrixOps.stddev(trainAccuracy[c]) + " stderr = "+ MatrixOps.stderr(trainAccuracy[c]);
				}	
				ConsoleView.printlInConsoleln(trainResult);
				
			}

			if (ReportOptions[ReportOption.validation][ReportOption.accuracy]) {
				/*
				ConsoleView.printlInConsoleln("Summary. validation accuracy mean = "
						+ MatrixOps.mean(validationAccuracy[c]) + " stddev = "
						+ MatrixOps.stddev(validationAccuracy[c])
						+ " stderr = "
						+ MatrixOps.stderr(validationAccuracy[c]));*/
				
				String validationResult = "";
				if (pvalue != 0)
					validationResult+="Summary. validation accuracy = " + MatrixOps.mean(validationAccuracy[c]);
				else
					validationResult+="Summary. validation accuracy = " + MatrixOps.mean(validationAccuracy[c]);

				if(numTrials > 1) {
					validationResult+=" stddev = " + MatrixOps.stddev(validationAccuracy[c]) + " stderr = "+ MatrixOps.stderr(validationAccuracy[c]);
				}	
				ConsoleView.printlInConsoleln(validationResult);
				
			}

			if (ReportOptions[ReportOption.test][ReportOption.accuracy]) {
				String testResult = "";
				if (pvalue != 0)
					testResult+="Summary. test accuracy = " + MatrixOps.mean(testAccuracy[c]) + " Binomial 2-Sided Pvalue = " + pvalue;
				else
					testResult+="Summary. test accuracy = " + MatrixOps.mean(testAccuracy[c]) + " Pvalue < 10^(-1022)\n";

				if(numTrials > 1) {
					testResult+=" stddev = " + MatrixOps.stddev(testAccuracy[c]) + " stderr = "+ MatrixOps.stderr(testAccuracy[c]);
				}	
				ConsoleView.printlInConsoleln(testResult);
				
				/*
				if (pvalue != 0)
					ConsoleView.printlInConsoleln("Summary. test accuracy mean = "
							+ MatrixOps.mean(testAccuracy[c]) + " stddev = "
							+ MatrixOps.stddev(testAccuracy[c]) + " stderr = "
							+ MatrixOps.stderr(testAccuracy[c]) + " pvalue = "
							+ pvalue);
				else
					ConsoleView.printlInConsoleln("Summary. test accuracy mean = "
							+ MatrixOps.mean(testAccuracy[c]) + " stddev = "
							+ MatrixOps.stddev(testAccuracy[c]) + " stderr = "
							+ MatrixOps.stderr(testAccuracy[c])
							+ " P value < 10^(-1022)\n"); */
			}

			// If we are testing the classifier with two folders, result will be
			// empty - no report is generated
			if (result.isEmpty()) {
				if (pvalue != 0)
					result.add("Summary. test accuracy = " + MatrixOps.mean(testAccuracy[c]) + " Binomial 2-Sided  Pvalue = " + pvalue);
				else
					result.add("Summary. test accuracy = " + MatrixOps.mean(testAccuracy[c]) + " Pvalue < 10^(-1022)\n");

				if(numTrials > 1) {
					result.add(" stddev = " + MatrixOps.stddev(testAccuracy[c]) + " stderr = "+ MatrixOps.stderr(testAccuracy[c]));
				}				
			}
		} // end for each trainer

		return result;
	}

	private static void printTrialClassification(Trial trial) {
		for (Classification c : trial) {
			String classification = "";
			Instance instance = c.getInstance();
			System.out.print(instance.getName() + " " + instance.getTarget()
					+ " ");
			classification = instance.getName() + "," + instance.getTarget()
					+ " ";
			Labeling labeling = c.getLabeling();
			for (int j = 0; j < labeling.numLocations(); j++) {
				if(!labeling.getLabelAtRank(j).toString().isEmpty()) {
				classification = classification
						+ labeling.getLabelAtRank(j).toString() + ","
						+ labeling.getValueAtRank(j) + ",";
				System.out.print(labeling.getLabelAtRank(j).toString() + ":"
						+ labeling.getValueAtRank(j) + " ");
				}
			}
			result.add(classification);
			System.out.print("\n");
		}
	}

	private static Object createTrainer(String arg) {
		try {
			return interpreter.eval(arg);
		} catch (bsh.EvalError e) {
			throw new IllegalArgumentException("Java interpreter eval error\n"
					+ e);
		}
	}

	private static ClassifierTrainer getTrainer(String arg) {
		// parse something like Maxent,gaussianPriorVariance=10,numIterations=20

		// first, split the argument at commas.
		java.lang.String fields[] = arg.split(",");

		// Massage constructor name, so that MaxEnt, MaxEntTrainer, new
		// MaxEntTrainer()
		// all call new MaxEntTrainer()
		java.lang.String constructorName = fields[0];
		Object trainer;
		if (constructorName.indexOf('(') != -1) // if contains (), pass it
												// though
			trainer = createTrainer(arg);
		else {
			if (constructorName.endsWith("Trainer")) {
				trainer = createTrainer("new " + constructorName + "()"); // add
																			// parens
																			// if
																			// they
																			// forgot
			} else {
				trainer = createTrainer("new " + constructorName + "Trainer()"); // make
																					// trainer
																					// name
																					// from
																					// classifier
																					// name
			}
		}

		// find methods associated with the class we just built
		Method methods[] = trainer.getClass().getMethods();

		// find setters corresponding to parameter names.
		for (int i = 1; i < fields.length; i++) {
			java.lang.String nameValuePair[] = fields[i].split("=");
			java.lang.String parameterName = nameValuePair[0];
			java.lang.String parameterValue = nameValuePair[1]; // todo: check
																// for val
																// present!
			java.lang.Object parameterValueObject;
			try {
				parameterValueObject = interpreter.eval(parameterValue);
			} catch (bsh.EvalError e) {
				throw new IllegalArgumentException(
						"Java interpreter eval error on parameter "
								+ parameterName + "\n" + e);
			}

			boolean foundSetter = false;
			for (int j = 0; j < methods.length; j++) {
				// System.out.println("method " + j + " name is " +
				// methods[j].getName());
				// System.out.println("set" +
				// Character.toUpperCase(parameterName.charAt(0)) +
				// parameterName.substring(1));
				if (("set" + Character.toUpperCase(parameterName.charAt(0)) + parameterName
						.substring(1)).equals(methods[j].getName())
						&& methods[j].getParameterTypes().length == 1) {
					// System.out.println("Matched method " +
					// methods[j].getName());
					// Class[] ptypes = methods[j].getParameterTypes();
					// System.out.println("Parameter types:");
					// for (int k=0; k<ptypes.length; k++){
					// System.out.println("class " + k + " = " +
					// ptypes[k].getName());
					// }

					try {
						java.lang.Object[] parameterList = new java.lang.Object[] { parameterValueObject };
						// System.out.println("Argument types:");
						// for (int k=0; k<parameterList.length; k++){
						// System.out.println("class " + k + " = " +
						// parameterList[k].getClass().getName());
						// }
						methods[j].invoke(trainer, parameterList);
					} catch (IllegalAccessException e) {
						System.out.println("IllegalAccessException " + e);
						throw new IllegalArgumentException(
								"Java access error calling setter\n" + e);
					} catch (InvocationTargetException e) {
						System.out.println("IllegalTargetException " + e);
						throw new IllegalArgumentException(
								"Java target error calling setter\n" + e);
					}
					foundSetter = true;
					break;
				}
			}
			if (!foundSetter) {
				System.out.println("Parameter " + parameterName
						+ " not found on trainer " + constructorName);
				System.out.println("Available parameters for "
						+ constructorName);
				for (int j = 0; j < methods.length; j++) {
					if (methods[j].getName().startsWith("set")
							&& methods[j].getParameterTypes().length == 1) {
						System.out.println(Character.toLowerCase(methods[j]
								.getName().charAt(3))
								+ methods[j].getName().substring(4));
					}
				}

				throw new IllegalArgumentException(
						"no setter found for parameter " + parameterName);
			}
		}
		assert (trainer instanceof ClassifierTrainer);
		return ((ClassifierTrainer) trainer);
	}
}
