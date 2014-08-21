 
package edu.usc.pil.nlputils.plugins.svmClassifier.parts;

import java.io.IOException;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
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

import edu.usc.pil.nlputils.plugins.preprocessorService.services.PreprocessorService;
import edu.usc.pil.nlputils.plugins.svmClassifier.process.SvmClassifier;

import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class SvmClassifierSettings {
	private Text txtLabel1;
	private Text txtFolderPath2;
	private Text txtFolderPath1;
	private Text txtLabel2;
	private PreprocessorService ppService = new PreprocessorService();
	
	@Inject IEclipseContext context;
	private Text txtTestFolder1;
	private Text txtTestFolder2;
	private Text txtClassifyInput;
	private Text txtOutputFile;
	private Text txtkVal;
	private Text txtClassifyOutput;
	private Text txtModelFilePath;
	private Text txtHashmapPath;
	public SvmClassifierSettings() {
		//TODO Your code here
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		parent.setLayout(new GridLayout(7, false));
		
		Group grpInputSettings = new Group(parent, SWT.NONE);
		grpInputSettings.setText("Training Settings");
		GridData gd_grpInputSettings = new GridData(SWT.LEFT, SWT.CENTER, false, false, 7, 1);
		gd_grpInputSettings.heightHint = 233;
		gd_grpInputSettings.widthHint = 529;
		grpInputSettings.setLayoutData(gd_grpInputSettings);
		
		Composite composite = new Composite(grpInputSettings, SWT.NONE);
		composite.setBounds(10, 20, 475, 95);
		
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
		button.setBounds(403, 0, 21, 25);
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
		button_1.setBounds(403, 30, 21, 25);
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
		
		Button btnPreprocess = new Button(composite, SWT.NONE);
		btnPreprocess.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				showPpOptions(shell);
			}

		});
		btnPreprocess.setBounds(0, 65, 75, 25);
		btnPreprocess.setText("Preprocess...");
		
		final Button btnLoadModel = new Button(grpInputSettings, SWT.CHECK);
		btnLoadModel.setBounds(18, 137, 168, 16);
		btnLoadModel.setText("Load Pretrained Model");
		
		txtModelFilePath = new Text(grpInputSettings, SWT.BORDER);
		txtModelFilePath.setEnabled(false);
		txtModelFilePath.setBounds(244, 132, 199, 21);
		
		final Button button_8 = new Button(grpInputSettings, SWT.NONE);
		button_8.setEnabled(false);
		button_8.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				txtModelFilePath.setText("");
				FileDialog fd = new FileDialog(shell,SWT.OPEN);
				fd.open();
				String oFile = fd.getFileName();
				String dir = fd.getFilterPath();
				txtModelFilePath.setText(dir+System.getProperty("file.separator")+oFile);
			}
		});
		button_8.setBounds(446, 130, 20, 25);
		button_8.setText("...");
		
		txtHashmapPath = new Text(grpInputSettings, SWT.BORDER);
		txtHashmapPath.setEnabled(false);
		txtHashmapPath.setBounds(244, 174, 199, 21);
		
		final Button button_9 = new Button(grpInputSettings, SWT.NONE);
		button_9.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				txtHashmapPath.setText("");
				FileDialog fd = new FileDialog(shell,SWT.OPEN);
				fd.open();
				String oFile = fd.getFileName();
				String dir = fd.getFilterPath();
				txtHashmapPath.setText(dir+System.getProperty("file.separator")+oFile);
			}
		});
		button_9.setEnabled(false);
		button_9.setBounds(446, 172, 20, 25);
		button_9.setText("...");
		
		final Label lblPretrainedHashMap = new Label(grpInputSettings, SWT.NONE);
		lblPretrainedHashMap.setEnabled(false);
		lblPretrainedHashMap.setBounds(34, 180, 128, 15);
		lblPretrainedHashMap.setText("Pretrained Hash Map");
		
		final Button btnWeights = new Button(grpInputSettings, SWT.CHECK);
		btnWeights.setSelection(true);
		btnWeights.setBounds(18, 216, 174, 16);
		btnWeights.setText("Create Feature Weights File");
		
		btnLoadModel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				button_8.setEnabled(true);
				txtModelFilePath.setEnabled(true);
				txtHashmapPath.setEnabled(true);
				button_9.setEnabled(true);
				lblPretrainedHashMap.setEnabled(true);
			}
		});
		
		
		final CTabFolder tabFolder = new CTabFolder(parent, SWT.BORDER | SWT.SINGLE);
		tabFolder.setSingle(false);
		tabFolder.setSimple(false);
		GridData gd_tabFolder = new GridData(SWT.LEFT, SWT.CENTER, false, false, 7, 1);
		gd_tabFolder.heightHint = 259;
		gd_tabFolder.widthHint = 534;
		tabFolder.setLayoutData(gd_tabFolder);
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		final CTabItem tbtmTesting = new CTabItem(tabFolder, SWT.NONE);
		tbtmTesting.setText("Test");
		
		Group grpTestMode = new Group(tabFolder, SWT.NONE);
		tbtmTesting.setControl(grpTestMode);
		
		Label lblTestFolder = new Label(grpTestMode, SWT.NONE);
		lblTestFolder.setBounds(34, 108, 95, 15);
		lblTestFolder.setText("Class 1 Test Data");
		
		txtTestFolder1 = new Text(grpTestMode, SWT.BORDER);
		txtTestFolder1.setBounds(135, 102, 220, 21);
		
		Label lblTestFolder_1 = new Label(grpTestMode, SWT.NONE);
		lblTestFolder_1.setBounds(34, 134, 95, 15);
		lblTestFolder_1.setText("Class 2 Test Data");
		
		txtTestFolder2 = new Text(grpTestMode, SWT.BORDER);
		txtTestFolder2.setBounds(135, 131, 220, 21);
		
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
		button_3.setBounds(359, 100, 21, 25);
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
		button_4.setBounds(359, 129, 21, 25);
		button_4.setText("...");
		
		Label lblKfoldCrossValidation = new Label(grpTestMode, SWT.NONE);
		lblKfoldCrossValidation.setBounds(34, 46, 46, 15);
		lblKfoldCrossValidation.setText("k Value");
		
		Label lblOutputPath = new Label(grpTestMode, SWT.NONE);
		lblOutputPath.setText("Output Path");
		lblOutputPath.setBounds(10, 175, 70, 15);
		
		txtkVal = new Text(grpTestMode, SWT.BORDER);
		txtkVal.setBounds(135, 43, 46, 21);
		
		txtOutputFile = new Text(grpTestMode, SWT.BORDER);
		txtOutputFile.setBounds(135, 172, 220, 21);
		
		Button button_2 = new Button(grpTestMode, SWT.NONE);
		button_2.setBounds(359, 170, 21, 25);
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
		
		final Button btnCrossVal = new Button(grpTestMode, SWT.RADIO);
		btnCrossVal.setBounds(10, 22, 149, 16);
		btnCrossVal.setText("k-Fold Cross Validation");
		
		
		Button btnTrain = new Button(grpTestMode, SWT.NONE);
		btnTrain.setBounds(10, 208, 52, 25);
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
				
				try {
				//SvmClassifier svm = new SvmClassifier(btnLowercase.getSelection(), txtDelimiters.getText(), txtStopWords.getText());  Preprocessing done separately
				SvmClassifier svm = new SvmClassifier(txtLabel1.getText(), txtLabel2.getText(), txtOutputFile.getText());
				
				// Injecting the context into Preprocessor object so that the appendLog function can modify the Context Parameter consoleMessage
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(svm,iEclipseContext);

				int selection = tabFolder.getSelectionIndex();
				if(btnLoadModel.getSelection())
					svm.loadPretrainedModel(txtLabel1.getText(), txtLabel2.getText(), txtModelFilePath.getText(), txtHashmapPath.getText());
				else
					svm.train(txtLabel1.getText(), ppDir1, txtLabel2.getText(), ppDir2, true, btnCrossVal.getSelection(), txtkVal.getText(), true, btnWeights.getSelection());  //btnLinear.getSelection() removed. made Linear Kernel default
				// Cross Validation => No need to call predict and output separately
				if (!btnCrossVal.getSelection()){
				if(selection == 0){
					System.out.println("Test Mode");
					appendLog("Test Mode");
					svm.predict(txtLabel1.getText(), txtTestFolder1.getText(), txtLabel2.getText(), txtTestFolder2.getText());
					svm.output(txtLabel1.getText(), txtTestFolder1.getText(), txtLabel2.getText(), txtTestFolder2.getText());
				} else if (selection == 1){
					System.out.println("Classification Mode");
					appendLog("Classification Mode");
				}
				}
				System.out.println("Completed classification in "+((System.currentTimeMillis()-currentTime)/(double)1000)+" seconds.");
				appendLog("Completed classification in "+((System.currentTimeMillis()-currentTime)/(double)1000)+" seconds.");
				} catch (IOException | ClassNotFoundException ie) {
					ie.printStackTrace();
				}
			}

		
		});
		btnTrain.setText("Test");
		
		Button btnSeparateTestData = new Button(grpTestMode, SWT.RADIO);
		btnSeparateTestData.setSelection(true);
		btnSeparateTestData.setBounds(10, 80, 119, 16);
		btnSeparateTestData.setText("Separate Test Data");
		
		
		//Setting Testing as the default tab
		tabFolder.setSelection(0);
		
		CTabItem tbtmClassify = new CTabItem(tabFolder, SWT.NONE);
		tbtmClassify.setText("Classify");
		
		Group grpClassifyMode = new Group(tabFolder, SWT.NONE);
		tbtmClassify.setControl(grpClassifyMode);
		grpClassifyMode.setText("Classify");
		
		Label lblFolder_2 = new Label(grpClassifyMode, SWT.NONE);
		lblFolder_2.setBounds(10, 32, 97, 15);
		lblFolder_2.setText("Directory Path");
		
		txtClassifyInput = new Text(grpClassifyMode, SWT.BORDER);
		txtClassifyInput.setBounds(135, 26, 234, 21);
		
		Label lblOutputPath_1 = new Label(grpClassifyMode, SWT.NONE);
		lblOutputPath_1.setText("Output Path");
		lblOutputPath_1.setBounds(10, 66, 59, 15);
		
		txtClassifyOutput = new Text(grpClassifyMode, SWT.BORDER);
		txtClassifyOutput.setBounds(135, 63, 234, 21);
		
		Button button_6 = new Button(grpClassifyMode, SWT.NONE);
		button_6.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				txtClassifyOutput.setText("");
				DirectoryDialog fd1 = new DirectoryDialog(shell);
				fd1.open();
				String fp1Directory = fd1.getFilterPath();
				txtClassifyOutput.setText(fp1Directory);
			}
		});
		button_6.setText("...");
		button_6.setBounds(370, 61, 21, 25);
		
		Button btnClassify = new Button(grpClassifyMode, SWT.NONE);
		btnClassify.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				long currentTime = System.currentTimeMillis();
				
				
				String ppDir1 = txtFolderPath1.getText();
				String ppDir2 = txtFolderPath2.getText();
				
				if (ppDir1.equals("") || ppDir2.equals("") || txtClassifyOutput.getText().equals("")){
					showError(shell);
					return;
			}
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

				
				try {
				//SvmClassifier svm = new SvmClassifier(btnLowercase.getSelection(), txtDelimiters.getText(), txtStopWords.getText());      Preprocessing done separately
				SvmClassifier svm = new SvmClassifier(txtLabel1.getText(), txtLabel2.getText(), txtClassifyOutput.getText());
				// Injecting the context into Preprocessor object so that the appendLog function can modify the Context Parameter consoleMessage
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(svm,iEclipseContext);

				int selection = tabFolder.getSelectionIndex();
				if(btnLoadModel.getSelection())
					svm.loadPretrainedModel(txtLabel1.getText(), txtLabel2.getText(), txtModelFilePath.getText(), txtHashmapPath.getText());
				else
					svm.train(txtLabel1.getText(), ppDir1, txtLabel2.getText(), ppDir2, true,false, null,true, btnWeights.getSelection());  //btnLinear.getSelection() removed. made Linear Kernel default
				if(selection == 0){
					System.out.println("Test Mode");
					appendLog("Test Mode");
					svm.predict(txtLabel1.getText(), txtTestFolder1.getText(), txtLabel2.getText(), txtTestFolder2.getText());
					svm.output(txtLabel1.getText(), txtTestFolder1.getText(), txtLabel2.getText(), txtTestFolder2.getText());
				} else if (selection == 1){
					System.out.println("Classification Mode");
					appendLog("Classification Mode");
					svm.classify(txtLabel1.getText(), txtLabel2.getText(), txtClassifyInput.getText());
					svm.outputPredictedOnly(txtLabel1.getText(), txtLabel2.getText(), txtClassifyInput.getText());
				}
				System.out.println("Completed classification in "+((System.currentTimeMillis()-currentTime)/(double)1000)+" seconds.");
				appendLog("Completed classification in "+((System.currentTimeMillis()-currentTime)/(double)1000)+" seconds.");
				} catch (IOException | ClassNotFoundException ie) {
					ie.printStackTrace();
				}

			}
		});
		btnClassify.setText("Classify");
		btnClassify.setBounds(10, 95, 52, 25);
		
		Button button_7 = new Button(grpClassifyMode, SWT.NONE);
		button_7.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog fd1 = new DirectoryDialog(shell);
				fd1.open();
				String fp1Directory = fd1.getFilterPath();
				txtClassifyInput.setText(fp1Directory);
				
			}
		});
		button_7.setBounds(370, 24, 21, 25);
		button_7.setText("...");
		//TODO Your code here
	}
	
	private String doPp(String inputPath) throws IOException{
		return ppService.doPreprocessing(inputPath);
	}
	
	private void showPpOptions(Shell shell) {
		ppService.setOptions(shell);
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