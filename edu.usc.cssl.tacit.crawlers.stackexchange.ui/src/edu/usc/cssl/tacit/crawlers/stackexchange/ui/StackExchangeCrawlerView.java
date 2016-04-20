package edu.usc.cssl.tacit.crawlers.stackexchange.ui;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import edu.usc.cssl.tacit.crawlers.stackexchange.services.StackConstants;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.StackExchangeApi;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.StackExchangeCrawler;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.StackExchangeSite;
import edu.usc.cssl.tacit.crawlers.stackexchange.ui.internal.IStackExchangeCrawlerUIConstants;
import edu.usc.cssl.tacit.crawlers.stackexchange.ui.internal.StackExchangeCrawlerViewImageRegistry;

public class StackExchangeCrawlerView extends ViewPart implements IStackExchangeCrawlerUIConstants {
	public static String ID = "edu.usc.cssl.tacit.crawlers.stackexchange.ui.view1";

	private Button crawlTrendingDataButton;
	private ScrolledForm form;
	private FormToolkit toolkit;
	private StackExchangeCrawler crawler = new StackExchangeCrawler();
	private StackExchangeSite sc = crawler.stackoverflow(new StackExchangeApi(), null);
	private Button crawlLabeledButton;
	private Button crawlSearchResultsButton;
	private Combo cmbTrendType;
	private Combo cmbLabelType;
	private Combo cmbTimeFrames;
	private Text numLinksText;
	private Text numCommentsText;
	private Composite labeledDataComposite;
	private Composite trendingDataComposite;
	private Label timeFrame;
	private Composite searchComposite;
	private Text titleText;
	private Text authorText;
	private Text siteText;
	private Composite searchComposite1;
	private Composite searchComposite2;
	private Text linkText;
	private Text queryText;
	private Text pageText;
	private Composite commonsearchComposite;
	private Combo cmbSortType;
	private Text subreddits;
	private Text corpusNameTxt;
	private static Button btnAnswer;
	private boolean[] jsonFilter = new boolean[8];
	

	private static Button btnQuestion;

	private static Button btnComment;
	String subredditText;
	int redditCount = 1;
	String oldSubredditText;
	ArrayList<String> content;
	private SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
	private DateTime fromDate, toDate;
	private Date maxDate;
	private Date minDate;
	private Button dateRange;


	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(form.getForm());
		form.setText("StackExchange Crawler");
		
		GridLayoutFactory.fillDefaults().applyTo(form.getBody());

		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);

		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(section);
		section.setExpanded(true);

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);
		TacitFormComposite.addErrorPopup(form.getForm(), toolkit);

		Section inputSection = toolkit.createSection(form.getBody(),
				Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);

		GridDataFactory.fillDefaults().span(3, 1).applyTo(inputSection);
		inputSection.setExpanded(true);
		inputSection.setText("Input Details"); //$NON-NLS-1$

		ScrolledComposite inputsc = new ScrolledComposite(inputSection, SWT.H_SCROLL | SWT.V_SCROLL);
		inputsc.setExpandHorizontal(true);
		inputsc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(inputsc);

		Composite InputSectionClient = toolkit.createComposite(inputSection);
		inputsc.setContent(InputSectionClient);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(inputsc);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(InputSectionClient);
		inputSection.setClient(InputSectionClient);

		Label sortType = new Label(InputSectionClient, SWT.NONE);
		sortType.setText("Select Domain:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(sortType);
		cmbSortType = new Combo(InputSectionClient, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(cmbSortType);
		cmbSortType.setItems(StackConstants.sortTypes);
		cmbSortType.select(0);

		Label textLabel = new Label(InputSectionClient, SWT.NONE);
		textLabel.setText("Search:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(textLabel);
		queryText = new Text(InputSectionClient, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(queryText);
		queryText.setMessage("Search by \"tags\", seperate multiple tags by ;");
		queryText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if(queryText.getText()!=null && !queryText.getText().equals("")){
					ansUserBtn.setEnabled(true);
					answerBodyBtn.setEnabled(true);
					questionBodyBtn.setEnabled(true);
					questionTitleBtn.setEnabled(true);
					questionUserBtn.setEnabled(true);
					isAnsweredBtn.setEnabled(true);
					commentBodyBtn.setEnabled(true);
					commentUserBtn.setEnabled(true);
				}else{
					ansUserBtn.setEnabled(false);
					answerBodyBtn.setEnabled(false);
					questionBodyBtn.setEnabled(false);
					questionTitleBtn.setEnabled(false);
					questionUserBtn.setEnabled(false);
					isAnsweredBtn.setEnabled(false);
					commentBodyBtn.setEnabled(false);
					commentUserBtn.setEnabled(false);
				}
				
			}
		});
		Label limitPages = new Label(InputSectionClient, SWT.NONE);
		limitPages.setText("Limit Pages:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(limitPages);
		pageText = new Text(InputSectionClient, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(pageText);
		pageText.setMessage("Enter the number pages");

		Label queryLabel = new Label(InputSectionClient, SWT.NONE);
		queryLabel.setText("Search for answers, questions and comments");
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0).applyTo(queryLabel);

		btnAnswer = new Button(InputSectionClient, SWT.CHECK);
		btnAnswer.setText("Answers");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(btnAnswer);
		btnAnswer.setSelection(false);
		btnAnswer.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if(btnAnswer.getSelection()){
					ansUserBtn.setEnabled(true);
					answerBodyBtn.setEnabled(true);
				}else{
					ansUserBtn.setEnabled(false);
					answerBodyBtn.setEnabled(false);
				}
			}
		});

		btnQuestion = new Button(InputSectionClient, SWT.CHECK);
		btnQuestion.setText("Questions");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(btnQuestion);
		btnQuestion.setSelection(false);
		btnQuestion.addListener(SWT.Selection, new Listener() {		
			@Override
			public void handleEvent(Event event) {
				if(btnQuestion.getSelection()){
					questionBodyBtn.setEnabled(true);
					questionTitleBtn.setEnabled(true);
					questionUserBtn.setEnabled(true);
					isAnsweredBtn.setEnabled(true);
				}else{
					questionBodyBtn.setEnabled(false);
					questionTitleBtn.setEnabled(false);
					questionUserBtn.setEnabled(false);
					isAnsweredBtn.setEnabled(false);
				}
				
			}
		});

		btnComment = new Button(InputSectionClient, SWT.CHECK);
		btnComment.setText("Comments");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(btnComment);
		btnComment.setSelection(false);
		btnComment.addListener(SWT.Selection, new Listener() {		
			@Override
			public void handleEvent(Event event) {
				if(btnComment.getSelection()){
					commentBodyBtn.setEnabled(true);
					commentUserBtn.setEnabled(true);
				}else{
					commentBodyBtn.setEnabled(false);
					commentUserBtn.setEnabled(false);
				}
			}
		});

		Label dummy1 = toolkit.createLabel(InputSectionClient, "", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0).applyTo(dummy1);

		Group dateGroup = new Group(InputSectionClient, SWT.SHADOW_IN);
		GridDataFactory.fillDefaults().grab(true, false).span(4, 0).applyTo(dateGroup);
		dateGroup.setText("Date");
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(dateGroup);

		dateRange = new Button(dateGroup, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, false).span(4, 0).indent(10, 10).applyTo(dateRange);
		dateRange.setText("Specify Date Range");

		final Composite dateRangeClient = new Composite(dateGroup, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).indent(10, 10).applyTo(dateRangeClient);
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

		fromDate.addListener(SWT.Selection, new Listener() {
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
		});

		final Label toLabel = new Label(dateRangeClient, SWT.NONE);
		toLabel.setText("To:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(toLabel);
		toDate = new DateTime(dateRangeClient, SWT.DATE | SWT.DROP_DOWN | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(toDate);
		toLabel.setEnabled(false);
		toDate.setEnabled(false);

		toDate.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				int day = toDate.getDay();
				int month = toDate.getMonth() + 1;
				int year = toDate.getYear();
				Date newDate = null;
				try {
					newDate = format.parse(day + "/" + month + "/" + year);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				if (newDate.after(maxDate) || newDate.before(minDate)) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(maxDate);
					toDate.setMonth(cal.get(Calendar.MONTH));
					toDate.setDay(cal.get(Calendar.DAY_OF_MONTH));
					toDate.setYear(cal.get(Calendar.YEAR));
				}
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

		TacitFormComposite.createEmptyRow(toolkit, dateGroup);

		createStoredAttributesSection(toolkit, form.getBody(), form.getMessageManager());

		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1).applyTo(client); // Align
																							// the
																							// composite
																							// section
																							// to
																							// one
																							// column
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(client);

		TacitFormComposite.createEmptyRow(toolkit, client);
		corpusNameTxt = TacitFormComposite.createCorpusSection(toolkit, client, form.getMessageManager());

		addButtonsToToolBar();
	}

	@Override
	public void setFocus() {
		form.setFocus();

	}

	private boolean canItProceed() {
		form.getMessageManager().removeAllMessages();
		
			try {
				String tags = queryText.getText();
				boolean question = btnQuestion.getSelection();
				boolean answer = btnAnswer.getSelection();
				boolean comment = btnComment.getSelection();
				if ((tags.equals("") || tags == null) && !(question || answer || comment)){
					form.getMessageManager().addMessage("searcherror", "Search by tag or Check either answer, question or comment", null,
							IMessageProvider.ERROR);
					return false;
				} else
					form.getMessageManager().removeMessage("searcherror");
			} catch (Exception e) {
				form.getMessageManager().addMessage("searcherror", "Search by tag or Check either answer, question or comment", null,
						IMessageProvider.ERROR);
				return false;
			}
		try {
			int pages = Integer.parseInt(pageText.getText());
			if (pages < 1) {
				form.getMessageManager().addMessage("pageLimit", "Provide valid no.of.Pages to crawl", null,
						IMessageProvider.ERROR);
				return false;
			} else
				form.getMessageManager().removeMessage("pageLimit");
		} catch (Exception e) {
			form.getMessageManager().addMessage("pageLimit", "Provide valid no.of.Pages to crawl", null,
					IMessageProvider.ERROR);
			return false;
		}

		/*
		 * String message =
		 * OutputPathValidation.getInstance().validateOutputDirectory(
		 * outputLayout.getOutputLabel().getText(), "Output"); if (message !=
		 * null) { message = outputLayout.getOutputLabel().getText() + " " +
		 * message; form.getMessageManager().addMessage("output", message,
		 * null,IMessageProvider.ERROR); return false; } else {
		 * form.getMessageManager().removeMessage("output"); }
		 */

		// Validate corpus name
		String corpusName = corpusNameTxt.getText();
		if (null == corpusName || corpusName.isEmpty()) {
			form.getMessageManager().addMessage("corpusName", "Provide corpus name", null, IMessageProvider.ERROR);
			return false;
		} else {
			String outputDir = IStackExchangeCrawlerUIConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
			if (new File(outputDir).exists()) {
				form.getMessageManager().addMessage("corpusName", "Corpus already exists", null,
						IMessageProvider.ERROR);
				return false;
			} else {
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
				return (StackExchangeCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Crawl";
			}

			String outputDir;
			String corpusName;
			int pages;
			String domain;
			boolean question, answer, comment;
			String tags;
			boolean canProceed;
			boolean isDate;
			Long from, to;
			@Override
			public void run() {
				final Job job = new Job("StackExchange Crawler") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						TacitFormComposite.setConsoleViewInFocus();
						TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {

								domain = cmbSortType.getText();
								tags = queryText.getText();
								pages = Integer.parseInt(pageText.getText());
								question = btnQuestion.getSelection();
								answer = btnAnswer.getSelection();
								comment = btnComment.getSelection();
								corpusName = corpusNameTxt.getText();
								isDate = dateRange.getSelection();
								jsonFilter[0] = ansUserBtn.getSelection();
								jsonFilter[1] = answerBodyBtn.getSelection();
								jsonFilter[2] = questionTitleBtn.getSelection();
								jsonFilter[3] = questionBodyBtn.getSelection();
								jsonFilter[4] = questionUserBtn.getSelection();
								jsonFilter[5] = isAnsweredBtn.getSelection();
								jsonFilter[6] = commentBodyBtn.getSelection();
								jsonFilter[7] = commentUserBtn.getSelection();
								
								System.out.println(isDate);
								if(isDate){
									System.out.print("_____________________________");
									Calendar cal = Calendar.getInstance();
									cal.set(fromDate.getYear(),fromDate.getMonth(),fromDate.getDay());
									from = cal.getTimeInMillis()/1000;
									cal.set(toDate.getYear(),toDate.getMonth(),toDate.getDay());
									to = cal.getTimeInMillis()/1000;
									
									System.out.println(from+"   "+to);
								}
								outputDir = IStackExchangeCrawlerUIConstants.DEFAULT_CORPUS_LOCATION + File.separator
										+ corpusName;
								if (!new File(outputDir).exists()) {
									new File(outputDir).mkdir();
								}
							}
						});
						int progressSize = pages + 30;
						monitor.beginTask("Running StackExchange Crawler...", progressSize);
						TacitFormComposite.writeConsoleHeaderBegining("StackExchange Crawler started");
						crawler.setDir(outputDir);
//						StackCaller s = new StackCaller(outputDir, null);
						monitor.subTask("Initializing...");
						monitor.worked(10);
						if (monitor.isCanceled())
							handledCancelRequest("Cancelled");
						try {
							monitor.subTask("Crawling...");
							if (monitor.isCanceled())
								return handledCancelRequest("Cancelled");
							if(!isDate)
								crawler.search(tags,pages,question,answer,comment,corpusName,sc,StackConstants.domainList.get(domain), jsonFilter);
							else
								crawler.search(tags,pages,question,answer,comment,corpusName,sc,StackConstants.domainList.get(domain), from, to, jsonFilter);
							if (monitor.isCanceled())
								return handledCancelRequest("Cancelled");
						} catch (Exception e) {
							return handleException(monitor, e, "Crawling failed. Provide valid data");
						}
						try {
							Display.getDefault().syncExec(new Runnable() {

								@Override
								public void run() {
									Corpus corpus = new Corpus(corpusName, CMDataType.TWITTER_JSON);
									CorpusClass cc= new CorpusClass(domain, outputDir);
									cc.setParent(corpus);
									corpus.addClass(cc);
									ManageCorpora.saveCorpus(corpus);
									
								}});
						} catch (Exception e) {
							e.printStackTrace();
							return Status.CANCEL_STATUS;
						}
						if (monitor.isCanceled())
							return handledCancelRequest("Cancelled");

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
								TacitFormComposite.writeConsoleHeaderBegining("Error: <Terminated> StackExchange Crawler  ");
								TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is stopped",
										IStatus.INFO, form);

							} else {
								TacitFormComposite.writeConsoleHeaderBegining("Success: <Completed> StackExchange Crawler  ");
								TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is completed",
										IStatus.INFO, form);
								ConsoleView.printlInConsoleln("Done");
								ConsoleView.printlInConsoleln("StackExchange crawler completed successfully.");

							}
						}
					});
				}
			}
		});

		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (StackExchangeCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_HELP_CO));
			}

			@Override
			public String getToolTipText() {
				return "Help";
			}

			@Override
			public void run() {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp("edu.usc.cssl.tacit.crawlers.reddit.ui.reddit");
			};
		};

		mgr.add(helpAction);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(helpAction, "edu.usc.cssl.tacit.crawlers.reddit.ui.reddit");
		PlatformUI.getWorkbench().getHelpSystem().setHelp(form, "edu.usc.cssl.tacit.crawlers.reddit.ui.reddit");
		form.getToolBarManager().update(true);
	}

	private IStatus handledCancelRequest(String message) {
		TacitFormComposite.updateStatusMessage(getViewSite(), message, IStatus.ERROR, form);
		ConsoleView.printlInConsoleln("Reddit crawler cancelled.");
		return Status.CANCEL_STATUS;
	}

	private IStatus handleException(IProgressMonitor monitor, Exception e, String message) {
		monitor.done();
		System.out.println(message);
		e.printStackTrace();
		TacitFormComposite.updateStatusMessage(getViewSite(), message, IStatus.ERROR, form);
		return Status.CANCEL_STATUS;
	}

	static Button ansUserBtn, answerBodyBtn, questionTitleBtn, questionBodyBtn, questionUserBtn, commentBodyBtn, isAnsweredBtn,
			commentUserBtn;

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

		ansUserBtn = new Button(sectionClient, SWT.CHECK);
		ansUserBtn.setText("User Details for answers");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(ansUserBtn);
		ansUserBtn.setEnabled(false);
		
		answerBodyBtn = new Button(sectionClient, SWT.CHECK);
		answerBodyBtn.setText("Answer Body");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(answerBodyBtn);
		answerBodyBtn.setEnabled(false);

		questionTitleBtn = new Button(sectionClient, SWT.CHECK);
		questionTitleBtn.setText("Question Title");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(questionTitleBtn);
		questionTitleBtn.setEnabled(false);

		questionBodyBtn = new Button(sectionClient, SWT.CHECK);
		questionBodyBtn.setText("Question Body");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(questionBodyBtn);
		questionBodyBtn.setEnabled(false);

		questionUserBtn = new Button(sectionClient, SWT.CHECK);
		questionUserBtn.setText("User Details for questions");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(questionUserBtn);
		questionUserBtn.setEnabled(false);
		
		isAnsweredBtn = new Button(sectionClient, SWT.CHECK);
		isAnsweredBtn.setText("is Question Answered");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(isAnsweredBtn);
		isAnsweredBtn.setEnabled(false);

		commentBodyBtn = new Button(sectionClient, SWT.CHECK);
		commentBodyBtn.setText("Comment Body");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(commentBodyBtn);
		commentBodyBtn.setEnabled(false);

		commentUserBtn = new Button(sectionClient, SWT.CHECK);
		commentUserBtn.setText("User Details for comments");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(commentUserBtn);
		commentUserBtn.setEnabled(false);

	}

}
