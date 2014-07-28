 
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
	public SvmClassifierSettings() {
		//TODO Your code here
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		parent.setLayout(new GridLayout(4, false));
		
		Label lblLabel = new Label(parent, SWT.NONE);
		lblLabel.setText("Label for Class 1");
		new Label(parent, SWT.NONE);
		
		txtLabel1 = new Text(parent, SWT.BORDER);
		GridData gd_txtLabel1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtLabel1.widthHint = 250;
		txtLabel1.setLayoutData(gd_txtLabel1);
		new Label(parent, SWT.NONE);
		
		Label lblFolder = new Label(parent, SWT.NONE);
		lblFolder.setText("Folder for Class 1");
		new Label(parent, SWT.NONE);
		
		txtFolderPath1 = new Text(parent, SWT.BORDER);
		txtFolderPath1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button button = new Button(parent, SWT.NONE);
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
		
		Label lblLabel_1 = new Label(parent, SWT.NONE);
		lblLabel_1.setText("Label for Class 2");
		new Label(parent, SWT.NONE);
		
		txtLabel2 = new Text(parent, SWT.BORDER);
		txtLabel2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(parent, SWT.NONE);
		
		Label lblFolder_1 = new Label(parent, SWT.NONE);
		lblFolder_1.setText("Folder for Class 2");
		new Label(parent, SWT.NONE);
		
		txtFolderPath2 = new Text(parent, SWT.BORDER);
		txtFolderPath2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button button_1 = new Button(parent, SWT.NONE);
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
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		final Button btnTest = new Button(parent, SWT.RADIO);
		btnTest.setSelection(true);
		btnTest.setText("Test Mode");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Group group = new Group(parent, SWT.NONE);
		GridData gd_group = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
		gd_group.heightHint = 86;
		gd_group.widthHint = 395;
		group.setLayoutData(gd_group);
		
		Label lblTestFolder = new Label(group, SWT.NONE);
		lblTestFolder.setBounds(10, 23, 119, 15);
		lblTestFolder.setText("Test Folder for Class 1");
		
		txtTestFolder1 = new Text(group, SWT.BORDER);
		txtTestFolder1.setBounds(135, 17, 220, 21);
		
		Label lblTestFolder_1 = new Label(group, SWT.NONE);
		lblTestFolder_1.setBounds(10, 52, 119, 15);
		lblTestFolder_1.setText("Test Folder for Class 2");
		
		txtTestFolder2 = new Text(group, SWT.BORDER);
		txtTestFolder2.setBounds(135, 46, 220, 21);
		
		Button button_3 = new Button(group, SWT.NONE);
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
		
		Button button_4 = new Button(group, SWT.NONE);
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
		new Label(parent, SWT.NONE);
		
		Button btnClassify = new Button(parent, SWT.RADIO);
		btnClassify.setText("Classify Mode");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Group grpClassify = new Group(parent, SWT.NONE);
		GridData gd_grpClassify = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
		gd_grpClassify.heightHint = 57;
		gd_grpClassify.widthHint = 400;
		grpClassify.setLayoutData(gd_grpClassify);
		grpClassify.setText("Classify");
		
		Label lblFolder_2 = new Label(grpClassify, SWT.NONE);
		lblFolder_2.setBounds(10, 32, 97, 15);
		lblFolder_2.setText("Directory Path");
		
		text_2 = new Text(grpClassify, SWT.BORDER);
		text_2.setBounds(135, 26, 234, 21);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Label lblOutputFile = new Label(parent, SWT.NONE);
		lblOutputFile.setText("Output File");
		new Label(parent, SWT.NONE);
		
		txtOutputFile = new Text(parent, SWT.BORDER);
		txtOutputFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
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
				SvmClassifier svm = new SvmClassifier();
				// Injecting the context into Preprocessor object so that the appendLog function can modify the Context Parameter consoleMessage
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(svm,iEclipseContext);
				
				try {
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