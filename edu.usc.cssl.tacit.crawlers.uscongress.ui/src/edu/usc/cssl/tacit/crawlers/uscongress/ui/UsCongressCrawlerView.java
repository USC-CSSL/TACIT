package edu.usc.cssl.tacit.crawlers.uscongress.ui;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.tacit.common.ui.validation.OutputPathValidation;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import edu.usc.cssl.tacit.crawlers.uscongress.services.AvailableRecords;
import edu.usc.cssl.tacit.crawlers.uscongress.services.UsCongressCrawler;
import edu.usc.cssl.tacit.crawlers.uscongress.ui.internal.IUsCongressCrawlerViewConstants;
import edu.usc.cssl.tacit.crawlers.uscongress.ui.internal.UsCongressCrawlerViewImageRegistry;

public class UsCongressCrawlerView extends ViewPart implements IUsCongressCrawlerViewConstants {
	public static String ID = "edu.usc.cssl.tacit.crawlers.uscongress.ui.uscongresscrawlerview";
	private ScrolledForm form;
	private FormToolkit toolkit;
	
	private OutputLayoutData outputLayout;
	private Combo sCmbCongress;
	
	private String[] allSenators;
	private String[] allRepresentatives;
	private String[] congresses;
	private String[] congressYears;
	
	private Date maxDate;
	private Date minDate;
	private SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
	private Button dateRange;
	
	private DateTime toDate;
	private DateTime fromDate;
	private Button limitRecords;
	private Text limitText;
	
	private int totalSenators;
	private int totalRepresentatives;
	private int progressSize = 100;
	private Button sortByDateYes;
	private Button sortByDateNo;
	private Table senatorTable;
	private Button removeSenatorButton;
	private ListDialog listDialog;
	private LinkedHashSet<String> senatorList;
	private LinkedHashSet<String> representativeList;
	private ArrayList<String> selectedSenators;
	private ArrayList<String> selectedRepresentatives;
	private Button addSenatorBtn;
	
	String previousSelectedCongress = "";
	String[] availabileSenators;
	String[] availableRepresentatives;
	private Button senatorButton;
	private Button representativeButton;
	private Composite senatorComposite;
	private Composite filterResultsComposite;
	private Composite representativeComposite;
	private Table representativeTable;
	private Button addRepresentativeBtn;
	private Button removeRepresentativeButton;
	private Combo rCmbCongress;
	private Button houseBtn;
	private Button senateBtn;
	private Button extensionBtn;
	private Button dailyDigestBtn;
	private Composite limitRecordsClient;
	
	@Override
	public void createPartControl(Composite parent) {
		// Creates toolkit and form
		toolkit = createFormBodySection(parent, "US Congress Crawler");
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(section);
		section.setExpanded(true);
		form.setImage(UsCongressCrawlerViewImageRegistry.getImageIconFactory().getImage(IUsCongressCrawlerViewConstants.IMAGE_US_CONGRESS_OBJ));

		// Create a composite to hold the other widgets
		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);
	
		// Creates an empty to create a empty space
		TacitFormComposite.createEmptyRow(toolkit, sc);

		// Create a composite that can hold the other widgets
		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1).applyTo(client); // Align the composite section to one column
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(client);		
		
		createSenateInputParameters(client);
		//TacitFormComposite.createEmptyRow(toolkit, client);
		outputLayout = TacitFormComposite.createOutputSection(toolkit, client, form.getMessageManager());
		// Add run and help button on the toolbar
		addButtonsToToolBar();	
	}
	

	private void createSenateInputParameters(final Composite client) {
		Group buttonComposite = new Group(client, SWT.LEFT);
		buttonComposite.setText("Member of Congress");
		buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout blayout = new GridLayout();
		blayout.numColumns = 2;
		buttonComposite.setLayout(blayout);
		senatorButton = new Button(buttonComposite, SWT.RADIO);
		senatorButton.setText("Senators");
		senatorButton.setSelection(true);
		
		senatorButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(senatorButton.getSelection()) {
					representativeComposite.setVisible(false);
					((GridData) representativeComposite.getLayoutData()).exclude = true;					
					senatorComposite.setVisible(true);
					((GridData) senatorComposite.getLayoutData()).exclude = false;
					senatorComposite.getParent().layout(true);
					client.layout(true);
					form.reflow(true);
					houseBtn.setEnabled(false);
					senateBtn.setEnabled(true);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
			}
		});

		
		representativeButton = new Button(buttonComposite, SWT.RADIO);
		representativeButton.setText("Representatives");
		representativeButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(representativeButton.getSelection()) {
					senatorComposite.setVisible(false);
					((GridData) senatorComposite.getLayoutData()).exclude = true;					
					representativeComposite.setVisible(true);
					((GridData) representativeComposite.getLayoutData()).exclude = false;
					representativeComposite.getParent().layout(true);
					client.layout(true);
					form.reflow(true);
					houseBtn.setEnabled(true);
					senateBtn.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
			}
		});
		
		Section inputParamsSection = toolkit.createSection(client, Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(inputParamsSection);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(inputParamsSection);
		inputParamsSection.setText("Input Parameters");
		
		ScrolledComposite sc = new ScrolledComposite(inputParamsSection, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);

		Composite mainComposite = toolkit.createComposite(inputParamsSection);
		sc.setContent(mainComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(mainComposite);
		inputParamsSection.setClient(mainComposite);
		
		senatorComposite = toolkit.createComposite(mainComposite);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(senatorComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 0).applyTo(senatorComposite);
		
		String[] sLoading = {"Loading..."};
		Label sCongressLabel = toolkit.createLabel(senatorComposite, "Congress:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(sCongressLabel);
		sCmbCongress = new Combo(senatorComposite, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(sCmbCongress);
		toolkit.adapt(sCmbCongress);
		sCmbCongress.setItems(sLoading);
		sCmbCongress.select(0);
		
		representativeComposite = toolkit.createComposite(mainComposite);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(representativeComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 0).applyTo(representativeComposite);

		String[] rLoading = {"Loading..."};
		Label rCongressLabel = toolkit.createLabel(representativeComposite, "Congress:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(rCongressLabel);
		rCmbCongress = new Combo(representativeComposite, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(rCmbCongress);
		toolkit.adapt(rCmbCongress);
		rCmbCongress.setItems(rLoading);
		rCmbCongress.select(0);
		
		filterResultsComposite = toolkit.createComposite(mainComposite);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(filterResultsComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 0).applyTo(filterResultsComposite);
		
		Label dummy1 = new Label(senatorComposite, SWT.NONE);
		dummy1.setText("Senators:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(dummy1);

		senatorTable = new Table(senatorComposite, SWT.BORDER | SWT.MULTI);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 3).hint(90, 50).applyTo(senatorTable);

		Composite buttonComp = new Composite(senatorComposite, SWT.NONE);
		GridLayout btnLayout = new GridLayout();
		btnLayout.marginWidth = btnLayout.marginHeight = 0;
		btnLayout.makeColumnsEqualWidth = false;
		buttonComp.setLayout(btnLayout);
		buttonComp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		
		addSenatorBtn = new Button(buttonComp, SWT.PUSH); //$NON-NLS-1$
		addSenatorBtn.setText("Add...");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(addSenatorBtn);

		addSenatorBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sHandleAdd(addSenatorBtn.getShell());
			}
		});
		addSenatorBtn.setEnabled(false);

		removeSenatorButton = new Button(buttonComp,SWT.PUSH);
		removeSenatorButton.setText("Remove...");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(removeSenatorButton);
		removeSenatorButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				for (TableItem item : senatorTable.getSelection()) {
					selectedSenators.remove(item.getText());
					item.dispose();
				}
				if(selectedSenators.size() == 0) {
					removeSenatorButton.setEnabled(false);
				}
			}
		});
		removeSenatorButton.setEnabled(false);

		Label dummy2 = new Label(representativeComposite, SWT.NONE);
		dummy2.setText("Representatives:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(dummy2);

		representativeTable = new Table(representativeComposite, SWT.BORDER | SWT.MULTI);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 3).hint(90, 50).applyTo(representativeTable);

		Composite rButtonComp = new Composite(representativeComposite, SWT.NONE);
		GridLayout rBtnLayout = new GridLayout();
		rBtnLayout.marginWidth = btnLayout.marginHeight = 0;
		rBtnLayout.makeColumnsEqualWidth = false;
		rButtonComp.setLayout(rBtnLayout);
		rButtonComp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		
		addRepresentativeBtn = new Button(rButtonComp, SWT.PUSH); //$NON-NLS-1$
		addRepresentativeBtn.setText("Add...");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(addRepresentativeBtn);

		addRepresentativeBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				rHandleAdd(addRepresentativeBtn.getShell());
			}
		});
		addRepresentativeBtn.setEnabled(false);

		removeRepresentativeButton = new Button(rButtonComp,SWT.PUSH);
		removeRepresentativeButton.setText("Remove...");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(removeRepresentativeButton);
		removeRepresentativeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				for (TableItem item : representativeTable.getSelection()) {
					selectedRepresentatives.remove(item.getText());
					item.dispose();
				}
				if(selectedRepresentatives.size() == 0) {
					removeRepresentativeButton.setEnabled(false);
				}
			}
		});
		removeRepresentativeButton.setEnabled(false);
		((GridData) representativeComposite.getLayoutData()).exclude = true; // hide this 
		
		Group limitGroup = new Group(filterResultsComposite, SWT.SHADOW_IN);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 0).applyTo(limitGroup);
		limitGroup.setText("Limit Records");
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(limitGroup);		
		
		limitRecordsClient = new Composite(limitGroup, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).span(1,1).indent(10, 10).applyTo(limitRecordsClient);
		GridLayoutFactory.fillDefaults().numColumns(5).equalWidth(false).applyTo(limitRecordsClient);
	
		final Label sectionLabel = new Label(limitRecordsClient, SWT.NONE);
		sectionLabel.setText("Section of Congressional Record:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(sectionLabel);

		extensionBtn = new Button(limitRecordsClient, SWT.CHECK);
		extensionBtn.setText("Extension of Remarks");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(extensionBtn);		
		extensionBtn.setSelection(true);
		
		senateBtn = new Button(limitRecordsClient, SWT.CHECK);
		senateBtn.setText("Senate");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(senateBtn);		
		senateBtn.setSelection(true);
		
		houseBtn = new Button(limitRecordsClient, SWT.CHECK);
		houseBtn.setText("House");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(houseBtn);
		houseBtn.setSelection(true);
		houseBtn.setEnabled(false);

		dailyDigestBtn = new Button(limitRecordsClient, SWT.CHECK);
		dailyDigestBtn.setText("Daily Digest ");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(dailyDigestBtn);		
		dailyDigestBtn.setSelection(true);
			
		limitRecords = new Button(limitRecordsClient, SWT.CHECK);
		limitRecords.setText("Limit records per congress member");	
		GridDataFactory.fillDefaults().grab(false, false).span(5, 0).applyTo(limitRecords);
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
		
		final Label sortLabel = new Label(limitRecordsClient, SWT.NONE);
		sortLabel.setText("Record Crawl Order:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(sortLabel);
		sortLabel.setEnabled(false);
		
		sortByDateYes = new Button(limitRecordsClient, SWT.RADIO);
		sortByDateYes.setText("Date (Newest First)");
		sortByDateYes.setEnabled(false);
		sortByDateYes.setSelection(true);

		sortByDateNo = new Button(limitRecordsClient, SWT.RADIO);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0).applyTo(sortByDateNo);
		sortByDateNo.setText("Random");
		sortByDateNo.setEnabled(false);

		final Label limitLabel = new Label(limitRecordsClient, SWT.NONE);
		limitLabel.setText("No of records per congress member:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(limitLabel);
		limitLabel.setEnabled(false);
		limitText = new Text(limitRecordsClient, SWT.BORDER);
		limitText.setText("1");
		GridDataFactory.fillDefaults().grab(true, false).span(4, 0).applyTo(limitText);
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
		TacitFormComposite.createEmptyRow(toolkit, limitGroup);
		
		Group dateGroup = new Group(filterResultsComposite, SWT.SHADOW_IN);
		GridDataFactory.fillDefaults().grab(true, false).span(4, 0).applyTo(dateGroup);
		dateGroup.setText("Date");
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(dateGroup);
		
		dateRange = new Button(dateGroup, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, false).span(4, 0).indent(10,10).applyTo(dateRange);
		dateRange.setText("Specify Date Range");
		
		final Composite dateRangeClient = new Composite(dateGroup, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).span(1,1).indent(10,10).applyTo(dateRangeClient);
		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(false).applyTo(dateRangeClient);
		dateRangeClient.setEnabled(false);
		dateRangeClient.pack();
		
		final Label fromLabel = new Label(dateRangeClient, SWT.NONE);
		fromLabel.setText("From:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(fromLabel);
		fromDate = new DateTime(dateRangeClient, SWT.DATE | SWT.DROP_DOWN | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(fromDate);
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
		
		final Label toLabel = new Label(dateRangeClient, SWT.NONE);
		toLabel.setText("To:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(toLabel);
		toDate = new DateTime(dateRangeClient, SWT.DATE | SWT.DROP_DOWN | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(toDate);
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
		TacitFormComposite.createEmptyRow(toolkit, dateGroup);

		
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
				    	  sCmbCongress.removeAll();
				    	  rCmbCongress.removeAll();
				    	  for(String key : congressDetails.keySet()) {
				    		  tempCongress.add(key);
				    		  String value = congressDetails.get(key);
				    		  tempCongressYears.add(value);
				    		 
				    		  sCmbCongress.add(key+" ("+ value+ ")");
				    		  rCmbCongress.add(key+" ("+ value+ ")");
				    		  
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
				    	  sCmbCongress.select(0);
				    	  rCmbCongress.select(0);
				      }});		
				congresses = tempCongress.toArray(new String[0]);
				congressYears = tempCongressYears.toArray(new String[0]);
				try {
					allSenators = AvailableRecords.getAllSenators(congresses);
					totalSenators = allSenators.length + 5;
					allRepresentatives = AvailableRecords.getAllRepresentatives(congresses);
					totalRepresentatives = allRepresentatives.length + 5;
					Display.getDefault().syncExec(new Runnable() {						
						@Override
						public void run() {
							addSenatorBtn.setEnabled(true);
							addRepresentativeBtn.setEnabled(true);
						}
					});
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		loadFieldValuesJob.schedule();
		
		sCmbCongress.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// set dates
				String tempYears[] = congressYears[sCmbCongress.getSelectionIndex()].split("-");
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
				//cmbSenator.select(0);
				
				//Empty the senatorTable
				senatorTable.removeAll();
				selectedSenators = new ArrayList<String>();
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
					sortByDateYes.setEnabled(true);
					sortByDateNo.setEnabled(true);
					sortLabel.setEnabled(true);
					limitLabel.setEnabled(true);
					limitText.setEnabled(true);
				} else {
					sortByDateYes.setEnabled(false);
					sortByDateNo.setEnabled(false);
					sortLabel.setEnabled(false);
					limitLabel.setEnabled(false);
					limitText.setEnabled(false);
				}
			}
		});
	}

	static class ArrayLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			return (String) element;
		}
	}
	
	public void processElementSelectionDialog(Shell shell) {
		ILabelProvider lp = new ArrayLabelProvider();
		listDialog = new ListDialog(shell, lp);
		listDialog.setTitle("Select the Authors from the list");
		listDialog.setMessage("Enter Author name to search");
	}
	

	private void rHandleAdd(Shell shell) {
		
		processElementSelectionDialog(shell);

		representativeList = new LinkedHashSet<String>();
		Job listRepresentatives = new Job("Retrieving representative list ...") {
			String selectedCongress = "";
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				representativeList.clear();
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						selectedCongress = congresses[rCmbCongress.getSelectionIndex()];
					}
				});
				
				try { 
					ArrayList<String> temp = new ArrayList<String>();
					temp.add(0, "All Representatives");
			    	temp.add(1, "All Democrats");
			    	temp.add(2, "All Republicans");
			    	temp.add(3, "All Independents");
					if(selectedCongress.equals("All")) {
						for(String s : allRepresentatives) 
							temp.add(s);					
					} else {
						if(previousSelectedCongress.isEmpty() || !previousSelectedCongress.equals(selectedCongress)) {
							availableRepresentatives = AvailableRecords.getRepresentatives(selectedCongress);
						}
						for(String s : availableRepresentatives) 
							temp.add(s);	
						
					}
					representativeList.addAll(temp);
					if (selectedRepresentatives != null)
						representativeList.removeAll(selectedRepresentatives);

					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							listDialog.refresh(representativeList.toArray());
						}
					});
					previousSelectedCongress = selectedCongress;
				} catch (final IOException exception) {
					ConsoleView.printlInConsole(exception.toString());
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							ErrorDialog.openError(Display.getDefault().getActiveShell(), "Problem Occurred", "Please Check your connectivity to server", new Status(IStatus.ERROR,
											CommonUiActivator.PLUGIN_ID,"Network is not reachable"));
						}
					});
				}
				return Status.OK_STATUS;
			}
		};

		listRepresentatives.schedule();
		representativeList.add("Loading...");
		listDialog.setElements(representativeList.toArray());
		listDialog.setMultipleSelection(true);
		if (listDialog.open() == Window.OK) {
			updateRepresentativeTable(listDialog.getResult());
		}
	}
	private void updateRepresentativeTable(Object[] result) {
		if (selectedRepresentatives == null) {
			selectedRepresentatives = new ArrayList<String>();
		}

		for (Object object : result) {
			selectedRepresentatives.add((String) object);
		}
		//Collections.sort(selectedSenators);
		representativeTable.removeAll();
		for (String itemName : selectedRepresentatives) {
			TableItem item = new TableItem(representativeTable, 0);
			item.setText(itemName);
			if(!removeRepresentativeButton.isEnabled()) {
				removeRepresentativeButton.setEnabled(true);
			}
		}

	}	
	
	private void sHandleAdd(Shell shell) {
		
		processElementSelectionDialog(shell);

		senatorList = new LinkedHashSet<String>();
		Job listSenators = new Job("Retrieving senator list ...") {

			String selectedCongress = "";
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				senatorList.clear();
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						selectedCongress = congresses[sCmbCongress.getSelectionIndex()];
					}
				});
				
				try { 
					ArrayList<String> temp = new ArrayList<String>();
					temp.add(0, "All Senators");
			    	temp.add(1, "All Democrats");
			    	temp.add(2, "All Republicans");
			    	temp.add(3, "All Independents");
					if(selectedCongress.equals("All")) {
						for(String s : allSenators) 
							temp.add(s);					
					} else {
						if(previousSelectedCongress.isEmpty() || !previousSelectedCongress.equals(selectedCongress)) {
							availabileSenators = AvailableRecords.getSenators(selectedCongress);
						}
						for(String s : availabileSenators) 
							temp.add(s);	
						
					}
					senatorList.addAll(temp);
					if (selectedSenators != null)
						senatorList.removeAll(selectedSenators);

					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							listDialog.refresh(senatorList.toArray());
						}
					});
					previousSelectedCongress = selectedCongress;
				} catch (final IOException exception) {
					ConsoleView.printlInConsole(exception.toString());
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							ErrorDialog.openError(Display.getDefault()
									.getActiveShell(), "Problem Occurred",
									"Please Check your connectivity to server",
									new Status(IStatus.ERROR,
											CommonUiActivator.PLUGIN_ID,
											"Network is not reachable"));

						}
					});
				}
				return Status.OK_STATUS;
			}
		};

		listSenators.schedule();
		senatorList.add("Loading...");
		listDialog.setElements(senatorList.toArray());
		listDialog.setMultipleSelection(true);
		if (listDialog.open() == Window.OK) {
			updateSenatorTable(listDialog.getResult());
		}

	}

	private void updateSenatorTable(Object[] result) {
		if (selectedSenators == null) {
			selectedSenators = new ArrayList<String>();
		}

		for (Object object : result) {
			selectedSenators.add((String) object);
		}
		//Collections.sort(selectedSenators);
		senatorTable.removeAll();
		for (String itemName : selectedSenators) {
			TableItem item = new TableItem(senatorTable, 0);
			item.setText(itemName);
			if(!removeSenatorButton.isEnabled()) {
				removeSenatorButton.setEnabled(true);
			}
		}

	}
	
	
	/**
	 * Adds "Classify" and "Help" buttons on the Naive Bayes Classifier form
	 */
	private void addButtonsToToolBar() {
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (UsCongressCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_LRUN_OBJ));
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
			ArrayList<String> congressMemberDetails = new ArrayList<String>();
			String outputDir = "";
			private boolean canProceed;
			boolean isSenate = false;
			boolean crawlSenateRecords = false;
			boolean crawlHouseRepRecords = false;
			boolean crawlDailyDigest = false;
			boolean crawlExtension = false;
			@Override
			public void run() {
				final UsCongressCrawler sc = new UsCongressCrawler();

				final Job job = new Job("US Congress Crawler") {					
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						TacitFormComposite.setConsoleViewInFocus();
						TacitFormComposite.updateStatusMessage(getViewSite(), null,null, form);						
						Display.getDefault().syncExec(new Runnable() {
							
							@Override
							public void run() {
								if(senatorButton.getSelection()) {
									if(congresses[sCmbCongress.getSelectionIndex()].indexOf("All")!=-1) {
										congressNum = "-1";
									} else {
										congressNum = congresses[sCmbCongress.getSelectionIndex()];	
									}									
									congressMemberDetails = selectedSenators;
									isSenate = true;
									
									crawlSenateRecords = senateBtn.getSelection();
								}
								else if(representativeButton.getSelection()) {
									if(congresses[rCmbCongress.getSelectionIndex()].indexOf("All")!=-1) {
										congressNum = "-1";
									} else {
										congressNum = congresses[rCmbCongress.getSelectionIndex()];	
									}									
									congressMemberDetails = selectedRepresentatives;
									crawlHouseRepRecords = houseBtn.getSelection();
								}
								crawlDailyDigest = dailyDigestBtn.getSelection();
								crawlExtension = extensionBtn.getSelection();								
								if (dateRange.getSelection()) {
									dateFrom = (fromDate.getMonth()+1)+"/"+fromDate.getDay()+"/"+fromDate.getYear();
									dateTo = (toDate.getMonth()+1)+"/"+toDate.getDay()+"/"+toDate.getYear();
								} else {
									dateFrom = "";
									dateTo = "";
								}
								if(limitRecords.getSelection()) {
									sortType = sortByDateNo.getSelection() ? "Default" : "Date"; 
									maxDocs = Integer.parseInt(limitText.getText());
								} else {
									maxDocs = -1;
									sortType = "Date";
								}
								outputDir = outputLayout.getOutputLabel().getText();	
							}
						});
						
						if(congressMemberDetails.contains("All Senators") && congressNum.equals("-1")) { // all senators and all congresses
							progressSize = (totalSenators * congresses.length) + 50;
						} else {
							int count = 1;
							if(congressNum.equals("-1")) {
								if(congressMemberDetails.contains("All Democrats")) {
									progressSize = (20 * congresses.length) + 50; // on an average of 20 democrats
									count++;
								}
								if(congressMemberDetails.contains("All Republicans")) {
									progressSize+= (20 * congresses.length) + 50;
									count++;
								}
								if(congressMemberDetails.contains("All Independents")) {
									progressSize+= (20 * congresses.length) + 50;
									count++;
								}
								progressSize+= ((congressMemberDetails.size() - count)+1 * congresses.length) + 50; // considering none of "All" selected
							} else {
								if(congressMemberDetails.contains("All Democrats")) {
									progressSize = 100 + 50; // on an average of 20 democrats
									count++;
								}
								if(congressMemberDetails.contains("All Republicans")) {
									progressSize+= 100 + 50;
									count++;
								}
								if(congressMemberDetails.contains("All Independents")) {
									progressSize+=  100 + 50;
									count++;
								}
								progressSize+= ((congressMemberDetails.size() - count)+1 * 10) + 50; // considering none of "All" selected								
							}
						}
						monitor.beginTask("Running US Congress Crawler..." , progressSize);
						TacitFormComposite.writeConsoleHeaderBegining("US Congress Crawler started ");						
						
						final ArrayList<Integer> allCongresses = new ArrayList<Integer>();
						for(String s: congresses) {
							if(!s.contains("All"))
								allCongresses.add(Integer.parseInt(s));
						}
							
						if(monitor.isCanceled()) {
							TacitFormComposite.writeConsoleHeaderBegining("<terminated> US Congress Crawler");
							return handledCancelRequest("Cancelled");
						}
						try {
							monitor.subTask("Initializing...");
							monitor.worked(10);
							if(monitor.isCanceled()) {
								TacitFormComposite.writeConsoleHeaderBegining("<terminated> US Congress Crawler");
								return handledCancelRequest("Cancelled");
							}
							sc.initialize(sortType, maxDocs, Integer.parseInt(congressNum), congressMemberDetails, dateFrom, dateTo, outputDir, allCongresses, monitor, progressSize - 30, isSenate, crawlSenateRecords, crawlHouseRepRecords, crawlDailyDigest, crawlExtension);
							if(monitor.isCanceled()) {
								TacitFormComposite.writeConsoleHeaderBegining("<terminated> US Congress Crawler");
								return handledCancelRequest("Cancelled");
							}
							monitor.worked(10);
														
							monitor.subTask("Crawling...");
							if(monitor.isCanceled()) {
								TacitFormComposite.writeConsoleHeaderBegining("<terminated> US Congress Crawler");
								return handledCancelRequest("Cancelled");
							}
							sc.crawl();
							if(monitor.isCanceled()) {
								TacitFormComposite.writeConsoleHeaderBegining("<terminated> US Congress Crawler");
								return handledCancelRequest("Cancelled");
							}
							monitor.worked(10);
						} catch (NumberFormatException e) {						
							return handleException(monitor, e, "Crawling failed. Provide valid data");
						} catch (IOException e) {							
							return handleException(monitor, e, "Crawling failed. Provide valid data");
						} catch(Exception e) {
							return handleException(monitor, e, "Crawling failed. Provide valid data");
						}
						monitor.worked(100);
						monitor.done();
						ConsoleView.printlInConsoleln("US Congress crawler completed successfully.");
						ConsoleView.printlInConsoleln("Total no.of.files downloaded : " + sc.totalFilesDownloaded);
						ConsoleView.printlInConsoleln("Done");
						TacitFormComposite.updateStatusMessage(getViewSite(), "US Congress crawler completed successfully.", IStatus.OK, form);
						TacitFormComposite.writeConsoleHeaderBegining("<terminated> US Congress Crawler");
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

		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (UsCongressCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_HELP_CO));
			}

			@Override
			public String getToolTipText() {
				return "Help";
			}
			@Override
			public void run() {
				PlatformUI
						.getWorkbench()
						.getHelpSystem()
						.displayHelp(
								"edu.usc.cssl.tacit.crawlers.uscongress.ui.uscongress");
			}			
		};
		mgr.add(helpAction);
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(helpAction,
						"edu.usc.cssl.tacit.crawlers.uscongress.ui.uscongress");
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(form,
						"edu.usc.cssl.tacit.crawlers.uscongress.ui.uscongress");
		form.getToolBarManager().update(true);
	}
	
	private IStatus handleException(IProgressMonitor monitor, Exception e, String message) {
		monitor.done();
		System.out.println(message);
		e.printStackTrace();
		TacitFormComposite.updateStatusMessage(getViewSite(), message, IStatus.ERROR, form);
		TacitFormComposite.writeConsoleHeaderBegining("<terminated> US Congress Crawler");
		return Status.CANCEL_STATUS;
	}
	
	private IStatus handledCancelRequest(String message) {
		TacitFormComposite.updateStatusMessage(getViewSite(), message, IStatus.ERROR, form);
		ConsoleView.printlInConsoleln("US Congress crawler cancelled.");
		TacitFormComposite.writeConsoleHeaderBegining("<terminated> US Congress Crawler");
		return Status.CANCEL_STATUS;
		
	}

	private boolean canItProceed() {
		if(senatorButton.getSelection()) {
			if(!senateBtn.getSelection() && !extensionBtn.getSelection() && !dailyDigestBtn.getSelection()) {
				form.getMessageManager() .addMessage( "section", "Provide select atleast one congressional section", null, IMessageProvider.ERROR);
				return false;
			} else
				form.getMessageManager().removeMessage("section");
		}
		if(representativeButton.getSelection()) {
			if(!senateBtn.getSelection() && !extensionBtn.getSelection() && !dailyDigestBtn.getSelection()) {
				form.getMessageManager() .addMessage( "section", "Provide select atleast one congressional section", null, IMessageProvider.ERROR);
				return false;
			} else
				form.getMessageManager().removeMessage("section");
		}
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
