package edu.usc.cssl.tacit.crawlers.gutenberg.ui;

import java.awt.Image;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.tacit.crawlers.gutenberg.ui.internal.IGutenbergCrawlerViewConstants;
import edu.usc.cssl.tacit.crawlers.gutenberg.ui.internal.GutenbergCrawlerViewImageRegistry;
import edu.usc.cssl.tacit.common.Preprocessor;
import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.internal.TargetLocationsGroup;
import edu.usc.cssl.tacit.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import edu.usc.cssl.tacit.crawlers.gutenberg.services.GutenbergConstants;
import edu.usc.cssl.tacit.crawlers.gutenberg.services.*;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.Corpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CMDataType;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;




/**
 * Naive Bayes Classifier View
 */
public class GutenbergCrawlerView extends ViewPart {
	public static String ID = "edu.usc.cssl.tacit.crawlers.gutenberg.ui.view1";

	private ScrolledForm form;
	private FormToolkit toolkit;
	private TableLayoutData classLayoutData;
	
	private Composite searchComposite;
	private Button searchButton;
	private Button MPButton;
	private Text keywordSearchText;
	
	private Button bothButton;
	private Button commonsButton;
	private Button lordsButton;
	private Button domainButton;
	
	Combo domainList;
	private Table subdomainTable;
	private Button addSubdomainBtn;
	private Button removeSubdomainButton;
	
	private Button checkPages;
	private Text pageText;
	private Text corpusNameTxt;
	private ElementListSelectionDialog listDialog;
	
	private List<String> selectedRepresentatives;

	// Classification parameters
	
	private Text outputPath;
	
	

	private boolean canProceed = false;

	protected Job job;

	private boolean checkType = true;
	boolean breakFlag = false;
	
	private boolean isDomain = false;
	private boolean isSearch = false;
	private boolean isLatest = false;
	
	final String[] domains = new String[]{"Animals","Children","Classics","Countries","Crime","Knowledge","Fiction","Fine Arts","General Works","Geography","History","Language and Literature","Law","Music","Periodicals","Psychology and Philosophy","Religion","Science","Social Sciences","Technology","Wars"};                                                  
	//final String[] domains = new String[]{"Animals","Children","Countries","Crime","Knowledge"};
	//final String[] domains = new String[]{"Animals","Children","Countries"};
	
	public org.eclipse.swt.graphics.Image getTitleImage() {
		return GutenbergCrawlerViewImageRegistry.getImageIconFactory().getImage(IGutenbergCrawlerViewConstants.IMAGE_GUTENBERG_OBJ);
	}
	@Override
	public void createPartControl(Composite parent) {
		// Creates toolkit and form
		toolkit = createFormBodySection(parent, "GUTENBERG CRAWLER");
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
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1).applyTo(client);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(client);
		GridLayout layout = new GridLayout();// Layout creation
		layout.numColumns = 2;
		
		createCrawlInputParameters(toolkit, client);
		// Create table layout to hold the input data
		/*
		classLayoutData = TacitFormComposite.createTableSection(client, toolkit, layout, "Input Details",
				"Add Folder(s) or Corpus Classes to include in analysis.", true, false, true, true);
		*/

		
		// Add run and help button on the toolbar 
		addButtonsToToolBar();
		form.setImage(GutenbergCrawlerViewImageRegistry.getImageIconFactory().getImage(IGutenbergCrawlerViewConstants.IMAGE_GUTENBERG_OBJ));

	}

	/**
	 * Opens a "Browse" dialog
	 * 
	 * @param browseBtn
	 * @return
	 */
	protected String openBrowseDialog(Button browseBtn) {
		DirectoryDialog dlg = new DirectoryDialog(browseBtn.getShell(), SWT.OPEN);
		dlg.setText("Open");
		String path = dlg.open();
		return path;
	}


	/**
	 * Checks to ensure read permission of the given location
	 * 
	 * @param location
	 *            - Directory path
	 * @return
	 */
	public String validateInputDirectory(String location) {
		File locationFile = new File(location);
		if (locationFile.canRead()) {
			return null;
		} else {
			return "Classification Input Path : Permission Denied";
		}
	}

	/**
	 * Checks to ensure read permission of the given location
	 * 
	 * @param location
	 *            - path
	 * @return
	 */
	public String validateOutputDirectory(String location) {
		File locationFile = new File(location);
		if (locationFile.canWrite()) {
			return null;
		} else {
			return "Output Path : Permission Denied";
		}
	}

	/**
	 * Validation for "Output path"
	 * 
	 * @param outputText
	 * @param errorMessage
	 *            - error message to be displayed if required
	 * @return
	 */
	protected boolean outputPathListener(Text outputText, String errorMessage) {
		if (outputText.getText().isEmpty()) {
			form.getMessageManager().addMessage("outputPath", errorMessage, null, IMessageProvider.ERROR);
			return false;
		}
		File tempFile = new File(outputText.getText());
		if (!tempFile.exists() || !tempFile.isDirectory()) {
			form.getMessageManager().addMessage("outputPath", errorMessage, null, IMessageProvider.ERROR);
			return false;
		} else {
			form.getMessageManager().removeMessage("outputPath");
			String message = validateOutputDirectory(outputText.getText().toString());
			if (null != message) {
				form.getMessageManager().addMessage("outputPath", message, null, IMessageProvider.ERROR);
				return false;
			}
		}
		return true;
	}
	
	/*Creates the input parameters for the crawler
	 */

	private void createCrawlInputParameters(final FormToolkit toolkit, final Composite parent) {
		
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
		
		searchComposite = toolkit.createComposite(mainComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(4, 0).applyTo(searchComposite);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).applyTo(searchComposite);
		
		Group searchGroup = new Group(searchComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(searchGroup);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(searchGroup);
		searchGroup.setText("Search type:");

		searchButton = new Button(searchGroup, SWT.RADIO);
		searchButton.setText("Popular Search");
		GridDataFactory.fillDefaults().grab(false, false).indent(4,10).span(1, 0).applyTo(searchButton);
		searchButton.setSelection(true);
		
		MPButton = new Button(searchGroup, SWT.RADIO);
		MPButton.setText("Latest Search");
		GridDataFactory.fillDefaults().grab(false, false).indent(4,10).span(1, 0).applyTo(MPButton);
		MPButton.setSelection(false);
		
		domainButton = new Button(searchGroup, SWT.RADIO);
		domainButton.setText("Domain and Sub Domain Search");
		GridDataFactory.fillDefaults().grab(false, false).indent(4,10).span(1, 0).applyTo(domainButton);
		domainButton.setSelection(false);
		
		/*
		//********************************
		Group tryComposite = new Group(mainComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(true).applyTo(tryComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(4, 0).indent(0,20).applyTo(tryComposite);
		tryComposite.setText("Keyword search:");
		
		searchButton = new Button(tryComposite, SWT.RADIO);
		searchButton.setText("Popular Search");
		GridDataFactory.fillDefaults().grab(false, false).indent(4,10).span(1, 0).applyTo(searchButton);
		searchButton.setSelection(true);
		
		MPButton = new Button(tryComposite, SWT.RADIO);
		MPButton.setText("Latest Search");
		GridDataFactory.fillDefaults().grab(false, false).indent(4,10).span(1, 0).applyTo(MPButton);
		MPButton.setSelection(false);
		
		domainButton = new Button(tryComposite, SWT.RADIO);
		domainButton.setText("Domain and Sub Domain Search");
		GridDataFactory.fillDefaults().grab(false, false).indent(4,10).span(1, 0).applyTo(domainButton);
		domainButton.setSelection(false);
		//********************************
		 * 
		 */

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
		
		final Group domainFilterComposite = new Group(mainComposite, SWT.SHADOW_IN);
		GridDataFactory.fillDefaults().grab(true, false).span(4, 0).applyTo(domainFilterComposite);
		domainFilterComposite.setText("Select Domain and Sub Domain:");
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(domainFilterComposite);
		
		Label domain = new Label(domainFilterComposite, SWT.NONE);
		domain.setText("Select Domain:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(domain);
		
		domainList = new Combo(domainFilterComposite, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(domainList);
		toolkit.adapt(domainList);
		domainList.setItems(domains);
		domainList.select(0);
		domainList.setEnabled(false);
		
		Label sortType = new Label(domainFilterComposite, SWT.NONE);
		sortType.setText("Select sub-domains:");
		subdomainTable = new Table(domainFilterComposite, SWT.BORDER | SWT.MULTI);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 3).hint(90, 50).applyTo(subdomainTable);
		subdomainTable.setEnabled(false);
		
		Composite buttonComp = new Composite(domainFilterComposite, SWT.NONE);
		GridLayout btnLayout = new GridLayout();
		btnLayout.marginWidth = btnLayout.marginHeight = 0;
		btnLayout.makeColumnsEqualWidth = false;
		buttonComp.setLayout(btnLayout);
		buttonComp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		
		addSubdomainBtn = new Button(buttonComp, SWT.PUSH); // $NON-NLS-1$
		addSubdomainBtn.setText("Add...");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(addSubdomainBtn);
		addSubdomainBtn.setEnabled(false);
		
		addSubdomainBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				ILabelProvider lp = new ArrayLabelProvider();
				listDialog = new ElementListSelectionDialog(addSubdomainBtn.getShell(), lp);
				listDialog.setTitle("Select domain");
				listDialog.setMessage("Type the name of the domain");
				listDialog.setMultipleSelection(true);
				listDialog.setElements(GutenbergConstants.sites.get(domainList.getSelectionIndex()));
				if (listDialog.open() == Window.OK) {
					updateTable(listDialog.getResult());
				}
			}

		});
		
		removeSubdomainButton = new Button(buttonComp, SWT.PUSH);
		removeSubdomainButton.setText("Remove...");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(removeSubdomainButton);
		removeSubdomainButton.setEnabled(false);
		
		removeSubdomainButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (TableItem item : subdomainTable.getSelection()) {
					selectedRepresentatives.remove(item.getText());
					item.dispose();
				}
				if (selectedRepresentatives.size() == 0) {
					removeSubdomainButton.setEnabled(false);
				}
			}
		});
		
		Group limitGroup = new Group(mainComposite, SWT.SHADOW_IN);
		GridDataFactory.fillDefaults().grab(true, false).span(4, 0).applyTo(limitGroup);
		limitGroup.setText("Filter Results");
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(limitGroup);
		
		final Composite limitClient = new Composite(limitGroup, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).indent(10, 10).applyTo(limitClient);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(limitClient);

		checkPages = new Button(limitClient, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(checkPages);
		checkPages.setText("Limit Pages");
		
		Label limitPages = new Label(limitClient, SWT.NONE);
		limitPages.setText("Limit records per sub-domains:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(limitPages);
		pageText = new Text(limitClient, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(pageText);
		pageText.setEnabled(false);	
		
		checkPages.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(checkPages.getSelection())
					pageText.setEnabled(true);
				else
					pageText.setEnabled(false);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		searchButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(searchButton.getSelection())
				{
					keywordSearchText.setEnabled(true);
					domainList.setEnabled(false);
					subdomainTable.setEnabled(false);
					
				}
				else
				{
					keywordSearchText.setEnabled(false);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		MPButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(MPButton.getSelection())
				{
					keywordSearchText.setEnabled(false);
					domainList.setEnabled(false);
					subdomainTable.setEnabled(false);
					
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		domainButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(domainButton.getSelection())
				{
					domainList.setEnabled(true);
					subdomainTable.setEnabled(true);
					addSubdomainBtn.setEnabled(true);
					removeSubdomainButton.setEnabled(true);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		TacitFormComposite.createEmptyRow(toolkit, limitGroup);

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
		TacitFormComposite.createEmptyRow(toolkit, client);
		Button btnRun = TacitFormComposite.createRunButton(client, toolkit);

		
		btnRun.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				final Job job = new Job("Gutenberg Crawler") {
					String outputDir;
					String corpusName;
					Corpus corpus;
					int pages;
					boolean canProceed;
					String query;
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						
						TacitFormComposite.setConsoleViewInFocus();
						TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								if(checkPages.getSelection())
									pages = Integer.parseInt(pageText.getText());
								else
									pages =-1;
								corpusName = corpusNameTxt.getText();
								isDomain = domainButton.getSelection();
								isSearch = searchButton.getSelection();
								isLatest = MPButton.getSelection();
								query = keywordSearchText.getText();
								outputDir = IGutenbergCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator+ corpusName.trim();
								if (!new File(outputDir).exists()) {
									new File(outputDir).mkdirs();
								}
							}
						});

						int progressSize = 30;
						monitor.beginTask("Running Gutenberg Crawler...", progressSize);
						TacitFormComposite.writeConsoleHeaderBegining("Gutenberg Crawler started");
						GutenbergMain objmain = new GutenbergMain();
						SearchLatest objlatest = new SearchLatest();
						SearchPopular objpopular = new SearchPopular();
						monitor.subTask("Initializing...");
						monitor.worked(10);
						if (monitor.isCanceled())
							handledCancelRequest("Crawling is Stopped");
						corpus = new Corpus(corpusName, CMDataType.GUTENBERG_JSON);
						if(isDomain){
						for (final String domain : selectedRepresentatives) {
							outputDir = IGutenbergCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
							outputDir += File.separator + domain;
							if (!new File(outputDir).exists()) {
								new File(outputDir).mkdirs();
							}

							try {
								monitor.subTask("Crawling...");
								if (monitor.isCanceled())
									return handledCancelRequest("Crawling is Stopped");
								objmain.crawl(outputDir, domain, pages, monitor);
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
						}
						if(isSearch)
						{
						System.out.println("I am inside search button");
								outputDir = IGutenbergCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
								outputDir += File.separator + query;
								if (!new File(outputDir).exists()) {
									new File(outputDir).mkdirs();
								}

								try {
									monitor.subTask("Crawling...");
									if (monitor.isCanceled())
										return handledCancelRequest("Crawling is Stopped");
									objpopular.popular(outputDir,pages,query, monitor);
									if (monitor.isCanceled())
										return handledCancelRequest("Crawling is Stopped");
								} catch (Exception e) {
									return handleException(monitor, e, "Crawling failed. Provide valid data");
								}
								try {
									Display.getDefault().syncExec(new Runnable() {

										@Override
										public void run() {

											CorpusClass cc = new CorpusClass(query, outputDir);
											cc.setParent(corpus);
											corpus.addClass(cc);

										}
									});
								} catch (Exception e) {
									e.printStackTrace();
									return Status.CANCEL_STATUS;
								}
							
						
						}
						if(isLatest)
						{

							System.out.println("I am inside latest button");
									outputDir = IGutenbergCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
									outputDir += File.separator + "latest";
									if (!new File(outputDir).exists()) {
										new File(outputDir).mkdirs();
									}

									try {
										monitor.subTask("Crawling...");
										if (monitor.isCanceled())
											return handledCancelRequest("Crawling is Stopped");
										objlatest.latest(outputDir,pages, monitor);
										if (monitor.isCanceled())
											return handledCancelRequest("Crawling is Stopped");
									} catch (Exception e) {
										return handleException(monitor, e, "Crawling failed. Provide valid data");
									}
									try {
										Display.getDefault().syncExec(new Runnable() {

											@Override
											public void run() {

												CorpusClass cc = new CorpusClass("latest", outputDir);
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
						ConsoleView.printlInConsoleln("Created Corpus: "+corpusName);
						monitor.worked(100);
						monitor.done();
						return Status.OK_STATUS;
					
					}
				};
				job.setUser(true);
				boolean canProceed = canItProceed();
				if (canProceed) {
					job.schedule(); // schedule the job
					job.addJobChangeListener(new JobChangeAdapter() {

						public void done(IJobChangeEvent event) {
							if (!event.getResult().isOK()) {
								TacitFormComposite
										.writeConsoleHeaderBegining("Error: <Terminated> Gutenberg Crawler  ");
								TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is stopped",
										IStatus.INFO, form);

							} else {
								TacitFormComposite.updateStatusMessage(getViewSite(),		
										"Gutenberg Crawler completed", IStatus.OK, form);		
								ConsoleView.printlInConsoleln("Gutenberg Crawler completed successfully.");		
								TacitFormComposite		
										.writeConsoleHeaderBegining("Success: <Completed> Gutenberg Crawler ");	

							}
						}
					});
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	static class ArrayLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			return (String) element;
		}
	}
	
	public void updateTable(Object[] result) {
		if (selectedRepresentatives == null) {
			selectedRepresentatives = new ArrayList<String>();
		}

		for (Object object : result) {
			if (!selectedRepresentatives.contains((String) object))
				selectedRepresentatives.add((String) object);
		}

		subdomainTable.removeAll();
		for (String itemName : selectedRepresentatives) {
			TableItem item = new TableItem(subdomainTable, 0);
			item.setText(itemName);
			if (!removeSubdomainButton.isEnabled()) {
				removeSubdomainButton.setEnabled(true);
			}
		}

	}


	/**
	 * Adds "Classify" and "Help" buttons on the Naive Bayes Classifier form
	 */
	private void addButtonsToToolBar() {
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (GutenbergCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IGutenbergCrawlerViewConstants.IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Crawl";
			}

			String outputDir;
			String corpusName;
			Corpus corpus;
			int pages;
			boolean canProceed;
			String query;
			
			@Override				
			public void run() {						
				TacitFormComposite.writeConsoleHeaderBegining("Gutenberg Crawler started");		
				TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);		
				job = new Job("Gutenberg Crawler") {		
					@Override		
					protected IStatus run(final IProgressMonitor monitor) {		
						TacitFormComposite.setConsoleViewInFocus();		
						TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);		
						monitor.beginTask("Running Gutenberg Crawler...", 100);		
						Date dateObj = new Date();		
						Display.getDefault().syncExec(new Runnable() {		
							@Override		
							public void run() {		
								if(checkPages.getSelection())
										pages = Integer.parseInt(pageText.getText());
									else
										pages =-1;
									corpusName = corpusNameTxt.getText();
									isDomain = domainButton.getSelection();
									isSearch = searchButton.getSelection();
									isLatest = MPButton.getSelection();
									query = keywordSearchText.getText();
									outputDir = IGutenbergCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator+ corpusName.trim();
									if (!new File(outputDir).exists()) {
										new File(outputDir).mkdirs();
									}
							}		
						});	
						//int progressSize = 0;
						//if(domainButton.getSelection())
						//{
							//progressSize =selectedRepresentatives.size()*pages + 30;
						//}
						int progressSize = 30;
						monitor.beginTask("Running Gutenberg Crawler...", progressSize);
						TacitFormComposite.writeConsoleHeaderBegining("Gutenberg Crawler started");
						GutenbergMain objmain = new GutenbergMain();
						SearchLatest objlatest = new SearchLatest();
						SearchPopular objpopular = new SearchPopular();
						monitor.subTask("Initializing...");
						monitor.worked(10);
							if (monitor.isCanceled())
							{
								handledCancelRequest("Crawling is Stopped");
							}
						corpus = new Corpus(corpusName, CMDataType.GUTENBERG_JSON);
						System.out.println("Name of corpus=============" + corpus);
						if(isDomain)
						{
							System.out.println("I am inside domain button");
						for (final String domain : selectedRepresentatives) {
								System.out.println("Selected Representatives&&&&&&&&&&&&&&&&=" + selectedRepresentatives);
								outputDir = IGutenbergCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
								outputDir += File.separator + domain;
								if (!new File(outputDir).exists()) {
									new File(outputDir).mkdirs();
								}

								try {
									monitor.subTask("Crawling...");
									if (monitor.isCanceled())
										return handledCancelRequest("Crawling is Stopped");
									objmain.crawl(outputDir, domain, pages, monitor);
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
						}
						if(isSearch)
						{
						System.out.println("I am inside search button");
								outputDir = IGutenbergCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
								outputDir += File.separator + query;
								if (!new File(outputDir).exists()) {
									new File(outputDir).mkdirs();
								}

								try {
									monitor.subTask("Crawling...");
									if (monitor.isCanceled())
										return handledCancelRequest("Crawling is Stopped");
									objpopular.popular(outputDir,pages,query, monitor);
									if (monitor.isCanceled())
										return handledCancelRequest("Crawling is Stopped");
								} catch (Exception e) {
									return handleException(monitor, e, "Crawling failed. Provide valid data");
								}
								try {
									Display.getDefault().syncExec(new Runnable() {

										@Override
										public void run() {

											CorpusClass cc = new CorpusClass(query, outputDir);
											cc.setParent(corpus);
											corpus.addClass(cc);

										}
									});
								} catch (Exception e) {
									e.printStackTrace();
									return Status.CANCEL_STATUS;
								}
							
						
						}
						if(isLatest)
						{

							System.out.println("I am inside latest button");
									outputDir = IGutenbergCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
									outputDir += File.separator + "latest";
									if (!new File(outputDir).exists()) {
										new File(outputDir).mkdirs();
									}

									try {
										monitor.subTask("Crawling...");
										if (monitor.isCanceled())
											return handledCancelRequest("Crawling is Stopped");
										objlatest.latest(outputDir,pages, monitor);
										if (monitor.isCanceled())
											return handledCancelRequest("Crawling is Stopped");
									} catch (Exception e) {
										return handleException(monitor, e, "Crawling failed. Provide valid data");
									}
									try {
										Display.getDefault().syncExec(new Runnable() {

											@Override
											public void run() {

												CorpusClass cc = new CorpusClass("latest", outputDir);
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
							ConsoleView.printlInConsoleln("Created Corpus: "+corpusName);
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
										.writeConsoleHeaderBegining("Error: <Terminated> Gutenberg Crawler");		
							} else {		
								TacitFormComposite.updateStatusMessage(getViewSite(),		
										"Gutenberg Crawler completed", IStatus.OK, form);		
								ConsoleView.printlInConsoleln("Gutenberg Crawler completed successfully.");		
								TacitFormComposite		
										.writeConsoleHeaderBegining("Success: <Completed> Gutenberg Crawler ");	
			
							}		
						}		
					});		
				}		
			};

		
			

		});
	
		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (GutenbergCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(IGutenbergCrawlerViewConstants.IMAGE_HELP_CO));
			}

			@Override
			public String getToolTipText() {
				return "Help";
			}

			@Override
			public void run() {
				PlatformUI.getWorkbench().getHelpSystem()
					.displayHelp("edu.usc.cssl.tacit.classify.naivebayes.ui.naivebayes");
			};
		};
		mgr.add(helpAction);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(helpAction,
				"edu.usc.cssl.tacit.classify.naivebayes.ui.naivebayes");
		PlatformUI.getWorkbench().getHelpSystem().setHelp(form, "edu.usc.cssl.tacit.classify.naivebayes.ui.naivebayes");
		form.getToolBarManager().update(true);
	}

	/**
	 * Handles cancel request by sending appropriate message to UI
	 * 
	 * @param message
	 * @return
	 */
	private IStatus handledCancelRequest(String message) {
		TacitFormComposite.updateStatusMessage(getViewSite(), message, IStatus.ERROR, form);
		ConsoleView.printlInConsoleln("Gutenberg Crawler cancelled.");
		return Status.CANCEL_STATUS;

	}

	/**
	 * Validates the input form to ensure correctness
	 * 
	 * @param classPaths
	 * @return
	 */
	private boolean canItProceed() {

		form.getMessageManager().removeAllMessages();
		Boolean isDomaincheck;
		Boolean isSearchcheck;
		
		isDomaincheck = domainButton.getSelection();
		System.out.println("value of ------------------------" + isDomaincheck);
		if(isDomaincheck)
		{
			System.out.println("I am inside");
		try{
		if(selectedRepresentatives.isEmpty()){
			form.getMessageManager().addMessage("DomainError", "Enter atleast one sub domain name", null,
					IMessageProvider.ERROR);
			return false;
		}else{
			form.getMessageManager().removeMessage("DomainError");
		}
		}catch(Exception e){
			form.getMessageManager().addMessage("DomainError", "Enter atleast one sub domain name", null,
					IMessageProvider.ERROR);
			return false;
		}
		}
		isSearchcheck = searchButton.getSelection();
		if(isSearchcheck)
		{
			try{
				String query =  keywordSearchText.getText();
				if(query == null || query.isEmpty())
				{
					form.getMessageManager().addMessage("keyword", "Enter the keyword to be crawled", null,
							IMessageProvider.ERROR);
					return false;
				}else
						form.getMessageManager().removeMessage("pageLimit");
			}catch (Exception e) {
				form.getMessageManager().addMessage("keyword", "Enter the keyword to be crawled", null,
						IMessageProvider.ERROR);
				return false;
			}
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
			String outputDir = IGutenbergCrawlerViewConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
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

	/**
	 * Maps each class to its selected files
	 * 
	 * @param classLayoutData
	 * @param classPaths
	 */
	protected void consolidateSelectedFiles(TableLayoutData classLayoutData, Map<String, List<String>> classPaths) {
		Tree tree = classLayoutData.getTree();
		for (int i = 0; i < tree.getItemCount(); i++) {
			TreeItem temp = tree.getItem(i);
			if (temp.getChecked()) {
				classPaths.put(temp.getData().toString(), classLayoutData.getSelectedItems(temp));
			}
		}
	}

	/**
	 * Function to be called incase of exception
	 * 
	 * @param monitor
	 * @param e
	 * @param message
	 * @return
	 */
	private IStatus handleException(IProgressMonitor monitor, Exception e, String message) {
		monitor.done();
		System.out.println(message);
		e.printStackTrace();
		TacitFormComposite.updateStatusMessage(getViewSite(), message + e.getMessage(), IStatus.ERROR, form);
		return Status.CANCEL_STATUS;
	}

	@Override
	public void setFocus() {
		form.setFocus();
	}

	/**
	 * Output file creation with statistics
	 * 
	 * @param location
	 * @param title
	 * @param dateObj
	 * @param perf
	 * @param kValue
	 * @param monitor
	 */
	

	/**
	 * 
	 * @param parent
	 * @param title
	 * @return - Creates a form body section for Naive Bayes Classifier
	 */
	private FormToolkit createFormBodySection(Composite parent, String title) {
		// Every interface requires a toolkit(Display) and form to store the
		// components
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(form.getForm());
		form.setText(title);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true).applyTo(form.getBody());
		return toolkit;
	}

}
