/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */ 
package edu.usc.pil.nlputils.plugins.lda.parts;

import java.io.IOException;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;



import edu.usc.pil.nlputils.plugins.lda.process.LDA;
import edu.usc.pil.nlputils.plugins.preprocessorService.services.PreprocessorService;


public class LDASettings {
	private Text txtSourceDir;
	private Text txtNumTopics;
	private Text txtOutputPath;
	private PreprocessorService ppService = new PreprocessorService();
	
	@Inject
	public LDASettings() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBounds(0, 0, 507, 298);
		
		Label lblData = new Label(composite, SWT.NONE);
		lblData.setBounds(10, 10, 55, 15);
		lblData.setText("Data Path");
		
		txtSourceDir = new Text(composite, SWT.BORDER);
		txtSourceDir.setBounds(115, 7, 334, 21);
		
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
		button.setBounds(450, 5, 26, 25);
		button.setText("...");
		
		txtNumTopics = new Text(composite, SWT.BORDER);
		txtNumTopics.setBounds(115, 46, 76, 21);
		
		Label lblNumberOfTopics = new Label(composite, SWT.NONE);
		lblNumberOfTopics.setBounds(10, 49, 99, 15);
		lblNumberOfTopics.setText("Number of Topics");
		
		txtOutputPath = new Text(composite, SWT.BORDER);
		txtOutputPath.setBounds(115, 83, 334, 21);
		
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
		button_1.setBounds(450, 81, 26, 25);
		button_1.setText("...");
		
		Label lblOutputPath = new Label(composite, SWT.NONE);
		lblOutputPath.setBounds(10, 89, 76, 15);
		lblOutputPath.setText("Output Path");
		
		Button btnProcess = new Button(composite, SWT.NONE);
		btnProcess.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				
				String ppDir = txtSourceDir.getText();
				if(ppService.doPP) {
					
					// Injecting the context into Preprocessor object so that the appendLog function can modify the Context Parameter consoleMessage
					IEclipseContext iEclipseContext = context;
					ContextInjectionFactory.inject(ppService,iEclipseContext);

				//Preprocessing
				appendLog("Preprocessing...");
				System.out.println("Preprocessing...");
				try {
					ppDir = doPp(txtSourceDir.getText());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				}
				
				LDA lda = new LDA();
				// Injecting the context into LDA object so that the appendLog function can modify the Context Parameter consoleMessage
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(lda,iEclipseContext);
				
				try {
					System.out.println("Processing...");
					appendLog("Processing...");
					
					long startTime = System.currentTimeMillis();
					lda.doLDA(ppDir, txtNumTopics.getText(), txtOutputPath.getText(), txtLabel.getText()); // Preprocessing is now done separately. Hence passing false
					System.out.println("Topic modelling completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
					appendLog("Topic modelling completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnProcess.setBounds(11, 163, 75, 25);
		btnProcess.setText("Process");
		
		Button btnPreprocess = new Button(composite, SWT.NONE);
		btnPreprocess.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				showPpOptions(shell);
			}
		});
		btnPreprocess.setBounds(482, 5, 75, 25);
		btnPreprocess.setText("Preprocess...");
		
		Label lblOutputPrefix = new Label(composite, SWT.NONE);
		lblOutputPrefix.setBounds(10, 125, 76, 15);
		lblOutputPrefix.setText("Output Prefix");
		
		txtLabel = new Text(composite, SWT.BORDER);
		txtLabel.setText("output");
		txtLabel.setBounds(115, 119, 76, 21);
		
	}
	
	
	private void showPpOptions(Shell shell){
		ppService.setOptions(shell);
	}
	
	private String doPp(String inputPath) throws IOException{
		return ppService.doPreprocessing(inputPath);
	}
	
	private String[] doPp(String inputPath1, String inputPath2) throws IOException{
		String[] dirPaths = new String[2];
		dirPaths[0] = ppService.doPreprocessing(inputPath1); 
		dirPaths[1] = ppService.doPreprocessing(inputPath2);
		return dirPaths;
	}
	
	@Inject IEclipseContext context;
	private Text txtLabel;
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