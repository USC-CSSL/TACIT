package edu.usc.cssl.tacit.crawlers.hansard.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
import edu.usc.cssl.tacit.crawlers.hansard.HansardDebatesCrawler;
import edu.usc.cssl.tacit.crawlers.hansard.ui.internal.HansardCrawlerViewImageRegistry;
import edu.usc.cssl.tacit.crawlers.hansard.ui.internal.IHansardCrawlerViewConstants;

public class HansardCrawlerView  extends ViewPart implements IHansardCrawlerViewConstants{
	public static String ID = "edu.usc.cssl.tacit.crawlers.hansard.ui.view1";
	
	private ScrolledForm form;
	private FormToolkit toolkit;

	private Button searchButton;
	private Button MPButton;
	
	private Button bothButton;
	private Button commonsButton;
	private Button lordsButton;
	
//	private Button allMembersButton;
//	private Button currentMembersButton;
//	private Button formerMembersButton;
	
	

	private int limitRecords = 0;
	private Button addSenatorBtn;
	private Button removeSenatorButton;
	
	private Composite searchComposite;

	private Text keywordSearchText;
	private Text houseMemberSearchText;
	private Text corpusNameTxt;
	private DateTime toDate;
	private DateTime fromDate;
	private Corpus hansardCorpus;
	boolean filesFound = false;
	private Text limitResultsText;
	private Text limitTotalResults;
	private Button limitByDate;
	private Table senatorTable;
	private ListDialog listDialog;
	private ArrayList<String> selectedMPs;
	private ArrayList<String> selectedMPLinks;
	private HashMap<String, String> map;
	private boolean limitByDateFlag = false;
	
	//Variables needed at runtime
	
	String outputDir;
	String corpusName;	
	String searchString;
	String startDate;
	String endDate;
	String house;
	boolean searchFlag;
	boolean canProceed; 
	
	@Override
	public void createPartControl(Composite parent) {
		toolkit = createFormBodySection(parent, "Hansard Debates Crawler");
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(section);
		section.setExpanded(true);
		form.setImage(HansardCrawlerViewImageRegistry.getImageIconFactory().getImage(IHansardCrawlerViewConstants.IMAGE_HANSARD_OBJ));

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
	
	private void createCrawlInputParameters(final FormToolkit toolkit, final Composite parent) {

		// create input parameters section
		// main section
		Section inputParamsSection = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(inputParamsSection);
		GridLayoutFactory.fillDefaults().numColumns(4).applyTo(inputParamsSection);
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
		GridDataFactory.fillDefaults().grab(true, false).span(4, 0).applyTo(searchComposite);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).applyTo(searchComposite);

		
		Group operatorButtonComposite = new Group(mainComposite, SWT.LEFT);
		operatorButtonComposite.setText("Select House:");
		GridDataFactory.fillDefaults().grab(true, false).span(4, 0).indent(0,10).applyTo(operatorButtonComposite);
		GridLayout operatorLayout = new GridLayout();
		operatorLayout.numColumns = 3;
		operatorButtonComposite.setLayout(operatorLayout);
	
		
		bothButton = new Button(operatorButtonComposite, SWT.RADIO);
		bothButton.setText("Both");
		bothButton.setSelection(true);
		
		commonsButton = new Button(operatorButtonComposite, SWT.RADIO);
		commonsButton.setText("Commons");
		commonsButton.setSelection(false);
		
		lordsButton = new Button(operatorButtonComposite, SWT.RADIO);
		lordsButton.setText("Lords");
		lordsButton.setSelection(false);
		
		
		Group searchGroup = new Group(searchComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(searchGroup);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(searchGroup);
		searchGroup.setText("Search type:");

		searchButton = new Button(searchGroup, SWT.RADIO);
		searchButton.setText("Keyword search");
		searchButton.setSelection(true);
		
		MPButton = new Button(searchGroup, SWT.RADIO);
		MPButton.setText("House member search");
		MPButton.setSelection(false);

		Group searchFilterComposite = new Group(mainComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(true).applyTo(searchFilterComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(4, 0).indent(0,20).applyTo(searchFilterComposite);
		searchFilterComposite.setText("Keyword search:");
	
		final Label searchLabel = new Label(searchFilterComposite, SWT.NONE);
		searchLabel.setText("Keyword:");
		GridDataFactory.fillDefaults().grab(false, false).indent(4,10).span(1, 0).applyTo(searchLabel);
		
		keywordSearchText = new Text(searchFilterComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).indent(0,10).span(2, 0).applyTo(keywordSearchText);	
		keywordSearchText.setMessage("Enter a search term");
		
		limitByDate  = new Button(searchFilterComposite, SWT.CHECK);
		limitByDate.setText("Limit by date");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(limitByDate);
		
		Composite fromComposite = new Composite(searchFilterComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).indent(0,10).span(1, 0).applyTo(fromComposite);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(true).applyTo(fromComposite);
		
		final Label fromLabel = new Label(fromComposite, SWT.NONE);
		fromLabel.setText("From:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(fromLabel);

		fromDate = new DateTime(fromComposite, SWT.DATE | SWT.DROP_DOWN | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(fromDate);
		
		Composite toComposite = new Composite(searchFilterComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).indent(0,10).span(1, 0).applyTo(toComposite);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(true).applyTo(toComposite);
		
		final Label toLabel = new Label(toComposite, SWT.NONE);
		toLabel.setText("To:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(toLabel);
		
		toDate = new DateTime(toComposite, SWT.DATE | SWT.DROP_DOWN | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(toDate);
		
		
		final Label limitTotalResultsLabel = new Label(searchFilterComposite, SWT.NONE);
		limitTotalResultsLabel.setText("Limit total results:");
		GridDataFactory.fillDefaults().grab(false, false).indent(4,10).span(1, 0).applyTo(limitTotalResultsLabel);
		
		limitTotalResults = new Text(searchFilterComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).indent(0,10).span(2, 0).applyTo(limitTotalResults);	
		limitTotalResults.setText("1000");
		
		selectedMPs = new ArrayList<String>();
		selectedMPLinks = new ArrayList<String>();
		
		searchButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				if (searchButton.getSelection()) {
					
//					allMembersButton.setEnabled(false);
//					currentMembersButton.setEnabled(false);
//					formerMembersButton.setEnabled(false);
					
					toDate.setEnabled(true);
					fromDate.setEnabled(true);
					keywordSearchText.setEnabled(true);
					limitByDate.setEnabled(true);
					
					houseMemberSearchText.setEnabled(false);
					limitResultsText.setEnabled(false);
					addSenatorBtn.setEnabled(false);
					removeSenatorButton.setEnabled(false);
				}
			}
		});
		
		MPButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				if (MPButton.getSelection()) {
					
//					allMembersButton.setEnabled(true);
//					currentMembersButton.setEnabled(true);
//					formerMembersButton.setEnabled(true);
					
					toDate.setEnabled(false);
					fromDate.setEnabled(false);
					keywordSearchText.setEnabled(false);
					limitByDate.setEnabled(false);
					
					houseMemberSearchText.setEnabled(true);
					limitResultsText.setEnabled(true);
					addSenatorBtn.setEnabled(true);
					removeSenatorButton.setEnabled(true);
				}
			}
		});

		
		
		
		Group houseMemberFilterComposite = new Group(mainComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(houseMemberFilterComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(4, 0).indent(0,20).applyTo(houseMemberFilterComposite);
		houseMemberFilterComposite.setText("House member search:");
	
		
		/*
		 * 
		Group representativesGroup = new Group(houseMemberFilterComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 0).indent(0,10).applyTo(representativesGroup);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(representativesGroup);
		representativesGroup.setText("Select members:");

		allMembersButton = new Button(representativesGroup, SWT.RADIO);
		allMembersButton.setText("Both");
		allMembersButton.setSelection(true);
		
		currentMembersButton = new Button(representativesGroup, SWT.RADIO);
		currentMembersButton.setText("Current members");
		currentMembersButton.setSelection(false);
		
		formerMembersButton = new Button(representativesGroup, SWT.RADIO);
		formerMembersButton.setText("Former members");
		formerMembersButton.setSelection(false);
		
	
		allMembersButton.setEnabled(false);
		currentMembersButton.setEnabled(false);
		formerMembersButton.setEnabled(false);
		
		*/
		final Label memberLabel = new Label(houseMemberFilterComposite, SWT.NONE);
		memberLabel.setText("House Member search:");
		GridDataFactory.fillDefaults().grab(false, false).indent(1,10).span(1, 0).applyTo(memberLabel);
		
		houseMemberSearchText = new Text(houseMemberFilterComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).indent(1,10).span(2, 0).applyTo(houseMemberSearchText);	
		houseMemberSearchText.setMessage("Search a House Member Eg. John");
		houseMemberSearchText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				selectedMPLinks.clear();
				selectedMPs.clear();
				senatorTable.removeAll();
				
			}
		});
		final Label limitResultsLabel = new Label(houseMemberFilterComposite, SWT.NONE);
		limitResultsLabel.setText("Limit results per member:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(limitResultsLabel);
		
		
		limitResultsText = new Text(houseMemberFilterComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(limitResultsText);
		limitResultsText.setText("5");
		
		Label dummy1 = new Label(houseMemberFilterComposite, SWT.NONE);
		dummy1.setText("Congress Member:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(dummy1);

		senatorTable = new Table(houseMemberFilterComposite, SWT.BORDER | SWT.MULTI);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 3).hint(90, 50).applyTo(senatorTable);

		Composite buttonComp = new Composite(houseMemberFilterComposite, SWT.NONE);
		GridLayout btnLayout = new GridLayout();
		btnLayout.marginWidth = btnLayout.marginHeight = 0;
		btnLayout.makeColumnsEqualWidth = false;
		buttonComp.setLayout(btnLayout);
		buttonComp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		

		addSenatorBtn = new Button(buttonComp, SWT.PUSH); // $NON-NLS-1$
		addSenatorBtn.setText("Add...");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(addSenatorBtn);

		addSenatorBtn.setEnabled(true);
		addSenatorBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sHandleAdd(addSenatorBtn.getShell());
			}
		});
		removeSenatorButton = new Button(buttonComp, SWT.PUSH);
		removeSenatorButton.setText("Remove...");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(removeSenatorButton);
		removeSenatorButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				for (TableItem item : senatorTable.getSelection()) {
					int index = selectedMPs.indexOf(item.getText());
					selectedMPs.remove(item.getText());
					selectedMPLinks.remove(index);
					item.dispose();
				}
				if (selectedMPs.size() == 0) {
					removeSenatorButton.setEnabled(false);
				}
			}
		});
		houseMemberSearchText.setEnabled(false);
		limitResultsText.setEnabled(false);
		addSenatorBtn.setEnabled(false);
		removeSenatorButton.setEnabled(false);

		bothButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedMPLinks.clear();
				selectedMPs.clear();
				senatorTable.removeAll();
			}
		});
		
		commonsButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedMPLinks.clear();
				selectedMPs.clear();
				senatorTable.removeAll();
			}
		});
		
		lordsButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {

				selectedMPLinks.clear();
				selectedMPs.clear();
				senatorTable.removeAll();
			}
		});
/*		
		allMembersButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {

				selectedMPLinks.clear();
				selectedMPs.clear();
				senatorTable.removeAll();
			}
		});
		
		currentMembersButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {

				selectedMPLinks.clear();
				selectedMPs.clear();
				senatorTable.removeAll();
			}
		});
		
		formerMembersButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {

				selectedMPLinks.clear();
				selectedMPs.clear();
				senatorTable.removeAll();
			}
		});
*/
	}
	
	private void updateSenatorTable(Object[] result) {
		
		for (Object object : result) {
			selectedMPs.add((String) object);
			selectedMPLinks.add(map.get((String)object));
		}
		//Collections.sort(selectedSenators);
		senatorTable.removeAll();
		for (String itemName : selectedMPs) {
			TableItem item = new TableItem(senatorTable, 0);
			item.setText(itemName);
			if(!removeSenatorButton.isEnabled()) {
				removeSenatorButton.setEnabled(true);
			}
		}

	}
	
	
	private void sHandleAdd(Shell shell) {

		processElementSelectionDialog(shell);
		String search = houseMemberSearchText.getText();
		String house, members = "1";
		if(bothButton.getSelection())
			house = "Both";
		else if(commonsButton.getSelection())
			house = "Commons";
		else
			house = "Lords";
/*		if(allMembersButton.getSelection())
			members = "0";
		else if(currentMembersButton.getSelection())
			members = "1";
		else
			members = "2";
*/
		try {
			map = HansardDebatesCrawler.crawlMPs(search, house, members);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(selectedMPs!=null)
			for(String element: selectedMPs) {
				if(map.containsKey(element)) {
					map.remove(element);
				}
			}
		if(map==null)
		{
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog dialog = new MessageDialog(null, "Alert", null, "No results found for the specified House Member.Kindly try again.", MessageDialog.INFORMATION, new String[]{"OK"}, 1);
					int result = dialog.open();
					if (result <= 0){
						dialog.close();
					}
				}
			});
		}else{
		listDialog.setElements(map.keySet().toArray());
		listDialog.setMultipleSelection(true);
		if (listDialog.open() == Window.OK) {
			updateSenatorTable(listDialog.getResult());
		}}

	}
	
	public void processElementSelectionDialog(Shell shell) {
		ILabelProvider lp = new ArrayLabelProvider();
		listDialog = new ListDialog(shell, lp);
		listDialog.setTitle("Select the Authors from the list");
		listDialog.setMessage("Enter Author name to search");
	}
	static class ArrayLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			return (String) element;
		}
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
	
		Calendar cal = Calendar.getInstance();
		cal.set(fromDate.getYear(), fromDate.getMonth(), fromDate.getDay());
		double from = cal.getTimeInMillis() / 1000;
		cal.set(toDate.getYear(), toDate.getMonth(), toDate.getDay());
		double to = cal.getTimeInMillis() / 1000;
		
		//Validate dates - fromDate should not be ahead of toDate
		if(from>to){
			
			form.getMessageManager().addMessage("queryText", "Invalid dates entered.", null, IMessageProvider.ERROR);
			return false;
		} else {
			form.getMessageManager().removeMessage("queryText");
		}
		
		//Validate corpus name
		String corpusName = corpusNameTxt.getText();
		if(null == corpusName || corpusName.isEmpty()) {
			form.getMessageManager().addMessage("corpusName", "Provide corpus name", null, IMessageProvider.ERROR);
			return false;
		} else {
			String outputDir = IHansardCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
			if (new File(outputDir).exists()) {
				form.getMessageManager().addMessage("corpusName", "Corpus already exists", null, IMessageProvider.ERROR);
				return false;
			}
			else {
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
				return (HansardCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_LRUN_OBJ));
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
				return (HansardCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_HELP_CO));
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
						"edu.usc.cssl.tacit.crawlers.hansard.ui.hansard");				
			};
		};
		
		mgr.add(helpAction);
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(helpAction,
						"edu.usc.cssl.tacit.crawlers.hansard.ui.hansard");
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(form,
						"edu.usc.cssl.tacit.crawlers.hansard.ui.hansard");
		form.getToolBarManager().update(true);
	}

	private IStatus handledCancelRequest(String message) {
		TacitFormComposite.updateStatusMessage(getViewSite(), message, IStatus.ERROR, form);
		ConsoleView.printlInConsoleln("Hansard crawler cancelled.");
		return Status.CANCEL_STATUS;
	}

	private IStatus handleException(IProgressMonitor monitor, Exception e, String message) {
		monitor.done();
		System.out.println(message);
		e.printStackTrace();
		TacitFormComposite.updateStatusMessage(getViewSite(), message, IStatus.ERROR, form);
		return Status.CANCEL_STATUS;
	}	
	
	private void runModule() {

		final Job job = new Job("Hansard Crawler") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				TacitFormComposite.setConsoleViewInFocus();
				TacitFormComposite.updateStatusMessage(getViewSite(), null,null, form);
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						searchFlag = searchButton.getSelection();
						if(searchFlag) {
							startDate = (fromDate.getDay()<10?"0"+fromDate.getDay():fromDate.getDay())+"/"+(fromDate.getMonth()<9?"0"+(int)(fromDate.getMonth()+1):(int)(fromDate.getMonth()+1))+"/"+fromDate.getYear();
							endDate = (toDate.getDay()<10?"0"+toDate.getDay():toDate.getDay())+"/"+(toDate.getMonth()<9?"0"+(int)(toDate.getMonth()+1):(int)(toDate.getMonth()+1))+"/"+toDate.getYear();
							try {
							limitRecords = Integer.parseInt(limitTotalResults.getText());
							}
							catch (Exception e) {
								limitRecords = -1;
							}
							limitByDateFlag = limitByDate.getSelection();	
							searchString = keywordSearchText.getText();
						}
						else
							limitRecords = Integer.parseInt(limitResultsText.getText());
						corpusName = corpusNameTxt.getText();
						if (bothButton.getSelection()) 
							house = "Both";
						else if(lordsButton.getSelection())
							house = "Lords";
						else
							house = "Commons";
						Date dateObj = new Date();
						corpusName+= "_" + dateObj.getTime();
						outputDir = IHansardCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
						if(!new File(outputDir).exists()) {
							new File(outputDir).mkdirs();									
						}
						
				}
				});
				int progressSize = 20000;//+30
				monitor.beginTask("Running Hansard Crawler..." , progressSize);
				TacitFormComposite.writeConsoleHeaderBegining("Hansard Crawler started");
				final HansardDebatesCrawler rc = new HansardDebatesCrawler(); // initialize all the common parameters	
				
				try{
					monitor.worked(1000);
					if(searchFlag)
						filesFound = rc.crawlByKeywordSearch(outputDir, searchString.trim(), house, limitByDateFlag, startDate, endDate, limitRecords, monitor);
					else{
						System.out.println(selectedMPLinks);
						
						filesFound = rc.crawlByHouseMemberSearch(outputDir, selectedMPLinks/*new ArrayList<String>(map.values())*/, limitRecords, monitor);
					}
				}
				catch(IndexOutOfBoundsException e){
					System.out.println(e.getMessage());
					e.printStackTrace();
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog dialog = new MessageDialog(null, "Alert", null, "No results were found", MessageDialog.INFORMATION, new String[]{"OK"}, 1);
							int result = dialog.open();
							if (result <= 0){
								dialog.close();
							}
						}
					});
					
				}catch (Exception e){
					
					e.printStackTrace();
				}
				System.out.println("FILES FOUND ---- "+filesFound);
				monitor.subTask("Initializing...");
				monitor.worked(100);
				if(monitor.isCanceled())
					handledCancelRequest("Cancelled");
				try {
					monitor.subTask("Crawling...");
					if(monitor.isCanceled())
						return handledCancelRequest("Cancelled");								
					if(monitor.isCanceled())
						return handledCancelRequest("Cancelled");
					
				} catch(IndexOutOfBoundsException e){
					
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog dialog = new MessageDialog(null, "Alert", null, "No results were found", MessageDialog.INFORMATION, new String[]{"OK"}, 1);
							int result = dialog.open();
							if (result <= 0) {
								dialog.close();
							}
						}
					});
						
					} catch (Exception e) {
						return handleException(monitor, e, "Crawling failed. Provide valid data");
					}
				
				try {
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							if(filesFound) {
								hansardCorpus = new Corpus(corpusName, CMDataType.HANSARD_JSON);
								CorpusClass cc = new CorpusClass("Hansard Debates", outputDir);
								cc.setParent(hansardCorpus);
								hansardCorpus.addClass(cc);
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				}
				
				try {
					ManageCorpora.saveCorpus(hansardCorpus);
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
						TacitFormComposite.writeConsoleHeaderBegining("Error: <Terminated> Hansard Crawler  ");
						TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is stopped",
								IStatus.INFO, form);

					} else {
						TacitFormComposite.writeConsoleHeaderBegining("Success: <Completed> Hansard Crawler  ");
						TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is completed",
								IStatus.INFO, form);
						ConsoleView.printlInConsoleln("Done");
						ConsoleView.printlInConsoleln("Hansard crawler completed successfully.");

					}
				}
			});
		}				
	
	}
}
