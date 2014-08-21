 
package edu.usc.pil.nlputils.plugins.nbclassifier.parts;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import bsh.EvalError;

import edu.usc.pil.nlputils.plugins.nbclassifier.process.NBClassifier;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class NBClassifierSettings {
	private Text txtSource1;
	private Text txtSource2;
	private Text txtTestPath1;
	private Text text_4;
	private Text text_5;
	private Text text_6;
	private Text txtTestPath2;
	private Text txtOutputPath;
	@Inject
	public NBClassifierSettings() {
		//TODO Your code here
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		Group grpTraining = new Group(parent, SWT.NONE);
		GridData gd_grpTraining = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_grpTraining.heightHint = 126;
		gd_grpTraining.widthHint = 430;
		grpTraining.setLayoutData(gd_grpTraining);
		grpTraining.setText("Training");
		
		Label lblDataSet = new Label(grpTraining, SWT.NONE);
		lblDataSet.setBounds(10, 26, 95, 15);
		lblDataSet.setText("Class 1 Path");
		
		txtSource1 = new Text(grpTraining, SWT.BORDER);
		txtSource1.setBounds(133, 19, 247, 21);
		
		Button button = new Button(grpTraining, SWT.NONE);
		button.setBounds(385, 16, 21, 25);
		button.setText("...");
		
		Label lblModelFilePath = new Label(grpTraining, SWT.NONE);
		lblModelFilePath.setBounds(10, 62, 95, 15);
		lblModelFilePath.setText("Class 2 Path");
		
		txtSource2 = new Text(grpTraining, SWT.BORDER);
		txtSource2.setBounds(133, 59, 247, 21);
		
		Button button_1 = new Button(grpTraining, SWT.NONE);
		button_1.setBounds(385, 57, 21, 25);
		button_1.setText("...");
		
		final Button btnConvertToLowercase = new Button(grpTraining, SWT.CHECK);
		btnConvertToLowercase.setBounds(286, 103, 140, 16);
		btnConvertToLowercase.setText("Convert to Lowercase");
		
		final Button btnKeepSequence = new Button(grpTraining, SWT.CHECK);
		btnKeepSequence.setBounds(10, 103, 108, 16);
		btnKeepSequence.setText("Keep Sequence");
		
		final Button btnRemoveStopWords = new Button(grpTraining, SWT.CHECK);
		btnRemoveStopWords.setBounds(133, 103, 128, 16);
		btnRemoveStopWords.setText("Remove Stop Words");
		
		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		
		TabItem tbtmTesting = new TabItem(tabFolder, SWT.NONE);
		tbtmTesting.setText("Test");
		
		Group grpTesting = new Group(tabFolder, SWT.NONE);
		tbtmTesting.setControl(grpTesting);
		
		Label lblNewLabel = new Label(grpTesting, SWT.NONE);
		lblNewLabel.setBounds(10, 28, 98, 15);
		lblNewLabel.setText("Class 1 Test Path");
		
		txtTestPath1 = new Text(grpTesting, SWT.BORDER);
		txtTestPath1.setBounds(134, 22, 245, 21);
		
		Button button_2 = new Button(grpTesting, SWT.NONE);
		button_2.setBounds(385, 20, 21, 25);
		button_2.setText("...");
		
		Button btnTest = new Button(grpTesting, SWT.NONE);
		btnTest.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				NBClassifier nb = new NBClassifier();
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(nb,iEclipseContext);
				
				try {
					System.out.println("Processing...");
					appendLog("Processing...");
					long startTime = System.currentTimeMillis();
					nb.doClassification(txtSource1.getText(), txtSource2.getText(), txtTestPath1.getText(), txtTestPath2.getText(), txtOutputPath.getText(), btnConvertToLowercase.getSelection(), btnKeepSequence.getSelection(), btnRemoveStopWords.getSelection());
					System.out.println("Naive Bayes Classification completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
					appendLog("Naive Bayes Classification completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (EvalError e1) {
					e1.printStackTrace();
				}
			}
		});
		btnTest.setBounds(10, 141, 75, 25);
		btnTest.setText("Test");
		
		Label lblClassTest = new Label(grpTesting, SWT.NONE);
		lblClassTest.setText("Class 2 Test Path");
		lblClassTest.setBounds(10, 63, 98, 15);
		
		txtTestPath2 = new Text(grpTesting, SWT.BORDER);
		txtTestPath2.setBounds(134, 57, 245, 21);
		
		Button button_6 = new Button(grpTesting, SWT.NONE);
		button_6.setText("...");
		button_6.setBounds(385, 55, 21, 25);
		
		Label lblOutputPath = new Label(grpTesting, SWT.NONE);
		lblOutputPath.setText("Output Path");
		lblOutputPath.setBounds(10, 101, 98, 15);
		
		txtOutputPath = new Text(grpTesting, SWT.BORDER);
		txtOutputPath.setBounds(134, 95, 245, 21);
		
		Button button_7 = new Button(grpTesting, SWT.NONE);
		button_7.setText("...");
		button_7.setBounds(385, 93, 21, 25);
		
		TabItem tbtmClassify = new TabItem(tabFolder, SWT.NONE);
		tbtmClassify.setText("Classify");
		
		Group grpClassify = new Group(tabFolder, SWT.NONE);
		tbtmClassify.setControl(grpClassify);
		grpClassify.setEnabled(false);
		
		Label lblInputFile = new Label(grpClassify, SWT.NONE);
		lblInputFile.setBounds(10, 29, 55, 15);
		lblInputFile.setText("Input File");
		
		text_4 = new Text(grpClassify, SWT.BORDER);
		text_4.setBounds(135, 23, 242, 21);
		
		Button button_3 = new Button(grpClassify, SWT.NONE);
		button_3.setBounds(383, 19, 21, 25);
		button_3.setText("...");
		
		Label lblOutputFile = new Label(grpClassify, SWT.NONE);
		lblOutputFile.setBounds(10, 64, 78, 15);
		lblOutputFile.setText("Model File");
		
		text_5 = new Text(grpClassify, SWT.BORDER);
		text_5.setBounds(135, 58, 242, 21);
		
		Button button_4 = new Button(grpClassify, SWT.NONE);
		button_4.setBounds(383, 54, 21, 25);
		button_4.setText("...");
		
		Label lblOutputFile_1 = new Label(grpClassify, SWT.NONE);
		lblOutputFile_1.setBounds(10, 97, 78, 15);
		lblOutputFile_1.setText("Output File");
		
		text_6 = new Text(grpClassify, SWT.BORDER);
		text_6.setBounds(135, 91, 242, 21);
		
		Button button_5 = new Button(grpClassify, SWT.NONE);
		button_5.setBounds(383, 87, 21, 25);
		button_5.setText("...");
		
		Button btnClassify = new Button(grpClassify, SWT.NONE);
		btnClassify.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				NBClassifier nb = new NBClassifier();
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(nb,iEclipseContext);
				
				try {
					System.out.println("Processing...");
					appendLog("Processing...");
					long startTime = System.currentTimeMillis();
					nb.doClassification(txtSource1.getText(), txtSource2.getText(), txtTestPath1.getText(), txtTestPath2.getText(), txtOutputPath.getText(), btnConvertToLowercase.getSelection(), btnKeepSequence.getSelection(), btnRemoveStopWords.getSelection());
					System.out.println("Naive Bayes Classification completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
					appendLog("Naive Bayes Classification completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (EvalError e1) {
					e1.printStackTrace();
				}
			}
		});
		btnClassify.setBounds(10, 131, 75, 25);
		btnClassify.setText("Classify");
		//TODO Your code here
	}
	
	@Inject IEclipseContext context;
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
}