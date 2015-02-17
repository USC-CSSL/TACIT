/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */ 
package edu.usc.cssl.nlputils.plugins.preprocessor.parts;

import java.io.IOException;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.resource.ImageDescriptor;
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

import edu.usc.cssl.nlputils.plugins.preprocessor.process.Preprocess;
import edu.usc.cssl.nlputils.utilities.Log;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.osgi.framework.FrameworkUtil;

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
		Label header = new Label(parent, SWT.NONE);
		header.setImage(ImageDescriptor.createFromURL(
				FileLocator.find(FrameworkUtil.getBundle(this.getClass()),
						new Path("plugin_icon/icon.png"), null)).createImage());
		for(int i=1; i<=5; i++){
			new Label(parent, SWT.NONE);
		}
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
					//txtInput.append(dir+"\\"+file+",");
					txtInput.append(dir+System.getProperty("file.separator")+file+",");
					//inputFiles[i++] = dir+"\\"+file;
					inputFiles[i++] = dir+System.getProperty("file.separator")+file;
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
				stopwordsFile = sfd.getFilterPath()+System.getProperty("file.separator")+sfd.getFileName();
				if (stopwordsFile.length()<2){
					stopwordsFile = "";
				} else{
					System.out.println(stopwordsFile);
				}
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
		lblNewLabel.setBounds(10, 26, 90, 20);
		lblNewLabel.setText("Delimiters");
		
		txtDelimiters = new Text(grpPreprocessing, SWT.BORDER);
		txtDelimiters.setText(" .,;'\\\"!-()[]{}:?");
		txtDelimiters.setBounds(176, 26, 249, 21);
		
		final Combo cmbStemLang = new Combo(grpPreprocessing, SWT.NONE);
		cmbStemLang.setEnabled(false);
		cmbStemLang.setItems(new String[] {"Auto Detect Language", "EN", "DE", "FR", "IT", "DA", "NL", "FI", "HU", "NO", "TR"});
		cmbStemLang.setBounds(275, 63, 150, 23);
		cmbStemLang.setText("Auto Detect Language");
		
		
		final Button btnStemming = new Button(grpPreprocessing, SWT.CHECK);
		btnStemming.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnStemming.getSelection())
					cmbStemLang.setEnabled(true);
				else
					cmbStemLang.setEnabled(false);
			}
		});
		btnStemming.setText("Stemming");
		btnStemming.setBounds(176, 65, 93, 16);
		
		final Button btnDoLowercase = new Button(grpPreprocessing, SWT.CHECK);
		btnDoLowercase.setBounds(10, 65, 143, 16);
		btnDoLowercase.setText("Convert to Lowercase");
		
		
		Button btnPreprocess = new Button(grpPreprocessing, SWT.NONE);
		btnPreprocess.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				final Preprocess pp = new Preprocess(inputFiles, txtStopWords.getText(), txtOutput.getText(), txtSuffix.getText(), txtDelimiters.getText(), btnDoLowercase.getSelection(), btnStemming.getSelection(),cmbStemLang.getText());
				
				// Injecting the context into Preprocessor object so that the appendLog function can modify the Context Parameter consoleMessage
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(pp,iEclipseContext);
				
				// Creating a new Job to do Preprocessing so that the UI will not freeze
				Job job = new Job("PP Job"){
					protected IStatus run(IProgressMonitor monitor){ 
				
					try{
						long startTime = System.currentTimeMillis();
						appendLog("PREPROCESSING...");
						int result = pp.doPreprocess();
						if (result == -1 || result == -2 || result == -3)
							appendLog("Preprocessing failed.");
						else
							appendLog("Preprocessing completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
						appendLog("DONE (Preprocessing)");
					} catch (Exception ioe){
						ioe.printStackTrace();
					}
				
					return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				job.schedule();
			}
		});
		btnPreprocess.setBounds(10, 101, 100, 25);
		btnPreprocess.setText("PreProcess");
		shell.setDefaultButton(btnPreprocess);
	}
	
	private void appendLog(String message){
		Log.append(context,message);
	}
}