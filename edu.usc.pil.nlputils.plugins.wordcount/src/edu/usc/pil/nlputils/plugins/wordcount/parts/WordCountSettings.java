 
package edu.usc.pil.nlputils.plugins.wordcount.parts;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import java.io.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.usc.pil.nlputils.plugins.wordcount.analysis.WordCount;

import org.eclipse.swt.widgets.Group;
import org.eclipse.wb.swt.SWTResourceManager;;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class WordCountSettings {
	private String[] inputFiles;
	private Text txtInputFile;
	private Text txtDictionary;
	private Text txtStopWords;
	private Text txtOutputFile;
	private static Logger logger = Logger.getLogger(WordCountSettings.class.getName());
	private Text txtDelimiters;
	//private static Handler consoleHandler = new ConsoleHandler();
	//private static Logger logger = LoggerFactory.getLogger(WordCountSettings.class.getName());
	
	@Inject
	public WordCountSettings() {
		//TODO Your code here
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		parent.setLayout(new GridLayout(9, false));
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Label lblInputFile = new Label(parent, SWT.NONE);
		lblInputFile.setText("Input File");
		new Label(parent, SWT.NONE);
		
		txtInputFile = new Text(parent, SWT.BORDER);
		GridData gd_txtInputFile = new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1);
		gd_txtInputFile.widthHint = 183;
		txtInputFile.setLayoutData(gd_txtInputFile);
		
		Button btnInputFile = new Button(parent, SWT.NONE);
		btnInputFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				/*
				 Multiple Files
				FileDialog ifDialog = new FileDialog(shell, SWT.MULTI);
				ifDialog.open();
				for (String file : ifDialog.getFileNames())
					System.out.println(file);
				System.out.println(ifDialog.getFilterPath());
				*/
				FileDialog ifDialog = new FileDialog(shell, SWT.MULTI);
				ifDialog.open();
				
				// Since the FileDialog will not return the absolute file paths, add directory path to each file name
				inputFiles = new String[ifDialog.getFileNames().length];
				for (int i=0; i<ifDialog.getFileNames().length; i++){
					inputFiles[i] = ifDialog.getFilterPath()+"\\"+ifDialog.getFileNames()[i];
				}
				// Display the concatenated paths in the text field.
				StringBuilder sb = new StringBuilder();
				for (String inputFile : ifDialog.getFileNames())
					sb.append(ifDialog.getFilterPath()+"\\"+inputFile+", ");
				txtInputFile.setText(sb.toString());
			}
		});
		btnInputFile.setText("...");
		new Label(parent, SWT.NONE);
		
		Label lblDictionary = new Label(parent, SWT.NONE);
		lblDictionary.setText("Dictionary");
		new Label(parent, SWT.NONE);
		
		txtDictionary = new Text(parent, SWT.BORDER);
		txtDictionary.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
		
		Button btnDictionary = new Button(parent, SWT.NONE);
		btnDictionary.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				FileDialog dfDialog = new FileDialog(shell, SWT.MULTI);
				dfDialog.open();
				txtDictionary.setText(dfDialog.getFilterPath()+"\\"+dfDialog.getFileName());
			}
		});
		btnDictionary.setText("...");
		new Label(parent, SWT.NONE);
		
		Label lblStopWords = new Label(parent, SWT.NONE);
		lblStopWords.setText("Stop Words");
		new Label(parent, SWT.NONE);
		
		txtStopWords = new Text(parent, SWT.BORDER);
		txtStopWords.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
		
		Button btnStopWords = new Button(parent, SWT.NONE);
		btnStopWords.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				FileDialog sfDialog = new FileDialog(shell, SWT.MULTI);
				sfDialog.open();
				txtStopWords.setText(sfDialog.getFilterPath()+"\\"+sfDialog.getFileName());
			}
		});
		btnStopWords.setText("...");
		new Label(parent, SWT.NONE);
		
		Label lblOutputFile = new Label(parent, SWT.NONE);
		lblOutputFile.setText("Output File");
		new Label(parent, SWT.NONE);
		
		txtOutputFile = new Text(parent, SWT.BORDER);
		GridData gd_txtOutputFile = new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1);
		gd_txtOutputFile.widthHint = 273;
		txtOutputFile.setLayoutData(gd_txtOutputFile);
		
		Button btnOutputFile = new Button(parent, SWT.NONE);
		btnOutputFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				FileDialog ofDialog = new FileDialog(shell, SWT.SAVE);
				ofDialog.open();
				txtOutputFile.setText(ofDialog.getFilterPath()+"\\"+ofDialog.getFileName());
			}
		});
		btnOutputFile.setText("...");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		final Button btnSpss = new Button(parent, SWT.CHECK);
		btnSpss.setText("Create SPSS raw file");
		new Label(parent, SWT.NONE);
		
		final Button btnWordDistribution = new Button(parent, SWT.CHECK);
		GridData gd_btnWordDistribution = new GridData(SWT.LEFT, SWT.CENTER, false, false, 6, 1);
		gd_btnWordDistribution.widthHint = 313;
		btnWordDistribution.setLayoutData(gd_btnWordDistribution);
		btnWordDistribution.setText("Create Category-wise Word Distribution Files");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Group grpPreprocessing = new Group(parent, SWT.NONE);
		grpPreprocessing.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		grpPreprocessing.setText("Pre-processing");
		GridData gd_grpPreprocessing = new GridData(SWT.LEFT, SWT.CENTER, false, false, 7, 1);
		gd_grpPreprocessing.heightHint = 106;
		gd_grpPreprocessing.widthHint = 441;
		grpPreprocessing.setLayoutData(gd_grpPreprocessing);
		
		Label lblConvertToLowercase = new Label(grpPreprocessing, SWT.NONE);
		lblConvertToLowercase.setBounds(10, 61, 114, 15);
		lblConvertToLowercase.setText("Convert to Lowercase");
		
		final Button btnYes = new Button(grpPreprocessing, SWT.RADIO);
		btnYes.setBounds(135, 60, 39, 16);
		btnYes.setSelection(true);
		btnYes.setText("Yes");
		
		final Button btnNo = new Button(grpPreprocessing, SWT.RADIO);
		btnNo.setBounds(187, 60, 37, 16);
		btnNo.setText("No");
		
		Label lblDelimiters = new Label(grpPreprocessing, SWT.NONE);
		lblDelimiters.setBounds(10, 33, 53, 15);
		lblDelimiters.setText("Delimiters");
		
		txtDelimiters = new Text(grpPreprocessing, SWT.BORDER);
		txtDelimiters.setBounds(135, 30, 288, 21);
		txtDelimiters.setText(" .,;'\\\"!-()[]{}:?");
		
		Composite composite = new Composite(grpPreprocessing, SWT.NONE);
		composite.setBounds(4, 83, 419, 31);
		
		final Button btnStemming = new Button(composite, SWT.RADIO);
		btnStemming.setSelection(true);
		btnStemming.setBounds(4, 9, 141, 16);
		btnStemming.setText("LIWC Style Stemming");
		
		final Button btnSnowball = new Button(composite, SWT.RADIO);
		btnSnowball.setBounds(183, 9, 188, 16);
		btnSnowball.setText("Snowball (Porter 2) Stemming");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		
		
		Button btnAnalyze = new Button(parent, SWT.NONE);
		btnAnalyze.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				/*logger.setLevel(Level.FINER);
				consoleHandler.setLevel(Level.FINER);
				Logger.getAnonymousLogger().addHandler(consoleHandler);
				logger.addHandler(consoleHandler);*/
				int returnCode = -1;
				String errorMessage = "Word Count failed. Please try again.";
				WordCount wc = new WordCount();
				
				// Injecting the context into WordCount object so that the appendLog function can modify the Context Parameter consoleMessage
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(wc,iEclipseContext);
				
				logger.info("Analyze button clicked. "+txtInputFile.getText()+" "+txtDictionary.getText()+" "+txtStopWords.getText()+" "+txtOutputFile.getText()+" "+txtDelimiters.getText()+" "+btnYes.getSelection());				
				
				
				try {
					returnCode=wc.wordCount(inputFiles, txtDictionary.getText(), txtStopWords.getText(), txtOutputFile.getText(), txtDelimiters.getText(),btnYes.getSelection(),btnStemming.getSelection(),btnSnowball.getSelection(), btnSpss.getSelection(),btnWordDistribution.getSelection());
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				
				if (returnCode == -2)
					errorMessage = "Please check the input file path.";
				if (returnCode == -3) 
					errorMessage = "Please check the dictionary file path.";
				if (returnCode == -4) 
					errorMessage = "Please check the stop words file path.";
				if (returnCode == -5) 
					errorMessage = "The output file path is incorrect or the file already exists.";
				if (returnCode == -6)
					errorMessage = "The SPSS output file path is incorrect or the file already exists.";
				if (returnCode == 0)
					errorMessage = "Word Count Completed Successfully.";
				
				appendLog(errorMessage);
				
				if (returnCode == 0){
					MessageBox message = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
					message.setMessage(errorMessage);
					message.setText("Success");
					message.open();
				} else{
					MessageBox message = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					message.setMessage(errorMessage);
					message.setText("Error");
					message.open();
				}
				
			}
		});
		btnAnalyze.setText("Analyze");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		
		//TODO Your code here
	}
	
	
	@Focus
	public void onFocus() {
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