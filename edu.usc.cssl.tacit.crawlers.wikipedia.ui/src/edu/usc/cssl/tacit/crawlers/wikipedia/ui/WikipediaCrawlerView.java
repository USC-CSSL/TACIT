package edu.usc.cssl.tacit.crawlers.wikipedia.ui;

import java.io.File;
import java.util.ArrayList;

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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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
import edu.usc.cssl.tacit.crawlers.wikipedia.services.Mode1;
import edu.usc.cssl.tacit.crawlers.wikipedia.services.Mode2;
import edu.usc.cssl.tacit.crawlers.wikipedia.ui.internal.IWikipediaCrawlerUIConstants;
import edu.usc.cssl.tacit.crawlers.wikipedia.ui.internal.WikipediaCrawlerViewImageRegistry;

public class WikipediaCrawlerView extends ViewPart implements IWikipediaCrawlerUIConstants {
	public static String ID = "edu.usc.cssl.tacit.crawlers.wikipedia.ui.view1";

	private Mode1 m1;
	private Mode2 m2;
	private ScrolledForm form;
	private FormToolkit toolkit;
	private Button btnKeyword, btnItem;
	private Text queryText;
	private Text pageText;
	private Text corpusNameTxt;
	private static Button btnContent;
	private static Button btnCategory;
	private Button btnRemoveGarbage;
	String subredditText;
	int redditCount = 1;
	String oldSubredditText;
	ArrayList<String> content;

	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(form.getForm());
		form.setText("Wikipedia Crawler");

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

		Composite inputSec = new Composite(InputSectionClient, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).indent(0, 0).applyTo(inputSec);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(inputSec);

		Composite bGroup = new Composite(inputSec, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).indent(0, 0).applyTo(bGroup);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(bGroup);

		btnKeyword = new Button(bGroup, SWT.RADIO);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(btnKeyword);
		btnKeyword.setText("Search by keyword");
		btnItem = new Button(bGroup, SWT.RADIO);
		GridDataFactory.fillDefaults().grab(false, false).span(2, 0).applyTo(btnItem);
		btnItem.setText("Search if item is valid");

		Label textLabel = new Label(inputSec, SWT.NONE);
		textLabel.setText("Search:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(textLabel);

		queryText = new Text(inputSec, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(queryText);
		queryText.setMessage("Search for either keyword or item");

		Label limitPages = new Label(inputSec, SWT.NONE);
		limitPages.setText("Number of Pages:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(limitPages);
		pageText = new Text(inputSec, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(pageText);
		pageText.setMessage("Enter the number pages");

		Label queryLabel = new Label(inputSec, SWT.NONE);
		queryLabel.setText("Search for content or category");
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0).applyTo(queryLabel);

		btnContent = new Button(inputSec, SWT.CHECK);
		btnContent.setText("Content");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(btnContent);
		btnContent.setSelection(false);
		btnCategory = new Button(inputSec, SWT.CHECK);
		btnCategory.setText("Category");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(btnCategory);
		btnCategory.setSelection(false);

		Label filterStratergy = new Label(inputSec, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0).applyTo(filterStratergy);
		filterStratergy.setText("Filter Data");
		btnRemoveGarbage = new Button(inputSec, SWT.RADIO);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(btnRemoveGarbage);
		btnRemoveGarbage.setText("Yes");
		Button btnAllInfo = new Button(inputSec, SWT.RADIO);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(btnAllInfo);
		btnAllInfo.setText("No");

		TacitFormComposite.createEmptyRow(toolkit, inputSec);

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
		boolean isCategory = btnCategory.getSelection();
		boolean isContent = btnContent.getSelection();

		try {
			String tags = queryText.getText();
			if (tags.equals("") || tags == null) {
				form.getMessageManager().addMessage("searcherror",
						"Enter a keyword to crawl", null, IMessageProvider.ERROR);
				return false;
			} else
				form.getMessageManager().removeMessage("searcherror");
		} catch (Exception e) {
			form.getMessageManager().addMessage("searcherror",
					"Enter a keyword to crawl", null, IMessageProvider.ERROR);
			return false;
		}
		if (!isCategory && !isContent) {
			form.getMessageManager().addMessage("KeyError", "Select either category or comment", null,
					IMessageProvider.ERROR);
			return false;
		} else {
			form.getMessageManager().removeMessage("KeyError");
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
			String outputDir = IWikipediaCrawlerUIConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
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
				return (WikipediaCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Crawl";
			}

			String outputDir;
			String corpusName;
			String keyword;
			boolean isKeyword;
			boolean isContent, isCategory;
			boolean filterData;
			Corpus corpus;
			int pages;
			boolean canProceed;

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
								keyword = queryText.getText();
								pages = Integer.parseInt(pageText.getText());
								isKeyword = btnKeyword.getSelection();
								isContent = btnContent.getSelection();
								isCategory = btnCategory.getSelection();
								filterData = btnRemoveGarbage.getSelection();
								corpusName = corpusNameTxt.getText();
								outputDir = IWikipediaCrawlerUIConstants.DEFAULT_CORPUS_LOCATION + File.separator
										+ corpusName.trim();
								if (!new File(outputDir).exists()) {
									new File(outputDir).mkdirs();
								}
							}
						});
						int progressSize = pages + 30;
						monitor.beginTask("Running StackExchange Crawler...", progressSize);
						TacitFormComposite.writeConsoleHeaderBegining("StackExchange Crawler started");
						monitor.subTask("Initializing...");
						monitor.worked(10);
						if (monitor.isCanceled())
							handledCancelRequest("Cancelled");
						corpus = new Corpus(corpusName, CMDataType.WIKI_JSON);
						outputDir = IWikipediaCrawlerUIConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
						outputDir += File.separator + keyword;
						if (!new File(outputDir).exists()) {
							new File(outputDir).mkdirs();
						}
						try {
							monitor.subTask("Crawling...");
							if (monitor.isCanceled())
								return handledCancelRequest("Cancelled");
							if (isKeyword) {
								int selection = 0;
								if (filterData)
									selection = 1;
								m2 = new Mode2(keyword, isContent, isCategory, selection, pages, outputDir, monitor);
								m2.Write();
							} else {
								int selection = 0;
								if (filterData)
									selection = 1;
								m1 = new Mode1(keyword, isContent, isCategory, selection, outputDir);
								m1.Write();
							}

							if (monitor.isCanceled())
								return handledCancelRequest("Cancelled");
						} catch (Exception e) {
							return handleException(monitor, e, "Crawling failed. Provide valid data");
						}
						try {
							Display.getDefault().syncExec(new Runnable() {

								@Override
								public void run() {

									CorpusClass cc = new CorpusClass(keyword, outputDir);
									cc.setParent(corpus);
									corpus.addClass(cc);

								}
							});
						} catch (Exception e) {
							e.printStackTrace();
							return Status.CANCEL_STATUS;
						}
						ManageCorpora.saveCorpus(corpus);
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
								TacitFormComposite
										.writeConsoleHeaderBegining("Error: <Terminated> Wikipedia Crawler  ");
								TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is stopped",
										IStatus.INFO, form);

							} else {
								TacitFormComposite
										.writeConsoleHeaderBegining("Success: <Completed> Wikipedia Crawler  ");
								TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is completed",
										IStatus.INFO, form);
								ConsoleView.printlInConsoleln("Done");
								ConsoleView.printlInConsoleln("Wikipedia crawler completed successfully.");

							}
						}
					});
				}
			}
		});

		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (WikipediaCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_HELP_CO));
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
		ConsoleView.printlInConsoleln("Wikipedia crawler cancelled.");
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
