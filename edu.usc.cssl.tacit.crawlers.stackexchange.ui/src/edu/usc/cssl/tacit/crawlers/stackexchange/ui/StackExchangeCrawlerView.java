package edu.usc.cssl.tacit.crawlers.stackexchange.ui;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.IMessageManager;
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
import edu.usc.cssl.tacit.crawlers.stackexchange.services.StackConstants;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.StackExchangeApi;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.StackExchangeCrawler;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.StackExchangeSite;
import edu.usc.cssl.tacit.crawlers.stackexchange.ui.internal.IStackExchangeCrawlerUIConstants;
import edu.usc.cssl.tacit.crawlers.stackexchange.ui.internal.StackExchangeCrawlerViewImageRegistry;
import edu.usc.cssl.tacit.crawlers.stackexchange.ui.preferencepage.IStackExchangeConstants;

public class StackExchangeCrawlerView extends ViewPart implements IStackExchangeCrawlerUIConstants {
	public static String ID = "edu.usc.cssl.tacit.crawlers.stackexchange.ui.view1";

	private ScrolledForm form;
	private FormToolkit toolkit;
	private StackExchangeCrawler crawler = new StackExchangeCrawler();
	private StackExchangeSite scs = crawler.stackoverflow(new StackExchangeApi(), null);
	private Text queryText;
	private Text pageText;
	private Text corpusNameTxt;
	private boolean[] jsonFilter = new boolean[6];
	private Table senatorTable;
	private Button addSenatorBtn;
	private ElementListSelectionDialog listDialog;
	private List<String> selectedRepresentatives;
	private Button removeSenatorButton;
	private Text answerCount;
	private Text commentCount;
	String subredditText;
	int redditCount = 1;
	String oldSubredditText;
	ArrayList<String> content;
	private SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
	private DateTime fromDate, toDate;
	private Date maxDate;
	private Date minDate;
	private Button dateRange;
	private Button Creation, Activity, Votes;  

	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(form.getForm());
		form.setImage(StackExchangeCrawlerViewImageRegistry.getImageIconFactory().getImage(IStackExchangeCrawlerUIConstants.IMAGE_STACK_OBJ));
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
		sortType.setText("Select Domains*:");
		senatorTable = new Table(InputSectionClient, SWT.BORDER | SWT.MULTI);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 3).hint(90, 50).applyTo(senatorTable);

		Composite buttonComp = new Composite(InputSectionClient, SWT.NONE);
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
				final String mainArray[] = StackConstants.sortTypes;
				ILabelProvider lp = new ArrayLabelProvider();
				listDialog = new ElementListSelectionDialog(addSenatorBtn.getShell(), lp);
				listDialog.setTitle("Select domain");
				listDialog.setMessage("Type the name of the domain");
				listDialog.setMultipleSelection(true);
				listDialog.setElements(mainArray);
				if (listDialog.open() == Window.OK) {
					updateTable(listDialog.getResult());
				}
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
					selectedRepresentatives.remove(item.getText());
					item.dispose();
				}
				if (selectedRepresentatives.size() == 0) {
					removeSenatorButton.setEnabled(false);
				}
			}
		});
		removeSenatorButton.setEnabled(false);

		Composite inputSec = new Composite(InputSectionClient, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 0).indent(0, 0).applyTo(inputSec);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(inputSec);

		Label textLabel = new Label(InputSectionClient, SWT.NONE);
		textLabel.setText("Search:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(textLabel);

		queryText = new Text(InputSectionClient, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(queryText);
		queryText.setMessage("Search by \"tags\", seperate multiple tags by ;");

		TacitFormComposite.createEmptyRow(toolkit, InputSectionClient);

		Composite inputSec1 = new Composite(InputSectionClient, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 0).indent(0, 0).applyTo(inputSec1);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(inputSec1);

		createStoredAttributesSection(toolkit, inputSec1, form.getMessageManager());

		Label dummy1 = toolkit.createLabel(InputSectionClient, "", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0).applyTo(dummy1);

		Group dateGroup = new Group(InputSectionClient, SWT.SHADOW_IN);
		GridDataFactory.fillDefaults().grab(true, false).span(4, 0).applyTo(dateGroup);
		dateGroup.setText("Filter Results");
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(dateGroup);

		final Composite limitClient = new Composite(dateGroup, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).indent(10, 10).applyTo(limitClient);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(limitClient);

		Label limitPages = new Label(limitClient, SWT.NONE);
		limitPages.setText("Limit pages per request: (30 records per page):");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(limitPages);
		pageText = new Text(limitClient, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(pageText);
		pageText.setEnabled(true);
		pageText.setText("2");
		Label limitanswers = new Label(limitClient, SWT.NONE);
		limitanswers.setText("Limit answers per question:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(limitanswers);
		answerCount = new Text(limitClient, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(answerCount);
		answerCount.setEnabled(true);
		answerCount.setText("5");
		Label limitcomments = new Label(limitClient, SWT.NONE);
		limitcomments.setText("Limit comments per question:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(limitcomments);
		commentCount = new Text(limitClient, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(commentCount);
		commentCount.setEnabled(true);
		commentCount.setText("5");
		Label sort = new Label(limitClient, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0).applyTo(sort);
		sort.setText("Crawl Order");
		
		Composite radioGroup = new Composite(limitClient, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 0).indent(0, 0).applyTo(radioGroup);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(radioGroup);
		Creation = new Button(radioGroup, SWT.RADIO);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(Creation);
		Creation.setText("Recently created");
		Activity = new Button(radioGroup, SWT.RADIO);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(Activity);
		Activity.setText("Highest Recent Activity");
		Activity.setSelection(true);
		Votes = new Button(radioGroup, SWT.RADIO);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(Votes);
		Votes.setText("Highest votes");
		

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

	public void updateTable(Object[] result) {
		if (selectedRepresentatives == null) {
			selectedRepresentatives = new ArrayList<String>();
		}

		for (Object object : result) {
			if (!selectedRepresentatives.contains((String) object))
				selectedRepresentatives.add((String) object);
		}
		senatorTable.removeAll();
		for (String itemName : selectedRepresentatives) {
			TableItem item = new TableItem(senatorTable, 0);
			item.setText(itemName);
			if (!removeSenatorButton.isEnabled()) {
				removeSenatorButton.setEnabled(true);
			}
		}

	}

	static class ArrayLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			return (String) element;
		}
	}

	private boolean canItProceed() {
		form.getMessageManager().removeAllMessages();

		String k = CommonUiActivator.getDefault().getPreferenceStore().getString(IStackExchangeConstants.CONSUMER_KEY);
		if (k == null || k.equals("")) {
			form.getMessageManager().addMessage("KeyError", "You have not entered a key for crawling", null,
					IMessageProvider.ERROR);
			ErrorDialog.openError(Display.getDefault().getActiveShell(), "Key has not been added",
					"Please check user settings for StackExchange Crawler",
					new Status(IStatus.ERROR, CommonUiActivator.PLUGIN_ID, "No key found"));
			String id = "edu.usc.cssl.tacit.crawlers.stackexchange.ui.config";
			PreferencesUtil
					.createPreferenceDialogOn(Display.getDefault().getActiveShell(), id, new String[] { id }, null)
					.open();
			return false;
		} else {
			form.getMessageManager().removeMessage("KeyError");
		}
		try{
		if(selectedRepresentatives.isEmpty()){
			form.getMessageManager().addMessage("DomainError", "Enter atleast one domain name", null,
					IMessageProvider.ERROR);
			return false;
		}else{
			form.getMessageManager().removeMessage("DomainError");
		}
		}catch(Exception e){
			form.getMessageManager().addMessage("DomainError", "Enter atleast one domain name", null,
					IMessageProvider.ERROR);
			return false;
		}
		try {
			int pages = Integer.parseInt(pageText.getText());
			if (pages < 1) {
				form.getMessageManager().addMessage("pageLimit", "Enter the number of pages to be crawled", null,
						IMessageProvider.ERROR);
				return false;
			} else
				form.getMessageManager().removeMessage("pageLimit");
		} catch (Exception e) {
			form.getMessageManager().addMessage("pageLimit", "Enter the number of pages to be crawled", null,
					IMessageProvider.ERROR);
			return false;
		}


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
			Corpus corpus;
			int pages;
			String tags;
			boolean canProceed;
			boolean isDate;
			int ansLimit, comLimit;
			Long from, to;
			String crawlOrder;
			
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
								tags = queryText.getText();
								pages = Integer.parseInt(pageText.getText());
								corpusName = corpusNameTxt.getText();
								isDate = dateRange.getSelection();
								jsonFilter[0] = questionUserBtn.getSelection();
								jsonFilter[1] = ansUserBtn.getSelection();
								jsonFilter[2] = commentUserBtn.getSelection();
								jsonFilter[3] = isAnsweredBtn.getSelection();
								jsonFilter[4] = answerBodyBtn.getSelection();
								jsonFilter[5] = commentBodyBtn.getSelection();
								ansLimit = Integer.parseInt(answerCount.getText().trim());
								comLimit = Integer.parseInt(commentCount.getText().trim());
								if(Creation.getSelection())
									crawlOrder = "creation";
								if(Activity.getSelection())
									crawlOrder = "activity";
								if(Votes.getSelection())
									crawlOrder = "votes";
								if (isDate) {
									System.out.print("_____________________________");
									Calendar cal = Calendar.getInstance();
									cal.set(fromDate.getYear(), fromDate.getMonth(), fromDate.getDay());
									from = cal.getTimeInMillis() / 1000;
									cal.set(toDate.getYear(), toDate.getMonth(), toDate.getDay());
									to = cal.getTimeInMillis() / 1000;
									System.out.println(from + "   " + to);
								}
								outputDir = IStackExchangeCrawlerUIConstants.DEFAULT_CORPUS_LOCATION + File.separator
										+ corpusName.trim();
								if (!new File(outputDir).exists()) {
									new File(outputDir).mkdirs();
								}
							}
						});

						int progressSize =selectedRepresentatives.size()*pages*30 + 30;
						monitor.beginTask("Running StackExchange Crawler...", progressSize);
						TacitFormComposite.writeConsoleHeaderBegining("StackExchange Crawler started");
						crawler.setDir(outputDir);
						monitor.subTask("Initializing...");
						monitor.worked(10);
						if (monitor.isCanceled())
							handledCancelRequest("Crawling is Stopped");
						corpus = new Corpus(corpusName, CMDataType.STACKEXCHANGE_JSON);
						for (final String domain : selectedRepresentatives) {
							outputDir = IStackExchangeCrawlerUIConstants.DEFAULT_CORPUS_LOCATION + File.separator
									+ corpusName;
							outputDir += File.separator + domain;
							if (!new File(outputDir).exists()) {
								new File(outputDir).mkdirs();
							}
							crawler.setDir(outputDir);
							try {
								monitor.subTask("Crawling...");
								if (monitor.isCanceled())
									return handledCancelRequest("Crawling is Stopped");
								if (!isDate)
									crawler.search(tags, pages, corpusName, scs,
											StackConstants.domainList.get(domain), jsonFilter, ansLimit, comLimit, crawlOrder, monitor);
								else
									crawler.search(tags, pages, corpusName, scs,
											StackConstants.domainList.get(domain), from, to, jsonFilter, ansLimit,
											comLimit, crawlOrder, monitor);
								if (monitor.isCanceled())
									return handledCancelRequest("Crawling is Stopped");
							} catch (Exception e) {
								return handleException(monitor, e, "Crawling failed. Provide valid data");
							}
							try {
								Display.getDefault().syncExec(new Runnable() {

									@Override
									public void run() {

										CorpusClass cc = new CorpusClass(domain, outputDir);
										cc.setParent(corpus);
										corpus.addClass(cc);

									}
								});
							} catch (Exception e) {
								e.printStackTrace();
								return Status.CANCEL_STATUS;
							}
						}
						ManageCorpora.saveCorpus(corpus);
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
										.writeConsoleHeaderBegining("Error: <Terminated> StackExchange Crawler  ");
								TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is stopped",
										IStatus.INFO, form);

							} else {
								TacitFormComposite
										.writeConsoleHeaderBegining("Success: <Completed> StackExchange Crawler  ");
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
				PlatformUI.getWorkbench().getHelpSystem().displayHelp("edu.usc.cssl.tacit.crawlers.stackexchange.ui.stackexchange");
			};
		};

		mgr.add(helpAction);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(helpAction, "edu.usc.cssl.tacit.crawlers.stackexchange.ui.stackexchange");
		PlatformUI.getWorkbench().getHelpSystem().setHelp(form, "edu.usc.cssl.tacit.crawlers.stackexchange.ui.stackexchange");
		form.getToolBarManager().update(true);
	}

	private IStatus handledCancelRequest(String message) {
		TacitFormComposite.updateStatusMessage(getViewSite(), message, IStatus.INFO, form);
		ConsoleView.printlInConsoleln("StackExchange crawler cancelled.");
		return Status.CANCEL_STATUS;
	}

	private IStatus handleException(IProgressMonitor monitor, Exception e, String message) {
		monitor.done();
		System.out.println(message);
		e.printStackTrace();
		TacitFormComposite.updateStatusMessage(getViewSite(), message, IStatus.ERROR, form);
		return Status.CANCEL_STATUS;
	}

	static Button ansUserBtn, answerBodyBtn, questionTitleBtn, questionBodyBtn, questionUserBtn, commentBodyBtn,
			isAnsweredBtn, commentUserBtn;

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

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);

		Composite sectionClient = toolkit.createComposite(section);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sectionClient);
		section.setClient(sectionClient);

		Label dummy = toolkit.createLabel(sectionClient, "", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0).applyTo(dummy);

		questionUserBtn = new Button(sectionClient, SWT.CHECK);
		questionUserBtn.setText("User Details for Questions");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(questionUserBtn);
		questionUserBtn.setEnabled(true);

		ansUserBtn = new Button(sectionClient, SWT.CHECK);
		ansUserBtn.setText("User Details for Answers");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(ansUserBtn);
		ansUserBtn.setEnabled(true);

		commentUserBtn = new Button(sectionClient, SWT.CHECK);
		commentUserBtn.setText("User Details for Comments");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(commentUserBtn);
		commentUserBtn.setEnabled(true);

		isAnsweredBtn = new Button(sectionClient, SWT.CHECK);
		isAnsweredBtn.setText("Is Question Answered");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(isAnsweredBtn);
		isAnsweredBtn.setEnabled(true);

		answerBodyBtn = new Button(sectionClient, SWT.CHECK);
		answerBodyBtn.setText("Answer Text");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(answerBodyBtn);
		answerBodyBtn.setEnabled(true);

		commentBodyBtn = new Button(sectionClient, SWT.CHECK);
		commentBodyBtn.setText("Comment Text");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(commentBodyBtn);
		commentBodyBtn.setEnabled(true);

	}

}
