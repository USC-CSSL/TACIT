 
package edu.usc.cssl.nlputils.plugins.zlda2.parts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import edu.usc.cssl.nlputils.plugins.zlda2.process.Zlda2;

public class ZldaSettings {
	private Text txtTopic;
	private Text txtData;
	private Text txtNum;
	private Text txtOutput;
	@Inject
	public ZldaSettings() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		Composite composite = new Composite(parent, SWT.NONE);
		
		Label lblTopicFile = new Label(composite, SWT.NONE);
		lblTopicFile.setBounds(10, 10, 59, 14);
		lblTopicFile.setText("Topic File");
		
		txtTopic = new Text(composite, SWT.BORDER);
		txtTopic.setBounds(118, 8, 156, 19);
		
		Button button = new Button(composite, SWT.NONE);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				FileDialog fd = new FileDialog(shell,SWT.OPEN);
				fd.open();
				String oFile = fd.getFileName();
				String dir = fd.getFilterPath();
				txtTopic.setText(dir+System.getProperty("file.separator")+oFile);
			}
		});
		button.setBounds(274, 5, 43, 28);
		button.setText("...");
		
		Label lblDataSetPath = new Label(composite, SWT.NONE);
		lblDataSetPath.setBounds(10, 41, 75, 14);
		lblDataSetPath.setText("Data Set Path");
		
		txtData = new Text(composite, SWT.BORDER);
		txtData.setBounds(118, 39, 156, 19);
		
		Button button_1 = new Button(composite, SWT.NONE);
		button_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog fd2 = new DirectoryDialog(shell);
				fd2.open();
				String fp2Directory = fd2.getFilterPath();
				txtData.setText(fp2Directory);
			
			}
		});
		button_1.setBounds(274, 35, 43, 28);
		button_1.setText("...");
		
		Label lblNumberOfTopics = new Label(composite, SWT.NONE);
		lblNumberOfTopics.setBounds(10, 73, 100, 14);
		lblNumberOfTopics.setText("Number of Topics");
		
		txtNum = new Text(composite, SWT.BORDER);
		txtNum.setBounds(118, 70, 156, 19);
		
		txtOutput = new Text(composite, SWT.BORDER);
		txtOutput.setBounds(118, 101, 156, 19);
		
		Label lblOutputPath = new Label(composite, SWT.NONE);
		lblOutputPath.setText("Output File Path");
		lblOutputPath.setBounds(10, 103, 100, 14);
		
		Button button_2 = new Button(composite, SWT.NONE);
		button_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				FileDialog fd = new FileDialog(shell,SWT.SAVE);
				fd.open();
				String oFile = fd.getFileName();
				String dir = fd.getFilterPath();
				txtOutput.setText(dir+System.getProperty("file.separator")+oFile);
			}
		});
		button_2.setText("...");
		button_2.setBounds(274, 97, 43, 28);
		
		Button btnCalculate = new Button(composite, SWT.NONE);
		btnCalculate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				appendLog("Processing Z-label LDA");
				long startTime = System.currentTimeMillis();
				Zlda2 z2 = new Zlda2();
				z2.callPython(txtTopic.getText(),txtData.getText(),txtNum.getText(),txtOutput.getText());
				appendLog("Completed writing output file "+txtOutput.getText());
				appendLog("Finished successfully in "+(System.currentTimeMillis()-startTime)/(float)1000+" seconds");
			}
		});
		btnCalculate.setBounds(6, 136, 94, 28);
		btnCalculate.setText("Calculate");
		
	}
	
	@Inject
	IEclipseContext context;
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