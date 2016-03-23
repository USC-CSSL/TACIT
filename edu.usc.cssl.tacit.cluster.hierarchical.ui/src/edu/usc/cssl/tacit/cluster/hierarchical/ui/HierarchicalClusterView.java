package edu.usc.cssl.tacit.cluster.hierarchical.ui;

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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
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

import edu.usc.cssl.tacit.cluster.hierarchical.services.HierarchicalClusterAnalysis;
import edu.usc.cssl.tacit.cluster.hierarchical.ui.internal.HeirarchicalClusterViewImageRegistry;
import edu.usc.cssl.tacit.cluster.hierarchical.ui.internal.IHeirarchicalClusterViewConstants;
import edu.usc.cssl.tacit.common.Preprocess;
import edu.usc.cssl.tacit.common.Preprocessor;
import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.tacit.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.tacit.common.ui.utility.TacitUtil;
import edu.usc.cssl.tacit.common.ui.validation.OutputPathValidation;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class HierarchicalClusterView extends ViewPart implements
		IHeirarchicalClusterViewConstants {
	public static String ID = "edu.usc.cssl.tacit.cluster.hierarchical.ui.view1";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private Button preprocessEnabled;
	private TableLayoutData layoutData;
	private OutputLayoutData layoutOutputData;
	private Button saveImage;
	protected Job performCluster;

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
		TacitFormComposite.addErrorPopup(form.getForm(), toolkit);
		TacitFormComposite.createEmptyRow(toolkit, sc);
		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1)
				.applyTo(client);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;

		layoutData = TacitFormComposite.createTableSection(client, toolkit,
				layout, "Input Details",
				"Add File(s) and Folder(s) to include in analysis.", true,
				true, true, true);

		Composite compInput;
		compInput = layoutData.getSectionClient();
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(compInput);

		createPreprocessLink(compInput);

		Composite client1 = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1)
				.applyTo(client1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client1);

		layoutOutputData = TacitFormComposite.createOutputSection(toolkit,
				client1, form.getMessageManager());
		createAdditionalOptions(toolkit, client1);

		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter());
		this.setPartName("Heirarchial Cluster");
		addButtonsToToolBar();
		toolkit.paintBordersFor(form.getBody());

	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == Job.class) {
			return performCluster;
		}
		return super.getAdapter(adapter);
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

	private void createAdditionalOptions(FormToolkit toolkit, Composite output) {
		Composite clientLink = toolkit.createComposite(output);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2)
				.applyTo(clientLink);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(clientLink);
		saveImage = toolkit.createButton(clientLink, "Save Dendogram as Image",
				SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(saveImage);
	}

	private FormToolkit createFormBodySection(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);

		toolkit.decorateFormHeading(form.getForm());
		form.setText("Heirarchial Cluster"); //$NON-NLS-1$
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true)
				.applyTo(form.getBody());
		return toolkit;
	}

	private void addButtonsToToolBar() {
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (HeirarchicalClusterViewImageRegistry
						.getImageIconFactory()
						.getImageDescriptor(IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Analyze";
			}

			@Override
			public void run() {
				if (!canProceedCluster()) {
					return;
				}
				final DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
				final Date dateObj = new Date();
				ConsoleView
						.writeInConsoleHeader("Hierarchical clustering started "
								+ (df.format(dateObj)));
				final boolean isPreprocess = preprocessEnabled.getSelection();
				final String outputPath = layoutOutputData.getOutputLabel()
						.getText();
				TacitUtil tacitHelper = new TacitUtil();
				final List<Object> selectedFiles = layoutData
						.getSelectedFiles();
				tacitHelper.writeSummaryFile(outputPath);

				final boolean isSaveImage = saveImage.getSelection();

				performCluster = new Job("Clustering...") {
					private Preprocess preprocessTask;
					private String dirPath;

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						TacitFormComposite.setConsoleViewInFocus();
						TacitFormComposite.updateStatusMessage(getViewSite(),
								null, null, form);
						monitor.beginTask("TACIT started clustering...", 100);
						preprocessTask = null;
						dirPath = "";
						List<File> inputFiles = new ArrayList<File>();
						Preprocessor ppObj = null;
						try {
							ppObj = new Preprocessor("Hierarchical_Clustering",
									isPreprocess);

							List<String> inFiles;

							inFiles = ppObj.processData(
									"Hierarchical_Clustering", selectedFiles);

							for (String f : inFiles) {
								inputFiles.add(new File(f));
							}
						} catch (IOException e) {
							e.printStackTrace();
							return Status.CANCEL_STATUS;
						} catch (Exception e) {
							e.printStackTrace();
							return Status.CANCEL_STATUS;
						}

						// Hierarchical processing
						long startTime = System.currentTimeMillis();
						boolean isSuccessful = HierarchicalClusterAnalysis
								.runClustering(inputFiles, outputPath,
										isSaveImage, new SubProgressMonitor(
												monitor, 50), dateObj, false);

						if (!isSuccessful)
							return Status.CANCEL_STATUS;

						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								if (isPreprocess && preprocessTask.doCleanUp()) {
									preprocessTask.clean();
									ConsoleView
											.printlInConsoleln("Cleaning up preprocessed files - "
													+ dirPath);
								}
							}

						});

						System.out
								.println("Hierarchical Clustering completed successfully in "
										+ (System.currentTimeMillis() - startTime)
										+ " milliseconds.");

						if (monitor.isCanceled()) {
							throw new OperationCanceledException();
						}
						ppObj.clean();
						monitor.done();

						return Status.OK_STATUS;
					}
				};

				performCluster.setUser(true);
				if (canProceedCluster()) {
					performCluster.schedule();
					performCluster.addJobChangeListener(new JobChangeAdapter() {

						@Override
						public void done(IJobChangeEvent event) {
							if (!event.getResult().isOK()) {
								TacitFormComposite
										.writeConsoleHeaderBegining("Error: <Terminated> Hierarchical Clustering");
								ConsoleView
										.printlInConsoleln("Hierarchical Clustering met with error.");
								ConsoleView
										.printlInConsoleln("Take appropriate action to resolve the issues and run again.");
							} else {
								TacitFormComposite
										.updateStatusMessage(
												getViewSite(),
												"Hierarchical Clustering successfully completed.",
												IStatus.OK, form);
								ConsoleView
										.writeInConsoleHeader("Success: <Completed> Hierarchical clustering  "
												+ (df.format(new Date())));
							}
						}
					});
				} else {
					TacitFormComposite
							.updateStatusMessage(
									getViewSite(),
									"CLustering cannot be started. Please check the Form status to correct the errors",
									IStatus.ERROR, form);
				}

			}

		});

		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (HeirarchicalClusterViewImageRegistry
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
								"edu.usc.cssl.tacit.cluster.hierarchical.ui.hierarchical");
			};
		};
		mgr.add(helpAction);
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(helpAction,
						"edu.usc.cssl.tacit.cluster.hierarchical.ui.hierarchical");
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(form,
						"edu.usc.cssl.tacit.cluster.hierarchical.ui.hierarchical");

		form.getToolBarManager().update(true);
	}

	@Override
	public void setFocus() {
		form.setFocus();
	}

	private boolean canProceedCluster() {
		TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
		boolean canProceed = true;
		form.getMessageManager().removeMessage("location");
		form.getMessageManager().removeMessage("input");
		String message = OutputPathValidation.getInstance()
				.validateOutputDirectory(
						layoutOutputData.getOutputLabel().getText(), "Output");
		if (message != null) {

			message = layoutOutputData.getOutputLabel().getText() + " "
					+ message;
			form.getMessageManager().addMessage("location", message, null,
					IMessageProvider.ERROR);
			canProceed = false;
		}

		// check input
		if (layoutData.getSelectedFiles().size() < 1) {
			form.getMessageManager().addMessage("input",
					"Select/Add atleast one input file", null,
					IMessageProvider.ERROR);
			canProceed = false;
		}
		return canProceed;
	}

}
