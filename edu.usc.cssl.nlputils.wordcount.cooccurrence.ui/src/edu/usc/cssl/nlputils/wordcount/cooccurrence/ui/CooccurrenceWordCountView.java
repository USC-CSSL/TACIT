package edu.usc.cssl.nlputils.wordcount.cooccurrence.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.nlputils.common.ui.composite.from.NlputilsFormComposite;
import edu.usc.cssl.nlputils.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.nlputils.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.nlputils.common.ui.validation.OutputPathValidation;
import edu.usc.cssl.nlputils.common.ui.views.ConsoleView;
import edu.usc.cssl.nlputils.wordcount.cooccurrence.services.CooccurrenceAnalysis;
import edu.usc.cssl.nlputils.wordcount.cooccurrence.ui.internal.CooccurrenceWordCountImageRegistry;
import edu.usc.cssl.nlputils.wordcount.cooccurrence.ui.internal.ICooccurrenceWordCountViewConstants;
import edu.usc.nlputils.common.Preprocess;

public class CooccurrenceWordCountView extends ViewPart implements
		ICooccurrenceWordCountViewConstants {
	public static String ID = "edu.usc.cssl.nlputils.wordcount.cooccurrence.ui.view1";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private OutputLayoutData layoutData;
	private Button buildMAtrix;
	private Button wordFile;
	private Button fAddFileButton;
	private TableLayoutData inputLayoutData;
	private Button preprocessEnabled;
	private Text seedFile;
	private Text windowSize;
	private Text thresholdValue;
	private Job cooccurrenceAnalysisJob;
	@Override
	public void createPartControl(Composite parent) {
		toolkit = createFormBodySection(parent);
		Section section = toolkit.createSection(form.getBody(),
				Section.TITLE_BAR | Section.EXPANDED);

		GridDataFactory.fillDefaults().grab(true, false).span(3, 1)
				.applyTo(section);
		section.setExpanded(true);
		String description = "This section gives details about Heirarchical clustering ";
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
		NlputilsFormComposite.addErrorPopup(form.getForm(), toolkit);
		NlputilsFormComposite.createEmptyRow(toolkit, sc);
		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1)
				.applyTo(client);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;

		inputLayoutData = NlputilsFormComposite.createTableSection(client,
				toolkit, layout, "Input Dtails",
				"Add File(s) and Folder(s) to include in analysis.", true);
		Composite compInput;
		compInput = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(3)
				.applyTo(compInput);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 1)
				.applyTo(compInput);
		createPreprocessLink(compInput);
		seedFile = createWordFileControl(compInput, "Word File :");
		windowSize = createAdditionalOptions(compInput, "Window Size :", "1");
		thresholdValue = createAdditionalOptions(compInput, "Threshold :", "");

		Composite client1 = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1)
				.applyTo(client1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client1);

		layoutData = NlputilsFormComposite.createOutputSection(toolkit,
				client1, form.getMessageManager());

		buildCooccurrenceMatrix(form.getBody());

		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter());
		addButtonsToToolBar();
		toolkit.paintBordersFor(form.getBody());

	}

	private Text createAdditionalOptions(Composite sectionClient,
			String numLabel, String defaultValue) {
		Text numTxt;
		Label noLabel = toolkit.createLabel(sectionClient, numLabel, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(noLabel);
		numTxt = toolkit.createText(sectionClient, "", SWT.BORDER);
		numTxt.setText(defaultValue);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0)
				.applyTo(numTxt);
		return numTxt;
	}
	
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == Job.class) {
			return cooccurrenceAnalysisJob;
		}
		return super.getAdapter(adapter);
	}

	private void createPreprocessLink(Composite client) {

		Composite clientLink = toolkit.createComposite(client);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(3)
				.applyTo(clientLink);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 1)
				.applyTo(clientLink);

		preprocessEnabled = toolkit.createButton(clientLink, "", SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(preprocessEnabled);
		final Hyperlink link = toolkit.createHyperlink(clientLink,
				"Preprocess", SWT.NONE);
		link.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		link.addHyperlinkListener(new IHyperlinkListener() {
			public void linkEntered(HyperlinkEvent e) {
			}

			public void linkExited(HyperlinkEvent e) {
			}

			public void linkActivated(HyperlinkEvent e) {
				String id = "edu.usc.cssl.nlputils.common.ui.prepocessorsettings";
				PreferencesUtil.createPreferenceDialogOn(link.getShell(), id,
						new String[] { id }, null).open();
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1)
				.applyTo(link);

	}

	private void buildCooccurrenceMatrix(Composite body) {

		buildMAtrix = toolkit.createButton(body,
				"Build Co-occurrence Matrices", SWT.CHECK);

		GridDataFactory.fillDefaults().grab(false, false).span(3, 1)
				.applyTo(buildMAtrix);

	}

	private Text createWordFileControl(Composite sectionClient, String lblText) {
		Label simpleTxtLbl = toolkit.createLabel(sectionClient, lblText,
				SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(simpleTxtLbl);
		final Text simpleTxt = toolkit
				.createText(sectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(simpleTxt);
		fAddFileButton = toolkit.createButton(sectionClient, "Browse...",
				SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(fAddFileButton);
		fAddFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(fAddFileButton.getShell(),
						SWT.OPEN);
				dlg.setText("Select File");
				String path = dlg.open();
				if (path == null)
					return;
				simpleTxt.setText(path);
			}
		});

		return simpleTxt;
	}

	private FormToolkit createFormBodySection(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);

		toolkit.decorateFormHeading(form.getForm());
		form.setText("Co-occurrence Analysis"); //$NON-NLS-1$
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true)
				.applyTo(form.getBody());
		return toolkit;
	}

	private void addButtonsToToolBar() {
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {

			@Override
			public ImageDescriptor getImageDescriptor() {
				return (CooccurrenceWordCountImageRegistry
						.getImageIconFactory()
						.getImageDescriptor(IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Analyze";
			}

			public void run() {

				final String outputPath = layoutData.getOutputLabel().getText();
				final boolean isPreprocess = preprocessEnabled.getSelection();
				final List<String> selectedFiles = inputLayoutData
						.getSelectedFiles();
				final boolean isBuildMatrix = buildMAtrix.getSelection();
				final String windowSizeStr = windowSize.getText();
				final String thresholdLimit = thresholdValue.getText();
				cooccurrenceAnalysisJob = new Job(
						"Co-occurrence Analysis...") {
					
					private Preprocess preprocessTask;
					private String dirPath;
					private String seedFilePath;
					private String seedFileLocation;

					protected IStatus run(IProgressMonitor monitor) {
						NlputilsFormComposite.setConsoleViewInFocus();
						monitor.beginTask("NLPUtils started clustering...", 100);
						preprocessTask = null;
						dirPath = "";
						seedFilePath = seedFile.getText();
						List<File> inputFiles = new ArrayList<File>();
						if (isPreprocess) {
							monitor.subTask("Preprocessing...");
							preprocessTask = new Preprocess(
									"CooccurrenceAnalysis");
							try {
								dirPath = preprocessTask.doPreprocessing(
										selectedFiles, "");
								ArrayList<String> seedList = new ArrayList<String>();
								seedList.add(seedFilePath);
								preprocessTask.doPreprocessing(seedList, "");
								seedFileLocation = new File(dirPath
										+ File.separator
										+ new File(seedFilePath).getName())
										.getAbsolutePath();
								File[] inputFile = new File(dirPath)
										.listFiles();
								for (File iFile : inputFile) {
									inputFiles.add(iFile);
								}

							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							for (String filepath : selectedFiles) {
								if ((new File(filepath).isDirectory())) {
									continue;
								}
								inputFiles.add(new File(filepath));
							}
						}

						long startTime = System.currentTimeMillis();
						boolean result = new CooccurrenceAnalysis()
								.invokeCooccurrence(selectedFiles,
										seedFileLocation, outputPath,
										windowSizeStr, thresholdLimit,
										isBuildMatrix);

						if (result) {
							NlputilsFormComposite.updateStatusMessage(getViewSite(), "Cooccurence analysis completed", IStatus.OK);
							ConsoleView.printlInConsoleln("Co-occurrence Analysis completed in "+(System.currentTimeMillis()-startTime)+" milliseconds.");
							preprocessTask.clean();
							return Status.OK_STATUS;
						} else {
							NlputilsFormComposite
									.updateStatusMessage(
											getViewSite(),
											"Co-occurrence Analysis is not Completed. Please check the log in the console",
											IStatus.ERROR);
							preprocessTask.clean();
							return Status.CANCEL_STATUS;
						}
					}
				};
				cooccurrenceAnalysisJob.setUser(true);
				if (canProceed()) {
					cooccurrenceAnalysisJob.schedule();
				}
			};
		});
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (CooccurrenceWordCountImageRegistry
						.getImageIconFactory()
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

	private boolean canProceed() {
		NlputilsFormComposite.updateStatusMessage(getViewSite(), null,null);
		boolean canPerform = true;
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

		return canPerform;
	}

	@Override
	public String getPartName() {
		// TODO Auto-generated method stub
		return "Co-occurrence Analysis";
	}

	@Override
	public void setFocus() {
		form.setFocus();
	}
}
