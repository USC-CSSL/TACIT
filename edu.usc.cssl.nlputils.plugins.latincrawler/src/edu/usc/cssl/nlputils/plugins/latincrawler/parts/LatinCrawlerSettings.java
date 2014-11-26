/**
 * @author Niki Parmar {nikijitp@usc.edu}
 */ 
package edu.usc.cssl.nlputils.plugins.latincrawler.parts;

import java.io.IOException;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Combo;

import edu.usc.cssl.nlputils.plugins.latincrawler.process.AvailableRecords;
import edu.usc.cssl.nlputils.plugins.latincrawler.process.LatinCrawler;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

public class LatinCrawlerSettings {
	
	@Inject
	public LatinCrawlerSettings() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		appendLog("Loading Latin Library...");
		String[] authors = null;

		try {
			 authors = AvailableRecords.getAllAuthors();
		} catch (IOException e) {
			e.printStackTrace();
		}

		appendLog("Loading Authors...");
		final String[] allAuthors = authors;
		/*
		String[] booksArray = null;

		try {
			booksArray = AvailableRecords.getAllBooks(authors);
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		
		*/
		
		appendLog("Loading Complete.");
	
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setSize(588, 300);
		composite.setLocation(0, 0);
		
		/*
		Label lblSenator = new Label(composite, SWT.NONE);
		lblSenator.setBounds(10, 36, 55, 15);
		lblSenator.setText("Congress");
		
		final DateTime dateTime = new DateTime(composite, SWT.BORDER);
		dateTime.setEnabled(false);
		dateTime.setBounds(89, 133, 101, 24);
		
		final DateTime dateTime_1 = new DateTime(composite, SWT.BORDER);
		dateTime_1.setEnabled(false);
		dateTime_1.setBounds(288, 133, 101, 24);
		
		Label lblToDate = new Label(composite, SWT.NONE);
		lblToDate.setBounds(245, 137, 21, 15);
		lblToDate.setText("To");
		
		txtMaxDocs = new Text(composite, SWT.BORDER);
		txtMaxDocs.setToolTipText("Enter the maximum number of records to extract for each senator");
		txtMaxDocs.setEnabled(false);
		txtMaxDocs.setBounds(189, 184, 40, 21);
		
		Label lblFrom = new Label(composite, SWT.NONE);
		lblFrom.setBounds(26, 137, 35, 15);
		lblFrom.setText("From");

		final Combo cmbSenator = new Combo(composite, SWT.NONE);
		cmbSenator.setBounds(89, 67, 300, 23);
		cmbSenator.setItems(allSenators);
		cmbSenator.add("All Senators", 0);
		cmbSenator.add("All Democrats", 1);
		cmbSenator.add("All Republicans", 2);
		cmbSenator.add("All Independents", 3);
		cmbSenator.select(0);
		
		final Combo cmbCongress = new Combo(composite, SWT.NONE);
		cmbCongress.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String selectedCongress = cmbCongress.getText().trim();
					if (selectedCongress.equals("All")){
						cmbSenator.setItems(allSenators);
						cmbSenator.add("All Senators", 0);
						cmbSenator.add("All Democrats", 1);
						cmbSenator.add("All Republicans", 2);
						cmbSenator.add("All Independents", 3);
						cmbSenator.select(0);
					}
					else{
						cmbSenator.setItems(AvailableRecords.getSenators(selectedCongress));
						cmbSenator.add("All Senators", 0);
						cmbSenator.add("All Democrats", 1);
						cmbSenator.add("All Republicans", 2);
						cmbSenator.add("All Independents", 3);
						cmbSenator.select(0);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				cmbSenator.select(0);
			}
		});
		//combo.setItems(new String[] {"All", "113"});
		cmbCongress.setItems(authors);
		cmbCongress.setBounds(89, 33, 101, 23);
		cmbCongress.select(0);
		
		Label lblSenator_1 = new Label(composite, SWT.NONE);
		lblSenator_1.setBounds(10, 70, 55, 15);
		lblSenator_1.setText("Senator");
		

		final Button btnDateRange = new Button(composite, SWT.CHECK);
		btnDateRange.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnDateRange.getSelection()){
					dateTime.setEnabled(true);
					dateTime_1.setEnabled(true);
				} else {
					dateTime.setEnabled(false);
					dateTime_1.setEnabled(false);
				}
			}
		});
		btnDateRange.setBounds(9, 112, 93, 16);
		btnDateRange.setText("Date Range");
		*/
		
		Button btnExtract = new Button(composite, SWT.NONE);
		btnExtract.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {

				
				LatinCrawler crawler = new LatinCrawler();
				// Injecting the context into Senatecrawler object so that the appendLog function can modify the Context Parameter consoleMessage
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(crawler,iEclipseContext);

			
				try {
					
					long startTime = System.currentTimeMillis();
					crawler.initialize( allAuthors,  txtOutput.getText());
				
					appendLog("Extraction completed in "+(System.currentTimeMillis()-startTime)/(float)1000+" seconds");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		btnExtract.setBounds(10, 263, 75, 25);
		btnExtract.setText("Extract");
		
		Label lblOutput = new Label(composite, SWT.NONE);
		lblOutput.setBounds(10, 231, 80, 15);
		lblOutput.setText("Output Path");
		
		txtOutput = new Text(composite, SWT.BORDER);
		txtOutput.setBounds(104, 225, 258, 21);
		
		Button button = new Button(composite, SWT.NONE);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog od = new DirectoryDialog(shell);
				od.open();
				String oDirectory = od.getFilterPath();
				txtOutput.setText(oDirectory);
			}
		});
		button.setBounds(358, 224, 40, 25);
		button.setText("...");
	
		
		Label lblNewLabel = new Label(composite, SWT.BORDER | SWT.SHADOW_NONE);
		lblNewLabel.setBounds(433, 14, 133, 215);
		lblNewLabel.setText(" Year                 Congress\r\n 1989-1990       101st\r\n 1991-1992       102nd\r\n 1993-1994       103rd\r\n 1995-1996       104th\r\n 1997-1998       105th\r\n 1999-2000       106th\r\n 2001-2002       107th\r\n 2003-2004       108th\r\n 2005-2006       109th\r\n 2007-2008       110th\r\n 2009-2010       111th\r\n 2011-2012       112th\r\n 2013-2014       113th");
		
		
	}
	
	@Inject IEclipseContext context;
	private Text txtOutput;
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