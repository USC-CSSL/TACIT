/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */ 
package edu.usc.cssl.nlputils.plugins.svmClassifier.parts;

import java.io.IOException;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import edu.usc.cssl.nlputils.application.handlers.GlobalPresserSettings;
import edu.usc.cssl.nlputils.plugins.svmClassifier.process.CrossValidator;
import edu.usc.cssl.nlputils.plugins.svmClassifier.process.SvmClassifier;
import edu.usc.cssl.nlputils.plugins.preprocessorService.services.PreprocessorService;

import org.eclipse.swt.widgets.Group;
import org.osgi.framework.FrameworkUtil;

public class SvmClassifierSettings {
	private Text txtLabel1;
	private Text txtFolderPath2;
	private Text txtFolderPath1;
	private Text txtLabel2;
	private PreprocessorService ppService = null;
	
	@Inject IEclipseContext context;
	private Text txtOutputFile;
	private Text txtkVal;
	public SvmClassifierSettings() {
		//TODO Your code here
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		parent.setLayout(new GridLayout(7, false));
		Label header = new Label(parent, SWT.NONE);
		header.setImage(ImageDescriptor.createFromURL(
				FileLocator.find(FrameworkUtil.getBundle(this.getClass()),
						new Path("plugin_icon/icon.png"), null)).createImage());
		Group grpInputSettings = new Group(parent, SWT.NONE);
		grpInputSettings.setText("Training Settings");
		GridData gd_grpInputSettings = new GridData(SWT.LEFT, SWT.CENTER, false, false, 7, 1);
		gd_grpInputSettings.heightHint = 259;
		gd_grpInputSettings.widthHint = 529;
		grpInputSettings.setLayoutData(gd_grpInputSettings);
		
		Composite composite = new Composite(grpInputSettings, SWT.NONE);
		composite.setBounds(10, 20, 515, 95);
		
		Label lblLabel_1 = new Label(composite, SWT.NONE);
		lblLabel_1.setBounds(0, 35, 45, 20);
		lblLabel_1.setText("Class 2");
		
		Label lblLabel = new Label(composite, SWT.NONE);
		lblLabel.setBounds(0, 5, 45, 20);
		lblLabel.setText("Class 1");
		
		txtLabel1 = new Text(composite, SWT.BORDER);
		txtLabel1.setText("Label1");
		txtLabel1.setBounds(66, 5, 157, 21);
		
		txtLabel2 = new Text(composite, SWT.BORDER);
		txtLabel2.setText("Label2");
		txtLabel2.setBounds(66, 35, 157, 21);
		
		Label lblFolder = new Label(composite, SWT.NONE);
		lblFolder.setBounds(237, 5, 30, 20);
		lblFolder.setText("Path");
		
		Label lblFolder_1 = new Label(composite, SWT.NONE);
		lblFolder_1.setBounds(237, 35, 30, 20);
		lblFolder_1.setText("Path");
		
		txtFolderPath1 = new Text(composite, SWT.BORDER);
		txtFolderPath1.setBounds(279, 3, 190, 21);
		
		txtFolderPath2 = new Text(composite, SWT.BORDER);
		txtFolderPath2.setBounds(279, 33, 190, 21);
		
		Button button = new Button(composite, SWT.NONE);
		button.setBounds(474, 2, 40, 25);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog fd1 = new DirectoryDialog(shell);
				fd1.open();
				String fp1Directory = fd1.getFilterPath();
				txtFolderPath1.setText(fp1Directory);
				
			}
		});
		button.setText("...");
		
		Button button_1 = new Button(composite, SWT.NONE);
		button_1.setBounds(474, 32, 40, 25);
		button_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog fd2 = new DirectoryDialog(shell);
				fd2.open();
				String fp2Directory = fd2.getFilterPath();
				txtFolderPath2.setText(fp2Directory);
			
			}
		});
		button_1.setText("...");
		
		final Button btnPreprocess = new Button(composite, SWT.CHECK);
		btnPreprocess.setBounds(0, 65, 100, 25);
		btnPreprocess.setText("Preprocess");
		
		final Button btnWeights = new Button(grpInputSettings, SWT.CHECK);
		btnWeights.setSelection(true);
		btnWeights.setBounds(10, 132, 174, 16);
		btnWeights.setText("Create Feature Weights File");
		
		Label lblKfoldCrossValidation = new Label(grpInputSettings, SWT.NONE);
		lblKfoldCrossValidation.setBounds(13, 163, 180, 20);
		lblKfoldCrossValidation.setText("k Value for Cross Validation");
		
		txtkVal = new Text(grpInputSettings, SWT.BORDER);
		txtkVal.setBounds(190, 163, 46, 21);
		
		Label lblOutputPath = new Label(grpInputSettings, SWT.NONE);
		lblOutputPath.setBounds(13, 195, 80, 20);
		lblOutputPath.setText("Output Path");
		
		txtOutputFile = new Text(grpInputSettings, SWT.BORDER);
		txtOutputFile.setBounds(190, 192, 270, 21);
		
		
		Button btnTrain = new Button(grpInputSettings, SWT.NONE);
		btnTrain.setBounds(10, 223, 52, 25);
		btnTrain.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				long currentTime = System.currentTimeMillis();
				String ppDir1 = txtFolderPath1.getText();
				String ppDir2 = txtFolderPath2.getText();
				
				if (ppDir1.equals("") || ppDir2.equals("") || txtOutputFile.getText().equals("")){
					showError(shell);
					return;
				}
				
				if(btnPreprocess.getSelection()) {
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
					ppDir1 = ppService.doPreprocessing(txtFolderPath1.getText());
					ppDir2 = ppService.doPreprocessing(txtFolderPath2.getText());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				}
				
				try {
				//SvmClassifier svm = new SvmClassifier(btnLowercase.getSelection(), txtDelimiters.getText(), txtStopWords.getText());  Preprocessing done separately
				SvmClassifier svm = new SvmClassifier(txtLabel1.getText(), txtLabel2.getText(), txtOutputFile.getText());
				
				// Injecting the context into Preprocessor object so that the appendLog function can modify the Context Parameter consoleMessage
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(svm,iEclipseContext);

				//int selection = tabFolder.getSelectionIndex();
				appendLog("PROCESSING...(SVM)");
				if (true){	// btnCrossVal.getSelection() always true
					CrossValidator cv = new CrossValidator();
					ContextInjectionFactory.inject(cv,iEclipseContext);
					cv.doCross(svm, txtLabel1.getText(), ppDir1, txtLabel2.getText(), ppDir2, Integer.parseInt(txtkVal.getText()), btnWeights.getSelection());
				}
				/*else{
					if(btnLoadModel.getSelection())
						svm.loadPretrainedModel(txtLabel1.getText(), txtLabel2.getText(), txtModelFilePath.getText(), txtHashmapPath.getText());
					else
						svm.train(txtLabel1.getText(), ppDir1, txtLabel2.getText(), ppDir2, true, false, txtkVal.getText(), true, btnWeights.getSelection());  //btnLinear.getSelection() removed. made Linear Kernel default
					// Cross Validation => No need to call predict and output separately
					
					if(selection == 0){
						System.out.println("Test Mode");
						appendLog("Test Mode");
						svm.predict(txtLabel1.getText(), txtTestFolder1.getText(), txtLabel2.getText(), txtTestFolder2.getText());
						svm.output(txtLabel1.getText(), txtTestFolder1.getText(), txtLabel2.getText(), txtTestFolder2.getText());
					} else if (selection == 1){
						System.out.println("Classification Mode");
						appendLog("Classification Mode");
					}
					
				}*/
				System.out.println("Completed classification in "+((System.currentTimeMillis()-currentTime)/(double)1000)+" seconds.");
				appendLog("Completed classification in "+((System.currentTimeMillis()-currentTime)/(double)1000)+" seconds.");
				if (btnPreprocess.getSelection() && ppService.doCleanUp()){
					ppService.clean(ppDir1);
					System.out.println("Cleaning up preprocessed files - "+ppDir1);
					appendLog("Cleaning up preprocessed files - "+ppDir1);
					ppService.clean(ppDir2);
					System.out.println("Cleaning up preprocessed files - "+ppDir2);
					appendLog("Cleaning up preprocessed files - "+ppDir2);
				}
				appendLog("DONE (SVM)");
				} catch (IOException ie) {
					ie.printStackTrace();
				}
			}

		
		});
		btnTrain.setText("Test");
		shell.setDefaultButton(btnTrain);
		
		Button button_2 = new Button(grpInputSettings, SWT.NONE);
		button_2.setBounds(482, 191, 40, 25);
		button_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				txtOutputFile.setText("");
				DirectoryDialog fd1 = new DirectoryDialog(shell);
				fd1.open();
				String fp1Directory = fd1.getFilterPath();
				txtOutputFile.setText(fp1Directory);
			}
		});
		button_2.setText("...");
		//TODO Your code here
	}
	

	private void showError(Shell shell){
		MessageBox message = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		message.setMessage("Please select input and output paths");
		message.setText("Error");
		message.open();
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