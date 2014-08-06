 
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

public class LDASettings {
	private Text txtSourceDir;
	private Text txtNumTopics;
	private Text txtOutputPath;
	@Inject
	public LDASettings() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		Composite composite = new Composite(parent, SWT.NONE);
		
		Label lblData = new Label(composite, SWT.NONE);
		lblData.setBounds(10, 10, 55, 15);
		lblData.setText("Data Path");
		
		txtSourceDir = new Text(composite, SWT.BORDER);
		txtSourceDir.setBounds(115, 7, 269, 21);
		
		Button button = new Button(composite, SWT.NONE);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog fd1 = new DirectoryDialog(shell);
				fd1.open();
				String fp1Directory = fd1.getFilterPath();
				txtSourceDir.setText(fp1Directory);
				
			}
		});
		button.setBounds(386, 5, 21, 25);
		button.setText("...");
		
		final Button btnRemoveStopWords = new Button(composite, SWT.CHECK);
		btnRemoveStopWords.setBounds(115, 47, 136, 16);
		btnRemoveStopWords.setText("Remove Stop Words");
		
		final Button btnDoLowercase = new Button(composite, SWT.CHECK);
		btnDoLowercase.setBounds(277, 47, 136, 16);
		btnDoLowercase.setText("Convert to Lowercase");
		
		txtNumTopics = new Text(composite, SWT.BORDER);
		txtNumTopics.setBounds(115, 80, 76, 21);
		
		Label lblNumberOfTopics = new Label(composite, SWT.NONE);
		lblNumberOfTopics.setBounds(10, 86, 99, 15);
		lblNumberOfTopics.setText("Number of Topics");
		
		txtOutputPath = new Text(composite, SWT.BORDER);
		txtOutputPath.setBounds(115, 124, 269, 21);
		
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
		button_1.setBounds(386, 122, 21, 25);
		button_1.setText("...");
		
		Label lblOutputPath = new Label(composite, SWT.NONE);
		lblOutputPath.setBounds(10, 130, 76, 15);
		lblOutputPath.setText("Output Path");
		
		Button btnProcess = new Button(composite, SWT.NONE);
		btnProcess.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				LDA lda = new LDA();
				// Injecting the context into LDA object so that the appendLog function can modify the Context Parameter consoleMessage
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(lda,iEclipseContext);
				
				try {
					System.out.println("Processing...");
					appendLog("Processing...");
					
					long startTime = System.currentTimeMillis();
					lda.doLDA(txtSourceDir.getText(), btnRemoveStopWords.getSelection(), btnDoLowercase.getSelection(), txtNumTopics.getText(), txtOutputPath.getText());
					System.out.println("Topic modelling completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
					appendLog("Topic modelling completed successfully in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnProcess.setBounds(10, 167, 75, 25);
		btnProcess.setText("Process");
		
	}
	
	@Inject IEclipseContext context;
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