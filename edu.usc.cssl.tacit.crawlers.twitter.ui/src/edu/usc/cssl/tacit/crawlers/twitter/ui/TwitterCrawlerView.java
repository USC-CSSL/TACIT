package edu.usc.cssl.tacit.crawlers.twitter.ui;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CMDataType;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.Corpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;
import edu.usc.cssl.tacit.common.ui.validation.OutputPathValidation;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import edu.usc.cssl.tacit.crawlers.twitter.services.TwitterStreamApi;
import edu.usc.cssl.tacit.crawlers.twitter.ui.internal.ITwitterCrawlerUIConstants;
import edu.usc.cssl.tacit.crawlers.twitter.ui.internal.TwitterCrawlerImageRegistry;

public class TwitterCrawlerView extends ViewPart implements ITwitterCrawlerUIConstants {
	public static final String ID = "edu.usc.cssl.tacit.crawlers.twitter.ui.view1";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private static Text wordFilterText;
	private static Text geoFilterText;
	private static Button limitRecords;
	private static Text dayText;
	private static Text hourText;
	private static Label maxLimit;
	private static Text maxText;
	private static Text minText;
	private static Button userNameBtn;
	private static Button favBtn;
	private static Button reTweetBtn;
	private static Button langBtn;
	private static Button createdBtn;
	private static Button textBtn;
	private static Button statusIdBtn;
	private static Button geoLocBtn;
	private long finishTime;
	private long maxTweetLimit;
	private boolean noWordFilter;
	private boolean noLocationFilter;
	private boolean[] storedAtts;

	private String keyWords[];
	private double[][] geoLocations;
	private Text corpusNameTxt;
	private static Button wordFilterLbl;
	private static Button geoFilterLbl;
	private static Button randomFilterLbl;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(form.getForm());
		form.setText("Twitter Crawler"); //$NON-NLS-1$
		form.setImage(TwitterCrawlerImageRegistry.getImageIconFactory()
				.getImage(ITwitterCrawlerUIConstants.IMAGE_CRAWL_TWITTER));
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
		this.setPartName("Twitter Crawler");
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {

			private Job job;

			@Override
			public ImageDescriptor getImageDescriptor() {
				return (TwitterCrawlerImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Crawl";
			}

			@Override
			public void run() {
				TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
				TacitFormComposite.writeConsoleHeaderBegining("Crawling Twitter started ");
				// Make sure input values are in valid state then launch
				// streamer

				noWordFilter = false;
				noLocationFilter = false;

				// check if the output address is correct and writable
				Date dateObj = new Date();
				final String corpusName = corpusNameTxt.getText();
				final String outputDir = System.getProperty("user.dir") + System.getProperty("file.separator")
						+ "json_corpuses" + System.getProperty("file.separator") + "twitter" + File.separator
						+ corpusName;
				;
				if (!new File(outputDir).exists()) {
					new File(outputDir).mkdir();
				}
				DateFormat df = new SimpleDateFormat("MM-dd-yyyy-HH-mm-ss");
				Date dateobj = new Date();
				final String outputFile = outputDir + File.separator + "Twitter_Stream_" + df.format(dateobj) + ".json";
				storedAtts = new boolean[8];

				// Get stored attribute values
				storedAtts[0] = userNameBtn.getSelection();
				storedAtts[1] = textBtn.getSelection();
				storedAtts[2] = reTweetBtn.getSelection();
				storedAtts[3] = geoLocBtn.getSelection();
				storedAtts[4] = createdBtn.getSelection();
				storedAtts[5] = favBtn.getSelection();
				storedAtts[6] = statusIdBtn.getSelection();
				storedAtts[7] = langBtn.getSelection();

				final TwitterStreamApi ttStream = new TwitterStreamApi();

				final boolean maxLimitEnabled = true;
				final boolean timeLimit = limitRecords.getSelection();
				job = new Job("Twitter Stream Job") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {

						try {
							TacitFormComposite.setConsoleViewInFocus();
							TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
							long totalJobWork = maxTweetLimit + 20;
							if (maxTweetLimit == 0) {
								totalJobWork = 200;
							}
							monitor.beginTask("Crawling Twitter ...", (int) totalJobWork);
							TacitFormComposite.writeConsoleHeaderBegining("Twitter Crawling Started... ");
							ttStream.stream(outputFile, maxLimitEnabled, maxTweetLimit, timeLimit, finishTime,
									noWordFilter, keyWords, noLocationFilter, geoLocations, storedAtts, monitor, job);
							monitor.done();
							TacitFormComposite.writeConsoleHeaderBegining("<terminated> Twitter Crawling  ");
							TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling completed", IStatus.OK,
									form);
							ConsoleView.printlInConsoleln("Creating Corpus " + corpusName + "...");
							Corpus twitterCorpus = new Corpus(corpusName, CMDataType.TWITTER_JSON);
							CorpusClass twitterCorpusClass = new CorpusClass();
							twitterCorpusClass.setClassName(corpusName + "_class1");
							twitterCorpusClass.setClassPath(outputDir);
							twitterCorpusClass.setKeyTextFields("Text");
							List<ICorpusClass> corpusList = new ArrayList<ICorpusClass>();
							corpusList.add(twitterCorpusClass);
							twitterCorpus.setClasses(corpusList);
							try {
								ConsoleView.printlInConsoleln("Saving Corpus " + corpusName + "...");
								ManageCorpora.saveCorpus(twitterCorpus);
							} catch (Exception e) {
								e.printStackTrace();
								return Status.CANCEL_STATUS;
							}
							return Status.OK_STATUS;
						} catch (IOException e1) {
							return Status.CANCEL_STATUS;
						} catch (OperationCanceledException e) {
							return Status.CANCEL_STATUS;
						}

					}
				};
				if (canProceedCrawl()) {
					job.setUser(true);
					job.schedule();
					job.addJobChangeListener(new JobChangeAdapter() {

						@Override
						public void done(IJobChangeEvent event) {
							if (!event.getResult().isOK()) {
								TacitFormComposite.writeConsoleHeaderBegining("Error: <Terminated> Twitter Crawler  ");
								TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is stopped",
										IStatus.INFO, form);

							} else {
								TacitFormComposite.writeConsoleHeaderBegining("Success: <Completed> Twitter Crawler  ");
								TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is completed",
										IStatus.INFO, form);
								ConsoleView.printlInConsoleln("Twitter crawler completed successfully.");
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
				return (TwitterCrawlerImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_HELP_CO));
			}

			@Override
			public String getToolTipText() {
				return "Help";
			}

			@Override
			public void run() {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp("edu.usc.cssl.tacit.crawlers.twitter.ui.twitter");
			};
		};
		mgr.add(helpAction);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(helpAction, "edu.usc.cssl.tacit.crawlers.twitter.ui.twitter");
		PlatformUI.getWorkbench().getHelpSystem().setHelp(form, "edu.usc.cssl.tacit.crawlers.twitter.ui.twitter");
		form.getToolBarManager().update(true);
		toolkit.paintBordersFor(form.getBody());
	}

	private void createFilterSection(GridLayout layout, Composite parent) {
		Section section = toolkit.createSection(parent, Section.TWISTIE | Section.TITLE_BAR);
		section.setActiveToggleColor(toolkit.getHyperlinkGroup().getActiveForeground());
		section.setToggleColor(toolkit.getColors().getColor(IFormColors.SEPARATOR));

		FormText descriptionFrm = toolkit.createFormText(section, false);
		descriptionFrm.setText("<form><p>" + "Filter settings" + "</p></form>", true, false);
		section.setDescriptionControl(descriptionFrm);

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		client.setLayout(layout);

		TacitFormComposite.createEmptyRow(toolkit, client);

		ScrolledComposite pluginTabContainer = new ScrolledComposite(client, SWT.H_SCROLL | SWT.V_SCROLL);
		pluginTabContainer.setExpandHorizontal(true);
		pluginTabContainer.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).applyTo(pluginTabContainer);

		Composite scInput = toolkit.createComposite(pluginTabContainer);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).applyTo(scInput);

		pluginTabContainer.setContent(scInput);

		createAdditionalOptions(scInput);

		Label dummyLb1 = toolkit.createLabel(scInput, "", SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(dummyLb1);

		section.setText("tttt");
		section.setDescription(
				"<form><p>This section has a <b>tree</b> and a button. It also has <a>a link</a> in the description.</p></form>");
		section.setClient(client);
		section.setExpanded(true);
		GridData gd = new GridData(GridData.FILL_BOTH);
		section.setLayoutData(gd);

	}

	private void createAdditionalOptions(Composite sectionClient) {
		Label noClusterTxtLbl = toolkit.createLabel(sectionClient, "Number of clusters:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(noClusterTxtLbl);
		Text noClusterTxt = toolkit.createText(sectionClient, "", SWT.BORDER);
		noClusterTxt.setText("1");
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(noClusterTxt);
	}

	private boolean canProceedCrawl() {

		TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
		boolean canProceed = true;
		long dayLimit;
		long hourLimit;
		long minLimit;
		boolean validGeoFilter = true;
		boolean validLimitParse = true;
		boolean validLimitState = true;
		// check limit tab attributes validity

		// check geofilter string validity

		if (geoFilterLbl.getSelection()) {
			String geoWords[] = geoFilterText.getText().split(";");
			geoLocations = new double[geoWords.length * 2][2];
			if ((geoWords.length % 2 == 1) && (geoWords.length != 1)) // check
																		// valid
																		// number
																		// of
																		// pairs
				validGeoFilter = false;
			else {
				for (int i = 0; i < geoWords.length && !geoFilterText.getText().isEmpty(); i += 2) {
					String geoTemp[] = geoWords[i].split(",");
					if (geoTemp.length != 4) // check if there are only two
												// values in the pair
						validGeoFilter = false;
					else {
						if (geoTemp[0].isEmpty() || geoTemp[1].isEmpty() || geoTemp[2].isEmpty()
								|| geoTemp[3].isEmpty())
							validGeoFilter = false; // check if there is
													// something to be read
						else {
							try { // check if there is a valid number
								geoLocations[i][0] = Double.parseDouble(geoTemp[0]);
								geoLocations[i][1] = Double.parseDouble(geoTemp[1]);
								geoLocations[i + 1][0] = Double.parseDouble(geoTemp[2]);
								geoLocations[i + 1][1] = Double.parseDouble(geoTemp[3]);
							} catch (NumberFormatException e2) {
								validGeoFilter = false;
							}
						}

					}
				}
			}
		}
		// is there any geofilter?
		if (geoFilterLbl.getSelection()) {
			if (geoLocations.length == 0 || geoFilterText.getText().isEmpty()) {
				noLocationFilter = true;
				validGeoFilter = false;
			}

		}

		if (limitRecords.getSelection()) {
			try {
				dayLimit = Long.parseLong(dayText.getText().toString());
				hourLimit = Long.parseLong(hourText.getText().toString());
				minLimit = Long.parseLong(minText.getText().toString());
				// Is streaming time positive interger?
				if (dayLimit < 0 || hourLimit < 0 || minLimit < 0 || (dayLimit == 0 && hourLimit == 0 && minLimit == 0))
					validLimitParse = false;
				finishTime = (minLimit + (hourLimit + dayLimit * 24) * 60) * 1000 * 60;
			} catch (NumberFormatException e1) {
				validLimitParse = false;
			}
		}
		// Is there valid max tweet number?
		try {
			maxTweetLimit = Long.parseLong(maxText.getText().toString());
			if (maxTweetLimit <= 0)
				validLimitState = false;
		} catch (NumberFormatException e1) {
			validLimitState = false;
		}

		form.getMessageManager().removeMessage("word");
		form.getMessageManager().removeMessage("location");
		form.getMessageManager().removeMessage("geolocation");
		form.getMessageManager().removeMessage("limit");
		form.getMessageManager().removeMessage("state");
		form.getMessageManager().removeMessage("maxlimit");
		form.getMessageManager().removeMessage("stored-attribute");

		String message = OutputPathValidation.getInstance().validateOutputCorpus(corpusNameTxt.getText());
		if (message != null) {

			form.getMessageManager().addMessage("location", message, null, IMessageProvider.ERROR);
			canProceed = false;
		}

		// first check if everything works then stream
		String userName = CommonUiActivator.getDefault().getPreferenceStore().getString("user");
		if (userName.length() == 0) {
			ErrorDialog.openError(Display.getDefault().getActiveShell(), "User name is not configured",
					"Please check user settings for Twitter",
					new Status(IStatus.ERROR, CommonUiActivator.PLUGIN_ID, "No/Valid user is not found"));
			String id = "edu.usc.cssl.tacit.crawlers.twitter.ui.userconfig";
			PreferencesUtil
					.createPreferenceDialogOn(Display.getDefault().getActiveShell(), id, new String[] { id }, null)
					.open();
			canProceed = false;

		}

		if (wordFilterLbl.getSelection()) {
			noLocationFilter = true;
			noWordFilter = false;
			keyWords = wordFilterText.getText().split(";");
			if (keyWords.length == 0 || wordFilterText.getText().isEmpty()) {
				form.getMessageManager().addMessage("word", "Word Filter cannot be empty", null,
						IMessageProvider.ERROR);
				canProceed = false;
			}
		}

		if (randomFilterLbl.getSelection()) {
			noLocationFilter = true;
			noWordFilter = true;
		}

		if (geoFilterLbl.getSelection()) {
			noLocationFilter = false;
			noWordFilter = true;
			if (!validGeoFilter) {
				form.getMessageManager().addMessage("geolocation",
						"Error: Invalid gelocation query. Please compare with example.", null, IMessageProvider.ERROR);
				canProceed = false;
			}
		}
		if (!validLimitParse) {
			form.getMessageManager().addMessage("limit",
					"Error: Invalid parsing. Please makes sure all enabled fields are positive discrete numbers.", null,
					IMessageProvider.ERROR);
			canProceed = false;

		}
		if (!validLimitState) {
			form.getMessageManager().addMessage("maxlimit",
					"Error: Invalid Limit tweets per Crawl is entered. Please enter valid positive number", null,
					IMessageProvider.ERROR);
			canProceed = false;

		}

		if (!(userNameBtn.getSelection() || createdBtn.getSelection() || geoLocBtn.getSelection()
				|| langBtn.getSelection() || statusIdBtn.getSelection() || reTweetBtn.getSelection()
				|| textBtn.getSelection() || favBtn.getSelection())) {
			form.getMessageManager().addMessage("stored-attribute", "Error: Select atleaset one Stored attribute", null,
					IMessageProvider.ERROR);
			canProceed = false;
		}
		return canProceed;
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
		section.setDescription(
				"Choose values for Filter; Use Semicolon as delimeter to give more than one filter value in field");

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
		wordFilterText.setMessage("For example: NLP;#USC");
		geoFilterLbl = toolkit.createButton(sectionClient, "Geo Filter", SWT.RADIO);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(geoFilterLbl);
		geoFilterText = toolkit.createText(sectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(geoFilterText);
		geoFilterText.setMessage("For example(Los Angeles): -118.442,33.72,-117.86,34.12");
		geoFilterText.setEditable(false);
		geoFilterText.setEnabled(false);
		randomFilterLbl = toolkit.createButton(sectionClient, "Random Tweets", SWT.RADIO);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(geoFilterLbl);
		wordFilterLbl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (wordFilterLbl.getSelection()) {
					wordFilterText.setEditable(true);
					wordFilterText.setEnabled(true);
					geoFilterText.setEditable(false);
					geoFilterText.setEnabled(false);
				} else if (geoFilterLbl.getSelection()) {
					wordFilterText.setEditable(false);
					wordFilterText.setEnabled(false);
					geoFilterText.setEditable(true);
					geoFilterText.setEnabled(true);
				} else if (randomFilterLbl.getSelection()) {
					wordFilterText.setEditable(false);
					wordFilterText.setEnabled(false);
					geoFilterText.setEditable(false);
					geoFilterText.setEnabled(false);

				}
			}
		});
		geoFilterLbl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (wordFilterLbl.getSelection()) {
					wordFilterText.setEditable(true);
					wordFilterText.setEnabled(true);
					geoFilterText.setEditable(false);
					geoFilterText.setEnabled(false);
				} else if (geoFilterLbl.getSelection()) {
					wordFilterText.setEditable(false);
					wordFilterText.setEnabled(false);
					geoFilterText.setEditable(true);
					geoFilterText.setEnabled(true);
				} else if (randomFilterLbl.getSelection()) {
					wordFilterText.setEditable(false);
					wordFilterText.setEnabled(false);
					geoFilterText.setEditable(false);
					geoFilterText.setEnabled(false);

				}
			}
		});
		randomFilterLbl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (wordFilterLbl.getSelection()) {
					wordFilterText.setEditable(true);
					wordFilterText.setEnabled(true);
					geoFilterText.setEditable(false);
					geoFilterText.setEnabled(false);
				} else if (geoFilterLbl.getSelection()) {
					wordFilterText.setEditable(false);
					wordFilterText.setEnabled(false);
					geoFilterText.setEditable(true);
					geoFilterText.setEnabled(true);
				} else if (randomFilterLbl.getSelection()) {
					wordFilterText.setEditable(false);
					wordFilterText.setEnabled(false);
					geoFilterText.setEditable(false);
					geoFilterText.setEnabled(false);

				}
			}
		});

	}

	public static void createLimitSection(FormToolkit toolkit, Composite parent, final IMessageManager mmng) {
		Section section = toolkit.createSection(parent,
				Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION | Section.TWISTIE);

		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).applyTo(section);
		section.setText("Limit Tweets"); //$NON-NLS-1$
		section.setDescription("Limit crawling based on the following settings");

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(6).equalWidth(false).applyTo(sc);

		Composite sectionClient = toolkit.createComposite(section);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(6).equalWidth(false).applyTo(sectionClient);
		section.setClient(sectionClient);

		TacitFormComposite.createEmptyRow(toolkit, sectionClient);
		TacitFormComposite.createEmptyRow(toolkit, sectionClient);

		limitRecords = new Button(sectionClient, SWT.CHECK);
		limitRecords.setText("Time Limit");
		GridDataFactory.fillDefaults().grab(false, false).span(6, 0).applyTo(limitRecords);

		Composite maxClient = toolkit.createComposite(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(maxClient);
		GridLayoutFactory.fillDefaults().numColumns(6).equalWidth(false).applyTo(maxClient);

		final Label dayLabel = new Label(maxClient, SWT.NONE);
		dayLabel.setText("Days :");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(dayLabel);

		dayText = toolkit.createText(maxClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(dayText);

		final Label hourLbl = new Label(maxClient, SWT.NONE);
		hourLbl.setText("Hours :");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(hourLbl);

		hourText = toolkit.createText(maxClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(hourText);
		final Label minLbl = new Label(maxClient, SWT.NONE);
		minLbl.setText("Minutes :");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(minLbl);

		minText = toolkit.createText(maxClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(minText);

		dayText.setEnabled(false);
		dayText.setEditable(false);
		hourText.setEnabled(false);
		hourText.setEditable(false);
		minText.setEnabled(false);
		minText.setEditable(false);
		// MAX Tweet area

		maxLimit = toolkit.createLabel(maxClient, "Limit Tweets per Crawl");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(maxLimit);

		maxText = toolkit.createText(maxClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(5, 0).applyTo(maxText);
		maxText.setText("10");
		limitRecords.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (limitRecords.getSelection()) {
					dayText.setEnabled(true);
					dayText.setEditable(true);
					hourText.setEnabled(true);
					hourText.setEditable(true);
					minText.setEnabled(true);
					minText.setEditable(true);
				} else {
					dayText.setEnabled(false);
					dayText.setEditable(false);
					hourText.setEnabled(false);
					hourText.setEditable(false);
					minText.setEnabled(false);
					minText.setEditable(false);
				}
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

		userNameBtn = new Button(sectionClient, SWT.CHECK);
		userNameBtn.setText("User Name");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(userNameBtn);
		userNameBtn.setSelection(true);

		geoLocBtn = new Button(sectionClient, SWT.CHECK);
		geoLocBtn.setText("Geo Location");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(geoLocBtn);

		statusIdBtn = new Button(sectionClient, SWT.CHECK);
		statusIdBtn.setText("Status Id");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(statusIdBtn);

		textBtn = new Button(sectionClient, SWT.CHECK);
		textBtn.setText("Text");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(textBtn);
		textBtn.setSelection(true);

		createdBtn = new Button(sectionClient, SWT.CHECK);
		createdBtn.setText("Created at");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(createdBtn);

		langBtn = new Button(sectionClient, SWT.CHECK);
		langBtn.setText("Language");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(langBtn);

		reTweetBtn = new Button(sectionClient, SWT.CHECK);
		reTweetBtn.setText("Re-tweet Number ");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(reTweetBtn);
		reTweetBtn.setSelection(true);

		favBtn = new Button(sectionClient, SWT.CHECK);
		favBtn.setText("Favorite Count");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(favBtn);
		favBtn.setSelection(true);

	}

}