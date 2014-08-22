/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */ 
package edu.usc.pil.nlputils.plugins.zlda.parts;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.usc.pil.nlputils.plugins.zlda.process.DTWC;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

public class ZldaSettings {
	private Text txtInputDir;
	@Inject
	public ZldaSettings() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		parent.setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite.widthHint = 431;
		gd_composite.heightHint = 477;
		composite.setLayoutData(gd_composite);
		
		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setText("Input Path");
		
		txtInputDir = new Text(composite, SWT.BORDER);
		GridData gd_txtInputDir = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtInputDir.widthHint = 244;
		txtInputDir.setLayoutData(gd_txtInputDir);
		
		button = new Button(composite, SWT.NONE);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog dd = new DirectoryDialog(shell);
				dd.open();
				String path = dd.getFilterPath();
				txtInputDir.setText(path);
			}
		});
		button.setText("...");
		
		lblStopFile = new Label(composite, SWT.NONE);
		lblStopFile.setText("Stop File");
		
		txtStopFile = new Text(composite, SWT.BORDER);
		GridData gd_txtStopFile = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtStopFile.widthHint = 244;
		txtStopFile.setLayoutData(gd_txtStopFile);
		
		button_1 = new Button(composite, SWT.NONE);
		button_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				txtStopFile.setText("");
				FileDialog fd = new FileDialog(shell,SWT.SAVE);
				fd.open();
				String oFile = fd.getFileName();
				String dir = fd.getFilterPath();
				txtStopFile.setText(dir+System.getProperty("file.separator")+oFile);
			}
		});
		button_1.setText("...");
		
		lblSeedFile = new Label(composite, SWT.NONE);
		lblSeedFile.setText("Seed File");
		
		txtSeedFile = new Text(composite, SWT.BORDER);
		GridData gd_txtSeedFile = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtSeedFile.widthHint = 244;
		txtSeedFile.setLayoutData(gd_txtSeedFile);
		
		button_2 = new Button(composite, SWT.NONE);
		button_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				txtSeedFile.setText("");
				FileDialog fd = new FileDialog(shell,SWT.SAVE);
				fd.open();
				String oFile = fd.getFileName();
				String dir = fd.getFilterPath();
				txtSeedFile.setText(dir+System.getProperty("file.separator")+oFile);
			}
		});
		button_2.setText("...");
		
		lblNumberOfTopics = new Label(composite, SWT.NONE);
		lblNumberOfTopics.setText("Number of Topics");
		
		txtNumTopics = new Text(composite, SWT.BORDER);
		new Label(composite, SWT.NONE);
		
		lblAlpha = new Label(composite, SWT.NONE);
		lblAlpha.setText("Alpha");
		
		txtAlpha = new Text(composite, SWT.BORDER);
		new Label(composite, SWT.NONE);
		
		lblBeta = new Label(composite, SWT.NONE);
		lblBeta.setText("Beta");
		
		txtBeta = new Text(composite, SWT.BORDER);
		new Label(composite, SWT.NONE);
		
		lblNumberOfSamples = new Label(composite, SWT.NONE);
		lblNumberOfSamples.setText("Number of Samples");
		
		txtNumSamples = new Text(composite, SWT.BORDER);
		new Label(composite, SWT.NONE);
		
		lblConfidence = new Label(composite, SWT.NONE);
		lblConfidence.setText("Confidence");
		
		txtConfidence = new Text(composite, SWT.BORDER);
		new Label(composite, SWT.NONE);
		
		lblOutputPath = new Label(composite, SWT.NONE);
		lblOutputPath.setText("Output Path");
		
		txtOutputDir = new Text(composite, SWT.BORDER);
		GridData gd_txtOutputDir = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtOutputDir.widthHint = 244;
		txtOutputDir.setLayoutData(gd_txtOutputDir);
		
		button_3 = new Button(composite, SWT.NONE);
		button_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog dd = new DirectoryDialog(shell);
				dd.open();
				String path = dd.getFilterPath();
				txtOutputDir.setText(path);
			}
		});
		button_3.setText("...");
		
		Button btnCalculate = new Button(composite, SWT.NONE);
		btnCalculate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				invokeLDA();
			}
		});
		btnCalculate.setText("Calculate");
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		
	}
	
	@Inject
	IEclipseContext context;
	private Text txtStopFile;
	private Text txtSeedFile;
	private Text txtNumTopics;
	private Text txtAlpha;
	private Text txtBeta;
	private Text txtNumSamples;
	private Text txtConfidence;
	private Text txtOutputDir;
	private Label lblOutputPath;
	private Label lblStopFile;
	private Label lblSeedFile;
	private Label lblNumberOfTopics;
	private Label lblAlpha;
	private Label lblBeta;
	private Label lblNumberOfSamples;
	private Label lblConfidence;
	private Button button;
	private Button button_1;
	private Button button_2;
	private Button button_3;
	private void appendLog(String message){
		IEclipseContext parent = context.getParent();
		//System.out.println(parent.get("consoleMessage"));
		String currentMessage = (String) parent.get("consoleMessage"); 
		if (currentMessage==null)
			parent.set("consoleMessage", message);
		else {
			if (currentMessage.equals(message)) {
				// Set the param to null before writing the message if it is the same as the previous message. 
				// Else, the change handler will not be called.
				parent.set("consoleMessage", null);
				parent.set("consoleMessage", message);
			}
			else
				parent.set("consoleMessage", message);
		}
	}
	
protected void invokeLDA(){
		File dir = new File(txtInputDir.getText());
		File[] listOfFiles =  dir.listFiles();
		/*
		java.util.List<File> inputFiles = new java.util.ArrayList<File>();
		for(int i=0; i<listOfFiles.length; i++){
			if(fileList[i+1].isSelected()){
				inputFiles.add(listOfFiles[i]);
			}
		}
		*/
		List<File> inputFiles = new ArrayList<File>();
		for (File f : listOfFiles)
			inputFiles.add(f);
		if(inputFiles.size() == 0){
			appendLog("Please select at least one file on which to run LDA");
			return;
		}
		
		File stopFile = new File(txtStopFile.getText());
		File seedFile = new File(txtSeedFile.getText());
		int numTopics = Integer.parseInt(txtNumTopics.getText());
		
		DTWC dtwc = new DTWC(inputFiles, seedFile, stopFile);
		dtwc.computeDocumentVectors();
		
		int[][][] zlabels = dtwc.getTopicSeedsAsInt();
		int[][] docs = dtwc.getDocVectorsAsInt();
		
		int T = numTopics;
		int W = dtwc.getVocabSize();
		
		double[][] alpha = new double[1][T];
		
		double alphaval;
		if(txtAlpha.getText().length() == 0){
			alphaval = 0.5;
		}
		else{
			alphaval = Double.parseDouble(txtAlpha.getText());
		}
		
		for(int i=0; i<T; i++){
			alpha[0][i] = alphaval;
		}
		
		double[][] beta = new double[T][W];
		
		double betaval;
		if(txtBeta.getText().length() == 0){
			betaval = 0.1;
		}
		else{
			betaval = txtBeta.getText().length();
		}
		for(int i=0; i<T; i++){
			for(int j=0; j<W; j++){
				beta[i][j] = betaval;
			}
		}
		
		int noOfSamples = 1000;
		if(txtNumSamples.getText().length() != 0){
			noOfSamples = Integer.parseInt(txtNumSamples.getText());
		}
		
		double confidenceValue = 0.95;
		if(txtConfidence.getText().length() !=0){
			confidenceValue = Double.parseDouble(txtConfidence.getText());
		}
		ZlabelLDA zelda = new ZlabelLDA(docs, zlabels, confidenceValue, alpha, beta, noOfSamples ); 
		
		boolean retVal = zelda.zLDA();
		
		if(!retVal){
			appendLog("Sorry, something is wrong with the input - please check format and try again");
			return;
		}
		
		double[][] theta, phi;
		
		theta = zelda.getTheta();
		phi = zelda.getPhi();
		
		/*System.out.println("\n");
		System.out.println(theta.length);
		System.out.println(theta[0].length);
		
		String toPrint;
		for(int i=0; i<docs.length; i++){
			toPrint = "";
			for(int j=0; j<T; j++){
				toPrint = toPrint + theta[i][j] + " ";
			}
			System.out.println(toPrint);
		}
		
		System.out.println("\n"); */
		
		java.util.Map<String, Integer> dictionary = dtwc.getTermIndex();
		java.util.Map<Integer, String> revDict = dtwc.getIndexTerm(); 

		java.util.List<java.util.List<String>> topicWords = new java.util.ArrayList<java.util.List<String>>(); 
		for(int i=0; i<T; i++){
			topicWords.add(new java.util.ArrayList<String>());
		}
		
		for(int i=0; i<T; i++){
			for(int j=0; j<W; j++){
				if(phi[i][j] > 0.001){
					topicWords.get(i).add(revDict.get(j));
				}
			}
		}
		
		try {
			FileWriter fw = new FileWriter(new File(txtOutputDir.getText() + "\\topicwords.csv"));
			for(int i=0; i<T; i++){
				fw.write("Topic" + i + ",");
				for(int j=0; j<(topicWords.get(i)).size(); j++){
					fw.write(topicWords.get(i).get(j) + ",");
				}
				fw.write("\n");
				fw.flush();
			}
			fw.flush();
			fw.close();
			
			fw = new FileWriter(new File(txtOutputDir.getText() + "\\phi.csv"));
			for(int i=0; i<T; i++){
				fw.write("Topic" + i + ",");
				for(int j=0; j<phi[i].length; j++){
					fw.write(phi[i][j] + ",");
				}
				fw.write("\n");
				fw.flush();
			}
			fw.flush();
			fw.close();
			
			fw = new FileWriter(new File(txtOutputDir.getText() + "\\theta.csv"));
			for(int i=0; i<docs.length; i++){
				fw.write("Document" + i + ",");
				for(int j=0; j<theta[i].length; j++){
					fw.write(theta[i][j] + ",");
				}
				fw.write("\n");
				fw.flush();
			}
			fw.flush();
			fw.close();
		}
		catch(Exception e){
			appendLog("Error writing output to files");
		}

		
	}
}