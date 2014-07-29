 
package edu.usc.pil.nlputils.plugins.svmClassifier.parts;

import java.io.IOException;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import edu.usc.pil.nlputils.plugins.svmClassifier.process.SvmClassifier;

import org.eclipse.swt.widgets.Group;

public class SvmClassifierSettings {
	private Text txtLabel1;
	private Text txtFolderPath2;
	private Text txtFolderPath1;
	private Text txtLabel2;
	
	@Inject IEclipseContext context;
	private Text txtTestFolder1;
	private Text txtTestFolder2;
	private Text text_2;
	private Text txtOutputFile;
	private Text txtDelimiters;
	private Text text_1;
	private Text txtStopWords;
	public SvmClassifierSettings() {
		//TODO Your code here
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		parent.setLayout(new GridLayout(7, false));
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 7, 3);
		gd_composite.heightHint = 180;
		gd_composite.widthHint = 436;
		composite.setLayoutData(gd_composite);
		
		Label lblLabel_1 = new Label(composite, SWT.NONE);
		lblLabel_1.setBounds(0, 35, 36, 15);
		lblLabel_1.setText("Class 2");
		
		Label lblLabel = new Label(composite, SWT.NONE);
		lblLabel.setBounds(0, 5, 36, 15);
		lblLabel.setText("Class 1");
		
		txtLabel1 = new Text(composite, SWT.BORDER);
		txtLabel1.setText("Label1");
		txtLabel1.setBounds(66, 2, 157, 21);
		
		txtLabel2 = new Text(composite, SWT.BORDER);
		txtLabel2.setText("Label2");
		txtLabel2.setBounds(66, 32, 157, 21);
		
		Label lblFolder = new Label(composite, SWT.NONE);
		lblFolder.setBounds(228, 5, 24, 15);
		lblFolder.setText("Path");
		
		Label lblFolder_1 = new Label(composite, SWT.NONE);
		lblFolder_1.setBounds(228, 35, 24, 15);
		lblFolder_1.setText("Path");
		
		txtFolderPath1 = new Text(composite, SWT.BORDER);
		txtFolderPath1.setBounds(257, 2, 143, 21);
		
		txtFolderPath2 = new Text(composite, SWT.BORDER);
		txtFolderPath2.setBounds(257, 32, 143, 21);
		
		Button button = new Button(composite, SWT.NONE);
		button.setBounds(406, 0, 21, 25);
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
		button_1.setBounds(406, 30, 21, 25);
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
		
		Group grpPreprocessingOptions = new Group(composite, SWT.NONE);
		grpPreprocessingOptions.setBounds(0, 56, 426, 114);
		grpPreprocessingOptions.setText("Preprocessing Options");
		
		txtStopWords = new Text(grpPreprocessingOptions, SWT.BORDER);
		txtStopWords.setBounds(178, 54, 195, 21);
		
		final Button btnLowercase = new Button(grpPreprocessingOptions, SWT.CHECK);
		btnLowercase.setBounds(25, 90, 140, 16);
		btnLowercase.setText("Convert to Lowercase");
		
		Label lblDelimiters = new Label(grpPreprocessingOptions, SWT.NONE);
		lblDelimiters.setBounds(25, 25, 55, 15);
		lblDelimiters.setText("Delimiters");
		
		txtDelimiters = new Text(grpPreprocessingOptions, SWT.BORDER);
		txtDelimiters.setText(" .,;'\\\"!-()[]{}:?");
		txtDelimiters.setBounds(178, 22, 195, 21);
		
		Button button_5 = new Button(grpPreprocessingOptions, SWT.NONE);
		button_5.setBounds(379, 51, 21, 25);
		button_5.setText("...");
		
		Label lblStopWordsFile = new Label(grpPreprocessingOptions, SWT.NONE);
		lblStopWordsFile.setBounds(25, 59, 98, 15);
		lblStopWordsFile.setText("Stop Words File");
		
		final Button btnTest = new Button(parent, SWT.RADIO);
		btnTest.setSelection(true);
		btnTest.setText("Test");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Group grpTestMode = new Group(parent, SWT.NONE);
		GridData gd_grpTestMode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 7, 1);
		gd_grpTestMode.heightHint = 86;
		gd_grpTestMode.widthHint = 395;
		grpTestMode.setLayoutData(gd_grpTestMode);
		
		Label lblTestFolder = new Label(grpTestMode, SWT.NONE);
		lblTestFolder.setBounds(10, 23, 119, 15);
		lblTestFolder.setText("Test Folder for Class 1");
		
		txtTestFolder1 = new Text(grpTestMode, SWT.BORDER);
		txtTestFolder1.setBounds(135, 17, 220, 21);
		
		Label lblTestFolder_1 = new Label(grpTestMode, SWT.NONE);
		lblTestFolder_1.setBounds(10, 52, 119, 15);
		lblTestFolder_1.setText("Test Folder for Class 2");
		
		txtTestFolder2 = new Text(grpTestMode, SWT.BORDER);
		txtTestFolder2.setBounds(135, 46, 220, 21);
		
		Button button_3 = new Button(grpTestMode, SWT.NONE);
		button_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog fd1 = new DirectoryDialog(shell);
				fd1.open();
				String fp1Directory = fd1.getFilterPath();
				txtTestFolder1.setText(fp1Directory);
				
			}
		});
		button_3.setBounds(359, 17, 21, 25);
		button_3.setText("...");
		
		Button button_4 = new Button(grpTestMode, SWT.NONE);
		button_4.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog fd2 = new DirectoryDialog(shell);
				fd2.open();
				String fp2Directory = fd2.getFilterPath();
				txtTestFolder2.setText(fp2Directory);
			
			}
		});
		button_4.setBounds(359, 45, 21, 25);
		button_4.setText("...");
		
		text_1 = new Text(grpTestMode, SWT.BORDER);
		text_1.setBounds(135, 73, 220, 21);
		
		Label lblKfoldCrossValidation = new Label(grpTestMode, SWT.NONE);
		lblKfoldCrossValidation.setBounds(10, 79, 130, 15);
		lblKfoldCrossValidation.setText("k-Fold Cross Validation");
		
		Button btnClassify = new Button(parent, SWT.RADIO);
		btnClassify.setText("Classify");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Group grpClassifyMode = new Group(parent, SWT.NONE);
		GridData gd_grpClassifyMode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 7, 1);
		gd_grpClassifyMode.heightHint = 57;
		gd_grpClassifyMode.widthHint = 400;
		grpClassifyMode.setLayoutData(gd_grpClassifyMode);
		grpClassifyMode.setText("Classify");
		
		Label lblFolder_2 = new Label(grpClassifyMode, SWT.NONE);
		lblFolder_2.setBounds(10, 32, 97, 15);
		lblFolder_2.setText("Directory Path");
		
		text_2 = new Text(grpClassifyMode, SWT.BORDER);
		text_2.setBounds(135, 26, 234, 21);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Label lblOutputFile = new Label(parent, SWT.NONE);
		lblOutputFile.setText("Output File");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		txtOutputFile = new Text(parent, SWT.BORDER);
		txtOutputFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(parent, SWT.NONE);
		
		Button button_2 = new Button(parent, SWT.NONE);
		button_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				txtOutputFile.setText("");
				FileDialog fd = new FileDialog(shell,SWT.SAVE);
				fd.open();
				String oFile = fd.getFileName();
				String dir = fd.getFilterPath();
				txtOutputFile.setText(dir+"\\"+oFile);
			}
		});
		button_2.setText("...");
		
		Button btnTrain = new Button(parent, SWT.NONE);
		btnTrain.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				long currentTime = System.currentTimeMillis();
				
				try {
				SvmClassifier svm = new SvmClassifier(btnLowercase.getSelection(), txtDelimiters.getText(), txtStopWords.getText());
				// Injecting the context into Preprocessor object so that the appendLog function can modify the Context Parameter consoleMessage
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(svm,iEclipseContext);

				svm.train(txtLabel1.getText(), txtFolderPath1.getText(), txtLabel2.getText(), txtFolderPath2.getText());
				if (btnTest.getSelection())
				svm.predict(txtLabel1.getText(), txtFolderPath1.getText(), txtLabel2.getText(), txtFolderPath2.getText(),txtOutputFile.getText());
				System.out.println("Completed classification in "+((System.currentTimeMillis()-currentTime)/(double)1000)+" seconds.");
				appendLog("Completed classification in "+((System.currentTimeMillis()-currentTime)/(double)1000)+" seconds.");
				} catch (IOException ie) {
					ie.printStackTrace();
				}
			}
		});
		btnTrain.setText("Classify");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
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