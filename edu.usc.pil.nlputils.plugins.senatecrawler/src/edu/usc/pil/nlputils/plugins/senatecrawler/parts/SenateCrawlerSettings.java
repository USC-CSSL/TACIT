 
package edu.usc.pil.nlputils.plugins.senatecrawler.parts;

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
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Combo;

import edu.usc.pil.nlputils.plugins.senatecrawler.process.AvailableRecords;
import edu.usc.pil.nlputils.plugins.senatecrawler.process.SenateCrawler;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

public class SenateCrawlerSettings {
	private Text txtMaxDocs;
	@Inject
	public SenateCrawlerSettings() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		appendLog("Loading Congresses...");
		String[] congresses = null;
		try {
			 congresses = AvailableRecords.getAllCongresses();
		} catch (IOException e) {
			e.printStackTrace();
		}
		appendLog("Loading Senators...");
		String[] senatorsArray = null;
		try {
			senatorsArray = AvailableRecords.getAllSenators(congresses);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		final String[] allSenators = senatorsArray;
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLocation(-87, -10);
		
		Label lblSenator = new Label(composite, SWT.NONE);
		lblSenator.setBounds(10, 36, 55, 15);
		lblSenator.setText("Congress");
		
		final DateTime dateTime = new DateTime(composite, SWT.BORDER);
		dateTime.setEnabled(false);
		dateTime.setBounds(51, 133, 80, 24);
		
		final DateTime dateTime_1 = new DateTime(composite, SWT.BORDER);
		dateTime_1.setEnabled(false);
		dateTime_1.setBounds(206, 133, 80, 24);
		
		Label lblToDate = new Label(composite, SWT.NONE);
		lblToDate.setBounds(179, 142, 21, 15);
		lblToDate.setText("To");
		
		Label lblMaximumRecordsPer = new Label(composite, SWT.NONE);
		lblMaximumRecordsPer.setBounds(10, 193, 182, 15);
		lblMaximumRecordsPer.setText("Maximum Records per Senator");
		
		txtMaxDocs = new Text(composite, SWT.BORDER);
		txtMaxDocs.setText("10");
		txtMaxDocs.setBounds(206, 187, 80, 21);
		
		Label lblFrom = new Label(composite, SWT.NONE);
		lblFrom.setBounds(10, 142, 35, 15);
		lblFrom.setText("From");

		final Combo cmbSenator = new Combo(composite, SWT.NONE);
		cmbSenator.setBounds(89, 67, 91, 23);
		cmbSenator.setItems(allSenators);
		cmbSenator.add("Any Senator", 0);
		cmbSenator.select(0);
		
		final Combo cmbCongress = new Combo(composite, SWT.NONE);
		cmbCongress.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String selectedCongress = cmbCongress.getText().trim();
					if (selectedCongress.equals("All")){
						cmbSenator.add("Any Senator", 0);
						cmbSenator.setItems(allSenators);
						cmbSenator.select(0);
					}
					else
						cmbSenator.setItems(AvailableRecords.getSenators(selectedCongress));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				cmbSenator.select(0);
			}
		});
		//combo.setItems(new String[] {"All", "113"});
		cmbCongress.setItems(congresses);
		cmbCongress.setBounds(89, 33, 91, 23);
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
		
		
		Button btnExtract = new Button(composite, SWT.NONE);
		btnExtract.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				int maxDocs = Integer.parseInt(txtMaxDocs.getText().trim());
				SenateCrawler senateCrawler = new SenateCrawler();
				// Injecting the context into Senatecrawler object so that the appendLog function can modify the Context Parameter consoleMessage
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(senateCrawler,iEclipseContext);

				String dateFrom ="";
				String dateTo = "";
				
				if (btnDateRange.getSelection()){
					dateFrom = (dateTime.getMonth()+1)+"/"+dateTime.getDay()+"/"+dateTime.getYear();
					dateTo = (dateTime_1.getMonth()+1)+"/"+dateTime_1.getDay()+"/"+dateTime_1.getYear();
				}
				String senator = cmbSenator.getText().replaceAll("\u00A0", "").trim();
				String congress = cmbCongress.getText().replaceAll("\u00A0", "").trim();
				
				try {
					
					long startTime = System.currentTimeMillis();
										
					if (senator.equals("Any Senator")){
						if (congress.equals("All")){
							System.out.println(maxDocs+", "+dateFrom+", "+dateTo+", "+txtOutput.getText());
							senateCrawler.initialize(maxDocs, dateFrom,dateTo, txtOutput.getText());
						}
						else{
							System.out.println(maxDocs+", "+congress+", null, "+dateFrom+", "+dateTo+", "+txtOutput.getText());
							senateCrawler.initialize(maxDocs, Integer.parseInt(congress), null, dateFrom,dateTo,txtOutput.getText());
						}
					} else {
						int cNum = -1;
						if (!congress.equals("All"))
							cNum = Integer.parseInt(congress);
						System.out.println(maxDocs+", "+congress+", "+senator+", "+dateFrom+", "+dateTo+", "+txtOutput.getText());
						senateCrawler.initialize(maxDocs, cNum, senator, dateFrom,dateTo,txtOutput.getText());
					}
					appendLog("Extraction completed in "+(System.currentTimeMillis()-startTime)/(float)1000+" seconds");
				} catch (IOException e1) {
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
		txtOutput.setBounds(104, 225, 182, 21);
		
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
		button.setBounds(286, 221, 21, 25);
		button.setText("...");
		
		
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