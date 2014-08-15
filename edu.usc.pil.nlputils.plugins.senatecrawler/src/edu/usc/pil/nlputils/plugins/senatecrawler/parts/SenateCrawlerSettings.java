 
package edu.usc.pil.nlputils.plugins.senatecrawler.parts;

import javax.inject.Inject;
import javax.annotation.PostConstruct;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.DateTime;

public class SenateCrawlerSettings {
	@Inject
	public SenateCrawlerSettings() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		
		Label lblSenator = new Label(composite, SWT.NONE);
		lblSenator.setBounds(10, 10, 55, 15);
		lblSenator.setText("Senator");
		
		DateTime dateTime = new DateTime(composite, SWT.BORDER);
		dateTime.setBounds(74, 45, 80, 24);
		
		Label lblFromDate = new Label(composite, SWT.NONE);
		lblFromDate.setBounds(10, 54, 55, 15);
		lblFromDate.setText("From Date");
		
		DateTime dateTime_1 = new DateTime(composite, SWT.BORDER);
		dateTime_1.setBounds(281, 32, 80, 24);
		
		Label lblToDate = new Label(composite, SWT.NONE);
		lblToDate.setBounds(204, 41, 55, 15);
		lblToDate.setText("To Date");
		
	}
}