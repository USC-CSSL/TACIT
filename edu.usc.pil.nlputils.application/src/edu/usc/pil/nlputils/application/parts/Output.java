 
package edu.usc.pil.nlputils.application.parts;

import javax.inject.Inject;
import javax.annotation.PostConstruct;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

public class Output {
	private Text text;
	@Inject
	public Output() {
		//TODO Your code here
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		parent.setLayout(new GridLayout(13, false));
		
		Label lblKeyword = new Label(parent, SWT.NONE);
		lblKeyword.setAlignment(SWT.CENTER);
		lblKeyword.setText("Keyword :");
		
		text = new Text(parent, SWT.BORDER);
		GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, false, false, 11, 1);
		gd_text.widthHint = 215;
		text.setLayoutData(gd_text);
		
		Button btnSearch = new Button(parent, SWT.NONE);
		GridData gd_btnSearch = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnSearch.widthHint = 70;
		btnSearch.setLayoutData(gd_btnSearch);
		btnSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnSearch.setText("Search");
		
		StyledText styledText = new StyledText(parent, SWT.BORDER);
		GridData gd_styledText = new GridData(SWT.LEFT, SWT.CENTER, false, false, 13, 1);
		gd_styledText.widthHint = 356;
		gd_styledText.heightHint = 211;
		styledText.setLayoutData(gd_styledText);
		//TODO Your code here
	}
	
	
	
	
}