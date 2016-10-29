package edu.usc.cssl.tacit.crawlers.uscongress.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

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
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IMessageManager;
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
import edu.usc.cssl.tacit.crawlers.uscongress.services.RecordCongressCrawl;
import edu.usc.cssl.tacit.crawlers.uscongress.services.SearchSenators;
import edu.usc.cssl.tacit.crawlers.uscongress.ui.internal.IUsCongressCrawlerViewConstants;
import edu.usc.cssl.tacit.crawlers.uscongress.ui.internal.UsCongressCrawlerViewImageRegistry;

public class UsCongressCrawlerView extends ViewPart implements IUsCongressCrawlerViewConstants {
	public static String ID = "edu.usc.cssl.tacit.crawlers.uscongress.ui.uscongresscrawlerview";
	private ScrolledForm form;
	private FormToolkit toolkit;

	// private OutputLayoutData outputLayout;
	private Combo sCmbCongress;

	private String[] allSenators;
	private String[] allRepresentatives;
	private String[] congresses;
	private String[] congressYears;
	private boolean retryFlag = false;
	private Date maxDate;
	private Date minDate;
	private SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
	private Button dateRange;
	String[] sLoading = { "All (1989-2016)", "114 (2015-2016)", "113 (2013-2014)", "112 (2011-2012)",
			"111 (2009-2010)", "110 (2007-2008)", "109 (2005-2006)", "108 (2003-2004)", "107 (2001-2002)",
			"106 (1999-2000)", "105 (1997-1998)", "104 (1995-1996)", "103 (1993-1994)", "102 (1991-1992)",
			"101 (1989-1990)" };
	private DateTime toDate;
	private DateTime fromDate;
	private Button limitRecords;
	private Text limitText;

	private int totalSenators;
	private int totalRepresentatives;
	private int progressSize;
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
	List<String> availabileSenators;
	List<String> availableRepresentatives;
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
	private Text keywordTxt;
	private Composite limitRecordsClient;
	private boolean crawlAgain;
	private Text corpusNameTxt;

	//variables needed at runtime
	String outputDir;
	String corpusName;
	Corpus corpus;
	String chamber ="", congress;
	int limit;
	String keyword;
	boolean random;
	boolean canProceed;
	boolean isDate;
	boolean[] fields = new boolean[4];
	
	@Override
	public void createPartControl(Composite parent) {
		// Creates toolkit and form
		
		
		toolkit = createFormBodySection(parent, "US Congress Crawler");
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(section);
		section.setExpanded(true);
		form.setImage(UsCongressCrawlerViewImageRegistry.getImageIconFactory()
				.getImage(IUsCongressCrawlerViewConstants.IMAGE_US_CONGRESS_OBJ));

		// Create a composite to hold the other widgets
		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);

		// Creates an empty to create a empty space
		TacitFormComposite.createEmptyRow(toolkit, sc);

		// Create a composite that can hold the other widgets
		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1).applyTo(client); // Align
																							// the
																							// composite
																							// section
																							// to
																							// one
																							// column
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(client);

		createSenateInputParameters(client);
		// TacitFormComposite.createEmptyRow(toolkit, client);
		// outputLayout = TacitFormComposite.createOutputSection(toolkit,
		// client, form.getMessageManager());
		corpusNameTxt = TacitFormComposite.createCorpusSection(toolkit, client, form.getMessageManager());
		
		Button btnRun = TacitFormComposite.createRunButton(client, toolkit);
		btnRun.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				runModule();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		// Add run and help button on the toolbar
		addButtonsToToolBar();
	}

	private void createSenateInputParameters(final Composite client) {

		Section inputParamsSection = toolkit.createSection(client,
				Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);
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

		houseBtn = toolkit.createButton(senatorComposite, "Representatives", SWT.RADIO);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(houseBtn);
		houseBtn.setSelection(true);
		
		houseBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedSenators.clear();
				senatorTable.removeAll();
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		senateBtn = toolkit.createButton(senatorComposite, "Senate", SWT.RADIO);
		GridDataFactory.fillDefaults().grab(false, false).span(2, 0).applyTo(senateBtn);
		
		senateBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedSenators.clear();
				senatorTable.removeAll();
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		Label sCongressLabel = toolkit.createLabel(senatorComposite, "Congress:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(sCongressLabel);
		sCmbCongress = new Combo(senatorComposite, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(sCmbCongress);
		toolkit.adapt(sCmbCongress);
		sCmbCongress.setItems(sLoading);
		sCmbCongress.select(0);
		
		Label keywordLabel = toolkit.createLabel(senatorComposite, "Keyword:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(keywordLabel);
		keywordTxt = toolkit.createText(senatorComposite, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(keywordTxt);
		
		keywordTxt.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				selectedSenators.clear();
				senatorTable.removeAll();
				
			}
		});

		filterResultsComposite = toolkit.createComposite(mainComposite);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(filterResultsComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 0).applyTo(filterResultsComposite);

		Label dummy1 = new Label(senatorComposite, SWT.NONE);
		dummy1.setText("Congress Member:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(dummy1);

		senatorTable = new Table(senatorComposite, SWT.BORDER | SWT.MULTI);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 3).hint(90, 50).applyTo(senatorTable);

		Composite buttonComp = new Composite(senatorComposite, SWT.NONE);
		GridLayout btnLayout = new GridLayout();
		btnLayout.marginWidth = btnLayout.marginHeight = 0;
		btnLayout.makeColumnsEqualWidth = false;
		buttonComp.setLayout(btnLayout);
		buttonComp.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		addSenatorBtn = new Button(buttonComp, SWT.PUSH); // $NON-NLS-1$
		addSenatorBtn.setText("Add...");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(addSenatorBtn);

		addSenatorBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sHandleAdd(addSenatorBtn.getShell());
			}
		});
		addSenatorBtn.setEnabled(true);

		removeSenatorButton = new Button(buttonComp, SWT.PUSH);
		removeSenatorButton.setText("Remove...");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(removeSenatorButton);
		removeSenatorButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				for (TableItem item : senatorTable.getSelection()) {
					selectedSenators.remove(item.getText());
					item.dispose();
				}
				if (selectedSenators.size() == 0) {
					removeSenatorButton.setEnabled(false);
				}
			}
		});
		removeSenatorButton.setEnabled(true);

		TacitFormComposite.createEmptyRow(toolkit, senatorComposite);
		
		representativeComposite = toolkit.createComposite(senatorComposite);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(representativeComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 0).applyTo(representativeComposite);
		
		createStoredAttributesSection(toolkit, representativeComposite, form.getMessageManager());
		
		TacitFormComposite.createEmptyRow(toolkit, representativeComposite);
		
		Group limitGroup = new Group(filterResultsComposite, SWT.SHADOW_IN);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 0).applyTo(limitGroup);
		limitGroup.setText("Limit Records");
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(limitGroup);

		limitRecordsClient = new Composite(limitGroup, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).indent(10, 10).applyTo(limitRecordsClient);
		GridLayoutFactory.fillDefaults().numColumns(5).equalWidth(false).applyTo(limitRecordsClient);


		limitRecords = new Button(limitRecordsClient, SWT.CHECK);
		limitRecords.setText("Limit records per congress member");
		GridDataFactory.fillDefaults().grab(false, false).span(5, 0).applyTo(limitRecords);
		limitRecords.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!limitRecords.getSelection()) {
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
		limitLabel.setText("No. of records per congress member:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(limitLabel);
		limitLabel.setEnabled(false);
		limitText = new Text(limitRecordsClient, SWT.BORDER);
		limitText.setText("1");
		GridDataFactory.fillDefaults().grab(true, false).span(4, 0).applyTo(limitText);
		limitText.setEnabled(false);

		limitText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (!(e.character >= '0' && e.character <= '9')) {
					form.getMessageManager().addMessage("limitText", "Provide valid no.of.records per senator", null,
							IMessageProvider.ERROR);
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

	private void sHandleAdd(Shell shell) {

		processElementSelectionDialog(shell);

		senatorList = new LinkedHashSet<String>();
		int num = sCmbCongress.getSelectionIndex();
		String congress = sLoading[num].substring(0,sLoading[num].indexOf("(")-1).toLowerCase();
		senatorList = SearchSenators.crawl(houseBtn.getSelection(), congress, keywordTxt.getText());
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
				return (UsCongressCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IUsCongressCrawlerViewConstants.IMAGE_LRUN_OBJ));
				
			}

			@Override
			public String getToolTipText() {
				return "Crawl";
			}

			
			
			@Override
			public void run() {
				runModule();
			}
		});

		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (UsCongressCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IUsCongressCrawlerViewConstants.IMAGE_HELP_CO));
			}

			@Override
			public String getToolTipText() {
				return "Help";
			}

			@Override
			public void run() {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp("edu.usc.cssl.tacit.crawlers.uscongress.ui.uscongress");
			};
		};

		mgr.add(helpAction);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(helpAction, "edu.usc.cssl.tacit.crawlers.uscongress.ui.uscongress");
		PlatformUI.getWorkbench().getHelpSystem().setHelp(form, "edu.usc.cssl.tacit.crawlers.uscongress.ui.uscongress");
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
	
	int returnCode = 0;	
	int counter = 0 ;

	
	private IStatus handledCancelRequest(String message) {
		TacitFormComposite.updateStatusMessage(getViewSite(), message, IStatus.ERROR, form);
		ConsoleView.printlInConsoleln("US Congress crawler cancelled.");
//		ConsoleView.printlInConsoleln("Total no.of.files downloaded : " + sc.totalFilesDownloaded);
//		sc.filesDownload = new HashSet<String>();
		TacitFormComposite.writeConsoleHeaderBegining("<terminated> US Congress Crawler");
		return Status.CANCEL_STATUS;
		
	}

	private boolean canItProceed() {

			if(!keywordTxt.getText().equals("") && senatorTable.getItemCount() == 0) {
				form.getMessageManager().addMessage("list", "Senator list cannot be empty", null, IMessageProvider.ERROR);
				return false;
			}
			else
				form.getMessageManager().removeMessage("list");
		if(limitRecords.getSelection()) {
			if(limitText.getText().isEmpty()) {
				form.getMessageManager() .addMessage( "limitText", "Provide valid no.of.records per senator", null, IMessageProvider.ERROR);
				return false;
			}
		}
		
		String corpusName = corpusNameTxt.getText();
		/*String message = OutputPathValidation.getInstance().validateOutputDirectory(outputLayout.getOutputLabel().getText(), "Output");
		if (message != null) {
			message = outputLayout.getOutputLabel().getText() + " " + message;
			form.getMessageManager().addMessage("output", message, null,IMessageProvider.ERROR);
			return false;
		} else {
			form.getMessageManager().removeMessage("output");
		}*/
		if(null == corpusName || corpusName.isEmpty()) {
			form.getMessageManager().addMessage("corpusName", "Provide corpus name", null, IMessageProvider.ERROR);
			return false;
		} else {
			String outputDir = IUsCongressCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
			if(new File(outputDir).exists()){
				form.getMessageManager().addMessage("corpusName", "Corpus already exists", null, IMessageProvider.ERROR);
				return false;
			}
			else{
			form.getMessageManager().removeMessage("corpusName");
			return true;
			}
			}		
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

	static Button chamberBtn, dateBtn, titleBtn, bodyBtn;

	public static void createStoredAttributesSection(FormToolkit toolkit, Composite parent,
		final IMessageManager mmng) {
		Section section = toolkit.createSection(parent,
				Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION | Section.TWISTIE);
		
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(4).applyTo(section);
		section.setText("Stored Attributes "); //$NON-NLS-1$
		section.setDescription("Choose values for Filter");
		
		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		
		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(false).applyTo(sc);
		
		Composite sectionClient = toolkit.createComposite(section);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(false).applyTo(sectionClient);
		section.setClient(sectionClient);
		
		Label dummy = toolkit.createLabel(sectionClient, "", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(4, 0).applyTo(dummy);
		
		chamberBtn = new Button(sectionClient, SWT.CHECK);
		chamberBtn.setText("Chamber of Congress");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(chamberBtn);
		chamberBtn.setSelection(true);
		
		dateBtn = new Button(sectionClient, SWT.CHECK);
		dateBtn.setText("Date");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(dateBtn);
		dateBtn.setSelection(true);
		
		titleBtn = new Button(sectionClient, SWT.CHECK);
		titleBtn.setText("Title");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(titleBtn);
		titleBtn.setSelection(true);
		
		bodyBtn = new Button(sectionClient, SWT.CHECK);
		bodyBtn.setText("Body");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(bodyBtn);
		bodyBtn.setSelection(true);
	
	}
	private void runModule() {

		final Job job = new Job("US Congress Crawler") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				TacitFormComposite.setConsoleViewInFocus();
				TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						
						corpusName = corpusNameTxt.getText();
						random = sortByDateNo.getSelection();
						fields[0] = chamberBtn.getSelection();
						fields[1] = dateBtn.getSelection();
						fields[2] = titleBtn.getSelection();
						fields[3] = bodyBtn.getSelection();
						
						if(limitRecords.getSelection())
							limit = Integer.parseInt(limitText.getText());
						else
							limit = -1;
						if(houseBtn.getSelection())
							chamber = "House";
						else if(senateBtn.getSelection())
							chamber = "Senate";
						
						int num = sCmbCongress.getSelectionIndex();
						congress = sLoading[num].substring(0,sLoading[num].indexOf("(")-1).toLowerCase();
						keyword = keywordTxt.getText();
						outputDir = IUsCongressCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator+ corpusName.trim();
						if (!new File(outputDir).exists()) {
							new File(outputDir).mkdirs();
						}
					}
				});
				int progressSize= limit+15;
				if(limit!=-1 && selectedSenators!=null)
					progressSize = limit*selectedSenators.size()+15;
				monitor.beginTask("Running US Congress Crawler...", progressSize);
				TacitFormComposite.writeConsoleHeaderBegining("US Congress Crawler started");
				RecordCongressCrawl crawler = new RecordCongressCrawl();
				monitor.subTask("Initializing...");
				monitor.worked(10);
				if (monitor.isCanceled())
					handledCancelRequest("Crawling is Stopped");
				corpus = new Corpus(corpusName, CMDataType.CONGRESS_JSON);

					try {
						monitor.subTask("Crawling...");
						if (monitor.isCanceled())
							return handledCancelRequest("Crawling is Stopped");
						
						if(selectedSenators == null || selectedSenators.isEmpty()){
						outputDir = IUsCongressCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
						outputDir += File.separator + "Congress"+chamber;
							if (!new File(outputDir).exists()) {
								new File(outputDir).mkdirs();
							}

						crawler.crawl(outputDir, limit, "" ,keyword, chamber, congress, random, monitor, fields);
						try {
							Display.getDefault().syncExec(new Runnable() {

								@Override
								public void run() {

									CorpusClass cc = new CorpusClass("Congress"+chamber, outputDir);
									cc.setParent(corpus);
									corpus.addClass(cc);

								}
							});
						} catch (Exception e) {
							e.printStackTrace();
							return Status.CANCEL_STATUS;
						}

						}
						else{
							for(final String senator: selectedSenators){
								
								outputDir = IUsCongressCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName+ File.separator + senator;
								if (!new File(outputDir).exists()) {
									new File(outputDir).mkdirs();
								}
								crawler.crawl(outputDir, limit, senator ,keyword, chamber, congress, random, monitor, fields);
								try {
									Display.getDefault().syncExec(new Runnable() {

										@Override
										public void run() {

											CorpusClass cc = new CorpusClass(senator, outputDir);
											cc.setParent(corpus);
											corpus.addClass(cc);

										}
									});
								} catch (Exception e) {
									e.printStackTrace();
									return Status.CANCEL_STATUS;
								}

							}
						}
						if (monitor.isCanceled())
							return handledCancelRequest("Crawling is Stopped");
					} catch (Exception e) {
						return handleException(monitor, e, "Crawling failed. Provide valid data");
					}
					
					ManageCorpora.saveCorpus(corpus);
					ConsoleView.printlInConsoleln("Created corpus: "+corpusName);
				if (monitor.isCanceled())
					return handledCancelRequest("Crawling is Stopped");

				monitor.worked(100);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		canProceed = canItProceed();
		if (canProceed) {
			job.schedule(); // schedule the job
			job.addJobChangeListener(new JobChangeAdapter() {

				public void done(IJobChangeEvent event) {
					if (!event.getResult().isOK()) {
						TacitFormComposite
								.writeConsoleHeaderBegining("Error: <Terminated> US Congress Crawler  ");
						TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is stopped",
								IStatus.INFO, form);

					} else {
						TacitFormComposite
								.writeConsoleHeaderBegining("Success: <Completed> US Congress Crawler  ");
						TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is completed",
								IStatus.INFO, form);
						ConsoleView.printlInConsoleln("Done");
						ConsoleView.printlInConsoleln("US Congress Crawler completed successfully.");

					}
				}
			});
		}
	
	}
	
}
