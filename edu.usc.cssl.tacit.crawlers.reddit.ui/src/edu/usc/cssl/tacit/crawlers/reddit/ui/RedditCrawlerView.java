package edu.usc.cssl.tacit.crawlers.reddit.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.tacit.crawlers.reddit.ui.internal.IRedditCrawlerViewConstants;
import edu.usc.cssl.tacit.crawlers.reddit.ui.internal.RedditCrawlerViewImageRegistry;

public class RedditCrawlerView extends ViewPart implements IRedditCrawlerViewConstants{

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

	private Button limitComments;

	private Composite labeledDataComposite;

	private Composite trendingDataComposite;

	private Label timeFrame;

	private Composite searchComposite;

	private Text titleText;

	private Text authorText;

	private Text urlText;

	private Composite searchComposite1;

	private Composite searchComposite2;

	private Text linkText;

	private Text queryText;

	private Composite commonsearchComposite;

	private Table subredditTable;

	private Button addSubredditBtn;

	private Button removeSubredditBtn;
	
	@Override
	public void createPartControl(Composite parent) {
		toolkit = createFormBodySection(parent, "Reddit Crawler");
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(section);
		section.setExpanded(true);

		// Create a composite to hold the other widgets
		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
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
					trendingDataComposite.getParent().layout(true);
					// hide - label
					labeledDataComposite.setVisible(false);
					((GridData) labeledDataComposite.getLayoutData()).exclude = true;
					labeledDataComposite.getParent().layout(true);
					// show - search
					searchComposite.setVisible(true);
					((GridData) searchComposite.getLayoutData()).exclude = false;
					searchComposite.getParent().layout(true);
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
					labeledDataComposite.getParent().layout(true);
					// hide - search
					searchComposite.setVisible(false);
					((GridData) searchComposite.getLayoutData()).exclude = true;
					searchComposite.getParent().layout(true);					
					// show - trend
					trendingDataComposite.setVisible(true);
					((GridData) trendingDataComposite.getLayoutData()).exclude = false;
					trendingDataComposite.getParent().layout(true);		
					timeFrame.setEnabled(false);
					cmbTimeFrames.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
			}
		});
		
		crawlLabeledButton = new Button(buttonComposite, SWT.RADIO);
		crawlLabeledButton.setText("Labeled Data");
		crawlLabeledButton.setSelection(false);
		crawlLabeledButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(crawlLabeledButton.getSelection()) {
					// hide - trend
					trendingDataComposite.setVisible(false);
					((GridData) trendingDataComposite.getLayoutData()).exclude = true;
					trendingDataComposite.getParent().layout(true);
					// hide - search
					searchComposite.setVisible(false);
					((GridData) searchComposite.getLayoutData()).exclude = true;
					searchComposite.getParent().layout(true);					
					// show - label
					labeledDataComposite.setVisible(true);
					((GridData) labeledDataComposite.getLayoutData()).exclude = false;
					labeledDataComposite.getParent().layout(true);	
					timeFrame.setEnabled(true);
					cmbTimeFrames.setEnabled(true);
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
		trendType.setText("Trend Type:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(trendType);
		cmbTrendType = new Combo(trendingDataComposite, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(cmbTrendType);
		String trendTypes[] = {"Hot", "New", "Rising"};
		cmbTrendType.setItems(trendTypes);
		cmbTrendType.select(0);	
	
		((GridData) trendingDataComposite.getLayoutData()).exclude = true; // hide this
		
		labeledDataComposite = toolkit.createComposite(mainComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(labeledDataComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(labeledDataComposite);
		
		Label labelType = new Label(labeledDataComposite, SWT.NONE);
		labelType.setText("Label Data:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(labelType);
		cmbLabelType = new Combo(labeledDataComposite, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(cmbLabelType);
		String labelDataTypes[] = {"Top", "Controversial"};
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

		Label subredditLabel = new Label(commonsearchComposite, SWT.NONE);
		subredditLabel.setText("Subreddit:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(subredditLabel);
		
		final Tree subreddits = new Tree(commonsearchComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 0).hint(90, 50).applyTo(subreddits);
		//GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 0);
		//subreddits.setLayoutData(gd_tree);
		TreeItem trainingItem = new TreeItem(subreddits, SWT.NONE);
		trainingItem.setText("Train");
		trainingItem.setData("Train");
		trainingItem.setImage(RedditCrawlerViewImageRegistry.getImageIconFactory().getImage(IMAGE_REDDIT_OBJ));
		TreeItem dummy= new TreeItem(trainingItem, SWT.NONE);
		dummy.setText("Test");

		Composite buttonComp = new Composite(commonsearchComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 0).applyTo(buttonComp);
		GridLayout btnLayout = new GridLayout();
		btnLayout.marginWidth = btnLayout.marginHeight = 0;
		btnLayout.makeColumnsEqualWidth = false;
		buttonComp.setLayout(btnLayout);
		buttonComp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		
		addSubredditBtn = new Button(buttonComp, SWT.PUSH); //$NON-NLS-1$
		addSubredditBtn.setText("Add...");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(addSubredditBtn);
		addSubredditBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//handleAdd(addSubredditBtn.getShell());
			}
		});

		removeSubredditBtn = new Button(buttonComp, SWT.PUSH);
		removeSubredditBtn.setText("Remove...");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(removeSubredditBtn);
		removeSubredditBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		
		Label titleLabel = new Label(searchComposite1, SWT.NONE);
		titleLabel.setText("Title:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(titleLabel);
		titleText = new Text(searchComposite1, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(titleText);
		Label authorLabel = new Label(searchComposite1, SWT.NONE);
		authorLabel.setText("Author:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(authorLabel);
		authorText = new Text(searchComposite1, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(authorText);

		Label urlLabel = new Label(searchComposite2, SWT.NONE);
		urlLabel.setText("URL:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(urlLabel);
		urlText = new Text(searchComposite2, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(urlText);	
		Label linkLabel = new Label(searchComposite2, SWT.NONE);
		linkLabel.setText("Link ID:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(linkLabel);
		linkText = new Text(searchComposite2, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(linkText);			
		
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
		String timeFrames[] = {"All time", "Past 24 hours", "Past hour", "Past week", "Past month", "Past year"};
		cmbTimeFrames.setItems(timeFrames);
		cmbTimeFrames.select(0);
		timeFrame.setEnabled(false);
		cmbTimeFrames.setEnabled(false);

		Label numLinksLabel = new Label(commonParamsGroup, SWT.None);
		numLinksLabel.setText("No.of.Links to Crawl:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(numLinksLabel);
		numLinksText = new Text(commonParamsGroup, SWT.BORDER);
		numLinksText.setText("10");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(numLinksText);
		numLinksText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (!(e.character >= '0' && e.character <= '9')) {
					form.getMessageManager().addMessage("numlinks", "Provide valid no.of.links to crawl", null, IMessageProvider.ERROR);
					numLinksText.setText("10");
				} else {
					form.getMessageManager().removeMessage("numlinks");
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
			}
		});
		
		limitComments = new Button(commonParamsGroup, SWT.CHECK);
		limitComments.setText("Limit comments per link to 200");
		limitComments.setSelection(true);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(limitComments);
		TacitFormComposite.createEmptyRow(toolkit, commonParamsGroup);
		//TacitFormComposite.createEmptyRow(toolkit, parent);
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
			@Override
			public void run() {

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

			};
		};
		
		mgr.add(helpAction);
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(helpAction,
						"edu.usc.cssl.tacit.crawlers.senate.ui.senate");
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(form,
						"edu.usc.cssl.tacit.crawlers.senate.ui.senate");
		form.getToolBarManager().update(true);
	}
}
