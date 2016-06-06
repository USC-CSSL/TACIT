package edu.usc.cssl.tacit.crawlers.americanpresidency.ui;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CMDataType;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.Corpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import edu.usc.cssl.tacit.crawlers.americanpresidency.services.AmericanPresidencyCrawler;
import edu.usc.cssl.tacit.crawlers.americanpresidency.ui.internal.AmericanPresidencyCrawlerViewImageRegistry;
import edu.usc.cssl.tacit.crawlers.americanpresidency.ui.internal.IAmericanPresidencyCrawlerViewConstants;

public class AmericanPresidencyCrawlerView  extends ViewPart implements IAmericanPresidencyCrawlerViewConstants{
	public static String ID = "edu.usc.cssl.tacit.crawlers.americanpresidency.ui.view1";
	
	private Button searchButton;
	private ScrolledForm form;
	private FormToolkit toolkit;
	private Button browseButton;

	private Button andButton;
	private Button orButton;
	private Button notButton;
	private Button checkboxSearch1;
	private Button checkboxSearch2;
	private Button checkboxBrowse1;
	private Button checkboxBrowse2;
	private Button dateCheckBrowse;
	private Button dateCheckSearch;
	
	private Composite browseComposite;
	private Composite searchComposite;

	private Text searchText1;
	private Composite commonsearchComposite;
	private Combo selectPresidentSearch;	
	private Combo selectPresidentBrowse;	
	private Combo selectDocumentSearch;	
	private Combo selectDocumentBrowse;
	private Text searchText2;
	private Text corpusNameTxt;
	private DateTime toDate;
	private DateTime fromDate;
	private DateTime date;
	Corpus americanPresidencyCorpus;
	
	String presidentNames[] = {"President","George Washington","John Adams","Thomas Jefferson","James Madison","James Monroe","John Quincy Adams","Andrew Jackson","Martin van Buren","William Henry Harrison","John Tyler","James K. Polk","Zachary Taylor","Millard Fillmore","Franklin Pierce","James Buchanan","Abraham Lincoln","Andrew Johnson","Ulysses S. Grant","Rutherford B. Hayes","James A. Garfield","Chester A. Arthur","Grover Cleveland","Benjamin Harrison","Grover Cleveland","William McKinley","Theodore Roosevelt","William Howard Taft","Woodrow Wilson","Warren G. Harding","Calvin Coolidge","Herbert Hoover","Franklin D. Roosevelt","Harry S. Truman","Dwight D. Eisenhower","John F. Kennedy","Lyndon B. Johnson","Richard Nixon","Gerald R. Ford","Jimmy Carter","Ronald Reagan","George Bush","William J. Clinton","George W. Bush","Barack Obama"};
	String documentTypes[] = {"Document Category","Oral: Address - Inaugural","Oral: Address - to Congress (non SOTU)","Oral: Address - State of the Union","Oral: Address - major to the Nation","Oral: Address - Farewell","Oral: Address - Saturday Radio","Oral: Address - Fireside Chats","Oral: Address  - \"Inaugural\" (Accidental Presidents)","Oral: Address - at College Commencements","Oral: Address - to the UN General Assembly","Oral: Address - to Foreign Legislatures","Oral: Address - Nomination Acceptance","Oral: Remarks - (non categorized)","Oral: Remarks - Toasts","Oral: Remarks - Bill Signings","Oral: Remarks - Bill Vetos","Oral: News Conferences","Oral: News Conferences - Joint","Debates: General Election","Oral: Remarks - Campaign Fundraiser","Oral: Remarks - regarding Executive Nominations","Oral: Remarks - regarding Executive Appointments","Oral: Remarks - regarding Resignations","Written: Messages - (non categorized)","Written: Messages -  to Congress","Written: Messages - Annual Messages (written SOTU)","Written: Messages - Veto Messages","Written: Messages - Pocket Veto Messages","Written: Messages - Budget Messages","Written: Messages -  Farewell Addresses","Written: Memorandums","Written: Memorandums - Pocket Vetos","Written: Memorandums - Determinations","Written: Memorandums - Diplomatic - Memo of Understanding","Written: Executive Orders","Written: Proclamations","Written: Statements - (non categorized)","Written: Statements - Signing Statements","Written: Statements - Veto Statements","Written: Letters - (non categorized)","Written: Letters - to Congress","Written: Notices","Written: Telegrams - Lincoln (Civil War)","E.O.P.: Press Briefings","OMB: Statements of Administration Policy","Debates: Vice-Presidential","Debates: Primary Elections-Democratic Party","Debates: Primary Elections-Republican Party","Party Platforms"};
	
	HashMap<Integer, String> documentCategoryMap = new HashMap<Integer, String>();
	{
		documentCategoryMap.put(0, "");
		documentCategoryMap.put(1, "1101");
		documentCategoryMap.put(2, "1102");
		documentCategoryMap.put(3, "1103");
		documentCategoryMap.put(4, "1104");
		documentCategoryMap.put(5, "1105");	
		documentCategoryMap.put(6, "1107");	
		documentCategoryMap.put(7, "1108");	
		documentCategoryMap.put(8, "1110");	
		documentCategoryMap.put(9, "1113");	
		documentCategoryMap.put(10, "1118");	
		documentCategoryMap.put(11, "1119");	
		documentCategoryMap.put(12, "1120");	
		documentCategoryMap.put(13, "1128");	
		documentCategoryMap.put(14, "1133");	
		documentCategoryMap.put(15, "1135");	
		documentCategoryMap.put(16, "1136");	
		documentCategoryMap.put(17, "1137");	
		documentCategoryMap.put(18, "1139");
		documentCategoryMap.put(19, "1141");	
		documentCategoryMap.put(20, "1157");	
		documentCategoryMap.put(21, "1160");
		documentCategoryMap.put(22, "1160");	
		documentCategoryMap.put(23, "1161");	
		documentCategoryMap.put(24, "1162");	
		documentCategoryMap.put(25, "2100");	
		documentCategoryMap.put(26, "2110");	
		documentCategoryMap.put(27, "2111");	
		documentCategoryMap.put(28, "2113");	
		documentCategoryMap.put(29, "2114");	
		documentCategoryMap.put(30, "2115");	
		documentCategoryMap.put(31, "2165");	
		documentCategoryMap.put(32, "2200");	
		documentCategoryMap.put(33, "2201");	
		documentCategoryMap.put(34, "2202");	
		documentCategoryMap.put(35, "2210");	
		documentCategoryMap.put(36, "2300");	
		documentCategoryMap.put(37, "2400");	
		documentCategoryMap.put(38, "2500");	
		documentCategoryMap.put(39, "2510");	
		documentCategoryMap.put(40, "2511");	
		documentCategoryMap.put(41, "2600");	
		documentCategoryMap.put(42, "2610");	
		documentCategoryMap.put(43, "2710");	
		documentCategoryMap.put(44, "2800");	
		documentCategoryMap.put(45, "3001");	
		documentCategoryMap.put(46, "3500");	
		documentCategoryMap.put(47, "5501");	
		documentCategoryMap.put(48, "5502");	
		documentCategoryMap.put(49, "5503");	
		documentCategoryMap.put(50, "8000");			
	}
	HashMap<Integer, String> presidentNameMap = new HashMap<Integer, String>();
	{
		presidentNameMap.put(0, "");
		presidentNameMap.put(1, "1");
		presidentNameMap.put(2, "2");
		presidentNameMap.put(3, "3");
		presidentNameMap.put(4, "4");
		presidentNameMap.put(5, "5");	
		presidentNameMap.put(6, "6");	
		presidentNameMap.put(7, "7");	
		presidentNameMap.put(8, "8");	
		presidentNameMap.put(9, "9");	
		presidentNameMap.put(10, "10");	
		presidentNameMap.put(11, "11");	
		presidentNameMap.put(12, "12");	
		presidentNameMap.put(13, "13");	
		presidentNameMap.put(14, "14");	
		presidentNameMap.put(15, "15");	
		presidentNameMap.put(16, "16");	
		presidentNameMap.put(17, "17");	
		presidentNameMap.put(18, "18");
		presidentNameMap.put(19, "19");	
		presidentNameMap.put(20, "20");	
		presidentNameMap.put(21, "21");
		presidentNameMap.put(22, "22");	
		presidentNameMap.put(23, "23");	
		presidentNameMap.put(24, "24");	
		presidentNameMap.put(25, "25");	
		presidentNameMap.put(26, "26");	
		presidentNameMap.put(27, "27");	
		presidentNameMap.put(28, "28");	
		presidentNameMap.put(29, "29");	
		presidentNameMap.put(30, "30");	
		presidentNameMap.put(31, "31");	
		presidentNameMap.put(32, "32");	
		presidentNameMap.put(33, "33");	
		presidentNameMap.put(34, "34");	
		presidentNameMap.put(35, "35");	
		presidentNameMap.put(36, "36");	
		presidentNameMap.put(37, "37");	
		presidentNameMap.put(38, "38");	
		presidentNameMap.put(39, "39");	
		presidentNameMap.put(40, "40");	
		presidentNameMap.put(41, "41");	
		presidentNameMap.put(42, "42");	
		presidentNameMap.put(43, "43");	
		presidentNameMap.put(44, "44");			
	}
	
	@Override
	public void createPartControl(Composite parent) {
		toolkit = createFormBodySection(parent, "American Presidency Documents Crawler");
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(section);
		section.setExpanded(true);
		form.setImage(AmericanPresidencyCrawlerViewImageRegistry.getImageIconFactory().getImage(IAmericanPresidencyCrawlerViewConstants.IMAGE_AMERICAN_PRESIDENCY_OBJ));

		// Create a composite to hold the other widgets
		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(sc);
	
		// Creates an empty to create a empty space
		TacitFormComposite.createEmptyRow(toolkit, sc);

		// Create a composite that can hold the other widgets
		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1).applyTo(client); // Align the composite section to one column
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(client);		
		
		createCrawlInputParameters(toolkit, client);
		//outputLayout = TacitFormComposite.createOutputSection(toolkit, client, form.getMessageManager());
		corpusNameTxt = TacitFormComposite.createCorpusSection(toolkit, client, form.getMessageManager());
		// Add run and help button on the toolbar
		addButtonsToToolBar();	
	}
	
	private void createCrawlInputParameters(final FormToolkit toolkit, final Composite parent) {
		
		Group buttonComposite = new Group(parent, SWT.LEFT);
		buttonComposite.setText("Crawl");
		buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		buttonComposite.setLayout(layout);

		
		
		searchButton = new Button(buttonComposite, SWT.RADIO);
		searchButton.setText("Search");
		searchButton.setSelection(true);
		searchButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(searchButton.getSelection()) {
					
					// hide - search 
					browseComposite.setVisible(false);
					((GridData) browseComposite.getLayoutData()).exclude = true;
					//searchComposite.getParent().layout(true);					
					// show - trend
					searchComposite.setVisible(true);
					((GridData) searchComposite.getLayoutData()).exclude = false;
					searchComposite.getParent().layout(true);		
					parent.layout(true);
					form.reflow(true);

				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
			}
		});
		
		browseButton = new Button(buttonComposite, SWT.RADIO);
		browseButton.setText("Browse");
		browseButton.setSelection(false);
		browseButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(browseButton.getSelection()) {
					//hide - trend 
					searchComposite.setVisible(false);
					((GridData) searchComposite.getLayoutData()).exclude = true;
					//trendingDataComposite.getParent().layout(true);
				
					// show - search
					browseComposite.setVisible(true);
					((GridData) browseComposite.getLayoutData()).exclude = false;
					browseComposite.getParent().layout(true);
					parent.layout(true);
					toolkit.paintBordersFor(form.getBody());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
			}
		});

		// create input parameters section
		// main section
		Section inputParamsSection = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(inputParamsSection);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(inputParamsSection);
		inputParamsSection.setText("Input Details");
		
		ScrolledComposite sc = new ScrolledComposite(inputParamsSection, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(sc);
			
		Composite mainComposite = toolkit.createComposite(inputParamsSection);
		sc.setContent(mainComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(mainComposite);
		inputParamsSection.setClient(mainComposite);
		

		
		//Gerneal search parameters
		searchComposite = toolkit.createComposite(mainComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(searchComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(searchComposite);
		
		commonsearchComposite = toolkit.createComposite(searchComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(commonsearchComposite);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(commonsearchComposite);
		
		TacitFormComposite.createEmptyRow(toolkit, searchComposite);
		
		// split it into 3		
		Group filterResultsGroup = new Group(searchComposite, SWT.LEFT);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(filterResultsGroup);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(true).applyTo(filterResultsGroup);
		filterResultsGroup.setText("Filter Results");
		TacitFormComposite.createEmptyRow(toolkit, filterResultsGroup);
		
		TacitFormComposite.createEmptyRow(toolkit, searchComposite);
		
		Label searchLabel1 = new Label(commonsearchComposite, SWT.NONE);
		searchLabel1.setText("Search Term 1:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(searchLabel1);
		searchText1 = new Text(commonsearchComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(searchText1);	
		searchText1.setMessage("Enter a search term");
		

		Group operatorButtonComposite = new Group(commonsearchComposite, SWT.LEFT);
		GridDataFactory.fillDefaults().span(3, 0).indent(0,10).applyTo(operatorButtonComposite);
		//operatorButtonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout operatorLayout = new GridLayout();
		operatorLayout.numColumns = 3;
		operatorButtonComposite.setLayout(operatorLayout);
	
		
		andButton = new Button(operatorButtonComposite, SWT.RADIO);
		andButton.setText("And");
		andButton.setSelection(true);
		
		orButton = new Button(operatorButtonComposite, SWT.RADIO);
		orButton.setText("Or");
		orButton.setSelection(false);
		
		notButton = new Button(operatorButtonComposite, SWT.RADIO);
		notButton.setText("Not");
		notButton.setSelection(false);
			
		TacitFormComposite.createEmptyRow(toolkit, commonsearchComposite);
		
		Label searchLabel2 = new Label(commonsearchComposite, SWT.NONE);
		searchLabel2.setText("Search Term 2:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(searchLabel2);
		searchText2 = new Text(commonsearchComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(searchText2);	
		searchText2.setMessage("Enter a search term");			

		dateCheckSearch = new Button(filterResultsGroup, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 0).indent(10, 0).applyTo(dateCheckSearch);
		dateCheckSearch.setText("Specify Date Range");
		
		
		
		Composite dateComposite1 = new Composite(filterResultsGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(dateComposite1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(dateComposite1);
		
		final Label fromLabel = new Label(dateComposite1, SWT.NONE);
		fromLabel.setText("From:");
		GridDataFactory.fillDefaults().grab(false, false).indent(10,0).span(1, 0).applyTo(fromLabel);
		
		final Composite dateHolder1 = new Composite(dateComposite1, SWT.None);
		GridDataFactory.fillDefaults().grab(false, false).span(1,1).applyTo(dateHolder1);
		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(true).applyTo(dateHolder1);
		
		fromDate = new DateTime(dateHolder1, SWT.DATE | SWT.DROP_DOWN | SWT.BORDER);
		
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(fromDate);
		fromLabel.setEnabled(false);
		fromDate.setEnabled(false);
	
		
		/*fromDate.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				int day = fromDate.getDay();
				int month = fromDate.getMonth() + 1;
				int year = fromDate.getYear();
				Date newDate = null;
				try {
					newDate = format.parse(day + "/" + month + "/" + year);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				if (newDate.before(minDate) || newDate.after(maxDate)) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(minDate);
					fromDate.setMonth(cal.get(Calendar.MONTH));
					fromDate.setDay(cal.get(Calendar.DAY_OF_MONTH));
					fromDate.setYear(cal.get(Calendar.YEAR));
				}
			}
		});*/
		
		
		
		checkboxSearch1 = new Button(filterResultsGroup, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).indent(10,10).applyTo(checkboxSearch1);
		checkboxSearch1.setText("Include documents from the Office of the Press Secretary");
		
		
		
		Composite comboComposite1 = new Composite(filterResultsGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(5).equalWidth(false).applyTo(comboComposite1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(comboComposite1);
		Label selectPresidentSearchLabel = new Label(comboComposite1, SWT.NONE);
		selectPresidentSearchLabel.setText("Select President:");
		selectPresidentSearch = new Combo(comboComposite1, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).indent(55,0).span(1, 0).applyTo(selectPresidentSearch);
		selectPresidentSearch.setItems(presidentNames);
		selectPresidentSearch.select(0);
		
		
		
		Composite dateComposite2 = new Composite(filterResultsGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(dateComposite2);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(dateComposite2);
		final Label toLabel = new Label(dateComposite2, SWT.NONE);
		toLabel.setText("To:");
		GridDataFactory.fillDefaults().grab(false, false).indent(10,0).span(1, 0).applyTo(toLabel);
		
		final Composite dateHolder2 = new Composite(dateComposite2, SWT.None);
		GridDataFactory.fillDefaults().grab(false, false).indent(15,0).span(1,1).applyTo(dateHolder2);
		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(true).applyTo(dateHolder2);
		
		
		toDate = new DateTime(dateHolder2, SWT.DATE | SWT.DROP_DOWN | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(toDate);
		toLabel.setEnabled(false);
		toDate.setEnabled(false);
		
		/*toDate.addListener(SWT.Selection, new Listener()
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
		});*/
		
		dateCheckSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (dateCheckSearch.getSelection()) {
					fromDate.setEnabled(true);
					toDate.setEnabled(true);
					fromLabel.setEnabled(true);
					toLabel.setEnabled(true);
				} else {
					fromDate.setEnabled(false);
					toDate.setEnabled(false);
					fromLabel.setEnabled(false);
					toLabel.setEnabled(false);
				}
			}
		});
		
		
		
		checkboxSearch2 = new Button(filterResultsGroup, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).indent(10,10).applyTo(checkboxSearch2);
		checkboxSearch2.setText("Include election campaign documents");
		
		Composite comboComposite2 = new Composite(filterResultsGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(5).equalWidth(false).applyTo(comboComposite2);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(comboComposite2);
		Label documentCategory = new Label(comboComposite2, SWT.NONE);
		documentCategory.setText("Select document category:");
		selectDocumentSearch = new Combo(comboComposite2, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(selectDocumentSearch);
		selectDocumentSearch.setItems(documentTypes);
		selectDocumentSearch.select(0);
		TacitFormComposite.createEmptyRow(toolkit, filterResultsGroup);
		
		
		browseComposite = toolkit.createComposite(mainComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(browseComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(browseComposite);
		

		Label selectPresidentBrowseLabel = new Label(browseComposite, SWT.NONE);
		selectPresidentBrowseLabel.setText("Select President:");
		selectPresidentBrowse = new Combo(browseComposite, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).indent(0,10).applyTo(selectPresidentBrowse);
		selectPresidentBrowse.setItems(presidentNames);
		selectPresidentBrowse.select(0);
		
		
		Label documentCategoryBrowseLabel = new Label(browseComposite, SWT.NONE);
		documentCategoryBrowseLabel.setText("Select document category:");
		selectDocumentBrowse = new Combo(browseComposite, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).indent(0,10).applyTo(selectDocumentBrowse);
		selectDocumentBrowse.setItems(documentTypes);
		selectDocumentBrowse.select(0);
		
		dateCheckBrowse = new Button(browseComposite, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, false).span(4, 0).indent(0, 10).applyTo(dateCheckBrowse);
		dateCheckBrowse.setText("Specify Date");
		
		
		Composite dateComposite3 = new Composite(browseComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(dateComposite3);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).indent(0,10).applyTo(dateComposite3);
		final Label dateLabel = new Label(dateComposite3, SWT.NONE);
		dateLabel.setText("Date:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(dateLabel);
		
		final Composite dateHolder3 = new Composite(dateComposite3, SWT.None);
		GridDataFactory.fillDefaults().grab(false, false).span(1,1).applyTo(dateHolder3);
		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(true).applyTo(dateHolder3);
		
		
		
		date = new DateTime(dateHolder3, SWT.DATE | SWT.DROP_DOWN | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(date);
		dateLabel.setEnabled(false);
		date.setEnabled(false);
		
		/*date.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(Event event) {
	            int day = date.getDay();
	            int month = date.getMonth() + 1;
	            int year = date.getYear();
	            Date newDate = null;
	            try {
	                newDate = format.parse(day + "/" + month + "/" + year);
	            }
	            catch (ParseException e) {
	                e.printStackTrace();
	            }
	            	Calendar cal = Calendar.getInstance();
	                cal.setTime(maxDate);
	                date.setMonth(cal.get(Calendar.MONTH));
	                date.setDay(cal.get(Calendar.DAY_OF_MONTH));
	                date.setYear(cal.get(Calendar.YEAR));
	            
			}
		});*/
		
		dateCheckBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (dateCheckBrowse.getSelection()) {
					date.setEnabled(true);
					dateLabel.setEnabled(true);
				} else {
					date.setEnabled(false);
					dateLabel.setEnabled(false);
				}
			}
		});
		
		checkboxBrowse1 = new Button(browseComposite, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).indent(0,10).applyTo(checkboxBrowse1);
		checkboxBrowse1.setText("Include documents from the Office of the Press Secretary");
		
		checkboxBrowse2 = new Button(browseComposite, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).indent(0,10).applyTo(checkboxBrowse2);
		checkboxBrowse2.setText("Include election campaign documents");
		
		((GridData) browseComposite.getLayoutData()).exclude = true; // hide this
		TacitFormComposite.createEmptyRow(toolkit, browseComposite);
		
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
	
	@Override
	public void setFocus() {
		form.setFocus();
		
	}

	private boolean canItProceed() {
		form.getMessageManager().removeAllMessages();
		
		if(browseButton.getSelection()) { 
			
			if(!dateCheckBrowse.getSelection()){
				
				if(selectPresidentBrowse.getSelectionIndex()==0&&selectDocumentBrowse.getSelectionIndex()==0)
				{
					form.getMessageManager().addMessage("queryText", "Please choose atleast one of the three fields - President, Document category, or Date.", null, IMessageProvider.ERROR);
					return false;
				}
				form.getMessageManager().removeMessage("queryText");
			}
			
		}
		else{
			Calendar cal = Calendar.getInstance();
			cal.set(fromDate.getYear(), fromDate.getMonth(), fromDate.getDay());
			double from = cal.getTimeInMillis() / 1000;
			cal.set(toDate.getYear(), toDate.getMonth(), toDate.getDay());
			double to = cal.getTimeInMillis() / 1000;
			
			if(searchText1.getText().equals("")){
				form.getMessageManager().addMessage("queryText", "Provide valid search text to crawl.", null, IMessageProvider.ERROR);
				return false;
			}
			else if(from>to){
				
				form.getMessageManager().addMessage("queryText", "Invalid dates entered.", null, IMessageProvider.ERROR);
				return false;
			}
			form.getMessageManager().removeMessage("queryText");
			
		}
		
		
		//Validate corpus name
		String corpusName = corpusNameTxt.getText();
		if(null == corpusName || corpusName.isEmpty()) {
			form.getMessageManager().addMessage("corpusName", "Provide corpus name", null, IMessageProvider.ERROR);
			return false;
		} else {
			String outputDir = IAmericanPresidencyCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
			if(new File(outputDir).exists()){
				form.getMessageManager().addMessage("corpusName", "Corpus already exists", null, IMessageProvider.ERROR);
				return false;
			}
			else{
				form.getMessageManager().removeMessage("corpusName");
				return true;
			}
			}		
		}
	
	private void addButtonsToToolBar() {
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (AmericanPresidencyCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_LRUN_OBJ));
			}
			@Override
			public String getToolTipText() {
				return "Crawl";
			}
			
			String outputDir;
			int presidentIndex; int documentIndex; String corpusName;			
			boolean officeDocs, campaignDocs;
			boolean search; boolean browse; boolean canProceed; 
			String query1; String query2;
			Calendar from = null;
			Calendar to  = null;
			Calendar dateBrowse = null;
			String operator;
			@Override
			public void run() {
				final Job job = new Job("American Presidency Crawler") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						TacitFormComposite.setConsoleViewInFocus();
						TacitFormComposite.updateStatusMessage(getViewSite(), null,null, form);
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								
								search = searchButton.getSelection();
								browse = browseButton.getSelection();
								corpusName = corpusNameTxt.getText();
								if(search) {
									
									if (andButton.getSelection())
										operator = "AND";
									else if(orButton.getSelection())
										operator = "OR";
									else
										operator = "AND NOT";
									query1 = searchText1.getText();
									query2 = searchText2.getText();
									presidentIndex = selectPresidentSearch.getSelectionIndex();
									documentIndex = selectDocumentSearch.getSelectionIndex();
									if (dateCheckSearch.getSelection())
									{	
										from = Calendar.getInstance();
										from.set(fromDate.getYear(), fromDate.getMonth(), fromDate.getDay());
										to = Calendar.getInstance();
										to.set(toDate.getYear(), toDate.getMonth(), toDate.getDay());
									}
									officeDocs = checkboxSearch1.getSelection();
									campaignDocs = checkboxSearch2.getSelection();
								} else if(browse) {
									if (dateCheckBrowse.getSelection())
									{	
										dateBrowse = Calendar.getInstance();
										dateBrowse.set(date.getYear(), date.getMonth(), date.getDay());
									}
									officeDocs = checkboxBrowse1.getSelection();
									campaignDocs = checkboxBrowse2.getSelection();
									documentIndex = selectDocumentBrowse.getSelectionIndex();	
									presidentIndex = selectPresidentBrowse.getSelectionIndex();							
								}
								Date dateObj = new Date();
								corpusName+= "_" + dateObj.getTime();
								outputDir = IAmericanPresidencyCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
								if(!new File(outputDir).exists()){
									System.out.println("---------------+++++++++++++++++++----------------"+outputDir);
									boolean flag = new File(outputDir).mkdirs();									

									System.out.println("---------------+++++++++++++++++++----------------"+flag);
								}
						}
						});
						int progressSize = 1000;//+30
						monitor.beginTask("Running American Presidency Crawler..." , progressSize);
						TacitFormComposite.writeConsoleHeaderBegining("American Presidency Crawler started");
						final AmericanPresidencyCrawler rc = new AmericanPresidencyCrawler(); // initialize all the common parameters	

						monitor.subTask("Initializing...");
						monitor.worked(10);
						if(monitor.isCanceled())
							handledCancelRequest("Cancelled");
						americanPresidencyCorpus = new Corpus(corpusName, CMDataType.PRESIDENCY_JSON);
						if(search) {
							try {
								monitor.subTask("Crawling...");
								if(monitor.isCanceled()) 
									return handledCancelRequest("Cancelled");					
								rc.crawlSearch(outputDir, query1, query2, operator, from, to, presidentNameMap.get(presidentIndex), documentCategoryMap.get(documentIndex), officeDocs, campaignDocs, monitor);
								if(monitor.isCanceled())
									return handledCancelRequest("Cancelled");
							} catch (Exception e) {
								return handleException(monitor, e, "Crawling failed. Provide valid data");
							} 
						} else if(browse) {
							try {
								monitor.subTask("Crawling...");
								if(monitor.isCanceled())
									return handledCancelRequest("Cancelled");								
								if(monitor.isCanceled())
									return handledCancelRequest("Cancelled");
								rc.crawlBrowse(outputDir,dateBrowse,presidentNameMap.get(presidentIndex),documentCategoryMap.get(documentIndex),officeDocs, campaignDocs, monitor);
							} catch (Exception e) {
								return handleException(monitor, e, "Crawling failed. Provide valid data");
							}
						}
						
						try {
							Display.getDefault().syncExec(new Runnable() {
								@Override
								public void run() {
									CorpusClass cc = new CorpusClass("American Presidency Documents", outputDir);
									cc.setParent(americanPresidencyCorpus);
									americanPresidencyCorpus.addClass(cc);
								}
							});
						} catch (Exception e) {
							e.printStackTrace();
							return Status.CANCEL_STATUS;
						}
						
						try {
							ManageCorpora.saveCorpus(americanPresidencyCorpus);
						} catch(Exception e) {
							e.printStackTrace();
							return Status.CANCEL_STATUS;
						}
						
						
						if(monitor.isCanceled())
							return handledCancelRequest("Cancelled");
						
						monitor.worked(10);//100
						monitor.done();
						return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				canProceed = canItProceed();
				if(canProceed) {
					job.schedule(); // schedule the job
					job.addJobChangeListener(new JobChangeAdapter() {

						public void done(IJobChangeEvent event) {
							if (!event.getResult().isOK()) {
								TacitFormComposite.writeConsoleHeaderBegining("Error: <Terminated> American Presidency Crawler  ");
								TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is stopped",
										IStatus.INFO, form);

							} else {
								TacitFormComposite.writeConsoleHeaderBegining("Success: <Completed> American Presidency Crawler  ");
								TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is completed",
										IStatus.INFO, form);
								ConsoleView.printlInConsoleln("Done");
								ConsoleView.printlInConsoleln("American Presidency crawler completed successfully.");
	
							}
						}
					});
				}				
			}
		});

		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (AmericanPresidencyCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_HELP_CO));
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
						"edu.usc.cssl.tacit.crawlers.americanpresidency.ui.americanpresidency");				
			};
		};
		
		mgr.add(helpAction);
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(helpAction,
						"edu.usc.cssl.tacit.crawlers.americanpresidency.ui.americanpresidency");
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(form,
						"edu.usc.cssl.tacit.crawlers.americanpresidency.ui.americanpresidency");
		form.getToolBarManager().update(true);
	}

	private IStatus handledCancelRequest(String message) {
		TacitFormComposite.updateStatusMessage(getViewSite(), message, IStatus.ERROR, form);
		ConsoleView.printlInConsoleln("American Presidency crawler cancelled.");
		return Status.CANCEL_STATUS;
	}

	
	private IStatus handleException(IProgressMonitor monitor, Exception e, String message) {
		monitor.done();
		System.out.println(message);
		e.printStackTrace();
		TacitFormComposite.updateStatusMessage(getViewSite(), message, IStatus.ERROR, form);
		return Status.CANCEL_STATUS;
	}	

}
/*

Corpus Management of the American presidency json 
UI of browse section will change if the date input style is modified - discuss
The progress bar has to be made proper 

*/
