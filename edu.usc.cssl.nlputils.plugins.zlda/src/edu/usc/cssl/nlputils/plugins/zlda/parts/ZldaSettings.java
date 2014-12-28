/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */ 
package edu.usc.cssl.nlputils.plugins.zlda.parts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.usc.cssl.nlputils.plugins.zlda.process.DTWC;
import edu.usc.cssl.nlputils.plugins.preprocessorService.services.PreprocessorService;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

public class ZldaSettings {
	private Text txtInputDir;
	private PreprocessorService ppService = new PreprocessorService();
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
				FileDialog fd = new FileDialog(shell,SWT.OPEN);
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
		
		Button btnPreprocess = new Button(composite, SWT.NONE);
		btnPreprocess.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				showPpOptions(shell);
			}
		});
		btnPreprocess.setBounds(482, 5, 75, 25);
		btnPreprocess.setText("Preprocess...");
		
		
		Button btnCalculate = new Button(composite, SWT.NONE);
		btnCalculate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				
				ppDir = txtInputDir.getText();
				ppSeedFile = txtSeedFile.getText();
				
				if(ppService.doPP) {
					
					// Injecting the context into Preprocessor object so that the appendLog function can modify the Context Parameter consoleMessage
					IEclipseContext iEclipseContext = context;
					ContextInjectionFactory.inject(ppService,iEclipseContext);

				//Preprocessing
				appendLog("Preprocessing...");
				System.out.println("Preprocessing...");
				try {
					ppDir = doPp(txtInputDir.getText());
					ppSeedFile = doPp(txtSeedFile.getText());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				}
				long startTime = System.currentTimeMillis();
				appendLog("PROCESSING...(Z-LDA)");
				invokeLDA();
				appendLog("z-label LDA completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
				appendLog("DONE");
			}
		});
		btnCalculate.setText("Calculate");
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		
	}

	private void showPpOptions(Shell shell){
		ppService.setOptions(shell);
	}
	
	private String doPp(String inputPath) throws IOException{
		return ppService.doPreprocessing(inputPath);
	}
	
	public static void main(String[] args)
	{		
		
		File dir = new File("C://Users//carlosg//Desktop//CSSL//zlab-0.1//sampledocs//");
		File seedFile = new File("C://Users//carlosg//Desktop//CSSL//zlab-0.1//topics.txt");
		int numTopics = 40;
		
		
		double alphaval = 0.5;
		double betaval = 0.1;
		
		String txtOutputDir = "C://Users//carlosg//Desktop//CSSL//zlab-0.1//sampleoutput//";
		ZldaSettings zlda = new ZldaSettings();
		zlda.runLDA(dir,  seedFile, numTopics, 2000, alphaval, betaval, 1, txtOutputDir);
	}
	
	@Inject
	IEclipseContext context;
	private Text txtSeedFile;
	private Text txtNumTopics;
	private Text txtOutputDir;
	private Label lblOutputPath;
	private Label lblSeedFile;
	private Label lblNumberOfTopics;
	private Button button;
	private Button button_2;
	private Button button_3;
	private String ppDir;
	private String ppSeedFile;
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
		File dir = new File(ppDir);
		
		File seedFile = new File(ppSeedFile);
		int numTopics = Integer.parseInt(txtNumTopics.getText());
		
		double alphaval = 0.5;
		double betaval = 0.1;
		int noOfSamples = 2000;
		double confidenceValue = 1;
		
		runLDA(dir, seedFile, numTopics, noOfSamples, alphaval, betaval, confidenceValue, txtOutputDir.getText());
		
}

protected void runLDA(File dir, File seedFile, int numTopics, int noOfSamples, 
		double alphaval, double betaval, double confidenceValue, String outputdir ){
		
		File[] listOfFiles =  dir.listFiles();
		List<File> inputFiles = new ArrayList<File>();
		for (File f : listOfFiles)
			inputFiles.add(f);
		if(inputFiles.size() == 0){
			appendLog("Please select at least one file on which to run LDA");
			return;
		}
		
		File preSeedFile;
		if(seedFile.isDirectory()){
			File [] listSeedFiles = seedFile.listFiles();
			preSeedFile = listSeedFiles[0];
		}else{
			preSeedFile = seedFile;
		}
		System.out.println("running zlabel LDA...");
		appendLog("running zlabel LDA...");
		DTWC dtwc = new DTWC(inputFiles, preSeedFile);
		dtwc.computeDocumentVectors();
		
		int[][][] zlabels = dtwc.getTopicSeedsAsInt();
		int[][] docs = dtwc.getDocVectorsAsInt();
		
	/*	System.out.println("\n");
		
		String toPrint;
		for(int i=0; i<docs.length; i++){
			toPrint = "";
			for(int j=0; j<docs[i].length; j++){
				toPrint = toPrint + docs[i][j];
			}
			System.out.println(toPrint);
		}
		
		System.out.println("\n"); */
	
		
		int T = numTopics;
		int W = dtwc.getVocabSize();
		
		double[][] alpha = new double[1][T];
		for(int i=0; i<T; i++){
			alpha[0][i] = alphaval;
		}
		
		double[][] beta = new double[T][W];
		for(int i=0; i<T; i++){
			for(int j=0; j<W; j++){
				beta[i][j] = betaval;
			}
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
		
		Map<String, Integer> dictionary = dtwc.getTermIndex();
		Map<Integer, String> revDict = dtwc.getIndexTerm(); 

		List< List <Map.Entry<String, Double>>> topicWords = new ArrayList< List<Map.Entry <String, Double>>>(); 
		for(int i=0; i<T; i++){
			topicWords.add(new ArrayList< Map.Entry<String, Double>>());
		}
		
		for(int i=0; i<T; i++){
			for(int j=0; j<W; j++){
				if(phi[i][j] > 0.001){
					topicWords.get(i).add(  new AbstractMap.SimpleEntry<String, Double>(revDict.get(j),new Double(phi[i][j])));
				}
			}
		}
		
		appendLog("Topic and its corresponding words and phi values stored in " + outputdir + "\\topicwords.csv" );
		try {
			FileWriter fw = new FileWriter(new File(outputdir + File.separator + "topicwords.csv"));
			for(int i=0; i<T; i++){
				fw.write("Topic" + i + ",");
				Collections.sort( topicWords.get(i), new Comparator<Map.Entry<String, Double>>()
				{	
							@Override
							public int compare(Entry<String, Double> arg0,
									Entry<String, Double> arg1) {
								return -(arg0.getValue()).compareTo(arg1.getValue());
							}
				} );
				for(int j=0; (j<topicWords.get(i).size() && j<50); j++){
					fw.write(topicWords.get(i).get(j).getKey()  + "," + topicWords.get(i).get(j).getValue() + 
				",");
				}
				fw.write("\n");
				fw.flush();
			}
			fw.flush();
			fw.close();
			
			appendLog("Phi values for each stopic stored in " + outputdir + File.separator + "phi.csv" );
			fw = new FileWriter(new File(outputdir + "\\phi.csv"));
			for(int i=0; i<T; i++){
				fw.write("Topic" + i + ",");
				for(int j=0; j<phi[i].length; j++){
					if(phi[i][j] > 0.001){
						fw.write(phi[i][j] + ",");
					}
				}
				fw.write("\n");
				fw.flush();
			}
			fw.flush();
			fw.close();
			
			appendLog("Theta values for each document stored in " + outputdir + File.separator + "theta.csv" );
			fw = new FileWriter(new File(outputdir + "\\theta.csv"));
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
			appendLog("Error writing output to files " + e);
		}
		appendLog("Done zlabel LDA...");
		
	}
}