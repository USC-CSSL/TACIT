package edu.usc.cssl.nlputils.crawlers.senate.ui;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.nlputils.common.ui.composite.from.NlputilsFormComposite;
import edu.usc.cssl.nlputils.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.nlputils.common.ui.validation.OutputPathValidation;
import edu.usc.cssl.nlputils.common.ui.views.ConsoleView;
import edu.usc.cssl.nlputils.crawlers.senate.services.AvailableRecords;
import edu.usc.cssl.nlputils.crawlers.senate.services.SenateCrawler;
import edu.usc.cssl.nlputils.crawlers.senate.ui.internal.ISenateCrawlerViewConstants;
import edu.usc.cssl.nlputils.crawlers.senate.ui.internal.SenateCrawlerViewImageRegistry;

public class SenateCrawlerView extends ViewPart implements ISenateCrawlerViewConstants {
	public static String ID = "edu.usc.cssl.nlputils.crawlers.senate.ui.senatecrawlerview";
	private ScrolledForm form;
	private FormToolkit toolkit;
	
	private OutputLayoutData outputLayout;
	private Combo cmbCongress;
	private Combo cmbSenator;
	
	private String[] allSenators;
	private String[] congresses;
	private String[] congressYears;
	
	private Date maxDate;
	private Date minDate;
	private SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
	private Button dateRange;
	
	private DateTime toDate;
	private DateTime fromDate;
	private Button limitRecords;
	private Combo cmbSort;
	private Text limitText;
	
	@Override
	public void createPartControl(Composite parent) {
		// Creates toolkit and form
		toolkit = createFormBodySection(parent, "Senate Crawler");
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(section);
		section.setExpanded(true);

		// Create a composite to hold the other widgets
		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);
	
		// Creates an empty to create a empty space
		NlputilsFormComposite.createEmptyRow(toolkit, sc);

		// Create a composite that can hold the other widgets
		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1).applyTo(client); // Align the composite section to one column
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(client);		
		GridLayout layout = new GridLayout();// Layout creation
		layout.numColumns = 2;
		
		createSenateInputParameters(client);
		NlputilsFormComposite.createEmptyRow(toolkit, client);
		outputLayout = NlputilsFormComposite.createOutputSection(toolkit, client, form.getMessageManager());
		// Add run and help button on the toolbar
		addButtonsToToolBar();	
	}
	

	private void createSenateInputParameters(Composite client) {
		Section inputParamsSection = toolkit.createSection(client, Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(inputParamsSection);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(inputParamsSection);
		inputParamsSection.setText("Input Parameters");
		
		ScrolledComposite sc = new ScrolledComposite(inputParamsSection, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);
		
		Composite sectionClient = toolkit.createComposite(inputParamsSection);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sectionClient);
		inputParamsSection.setClient(sectionClient);
		
		String[] loading = {"Loading..."};

		Label congressLabel = toolkit.createLabel(sectionClient, "Congress:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(congressLabel);
		cmbCongress = new Combo(sectionClient, SWT.FLAT);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(cmbCongress);
		toolkit.adapt(cmbCongress);
		cmbCongress.setItems(loading);
		cmbCongress.select(0);

		Label senatorLabel = toolkit.createLabel(sectionClient, "Senator:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(senatorLabel);
		cmbSenator = new Combo(sectionClient, SWT.FLAT);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(cmbSenator);
		toolkit.adapt(cmbSenator);
		cmbSenator.setItems(loading);
		cmbSenator.select(0);
	
		NlputilsFormComposite.createEmptyRow(toolkit, sectionClient);
		
		Group group = new Group(client, SWT.SHADOW_IN);
		group.setText("Date \u0026 Limit Records");

		group.setBackground(client.getBackground());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);

		dateRange = toolkit.createButton(group, "Specify Date Range", SWT.CHECK);
		dateRange.setBounds(10, 10, 10, 10);
		dateRange.pack();
		
		//NlputilsFormComposite.createEmptyRow(toolkit, group);
		final Composite dateRangeClient = toolkit.createComposite(group);
		GridDataFactory.fillDefaults().grab(true, false).span(1,1).applyTo(dateRangeClient);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(dateRangeClient);
		dateRangeClient.setEnabled(false);
		dateRangeClient.pack();

		final Composite fromClinet = toolkit.createComposite(dateRangeClient);
		GridDataFactory.fillDefaults().grab(true, false).span(1,0).applyTo(fromClinet);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(fromClinet);
		//fromClinet.setEnabled(false);
		//fromClinet.pack();
		
		final Label fromLabel = toolkit.createLabel(fromClinet, "From:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(fromLabel);
		fromDate = new DateTime(fromClinet, SWT.DATE | SWT.DROP_DOWN);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(fromDate);
		fromLabel.setEnabled(false);
		fromDate.setEnabled(false);
		
		fromDate.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(Event event) {
	            int day = fromDate.getDay();
	            int month = fromDate.getMonth() + 1;
	            int year = fromDate.getYear();
	            Date newDate = null;
	            try {
	                newDate = format.parse(day + "/" + month + "/" + year);
	            }
	            catch (ParseException e) {
	                e.printStackTrace();
	            }
	            
	            if(newDate.before(minDate) || newDate.after(maxDate))
	            {
	                Calendar cal = Calendar.getInstance();
	                cal.setTime(minDate);
	                fromDate.setMonth(cal.get(Calendar.MONTH));
	                fromDate.setDay(cal.get(Calendar.DAY_OF_MONTH));
	                fromDate.setYear(cal.get(Calendar.YEAR));
	            }	            
			}
		});
		
		final Composite toClient = toolkit.createComposite(dateRangeClient);
		GridDataFactory.fillDefaults().grab(true, false).span(1,0).applyTo(toClient);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(toClient);
		//toClient.setEnabled(false);
		//toClient.pack();
		
		final Label toLabel = toolkit.createLabel(toClient, "To:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(toLabel);
		toDate = new DateTime(toClient, SWT.DATE | SWT.DROP_DOWN);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(toDate);
		toLabel.setEnabled(false);
		toDate.setEnabled(false);
		
		toDate.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(Event event) {
	            int day = toDate.getDay();
	            int month = toDate.getMonth() + 1;
	            int year = toDate.getYear();
	            Date newDate = null;
	            try {
	                newDate = format.parse(day + "/" + month + "/" + year);
	            }
	            catch (ParseException e) {
	                e.printStackTrace();
	            }
	            
	            if(newDate.after(maxDate) || newDate.before(minDate))
	            {
	                Calendar cal = Calendar.getInstance();
	                cal.setTime(maxDate);
	                toDate.setMonth(cal.get(Calendar.MONTH));
	                toDate.setDay(cal.get(Calendar.DAY_OF_MONTH));
	                toDate.setYear(cal.get(Calendar.YEAR));
	            }
			}
		});
		
		NlputilsFormComposite.createEmptyRow(toolkit, group);
		
		limitRecords = toolkit.createButton(group, "Limit Records per Senator", SWT.CHECK);
		limitRecords.setBounds(10, 10, 10, 10);
		limitRecords.pack();
		limitRecords.addSelectionListener(new SelectionListener() {			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(!limitRecords.getSelection()){
					form.getMessageManager().removeMessage("limitText");
				}				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub			
			}
		});
				
		final Composite limitRecordsClient = toolkit.createComposite(group);
		GridDataFactory.fillDefaults().grab(true, false).span(1,1).applyTo(limitRecordsClient);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(limitRecordsClient);
		limitRecordsClient.setEnabled(false);
		limitRecordsClient.pack();
		
		final Composite sortClient = toolkit.createComposite(limitRecordsClient);
		GridDataFactory.fillDefaults().grab(true, false).span(1,1).applyTo(sortClient);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(sortClient);
		
		final Label sortLabel = toolkit.createLabel(sortClient, "Limit Records", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(sortLabel);
		sortLabel.setEnabled(false);
		cmbSort = new Combo(sortClient, SWT.FLAT);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(cmbSort);
		cmbSort.setEnabled(false);
		toolkit.adapt(cmbSort);
		cmbSort.add("From Begining");
		cmbSort.add("From End");
		cmbSort.select(0);
		
		final Composite limitClient = toolkit.createComposite(limitRecordsClient);
		GridDataFactory.fillDefaults().grab(true, false).span(1,1).applyTo(limitClient);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(limitClient);
		
		
		final Label limitLabel = toolkit.createLabel(limitClient, "# Records", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(limitLabel);
		limitLabel.setEnabled(false);
		limitText = toolkit.createText(limitClient, "",SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(limitText);
		limitText.setEnabled(false);
		
		limitText.addKeyListener(new KeyListener() {			
			@Override
			public void keyReleased(KeyEvent e) {
	             if(!(e.character>='0' && e.character<='9')) {
	            	 form.getMessageManager() .addMessage( "limitText", "Provide valid no.of.records per senator", null, IMessageProvider.ERROR);
	            	 limitText.setText(""); 
	             } else {
	            	 form.getMessageManager().removeMessage("limitText");
	             }			
			}			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub			
			}
		});
		
		
		Job loadFieldValuesJob = new Job("Loading form field values") {			
			HashMap<String, String> congressDetails = null;
			final ArrayList<String> tempCongress = new ArrayList<String>();
			final ArrayList<String> tempCongressYears = new ArrayList<String>();
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					congressDetails = AvailableRecords.getAllCongresses();
				} catch (IOException e) {
					e.printStackTrace();
				}
				Display.getDefault().syncExec(new Runnable() {
				      @Override
				      public void run() {
				    	  cmbCongress.removeAll();
				    	  for(String key : congressDetails.keySet()) {
				    		  tempCongress.add(key);
				    		  String value = congressDetails.get(key);
				    		  tempCongressYears.add(value);
				    		 
				    		  cmbCongress.add(key+" ("+ value+ ")");
				    		  
				    		  if(key.equalsIgnoreCase("All")) {
				    			  String[] tempYears = value.split("-");
				    			  Calendar cal = Calendar.getInstance();
				    			  cal.set(Integer.parseInt(tempYears[0]), 0, 1);
				    			  minDate = cal.getTime();
				    			  fromDate.setMonth(cal.get(Calendar.MONTH));
				    			  fromDate.setDay(cal.get(Calendar.DAY_OF_MONTH));
				    			  fromDate.setYear(cal.get(Calendar.YEAR));
					                
				    			  cal.set(Integer.parseInt(tempYears[1]), 11, 31);
				    			  toDate.setMonth(cal.get(Calendar.MONTH));
				    			  toDate.setDay(cal.get(Calendar.DAY_OF_MONTH));
				    			  toDate.setYear(cal.get(Calendar.YEAR));
				    			  maxDate = cal.getTime();
				    		  }
				    	  }
				    	  //cmbCongress.setItems(congresses);
				    	  cmbCongress.select(0);
				      }});		
				congresses = tempCongress.toArray(new String[0]);
				congressYears = tempCongressYears.toArray(new String[0]);
				try {
					allSenators = AvailableRecords.getAllSenators(congresses);
				} catch (IOException e2) {
					e2.printStackTrace();
				}
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
				return Status.OK_STATUS;
			}
		};
		//loadFieldValuesJob.setUser(true);
		loadFieldValuesJob.schedule();
		
		cmbCongress.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					//String selectedCongress = cmbCongress.getText().trim();
					String selectedCongress = congresses[cmbCongress.getSelectionIndex()];
					
					if (selectedCongress.equals("All")){
						cmbSenator.setItems(allSenators);
						cmbSenator.add("All Senators", 0);
						cmbSenator.add("All Democrats", 1);
						cmbSenator.add("All Republicans", 2);
						cmbSenator.add("All Independents", 3);
						cmbSenator.select(0);
					}
					else {
						cmbSenator.setItems(AvailableRecords.getSenators(selectedCongress));
						cmbSenator.add("All Senators", 0);
						cmbSenator.add("All Democrats", 1);
						cmbSenator.add("All Republicans", 2);
						cmbSenator.add("All Independents", 3);
						cmbSenator.select(0);
					}
					// set dates
					String tempYears[] = congressYears[cmbCongress.getSelectionIndex()].split("-");
					Calendar cal = Calendar.getInstance();
					cal.set(Integer.parseInt(tempYears[0]), 0, 1);
	    			minDate = cal.getTime();
	    			fromDate.setMonth(cal.get(Calendar.MONTH));
	    			fromDate.setDay(cal.get(Calendar.DAY_OF_MONTH));
	    			fromDate.setYear(cal.get(Calendar.YEAR));
		                
	    			cal.set(Integer.parseInt(tempYears[1]), 11, 31);
	    			toDate.setMonth(cal.get(Calendar.MONTH));
	    			toDate.setDay(cal.get(Calendar.DAY_OF_MONTH));
	    			toDate.setYear(cal.get(Calendar.YEAR));
	    			maxDate = cal.getTime();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				cmbSenator.select(0);
			}
		});	
		
		dateRange.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (dateRange.getSelection()) {					
					dateRangeClient.setEnabled(true);
					fromLabel.setEnabled(true);
					fromDate.setEnabled(true);
					toLabel.setEnabled(true);
					toDate.setEnabled(true);
				} else {					
					dateRangeClient.setEnabled(false);
					fromLabel.setEnabled(false);
					fromDate.setEnabled(false);
					toLabel.setEnabled(false);
					toDate.setEnabled(false);
				}
			}
		});	
		
		limitRecords.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (limitRecords.getSelection()) {					
					limitRecordsClient.setEnabled(true);
					sortLabel.setEnabled(true);
					cmbSort.setEnabled(true);
					limitLabel.setEnabled(true);
					limitText.setEnabled(true);
				} else {					
					limitRecordsClient.setEnabled(false);
					sortLabel.setEnabled(false);
					cmbSort.setEnabled(false);
					limitLabel.setEnabled(false);
					limitText.setEnabled(false);
				}
			}
		});
	}
	
	
	/**
	 * Adds "Classify" and "Help" buttons on the Naive Bayes Classifier form
	 */
	private void addButtonsToToolBar() {
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (SenateCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Crawl";
			}

			String dateFrom = "";
			String dateTo = "";
			int maxDocs = -1;
			String sortType = "Default";
			String congressNum = "-1";
			String senatorDetails = "";
			String outputDir = "";
			private boolean canProceed;
			@Override
			public void run() {
				final SenateCrawler sc = new SenateCrawler();

				final Job job = new Job("Senate Crawler") {					
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						NlputilsFormComposite.updateStatusMessage(getViewSite(), null,null);
						monitor.beginTask("Running Senate Crawler..." , 100);						
						Display.getDefault().syncExec(new Runnable() {
							
							@Override
							public void run() {
								if(congresses[cmbCongress.getSelectionIndex()].indexOf("All")!=-1) {
									congressNum = "-1";
								} else {
									congressNum = congresses[cmbCongress.getSelectionIndex()];	
								}
								senatorDetails = cmbSenator.getText();
								if (dateRange.getSelection()) {
									dateFrom = (fromDate.getMonth()+1)+"/"+fromDate.getDay()+"/"+fromDate.getYear();
									dateTo = (toDate.getMonth()+1)+"/"+toDate.getDay()+"/"+toDate.getYear();
								} else {
									dateFrom = "";
									dateTo = "";
								}
								if(limitRecords.getSelection()) {
									//sort by date : begining
									sortType = cmbSort.getSelection().equals("From Begining") ? "Default" : "Date"; 
									maxDocs = Integer.parseInt(limitText.getText());
								} else {
									maxDocs = -1;
									sortType = "Default";
								}
								outputDir = outputLayout.getOutputLabel().getText();	
							}
						});
						
						final ArrayList<Integer> allCongresses = new ArrayList<Integer>();
						for(String s: congresses) {
							if(!s.contains("All"))
								allCongresses.add(Integer.parseInt(s));
						}
							
						if(monitor.isCanceled()) 
							handledCancelRequest("Cancelled");
						try {
							monitor.subTask("Initializing");
							monitor.worked(5);
							if(monitor.isCanceled()) 
								handledCancelRequest("Cancelled");
							sc.initialize(sortType, maxDocs, Integer.parseInt(congressNum), senatorDetails, dateFrom, dateTo, outputDir, allCongresses, monitor);
							if(monitor.isCanceled()) 
								handledCancelRequest("Cancelled");
							monitor.worked(5);
														
							monitor.subTask("Crawling");
							if(monitor.isCanceled()) 
								handledCancelRequest("Cancelled");
							sc.crawl();
							if(monitor.isCanceled()) 
								handledCancelRequest("Cancelled");
							monitor.worked(50);
						} catch (NumberFormatException e) {						
							return handleException(monitor, e, "Crawling failed. Provide valid data");
						} catch (IOException e) {							
							return handleException(monitor, e, "Crawling failed. Provide valid data");
						}
						monitor.worked(100);
						monitor.done();
						ConsoleView.printlInConsoleln("Done");
						NlputilsFormComposite.updateStatusMessage(getViewSite(), "Senate crawler completed successfully", IStatus.OK);
						return Status.OK_STATUS;
					}					
				};	
				job.setUser(true);
				canProceed = canItProceed();
				if(canProceed) {
					job.schedule(); // schedule the job
				}
			}
		});

		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (SenateCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_HELP_CO));
			}

			@Override
			public String getToolTipText() {
				return "Help";
			}

			@Override
			public void run() {
			};
		});
		form.getToolBarManager().update(true);
	}
	
	private IStatus handleException(IProgressMonitor monitor, Exception e, String message) {
		monitor.done();
		System.out.println(message);
		e.printStackTrace();
		NlputilsFormComposite.updateStatusMessage(getViewSite(), message, IStatus.ERROR);
		return Status.CANCEL_STATUS;
	}
	
	private IStatus handledCancelRequest(String message) {
		NlputilsFormComposite.updateStatusMessage(getViewSite(), message, IStatus.ERROR);
		return Status.CANCEL_STATUS;
		
	}

	private boolean canItProceed() {
		if(limitRecords.getSelection()) {
			if(limitText.getText().isEmpty()) {
				form.getMessageManager() .addMessage( "limitText", "Provide valid no.of.records per senator", null, IMessageProvider.ERROR);
				return false;
			}
		}
		
		String message = OutputPathValidation.getInstance().validateOutputDirectory(outputLayout.getOutputLabel().getText(), "Output");
		if (message != null) {
			message = outputLayout.getOutputLabel().getText() + " " + message;
			form.getMessageManager().addMessage("output", message, null,IMessageProvider.ERROR);
			return false;
		} else {
			form.getMessageManager().removeMessage("output");
		}
		
		return true;
	};
	
	@Override
	public void setFocus() {
		form.setFocus();
	}
	
	/**
	 * 
	 * @param parent
	 * @param title
	 * @return - Creates a form body section for Naive Bayes Classifier
	 */
	private FormToolkit createFormBodySection(Composite parent, String title) {
		// Every interface requires a toolkit(Display) and form to store the components
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(form.getForm());
		form.setText(title);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true)
				.applyTo(form.getBody());
		return toolkit;
	}

}
