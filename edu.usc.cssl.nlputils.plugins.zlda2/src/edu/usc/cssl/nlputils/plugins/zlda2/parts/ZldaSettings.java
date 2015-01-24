 
package edu.usc.cssl.nlputils.plugins.zlda2.parts;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.osgi.framework.FrameworkUtil;

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
		Label header = new Label(composite, SWT.NONE);
		header.setImage(ImageDescriptor.createFromURL(
				FileLocator.find(FrameworkUtil.getBundle(this.getClass()),
						new Path("plugin_icon/icon.png"), null)).createImage());
		header.setBounds(10, 0, 161, 40);

		Label lblTopicFile = new Label(composite, SWT.NONE);
		lblTopicFile.setBounds(10, 67, 65, 20);
		lblTopicFile.setText("Topic File");
		
		txtTopic = new Text(composite, SWT.BORDER);
		txtTopic.setBounds(170, 67, 334, 19);
		
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
		button.setBounds(510, 63, 43, 28);
		button.setText("...");
		
		Label lblDataSetPath = new Label(composite, SWT.NONE);
		lblDataSetPath.setBounds(10, 101, 90, 20);
		lblDataSetPath.setText("Data Set Path");
		
		txtData = new Text(composite, SWT.BORDER);
		txtData.setBounds(170, 101, 334, 19);
		
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
		button_1.setBounds(510,99, 43, 28);
		button_1.setText("...");
		
		Label lblNumberOfTopics = new Label(composite, SWT.NONE);
		lblNumberOfTopics.setBounds(10, 133, 110, 20);
		lblNumberOfTopics.setText("Number of Topics");
		
		txtNum = new Text(composite, SWT.BORDER);
		txtNum.setBounds(170, 130, 334, 19);
		
		txtOutput = new Text(composite, SWT.BORDER);
		txtOutput.setBounds(170	, 163, 334, 19);
		
		Label lblOutputPath = new Label(composite, SWT.NONE);
		lblOutputPath.setText("Output File Path");
		lblOutputPath.setBounds(10, 163, 101, 20);
		
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
		button_2.setBounds(510, 161, 43, 28);
		
		Button btnCalculate = new Button(composite, SWT.NONE);
		btnCalculate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				appendLog("PROCESSING...(Z-LDA Python Based)");
				long startTime = System.currentTimeMillis();
				Zlda2 z2 = new Zlda2();
				z2.callPython(txtTopic.getText(),txtData.getText(),txtNum.getText(),txtOutput.getText());
				appendLog("Completed writing output file "+txtOutput.getText());
				appendLog("Finished successfully in "+(System.currentTimeMillis()-startTime)/(float)1000+" seconds");
				appendLog("DONE");
			}
		});
		btnCalculate.setBounds(6, 196, 94, 28);
		btnCalculate.setText("Calculate");
		shell.setDefaultButton(btnCalculate);

		
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