package edu.usc.cssl.tacit.crawlers.govtrack.ui;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CMDataType;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.Corpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import edu.usc.cssl.tacit.crawlers.govtrack.services.ProPublicaCrawler;
import edu.usc.cssl.tacit.crawlers.govtrack.ui.internal.GovTrackCrawlerViewImageRegistry;
import edu.usc.cssl.tacit.crawlers.govtrack.ui.internal.IGovTrackCrawlerViewConstants;
import edu.usc.cssl.tacit.crawlers.govtrack.ui.preferencepage.IProPublicaConstants;


public class GovTrackCrawlerView  extends ViewPart implements IGovTrackCrawlerViewConstants{
	public static String ID = "edu.usc.cssl.tacit.crawlers.govtrack.ui.view1";
	
	private ScrolledForm form;
	private FormToolkit toolkit;

	private Button limitResults;
	
	private Composite searchComposite;

	private Text searchText;
	private Combo selectCongressNumber;
	private Text limitResultsText;
	private Composite commonsearchComposite;
	private Combo selectChamberType;	
	private Combo selectBillType;	
	private Text corpusNameTxt;
	Corpus govtrackCorpus;
	private boolean filesFound;
	private boolean messageDisplayed;
	String chamberTypes[] = {"House", "Senate","Both"};
	String billTypes[] = {"Introduced", "Updated", "Active", "Passed", "Enacted", "Vetoed"};
	String congressNumbers[] = {"105-115","105","106","107","108","109","110","111","112","113","114","115"};
	
	ProPublicaCrawler proPublicaCrawler ;
	
	long limit;
	HashMap<Integer, String> chamberMap = new HashMap<Integer, String>();
	{
		chamberMap.put(0, "house");
		chamberMap.put(1, "senate");
		chamberMap.put(2, "both");

	}
	HashMap<Integer, String> billTypeMap = new HashMap<Integer, String>();
	{
		billTypeMap.put(0, "introduced");
		billTypeMap.put(1, "updated");
		billTypeMap.put(2, "active");
		billTypeMap.put(3, "passed");
		billTypeMap.put(4, "enacted");
		billTypeMap.put(5, "vetoed");			
	}
	
	HashMap<Integer, String> congressNumbersMap = new HashMap<Integer, String>();
	{
		congressNumbersMap.put(0, "all");
		congressNumbersMap.put(1, "105");
		congressNumbersMap.put(2, "106");
		congressNumbersMap.put(3, "107");
		congressNumbersMap.put(4, "108");
		congressNumbersMap.put(5, "109");
		congressNumbersMap.put(6, "110");
		congressNumbersMap.put(7, "111");
		congressNumbersMap.put(8, "112");
		congressNumbersMap.put(9, "113");
		congressNumbersMap.put(10, "114");
		congressNumbersMap.put(11, "115");

	}
	
	//variables needed at runtime
	
	String outputDir;
	int chamberIndex; int billTypeIndex; String corpusName;	int congressNumberIndex;		
	boolean canProceed; 
	String query;
	
	
	@Override
	public void createPartControl(Composite parent) {
		toolkit = createFormBodySection(parent, "ProPublica Crawler");
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(section);
		section.setExpanded(true);
		form.setImage(GovTrackCrawlerViewImageRegistry.getImageIconFactory().getImage(IGovTrackCrawlerViewConstants.IMAGE_GOVTRACK_OBJ));

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
		
		
		TacitFormComposite.createEmptyRow(toolkit, searchComposite);
		
		final Label searchLabel1 = new Label(commonsearchComposite, SWT.NONE);
		searchLabel1.setText("Search term:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(searchLabel1);
		searchText = new Text(commonsearchComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(searchText);	
		searchText.setMessage("Enter a search term");
		

		Composite comboComposite0 = new Composite(commonsearchComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(false).applyTo(comboComposite0);
		GridDataFactory.fillDefaults().grab(true, false).indent(0,10).span(2, 0).applyTo(comboComposite0);
		Label selectCongressNumberLabel = new Label(comboComposite0, SWT.NONE);
		selectCongressNumberLabel.setText("Congress number:");
		selectCongressNumber = new Combo(comboComposite0, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(selectCongressNumber);
		selectCongressNumber.setItems(congressNumbers);
		selectCongressNumber.select(0);
		
		
		Composite comboComposite1 = new Composite(commonsearchComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(false).applyTo(comboComposite1);
		GridDataFactory.fillDefaults().grab(true, false).indent(0,10).span(2, 0).applyTo(comboComposite1);
		Label selectPresidentSearchLabel = new Label(comboComposite1, SWT.NONE);
		selectPresidentSearchLabel.setText("Bill Type:");
		selectChamberType = new Combo(comboComposite1, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(selectChamberType);
		selectChamberType.setItems(chamberTypes);
		selectChamberType.select(0);
		
		
		Composite comboComposite2 = new Composite(commonsearchComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(false).applyTo(comboComposite2);
		GridDataFactory.fillDefaults().grab(true, false).indent(0,10).span(2, 0).applyTo(comboComposite2);
		Label documentCategory = new Label(comboComposite2, SWT.NONE);
		documentCategory.setText("Current status:");
		selectBillType = new Combo(comboComposite2, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(selectBillType);
		selectBillType.setItems(billTypes);
		selectBillType.select(0);
		TacitFormComposite.createEmptyRow(toolkit, commonsearchComposite);
		
		
	
		Group limitResultsGroup = new Group(searchComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(limitResultsGroup);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(limitResultsGroup);
		limitResultsGroup.setText("Limit results");
		limitResults = new Button(limitResultsGroup, SWT.CHECK);
		limitResults.setText("Limit the results");
		
		limitResultsText = new Text(limitResultsGroup, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(limitResultsText);
		limitResultsText.setEnabled(false);
		limitResultsText.setMessage("Number of records crawled");
		limitResults.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (limitResults.getSelection()) {
					limitResultsText.setText("50");
					limitResultsText.setEnabled(true);
				} else {
					limitResultsText.setText("");
					limitResultsText.setEnabled(false);
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

	
	private void addButtonsToToolBar() {
		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter());
		// form.setMessage("Invalid path", IMessageProvider.ERROR);
		this.setPartName("ProPublica Crawler");
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (GovTrackCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_LRUN_OBJ));
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
				return (GovTrackCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_HELP_CO));
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
						"edu.usc.cssl.tacit.crawlers.govtrack.ui.govtrack");				
			};
		};
		
		mgr.add(helpAction);
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(helpAction,
						"edu.usc.cssl.tacit.crawlers.govtrack.ui.govtrack");
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(form,
						"edu.usc.cssl.tacit.crawlers.govtrack.ui.govtrack");
		form.getToolBarManager().update(true);
	}

	private void runModule() {
		
		TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
		TacitFormComposite.writeConsoleHeaderBegining("Crawling ProPublica started ");
		
		corpusName = corpusNameTxt.getText();
		chamberIndex = selectChamberType.getSelectionIndex();
		billTypeIndex = selectBillType.getSelectionIndex();
		query = searchText.getText();
		congressNumberIndex = selectCongressNumber.getSelectionIndex();
		if(limitResults.getSelection())
			limit = Long.parseLong(limitResultsText.getText());
		else
			limit = -1;


		final Job job = new Job("ProPublica Crawler") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					TacitFormComposite.setConsoleViewInFocus();
					TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
					TacitFormComposite.writeConsoleHeaderBegining("ProPublica Crawling Started... ");
					
					//Obtaining the api key
					String apiKey = CommonUiActivator.getDefault().getPreferenceStore().getString(IProPublicaConstants.PROPUBLICA_API_KEY);
					proPublicaCrawler = new ProPublicaCrawler(apiKey);
					
					if (!proPublicaCrawler.isAPIKeyValid()) {
						ConsoleView.printlInConsoleln("There seems to be either something wrong with the server or API key is incorrect.");
						throw new Exception();
					}
					
					monitor.beginTask("Crawling ProPublica...",7000);
					
					String corpusClassDir = IGovTrackCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName + File.separator + corpusName + "_class";
					//String originalCorpusClassDir = CommonUiActivator.getDefault().getPreferenceStore()
							//.getString(ICommonUiConstants.CORPUS_LOCATION) + System.getProperty("file.separator") + corpusName + File.separator + corpusName + "_class";
					if (!new File(corpusClassDir).exists()) {
						new File(corpusClassDir).mkdirs();
					}
					
					//Creating the corpus and corpus class
					//ConsoleView.printlInConsoleln("Creating Corpus " + corpusName + "...");
					monitor.subTask("Creating Corpus " + corpusName + "...");
					
					Corpus plosoneCorpus = new Corpus(corpusName, CMDataType.PROPUBLICA_JSON);
					CorpusClass typepadCorpusClass = new CorpusClass();
					typepadCorpusClass.setClassName(corpusName + "_class");
					typepadCorpusClass.setClassPath(corpusClassDir);
					plosoneCorpus.addClass(typepadCorpusClass);
					monitor.worked(50);
					
					if (monitor.isCanceled()){
						throw new OperationCanceledException();
					}
					
					if (query.equals("")) {
						if (congressNumberIndex == 0) {
							proPublicaCrawler.crawlBillsForAllCongress(chamberMap.get(chamberIndex), billTypeMap.get(billTypeIndex), corpusClassDir + File.separator + corpusName + ".json",monitor,limit);
						}else {
							proPublicaCrawler.crawlBillsForSingleCongress(congressNumbersMap.get(congressNumberIndex), chamberMap.get(chamberIndex), billTypeMap.get(billTypeIndex),  corpusClassDir + File.separator + corpusName + ".json", monitor,limit);
						}
					}else {
						proPublicaCrawler.searchBillsForAllCongress(query, corpusClassDir + File.separator + corpusName + ".json",monitor,limit);
					}
						

					TacitFormComposite.writeConsoleHeaderBegining("<terminated> PLOS Crawling  ");
					TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling completed", IStatus.OK,form);
					
					
					monitor.subTask("Saving Corpus " + corpusName + "...");
					ManageCorpora.saveCorpus(plosoneCorpus);
					monitor.worked(50);
					

					
					monitor.done();
					return Status.OK_STATUS;
					
				}catch (OperationCanceledException e) {
					ConsoleView.printlInConsoleln("Operation Cancelled by the user.");
					monitor.done();
					return Status.CANCEL_STATUS;
				}catch (Exception e){
					e.printStackTrace();
					monitor.done();
					return Status.CANCEL_STATUS;
				}
				
				
			}	
		};
		job.setUser(true);
		canProceed = canItProceed();
		
		if (canProceed) {
			job.setUser(true);
			job.schedule();
			job.addJobChangeListener(new JobChangeAdapter() {

				public void done(IJobChangeEvent event) {
					if (!event.getResult().isOK()) {
						TacitFormComposite.writeConsoleHeaderBegining("Error: <Terminated> ProPublica Crawler  ");
						TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is stopped",
								IStatus.INFO, form);
						
						//Delete the temp corpus if the operation is cancelled or met with an exception.
						File tempCorpus = new File(IGovTrackCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName);
						if (tempCorpus.exists()){
							deleteDir(tempCorpus);
						}
						

					} else {
						TacitFormComposite.writeConsoleHeaderBegining("Success: <Completed> ProPublica Crawler  ");
						TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is completed",
								IStatus.INFO, form);
						long numResults = proPublicaCrawler.getLastCrawlCount();
						ConsoleView.printlInConsoleln(numResults + " bills downloaded.");
						
						if (numResults != 0){
							ConsoleView.printlInConsoleln("Created corpus: "+corpusName);
						}else {
							File tempCorpus = new File(IGovTrackCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName);
							if (tempCorpus.exists()){
								deleteDir(tempCorpus);
							}
						}

						ConsoleView.printlInConsoleln("ProPublica crawler completed successfully.");
						ConsoleView.printlInConsoleln("Done");

					}
				}
			});
		}
		
			
	
	}
	
	
	private boolean canItProceed() {
		TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
		form.getMessageManager().removeAllMessages();
		
		//Check 1: Check if the user has entered the api key 
		String k = CommonUiActivator.getDefault().getPreferenceStore().getString(IProPublicaConstants.PROPUBLICA_API_KEY);
		if (k == null || k.equals("")) {
			form.getMessageManager().addMessage("keyerror", "You have not entered a key for crawling", null,IMessageProvider.ERROR);
			ErrorDialog.openError(Display.getDefault().getActiveShell(), "Key has not been added",
					"Please check user settings for ProPublica Crawler",
					new Status(IStatus.ERROR, CommonUiActivator.PLUGIN_ID, "No key found"));
			String id = "edu.usc.cssl.tacit.crawlers.govtrack.ui.configuration";
			PreferencesUtil.createPreferenceDialogOn(Display.getDefault().getActiveShell(), id, new String[] { id }, null).open();
			return false;
		}

		//Check 2: Validate corpus name
		String corpusName = corpusNameTxt.getText();
		if(null == corpusName || corpusName.isEmpty()) {
			form.getMessageManager().addMessage("corpusName", "Provide corpus name", null, IMessageProvider.ERROR);
			return false;
		} else {
			String outputDir = IGovTrackCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
			if(new File(outputDir).exists()){
				form.getMessageManager().addMessage("corpusName", "Corpus already exists", null, IMessageProvider.ERROR);
				return false;
			}
		}
		
		//Check 3: Check if the max limit is valid or not
		if (limitResults.getSelection()) {
			try {
				long maxLimit = Long.parseLong(limitResultsText.getText());
				if (maxLimit <= 0){
					form.getMessageManager().addMessage("maxlimit","Error: Invalid Max Limit for documents. Please enter valid positive number.", null,IMessageProvider.ERROR);
					return false;
				}

			} catch (NumberFormatException e1) {
				form.getMessageManager().addMessage("maxlimit","Error: Invalid Max Limit for documents. Please enter valid positive number.", null,IMessageProvider.ERROR);
				return false;
			}
		}
		
		return true;
		
	}

	
	private boolean deleteDir(File dir) {
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i = 0; i < children.length; i++) {
	            boolean success = deleteDir(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	    }

	    return dir.delete(); // The directory is empty now and can be deleted.
	}

}
