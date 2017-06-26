package edu.usc.cssl.tacit.classify.id3.weka;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.stemmers.LovinsStemmer;
import weka.core.FastVector;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;
import edu.usc.cssl.tacit.classify.id3.weka.DirectoryToArffId3;
//import edu.usc.cssl.tacit.classify.naivebayes.weka.DirectoryToArff;
//import edu.usc.cssl.tacit.classify.naivebayes.weka.DirectoryToArff;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import prefuse.demos.PrefuseTree;
import weka.classifiers.meta.FilteredClassifier;
import weka.gui.visualize.plugins.TreeVisualizePlugin;
import weka.core.*;


public class Id3Weka{
	private Map<String, List<String>> classPaths;
	private J48 classifier ;
	//private Instances dataFiltered;
	private StringToWordVector filter;
	private FilteredClassifier fc;
	private Instances dataRaw;
	
	public Id3Weka(Map<String, List<String>> classPaths) {
		this.classPaths = classPaths;
	}
	
	public void initializeInstances() throws Exception {
		DirectoryToArffId3 ref = new DirectoryToArffId3();
		ref.createTrainInstances(classPaths);
		dataRaw = ref.loadArff();
		dataRaw.setClassIndex(dataRaw.numAttributes()-1);
		J48 classifier = new J48();
		fc = new FilteredClassifier();
		StringToWordVector filter = new StringToWordVector();
		filter.setOutputWordCounts(true);
		String[] options = new String[3];
		options[0] = "-C";
		options[1] = "-T";
		options[2] = "-I";
		filter.setOptions(options);
		LovinsStemmer stemmer = new LovinsStemmer();
		filter.setStemmer(stemmer);
		filter.setLowerCaseTokens(true);
		
		fc.setFilter(filter);
		fc.setClassifier(classifier);
		fc.buildClassifier(dataRaw);
		//System.out.println(fc);
//Evaluation eval = new Evaluation(dataRaw);
//eval.crossValidateModel(fc,dataRaw,2,new Random(1));
		//ConsoleView.printlInConsoleln(fc.graph().);
		//System.out.println(eval.toSummaryString("Results\n",false));
//ConsoleView.printlInConsole(eval.toSummaryString("\nResults\n======\n",false));
//double[][] confusion = eval.confusionMatrix();
		
//for (int i = 0; i < confusion.length; i++) {
//for (int j = 0; j < confusion[0].length; j++)
	//ConsoleView.printlInConsole(confusion[i][j] + "\t");
//ConsoleView.printlInConsoleln();
//}
//ConsoleView.printlInConsoleln("Accuracy:" + calculateAccuracy(eval.predictions()));
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
		
	public boolean doCrossValidate(int k, IProgressMonitor monitor, Date dateObj) throws Exception {
		//System.out.println("Value of K here like from UI==="+k);
		//System.out.println(dataRaw);
		crossValidate(fc, dataRaw, k);
		
		return true;
	}
	
	
	private static void crossValidate(FilteredClassifier fc, Instances dataRaw, int k) throws Exception {
		Evaluation eval = new Evaluation(dataRaw);
		//System.out.println("Value of K============" + k);
		eval.crossValidateModel(fc,dataRaw,k,new Random(1));
		ConsoleView.printlInConsole(eval.toSummaryString("\nResults\n======\n",false));
		double[][] confusion = eval.confusionMatrix();
		
		for (int i = 0; i < confusion.length; i++) {
			for (int j = 0; j < confusion[0].length; j++)
				ConsoleView.printlInConsole(confusion[i][j] + "\t");
			ConsoleView.printlInConsoleln();
		}
		ConsoleView.printlInConsoleln("Accuracy:" + calculateAccuracy(eval.predictions()));
		//writeToWebView("");
	}
	
	static	void writeToWebView(String cluster) {
		Bundle bundle = Platform.getBundle("edu.usc.cssl.tacit.classify.id3.ui.treevisualization");
		//URL fileURL = bundle.getEntry("test");
		
		File file = null;
		try {
			System.out.println("**********************************");
			//URI fileURI= new URI(FileLocator.resolve(fileURL).toString().replaceAll(" ", "%20"));
			//file = new File(fileURI);
			//FileWriter fw = new FileWriter(file);
			//fw.write(cluster);
			//fw.close();
			
			if(true) {//check box
				Display.getDefault().asyncExec(new Runnable() {
				    @Override
				    public void run() {
				    	try {
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("edu.usc.cssl.tacit.classify.id3.ui.treevisuals");
						} catch (PartInitException e) {
							e.printStackTrace();
						}
				    }
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}	
		
		//DataSource source1 = new DataSource("C:/Users/RESHMA BHATIA/Desktop/weka/mytest.arff");
		//Instances test = source1.getDataSet();
		//test.setClassIndex(test.numAttributes()-1);
		//Evaluation testEval = new Evaluation(data);
		//testEval.evaluateModel(fc, test);
		//System.out.println(testEval.toSummaryString("\nResults\n======\n", false));
		//System.out.println(test.numInstances()+"instances loaded");

		String big;
		/*
		for (int i = 0; i < test.numInstances(); i++) {
		   double pred = fc.classifyInstance(test.instance(i));
		   //System.out.print("ID: " + test.instance(i).value(0));
		   //System.out.print(", actual: " + test.classAttribute().value((int) test.instance(i).classValue()));
		   //System.out.println(", predicted: " + test.classAttribute().value((int) pred));
		   big=test.classAttribute().value((int) pred);
		   ConsoleView.printlInConsole("ID: " + test.instance(i).value(0));
		   ConsoleView.printlInConsole(", actual: " + test.classAttribute().value((int) test.instance(i).classValue()));
		   ConsoleView.printlInConsole(", predicted: " + test.classAttribute().value((int) pred));
		   ConsoleView.printlInConsole("**************");
		   //System.out.println("***********************");
		   //System.out.println(big);
		   
		}
		*/
		
	
	
	public boolean doClassify(String classificationInputDir, Map<String, List<String>> classPaths2, String classificationOutputDir, IProgressMonitor monitor, 
			Date dateObj) throws Exception {
		//System.out.println("Testing");
		//System.out.println(classPaths2);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(generateOutputFileName(classificationOutputDir, dateObj))));
		Instances rawTestData = new DirectoryToArffId3().createTestInstances(classificationInputDir,classPaths2);
		System.out.println("***************RawTestData***********");
		System.out.println(rawTestData);
		System.out.println("***************RawTestData***********");
		rawTestData.setClassIndex(rawTestData.numAttributes()-1);
		for (int i = 0; i < rawTestData.numInstances(); i++)
		{
			double pred = fc.classifyInstance(rawTestData.instance(i));
			//ConsoleView.printlInConsole("ID: " + rawTestData.instance(i).value(0));
			//ConsoleView.printlInConsole(", actual: " + rawTestData.classAttribute().value((int) rawTestData.instance(i).classValue()));
			//ConsoleView.printlInConsole(", predicted: " + rawTestData.classAttribute().value((int) pred));
			//ConsoleView.printlInConsole("**************");
			String fileName = DirectoryToArffId3.instanceIdNameMap.get(i).replaceAll("[,:*?\"<>|]+", ""); 
			String predictedClass = rawTestData.classAttribute().value((int) pred).replaceAll("[,:*?\"<>|]+", "");
			bw.write(fileName + "," + predictedClass + "\n");
		}
		bw.close();
		return true;
	}
	
	protected String generateOutputFileName(String classificationOutputDir, Date dateObj){
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
		String outputFileName = classificationOutputDir + System.getProperty("file.separator") + "C4.5_Decision_Trees_classification_results" + "-" + df.format(dateObj)+"-output.csv";
		return outputFileName;
	}
	
	public void generateTree() throws Exception {
		// TODO Auto-generated method stub
		String graph=fc.graph();
	    //System.out.println(graph);
		PrefuseTree obj = new PrefuseTree();
		//String dotFormat="digraph J48Tree {N0 [label=\"hi\" ]N0->N1 [label=\"<= 0\"]N1 [label=\"bermuda\" ]N1->N2 [label=\"<= 0\"]N2 [label=\"C:\\\\Users\\\\RESHMA BHATIA\\\\Desktop\\\\ID3 Data\\\\fao780 (1542.0/5.0)\" shape=box style=filled ]N1->N3 [label=\"> 0\"]N3 [label=\"0\" ]N3->N4 [label=\"<= 0\"]N4 [label=\"C:\\\\Users\\\\RESHMA BHATIA\\\\Desktop\\\\ID3 Data\\\\reuters21578.tar (3.0)\" shape=box style=filled ]N3->N5 [label=\"> 0\"]N5 [label=\"C:\\\\Users\\\\RESHMA BHATIA\\\\Desktop\\\\ID3 Data\\\\fao780 (22.0)\" shape=box style=filled ]N0->N6 [label=\"> 0\"]N6 [label=\"C:\\\\Users\\\\RESHMA BHATIA\\\\Desktop\\\\ID3 Data\\\\citeulike180 (183.0)\" shape=box style=filled ]}";
		obj.display(graph, "DotGraph");
		//demo.main(null);
	}
	}
