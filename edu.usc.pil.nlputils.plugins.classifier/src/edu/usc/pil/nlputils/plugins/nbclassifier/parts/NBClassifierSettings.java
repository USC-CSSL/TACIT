 
package edu.usc.pil.nlputils.plugins.nbclassifier.parts;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import edu.usc.pil.nlputils.plugins.nbclassifier.algorithms.NaiveBayesClass;

public class NBClassifierSettings {
	private Text text;
	private Text text_2;
	private Text text_3;
	private Text text_4;
	private Text text_5;
	private Text text_6;
	@Inject
	public NBClassifierSettings() {
		//TODO Your code here
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		Group grpTraining = new Group(parent, SWT.NONE);
		GridData gd_grpTraining = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_grpTraining.heightHint = 126;
		gd_grpTraining.widthHint = 430;
		grpTraining.setLayoutData(gd_grpTraining);
		grpTraining.setText("Training");
		
		Label lblDataSet = new Label(grpTraining, SWT.NONE);
		lblDataSet.setBounds(10, 26, 95, 15);
		lblDataSet.setText("Data Set Path");
		
		text = new Text(grpTraining, SWT.BORDER);
		text.setBounds(133, 19, 247, 21);
		
		Button button = new Button(grpTraining, SWT.NONE);
		button.setBounds(385, 16, 21, 25);
		button.setText("...");
		
		Button btnTrain = new Button(grpTraining, SWT.NONE);
		btnTrain.setBounds(10, 97, 75, 25);
		btnTrain.setText("Train");
		
		Label lblModelFilePath = new Label(grpTraining, SWT.NONE);
		lblModelFilePath.setBounds(10, 62, 95, 15);
		lblModelFilePath.setText("Model File Path");
		
		text_2 = new Text(grpTraining, SWT.BORDER);
		text_2.setBounds(133, 59, 247, 21);
		
		Button button_1 = new Button(grpTraining, SWT.NONE);
		button_1.setBounds(385, 57, 21, 25);
		button_1.setText("...");
		
		Group grpTesting = new Group(parent, SWT.NONE);
		GridData gd_grpTesting = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_grpTesting.heightHint = 77;
		gd_grpTesting.widthHint = 428;
		grpTesting.setLayoutData(gd_grpTesting);
		grpTesting.setText("Testing");
		
		Label lblNewLabel = new Label(grpTesting, SWT.NONE);
		lblNewLabel.setBounds(10, 28, 88, 15);
		lblNewLabel.setText("Test Data Path");
		
		text_3 = new Text(grpTesting, SWT.BORDER);
		text_3.setBounds(134, 22, 245, 21);
		
		Button button_2 = new Button(grpTesting, SWT.NONE);
		button_2.setBounds(385, 20, 21, 25);
		button_2.setText("...");
		
		Button btnTest = new Button(grpTesting, SWT.NONE);
		btnTest.setBounds(10, 60, 75, 25);
		btnTest.setText("Test");
		
		Group grpClassify = new Group(parent, SWT.NONE);
		GridData gd_grpClassify = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_grpClassify.heightHint = 160;
		gd_grpClassify.widthHint = 431;
		grpClassify.setLayoutData(gd_grpClassify);
		grpClassify.setText("Classify");
		
		Label lblInputFile = new Label(grpClassify, SWT.NONE);
		lblInputFile.setBounds(10, 29, 55, 15);
		lblInputFile.setText("Input File");
		
		text_4 = new Text(grpClassify, SWT.BORDER);
		text_4.setBounds(135, 23, 242, 21);
		
		Button button_3 = new Button(grpClassify, SWT.NONE);
		button_3.setBounds(383, 19, 21, 25);
		button_3.setText("...");
		
		Label lblOutputFile = new Label(grpClassify, SWT.NONE);
		lblOutputFile.setBounds(10, 64, 78, 15);
		lblOutputFile.setText("Model File");
		
		text_5 = new Text(grpClassify, SWT.BORDER);
		text_5.setBounds(135, 58, 242, 21);
		
		Button button_4 = new Button(grpClassify, SWT.NONE);
		button_4.setBounds(383, 54, 21, 25);
		button_4.setText("...");
		
		Label lblOutputFile_1 = new Label(grpClassify, SWT.NONE);
		lblOutputFile_1.setBounds(10, 97, 78, 15);
		lblOutputFile_1.setText("Output File");
		
		text_6 = new Text(grpClassify, SWT.BORDER);
		text_6.setBounds(135, 91, 242, 21);
		
		Button button_5 = new Button(grpClassify, SWT.NONE);
		button_5.setBounds(383, 87, 21, 25);
		button_5.setText("...");
		
		Button btnClassify = new Button(grpClassify, SWT.NONE);
		btnClassify.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				NaiveBayesClass nb = new NaiveBayesClass();
				try{
				nb.doClassification();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		btnClassify.setBounds(10, 131, 75, 25);
		btnClassify.setText("Classify");
		//TODO Your code here
	}
}