package edu.usc.cssl.tacit.crawlers.reddit.ui;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
import edu.usc.cssl.tacit.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.tacit.common.ui.validation.OutputPathValidation;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import edu.usc.cssl.tacit.crawlers.reddit.services.RedditCrawler;
import edu.usc.cssl.tacit.crawlers.reddit.ui.internal.IRedditCrawlerViewConstants;
import edu.usc.cssl.tacit.crawlers.reddit.ui.internal.RedditCrawlerViewImageRegistry;

public class RedditCrawlerView extends ViewPart implements IRedditCrawlerViewConstants {
	public static String ID = "edu.usc.cssl.tacit.crawlers.reddit.ui.redditview";
	
	private Button crawlTrendingDataButton;
	private ScrolledForm form;
	private FormToolkit toolkit;
	private Button crawlLabeledButton;
	private Button crawlSearchResultsButton;
	private OutputLayoutData outputLayout;
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
	private Composite commonsearchComposite;
	private Combo cmbSortType;
	
	String subredditText;
	int redditCount = 1;
	String oldSubredditText;
	ArrayList<String> content;
	
	String sortTypes[] = {"Relevance", "Top", "Hot", "New", "Comments"};
	String trendTypes[] = {"Hot", "New", "Rising"};
	HashMap<Integer, String> timeFramesMap = new HashMap<Integer, String>();
	{
		timeFramesMap.put(0, "All");
		timeFramesMap.put(1, "Hour");
		timeFramesMap.put(2, "Day");
		timeFramesMap.put(3, "Week");
		timeFramesMap.put(4, "Month");
		timeFramesMap.put(5, "Year");	
	}
	String timeFrames[] = {"All", "Past hour", "Past 24 hours", "Past week", "Past month", "Past year"};
	//String actualTimeFrames[] = {"All", "Hour", "Day", "Week", "Month", "Year"};
	String labelDataTypes[] = {"Top", "Controversial"};

	private Text subreddits;
	
	@Override
	public void createPartControl(Composite parent) {
		toolkit = createFormBodySection(parent, "Reddit Crawler");
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(section);
		section.setExpanded(true);
		form.setImage(RedditCrawlerViewImageRegistry.getImageIconFactory().getImage(IRedditCrawlerViewConstants.IMAGE_REDDIT_OBJ));

		// Create a composite to hold the other widgets
		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);
	
		// Creates an empty to create a empty space
		TacitFormComposite.createEmptyRow(toolkit, sc);

		// Create a composite that can hold the other widgets
		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1).applyTo(client); // Align the composite section to one column
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(client);		
		
		createCrawlInputParameters(toolkit, client);
		outputLayout = TacitFormComposite.createOutputSection(toolkit, client, form.getMessageManager());
		// Add run and help button on the toolbar
		addButtonsToToolBar();	
	}
	
	private void createCrawlInputParameters(final FormToolkit toolkit, final Composite parent) {
		
		Group buttonComposite = new Group(parent, SWT.LEFT);
		buttonComposite.setText("Crawl");
		buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		buttonComposite.setLayout(layout);

		crawlSearchResultsButton = new Button(buttonComposite, SWT.RADIO);
		crawlSearchResultsButton.setText("Search");
		crawlSearchResultsButton.setSelection(true);
		crawlSearchResultsButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(crawlSearchResultsButton.getSelection()) {
					//hide - trend
					trendingDataComposite.setVisible(false);
					((GridData) trendingDataComposite.getLayoutData()).exclude = true;
					//trendingDataComposite.getParent().layout(true);
					// hide - label
					labeledDataComposite.setVisible(false);
					((GridData) labeledDataComposite.getLayoutData()).exclude = true;
					//labeledDataComposite.getParent().layout(true);
					// show - search
					searchComposite.setVisible(true);
					((GridData) searchComposite.getLayoutData()).exclude = false;
					searchComposite.getParent().layout(true);
					parent.layout(true);
					timeFrame.setEnabled(true);
					cmbTimeFrames.setEnabled(true);	
					form.reflow(true);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
			}
		});

		
		crawlTrendingDataButton = new Button(buttonComposite, SWT.RADIO);
		crawlTrendingDataButton.setText("Trending Data");
		crawlTrendingDataButton.setSelection(false);
		crawlTrendingDataButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(crawlTrendingDataButton.getSelection()) {
					// hide - label
					labeledDataComposite.setVisible(false);
					((GridData) labeledDataComposite.getLayoutData()).exclude = true;
					//labeledDataComposite.getParent().layout(true);
					// hide - search
					searchComposite.setVisible(false);
					((GridData) searchComposite.getLayoutData()).exclude = true;
					//searchComposite.getParent().layout(true);					
					// show - trend
					trendingDataComposite.setVisible(true);
					((GridData) trendingDataComposite.getLayoutData()).exclude = false;
					trendingDataComposite.getParent().layout(true);		
					parent.layout(true);
					timeFrame.setEnabled(false);
					cmbTimeFrames.setEnabled(false);
					toolkit.paintBordersFor(form.getBody());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
			}
		});
		
		crawlLabeledButton = new Button(buttonComposite, SWT.RADIO);
		crawlLabeledButton.setText("Top/Controversial Data");
		crawlLabeledButton.setSelection(false);
		crawlLabeledButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(crawlLabeledButton.getSelection()) {
					// hide - trend
					trendingDataComposite.setVisible(false);
					((GridData) trendingDataComposite.getLayoutData()).exclude = true;
					//trendingDataComposite.getParent().layout(true);
					// hide - search
					searchComposite.setVisible(false);
					((GridData) searchComposite.getLayoutData()).exclude = true;
					//searchComposite.getParent().layout(true);					
					// show - label
					labeledDataComposite.setVisible(true);
					((GridData) labeledDataComposite.getLayoutData()).exclude = false;
					labeledDataComposite.getParent().layout(true);	
					parent.layout(true);
					timeFrame.setEnabled(true);
					cmbTimeFrames.setEnabled(true);
					toolkit.paintBordersFor(form.getBody());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
			}
		});
		
		// create input parameters section
		// main section
		Section inputParamsSection = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(inputParamsSection);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(inputParamsSection);
		inputParamsSection.setText("Input Parameters");
		
		ScrolledComposite sc = new ScrolledComposite(inputParamsSection, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(sc);
			
		Composite mainComposite = toolkit.createComposite(inputParamsSection);
		sc.setContent(mainComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(mainComposite);
		inputParamsSection.setClient(mainComposite);
		
		trendingDataComposite = toolkit.createComposite(mainComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(trendingDataComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(trendingDataComposite);
		
		Label trendType = new Label(trendingDataComposite, SWT.NONE);
		trendType.setText("Select Stream:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(trendType);
		cmbTrendType = new Combo(trendingDataComposite, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(cmbTrendType);
		cmbTrendType.setItems(trendTypes);
		cmbTrendType.select(0);	
	
		((GridData) trendingDataComposite.getLayoutData()).exclude = true; // hide this
		
		labeledDataComposite = toolkit.createComposite(mainComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(labeledDataComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(labeledDataComposite);
		
		Label labelType = new Label(labeledDataComposite, SWT.NONE);
		labelType.setText("Select Stream:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(labelType);
		cmbLabelType = new Combo(labeledDataComposite, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(cmbLabelType);
		cmbLabelType.setItems(labelDataTypes);
		cmbLabelType.select(0);	
		((GridData) labeledDataComposite.getLayoutData()).exclude = true; // hide this
		
		//Gerneal search parameters
		searchComposite = toolkit.createComposite(mainComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(searchComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(searchComposite);
		
		commonsearchComposite = toolkit.createComposite(searchComposite);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(commonsearchComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(commonsearchComposite);
		
		TacitFormComposite.createEmptyRow(toolkit, searchComposite);
		
		// split it into 2		
		Group filterResultsGroup = new Group(searchComposite, SWT.LEFT);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(filterResultsGroup);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(filterResultsGroup);
		filterResultsGroup.setText("Filter Results");
		TacitFormComposite.createEmptyRow(toolkit, filterResultsGroup);
		
		TacitFormComposite.createEmptyRow(toolkit, searchComposite);
		
		searchComposite1 = new Composite(filterResultsGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(searchComposite1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(searchComposite1);
		
		searchComposite2 = new Composite(filterResultsGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(searchComposite2);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(searchComposite2);
		TacitFormComposite.createEmptyRow(toolkit, filterResultsGroup);
		
		Label textLabel = new Label(commonsearchComposite, SWT.NONE);
		textLabel.setText("Text:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(textLabel);
		queryText = new Text(commonsearchComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(queryText);	
		queryText.setMessage("Search for \"text\" in url");
		Label subredditLabel = new Label(commonsearchComposite, SWT.NONE);
		subredditLabel.setText("Subreddit:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(subredditLabel);
		content = new ArrayList<String>();
		subreddits = new Text(commonsearchComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(subreddits);	
		subreddits.setMessage("Provide subreddit names seperated by \",\"");		
		
		Label sortType = new Label(commonsearchComposite, SWT.NONE);
		sortType.setText("Sort Links By:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(trendType);
		cmbSortType = new Combo(commonsearchComposite, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(cmbSortType);
		cmbSortType.setItems(sortTypes);
		cmbSortType.select(0);	
	
		Label titleLabel = new Label(searchComposite1, SWT.NONE);
		titleLabel.setText("Title keyword(s):");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(titleLabel);
		titleText = new Text(searchComposite1, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(titleText);
		titleText.setMessage("Submission title e.g. cats");
		Label authorLabel = new Label(searchComposite1, SWT.NONE);
		authorLabel.setText("Author:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(authorLabel);
		authorText = new Text(searchComposite1, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(authorText);
		authorText.setMessage("User who submitted the post e.g. yknjsnow");
		
		Label siteLabel = new Label(searchComposite2, SWT.NONE);
		siteLabel.setText("Site:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(siteLabel);
		siteText = new Text(searchComposite2, SWT.BORDER);
		siteText.setMessage("e.g. example.com");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(siteText);	
		Label linkLabel = new Label(searchComposite2, SWT.NONE);
		linkLabel.setText("Link ID:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(linkLabel);
		linkText = new Text(searchComposite2, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(linkText);	
		linkText.setMessage("Link ID of the post e.g. 26kbuh");
		
		//common parameters
		Composite commomParamsComposite = toolkit.createComposite(mainComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(commomParamsComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(commomParamsComposite);
		
		Group commonParamsGroup = new Group(commomParamsComposite, SWT.LEFT);
		commonParamsGroup.setText("Limit Records");
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(commonParamsGroup);
		commonParamsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		TacitFormComposite.createEmptyRow(toolkit, commonParamsGroup);

		timeFrame = new Label(commonParamsGroup, SWT.NONE);
		timeFrame.setText("Time Frame:");		
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(timeFrame);
		cmbTimeFrames = new Combo(commonParamsGroup, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(cmbTimeFrames);
		cmbTimeFrames.setItems(timeFrames);
		cmbTimeFrames.select(0);
		//timeFrame.setEnabled(false);
		//cmbTimeFrames.setEnabled(false);

		Label numLinksLabel = new Label(commonParamsGroup, SWT.None);
		numLinksLabel.setText("Limit Links Per Request:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(numLinksLabel);
		numLinksText = new Text(commonParamsGroup, SWT.BORDER);
		numLinksText.setText("10");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(numLinksText);
		
		Label numCommentsLabel = new Label(commonParamsGroup, SWT.None);
		numCommentsLabel.setText("Limit Comments Per Link:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(numCommentsLabel);
		numCommentsText = new Text(commonParamsGroup, SWT.BORDER);
		numCommentsText.setText("200");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(numCommentsText);
		TacitFormComposite.createEmptyRow(toolkit, commonParamsGroup);
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
		
		if(crawlSearchResultsButton.getSelection()) { // perform all validations
			// check if any if the filter is set
			String title = titleText.getText();
			String author = authorText.getText();
			String url = siteText.getText();
			String linkId = linkText.getText();
			String text = queryText.getText();
			String[] temp = subreddits.getText().split(",");
			content.clear(); // remove old contents, subreddits
			for(String s : temp) 
				content.add(s);
			
			if(title.isEmpty() && author.isEmpty() && url.isEmpty() && linkId.isEmpty() && content.size() == 0) {
				if(text.isEmpty()) {
					form.getMessageManager().addMessage("queryText", "Provide valid text to crawl", null, IMessageProvider.ERROR);
					return false;
				}
			} 
			form.getMessageManager().removeMessage("queryText");
		}
		// validation for num links
		try {
			int linksLimit = Integer.parseInt(numLinksText.getText());
			if(linksLimit<0) {
				form.getMessageManager().addMessage("numlinks", "Provide valid no.of.links to crawl", null, IMessageProvider.ERROR);
				return false;
			} else
				form.getMessageManager().removeMessage("numlinks");
		} catch(Exception e) {
			form.getMessageManager().addMessage("numlinks", "Provide valid no.of.links to crawl", null, IMessageProvider.ERROR);
			return false;
		}
		// validation for num comments
		try {
			int commentsLimit = Integer.parseInt(numCommentsText.getText());
			if(commentsLimit<0) {
				form.getMessageManager().addMessage("numcomments", "Provide valid no.of.comments to crawl", null, IMessageProvider.ERROR);
				return false;
			} else
				form.getMessageManager().removeMessage("numcomments");
		} catch(Exception e) {
			form.getMessageManager().addMessage("numcomments", "Provide valid no.of.comments to crawl", null, IMessageProvider.ERROR);
			return false;
		}	
		
		String message = OutputPathValidation.getInstance().validateOutputDirectory(outputLayout.getOutputLabel().getText(), "Output");
		if (message != null) {
			message = outputLayout.getOutputLabel().getText() + " " + message;
			form.getMessageManager().addMessage("output", message, null,IMessageProvider.ERROR);
			return false;
		} else {
			form.getMessageManager().removeMessage("output");
		}
		
		return true;
	}
	
	private void addButtonsToToolBar() {
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (RedditCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_LRUN_OBJ));
			}
			@Override
			public String getToolTipText() {
				return "Crawl";
			}
			
			String outputDir; String query; String title; String author; String site;
			String linkId; String sortType; String trendType; String labelType; String timeFrame;			
			int limitLinks, limitComments;
			boolean search; boolean trendingData; boolean labeledData; boolean canProceed; 
			
			@Override
			public void run() {
				final Job job = new Job("Reddit Crawler") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						TacitFormComposite.setConsoleViewInFocus();
						TacitFormComposite.updateStatusMessage(getViewSite(), null,null, form);
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								search = crawlSearchResultsButton.getSelection();
								trendingData = crawlTrendingDataButton.getSelection();
								labeledData = crawlLabeledButton.getSelection();
								if(search) {
									query = queryText.getText();
									title = titleText.getText();
									author = authorText.getText();
									site = siteText.getText();
									linkId = linkText.getText();
									sortType = sortTypes[cmbSortType.getSelectionIndex()].toLowerCase();
									timeFrame = timeFramesMap.get(cmbTimeFrames.getSelectionIndex()).toLowerCase();
								} else if(trendingData) {
									trendType = trendTypes[cmbTrendType.getSelectionIndex()].toLowerCase();								
								} else if(labeledData) {
									labelType = labelDataTypes[cmbLabelType.getSelectionIndex()].toLowerCase();
									timeFrame = timeFramesMap.get(cmbTimeFrames.getSelectionIndex()).toLowerCase();
								}
								limitLinks = Integer.parseInt(numLinksText.getText());
								limitComments = Integer.parseInt(numCommentsText.getText());						
								outputDir = outputLayout.getOutputLabel().getText();	
							}
						});
						int progressSize = limitLinks+30;
						if(content.size()>0)
							progressSize = (content.size()*limitLinks)+30;
						monitor.beginTask("Running Reddit Crawler..." , progressSize);
						TacitFormComposite.writeConsoleHeaderBegining("Reddit Crawler started");						
						final RedditCrawler rc = new RedditCrawler(outputDir, limitLinks, limitComments, monitor); // initialize all the common parameters	
						
						monitor.subTask("Initializing...");
						monitor.worked(10);
						if(monitor.isCanceled())
							handledCancelRequest("Cancelled");						
						if(search) {
							try {
								monitor.subTask("Crawling...");
								if(monitor.isCanceled()) 
									return handledCancelRequest("Cancelled");								
								rc.search(query, title, author, site, linkId, timeFrame, sortType, content);
								if(monitor.isCanceled())
									return handledCancelRequest("Cancelled");
							} catch (Exception e) {
								return handleException(monitor, e, "Crawling failed. Provide valid data");
							} 
						} else if(trendingData) {
							try {
								monitor.subTask("Crawling...");
								if(monitor.isCanceled())
									return handledCancelRequest("Cancelled");								
								if(monitor.isCanceled())
									return handledCancelRequest("Cancelled");
								rc.crawlTrendingData(trendType);
							} catch (Exception e) {
								return handleException(monitor, e, "Crawling failed. Provide valid data");
							}
						} else if(labeledData) {
							try {
								monitor.subTask("Crawling...");
								if(monitor.isCanceled())
									return handledCancelRequest("Cancelled");																
								if(monitor.isCanceled())
									return handledCancelRequest("Cancelled");								
								rc.crawlLabeledData(labelType, timeFrame);
							} catch (Exception e) {
								return handleException(monitor, e, "Crawling failed. Provide valid data");
							}
						}
						if(monitor.isCanceled())
							return handledCancelRequest("Cancelled");
						
						monitor.worked(100);
						monitor.done();
						ConsoleView.printlInConsoleln("Reddit crawler completed successfully.");
						ConsoleView.printlInConsoleln("Done");
						TacitFormComposite.updateStatusMessage(getViewSite(), "Reddit crawler completed successfully.", IStatus.OK, form);
						TacitFormComposite.writeConsoleHeaderBegining("<terminated> Reddit Crawler");
						return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				canProceed = canItProceed();
				if(canProceed) {
					job.schedule(); // schedule the job
				}				
			}
		});

		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (RedditCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_HELP_CO));
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
						"edu.usc.cssl.tacit.crawlers.reddit.ui.reddit");				
			};
		};
		
		mgr.add(helpAction);
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(helpAction,
						"edu.usc.cssl.tacit.crawlers.reddit.ui.reddit");
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(form,
						"edu.usc.cssl.tacit.crawlers.reddit.ui.reddit");
		form.getToolBarManager().update(true);
	}

	private IStatus handledCancelRequest(String message) {
		TacitFormComposite.updateStatusMessage(getViewSite(), message, IStatus.ERROR, form);
		ConsoleView.printlInConsoleln("Reddit crawler cancelled.");
		TacitFormComposite.writeConsoleHeaderBegining("<terminated> Reddit Crawler");
		return Status.CANCEL_STATUS;
	}

	
	private IStatus handleException(IProgressMonitor monitor, Exception e, String message) {
		monitor.done();
		System.out.println(message);
		e.printStackTrace();
		TacitFormComposite.updateStatusMessage(getViewSite(), message, IStatus.ERROR, form);
		TacitFormComposite.writeConsoleHeaderBegining("<terminated> Reddit Crawler");
		return Status.CANCEL_STATUS;
	}	
}


