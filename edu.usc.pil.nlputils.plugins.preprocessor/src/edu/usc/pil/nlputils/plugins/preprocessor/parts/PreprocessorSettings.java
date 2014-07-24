 
package edu.usc.pil.nlputils.plugins.preprocessor.parts;

import java.io.IOException;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import edu.usc.pil.nlputils.plugins.preprocessor.process.Preprocess;

public class PreprocessorSettings {
	private String[] inputFiles;
	private String outputDirectory;
	private String stopwordsFile;
	private Text txtDelimiters;
	private Text txtInput;
	private Text txtOutput;
	private Text txtSuffix;
	private Text txtStopWords;
	
	@Inject IEclipseContext context;
	public PreprocessorSettings() {
		//TODO Your code here
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		parent.setLayout(new GridLayout(3, false));
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Label lblInputFile = new Label(parent, SWT.NONE);
		lblInputFile.setText("Input File(s)");
		
		txtInput = new Text(parent, SWT.BORDER);
		GridData gd_txtInput = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtInput.widthHint = 335;
		txtInput.setLayoutData(gd_txtInput);
		
		Button button = new Button(parent, SWT.NONE);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				txtInput.setText("");
				FileDialog fd = new FileDialog(shell,SWT.MULTI);
				fd.open();
				String[] iFiles = fd.getFileNames();
				String dir = fd.getFilterPath();
				int i=0;
				inputFiles = new String[iFiles.length];
				for (String file:iFiles){
					txtInput.append(dir+"\\"+file+",");
					inputFiles[i++] = dir+"\\"+file;
					//inputFiles[i++] = file;
				}
			}
		});
		button.setText("...");
		
		Label lblStopWords = new Label(parent, SWT.NONE);
		lblStopWords.setText("Stop Words");
		
		txtStopWords = new Text(parent, SWT.BORDER);
		txtStopWords.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button button_2 = new Button(parent, SWT.NONE);
		button_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				FileDialog sfd = new FileDialog(shell,SWT.OPEN);
				sfd.open();
				stopwordsFile = sfd.getFilterPath()+"\\"+sfd.getFileName();
				txtStopWords.setText(stopwordsFile);
			}
		});
		button_2.setText("...");
		
		Label lblOutputFile = new Label(parent, SWT.NONE);
		lblOutputFile.setText("Output Path");
		
		txtOutput = new Text(parent, SWT.BORDER);
		GridData gd_txtOutput = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtOutput.widthHint = 335;
		txtOutput.setLayoutData(gd_txtOutput);
		
		Button button_1 = new Button(parent, SWT.NONE);
		button_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog od = new DirectoryDialog(shell);
				od.open();
				String oDirectory = od.getFilterPath();
				txtOutput.setText(oDirectory);
				outputDirectory = oDirectory;
			}
		});
		button_1.setText("...");
		
		Label lblSuffix = new Label(parent, SWT.NONE);
		lblSuffix.setText("Output File Suffix");
		
		txtSuffix = new Text(parent, SWT.BORDER);
		txtSuffix.setText("_preprocessed");
		GridData gd_txtSuffix = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtSuffix.widthHint = 335;
		txtSuffix.setLayoutData(gd_txtSuffix);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Group grpPreprocessing = new Group(parent, SWT.NONE);
		GridData gd_grpPreprocessing = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
		gd_grpPreprocessing.heightHint = 118;
		gd_grpPreprocessing.widthHint = 429;
		grpPreprocessing.setLayoutData(gd_grpPreprocessing);
		grpPreprocessing.setText("Preprocessing");
		
		Label lblNewLabel = new Label(grpPreprocessing, SWT.NONE);
		lblNewLabel.setBounds(10, 32, 55, 15);
		lblNewLabel.setText("Delimiters");
		
		txtDelimiters = new Text(grpPreprocessing, SWT.BORDER);
		txtDelimiters.setText(" .,;'\\\"!-()[]{}:?");
		txtDelimiters.setBounds(176, 26, 219, 21);
		
		final Button btnStemming = new Button(grpPreprocessing, SWT.CHECK);
		btnStemming.setText("Stemming");
		btnStemming.setBounds(176, 65, 93, 16);
		
		final Button btnDoLowercase = new Button(grpPreprocessing, SWT.CHECK);
		btnDoLowercase.setBounds(10, 65, 143, 16);
		btnDoLowercase.setText("Convert to Lowercase");
		
		Button btnPreprocess = new Button(grpPreprocessing, SWT.NONE);
		btnPreprocess.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				Preprocess pp = new Preprocess(inputFiles, stopwordsFile, outputDirectory, txtSuffix.getText(), txtDelimiters.getText(), btnDoLowercase.getSelection(), btnStemming.getSelection());
				
				// Injecting the context into WordCount object so that the appendLog function can modify the Context Parameter consoleMessage
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(pp,iEclipseContext);
				
				try{
					long startTime = System.currentTimeMillis();
					int result = pp.doPreprocess();
					if (result == -1 || result == -2)
						appendLog("Preprocessing failed.");
					else
						appendLog("Preprocessing completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
				} catch (IOException ioe){
					ioe.printStackTrace();
				}
			}
		});
		btnPreprocess.setBounds(10, 101, 75, 25);
		btnPreprocess.setText("PreProcess");
		//TODO Your code here
	}
	
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