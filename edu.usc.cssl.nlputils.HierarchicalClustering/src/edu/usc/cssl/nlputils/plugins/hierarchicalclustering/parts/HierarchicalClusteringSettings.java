
package edu.usc.cssl.nlputils.plugins.hierarchicalclustering.parts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.usc.cssl.nlputils.application.handlers.GlobalPresserSettings;
import edu.usc.cssl.nlputils.plugins.hierarchicalclustering.process.HierarchicalClustering;
import edu.usc.cssl.nlputils.plugins.preprocessorService.services.PreprocessorService;

public class HierarchicalClusteringSettings {
	private Text txtInputDir;
	private PreprocessorService ppService = null;
	@Inject
	public HierarchicalClusteringSettings() {
		
	}
	
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		parent.setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(4, false));
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
		
		btnPreprocess = new Button(composite, SWT.CHECK);
		btnPreprocess.setText("Preprocess");
		
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
		new Label(composite, SWT.NONE);
		
		//new Label(composite, SWT.NONE);
		final Button btnImg = new Button(composite, SWT.CHECK);
		btnImg.setText("Save Image");
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		
		
		Button btnCalculate = new Button(composite, SWT.NONE);
		btnCalculate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				
				ppDir = txtInputDir.getText();
				
				if(btnPreprocess.getSelection()) {
					ppService = GlobalPresserSettings.ppService;
					// Injecting the context into Preprocessor object so that the appendLog function can modify the Context Parameter consoleMessage
					IEclipseContext iEclipseContext = context;
					ContextInjectionFactory.inject(ppService,iEclipseContext);

				//Preprocessing
				appendLog("Preprocessing...");
				System.out.println("Preprocessing...");
				try {
					ppDir = ppService.doPreprocessing(txtInputDir.getText());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				}
				long startTime = System.currentTimeMillis();
				appendLog("PROCESSING...(Hierarchical Clustering)");
				isSaveImg = btnImg.getSelection();
				runClustering();
				appendLog("Hierarchical Clustering completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
				if (btnPreprocess.getSelection() && ppService.doCleanUp()){
					ppService.clean(ppDir);
					System.out.println("Cleaning up preprocessed files - "+ppDir);
					appendLog("Cleaning up preprocessed files - "+ppDir);
				}
				appendLog("DONE (Hierarchinal Clustering)");
			}
		});
		btnCalculate.setText("Cluster");
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		
	}
		
	
	@Inject
	IEclipseContext context;
	private Text txtNumClusters;
	private Text txtOutputDir;
	private Label lblOutputPath;

	private Label lblNumberOfClusters;
	private Button button;

	private Button button_3;
	private String ppDir;
	private boolean isSaveImg;
	private Button btnPreprocess;
	
	
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
	

protected void runClustering( ){
		
		
	
		File dir = new File(ppDir);
		File[] listOfFiles =  dir.listFiles();
		List<File> inputFiles = new ArrayList<File>();
		for (File f : listOfFiles){
			if (f.getAbsolutePath().contains("DS_Store"))
				continue;
			if (!f.isDirectory() && f.exists())
				inputFiles.add(f);
			}
		if(inputFiles.size() == 0){
			appendLog("Please select at least one file on which to run Hierarchical Clustering");
			return;
		}
		
		
		System.out.println("Running Hierarchical Clustering...");
		appendLog("Running Hierarchical Clustering...");
		String clusters = HierarchicalClustering.doClustering(inputFiles, txtOutputDir.getText(), isSaveImg);
		if(clusters == null)
		{
			appendLog("Sorry. Something went wrong with Hierarchical Clustering. Please check your input and try again.\n");
			return;
		}

		appendLog("Output for Hierarchical Clustering");
		appendLog("Clusters formed: \n");
		
		appendLog(clusters);
		appendLog("Saving the output to cluster.txt");
		appendLog("\nDone Hierarchical Clustering...");
		
	}
}