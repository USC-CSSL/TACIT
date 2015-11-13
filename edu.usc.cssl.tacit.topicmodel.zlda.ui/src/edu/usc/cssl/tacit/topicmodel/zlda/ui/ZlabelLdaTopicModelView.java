package edu.usc.cssl.tacit.topicmodel.zlda.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
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

import edu.usc.cssl.tacit.common.Preprocess;
import edu.usc.cssl.tacit.common.Preprocessor;
import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.tacit.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.tacit.common.ui.utility.TacitUtil;
import edu.usc.cssl.tacit.common.ui.validation.OutputPathValidation;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import edu.usc.cssl.tacit.topicmodel.zlda.services.ZlabelTopicModelAnalysis;
import edu.usc.cssl.tacit.topicmodel.zlda.ui.internal.IZlabelLdaTopicModelClusterViewConstants;
import edu.usc.cssl.tacit.topicmodel.zlda.ui.internal.ZlabelLdaTopicModelViewImageRegistry;

public class ZlabelLdaTopicModelView extends ViewPart implements
		IZlabelLdaTopicModelClusterViewConstants {
	public static final String ID = "edu.usc.cssl.tacit.topicmodel.zlda.ui.view1";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private Button preprocessEnabled;

	private OutputLayoutData layoutData;
	private TableLayoutData inputLayoutData;
	private Text seedFileText;
	private Text topics;
	private Button fAddFileButton;
	protected Job job;

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
		TacitFormComposite.addErrorPopup(form.getForm(), toolkit);
		TacitFormComposite.createEmptyRow(toolkit, sc);
		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1)
				.applyTo(client);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		// create input layout
		inputLayoutData = TacitFormComposite.createTableSection(client,
				toolkit, layout, "Input Details",
				"Add File(s) and Folder(s) to include in analysis.", true,
				true, true,true);
		Composite compInput;
		compInput = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(3)
				.applyTo(compInput);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 1)
				.applyTo(compInput);
		createPreprocessLink(compInput);
		seedFileText = createSeedFileControl(compInput, "Seed File Location :",
				"");
		topics = createAdditionalOptions(compInput, "No. of Topics :", "1");
		Composite client1 = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1)
				.applyTo(client1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client1);

		layoutData = TacitFormComposite.createOutputSection(toolkit, client1,
				form.getMessageManager());

		// we dont need stop word's as it will be taken from the preprocessor
		// settings

		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter());
		// form.setMessage("Invalid path", IMessageProvider.ERROR);
		this.setPartName("ZLabel LDA Topic Model");
		addButtonsToToolBar();
		toolkit.paintBordersFor(form.getBody());

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
				String id = "edu.usc.cssl.tacit.common.ui.prepocessorsettings";
				PreferencesUtil.createPreferenceDialogOn(link.getShell(), id,
						new String[] { id }, null).open();
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1)
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

	private Text createSeedFileControl(Composite sectionClient, String lblText,
			String defaultText) {
		Label simpleTxtLbl = toolkit.createLabel(sectionClient, lblText,
				SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(simpleTxtLbl);
		final Text simpleTxt = toolkit
				.createText(sectionClient, "", SWT.BORDER);
		simpleTxt.setText(defaultText);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
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
		form.setText("ZLabel LDA Topic Model"); //$NON-NLS-1$
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true)
				.applyTo(form.getBody());
		return toolkit;
	}

	private void addButtonsToToolBar() {
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (ZlabelLdaTopicModelViewImageRegistry
						.getImageIconFactory()
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
			@Override
			public void run() {
				if(!canProceedJob()) {
					return;
				}
				final int noOfTopics = Integer.valueOf(topics.getText())
						.intValue();
				final boolean isPreprocess = preprocessEnabled.getSelection();
				final String outputPath = layoutData.getOutputLabel().getText();
				TacitUtil tacitHelper = new TacitUtil();
				final List<Object> selectedFiles = inputLayoutData.getSelectedFiles();
				tacitHelper.writeSummaryFile(outputPath);
				
				final String seedFilePath = seedFileText.getText();
				TacitFormComposite
						.writeConsoleHeaderBegining("Topic Modelling  started ");
				job = new Job("Analyzing...") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						TacitFormComposite.setConsoleViewInFocus();
						TacitFormComposite.updateStatusMessage(getViewSite(),
								null, null, form);
						monitor.beginTask("TACIT started analyzing...", 100);
						String topicModelDirPath = System
								.getProperty("user.dir")
								+ File.separator
								+ "ZldaTopicModel"
								+ "_"
								+ String.valueOf(System.currentTimeMillis());
						Preprocessor ppObj = new Preprocessor();
						
						List<String> inFiles = null;
						try {
							inFiles = ppObj.processData("ZLabel_LDA", selectedFiles, isPreprocess);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						
						new File(topicModelDirPath).mkdir();
						for (String filename : inFiles) {
							File srcFile = new File(filename);
							File destDir = new File(topicModelDirPath);
							try {
								FileUtils.copyFileToDirectory(srcFile,
										destDir, false);
							} catch (IOException e) {
								e.printStackTrace();
								return Status.CANCEL_STATUS;
							}
						}

						long startTime = System.currentTimeMillis();

						Date dateObj = new Date();
						ZlabelTopicModelAnalysis zlda = new ZlabelTopicModelAnalysis(
								new SubProgressMonitor(monitor, 70));
						monitor.subTask("Topic Modelling...");
						try {
							zlda.invokeLDA(topicModelDirPath, seedFilePath,
									noOfTopics, outputPath, dateObj);
						} catch (Exception e) {
							e.printStackTrace();
							monitor.done();
							return Status.CANCEL_STATUS;
						}
						
						if (monitor.isCanceled()) {
							return Status.CANCEL_STATUS;
						}
						System.out
						.println("ZLabel LDA Topic Modelling completed successfully in "
								+ (System.currentTimeMillis() - startTime)
								+ " milliseconds.");
		
						ppObj.clean();
						monitor.worked(10);
						monitor.done();
						
						return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				if (canProceedJob()) {
					job.schedule();
					job.addJobChangeListener(new JobChangeAdapter() {

						public void done(IJobChangeEvent event) {
							if (!event.getResult().isOK()) {
								TacitFormComposite
										.writeConsoleHeaderBegining("Error: <Terminated> Z-LDA Topic Modelling");
								ConsoleView
										.printlInConsoleln("Z-LDA not successful.");
							}
							else {
								TacitFormComposite.updateStatusMessage(getViewSite(),
										"Z-Label LDA topic modelling completed", IStatus.OK,
										form);
								
								TacitFormComposite
										.writeConsoleHeaderBegining("Success: <Completed> Z-LDA Topic Modelling ");
								
							}
						}
					});
				} else {
					TacitFormComposite
							.updateStatusMessage(
									getViewSite(),
									"ZLabel LDA Topic Modelling cannot be started. Please check the Form status to correct the errors",
									IStatus.ERROR, form);
				}

			}

		});
		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (ZlabelLdaTopicModelViewImageRegistry
						.getImageIconFactory()
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
								"edu.usc.cssl.tacit.topicmodel.zlda.ui.zlda");
			};
		};
		mgr.add(helpAction);
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(helpAction,
						"edu.usc.cssl.tacit.topicmodel.zlda.ui.zlda");
		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(form, "edu.usc.cssl.tacit.topicmodel.zlda.ui.zlda");
		form.getToolBarManager().update(true);
	}

	@Override
	public void setFocus() {
		form.setFocus();
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == Job.class) {
			return job;
		}
		return super.getAdapter(adapter);
	}

	private boolean canProceedJob() {
		TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
		boolean canProceed = true;
		form.getMessageManager().removeMessage("location");
		form.getMessageManager().removeMessage("inputlocation");
		form.getMessageManager().removeMessage("topics");
		form.getMessageManager().removeMessage("seedfile");
		String message = OutputPathValidation.getInstance()
				.validateOutputDirectory(layoutData.getOutputLabel().getText(),
						"Output");
		if (message != null) {

			message = layoutData.getOutputLabel().getText() + " " + message;
			form.getMessageManager().addMessage("location", message, null,
					IMessageProvider.ERROR);
			canProceed = false;
		}
		// validate input
		if (inputLayoutData.getSelectedFiles().size() < 1) {
			form.getMessageManager().addMessage("input",
					"Select/Add atleast one input file", null,
					IMessageProvider.ERROR);
			canProceed = false;
		}
		String seedFileMsg = OutputPathValidation.getInstance()
				.validateOutputDirectory(seedFileText.getText(), "Seed File");
		if (seedFileMsg != null) {

			seedFileMsg = seedFileText.getText() + " " + seedFileMsg;
			form.getMessageManager().addMessage("seedfile", seedFileMsg, null,
					IMessageProvider.ERROR);
			canProceed = false;
		}
		if (topics.getText().isEmpty() || Integer.parseInt(topics.getText()) < 1) {
			form.getMessageManager().addMessage("topic",
					"Number of topics cannot be empty or less than 1", null,
					IMessageProvider.ERROR);
			canProceed = false;
		}
		return canProceed;
	}

}
