package edu.usc.cssl.tacit.crawlers.govtrack.ui;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
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
import edu.usc.cssl.tacit.crawlers.govtrack.services.GovTrackCrawler;
import edu.usc.cssl.tacit.crawlers.govtrack.ui.internal.GovTrackCrawlerViewImageRegistry;
import edu.usc.cssl.tacit.crawlers.govtrack.ui.internal.IGovTrackCrawlerViewConstants;

public class GovTrackCrawlerView  extends ViewPart implements IGovTrackCrawlerViewConstants{
	public static String ID = "edu.usc.cssl.tacit.crawlers.govtrack.ui.view1";
	
	private ScrolledForm form;
	private FormToolkit toolkit;

	private Button limitResults;
	
	private Composite searchComposite;

	private Text searchText;
	private Text congressNumber;
	private Text limitResultsText;
	private Composite commonsearchComposite;
	private Combo selectBillType;	
	private Combo selectCurrentStatus;	
	private Text corpusNameTxt;
	Corpus govtrackCorpus;
	private boolean filesFound;
	private boolean messageDisplayed;
	String chamberTypes[] = {"","House simple resolutions","Senate bills","Senate joint resolution","House bills","Concurrent resolutions originating in the House","Concurrent resolutions originating in the Senate","Joint resolutions originating in the House","Senate simple resolutions"};
	String statusTypes[] = {"","Vetoed (No Override Attempt)","Passed House, Failed Senate ","Passed House & Senate","Agreed To (Constitutional Amendment Proposal)","Passed Senate with Changes","Vetoed & Override Passed Senate, Failed in House","Failed House","Passed Senate, Failed House","Vetoed & House Overrides (Senate Next)","Vetoed & Senate Overrides (House Next)","Passed House with Changes","Failed Cloture","Enacted — Veto Overridden","Agreed To (Concurrent Resolution)","Failed Under Suspension","Agreed To (Simple Resolution)","Pocket Vetoed","Vetoed & Override Failed in House","Conference Report Agreed to by Senate","Failed Senate","Passed Senate","Failed to Resolve Differences","Enacted — Signed by the President","Passed House","Conference Report Agreed to by House","Reported by Committee","Vetoed & Override Passed House, Failed in Senate","Vetoed & Override Failed in Senate","Enacted — By 10 Day Rule","Introduced","Enacted (Unknown Final Step)","Referred to Committee"};
	
	int limit;
	HashMap<Integer, String> currentStatusMap = new HashMap<Integer, String>();
	{
		currentStatusMap.put(0, "");
		currentStatusMap.put(1, "prov_kill_veto");
		currentStatusMap.put(2, "fail_second_senate");
		currentStatusMap.put(3, "passed_bill");
		currentStatusMap.put(4, "passed_constamend");
		currentStatusMap.put(5, "pass_back_senate");	
		currentStatusMap.put(6, "vetoed_override_fail_second_house");	
		currentStatusMap.put(7, "fail_originating_house");	
		currentStatusMap.put(8, "fail_second_house");	
		currentStatusMap.put(9, "override_pass_over_house");	
		currentStatusMap.put(10, "override_pass_over_senate");	
		currentStatusMap.put(11, "pass_back_house");	
		currentStatusMap.put(12, "prov_kill_cloturefailed");	
		currentStatusMap.put(13, "enacted_veto_override");	
		currentStatusMap.put(14, "passed_concurrentres");	
		currentStatusMap.put(15, "prov_kill_suspensionfailed");	
		currentStatusMap.put(16, "passed_simpleres");	
		currentStatusMap.put(17, "vetoed_pocket");	
		currentStatusMap.put(18, "vetoed_override_fail_originating_house");
		currentStatusMap.put(19, "conference_passed_senate");	
		currentStatusMap.put(20, "fail_originating_senate");	
		currentStatusMap.put(21, "pass_over_senate");
		currentStatusMap.put(22, "prov_kill_pingpongfail");	
		currentStatusMap.put(23, "enacted_signed");	
		currentStatusMap.put(24, "pass_over_house");	
		currentStatusMap.put(25, "conference_passed_house");	
		currentStatusMap.put(26, "reported");	
		currentStatusMap.put(27, "vetoed_override_fail_second_senate");	
		currentStatusMap.put(28, "vetoed_override_fail_originating_senate");	
		currentStatusMap.put(29, "enacted_tendayrule");	
		currentStatusMap.put(30, "introduced");	
		currentStatusMap.put(31, "enacted_unknown");	
		currentStatusMap.put(32, "referred");	
		
	}
	HashMap<Integer, String> billTypeMap = new HashMap<Integer, String>();
	{
		billTypeMap.put(0, "");
		billTypeMap.put(1, "house_resolution");
		billTypeMap.put(2, "senate_bill");
		billTypeMap.put(3, "senate_joint_resolution");
		billTypeMap.put(4, "house_bill");
		billTypeMap.put(5, "house_concurrent_resolution");	
		billTypeMap.put(6, "senate_concurrent_resolution");	
		billTypeMap.put(7, "house_joint_resolution");	
		billTypeMap.put(8, "senate_resolution");	
		
	}
	
	
	//variables needed at runtime
	
	String outputDir;
	int billTypeIndex; int currentStatusIndex; String corpusName;			
	boolean canProceed; 
	String query;
	String congress;
	
	@Override
	public void createPartControl(Composite parent) {
		toolkit = createFormBodySection(parent, "GovTrack Crawler");
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
		
		final Label searchLabel2 = new Label(commonsearchComposite, SWT.NONE);
		searchLabel2.setText("Congress number:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(searchLabel2);
		congressNumber = new Text(commonsearchComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(congressNumber);	
		congressNumber.setMessage("Enter a Congress number");

		
		Composite comboComposite1 = new Composite(commonsearchComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(false).applyTo(comboComposite1);
		GridDataFactory.fillDefaults().grab(true, false).indent(0,10).span(2, 0).applyTo(comboComposite1);
		Label selectPresidentSearchLabel = new Label(comboComposite1, SWT.NONE);
		selectPresidentSearchLabel.setText("Bill Type:");
		selectBillType = new Combo(comboComposite1, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(selectBillType);
		selectBillType.setItems(chamberTypes);
		selectBillType.select(0);
		
		
		Composite comboComposite2 = new Composite(commonsearchComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(false).applyTo(comboComposite2);
		GridDataFactory.fillDefaults().grab(true, false).indent(0,10).span(2, 0).applyTo(comboComposite2);
		Label documentCategory = new Label(comboComposite2, SWT.NONE);
		documentCategory.setText("Current status:");
		selectCurrentStatus = new Combo(comboComposite2, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(selectCurrentStatus);
		selectCurrentStatus.setItems(statusTypes);
		selectCurrentStatus.select(0);
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

	private boolean canItProceed() {
		form.getMessageManager().removeAllMessages();
		//Validate congress number
		try {
			if(!congressNumber.getText().isEmpty())
				Integer.parseInt(congressNumber.getText());
		} catch(Exception e) {
			form.getMessageManager().addMessage("congressName", "Provide proper congress number", null, IMessageProvider.ERROR);
			return false;
		}
		
		//Validate corpus name
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

		final Job job = new Job("GovTrack Crawler") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				TacitFormComposite.setConsoleViewInFocus();
				TacitFormComposite.updateStatusMessage(getViewSite(), null,null, form);
				
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						
						corpusName = corpusNameTxt.getText();
						billTypeIndex = selectBillType.getSelectionIndex();
						currentStatusIndex = selectCurrentStatus.getSelectionIndex();
						query = searchText.getText();
						congress = congressNumber.getText();
						if(limitResults.getSelection())
							limit = Integer.parseInt(limitResultsText.getText());
						else
							limit = -1;
						
						outputDir = IGovTrackCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
						if(!new File(outputDir).exists()){
							new File(outputDir).mkdirs();									

						}
				}
				});
				int progressSize = 500338;//+30
				if(limit!=-1){
					progressSize = limit+10;
				}
				monitor.beginTask("Running GovTrack Crawler..." , progressSize);
				TacitFormComposite.writeConsoleHeaderBegining("GovTrack Crawler started");
				final GovTrackCrawler rc = new GovTrackCrawler(); // initialize all the common parameters	

				monitor.subTask("Initializing...");
				monitor.worked(10);
				if(monitor.isCanceled())
				{
					try {
						FileUtils.deleteDirectory(new File(IGovTrackCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName));
					} catch (IOException e) {
						e.printStackTrace();
					}
					handledCancelRequest("Cancelled");
				}
					try {
						monitor.subTask("Crawling...");
						if(monitor.isCanceled()) {
							return handledCancelRequest("Cancelled");					
						} 
							filesFound = rc.crawl(outputDir, query, congress, billTypeMap.get(billTypeIndex), currentStatusMap.get(currentStatusIndex), limit, monitor);
						if(monitor.isCanceled())
							return handledCancelRequest("Cancelled");
					} catch(IndexOutOfBoundsException e){

						filesFound = false;
						
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								messageDisplayed = true;
								MessageDialog dialog = new MessageDialog(null, "Alert", null, "No results were found", MessageDialog.INFORMATION, new String[]{"OK"}, 1);
								int result = dialog.open();
								if (result <= 0){
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

								govtrackCorpus = new Corpus(corpusName, CMDataType.GOVTRACK_JSON);
								
								CorpusClass cc = new CorpusClass("GovTrack", outputDir);
								cc.setParent(govtrackCorpus);
								govtrackCorpus.addClass(cc);
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				}

				System.out.println("files found? "+filesFound);
				
				try {
					ManageCorpora.saveCorpus(govtrackCorpus);

					ConsoleView.printlInConsoleln("Created Corpus: "+ corpusName);
				} catch(Exception e) {
					e.printStackTrace();
					
					if(!messageDisplayed) {
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								messageDisplayed = true;
								MessageDialog dialog = new MessageDialog(null, "Alert", null, "No results were found", MessageDialog.INFORMATION, new String[]{"OK"}, 1);
								int result = dialog.open();
								if (result <= 0){
									dialog.close();
									
								}
							}
						});
						
					}
					
					System.out.println(new File(IGovTrackCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName).exists());
					try {
						FileUtils.deleteDirectory(new File(IGovTrackCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName));
					} catch (IOException e1) {
					}
					ConsoleView.printlInConsoleln("No corpus created");
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
						TacitFormComposite.writeConsoleHeaderBegining("Error: <Terminated> GovTrack Crawler  ");
						TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is stopped",
								IStatus.INFO, form);

					} else {
						TacitFormComposite.writeConsoleHeaderBegining("Success: <Completed> GovTrack Crawler  ");
						TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is completed",
								IStatus.INFO, form);
						ConsoleView.printlInConsoleln("Done");
						ConsoleView.printlInConsoleln("GovTrack Papers crawler completed successfully.");

					}
				}
			});
		}				
	
	}
	private IStatus handledCancelRequest(String message) {
		TacitFormComposite.updateStatusMessage(getViewSite(), message, IStatus.ERROR, form);
		ConsoleView.printlInConsoleln("GovTrack crawler cancelled.");
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
