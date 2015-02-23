/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */ 
package edu.usc.cssl.nlputils.plugins.nbclassifier.parts;

import java.io.File;
import java.io.FileNotFoundException;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import bsh.EvalError;
import edu.usc.cssl.nlputils.application.handlers.GlobalPresserSettings;
import edu.usc.cssl.nlputils.plugins.nbclassifier.process.NBClassifier;
import edu.usc.cssl.nlputils.plugins.preprocessorService.services.PreprocessorService;
import edu.usc.cssl.nlputils.utilities.Log;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.custom.CTabItem;
import org.osgi.framework.FrameworkUtil;

public class NBClassifierSettings {
	private Text txtFolderPath1;
	private Text txtFolderPath2;
	private Text txtTestPath1;
	private Text txtCInput;
	private Text txtCOutput;
	private Text txtTestPath2;
	private Text txtOutputPath;
	private PreprocessorService ppService = null;
	
	@Inject
	public NBClassifierSettings() {
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		parent.setLayout(new GridLayout(1, false));
		Label header = new Label(parent, SWT.NONE);
		header.setImage(ImageDescriptor.createFromURL(
				FileLocator.find(FrameworkUtil.getBundle(this.getClass()),
						new Path("plugin_icon/icon.png"), null)).createImage());

		Group grpTraining = new Group(parent, SWT.NONE);
		GridData gd_grpTraining = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_grpTraining.heightHint = 111;
		gd_grpTraining.widthHint = 505;
		grpTraining.setLayoutData(gd_grpTraining);
		grpTraining.setText("Training");
		
		Label lblDataSet = new Label(grpTraining, SWT.NONE);
		lblDataSet.setBounds(10, 26, 95, 20);
		lblDataSet.setText("Class 1 Path");
		
		txtFolderPath1 = new Text(grpTraining, SWT.BORDER);
		txtFolderPath1.setBounds(142, 26, 332, 21);
		
		Button button = new Button(grpTraining, SWT.NONE);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog fd1 = new DirectoryDialog(shell);
				fd1.open();
				String fp1Directory = fd1.getFilterPath();
				txtFolderPath1.setText(fp1Directory);
				
			}
		});
		button.setBounds(474, 26, 40, 25);
		button.setText("...");
		
		Label lblModelFilePath = new Label(grpTraining, SWT.NONE);
		lblModelFilePath.setBounds(10, 62, 95, 20);
		lblModelFilePath.setText("Class 2 Path");
		
		txtFolderPath2 = new Text(grpTraining, SWT.BORDER);
		txtFolderPath2.setBounds(142, 62, 332, 21);
		
		Button button_1 = new Button(grpTraining, SWT.NONE);
		button_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog fd1 = new DirectoryDialog(shell);
				fd1.open();
				String fp1Directory = fd1.getFilterPath();
				txtFolderPath2.setText(fp1Directory);
				
			}
		});
		button_1.setBounds(474, 62, 40, 25);
		button_1.setText("...");
		
		final Button btnPreprocess = new Button(grpTraining, SWT.CHECK);
		btnPreprocess.setBounds(10, 93, 94, 18);
		btnPreprocess.setText("Preprocess");
		
		CTabFolder tabFolder = new CTabFolder(parent, SWT.BORDER);
		tabFolder.marginHeight = 0;
		tabFolder.marginWidth = 0;
		tabFolder.setTabHeight(35);
		GridData gd_tabFolder = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_tabFolder.widthHint = 512;
		tabFolder.setLayoutData(gd_tabFolder);
		tabFolder.setSimple(false);
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		CTabItem tbtmTest = new CTabItem(tabFolder, SWT.NONE);
		tbtmTest.setText("Test");
		
		Group grpTesting = new Group(tabFolder, SWT.NONE);
		tbtmTest.setControl(grpTesting);
		
		Label lblNewLabel = new Label(grpTesting, SWT.NONE);
		lblNewLabel.setBounds(10, 28, 105, 20);
		lblNewLabel.setText("Class 1 Test Path");
		
		txtTestPath1 = new Text(grpTesting, SWT.BORDER);
		txtTestPath1.setBounds(134, 28, 337, 21);
		
		Button button_2 = new Button(grpTesting, SWT.NONE);
		button_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog fd1 = new DirectoryDialog(shell);
				fd1.open();
				String fp1Directory = fd1.getFilterPath();
				txtTestPath1.setText(fp1Directory);
				
			}
		});
		button_2.setBounds(472, 28, 40, 25);
		button_2.setText("...");
		
		Button btnTest = new Button(grpTesting, SWT.NONE);
		btnTest.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				

				String ppDir1 = txtFolderPath1.getText();
				String ppDir2 = txtFolderPath2.getText();
				
				if (ppDir1.equals("") || ppDir2.equals("") || txtOutputPath.getText().equals("")){
					showError(shell);
					return;
				}
				
				File iDir1 = new File(ppDir1);
				File[] iFiles1 = iDir1.listFiles();
				for (File f : iFiles1){
					if (f.getAbsolutePath().contains("DS_Store"))
						f.delete();
				}
				
				File iDir2 = new File(ppDir2);
				File[] iFiles2 = iDir2.listFiles();
				for (File f : iFiles2){
					if (f.getAbsolutePath().contains("DS_Store"))
						f.delete();
				}
				
				if (btnPreprocess.getSelection()){
					ppService = GlobalPresserSettings.ppService;
					if (ppService.options == null){
						System.out.println("Preprocessing Options not set.");
						appendLog("Preprocessing Options not set. Please select the preprocessing options from the dialog box.");
						GlobalPresserSettings.ppService.setOptions(shell);
					}
					
					// Injecting the context into Preprocessor object so that the appendLog function can modify the Context Parameter consoleMessage
					IEclipseContext iEclipseContext = context;
					ContextInjectionFactory.inject(ppService,iEclipseContext);
					//Preprocessing
					appendLog("Preprocessing...");
					System.out.println("Preprocessing...");
					try {
						ppDir1 = ppService.doPreprocessing(ppDir1);
						ppDir2 = ppService.doPreprocessing(ppDir2);
						File dir1 = new File(ppDir1);
						File dest1 = new File(txtFolderPath1.getText()+"/"+"Class1_pre");
						dir1.renameTo(dest1);
						ppDir1 = dest1.getAbsolutePath();
						File dir2 = new File(ppDir2);
						File dest2 = new File(txtFolderPath2.getText()+"/"+"Class2_pre");
						dir2.renameTo(dest2);
						ppDir2 = dest2.getAbsolutePath();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
				}
				/* Mallet is not compatible with the Preprocessing plugin
				if(ppService.doPP) {
					
					// Injecting the context into Preprocessor object so that the appendLog function can modify the Context Parameter consoleMessage
					IEclipseContext iEclipseContext = context;
					ContextInjectionFactory.inject(ppService,iEclipseContext);

				//Preprocessing
				appendLog("Preprocessing...");
				System.out.println("Preprocessing...");
				try {
					ppDir1 = doPp(txtFolderPath1.getText());
					ppDir2 = doPp(txtFolderPath2.getText());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				}
				*/
				
				final NBClassifier nb = new NBClassifier();
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(nb,iEclipseContext);
				System.out.println("Processing...");
				appendLog("PROCESSING...(Naive Bayes)");
				
				final String fPpDir1 = ppDir1;
				final String fPpDir2 = ppDir2;
				final String testPath1 = txtTestPath1.getText();
				final String testPath2 = txtTestPath2.getText();
				final String outputPath = txtOutputPath.getText();
				
				// Creating a new Job to do NB so that the UI will not freeze
				Job job = new Job("NB Job"){
					protected IStatus run(IProgressMonitor monitor){ 
						

				try {

					long startTime = System.currentTimeMillis();
					//nb.doClassification(ppDir1, ppDir2, txtTestPath1.getText(), txtTestPath2.getText(), txtOutputPath.getText(), btnStopWords.getSelection(), btnDoLowercase.getSelection());
					nb.doClassification(fPpDir1, fPpDir2, testPath1, testPath2, outputPath, false,false);
					System.out.println("Naive Bayes Classification completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
					appendLog("Naive Bayes Classification completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
					
					Display.getDefault().asyncExec(new Runnable() {
					      @Override
					      public void run() {
					if (btnPreprocess.getSelection() && ppService.doCleanUp()){
						ppService.clean(fPpDir1);
						System.out.println("Cleaning up preprocessed files - "+fPpDir1);
						appendLog("Cleaning up preprocessed files - "+fPpDir1);
						ppService.clean(fPpDir2);
						System.out.println("Cleaning up preprocessed files - "+fPpDir2);
						appendLog("Cleaning up preprocessed files - "+fPpDir2);
					}
					appendLog("DONE (Naive Bayes)");
					      }
					});
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (EvalError e1) {
					e1.printStackTrace();
				}
				return Status.OK_STATUS;
					}
				};
				job.setPriority(Job.LONG);
				job.setUser(true);
				job.schedule();
			}
		});
		btnTest.setBounds(10, 141, 75, 25);
		btnTest.setText("Test");
		
		Label lblClassTest = new Label(grpTesting, SWT.NONE);
		lblClassTest.setText("Class 2 Test Path");
		lblClassTest.setBounds(10, 63, 105, 20);
		
		txtTestPath2 = new Text(grpTesting, SWT.BORDER);
		txtTestPath2.setBounds(134, 63, 337, 21);
		
		Button button_6 = new Button(grpTesting, SWT.NONE);
		button_6.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog fd1 = new DirectoryDialog(shell);
				fd1.open();
				String fp1Directory = fd1.getFilterPath();
				txtTestPath2.setText(fp1Directory);
				
			}
		});
		button_6.setText("...");
		button_6.setBounds(472, 63, 40, 25);
		
		Label lblOutputPath = new Label(grpTesting, SWT.NONE);
		lblOutputPath.setText("Output Path");
		lblOutputPath.setBounds(10, 101, 98, 20);
		
		txtOutputPath = new Text(grpTesting, SWT.BORDER);
		txtOutputPath.setBounds(134, 95, 337, 21);
		
		Button button_7 = new Button(grpTesting, SWT.NONE);
		button_7.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog fd1 = new DirectoryDialog(shell);
				fd1.open();
				String fp1Directory = fd1.getFilterPath();
				txtOutputPath.setText(fp1Directory);
				
			}
		});
		button_7.setText("...");
		button_7.setBounds(472, 95, 40, 25);
		
		CTabItem tbtmClassify = new CTabItem(tabFolder, SWT.NONE);
		tbtmClassify.setText("Classify");
		
		Group grpClassify = new Group(tabFolder, SWT.NONE);
		tbtmClassify.setControl(grpClassify);
		
		Label lblInputFile = new Label(grpClassify, SWT.NONE);
		lblInputFile.setBounds(10, 29, 65, 20);
		lblInputFile.setText("Input Path");
		
		txtCInput = new Text(grpClassify, SWT.BORDER);
		txtCInput.setEnabled(true);
		txtCInput.setBounds(135, 29, 336, 21);
		
		Button button_3 = new Button(grpClassify, SWT.NONE);
		button_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog fd1 = new DirectoryDialog(shell);
				fd1.open();
				String fp1Directory = fd1.getFilterPath();
				txtCInput.setText(fp1Directory);
				
			}
		});
		button_3.setEnabled(true);
		button_3.setBounds(471, 29, 40, 25);
		button_3.setText("...");
		
		Label lblOutputFile_1 = new Label(grpClassify, SWT.NONE);
		lblOutputFile_1.setBounds(10, 69, 78, 20);
		lblOutputFile_1.setText("Output Path");
		
		txtCOutput = new Text(grpClassify, SWT.BORDER);
		txtCOutput.setEnabled(true);
		txtCOutput.setBounds(135, 69, 336, 21);
		
		Button button_5 = new Button(grpClassify, SWT.NONE);
		button_5.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog fd1 = new DirectoryDialog(shell);
				fd1.open();
				String fp1Directory = fd1.getFilterPath();
				txtCOutput.setText(fp1Directory);
				
			}
		});
		button_5.setEnabled(true);
		button_5.setBounds(471, 69, 40, 25);
		button_5.setText("...");
		
		Button btnClassify = new Button(grpClassify, SWT.NONE);
		btnClassify.setEnabled(true);
		btnClassify.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
						
				String ppDir1 = txtFolderPath1.getText();
				String ppDir2 = txtFolderPath2.getText();
				
				if (ppDir1.equals("") || ppDir2.equals("") || txtCOutput.getText().equals("")){
					showError(shell);
					return;
				}
				

				File iDir1 = new File(ppDir1);
				File[] iFiles1 = iDir1.listFiles();
				for (File f : iFiles1){
					if (f.getAbsolutePath().contains("DS_Store"))
						f.delete();
				}
				
				File iDir2 = new File(ppDir2);
				File[] iFiles2 = iDir2.listFiles();
				for (File f : iFiles2){
					if (f.getAbsolutePath().contains("DS_Store"))
						f.delete();
				}
				
				
				/* Mallet is not compatible with the Preprocessing plugin
				if(ppService.doPP) {
					
					// Injecting the context into Preprocessor object so that the appendLog function can modify the Context Parameter consoleMessage
					IEclipseContext iEclipseContext = context;
					ContextInjectionFactory.inject(ppService,iEclipseContext);

				//Preprocessing
				appendLog("Preprocessing...");
				System.out.println("Preprocessing...");
				try {
					ppDir1 = doPp(txtFolderPath1.getText());
					ppDir2 = doPp(txtFolderPath2.getText());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				}
				*/
				
				final NBClassifier nb = new NBClassifier();
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(nb,iEclipseContext);
				System.out.println("Processing...");
				appendLog("PROCESSING...(Naive Bayes)");
				
				final String fPpDir1 = ppDir1;
				final String fPpDir2 = ppDir2;
				final String cInput = txtCInput.getText();
				final String cOutput = txtCOutput.getText();
				final String outputPath = txtOutputPath.getText();
				
				// Creating a new Job to do NB so that the UI will not freeze
				Job job = new Job("NB Job"){
					protected IStatus run(IProgressMonitor monitor){

					try {
						long startTime = System.currentTimeMillis();
						//nb.doValidation(ppDir1, ppDir2, txtCInput.getText(), txtCOutput.getText(), btnStopWords.getSelection(), btnDoLowercase.getSelection());
						nb.doValidation(fPpDir1, fPpDir2, cInput, cOutput, false, false);
						System.out.println("Naive Bayes Classification completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
						appendLog("Naive Bayes Classification completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
						appendLog("DONE");
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (EvalError e1) {
						e1.printStackTrace();
					}
					
					return Status.OK_STATUS;
					}
				};
				job.setPriority(Job.LONG);
				job.setUser(true);
				job.schedule();
			}
		});
		btnClassify.setBounds(10, 141, 75, 25);
		btnClassify.setText("Classify");
		shell.setDefaultButton(btnTest);
		
		tabFolder.setSelection(0);
	}
	
	/*
	private String doPp(String inputPath) throws IOException{
		return ppService.doPreprocessing(inputPath);
	}
	 */
	
	
	@Inject IEclipseContext context;
	private void appendLog(String message){
		Log.append(context,message);
	}
	
	private void showError(Shell shell){
		MessageBox message = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		message.setMessage("Please select input and output paths");
		message.setText("Error");
		message.open();
	}
}