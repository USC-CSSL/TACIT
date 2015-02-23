
package edu.usc.cssl.nlputils.plugins.WordCountPlugin.parts;

import java.io.File;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.FrameworkUtil;

import edu.usc.cssl.nlputils.plugins.WordCountPlugin.process.WordCountPlugin;
import edu.usc.cssl.nlputils.utilities.Log;


public class WordCountPluginSettings {
	
	@Inject
	IEclipseContext context;
	private String[] inputFiles;
	private Text txtInputFile;
	private Text txtDictionary;
	private Text txtStopWords;
	private Text txtOutputFile;
	private Label lblInput;
	
	@Inject
	public WordCountPluginSettings() {
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		parent.setLayout(new GridLayout(9, false));
		Label header = new Label(parent, SWT.NONE);
		header.setImage(ImageDescriptor.createFromURL(
				FileLocator.find(FrameworkUtil.getBundle(this.getClass()),
						new Path("plugin_icon/icon.png"), null)).createImage());
		for(int i=1; i<=17; i++){
			new Label(parent, SWT.NONE);
		}
		
		Label lblInputType = new Label(parent, SWT.NONE);
		lblInputType.setText("Input Type");
		new Label(parent, SWT.NONE);
		
		final Button btnFiles = new Button(parent, SWT.RADIO);
		btnFiles.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				lblInput.setText("Input File(s)");
			}
		});
		btnFiles.setSelection(true);
		btnFiles.setText("Files");
		new Label(parent, SWT.NONE);
		
		Button btnFolder = new Button(parent, SWT.RADIO);
		btnFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				lblInput.setText("Input Folder");
			}
		});
		btnFolder.setText("Folder");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		lblInput = new Label(parent, SWT.NONE);
		GridData gd_lblInput = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblInput.widthHint = 75;
		lblInput.setLayoutData(gd_lblInput);
		lblInput.setText("Input File(s)");
		new Label(parent, SWT.NONE);
		
		txtInputFile = new Text(parent, SWT.BORDER);
		GridData gd_txtInputFile = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		gd_txtInputFile.widthHint = 405;
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
				if (btnFiles.getSelection()) {
					FileDialog ifDialog = new FileDialog(shell, SWT.MULTI);
					ifDialog.open();
					
					// Since the FileDialog will not return the absolute file paths, add directory path to each file name
					inputFiles = new String[ifDialog.getFileNames().length];
					for (int i=0; i<ifDialog.getFileNames().length; i++){
						inputFiles[i] = ifDialog.getFilterPath()+System.getProperty("file.separator")+ifDialog.getFileNames()[i];
					}
					// Display the concatenated paths in the text field.
					StringBuilder sb = new StringBuilder();
					String selectedFiles[] = ifDialog.getFileNames();
					if (selectedFiles.length<=10)
						for (String inputFile : selectedFiles)
							sb.append(ifDialog.getFilterPath()+System.getProperty("file.separator")+inputFile+", ");
					else
						sb.append(selectedFiles.length +" files selected");
					txtInputFile.setText(sb.toString());
				} else {
					DirectoryDialog fd1 = new DirectoryDialog(shell);
					fd1.open();
					String fp1Directory = fd1.getFilterPath();
					if (fp1Directory.length()<3)
						return;
					txtInputFile.setText(fp1Directory);
					File dir = new File(fp1Directory);
					ArrayList<String> paths = getFiles(fp1Directory);
					int i = 0;
					inputFiles = new String[paths.size()];
					for (String path:paths)
						inputFiles[i++] = path;
				}
			}
		});
		btnInputFile.setText("...");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Label lblDictionary = new Label(parent, SWT.NONE);
		lblDictionary.setText("Dictionary");
		new Label(parent, SWT.NONE);
		
		txtDictionary = new Text(parent, SWT.BORDER);
		GridData gd_txtDictionary = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		gd_txtDictionary.widthHint = 405;
		txtDictionary.setLayoutData(gd_txtDictionary);
		
		Button btnDictionary = new Button(parent, SWT.NONE);
		btnDictionary.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				FileDialog dfDialog = new FileDialog(shell, SWT.MULTI);
				dfDialog.open();
				txtDictionary.setText(dfDialog.getFilterPath()+System.getProperty("file.separator")+dfDialog.getFileName());
			}
		});
		btnDictionary.setText("...");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Label lblStopWords = new Label(parent, SWT.NONE);
		lblStopWords.setText("Stop Words");
		new Label(parent, SWT.NONE);
		
		txtStopWords = new Text(parent, SWT.BORDER);
		GridData gd_txtStopWords = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		gd_txtStopWords.widthHint = 405;
		txtStopWords.setLayoutData(gd_txtStopWords);
		
		Button btnStopWords = new Button(parent, SWT.NONE);
		btnStopWords.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				FileDialog sfDialog = new FileDialog(shell, SWT.MULTI);
				sfDialog.open();
				txtStopWords.setText(sfDialog.getFilterPath()+System.getProperty("file.separator")+sfDialog.getFileName());
				if (txtStopWords.getText().length() <3)
					txtStopWords.setText("");
			}
		});
		btnStopWords.setText("...");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Label lblOutputFile = new Label(parent, SWT.NONE);
		lblOutputFile.setText("Output File");
		new Label(parent, SWT.NONE);
		
		txtOutputFile = new Text(parent, SWT.BORDER);
		GridData gd_txtOutputFile = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		gd_txtOutputFile.widthHint = 405;
		txtOutputFile.setLayoutData(gd_txtOutputFile);
		
		Button btnOutputFile = new Button(parent, SWT.NONE);
		btnOutputFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				FileDialog ofDialog = new FileDialog(shell, SWT.SAVE);
				String[] ext = new String[1];
				ext[0] = "*.csv";
				ofDialog.setFilterExtensions(ext);
				ofDialog.open();
				txtOutputFile.setText(ofDialog.getFilterPath()+System.getProperty("file.separator")+ofDialog.getFileName());
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
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		final Button btnStem = new Button(parent, SWT.CHECK);
		btnStem.setText("Stem");
//		new Label(parent, SWT.NONE);
//		
//
//		new Label(parent, SWT.NONE);
//		new Label(parent, SWT.NONE);
//		new Label(parent, SWT.NONE);
//		new Label(parent, SWT.NONE);
//		new Label(parent, SWT.NONE);
//		new Label(parent, SWT.NONE);
//		new Label(parent, SWT.NONE);
//		new Label(parent, SWT.NONE);
//		new Label(parent, SWT.NONE);
//		new Label(parent, SWT.NONE);
//		new Label(parent, SWT.NONE);
//		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
//		
		
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
				WordCountPlugin wc = new WordCountPlugin();
				
				// Injecting the context into WordCount object so that the appendLog function can modify the Context Parameter consoleMessage
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(wc,iEclipseContext);
				
				MessageBox overwriteMsg = new MessageBox(shell,SWT.YES|SWT.NO);
				overwriteMsg.setMessage("The output file already exists. Do you want to overwrite?");
				overwriteMsg.setText("Warning");
				int overwrite = 0;
				
				String oPath = txtOutputFile.getText().substring(0, txtOutputFile.getText().length()-4);
				
				File oFile = new File(oPath+".csv");
				File sFile = new File(oPath+".dat");
				
				if (oFile.exists() || sFile.exists())
					overwrite = overwriteMsg.open();
				
				if (overwrite == SWT.NO){
					System.out.println("Do not overwrite. Return to GUI.");
					return;
				}
				System.out.println("Overwrite files.");
				
				
				try {
						//Niki Change here
					returnCode=wc.invokeWordCount(inputFiles, txtDictionary.getText(), txtStopWords.getText(), oPath, btnStem.getSelection());
				} catch (Exception ioe) {
					ioe.printStackTrace();
				}
				
				if (returnCode == -2)
					errorMessage = "Please check the input file path.";
				if (returnCode == -3) 
					errorMessage = "Please check the dictionary file path.";
				if (returnCode == -4) 
					errorMessage = "Please check the stop words file path.";
				if (returnCode == -5) 
					errorMessage = "The output file path is incorrect.";
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
		shell.setDefaultButton(btnAnalyze);
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
	
	protected ArrayList<String> getFiles(String fp1Directory) {
		File dir = new File(fp1Directory);
		String[] dirFiles = dir.list();
		ArrayList<String> paths = new ArrayList<String>();
		for (String s : dirFiles){
			String path = fp1Directory+System.getProperty("file.separator")+s;
			File temp = new File(path);
			if (temp.exists() && !temp.isDirectory()){
				paths.add(path);
				//System.out.println(path);
			} else if (temp.exists() && temp.isDirectory()) {
				String directory = temp.getAbsolutePath();
				paths.addAll(getFiles(directory));
			}
		}
		return paths;
	}

	private void appendLog(String message){
		Log.append(context,message);
	}
	


}