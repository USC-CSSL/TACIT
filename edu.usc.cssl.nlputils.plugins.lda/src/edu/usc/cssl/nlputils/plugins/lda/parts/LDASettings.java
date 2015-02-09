/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */ 
package edu.usc.cssl.nlputils.plugins.lda.parts;

import java.io.File;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;









import org.osgi.framework.FrameworkUtil;

import edu.usc.cssl.nlputils.application.handlers.GlobalPresserSettings;
import edu.usc.cssl.nlputils.plugins.lda.process.LDA;
import edu.usc.cssl.nlputils.plugins.preprocessorService.services.PreprocessorService;
import edu.usc.cssl.nlputils.utilities.Log;


public class LDASettings {
	private Text txtSourceDir;
	private Text txtNumTopics;
	private Text txtOutputPath;
	private Text txtLabel;
	
	private PreprocessorService ppService = null;
	
	@Inject
	public LDASettings() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBounds(0, 0, 507, 298);
		Label header = new Label(composite, SWT.NONE);
		header.setImage(ImageDescriptor.createFromURL(
				FileLocator.find(FrameworkUtil.getBundle(this.getClass()),
						new Path("plugin_icon/icon.png"), null)).createImage());
		header.setBounds(10, 0, 200, 40);
		
		Label lblData = new Label(composite, SWT.NONE);
		lblData.setBounds(10, 70, 70, 20);
		lblData.setText("Data Path");
		
		txtSourceDir = new Text(composite, SWT.BORDER);
		txtSourceDir.setBounds(130, 70, 334, 21);
		
		Button button = new Button(composite, SWT.NONE);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog fd1 = new DirectoryDialog(shell);
				fd1.open();
				String fp1Directory = fd1.getFilterPath();
				txtSourceDir.setText(fp1Directory);
				txtLabel.setText(fd1.getFilterPath().substring(1+fd1.getFilterPath().lastIndexOf(System.getProperty("file.separator"))));
			}
		});
		button.setBounds(465, 70, 40, 25);
		button.setText("...");
		
		txtNumTopics = new Text(composite, SWT.BORDER);
		txtNumTopics.setBounds(130, 99, 76, 21);
		
		Label lblNumberOfTopics = new Label(composite, SWT.NONE);
		lblNumberOfTopics.setBounds(10, 99, 110, 20);
		lblNumberOfTopics.setText("Number of Topics");
		
		txtOutputPath = new Text(composite, SWT.BORDER);
		txtOutputPath.setBounds(130, 149, 334, 21);
		
		Button button_1 = new Button(composite, SWT.NONE);
		button_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog fd1 = new DirectoryDialog(shell);
				fd1.open();
				String fp1Directory = fd1.getFilterPath();
				txtOutputPath.setText(fp1Directory);
			}
		});
		button_1.setBounds(465, 149, 40, 25);
		button_1.setText("...");
		
		Label lblOutputPath = new Label(composite, SWT.NONE);
		lblOutputPath.setBounds(10, 149, 80, 20);
		lblOutputPath.setText("Output Path");
		
		final Button btnPreprocess = new Button(composite, SWT.CHECK);
		btnPreprocess.setBounds(520, 70, 94, 18);
		btnPreprocess.setText("Preprocess");
		
		Button btnProcess = new Button(composite, SWT.NONE);
		btnProcess.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				
				String ppDir = txtSourceDir.getText();
				File iDir = new File(ppDir);
				File[] iFiles = iDir.listFiles();
				for (File f : iFiles){
					if (f.getAbsolutePath().contains("DS_Store"))
						f.delete();
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
					ppDir = ppService.doPreprocessing(txtSourceDir.getText());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				}
				
				LDA lda = new LDA();
				// Injecting the context into LDA object so that the appendLog function can modify the Context Parameter consoleMessage
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(lda,iEclipseContext);
				// Preprocessing is now done separately. Hence passing false
				lda.initialize(ppDir, txtNumTopics.getText(), txtOutputPath.getText(), txtLabel.getText());
				
				final String fppDir = ppDir;
				// Creating a new Job to do LDA so that the UI will not freeze
				Job job = new Job("LDA Job"){
					protected IStatus run(IProgressMonitor monitor){ 
					
						try {
							System.out.println("Processing...");
							appendLog("Processing...(LDA)");
							
							long startTime = System.currentTimeMillis();
							//appendLog("PROCESSING...");
							lda.doLDA(); 					
							
							System.out.println("Topic modelling completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
							appendLog("Topic modelling completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
							
							// Async callback to access UI elements 
							Display.getDefault().asyncExec(new Runnable() {
							      @Override
							      public void run() {
							    	  if (btnPreprocess.getSelection() && ppService.doCleanUp()){
											ppService.clean(fppDir);
											System.out.println("Cleaning up preprocessed files - "+fppDir);
											appendLog("Cleaning up preprocessed files - "+fppDir);
										}
							      }
							    });
//							if (btnPreprocess.getSelection() && ppService.doCleanUp()){
//								ppService.clean(fppDir);
//								System.out.println("Cleaning up preprocessed files - "+fppDir);
//								appendLog("Cleaning up preprocessed files - "+fppDir);
//							}
							appendLog("DONE (LDA)");
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
					return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				job.schedule();
				
			}
		});
		btnProcess.setBounds(11, 223, 75, 25);
		btnProcess.setText("Process");
		shell.setDefaultButton(btnProcess);
		
		Label lblOutputPrefix = new Label(composite, SWT.NONE);
		lblOutputPrefix.setBounds(10, 185, 100, 20);
		lblOutputPrefix.setText("Output Prefix");
		
		txtLabel = new Text(composite, SWT.BORDER);
		txtLabel.setText("output");
		txtLabel.setBounds(130, 185, 76, 21);	
		
	}
	
	
	@Inject IEclipseContext context;
	private void appendLog(String message){
		Log.append(context,message);
	}
}