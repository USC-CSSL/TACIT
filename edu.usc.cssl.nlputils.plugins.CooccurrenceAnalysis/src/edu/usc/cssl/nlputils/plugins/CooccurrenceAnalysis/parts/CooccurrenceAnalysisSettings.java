
package edu.usc.cssl.nlputils.plugins.CooccurrenceAnalysis.parts;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.usc.cssl.nlputils.plugins.CooccurrenceAnalysis.process.CooccurrenceAnalysis;
import edu.usc.cssl.nlputils.plugins.preprocessorService.services.PreprocessorService;

public class CooccurrenceAnalysisSettings {
	private Text txtInputDir;
	private PreprocessorService ppService = new PreprocessorService();
	@Inject
	public CooccurrenceAnalysisSettings() {
		
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
		lblSeedFile.setText("Word File");
		
		txtSeedFile = new Text(composite, SWT.BORDER);
		GridData gd_txtSeedFile = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtSeedFile.widthHint = 244;
		txtSeedFile.setLayoutData(gd_txtSeedFile);
		
		button_2 = new Button(composite, SWT.NONE);
		button_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				txtSeedFile.setText("");
				FileDialog fd = new FileDialog(shell,SWT.SAVE);
				fd.open();
				String oFile = fd.getFileName();
				String dir = fd.getFilterPath();
				txtSeedFile.setText(dir+System.getProperty("file.separator")+oFile);
			}
		});
		button_2.setText("...");
		
		lblNumberOfTopics = new Label(composite, SWT.NONE);
		lblNumberOfTopics.setText("Window Size");
		
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
				invokeCooccurrence();
				appendLog("Co-occurrence Analysis LDA completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
			}
		});
		btnCalculate.setText("Co-occurrence Analysis");
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		
	}

	private void showPpOptions(Shell shell){
		ppService.setOptions(shell);
	}
	
	private String doPp(String inputPath) throws IOException{
		return ppService.doPreprocessing(inputPath);
	}
	
	
	@Inject
	IEclipseContext context;
	private Text txtOutputDir;
	private Label lblOutputPath;
	private Text txtSeedFile;
	private Text txtNumTopics;
	private Button button;
	private Label lblSeedFile;
	private Label lblNumberOfTopics;
	private Button button_3;
	private String ppDir;
	private Button button_2;
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
	

protected void invokeCooccurrence( ){
		
	
		File dir = new File(ppDir);
		File[] listOfFiles =  dir.listFiles();
		
		if(listOfFiles.length == 0){
			appendLog("Please select at least one file on which to run Co-occurrence Analysis");
			return;
		}
		int windowSize = Integer.parseInt(txtNumTopics.getText());
		
		
		System.out.println("Running Co-occurrence Analysis...");
		appendLog("Running Co-occurrence Analysis...");
		boolean isSuccess = CooccurrenceAnalysis.calculateCooccurrences(ppDir, ppSeedFile, windowSize, txtOutputDir.getText());
		if(isSuccess == false)
		{
			appendLog("Sorry. Something went wrong with Co-occurrence Analysis. Please check your input and try again.\n");
			return;
		}

		appendLog("Output for Co-occurrence Analysis");
		
		appendLog("Word to word matrix stored in " + txtOutputDir + "\\word-matrix.csv" );
		appendLog("Phrases stored in " + txtOutputDir + "\\phrases.csv" );
		appendLog("Done Co-occurrence Analysis...");
		
	}
}