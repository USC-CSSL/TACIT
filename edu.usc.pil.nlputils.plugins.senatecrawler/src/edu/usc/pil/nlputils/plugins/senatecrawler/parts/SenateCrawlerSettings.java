 
package edu.usc.pil.nlputils.plugins.senatecrawler.parts;

import java.io.IOException;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Combo;

import edu.usc.pil.nlputils.plugins.senatecrawler.process.AvailableRecords;

public class SenateCrawlerSettings {
	private Text txtMaxDocs;
	@Inject
	public SenateCrawlerSettings() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		/*
		String[] congresses = null;
		try {
			 congresses = AvailableRecords.getAllCongresses();
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLocation(-87, -10);
		
		Label lblSenator = new Label(composite, SWT.NONE);
		lblSenator.setBounds(10, 10, 55, 15);
		lblSenator.setText("Senator");
		
		DateTime dateTime = new DateTime(composite, SWT.BORDER);
		dateTime.setBounds(51, 97, 80, 24);
		
		Label lblFromDate = new Label(composite, SWT.NONE);
		lblFromDate.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblFromDate.setBounds(10, 74, 76, 15);
		lblFromDate.setText("Date Range");
		
		DateTime dateTime_1 = new DateTime(composite, SWT.BORDER);
		dateTime_1.setBounds(206, 97, 80, 24);
		
		Label lblToDate = new Label(composite, SWT.NONE);
		lblToDate.setBounds(179, 106, 21, 15);
		lblToDate.setText("To");
		
		Label lblMaximumRecordsPer = new Label(composite, SWT.NONE);
		lblMaximumRecordsPer.setBounds(10, 157, 182, 15);
		lblMaximumRecordsPer.setText("Maximum Records per Senator");
		
		txtMaxDocs = new Text(composite, SWT.BORDER);
		txtMaxDocs.setBounds(206, 151, 76, 21);
		
		Label lblFrom = new Label(composite, SWT.NONE);
		lblFrom.setBounds(10, 106, 35, 15);
		lblFrom.setText("From");
		
		Combo combo = new Combo(composite, SWT.NONE);
		combo.setItems(new String[] {"All", "113"});
		//combo.setItems(congresses);
		combo.setBounds(10, 33, 91, 23);
		combo.select(0);
		
	}
}