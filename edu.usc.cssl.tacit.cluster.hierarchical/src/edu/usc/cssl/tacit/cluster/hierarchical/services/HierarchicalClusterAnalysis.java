package edu.usc.cssl.tacit.cluster.hierarchical.services;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import edu.usc.cssl.tacit.common.TacitUtility;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import weka.clusterers.HierarchicalClusterer;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.gui.hierarchyvisualizer.HierarchyVisualizer;


public class HierarchicalClusterAnalysis {
	static boolean showGraph = false;
	static List<File> inputFiles;
	public static String doClustering(List<File> inputFiles, String outputPath,
			boolean openResults, SubProgressMonitor subProgressMonitor, Date dateObj, boolean junitTest) {
		showGraph = openResults;
		HierarchicalClusterAnalysis.inputFiles = inputFiles;
		try {
			DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
			StringToWordVector filter = new StringToWordVector();
			HierarchicalClusterer aggHierarchical = new HierarchicalClusterer();

			FastVector atts = new FastVector(1);
			atts.addElement(new Attribute("text", (FastVector) null));

			Instances docs = new Instances("text_files", atts, 0);

			ConsoleView.printlInConsoleln(outputPath);

			for (int i = 0; i < inputFiles.size(); i++) {

				try {
					double[] newInst = new double[1];
					String content = new Scanner(inputFiles.get(i))
							.useDelimiter("\\Z").next();
					newInst[0] = (double) docs.attribute(0).addStringValue(
							content);
					docs.add(new Instance(1.0, newInst));
				} catch (Exception e) {
					ConsoleView.printlInConsoleln("Exception occurred in reading files"
							+ e);
				}
			}
			filter.setInputFormat(docs);
			Instances filteredData = Filter.useFilter(docs, filter);

			aggHierarchical.setNumClusters(1);
			aggHierarchical.setPrintNewick(true);
			subProgressMonitor.subTask("Building cluster");
			aggHierarchical.buildClusterer(filteredData);
			subProgressMonitor.worked(20);
			String g = aggHierarchical.graph();
			String output = formatGraph(g, inputFiles);
			System.out.println(output);
			ConsoleView.printlInConsoleln("Network " + output);
			subProgressMonitor.subTask("Formating Image");
			aggHierarchical.linkTypeTipText();

			subProgressMonitor.worked(15);

/*		
	  		HierarchyVisualizer tv = new HierarchyVisualizer(output);

			tv.setSize(1024, 1024);
			JFrame f;
			f = new JFrame();
			JPanel container = new JPanel();
			JScrollPane scrPane = new JScrollPane(container);
			Container contentPane = f.getContentPane();
			contentPane.setLayout(new BorderLayout());
			f.getContentPane().add(scrPane);
			contentPane.add(tv, BorderLayout.CENTER);
			f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			f.setSize(1024, 1024);
			f.setVisible(true);
			tv.fitToScreen();

			if (saveImg) {
				try {
					BufferedImage image = new BufferedImage(
							contentPane.getWidth(), contentPane.getHeight(),
							BufferedImage.TYPE_INT_RGB);
					Graphics2D graphics2D = image.createGraphics();
					contentPane.printAll(graphics2D);
					graphics2D.dispose();
					subProgressMonitor.subTask("Saving image @ " + outputPath
							+ File.separator
							+ "Hierarchical Clustering Output "+df.format(dateObj)+".jpeg");
					ImageIO.write(image, "jpeg", new File(outputPath
							+ File.separator
							+ "Hierarchical Clustering Output "+df.format(dateObj)+".jpeg"));
					subProgressMonitor.worked(10);
				} catch (Exception e) {
					System.out
							.println("Exception occurred in saving image of output "
									+ e);
				}
			}
			
*/
			
			File outputFile = null;
			if (junitTest)
				outputFile = new File(outputPath + File.separator + "GeneratedHierarchicalClustersOutput.txt");
			else
				outputFile = new File(
						outputPath + File.separator + "hierarchical-cluster-"+df.format(dateObj)+".txt");
			BufferedWriter buf = new BufferedWriter(new FileWriter(outputFile));
			buf.write("Mapping of document ID to actual names\n");
			for (int i = 0; i < inputFiles.size(); i++) {
				buf.write((i + 1) + " " + inputFiles.get(i).getName() + "\n");
			}
			buf.write(output);
			buf.close();
			
			subProgressMonitor.done();
			return output;
		} catch (Exception e) {
			System.out
					.println("Exception occurred in Hierarchical Clustering  "
							+ e);
		}
		return null;
	}

	public static String formatGraph(String graph, List<File> files) {
		StringBuffer fgraph = new StringBuffer();

		String input = graph.substring(7);
		int i = 0, len = input.length();
		char c;
		int count = 0;

		fgraph.append(graph.substring(0, 7));
		ConsoleView.printlInConsoleln(graph);
		while (i < len) {
			c = input.charAt(i);
			if (c == '(') {
				fgraph.append(input.charAt(i++));
			} else if (c == ':') {
				if (input.charAt(i - 1) != ')') {
					fgraph.append(++count);

				}
				while (i < len
						&& (input.charAt(i) != ',' && input.charAt(i) != '(')) {
					fgraph.append(input.charAt(i++));
					// ConsoleView.writeInConsole(fgraph.toString());
				}
				if (i < len)
					fgraph.append(input.charAt(i++));
			} else {
				i++;
			}
		}
		ConsoleView.printlInConsoleln(fgraph.toString());
		return fgraph.toString();
	}

	public static boolean runClustering(List<File> listOfFiles,
			String fOutputDir, boolean openResults,
			SubProgressMonitor subProgressMonitor, Date dateObj, boolean junitTest) {

		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
		List<File> inputFiles = new ArrayList<File>();
		for (File f : listOfFiles) {
			if (f.getAbsolutePath().contains("DS_Store"))
				continue;
			if (!f.isDirectory() && f.exists())
				inputFiles.add(f);
		}
		subProgressMonitor.beginTask("Running CLustering", 50);
		subProgressMonitor.subTask("Running Hierarchical Clustering...");
		ConsoleView.printlInConsoleln("Running Hierarchical Clustering...");
		String clusters = doClustering(inputFiles, fOutputDir, openResults,
				new SubProgressMonitor(subProgressMonitor, 45), dateObj, junitTest);
		if (subProgressMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		if (clusters == null) {
			return false;
		}
		ConsoleView.printlInConsoleln("Output for Hierarchical Clustering");
		ConsoleView.printlInConsoleln("Mapping of document ID to actual names");
		subProgressMonitor.subTask("Mapping of document ID to actual names");
		for (int i = 0; i < inputFiles.size(); i++) {
			ConsoleView.printlInConsoleln((i + 1) + " " + inputFiles.get(i).getName());
		}
		subProgressMonitor.worked(5);
		ConsoleView.printlInConsoleln("Clusters formed: \n");
		System.out.println("clusters are "+clusters);
		ConsoleView.printlInConsoleln(clusters);
		ConsoleView.printlInConsoleln("Saving the output to hierarchical-cluster-"+df.format(dateObj)+".txt");
		subProgressMonitor.subTask("Saving the output to hierarchical-cluster-"+df.format(dateObj)+".txt");
		ConsoleView.printlInConsoleln("\nDone Hierarchical Clustering...");
		if (!junitTest)
			TacitUtility.createRunReport(fOutputDir, "Hierarchical Clustering",dateObj,null);
		subProgressMonitor.worked(5);
		subProgressMonitor.done();
		if(clusters!=null)
			System.out.println(clusters);
			writeToWebView(buildGraphString(clusters.substring(7)));	//clip the "Newick:" portion out
		
		return true;
	}
	
	
	static	void writeToWebView(String cluster) {
		Bundle bundle = Platform.getBundle("edu.usc.cssl.tacit.webview.ui");
		URL fileURL = bundle.getEntry("test");
		File file = null;
		try {
			//file = new File(FileLocator.resolve(fileURL).toURI());
			URI fileURI= new URI(FileLocator.resolve(fileURL).toString().replaceAll(" ", "%20"));
			file = new File(fileURI);
			FileWriter fw = new FileWriter(file);
			fw.write(cluster);
			fw.close();
			
			if(showGraph) {
				Display.getDefault().asyncExec(new Runnable() {
				    @Override
				    public void run() {
				    	try {
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("edu.usc.cssl.tacit.webview.ui.view");
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
	
	static String buildGraphString(String cluster) {
		int i2 = cluster.length();
		String str = "";
		boolean flag = false;
		for (int i = cluster.length()-1; i >=0; i--){
			if (cluster.charAt(i)==':') {
				str = cluster.substring(i, i2) + str;
				i2 = i;
				flag = true;

			}
			if(flag && (cluster.charAt(i)==','||cluster.charAt(i)=='(')) {
				flag = false;
				str = inputFiles.get(Integer.parseInt(cluster.substring(i+1, i2))-1).getName() + str;

				i2 = i+1;

			}
		}

		str = cluster.substring(0, i2) + str;
		System.out.println(str);
		return str;
	}
}


