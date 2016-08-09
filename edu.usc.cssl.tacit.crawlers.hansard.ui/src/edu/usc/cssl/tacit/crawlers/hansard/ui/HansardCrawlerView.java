package edu.usc.cssl.tacit.crawlers.hansard.ui;

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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import edu.usc.cssl.tacit.crawlers.hansard.HansardDebatesCrawler;
import edu.usc.cssl.tacit.crawlers.hansard.ui.internal.HansardCrawlerViewImageRegistry;
import edu.usc.cssl.tacit.crawlers.hansard.ui.internal.IHansardCrawlerViewConstants;

public class HansardCrawlerView  extends ViewPart implements IHansardCrawlerViewConstants{
	public static String ID = "edu.usc.cssl.tacit.crawlers.hansard.ui.view1";
	
	private ScrolledForm form;
	private FormToolkit toolkit;

	private Button bothButton;
	private Button commonsButton;
	private Button lordsButton;
	
	private Composite searchComposite;

	private Text searchText;
	private Text corpusNameTxt;
	private DateTime toDate;
	private DateTime fromDate;
	private Corpus hansardCorpus;

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

		final Label radioLabel1 = new Label(searchComposite, SWT.NONE);
		radioLabel1.setText("Search House:");
		Group operatorButtonComposite = new Group(searchComposite, SWT.LEFT);
		GridDataFactory.fillDefaults().span(3, 0).indent(0,10).applyTo(operatorButtonComposite);
		//operatorButtonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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
		
		TacitFormComposite.createEmptyRow(toolkit, searchComposite);
		
		final Label searchLabel = new Label(searchComposite, SWT.NONE);
		searchLabel.setText("Search Term:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(searchLabel);
		searchText = new Text(searchComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(searchText);	
		searchText.setMessage("Enter a search term");
		
		Group dateComposite1 = new Group(searchComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(false).applyTo(dateComposite1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).indent(0,20).applyTo(dateComposite1);
		
		final Label fromLabel = new Label(dateComposite1, SWT.NONE);
		fromLabel.setText("From:");
		GridDataFactory.fillDefaults().grab(false, false).indent(10,15).span(1, 0).applyTo(fromLabel);

		fromDate = new DateTime(dateComposite1, SWT.DATE | SWT.DROP_DOWN | SWT.BORDER);
		
		GridDataFactory.fillDefaults().grab(false, false).indent(15,15).span(1, 0).applyTo(fromDate);
	
		final Label toLabel = new Label(dateComposite1, SWT.NONE);
		toLabel.setText("To:");
		GridDataFactory.fillDefaults().grab(false, false).indent(10,15).span(1, 0).applyTo(toLabel);
		
		toDate = new DateTime(dateComposite1, SWT.DATE | SWT.DROP_DOWN | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(false, false).indent(15,15).span(1, 0).applyTo(toDate);

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
			String outputDir;
			String corpusName;	
			String searchString;
			String startDate;
			String endDate;
			String house;
			boolean canProceed; 
			@Override
			public void run() {
				final Job job = new Job("Hansard Crawler") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						TacitFormComposite.setConsoleViewInFocus();
						TacitFormComposite.updateStatusMessage(getViewSite(), null,null, form);
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								
								corpusName = corpusNameTxt.getText();
								searchString = searchText.getText();
								startDate = (fromDate.getDay()<10?"0"+fromDate.getDay():fromDate.getDay())+"/"+(fromDate.getMonth()<10?"0"+fromDate.getMonth():fromDate.getMonth())+"/"+fromDate.getYear();
								endDate = (toDate.getDay()<10?"0"+toDate.getDay():toDate.getDay())+"/"+(toDate.getMonth()<10?"0"+toDate.getMonth():toDate.getMonth())+"/"+toDate.getYear();
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
							System.out.println(startDate);
							System.out.println(endDate);
							rc.crawl(outputDir, searchString.trim(), house, startDate, endDate, monitor);
						}
						catch(Exception e){
							e.printStackTrace();
						}
						monitor.subTask("Initializing...");
						monitor.worked(100);
						if(monitor.isCanceled())
							handledCancelRequest("Cancelled");
						hansardCorpus = new Corpus(corpusName, CMDataType.HANSARD_JSON);
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
									CorpusClass cc = new CorpusClass("Hansard Debates", outputDir);
									cc.setParent(hansardCorpus);
									hansardCorpus.addClass(cc);
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

}
