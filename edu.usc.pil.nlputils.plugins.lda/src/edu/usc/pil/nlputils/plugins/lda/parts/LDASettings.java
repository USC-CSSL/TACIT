 
package edu.usc.pil.nlputils.plugins.lda.parts;

import javax.inject.Inject;
import javax.annotation.PostConstruct;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;

public class LDASettings {
	private Text text;
	private Text text_1;
	private Text text_2;
	@Inject
	public LDASettings() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		
		Label lblData = new Label(composite, SWT.NONE);
		lblData.setBounds(10, 10, 55, 15);
		lblData.setText("Data Path");
		
		text = new Text(composite, SWT.BORDER);
		text.setBounds(115, 7, 269, 21);
		
		Button button = new Button(composite, SWT.NONE);
		button.setBounds(386, 5, 21, 25);
		button.setText("...");
		
		Button btnKeepSequence = new Button(composite, SWT.CHECK);
		btnKeepSequence.setBounds(10, 47, 123, 16);
		btnKeepSequence.setText("Keep Sequence");
		
		Button btnRemoveStopWords = new Button(composite, SWT.CHECK);
		btnRemoveStopWords.setBounds(139, 47, 136, 16);
		btnRemoveStopWords.setText("Remove Stop Words");
		
		Button btnConvertToLowercase = new Button(composite, SWT.CHECK);
		btnConvertToLowercase.setBounds(292, 47, 136, 16);
		btnConvertToLowercase.setText("Convert to Lowercase");
		
		text_1 = new Text(composite, SWT.BORDER);
		text_1.setBounds(115, 80, 76, 21);
		
		Label lblNumberOfTopics = new Label(composite, SWT.NONE);
		lblNumberOfTopics.setBounds(10, 86, 99, 15);
		lblNumberOfTopics.setText("Number of Topics");
		
		text_2 = new Text(composite, SWT.BORDER);
		text_2.setBounds(115, 124, 269, 21);
		
		Button button_1 = new Button(composite, SWT.NONE);
		button_1.setBounds(386, 122, 21, 25);
		button_1.setText("...");
		
		Label lblOutputPath = new Label(composite, SWT.NONE);
		lblOutputPath.setBounds(10, 130, 76, 15);
		lblOutputPath.setText("Output Path");
		
	}
}