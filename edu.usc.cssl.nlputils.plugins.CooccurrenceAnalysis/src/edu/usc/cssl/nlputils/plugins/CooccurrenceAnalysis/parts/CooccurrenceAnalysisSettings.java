
package edu.usc.cssl.nlputils.plugins.CooccurrenceAnalysis.parts;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.osgi.framework.FrameworkUtil;

import edu.usc.cssl.nlputils.application.handlers.GlobalPresserSettings;
import edu.usc.cssl.nlputils.plugins.CooccurrenceAnalysis.process.CooccurrenceAnalysis;
import edu.usc.cssl.nlputils.plugins.preprocessorService.services.PreprocessorService;

public class CooccurrenceAnalysisSettings {
	private Text txtInputDir;
	private PreprocessorService ppService = null;
	
	@Inject
	public CooccurrenceAnalysisSettings() {
		
	}
	
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		parent.setLayout(new GridLayout(1, false));
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		Label header = new Label(composite, SWT.NONE);
		header.setImage(ImageDescriptor.createFromURL(
				FileLocator.find(FrameworkUtil.getBundle(this.getClass()),
						new Path("plugin_icon/icon.png"), null)).createImage());
		header.setBounds(10, 0, 161, 40);
		for(int i=1; i<=5; i++){
			new Label(composite, SWT.NONE);
		}
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite.widthHint = 700;
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
				FileDialog fd = new FileDialog(shell,SWT.OPEN);
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
		
		lblThreshold = new Label(composite, SWT.NONE);
		lblThreshold.setText("Threshold");
		
		txtThreshold = new Text(composite, SWT.BORDER);
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
		
		btnPreprocess = new Button(composite, SWT.CHECK);
		btnPreprocess.setText("Preprocess");
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		
		btnOption = new Button(composite, SWT.CHECK);
		btnOption.setText("Build Co-occurrence Matrix");
		new Label(composite, SWT.NONE);
		
		Button btnCalculate = new Button(composite, SWT.NONE);
		btnCalculate.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		btnCalculate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				
				ppDir = txtInputDir.getText();
				ppSeedFile = txtSeedFile.getText();
				
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
				//System.out.println("Preprocessing...");
				try {
					ppDir = ppService.doPreprocessing(txtInputDir.getText());
					if(ppSeedFile!= "" && !ppSeedFile.isEmpty())
						ppSeedFile = ppService.doPreprocessing(txtSeedFile.getText());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				}
				long startTime = System.currentTimeMillis();
				invokeCooccurrence();
				
				if (btnPreprocess.getSelection() && ppService.doCleanUp()){
					ppService.clean(ppDir);
					System.out.println("Cleaning up preprocessed files - "+ppDir);
					appendLog("Cleaning up preprocessed files - "+ppDir);
					ppService.clean(ppSeedFile);
					System.out.println("Cleaning up preprocessed seed file - "+ppSeedFile);
					appendLog("Cleaning up preprocessed seed file - "+ppSeedFile);
				}
				
				appendLog("Co-occurrence Analysis completed in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
				appendLog("DONE (Co-occurrence Analysis)");
			}
		});
		btnCalculate.setText("Co-occurrence Analysis");
		shell.setDefaultButton(btnCalculate);
		new Label(composite, SWT.NONE);
		
	}
	
	@Inject
	IEclipseContext context;
	private Text txtOutputDir;
	private Label lblOutputPath;
	private Text txtThreshold;
	private Text txtSeedFile;
	private Text txtNumTopics;
	private Button button;
	private Label lblSeedFile;
	private Label lblNumberOfTopics;
	private Label lblThreshold;
	private Button button_3;
	private String ppDir;
	private Button button_2;
	private String ppSeedFile;
	private Button btnPreprocess;
	private Button btnOption;

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
		if(!dir.exists() || !dir.isDirectory())
		{
			appendLog("Input Path not correct. Please check and try again. Exiting ..");
			return;
		}
		File[] listOfFiles =  dir.listFiles();
		
		if(listOfFiles.length == 0){
			appendLog("Please select at least one file on which to run Co-occurrence Analysis");
			return;
		}
		int windowSize = 0;
		if(!txtNumTopics.getText().equals(""))
			windowSize = Integer.parseInt(txtNumTopics.getText());
		
		int threshold = 0;
		if(!txtThreshold.getText().equals(""))
			threshold = Integer.parseInt(txtThreshold.getText());
		
		boolean buildMatrix = false;
		if(btnOption.getSelection())
			buildMatrix = true;
		
		//System.out.println("Running Co-occurrence Analysis...");
		appendLog("Running Co-occurrence Analysis...");
		boolean isSuccess = CooccurrenceAnalysis.calculateCooccurrences(ppDir, ppSeedFile, windowSize, txtOutputDir.getText(), threshold, buildMatrix);
		if(isSuccess == false)
		{
			appendLog("Sorry. Something went wrong with Co-occurrence Analysis. Please check your input and try again.\n");
			return;
		}

		appendLog("Output for Co-occurrence Analysis");
		
		appendLog("Word to word matrix stored in " + txtOutputDir.getText() + File.separator + "word-to-word-matrix.csv" );
		if(ppSeedFile!= "" && !ppSeedFile.isEmpty() && windowSize !=0)
			appendLog("Phrases stored in " + txtOutputDir.getText() + File.separator + "phrases.txt" );
		
		
	}
}