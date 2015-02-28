
package edu.usc.cssl.nlputils.plugins.hierarchicalclustering.parts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.FrameworkUtil;

import edu.usc.cssl.nlputils.application.handlers.GlobalPresserSettings;
import edu.usc.cssl.nlputils.plugins.hierarchicalclustering.process.HierarchicalClustering;
import edu.usc.cssl.nlputils.plugins.preprocessorService.services.PreprocessorService;
import edu.usc.cssl.nlputils.utilities.Log;

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
		Label header = new Label(parent, SWT.NONE);
		header.setImage(ImageDescriptor.createFromURL(
				FileLocator.find(FrameworkUtil.getBundle(this.getClass()),
						new Path("plugin_icon/icon.png"), null)).createImage());
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(4, false));
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite.widthHint = 500;
		gd_composite.heightHint = 500;
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
		btnImg.setBounds(482, 5, 75, 25);
		
		Button btnCalculate = new Button(composite, SWT.NONE);
		btnCalculate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				
				ppDir = txtInputDir.getText();
				
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
					ppDir = ppService.doPreprocessing(txtInputDir.getText());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				}
				appendLog("PROCESSING...(Hierarchical Clustering)");
				isSaveImg = btnImg.getSelection();
				final String fppDir = ppDir;
				final String fOutputDir = txtOutputDir.getText();
				final boolean fSaveImg = isSaveImg;
				
				// Creating a new Job to do HClustering so that the UI will not freeze
				Job job = new Job("HCluster Job"){
					protected IStatus run(IProgressMonitor monitor){ 
						long startTime = System.currentTimeMillis();
						
				runClustering(fppDir, fOutputDir, fSaveImg);
				appendLog("Hierarchical Clustering completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
				
				Display.getDefault().asyncExec(new Runnable() {
				      @Override
				      public void run() {
				if (btnPreprocess.getSelection() && ppService.doCleanUp()){
					ppService.clean(ppDir);
					System.out.println("Cleaning up preprocessed files - "+ppDir);
					appendLog("Cleaning up preprocessed files - "+ppDir);
				}
				appendLog("DONE (Hierarchinal Clustering)");
				      }
			    });
				
				return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				job.schedule();
			}
		});
		btnCalculate.setText("Cluster");
		shell.setDefaultButton(btnCalculate);
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
		Log.append(context,message);
	}
	

protected void runClustering(String fppDir, String fOutputDir, boolean fSaveImg ){
		File dir = new File(fppDir);
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
		String clusters = HierarchicalClustering.doClustering(inputFiles, fOutputDir, fSaveImg);
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