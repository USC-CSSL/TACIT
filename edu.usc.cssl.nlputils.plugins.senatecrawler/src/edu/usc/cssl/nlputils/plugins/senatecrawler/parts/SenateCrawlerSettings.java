/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */ 
package edu.usc.cssl.nlputils.plugins.senatecrawler.parts;

import java.io.IOException;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Combo;

import edu.usc.cssl.nlputils.plugins.senatecrawler.process.AvailableRecords;
import edu.usc.cssl.nlputils.plugins.senatecrawler.process.SenateCrawler;
import edu.usc.cssl.nlputils.utilities.Log;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.osgi.framework.FrameworkUtil;

public class SenateCrawlerSettings {
	private Text txtMaxDocs;
	private Text txtOutput;
	String[] allSenators;
	String[] congresses;
	
	@Inject
	public SenateCrawlerSettings() {
		
	}

	// IStylingEngine is injected
	@Inject IStylingEngine style_engine;
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		
		
		
		
//		appendLog("Loading Senators...");
//		String[] senatorsArray = null;
//
//		try {
//			senatorsArray = AvailableRecords.getAllSenators(congresses);
//		} catch (IOException e2) {
//			e2.printStackTrace();
//		}
//
//		final String[] allSenators = senatorsArray;
		
//		appendLog("Loading Complete.");
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setSize(588, 300);
		composite.setLocation(0, 0);
		
		Label header = new Label(composite, SWT.NONE);
		header.setImage(ImageDescriptor.createFromURL(
				FileLocator.find(FrameworkUtil.getBundle(this.getClass()),
						new Path("plugin_icon/icon.png"), null)).createImage());
		header.setBounds(10, 0, 161, 40);
		
		Label lblSenator = new Label(composite, SWT.NONE);
		lblSenator.setBounds(10, 66, 60, 20);
		lblSenator.setText("Congress");
		
		final DateTime dateTime = new DateTime(composite, SWT.BORDER);
		dateTime.setEnabled(false);
		dateTime.setBounds(89, 167, 101, 24);
		
		final DateTime dateTime_1 = new DateTime(composite, SWT.BORDER);
		dateTime_1.setEnabled(false);
		dateTime_1.setBounds(288, 167, 101, 24);
		
		Label lblToDate = new Label(composite, SWT.NONE);
		lblToDate.setBounds(245, 167, 21, 20);
		lblToDate.setText("To");
		
		txtMaxDocs = new Text(composite, SWT.BORDER);
		txtMaxDocs.setToolTipText("Enter the maximum number of records to extract for each senator");
		txtMaxDocs.setEnabled(false);
		txtMaxDocs.setBounds(189, 219, 40, 21);
		
		Label lblFrom = new Label(composite, SWT.NONE);
		lblFrom.setBounds(26, 167, 35, 20);
		lblFrom.setText("From");

		final Combo cmbSenator = new Combo(composite, SWT.NONE);
		cmbSenator.setBounds(89, 100, 300, 23);
//		cmbSenator.setItems(allSenators);
//		cmbSenator.add("All Senators", 0);
//		cmbSenator.add("All Democrats", 1);
//		cmbSenator.add("All Republicans", 2);
//		cmbSenator.add("All Independents", 3);
		String[] loading = {"Loading..."};
		cmbSenator.setItems(loading);
		cmbSenator.select(0);
		
		final Combo cmbCongress = new Combo(composite, SWT.NONE);
		cmbCongress.setBounds(89, 66, 101, 20);
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
						
						/* Set date time*/
						String congress = cmbCongress.getText().replaceAll("\u00A0", "").trim();
						int cNum = -1;
						if (!congress.equals("All")){
							cNum = Integer.parseInt(congress);
							cNum = 2*(cNum - 101);
							dateTime.setYear(1989+cNum);
							dateTime.setDay(3);
							dateTime.setMonth(0);
							dateTime_1.setYear(1990+cNum);
							dateTime_1.setDay(3);
							dateTime_1.setMonth(0);
						}
						
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				cmbSenator.select(0);
			}
		});
		//combo.setItems(new String[] {"All", "113"});
		cmbCongress.setItems(loading);
		cmbCongress.select(0);
		
		Label lblSenator_1 = new Label(composite, SWT.NONE);
		lblSenator_1.setBounds(10, 100, 55, 20);
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
		btnDateRange.setBounds(9, 142, 93, 16);
		btnDateRange.setText("Date Range");
		
		
		
		Button btnExtract = new Button(composite, SWT.NONE);
		btnExtract.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				int maxDocs = -1;
				if (!txtMaxDocs.getText().equals(""))
					maxDocs = Integer.parseInt(txtMaxDocs.getText().trim());
				final SenateCrawler senateCrawler = new SenateCrawler();
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
				
				int cNum = -1;
				if (!congress.equals("All"))
					cNum = Integer.parseInt(congress);
				
				senateCrawler.initialize(maxDocs, cNum, senator, dateFrom, dateTo, txtOutput.getText());
				
				// Creating a new Job to do crawling so that the UI will not freeze
				Job job = new Job("Crawler Job"){
					protected IStatus run(IProgressMonitor monitor){ 
					
						long startTime = System.currentTimeMillis();
						appendLog("PROCESSING...(Senate Crawler)");
						try {
							senateCrawler.crawl();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						appendLog("Crawling completed in "+(System.currentTimeMillis()-startTime)/(float)1000+" seconds");
						appendLog("DONE (Senate Crawler)");
						
					return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				job.schedule();

			}
		});
		btnExtract.setBounds(10, 320, 75, 25);
		btnExtract.setText("Crawl");
		shell.setDefaultButton(btnExtract);
		
		Label lblOutput = new Label(composite, SWT.NONE);
		lblOutput.setBounds(10, 261, 80, 20);
		lblOutput.setText("Output Path");
		
		txtOutput = new Text(composite, SWT.BORDER);
		txtOutput.setBounds(100, 261, 258, 21);
		
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
		button.setBounds(358, 261, 40, 25);
		button.setText("...");
		
		final Button btnLimitRecords = new Button(composite, SWT.CHECK);
		btnLimitRecords.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnLimitRecords.getSelection())
					txtMaxDocs.setEnabled(true);
				else
					txtMaxDocs.setEnabled(false);
			}
		});
		btnLimitRecords.setBounds(10, 219, 175, 20);
		btnLimitRecords.setText("Limit Records per Senator");
		
		Label lblNewLabel = new Label(composite, SWT.BORDER | SWT.SHADOW_NONE);
		lblNewLabel.setBounds(433, 24, 175, 320);
		lblNewLabel.setText("\nYear\t\t\t\tCongress\r\n----------------------------------\r\n1989-1990\t-|-\t101st\r\n1991-1992\t-|-\t102nd\r\n1993-1994\t-|-\t103rd\r\n1995-1996\t-|-\t104th\r\n1997-1998\t-|-\t105th\r\n1999-2000\t-|-\t106th\r\n2001-2002\t-|-\t107th\r\n2003-2004\t-|-\t108th\r\n2005-2006\t-|-\t109th\r\n2007-2008\t-|-\t110th\r\n2009-2010\t-|-\t111th\r\n2011-2012\t-|-\t112th\r\n2013-2014\t-|-\t113th");
		
		
		// Creating a new Job to fetch data for populating the drop down menus so that the UI will not freeze
		Job job2 = new Job("Fetch Job"){
			protected IStatus run(IProgressMonitor monitor){ 
				
				appendLog("Loading Congresses...");
				String[] cgrss = null;
				
				try {
					 cgrss = AvailableRecords.getAllCongresses();
				} catch (IOException e) {
					e.printStackTrace();
				}
				congresses = cgrss;
				
				Display.getDefault().asyncExec(new Runnable() {
				      @Override
				      public void run() {
				    	cmbCongress.setItems(congresses);
				  		cmbCongress.select(0);
				      }});
				
				appendLog("Loading Senators...");
				String[] senatorsArray = null;

				try {
					senatorsArray = AvailableRecords.getAllSenators(congresses);
				} catch (IOException e2) {
					e2.printStackTrace();
				}

				allSenators = senatorsArray;
				// Async callback to access UI elements 
				Display.getDefault().asyncExec(new Runnable() {
				      @Override
				      public void run() {	
				    	cmbSenator.setItems(allSenators);
				  		cmbSenator.add("All Senators", 0);
				  		cmbSenator.add("All Democrats", 1);
				  		cmbSenator.add("All Republicans", 2);
				  		cmbSenator.add("All Independents", 3);
				  		cmbSenator.select(0);
				      }
				});
				
				appendLog("Loading Complete.");
			return Status.OK_STATUS;
			}
		};
		job2.setUser(true);
		job2.schedule();
	}
	
	@Inject IEclipseContext context;
	private void appendLog(String message){
		Log.append(context, message);
	}
}