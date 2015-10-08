package edu.usc.cssl.tacit.wordcount.weighted.ui;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.uc.cssl.tacit.wordcount.weighted.services.WordCountApi;
import edu.usc.cssl.tacit.common.Preprocess;
import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.common.ui.IPreprocessorSettingsConstant;
import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.tacit.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.tacit.common.ui.utility.TacitUtil;
import edu.usc.cssl.tacit.common.ui.validation.OutputPathValidation;
import edu.usc.cssl.tacit.wordcount.weighted.ui.internal.IWeightedWordCountViewConstants;
import edu.usc.cssl.tacit.wordcount.weighted.ui.internal.WeightedWordCountImageRegistry;

public class WeightedWordCountView extends ViewPart implements
		IWeightedWordCountViewConstants {
	public static String ID = "edu.usc.cssl.tacit.wordcount.weighted.ui.view1";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private OutputLayoutData layoutData;
	private TableLayoutData inputLayoutData;
	private TableLayoutData dictLayoutData;
	private Button snowballStemming;
	private Button liwcStemming;
	private Button stemEnabled;
	private Button spssRawFile;
	private Button wordDistributionFile;
	private WordCountApi wordCountController;
	private Button weightedWordCountButton;
	private Button liwcWordCountButton;
	private Button stopWordPathEnabled;
	private Job wordCountJob;
	
	@Override
	public void createPartControl(Composite parent) {
		toolkit = createFormBodySection(parent);
		Section section = toolkit.createSection(form.getBody(),
				Section.TITLE_BAR | Section.EXPANDED);

		GridDataFactory.fillDefaults().grab(true, false).span(3, 1)
				.applyTo(section);
		section.setExpanded(true);
		String description = "This sections gives details about the Weighted word count";
		FormText descriptionFrm = toolkit.createFormText(section, false);
		descriptionFrm.setText("<form><p>" + description + "</p></form>", true,
				false);
		section.setDescriptionControl(descriptionFrm);

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL
				| SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
				.applyTo(sc);

		TacitFormComposite.createEmptyRow(toolkit, sc);

		// create type either LIWC or Weighted

		Composite wcTypeComposite = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2)
				.applyTo(wcTypeComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1)
				.applyTo(wcTypeComposite);
		TacitFormComposite.addErrorPopup(form.getForm(), toolkit);

		createWordCountType(toolkit, wcTypeComposite, form.getMessageManager());

		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2)
				.applyTo(client);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		inputLayoutData = TacitFormComposite
				.createTableSection(client, toolkit, layout, "Input Details",
						"Add File(s) and Folder(s) to include in analysis.",
						true, true, true);
		dictLayoutData = TacitFormComposite
				.createTableSection(client, toolkit, layout, "Dictionary",
						"Add location of Dictionary", false, true, false);

		Composite compInput = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(2)
				.applyTo(compInput);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(compInput);
		layout = new GridLayout();
		layout.numColumns = 2;

		createPreprocessLink(compInput);

		createStemmingOptions(compInput);

		//createAdditionalOptions(toolkit, form.getBody());

		//TacitFormComposite.createEmptyRow(toolkit, form.getBody());

		Composite client1 = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1)
				.applyTo(client1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client1);

		layoutData = TacitFormComposite.createOutputSection(toolkit, client1,
				form.getMessageManager());

		createAdditionalOptions(toolkit, client1);
		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter());
		addButtonsToToolBar();
		toolkit.paintBordersFor(form.getBody());

	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == Job.class) {
			return wordCountJob;
		}
		return super.getAdapter(adapter);
	}

	private void createPreprocessLink(Composite client) {

		Composite clientLink = toolkit.createComposite(client);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2)
				.applyTo(clientLink);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(clientLink);

		stopWordPathEnabled = toolkit.createButton(clientLink, "", SWT.CHECK);
		stopWordPathEnabled.setEnabled(false);
		stopWordPathEnabled.setSelection(false);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(stopWordPathEnabled);
		final Hyperlink link = toolkit.createHyperlink(clientLink,
				"Preprocess", SWT.NONE);
		link.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		link.addHyperlinkListener(new IHyperlinkListener() {
			@Override
			public void linkEntered(HyperlinkEvent e) {
			}

			@Override
			public void linkExited(HyperlinkEvent e) {
			}

			@Override
			public void linkActivated(HyperlinkEvent e) {
				String id = "edu.usc.cssl.tacit.common.ui.prepocessorsettings";
				PreferencesUtil.createPreferenceDialogOn(link.getShell(), id,
						new String[] { id }, null).open();
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(link);

	}

	private void createWordCountType(FormToolkit toolkit2, Composite parent,
			IMessageManager messageManager) {

		Group buttonComposite = new Group(parent, SWT.LEFT);
		buttonComposite.setText("Word Count Technique");
		// buttonComposite.setBackground(parent.getBackground());
		buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonComposite.setLayout(layout);
		buttonComposite.setForeground(parent.getForeground());

		liwcWordCountButton = new Button(buttonComposite, SWT.RADIO);
		liwcWordCountButton.setText("Standard Word Count");
		liwcWordCountButton.setSelection(true);
		// liwcWordCountButton.setBackground(parent.getBackground());
		liwcWordCountButton.setForeground(parent.getForeground());
		liwcWordCountButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (liwcWordCountButton.getSelection()) {
					stemEnabled.setEnabled(false);
					stemEnabled.setSelection(false);
					liwcStemming.setEnabled(false);
					liwcStemming.setSelection(false);
					snowballStemming.setSelection(false);
					snowballStemming.setEnabled(false);
					liwcStemming.setVisible(false);
					stemEnabled.setVisible(false);
					snowballStemming.setVisible(false);
					stopWordPathEnabled.setSelection(false);
				} else {
					stemEnabled.setVisible(true);
					stemEnabled.setEnabled(true);
					stemEnabled.setSelection(false);
					liwcStemming.setVisible(true);
					liwcStemming.setSelection(false);
					snowballStemming.setVisible(true);
					snowballStemming.setSelection(false);
					stopWordPathEnabled.setEnabled(true);
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		weightedWordCountButton = new Button(buttonComposite, SWT.RADIO);
		weightedWordCountButton.setText("Weighted Word Count");
		weightedWordCountButton.setSelection(false);
		// weightedWordCountButton.setBackground(parent.getBackground());
		weightedWordCountButton.setForeground(parent.getForeground());

		// standardWordCountButton = new Button(buttonComposite, SWT.RADIO);
		// standardWordCountButton.setText("Standard Word Count");
		// standardWordCountButton.setSelection(false);
		// standardWordCountButton.setBackground(parent.getBackground());
		// standardWordCountButton.setForeground(parent.getForeground());

	}

	private void createStemmingOptions(Composite body) {
		Composite downloadGroup = toolkit.createComposite(body, SWT.NONE);
		// downloadGroup.setText("Steming");

		downloadGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		downloadGroup.setLayout(layout);

		stemEnabled = toolkit.createButton(downloadGroup, "Stem Dictionary",
				SWT.CHECK);
		stemEnabled.pack();

		liwcStemming = toolkit.createButton(downloadGroup, "LIWC", SWT.RADIO);
		liwcStemming.setEnabled(false);
		liwcStemming.setSelection(false);
		liwcStemming.pack();
		liwcStemming.setVisible(false);

		snowballStemming = toolkit.createButton(downloadGroup, "Porter",
				SWT.RADIO);
		snowballStemming.setEnabled(false);
		snowballStemming.pack();
		snowballStemming.setVisible(false);

		stemEnabled.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (stemEnabled.getSelection()) {
					liwcStemming.setEnabled(true);
					snowballStemming.setEnabled(true);
					if (!liwcStemming.getSelection()
							&& !snowballStemming.getSelection()) {
						snowballStemming.setSelection(true);
					}
				} else {
					liwcStemming.setEnabled(false);
					snowballStemming.setEnabled(false);
				}
			}
		});
		stemEnabled.setEnabled(false);
		stemEnabled.setVisible(false);
	}

	private void createAdditionalOptions(FormToolkit toolkit, Composite output) {
		spssRawFile = toolkit.createButton(output, "Create .DAT file",
				SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(spssRawFile);
		wordDistributionFile = toolkit.createButton(output,
				"Create category-wise word distribution files", SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(wordDistributionFile);
	}

	private FormToolkit createFormBodySection(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);

		toolkit.decorateFormHeading(form.getForm());
		form.setText("LIWC-Style Word Count"); //$NON-NLS-1$
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true)
				.applyTo(form.getBody());
		return toolkit;
	}

	private void addButtonsToToolBar() {
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {

			@Override
			public ImageDescriptor getImageDescriptor() {
				return (WeightedWordCountImageRegistry.getImageIconFactory()
						.getImageDescriptor(IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Analyze";
			}

			@Override
			public void run() {
				if(!canProceed()) {
					return;
				}
				TacitFormComposite
						.writeConsoleHeaderBegining("Word count analysis");
				final String stopWordPath = CommonUiActivator.getDefault()
						.getPreferenceStore()
						.getString(IPreprocessorSettingsConstant.STOP_PATH);
				// lindapulickal: handling case where user types in a file
				// without extension

				final String outputPath = layoutData.getOutputLabel().getText();
				String fileName = "wordcount";
				DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
				final Date dateobj = new Date();
				if (weightedWordCountButton.getSelection()) {
					fileName = "weighted-" + fileName + "-"
							+ df.format(dateobj);
				} else {
					fileName = "LIWC-" + fileName + "-" + df.format(dateobj);
				}
				final File oFile = new File(outputPath + File.separator
						+ fileName + ".csv");
				final File sFile = new File(outputPath + File.separator
						+ fileName + ".dat");

				final List<String> inputFiles = TacitUtil.refineInput(inputLayoutData
						.getSelectedFiles());
				final List<String> dictionaryFiles = dictLayoutData
						.getSelectedFiles(false);
				final boolean isLiwcStemming = liwcStemming.getSelection();
				final boolean isSnowBall = snowballStemming.getSelection();
				final boolean isSpss = spssRawFile.getSelection();
				final boolean isWdist = wordDistributionFile.getSelection();
				final boolean isStemDic = stemEnabled.getSelection();
				final boolean isPreprocess = stopWordPathEnabled.getSelection();
				wordCountJob = new Job("Analyzing...") {
					private Preprocess preprocessTask;
					private String dirPath;

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						// Date dateObj = new Date();
						TacitFormComposite.setConsoleViewInFocus();
						List<File> selectedFiles = new ArrayList<File>();
						TacitFormComposite.updateStatusMessage(getViewSite(),
								null, null, form);
						monitor.beginTask(
								"TACIT started Analyzing WordCount...",
								inputFiles.size() + 20);
						try {
							Display.getDefault().syncExec(new Runnable() {

								@Override
								public void run() {
									if (weightedWordCountButton.getSelection()) {
										wordCountController = new WordCountApi(
												true);
									} else {
										wordCountController = new WordCountApi(
												false);
									}

								}
							});
							if (isPreprocess) {
								monitor.subTask("Preprocessing...");
								preprocessTask = new Preprocess(
										"WeightedWordCount");
								try {
									dirPath = preprocessTask.doPreprocessing(
											inputFiles, "");
									File[] inputFile = new File(dirPath)
											.listFiles();
									for (File iFile : inputFile) {
										selectedFiles.add(iFile);
									}

								} catch (IOException e) {
									e.printStackTrace();
								}
								catch (NullPointerException e) {
									e.printStackTrace();
								}
							} else {
								for (String filepath : inputFiles) {
									if ((new File(filepath).isDirectory())) {
										continue;
									}
									selectedFiles.add(new File(filepath));
								}
							}

							wordCountController.wordCount(monitor,
									selectedFiles, dictionaryFiles,
									isPreprocess ? stopWordPath : "",
									outputPath, "", true, isLiwcStemming,
									isSnowBall, isSpss, isWdist, isStemDic,
									oFile, sFile, dateobj);

						} catch (IOException ioe) {

							ioe.printStackTrace();
						}
						TacitFormComposite.updateStatusMessage(getViewSite(),
								"Word count analysis completed", IStatus.OK,
								form);
						monitor.subTask("Cleaning Preprocessed Files...");
						if (isPreprocess)
							preprocessTask.clean();
						TacitFormComposite
								.writeConsoleHeaderBegining("<terminated> Word count analysis");
						monitor.done();
						return Status.OK_STATUS;
					}
				};
				wordCountJob.setUser(true);
				if (canProceed()) {
					wordCountJob.schedule();
				}
			};
		});
		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (WeightedWordCountImageRegistry.getImageIconFactory()
						.getImageDescriptor(IMAGE_HELP_CO));
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
								"edu.usc.cssl.tacit.wordcount.weighted.ui.weighted");
			};
		};
		mgr.add(helpAction);
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(helpAction,
						"edu.usc.cssl.tacit.wordcount.weighted.ui.weighted");
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(form,
						"edu.usc.cssl.tacit.wordcount.weighted.ui.weighted");
		form.getToolBarManager().update(true);
	}

	private boolean canProceed() {
		boolean canPerform = true;
		TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
		form.getMessageManager().removeMessage("location");
		form.getMessageManager().removeMessage("input");
		form.getMessageManager().removeMessage("dict");
		String message = OutputPathValidation.getInstance()
				.validateOutputDirectory(layoutData.getOutputLabel().getText(),
						"Output");
		if (message != null) {
			message = layoutData.getOutputLabel().getText() + " " + message;
			form.getMessageManager().addMessage("location", message, null,
					IMessageProvider.ERROR);
			canPerform = false;
		}
		// check input
		if (inputLayoutData.getSelectedFiles().size() < 1) {
			form.getMessageManager().addMessage("input",
					"Select/Add atleast one input file", null,
					IMessageProvider.ERROR);
			canPerform = false;
		}
		if (dictLayoutData.getSelectedFiles().size() < 1) {
			form.getMessageManager().addMessage("dict",
					"Select/Add atleast one Dictionary file", null,
					IMessageProvider.ERROR);
			canPerform = false;
		}
		return canPerform;
	}

	@Override
	public String getPartName() {
		// TODO Auto-generated method stub
		return "LIWC-Style Word Count";
	}

	@Override
	public void setFocus() {
		form.setFocus();
	}

}
