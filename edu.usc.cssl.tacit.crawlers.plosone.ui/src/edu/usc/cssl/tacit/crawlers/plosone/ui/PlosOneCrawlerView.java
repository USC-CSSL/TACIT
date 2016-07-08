package edu.usc.cssl.tacit.crawlers.plosone.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.common.ui.ICommonUiConstants;
import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CMDataType;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.Corpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import edu.usc.cssl.tacit.crawlers.plosone.services.PLOSOneCrawler;
import edu.usc.cssl.tacit.crawlers.plosone.services.PLOSOneWebConstants;
import edu.usc.cssl.tacit.crawlers.plosone.ui.internal.IPlosOneCrawlerUIConstants;
import edu.usc.cssl.tacit.crawlers.plosone.ui.internal.PlosOneCrawlerViewImageRegistry;
import edu.usc.cssl.tacit.crawlers.plosone.ui.preferencepage.IPlosOneConstants;

public class PlosOneCrawlerView extends ViewPart implements IPlosOneCrawlerUIConstants{
	public static final String ID = "edu.usc.cssl.tacit.crawlers.plosone.ui.plosOneView";
	
	private ScrolledForm form;
	private FormToolkit toolkit;
	
	private static Text wordFilterText;
	private String wordFilter = ""; 
	private static Text authorFilterText;
	private String authorFilter = "";
	
	private static Button titleBtn;
	private static Button conclusionBtn;
	private static Button resultsAndDiscussionBtn;
	private static Button materialAndMethodsBtn;
	private static Button bodyBtn;
	private static Button introductionBtn;
	private static Button abstractBtn;
	private static Button authorBtn;
	private static Button publicationDateBtn;
	private static Button subjectBtn;
	private static Button journalBtn;
	private static Button scoreBtn;
	

	private boolean[] storedAtts;
	
	private static Button limitDocuments;
	private static Label maxLimit;
	private static Text maxText;
	private int maxDocumentLimit = -1;

	private Text corpusNameTxt;
	private static Button wordFilterLbl;
	private boolean wordFilterFlag;
	private static Button authorFilterLbl;
	private boolean authorFilterFlag;


	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(form.getForm());
		form.setText("PLOS ONE Crawler"); //$NON-NLS-1$
		form.setImage(PlosOneCrawlerViewImageRegistry.getImageIconFactory()
				.getImage(IPlosOneCrawlerUIConstants.IMAGE_PLOSONE_OBJ));
		GridLayoutFactory.fillDefaults().applyTo(form.getBody());

		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);

		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(section);
		section.setExpanded(true);

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);
		// TacitFormComposite.addErrorPopup(form.getForm(), toolkit);

		// Output Data
		Composite client1 = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(2).applyTo(client1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(client1);

		createFilterSection(toolkit, client1, form.getMessageManager());
		createLimitSection(toolkit, client1, form.getMessageManager());

		Label dummy = toolkit.createLabel(form.getBody(), "", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(2, 0).applyTo(dummy);

		createStoredAttributesSection(toolkit, form.getBody(), form.getMessageManager());

		Label filler = toolkit.createLabel(form.getBody(), "", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(2, 0).applyTo(filler);

		corpusNameTxt = TacitFormComposite.createCorpusSection(toolkit, form.getBody(), form.getMessageManager());

		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter());
		// form.setMessage("Invalid path", IMessageProvider.ERROR);
		this.setPartName("PLOS ONE Crawler");
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {

			private Job job;

			@Override
			public ImageDescriptor getImageDescriptor() {
				return (PlosOneCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Crawl";
			}

			@Override
			public void run() {
				TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
				TacitFormComposite.writeConsoleHeaderBegining("Crawling PLOS ONE started ");

				// Get the corpus name
				final String corpusName = corpusNameTxt.getText();
				
				// Get stored attribute values
				storedAtts = new boolean[12];
				storedAtts[0] = titleBtn.getSelection();
				storedAtts[1] = authorBtn.getSelection();
				storedAtts[2] = abstractBtn.getSelection();
				storedAtts[3] = introductionBtn.getSelection();
				storedAtts[4] = bodyBtn.getSelection();
				storedAtts[5] = materialAndMethodsBtn.getSelection();
				storedAtts[6] = resultsAndDiscussionBtn.getSelection();
				storedAtts[7] = conclusionBtn.getSelection();
				storedAtts[8] = publicationDateBtn.getSelection();
				storedAtts[9] = subjectBtn.getSelection();
				storedAtts[10] = journalBtn.getSelection();
				storedAtts[11] = scoreBtn.getSelection();
				
				//Get max blog limit
				if (limitDocuments.getSelection()) {
					maxDocumentLimit = Integer.parseInt(maxText.getText());
						
				}else{
					maxDocumentLimit = -1; //Indicates max limit is not imposed
				}
				
				//Get the filter text
				if (authorFilterLbl.getSelection()){
					authorFilter = authorFilterText.getText();
				}else if(wordFilterLbl.getSelection()){
					wordFilter = wordFilterText.getText();
				}
				
				//Get the filter selection flag
				wordFilterFlag = wordFilterLbl.getSelection();
				authorFilterFlag = authorFilterLbl.getSelection();


				job = new Job("PLOS ONE Crawl Job") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {

						try {
							TacitFormComposite.setConsoleViewInFocus();
							TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
							TacitFormComposite.writeConsoleHeaderBegining("PLOS ONE Crawling Started... ");
							
							PLOSOneCrawler plosOneCrawler = new PLOSOneCrawler();
							
							List<String> outputFields = new ArrayList<String>();
							outputFields.add(PLOSOneWebConstants.FIELD_EVERYTHING);
							if (storedAtts[0]){
								outputFields.add(PLOSOneWebConstants.FIELD_TITLE);
							}
							if (storedAtts[1]){
								outputFields.add(PLOSOneWebConstants.FIELD_AUTHOR);
							}
							if (storedAtts[2]){
								outputFields.add(PLOSOneWebConstants.FIELD_ABSTRACT);
							}
							if (storedAtts[3]){
								outputFields.add(PLOSOneWebConstants.FIELD_INTRODUCTION);
							}
							if (storedAtts[4]){
								outputFields.add(PLOSOneWebConstants.FIELD_BODY);
							}
							if (storedAtts[5]){
								outputFields.add(PLOSOneWebConstants.FIELD_MATERIALS_AND_METHODS);
							}
							if (storedAtts[6]){
								outputFields.add(PLOSOneWebConstants.FIELD_RESULTS_AND_DISCUSSION);
							}
							if (storedAtts[7]){
								outputFields.add(PLOSOneWebConstants.FIELD_CONCLUSIONS);
							}
							if (storedAtts[8]){
								outputFields.add(PLOSOneWebConstants.FIELD_PUBLICATION_DATE);
							}
							if (storedAtts[9]){
								outputFields.add(PLOSOneWebConstants.FIELD_SUBJECT);
							}
							if (storedAtts[10]){
								outputFields.add(PLOSOneWebConstants.FIELD_JOURNAL);
							}
							if (storedAtts[11]){
								outputFields.add(PLOSOneWebConstants.FIELD_SCORE);
							}
							
							//Obtaining the api key
							String apiKey = CommonUiActivator.getDefault().getPreferenceStore().getString(IPlosOneConstants.PLOS_ONE_API_KEY);							
							
							//Building the url features for hitting the API
							Map<String,String> urlFeatures = new HashMap<String,String>();
							
							urlFeatures.put(PLOSOneWebConstants.FEATURE_APIKEY, apiKey);
							urlFeatures.put(PLOSOneWebConstants.FEATURE_DOCTYPE, "json");
							urlFeatures.put(PLOSOneWebConstants.FEATURE_FIELDS, plosOneCrawler.getOutputFields(outputFields));
							urlFeatures.put(PLOSOneWebConstants.FEATURE_FILTER_QUERY, "doc_type:full");
							
							if (authorFilterFlag){
								urlFeatures.put(PLOSOneWebConstants.FEATURE_QUERY, plosOneCrawler.getModifiedQuery(PLOSOneWebConstants.FIELD_AUTHOR ,authorFilter));
							}else if(wordFilterFlag){
								urlFeatures.put(PLOSOneWebConstants.FEATURE_QUERY, plosOneCrawler.getModifiedQuery(PLOSOneWebConstants.FIELD_EVERYTHING ,wordFilter));
							}
							//This is the number of documents in a single paged response. Several pages need to be combined.
							urlFeatures.put(PLOSOneWebConstants.FEATURE_ROWS, PLOSOneCrawler.DOCUMENTS_PER_RESPONSE_PAGE + ""); 
							//Initialize start to 0.
							urlFeatures.put(PLOSOneWebConstants.FEATURE_START, "0");
							
							
							int numOfRows;
							if (maxDocumentLimit == -1){
								numOfRows= plosOneCrawler.getNumOfRows(plosOneCrawler.buildURL(urlFeatures));
							}else{
								numOfRows = maxDocumentLimit;
							}
							
							ConsoleView.printlInConsoleln(numOfRows+" documents found in the search result.");
							monitor.beginTask("Crawling plos one...",(int)numOfRows+100);
							
							String corpusClassDir = IPlosOneCrawlerUIConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName + File.separator + corpusName + "_class";
							//String originalCorpusClassDir = CommonUiActivator.getDefault().getPreferenceStore()
									//.getString(ICommonUiConstants.CORPUS_LOCATION) + System.getProperty("file.separator") + corpusName + File.separator + corpusName + "_class";
							if (!new File(corpusClassDir).exists()) {
								new File(corpusClassDir).mkdirs();
							}
							
							//Creating the corpus and corpus class
							ConsoleView.printlInConsoleln("Creating Corpus " + corpusName + "...");
							monitor.subTask("Creating Corpus " + corpusName + "...");
							
							Corpus plosoneCorpus = new Corpus(corpusName, CMDataType.PLOSONE_JSON);
							CorpusClass typepadCorpusClass = new CorpusClass();
							typepadCorpusClass.setClassName(corpusName + "_class");
							typepadCorpusClass.setClassPath(corpusClassDir);
							plosoneCorpus.addClass(typepadCorpusClass);
							monitor.worked(50);
							
							if (monitor.isCanceled()){
								throw new OperationCanceledException();
							}
							
							ConsoleView.printlInConsoleln("Started Crawling...");
							
							//This is the core of the job.
							plosOneCrawler.invokePlosOneCrawler(urlFeatures, maxDocumentLimit, corpusClassDir, corpusName, monitor);
							
							TacitFormComposite.writeConsoleHeaderBegining("<terminated> PLOS ONE Crawling  ");
							TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling completed", IStatus.OK,form);
							
							//Saving the corpus in the tacit corpora
							ConsoleView.printlInConsoleln("Saving Corpus " + corpusName + "...");
							monitor.subTask("Saving Corpus " + corpusName + "...");
							ManageCorpora.saveCorpus(plosoneCorpus);
							monitor.worked(50);
							
							/*//Removing the temporary corpus in json corpora
							ConsoleView.printlInConsoleln("Removed duplicate files..");
							monitor.subTask("Removing duplicate files..");
							File tempCorpus = new File(IPlosOneCrawlerUIConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName);
							if (tempCorpus.exists()){
								deleteDir(tempCorpus);
							}
							monitor.worked(50);*/
							
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
				if (canProceedCrawl()) {
					job.setUser(true);
					job.schedule();
					job.addJobChangeListener(new JobChangeAdapter() {

						public void done(IJobChangeEvent event) {
							if (!event.getResult().isOK()) {
								TacitFormComposite.writeConsoleHeaderBegining("Error: <Terminated> PLOS ONE Crawler  ");
								TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is stopped",
										IStatus.INFO, form);
								
								//Delete the temp corpus if the operation is cancelled or met with an exception.
								File tempCorpus = new File(IPlosOneCrawlerUIConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName);
								if (tempCorpus.exists()){
									deleteDir(tempCorpus);
								}
								

							} else {
								TacitFormComposite.writeConsoleHeaderBegining("Success: <Completed> PLOS ONE Crawler  ");
								TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is completed",
										IStatus.INFO, form);
								ConsoleView.printlInConsoleln("PLOS ONE crawler completed successfully.");
								ConsoleView.printlInConsoleln("Done");

							}
						}
					});
				}
			}

		});
		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (PlosOneCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_HELP_CO));
			}

			@Override
			public String getToolTipText() {
				return "Help";
			}

			@Override
			public void run() {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp("edu.usc.cssl.tacit.crawlers.plosone.ui.plosone");
			};
		};
		mgr.add(helpAction);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(helpAction, "edu.usc.cssl.tacit.crawlers.plosone.ui.plosone");
		PlatformUI.getWorkbench().getHelpSystem().setHelp(form, "edu.usc.cssl.tacit.crawlers.plosone.ui.plosone");
		form.getToolBarManager().update(true);
		toolkit.paintBordersFor(form.getBody());
	}

	
	private boolean canProceedCrawl() {
		String contentKeywords[] = {};
		String authorKeywords[] = {};
		
		TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);

		//Removing any other previous messages with the same key.
		form.getMessageManager().removeMessage("maxlimit");
		form.getMessageManager().removeMessage("wordfilter");
		form.getMessageManager().removeMessage("authorfilter");
		form.getMessageManager().removeMessage("corpusname");
		form.getMessageManager().removeMessage("keyerror");
		form.getMessageManager().removeMessage("storedattribute");
		form.getMessageManager().removeAllMessages();
		
		//Check 1: Check if the user has entered the api key 
		String k = CommonUiActivator.getDefault().getPreferenceStore().getString(IPlosOneConstants.PLOS_ONE_API_KEY);
		if (k == null || k.equals("")) {
			form.getMessageManager().addMessage("keyerror", "You have not entered a key for crawling", null,IMessageProvider.ERROR);
			ErrorDialog.openError(Display.getDefault().getActiveShell(), "Key has not been added",
					"Please check user settings for PLOS ONE Crawler",
					new Status(IStatus.ERROR, CommonUiActivator.PLUGIN_ID, "No key found"));
			String id = "edu.usc.cssl.tacit.crawlers.plosone.ui.configuration";
			PreferencesUtil.createPreferenceDialogOn(Display.getDefault().getActiveShell(), id, new String[] { id }, null).open();
			return false;
		}
		
		
		//Check 2: Check if the the word and author keyword field is not empty and properly formed
		if (wordFilterLbl.getSelection()){
			if(wordFilterText.getText().isEmpty()){
				//Check if the word filter is not empty.
				form.getMessageManager().addMessage("wordfilter", "Word Filter cannot be empty.", null,IMessageProvider.ERROR);
				return false;
			}else{
				//Check if the word filter is properly formed.
				contentKeywords = wordFilterText.getText().split(";");
				if (contentKeywords.length != 0 && !isValidKeywordState(wordFilterText.getText())){
					form.getMessageManager().addMessage("wordfilter", "Word filter is not properly formed.", null,IMessageProvider.ERROR);
					return false;
				}
			}
		}
		
		if (authorFilterLbl.getSelection()){
			if(authorFilterText.getText().isEmpty()){
				//Check if the word filter is not empty.
				form.getMessageManager().addMessage("authorfilter", "Author Filter cannot be empty.", null,IMessageProvider.ERROR);
				return false;
			}else{
				//Check if the word filter is properly formed.
				authorKeywords = authorFilterText.getText().split(";");
				if (authorKeywords.length != 0 && !isValidKeywordState(authorFilterText.getText())){
					form.getMessageManager().addMessage("authorfilter", "Author filter is not properly formed.", null,IMessageProvider.ERROR);
					return false;
				}
			}
		}
		

		//Check 3: Check if the max limit for blogs is valid or not
		if (limitDocuments.getSelection()) {
			
			try {
				long maxBlogLimit = Long.parseLong(maxText.getText());
				if (maxBlogLimit <= 0){
					form.getMessageManager().addMessage("maxlimit","Error: Invalid Max Limit for documents. Please enter valid positive number.", null,IMessageProvider.ERROR);
					return false;
				}

			} catch (NumberFormatException e1) {
				form.getMessageManager().addMessage("maxlimit","Error: Invalid Max Limit for documents. Please enter valid positive number.", null,IMessageProvider.ERROR);
				return false;
			}
		}
		
		
		//Check 4: Check if atleast one of the stored attributes is selected.
		if (!(titleBtn.getSelection() || authorBtn.getSelection() || abstractBtn.getSelection() || introductionBtn.getSelection()
				|| bodyBtn.getSelection() || materialAndMethodsBtn.getSelection() || resultsAndDiscussionBtn.getSelection() || conclusionBtn.getSelection()
				|| publicationDateBtn.getSelection() || subjectBtn.getSelection() || journalBtn.getSelection() || scoreBtn.getSelection())) {
			form.getMessageManager().addMessage("storedattribute", "Error: Select at least one stored attribute", null,
					IMessageProvider.ERROR);
			return false;
		}
		
		
		//Check 5: Check if the output corpus is valid.
		String corpusName = corpusNameTxt.getText();
		
		if(corpusName == null|| corpusName.isEmpty()) {
			form.getMessageManager().addMessage("corpusname", "Provide corpus name", null, IMessageProvider.ERROR);
			return false;
		} else {
			String outputDir = CommonUiActivator.getDefault().getPreferenceStore().getString(ICommonUiConstants.CORPUS_LOCATION) + File.separator + corpusName;
			if(new File(outputDir).exists()){
				form.getMessageManager().addMessage("corpusname", "Corpus already exists", null, IMessageProvider.ERROR);
				return false;
			}

		}
		
		return true;
		
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		form.setFocus();
	}

	public static void createFilterSection(FormToolkit toolkit, Composite parent, final IMessageManager mmng) {
		Section section = toolkit.createSection(parent,
				Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION | Section.TWISTIE);

		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(section);
		section.setText("Filter Settings "); //$NON-NLS-1$
		section.setDescription("Choose values for Filter; Use Semicolon as delimeter to give more than one filter value in field");

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(sc);

		Composite sectionClient = toolkit.createComposite(section);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(sectionClient);
		section.setClient(sectionClient);

		TacitFormComposite.createEmptyRow(toolkit, sectionClient);

		wordFilterLbl = toolkit.createButton(sectionClient, "Word Filter", SWT.RADIO);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(wordFilterLbl);
		wordFilterLbl.setSelection(true);
		
		wordFilterText = toolkit.createText(sectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(wordFilterText);
		wordFilterText.setMessage("For example: artificial intelligence;learning");
		
		authorFilterLbl = toolkit.createButton(sectionClient, "Author Filter", SWT.RADIO);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(authorFilterLbl);
		
		authorFilterText = toolkit.createText(sectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(authorFilterText);
		authorFilterText.setMessage("For example: morteza dehghani");
		authorFilterText.setEditable(false);
		authorFilterText.setEnabled(false);

		wordFilterLbl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (wordFilterLbl.getSelection()) {
					wordFilterText.setEditable(true);
					wordFilterText.setEnabled(true);
					wordFilterText.setText("");
					authorFilterText.setEditable(false);
					authorFilterText.setEnabled(false);
					authorFilterText.setText("");
				} else if (authorFilterLbl.getSelection()) {
					wordFilterText.setEditable(false);
					wordFilterText.setEnabled(false);
					wordFilterText.setText("");
					authorFilterText.setEditable(true);
					authorFilterText.setEnabled(true);
					authorFilterText.setText("");
				}
			}
		});
		
		authorFilterLbl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (wordFilterLbl.getSelection()) {
					wordFilterText.setEditable(true);
					wordFilterText.setEnabled(true);
					wordFilterText.setText("");
					authorFilterText.setEditable(false);
					authorFilterText.setEnabled(false);
					authorFilterText.setText("");
				} else if (authorFilterLbl.getSelection()) {
					wordFilterText.setEditable(false);
					wordFilterText.setEnabled(false);
					wordFilterText.setText("");
					authorFilterText.setEditable(true);
					authorFilterText.setEnabled(true);
					authorFilterText.setText("");
				} 
			}
		});

	}

	public static void createLimitSection(FormToolkit toolkit, Composite parent, final IMessageManager mmng) {
		Section section = toolkit.createSection(parent,Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION | Section.TWISTIE);

		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).applyTo(section);
		section.setText("Limit Documents"); //$NON-NLS-1$

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(6).equalWidth(false).applyTo(sc);

		Composite sectionClient = toolkit.createComposite(section);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(sectionClient);
		section.setClient(sectionClient);

		TacitFormComposite.createEmptyRow(toolkit, sectionClient);

		//Adding the components of Filter Section
		
		limitDocuments = new Button(sectionClient, SWT.CHECK);
		limitDocuments.setText("Limit number of documents to be crawled");
		GridDataFactory.fillDefaults().grab(false, false).span(2, 0).applyTo(limitDocuments);
		
		//Max Limit
		maxLimit = toolkit.createLabel(sectionClient, "Max Limit:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(maxLimit);

		maxText = toolkit.createText(sectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(maxText);
		maxText.setText("100");
		maxText.setEnabled(false);
		
		limitDocuments.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (limitDocuments.getSelection()){
					maxText.setEnabled(true);
				}else{
					maxText.setEnabled(false);
					
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
	}

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

		titleBtn = new Button(sectionClient, SWT.CHECK);
		titleBtn.setText("Title");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(titleBtn);
		titleBtn.setSelection(true);

		authorBtn = new Button(sectionClient, SWT.CHECK);
		authorBtn.setText("Author");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(authorBtn);
		authorBtn.setSelection(true);
		
		abstractBtn = new Button(sectionClient, SWT.CHECK);
		abstractBtn.setText("Abstract");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(abstractBtn);
		abstractBtn.setSelection(true);
		
		introductionBtn = new Button(sectionClient, SWT.CHECK);
		introductionBtn.setText("Introduction");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(introductionBtn);


		bodyBtn = new Button(sectionClient, SWT.CHECK);
		bodyBtn.setText("Body");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(bodyBtn);
		bodyBtn.setSelection(true);
		
		materialAndMethodsBtn = new Button(sectionClient, SWT.CHECK);
		materialAndMethodsBtn.setText("Material and Methods");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(materialAndMethodsBtn);

		resultsAndDiscussionBtn = new Button(sectionClient, SWT.CHECK);
		resultsAndDiscussionBtn.setText("Results and Discussion");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(resultsAndDiscussionBtn);


		conclusionBtn = new Button(sectionClient, SWT.CHECK);
		conclusionBtn.setText("Conclusion");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(conclusionBtn);
		conclusionBtn.setSelection(true);

		publicationDateBtn = new Button(sectionClient, SWT.CHECK);
		publicationDateBtn.setText("Publication Date");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(publicationDateBtn);
		publicationDateBtn.setSelection(true);

		subjectBtn = new Button(sectionClient, SWT.CHECK);
		subjectBtn.setText("Subject");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(subjectBtn);


		journalBtn = new Button(sectionClient, SWT.CHECK);
		journalBtn.setText("Journal");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(journalBtn);


		scoreBtn = new Button(sectionClient, SWT.CHECK);
		scoreBtn.setText("Score");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(scoreBtn);
		scoreBtn.setSelection(true);

	}
	
	/**
	 * This method returns the boolean indicating whether the raw input keyword string is valid or not.
	 * @param rawKeywords user input raw keyword string
	 * @return
	 */
	private static boolean isValidKeywordState(String rawKeywords){
		boolean isValid = true;
		Pattern wordPattern = Pattern.compile("\\p{Punct}");
		Matcher matcher;
		
		if (rawKeywords != null && !rawKeywords.isEmpty()){
			String[] splitKeywords = rawKeywords.split(";");
			
			for (String keyword: splitKeywords){
				keyword = keyword.trim();
				if (keyword.length() != 0){

					matcher = wordPattern.matcher(keyword);
					
					if (matcher.find()){
						isValid &= false; //Indicates that keywords do not match the specified pattern
						break; 
					}
				}else{
					isValid &= false; //Indicates that there is an empty string as a keyword
					break;
				}
			}
		}
		else{
			isValid &= false; //Indicates that there is no keyword is entered  
		}
		
		return isValid;
	}
	
	
	/**
	 * This method deletes the directory with all its file
	 * @param dir
	 * @return
	 */
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
