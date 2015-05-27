package edu.usc.cssl.nlputils.wordcount.weighted.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.uc.cssl.nlputils.wordcount.weighted.services.WordCountApi;
import edu.usc.cssl.nlputils.common.ui.CommonUiActivator;
import edu.usc.cssl.nlputils.common.ui.composite.from.NlputilsFormComposite;
import edu.usc.cssl.nlputils.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.nlputils.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.nlputils.common.ui.validation.OutputPathValidation;
import edu.usc.cssl.nlputils.wordcount.weighted.ui.internal.IWeightedWordCountViewConstants;
import edu.usc.cssl.nlputils.wordcount.weighted.ui.internal.WeightedWordCountImageRegistry;

public class WeightedWordCountView extends ViewPart implements
		IWeightedWordCountViewConstants {
	public static String ID = "edu.usc.cssl.nlputils.wordcount.weighted.ui.view1";
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

		NlputilsFormComposite.createEmptyRow(toolkit, sc);

		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(2)
				.applyTo(client);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		inputLayoutData = NlputilsFormComposite.createTableSection(client,
				toolkit, layout, "Input Dtails",
				"Add file(s) or Folder(s) which contains data");
		dictLayoutData = NlputilsFormComposite.createTableSection(client,
				toolkit, layout, "Dictionary Details",
				"Add the location of Dictionary");

		// create type either LIWC or Weighted

		Composite wcTypeComposite = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(2)
				.applyTo(wcTypeComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(wcTypeComposite);

		createWordCountType(toolkit, wcTypeComposite, form.getMessageManager());

		Composite client1 = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1)
				.applyTo(client1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client1);

		layoutData = NlputilsFormComposite.createOutputSection(toolkit,
				client1, form.getMessageManager());

		// we dont need stop word's as it will be taken from the preprocessor
		// settings

		Composite output = layoutData.getSectionClient();

		createAdditionalOptions(toolkit, output);

		createStemmingOptions(form.getBody());

		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter());
		// form.setMessage("Invalid path", IMessageProvider.ERROR);
		this.setPartName("Weighted Word Count");
		addButtonsToToolBar();
		toolkit.paintBordersFor(form.getBody());

	}

	private void createWordCountType(FormToolkit toolkit2, Composite parent,
			IMessageManager messageManager) {

		Group buttonComposite = new Group(parent, SWT.LEFT);
		buttonComposite.setText("Word count type");
		buttonComposite.setBackground(parent.getBackground());
		buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonComposite.setLayout(layout);

		weightedWordCountButton = new Button(buttonComposite, SWT.RADIO);
		weightedWordCountButton.setText("Weighted Word Count");
		weightedWordCountButton.setSelection(true);
		weightedWordCountButton.setBackground(parent.getBackground());

		liwcWordCountButton = new Button(buttonComposite, SWT.RADIO);
		liwcWordCountButton.setText("LIWC Word Count");
		liwcWordCountButton.setSelection(false);
		liwcWordCountButton.setBackground(parent.getBackground());

		Label lblEmpty = new Label(buttonComposite, SWT.None);
		NlputilsFormComposite.createEmptyRow(toolkit, parent);

	}

	private void createStemmingOptions(Composite body) {
		Group downloadGroup = new Group(body, SWT.SHADOW_IN);
		downloadGroup.setText("Steming");

		downloadGroup.setBackground(body.getBackground());
		downloadGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		downloadGroup.setLayout(layout);

		
		stemEnabled = toolkit.createButton(downloadGroup, "Stem Dictionary",
				SWT.CHECK);
		stemEnabled.setBounds(10, 10, 10, 10);
		stemEnabled.pack();

		liwcStemming = toolkit.createButton(downloadGroup, "LIWC", SWT.RADIO);
		liwcStemming.setBounds(35, 35, 10, 10);
		liwcStemming.setEnabled(false);
		liwcStemming.setSelection(true);
		liwcStemming.pack();

		snowballStemming = toolkit.createButton(downloadGroup, "Snowball",
				SWT.RADIO);
		snowballStemming.setBounds(35, 60, 10, 10);
		snowballStemming.setEnabled(false);
		snowballStemming.pack();

		stemEnabled.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (stemEnabled.getSelection()) {
					liwcStemming.setEnabled(true);
					snowballStemming.setEnabled(true);
				} else {
					liwcStemming.setEnabled(false);
					snowballStemming.setEnabled(false);
				}
			}
		});

	}

	private void createAdditionalOptions(FormToolkit toolkit, Composite output) {
		spssRawFile = toolkit.createButton(output, "Create SPSS raw file",
				SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(spssRawFile);
		wordDistributionFile = toolkit.createButton(output,
				"Create Category-wise Word Distribution Files", SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(wordDistributionFile);
	}

	private FormToolkit createFormBodySection(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);

		toolkit.decorateFormHeading(form.getForm());
		form.setText("Weighted Word Count"); //$NON-NLS-1$
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

			public void run() {
				// WeightedCount wc = new WeightedCount();
				final String stopWordPath = CommonUiActivator.getDefault()
						.getPreferenceStore().getString("stop_words_path");
				// lindapulickal: handling case where user types in a file
				// without extension
				final String outputPath = layoutData.getOutputLabel().getText();
				String fileName = "wordcount";
				if(weightedWordCountButton.getSelection()){
					fileName = "weighted_" +fileName;
				}
				else {
					fileName = "liwc_" +fileName;
				}
				final File oFile = new File(outputPath + File.separator
						+ fileName + ".csv");
				final File sFile = new File(outputPath + File.separator
						+ fileName + ".dat");

				final List<String> inputFiles = inputLayoutData
						.getSelectedFiles();
				final List<String> dictionaryFiles = dictLayoutData
						.getSelectedFiles();
				final boolean isLiwcStemming = liwcStemming.getSelection();
				final boolean isSnowBall = snowballStemming.getSelection();
				final boolean isSpss = spssRawFile.getSelection();
				final boolean isWdist = wordDistributionFile.getSelection();
				final boolean isStemDic = stemEnabled.getSelection();
		
				Job wordCountJob = new Job("Analyzing...") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("NLPUtils started Analyzing WordCount...", 100);
						try { 
							Display.getDefault().syncExec(new Runnable() {
								
								@Override
								public void run() {
									if (weightedWordCountButton.getSelection()) {
										wordCountController = new WordCountApi(true);
									}
									else {
										wordCountController = new WordCountApi(false);
									}
									
								}
							});
							
							wordCountController.wordCount(new SubProgressMonitor(
									monitor, 100), inputFiles, dictionaryFiles,
									stopWordPath, outputPath, "", true,
									isLiwcStemming, isSnowBall, isSpss,
									isWdist, isStemDic, oFile, sFile);
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}
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
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (WeightedWordCountImageRegistry.getImageIconFactory()
						.getImageDescriptor(IMAGE_HELP_CO));
			}

			@Override
			public String getToolTipText() {
				return "Help";
			}

			public void run() {

			};
		});
		form.getToolBarManager().update(true);
	}

	protected boolean canProceed() {
		String message = OutputPathValidation.getInstance()
				.validateOutputDirectory(layoutData.getOutputLabel().getText());
		if (message != null) {

			message = layoutData.getOutputLabel().getText() + " " + message;
			form.getMessageManager().addMessage("location", message, null,
					IMessageProvider.ERROR);
			return false;
		} else {
			form.getMessageManager().removeMessage("location");
			// check input
			if (inputLayoutData.getSelectedFiles().size() < 1) {
				form.getMessageManager().addMessage("input",
						"Select/Add atleast one input file", null,
						IMessageProvider.ERROR);
				return false;
			} else {
				form.getMessageManager().removeMessage("input");
				if (dictLayoutData.getSelectedFiles().size() < 1) {
					form.getMessageManager().addMessage("dict",
							"Select/Add atleast one Dictionary file", null,
							IMessageProvider.ERROR);
					return false;

				} else {
					form.getMessageManager().removeMessage("dict");
					return true;
				}
			}
		}
	}

	@Override
	public void setFocus() {
		form.setFocus();
	}
}
