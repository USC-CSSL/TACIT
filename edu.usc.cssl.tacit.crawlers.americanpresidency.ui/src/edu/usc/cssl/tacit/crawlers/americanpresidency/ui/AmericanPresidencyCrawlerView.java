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
	
	private ScrolledForm form;
	private FormToolkit toolkit;

	private Button andButton;
	private Button orButton;
	private Button notButton;
	private Button checkboxSearch1;
	private Button checkboxSearch2;
	private Button dateCheckBrowse;
	private Button dateCheckSearch;
	
	private Composite searchComposite;

	private Text searchText1;
	private Composite commonsearchComposite;
	private Combo selectPresidentSearch;	
	private Combo selectDocumentSearch;	
	private Text searchText2;
	private Text corpusNameTxt;
	private DateTime toDate;
	private DateTime fromDate;
	private Combo browseMonth;	
	private Combo browseDay;	
	private Combo browseYear;
	Corpus americanPresidencyCorpus;
	
	String presidentNames[] = {"President","George Washington","John Adams","Thomas Jefferson","James Madison","James Monroe","John Quincy Adams","Andrew Jackson","Martin van Buren","William Henry Harrison","John Tyler","James K. Polk","Zachary Taylor","Millard Fillmore","Franklin Pierce","James Buchanan","Abraham Lincoln","Andrew Johnson","Ulysses S. Grant","Rutherford B. Hayes","James A. Garfield","Chester A. Arthur","Grover Cleveland","Benjamin Harrison","Grover Cleveland","William McKinley","Theodore Roosevelt","William Howard Taft","Woodrow Wilson","Warren G. Harding","Calvin Coolidge","Herbert Hoover","Franklin D. Roosevelt","Harry S. Truman","Dwight D. Eisenhower","John F. Kennedy","Lyndon B. Johnson","Richard Nixon","Gerald R. Ford","Jimmy Carter","Ronald Reagan","George Bush","William J. Clinton","George W. Bush","Barack Obama"};
	String documentTypes[] = {"Document Category","Oral: Address - Inaugural","Oral: Address - to Congress (non SOTU)","Oral: Address - State of the Union","Oral: Address - major to the Nation","Oral: Address - Farewell","Oral: Address - Saturday Radio","Oral: Address - Fireside Chats","Oral: Address  - \"Inaugural\" (Accidental Presidents)","Oral: Address - at College Commencements","Oral: Address - to the UN General Assembly","Oral: Address - to Foreign Legislatures","Oral: Address - Nomination Acceptance","Oral: Remarks - (non categorized)","Oral: Remarks - Toasts","Oral: Remarks - Bill Signings","Oral: Remarks - Bill Vetos","Oral: News Conferences","Oral: News Conferences - Joint","Debates: General Election","Oral: Remarks - Campaign Fundraiser","Oral: Remarks - regarding Executive Nominations","Oral: Remarks - regarding Executive Appointments","Oral: Remarks - regarding Resignations","Written: Messages - (non categorized)","Written: Messages -  to Congress","Written: Messages - Annual Messages (written SOTU)","Written: Messages - Veto Messages","Written: Messages - Pocket Veto Messages","Written: Messages - Budget Messages","Written: Messages -  Farewell Addresses","Written: Memorandums","Written: Memorandums - Pocket Vetos","Written: Memorandums - Determinations","Written: Memorandums - Diplomatic - Memo of Understanding","Written: Executive Orders","Written: Proclamations","Written: Statements - (non categorized)","Written: Statements - Signing Statements","Written: Statements - Veto Statements","Written: Letters - (non categorized)","Written: Letters - to Congress","Written: Notices","Written: Telegrams - Lincoln (Civil War)","E.O.P.: Press Briefings","OMB: Statements of Administration Policy","Debates: Vice-Presidential","Debates: Primary Elections-Democratic Party","Debates: Primary Elections-Republican Party","Party Platforms"};
	String days[]={"Day","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"};
	String months[] = {"Month","January","February","March","April","May","June","July","August","September","October","November","December"};
	String years[] = {"Year","1789","1790","1791","1792","1793","1794","1795","1796","1797","1798","1799","1800","1801","1802","1803","1804","1805","1806","1807","1808","1809","1810","1811","1812","1813","1814","1815","1816","1817","1818","1819","1820","1821","1822","1823","1824","1825","1826","1827","1828","1829","1830","1831","1832","1833","1834","1835","1836","1837","1838","1839","1840","1841","1842","1843","1844","1845","1846","1847","1848","1849","1850","1851","1852","1853","1854","1855","1856","1857","1858","1859","1860","1861","1862","1863","1864","1865","1866","1867","1868","1869","1870","1871","1872","1873","1874","1875","1876","1877","1878","1879","1880","1881","1882","1883","1884","1885","1886","1887","1888","1889","1890","1891","1892","1893","1894","1895","1896","1897","1898","1899","1900","1901","1902","1903","1904","1905","1906","1907","1908","1909","1910","1911","1912","1913","1914","1915","1916","1917","1918","1919","1920","1921","1922","1923","1924","1925","1926","1927","1928","1929","1930","1931","1932","1933","1934","1935","1936","1937","1938","1939","1940","1941","1942","1943","1944","1945","1946","1947","1948","1949","1950","1951","1952","1953","1954","1955","1956","1957","1958","1959","1960","1961","1962","1963","1964","1965","1966","1967","1968","1969","1970","1971","1972","1973","1974","1975","1976","1977","1978","1979","1980","1981","1982","1983","1984","1985","1986","1987","1988","1989","1990","1991","1992","1993","1994","1995","1996","1997","1998","1999","2000","2001","2002","2003","2004","2005","2006","2007","2008","2009","2010","2011","2012","2013","2014","2015","2016"};
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
		toolkit = createFormBodySection(parent, "UC Santa Barbara Presidential Papers Crawler");
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(section);
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
		buttonComposite.setText("Type of crawl");
		buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonComposite.setLayout(layout);

		

		

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
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(searchComposite);
		GridLayoutFactory.fillDefaults().equalWidth(true).applyTo(searchComposite);
		
		commonsearchComposite = toolkit.createComposite(searchComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(commonsearchComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(commonsearchComposite);
		
		TacitFormComposite.createEmptyRow(toolkit, searchComposite);
		
		// split it into 3		
		Group filterResultsGroup = new Group(searchComposite, SWT.LEFT);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(filterResultsGroup);
		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(false).applyTo(filterResultsGroup);
		filterResultsGroup.setText("Filter Results");
		TacitFormComposite.createEmptyRow(toolkit, filterResultsGroup);
		
		TacitFormComposite.createEmptyRow(toolkit, searchComposite);
		
		final Label searchLabel1 = new Label(commonsearchComposite, SWT.NONE);
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
		
		final Label searchLabel2 = new Label(commonsearchComposite, SWT.NONE);
		searchLabel2.setText("Search Term 2:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(searchLabel2);
		searchText2 = new Text(commonsearchComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(searchText2);	
		searchText2.setMessage("Enter a search term");			

		dateCheckSearch = new Button(filterResultsGroup, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).indent(10,0).applyTo(dateCheckSearch);
		dateCheckSearch.setText("Specify Date Range");
		
 		
		dateCheckBrowse = new Button(filterResultsGroup, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(dateCheckBrowse);
		dateCheckBrowse.setText("Specify Date");

		Composite dateComposite1 = new Composite(filterResultsGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(false).applyTo(dateComposite1);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).indent(0,10).applyTo(dateComposite1);
		
		final Label fromLabel = new Label(dateComposite1, SWT.NONE);
		fromLabel.setText("From:");
		GridDataFactory.fillDefaults().grab(false, false).indent(10,0).span(1, 0).applyTo(fromLabel);

		fromDate = new DateTime(dateComposite1, SWT.DATE | SWT.DROP_DOWN | SWT.BORDER);
		
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(fromDate);
		fromLabel.setEnabled(false);
		fromDate.setEnabled(false);

		
		
		final Label toLabel = new Label(dateComposite1, SWT.NONE);
		toLabel.setText("To:");
		GridDataFactory.fillDefaults().grab(false, false).indent(10,0).span(1, 0).applyTo(toLabel);
		
		toDate = new DateTime(dateComposite1, SWT.DATE | SWT.DROP_DOWN | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(false, false).indent(15,0).span(1, 0).applyTo(toDate);
		toLabel.setEnabled(false);
		toDate.setEnabled(false);
		
		
		
		
		Composite dateComposite3 = new Composite(filterResultsGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(dateComposite3);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).indent(0,10).applyTo(dateComposite3);
		
		final Composite dateHolder3 = new Composite(dateComposite3, SWT.None);
		GridDataFactory.fillDefaults().grab(false, false).span(2,1).applyTo(dateHolder3);
		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(true).applyTo(dateHolder3);
	
		
		checkboxSearch1 = new Button(filterResultsGroup, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).indent(10,10).applyTo(checkboxSearch1);
		checkboxSearch1.setText("Include documents from the Office of the Press Secretary");

		Composite comboComposite1 = new Composite(filterResultsGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(comboComposite1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(comboComposite1);
		Label selectPresidentSearchLabel = new Label(comboComposite1, SWT.NONE);
		selectPresidentSearchLabel.setText("Select President:");
		selectPresidentSearch = new Combo(comboComposite1, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).indent(55,10).span(1, 0).applyTo(selectPresidentSearch);
		selectPresidentSearch.setItems(presidentNames);
		selectPresidentSearch.select(0);
		
		dateCheckSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (dateCheckSearch.getSelection()) {
					dateCheckBrowse.setSelection(false);
					fromDate.setEnabled(true);
					toDate.setEnabled(true);
					fromLabel.setEnabled(true);
					toLabel.setEnabled(true);
					dateHolder3.setEnabled(false);
					searchLabel1.setEnabled(true);
					searchLabel2.setEnabled(true);
					andButton.setEnabled(true);
					orButton.setEnabled(true);
					notButton.setEnabled(true);
				} else {
					fromDate.setEnabled(false);
					toDate.setEnabled(false);
					fromLabel.setEnabled(false);
					toLabel.setEnabled(false);
				}
			}
		});
		
		dateCheckBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (dateCheckBrowse.getSelection()) {
					dateCheckSearch.setSelection(false);
					fromDate.setEnabled(false);
					toDate.setEnabled(false);
					fromLabel.setEnabled(false);
					toLabel.setEnabled(false);
					dateHolder3.setEnabled(true);
					searchText1.setEnabled(false);
					searchText2.setEnabled(false);
					searchLabel1.setEnabled(false);
					searchLabel2.setEnabled(false);
					andButton.setEnabled(false);
					orButton.setEnabled(false);
					notButton.setEnabled(false);
				} else {
					dateHolder3.setEnabled(false);
					commonsearchComposite.setEnabled(true);

					searchText1.setEnabled(true);
					searchText2.setEnabled(true);
					searchLabel1.setEnabled(true);
					searchLabel2.setEnabled(true);
					andButton.setEnabled(true);
					orButton.setEnabled(true);
					notButton.setEnabled(true);
				}
			}
		});
		
		checkboxSearch2 = new Button(filterResultsGroup, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).indent(10,10).applyTo(checkboxSearch2);
		checkboxSearch2.setText("Include election campaign documents");
		
		Composite comboComposite2 = new Composite(filterResultsGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(comboComposite2);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(comboComposite2);
		Label documentCategory = new Label(comboComposite2, SWT.NONE);
		documentCategory.setText("Select document category:");
		selectDocumentSearch = new Combo(comboComposite2, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(selectDocumentSearch);
		selectDocumentSearch.setItems(documentTypes);
		selectDocumentSearch.select(0);
		TacitFormComposite.createEmptyRow(toolkit, filterResultsGroup);
		
		
		
		browseMonth = new Combo(dateHolder3, SWT.FLAT | SWT.READ_ONLY);
		browseMonth.setItems(months);
		browseMonth.select(0);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(browseMonth);
		browseDay = new Combo(dateHolder3, SWT.FLAT | SWT.READ_ONLY);
		browseDay.setItems(days);
		browseDay.select(0);
		GridDataFactory.fillDefaults().grab(false, false).indent(10,0).span(1, 0).applyTo(browseDay);
		browseYear = new Combo(dateHolder3, SWT.FLAT | SWT.READ_ONLY);
		browseYear.setItems(years);
		browseYear.select(0);
		GridDataFactory.fillDefaults().grab(false, false).indent(10,0).span(1, 0).applyTo(browseYear);
		browseDay.setEnabled(false);
		browseMonth.setEnabled(false);
		browseYear.setEnabled(false);
		/*
		date = new DateTime(dateHolder3, SWT.DATE | SWT.DROP_DOWN | SWT.BORDER);
		date.setEnabled(false);
		*/
		dateCheckBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (dateCheckBrowse.getSelection()) {
					browseDay.setEnabled(true);
					browseMonth.setEnabled(true);
					browseYear.setEnabled(true);
				} else {
					browseDay.setEnabled(false);
					browseMonth.setEnabled(false);
					browseYear.setEnabled(false);
				}
			}
		});
		
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
		
		if(dateCheckBrowse.getSelection()) {
			
			if(browseDay.getSelectionIndex()==0&&browseMonth.getSelectionIndex()==0&&browseYear.getSelectionIndex()==0){
				form.getMessageManager().addMessage("queryText", "Please choose atleast one of the three fields - Month, Day, or Year.", null, IMessageProvider.ERROR);
				return false;
			}
			else
				form.getMessageManager().removeMessage("queryText");
		}
		else{
			Calendar cal = Calendar.getInstance();
			cal.set(fromDate.getYear(), fromDate.getMonth(), fromDate.getDay());
			double from = cal.getTimeInMillis() / 1000;
			cal.set(toDate.getYear(), toDate.getMonth(), toDate.getDay());
			double to = cal.getTimeInMillis() / 1000;
			
			if(searchText1.getText().equals("") && selectPresidentSearch.getSelectionIndex()==0&&selectDocumentSearch.getSelectionIndex()==0){
				form.getMessageManager().addMessage("queryText", "Provide provide atleast one of the three fields - Search text, President, or Document category.", null, IMessageProvider.ERROR);
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
			String month, day, year;
			String outputDir;
			int presidentIndex; int documentIndex; String corpusName;			
			boolean officeDocs, campaignDocs;
			boolean browse; boolean canProceed; 
			String query1; String query2;
			Calendar from = null;
			Calendar to  = null;
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
								
								browse = dateCheckBrowse.getSelection();
								corpusName = corpusNameTxt.getText();
								if(!browse) {
									
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
								} else {
									month = browseMonth.getSelectionIndex() == 0 ? "" : months[browseMonth.getSelectionIndex()];
									day = browseDay.getSelectionIndex() == 0 ? "" : days[browseDay.getSelectionIndex()];
									year = browseYear.getSelectionIndex() == 0 ? "" : years[browseYear.getSelectionIndex()];
									officeDocs = checkboxSearch1.getSelection();
									campaignDocs = checkboxSearch2.getSelection();
									documentIndex = selectDocumentSearch.getSelectionIndex();	
									presidentIndex = selectPresidentSearch.getSelectionIndex();							
								}
								Date dateObj = new Date();
								corpusName+= "_" + dateObj.getTime();
								outputDir = IAmericanPresidencyCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
								if(!new File(outputDir).exists()){
									boolean flag = new File(outputDir).mkdirs();									

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
						if(!browse) {
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
						} else {
							try {
								monitor.subTask("Crawling...");
								if(monitor.isCanceled())
									return handledCancelRequest("Cancelled");								
								if(monitor.isCanceled())
									return handledCancelRequest("Cancelled");
								
								rc.crawlBrowse(outputDir,month,day,year,presidentNameMap.get(presidentIndex),documentCategoryMap.get(documentIndex),officeDocs, campaignDocs, monitor);
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
