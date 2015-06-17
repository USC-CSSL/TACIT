package edu.usc.cssl.nlputils.topicmodel.lda.ui;

import java.io.File;
import java.io.FileNotFoundException;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.nlputils.common.ui.composite.from.NlputilsFormComposite;
import edu.usc.cssl.nlputils.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.nlputils.common.ui.validation.OutputPathValidation;
import edu.usc.cssl.nlputils.topicmodel.lda.services.LdaAnalysis;
import edu.usc.cssl.nlputils.topicmodel.lda.ui.internal.ILdaTopicModelClusterViewConstants;
import edu.usc.cssl.nlputils.topicmodel.lda.ui.internal.LdaTopicModelViewImageRegistry;
import edu.usc.nlputils.common.Preprocess;

public class LdaTopicModelView extends ViewPart implements
		ILdaTopicModelClusterViewConstants {
	public static final String ID = "edu.usc.cssl.nlputils.topicmodel.lda.ui.view1";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private Button preprocessEnabled;

	private OutputLayoutData layoutData;
	private OutputLayoutData inputLayoutData;
	private Text numberOfTopics;
	private Text prefixTxt;
	private LdaAnalysis lda = new LdaAnalysis();
	protected Job job;
	private Button wordWeights;

	@Override
	public void createPartControl(Composite parent) {
		toolkit = createFormBodySection(parent);
		Section section = toolkit.createSection(form.getBody(),
				Section.TITLE_BAR | Section.EXPANDED);

		GridDataFactory.fillDefaults().grab(true, false).span(3, 1)
				.applyTo(section);
		section.setExpanded(true);
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
		// create input data
		inputLayoutData = NlputilsFormComposite.createInputSection(toolkit,
				client, form.getMessageManager());
		Composite compInput;
		// Create pre process link
		compInput = inputLayoutData.getSectionClient();

		numberOfTopics = createAdditionalOptions(compInput, "No of Topics :",
				"1");

		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(compInput);
		createPreprocessLink(compInput);

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

		prefixTxt = createAdditionalOptions(output, "Output Prefix", "Lda_");
		
		wordWeights = toolkit.createButton(output, "Create Word Weight File", SWT.CHECK);

		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter());
		// form.setMessage("Invalid path", IMessageProvider.ERROR);
		this.setPartName("LDA Topic Model");
		addButtonsToToolBar();
		toolkit.paintBordersFor(form.getBody());

	}

	private void createPreprocessLink(Composite client) {

		Composite clientLink = toolkit.createComposite(client);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2)
				.applyTo(clientLink);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
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
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(link);

	}

	private Text createAdditionalOptions(Composite sectionClient,
			String lblText, String defaultText) {
		Label simpleTxtLbl = toolkit.createLabel(sectionClient, lblText,
				SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(simpleTxtLbl);
		Text simpleTxt = toolkit.createText(sectionClient, "", SWT.BORDER);
		simpleTxt.setText(defaultText);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0)
				.applyTo(simpleTxt);
		return simpleTxt;
	}

	private FormToolkit createFormBodySection(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);

		toolkit.decorateFormHeading(form.getForm());
		form.setText("LDA Topic Model"); //$NON-NLS-1$
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true)
				.applyTo(form.getBody());
		return toolkit;
	}

	private void addButtonsToToolBar() {
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (LdaTopicModelViewImageRegistry.getImageIconFactory()
						.getImageDescriptor(IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Analyze";
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.Action#run()
			 */
			public void run() {
				final int noOfTopics = Integer.valueOf(
						numberOfTopics.getText()).intValue();
				final boolean isPreprocess = preprocessEnabled.getSelection();
				final String inputPath = inputLayoutData
						.getOutputLabel().getText();
				final String outputPath = layoutData.getOutputLabel().getText();
				final String preFix =  prefixTxt.getText();
				final boolean wordWeightFile = wordWeights.getSelection();
				
				 job = new Job("Analyzing...") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("NLPUtils started clustering...", 100);
						List<String> inputFiles = new ArrayList<String>();
						String topicModelDirPath = inputPath;
						if (isPreprocess) {
							monitor.subTask("Preprocessing...");
							Preprocess preprocessTask = new Preprocess(
									"ZLabel");
							try {
								File[] inputFile = new File(inputPath)
										.listFiles();
								for (File iFile : inputFile) {
									if (iFile.getAbsolutePath().contains("DS_Store")) continue;
									inputFiles.add(iFile.toString());

								}
								topicModelDirPath = preprocessTask
										.doPreprocessing(inputFiles, "");

							} catch (IOException e) {
								e.printStackTrace();
							}
							monitor.worked(10);
						} 
						
						
						
						lda.initialize(topicModelDirPath, noOfTopics, outputPath,preFix,wordWeightFile);

						// lda processsing
						long startTime = System.currentTimeMillis();
						monitor.subTask("Topic Modelling...");
						try {
							lda.doLDA(monitor);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
							return Status.CANCEL_STATUS;
						} catch (IOException e) {
							monitor.done();
							return Status.CANCEL_STATUS;
						}
						monitor.worked(20);
						System.out
								.println("LDA TOpic Modelling completed successfully in "
										+ (System.currentTimeMillis() - startTime)
										+ " milliseconds.");

						if (monitor.isCanceled()) {
							return Status.CANCEL_STATUS;
						}
						monitor.worked(10);
						monitor.done();
						NlputilsFormComposite.updateStatusMessage(
								getViewSite(),
								"LDA Topic Modelling is successfully Completed.",
								IStatus.OK);

						return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				if (canProceedCluster()) {
					job.schedule();
				} else {
					NlputilsFormComposite
							.updateStatusMessage(
									getViewSite(),
									"LDA Topic Modelling cannot be started. Please check the Form status to correct the errors",
									IStatus.ERROR);
				}

			}

		});
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (LdaTopicModelViewImageRegistry.getImageIconFactory()
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

	@Override
	public void setFocus() {
		form.setFocus();
	}

	private boolean canProceedCluster() {
		NlputilsFormComposite.updateStatusMessage(getViewSite(), null,null);
		boolean canProceed = true;
		form.getMessageManager().removeMessage("location");
		form.getMessageManager().removeMessage("inputlocation");
		form.getMessageManager().removeMessage("prefix");
		form.getMessageManager().removeMessage("topic");
		String message = OutputPathValidation.getInstance()
				.validateOutputDirectory(layoutData.getOutputLabel().getText(),"Output");
		if (message != null) {

			message = layoutData.getOutputLabel().getText() + " " + message;
			form.getMessageManager().addMessage("location", message, null,
					IMessageProvider.ERROR);
			canProceed = false;
		}

		String inputMessage = OutputPathValidation.getInstance()
				.validateOutputDirectory(
						inputLayoutData.getOutputLabel().getText(),"Input");
		if (inputMessage != null) {

			inputMessage = layoutData.getOutputLabel().getText() + " "
					+ inputMessage;
			form.getMessageManager().addMessage("inputlocation", inputMessage,
					null, IMessageProvider.ERROR);
			canProceed = false;
		}

		if (prefixTxt.getText().length() < 1) {
			form.getMessageManager().addMessage("prefix",
					"Prefix Cannout be empty", null, IMessageProvider.ERROR);
			canProceed = false;
		}

		if (Integer.parseInt(numberOfTopics.getText()) < 1) {
			form.getMessageManager().addMessage("topic",
					"Number of topics cannot be less than 1", null,
					IMessageProvider.ERROR);
			canProceed = false;
		}
		return canProceed;
	}
	
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == Job.class) {
			return job;
		}
		return super.getAdapter(adapter);
	}


}
