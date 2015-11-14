package edu.usc.cssl.tacit.classify.svm.ui;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
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

import edu.usc.cssl.tacit.classify.svm.services.CrossValidator;
import edu.usc.cssl.tacit.classify.svm.services.SVMClassify;
import edu.usc.cssl.tacit.classify.svm.ui.internal.ISVMViewConstants;
import edu.usc.cssl.tacit.classify.svm.ui.internal.SVMViewImageRegistry;
import edu.usc.cssl.tacit.common.Preprocess;
import edu.usc.cssl.tacit.common.Preprocessor;
import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.tacit.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.tacit.common.ui.utility.TacitUtil;
import edu.usc.cssl.tacit.common.ui.validation.OutputPathValidation;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class SVMView extends ViewPart implements ISVMViewConstants {

	private FormToolkit toolkit;
	private ScrolledForm form;
	public static String ID = "edu.usc.cssl.tacit.classify.svm.ui.view1";
	private TableLayoutData class1LayoutData;
	private TableLayoutData class2LayoutData;
	private OutputLayoutData layoutData;
	private Text class1Name;
	private Text class2Name;
	private Label class1Label;
	private Label class2Label;
	private Label kValueLabel;
	private Text kValue;
	private Button preprocessButton;
	private Button featureFileButton;
	protected Job job;

	@Override
	public void createPartControl(Composite parent) {
		toolkit = createFormBodySection(parent);
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(section);
		section.setExpanded(true);

		String description = "This sections gives details about the SVM Classifier";
		FormText descriptionFrm = toolkit.createFormText(section, false);
		descriptionFrm.setText("<form><p>" + description + "</p></form>", true, false);
		section.setDescriptionControl(descriptionFrm);

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);

		TacitFormComposite.createEmptyRow(toolkit, sc);

		createInputParams(form.getBody());

		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(2).applyTo(client);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(client);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		class1LayoutData = TacitFormComposite.createTableSection(client, toolkit, layout, "Class 1 Details",
				"Add File(s) and Folder(s) to include in analysis.", true, true, true, true);
		class2LayoutData = TacitFormComposite.createTableSection(client, toolkit, layout, "Class 2 Details",
				"Add File(s) and Folder(s) to include in analysis.", true, true, true, false);

		createPreprocessLink(form.getBody());

		// Output Data
		Composite client1 = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1).applyTo(client1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(client1);

		layoutData = TacitFormComposite.createOutputSection(toolkit, client1, form.getMessageManager());
		Composite output = layoutData.getSectionClient();

		kValueLabel = toolkit.createLabel(output, "k Value for Cross Validation:", SWT.None);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(kValueLabel);
		kValue = toolkit.createText(output, "10", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(kValue);

		featureFileButton = toolkit.createButton(output, "Create feature weights file", SWT.CHECK);
		featureFileButton.setBounds(10, 35, 10, 10);
		featureFileButton.pack();

		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter());
		this.setPartName("SVM Classification");
		addButtonsToToolBar();
		toolkit.paintBordersFor(form.getBody());
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == Job.class) {
			return job;
		}
		return super.getAdapter(adapter);
	}

	private void addButtonsToToolBar() {
		IToolBarManager mgr = form.getToolBarManager();

		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (SVMViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Run";
			}

			public void run() {
				if (!canProceed())
					return;
				final List<Object> class1Files = class1LayoutData.getSelectedFiles();
				final List<Object> class2Files = class2LayoutData.getSelectedFiles();
				final String class1NameStr = class1Name.getText();
				final String class2NameStr = class2Name.getText();
				final int kValueInt = Integer.parseInt(kValue.getText());
				;
				final String outputPath = layoutData.getOutputLabel().getText();
				final boolean featureFile = featureFileButton.getSelection();
				final boolean ppValue = preprocessButton.getSelection();
				final Preprocess preprocessor = new Preprocess("SVM_Classifier");

				final SVMClassify svm = new SVMClassify(class1NameStr, class2NameStr, outputPath);
				final CrossValidator cv = new CrossValidator();
				TacitFormComposite.writeConsoleHeaderBegining("SVM classification started  ");
				job = new Job("Classifier Job") {
					protected IStatus run(IProgressMonitor monitor) {
						TacitFormComposite.setConsoleViewInFocus();
						TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
						monitor.beginTask("SVM Classification", kValueInt + 4);
						String ppClass1 = "";
						String ppClass2 = "";
						File[] class1FilesL;
						File[] class2FilesL;
						Date dateObj = new Date();
						Preprocessor ppObjC1 = new Preprocessor();
						Preprocessor ppObjC2 = new Preprocessor();

						try {
							List<String> c1Files = ppObjC1.processData("SVM_Class1", class1Files, ppValue);
							List<String> c2Files = ppObjC2.processData("SVM_Class2", class2Files, ppValue);
							class1FilesL = new File[c1Files.size()];
							class2FilesL = new File[c2Files.size()];

							for (int k = 0; k < c1Files.size(); k++) {
								class1FilesL[k] = new File(c1Files.get(k));
							}

							for (int k = 0; k < c2Files.size(); k++) {
								class2FilesL[k] = new File(c2Files.get(k));
							}

							monitor.worked(2);

							cv.doCross(svm, class1NameStr, class1FilesL, class2NameStr, class2FilesL, kValueInt,
									featureFile, outputPath, monitor, dateObj);
							monitor.worked(5);
							ppObjC1.clean();
							ppObjC2.clean();

							monitor.worked(2);

						} catch (IOException e) {
							e.printStackTrace();
						}
						monitor.done();
						return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				job.schedule();
				job.addJobChangeListener(new JobChangeAdapter() {

					public void done(IJobChangeEvent event) {
						if (!event.getResult().isOK()) {
							TacitFormComposite.writeConsoleHeaderBegining("Error: <Terminated> SVM classification");
							ConsoleView.printlInConsoleln("SVM met with error.");
							ConsoleView
									.printlInConsoleln("Take appropriate action to resolve the issues and try again.");
						} else {
							TacitFormComposite.updateStatusMessage(getViewSite(), "SVM classification completed",
									IStatus.OK, form);
							TacitFormComposite.writeConsoleHeaderBegining("Success: <Completed> SVM Classification");

						}
					}
				});
			};
		});

		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (SVMViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_HELP_CO));
			}

			@Override
			public String getToolTipText() {
				return "Help";
			}

			@Override
			public void run() {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp("edu.usc.cssl.tacit.classify.svm.ui.svm");
			};
		};
		mgr.add(helpAction);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(helpAction, "edu.usc.cssl.tacit.classify.svm.ui.svm");
		PlatformUI.getWorkbench().getHelpSystem().setHelp(form, "edu.usc.cssl.tacit.classify.svm.ui.svm");
		form.getToolBarManager().update(true);
	}

	protected boolean canProceed() {
		// Remove all errors from any previous tries
		TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
		form.getMessageManager().removeMessage("class1");
		form.getMessageManager().removeMessage("class2");
		form.getMessageManager().removeMessage("class1NoProper");
		form.getMessageManager().removeMessage("class2NoProper");
		form.getMessageManager().removeMessage("class1Name");
		form.getMessageManager().removeMessage("class2Name");
		form.getMessageManager().removeMessage("sameName");
		form.getMessageManager().removeMessage("kValueEmpty");
		form.getMessageManager().removeMessage("kValue");
		form.getMessageManager().removeMessage("output");

		List<String> class1Files = new TacitUtil().refineInput(class1LayoutData.getSelectedFiles());
		List<String> class2Files = new TacitUtil().refineInput(class2LayoutData.getSelectedFiles());
		boolean noProperFiles = true;

		if (class1Files.size() < 1) {
			form.getMessageManager().addMessage("class1", "Select/Add atleast one Class 1 file", null,
					IMessageProvider.ERROR);
			return false;
		}

		for (String string : class1Files) {
			if (new File(string).isFile() && !string.contains("DS_Store")) {
				noProperFiles = false;
				break;
			}
		}
		if (noProperFiles) {
			form.getMessageManager().addMessage("class1NoProper", "Select/Add atleast one Proper Class 1 file", null,
					IMessageProvider.ERROR);
			return false;
		}

		noProperFiles = true;
		if (class2Files.size() < 1) {
			form.getMessageManager().addMessage("class2", "Select/Add atleast one Class 2 file", null,
					IMessageProvider.ERROR);
			return false;
		}
		form.getMessageManager().removeMessage("class2");
		for (String string : class2Files) {
			if (new File(string).isFile() && !string.contains("DS_Store")) {
				noProperFiles = false;
				break;
			}
		}
		if (noProperFiles) {
			form.getMessageManager().addMessage("class2NoProper", "Select/Add atleast one Proper Class 2 file", null,
					IMessageProvider.ERROR);
			return false;
		}

		if (class1Name.getText().trim().length() == 0) {
			form.getMessageManager().addMessage("class1Name", "Class 1 name cannot be empty", null,
					IMessageProvider.ERROR);
			return false;
		}
		form.getMessageManager().removeMessage("class1Name");

		if (class2Name.getText().trim().length() == 0) {
			form.getMessageManager().addMessage("class2Name", "Class 2 name cannot be empty", null,
					IMessageProvider.ERROR);
			return false;
		}
		form.getMessageManager().removeMessage("class2Name");

		if (class2Name.getText().trim().equals(class1Name.getText().trim())) {
			form.getMessageManager().addMessage("sameName", "Class 1 and Class 2 cannot have the same name", null,
					IMessageProvider.ERROR);
			return false;
		}
		form.getMessageManager().removeMessage("sameName");

		String kValueText = kValue.getText();
		if (kValueText.trim().length() == 0) {
			form.getMessageManager().addMessage("kValueEmpty", "k Value cannot be empty", null, IMessageProvider.ERROR);
			return false;
		}
		form.getMessageManager().removeMessage("kValueEmpty");

		int value = 0;
		try {
			value = Integer.parseInt(kValueText);
		} catch (NumberFormatException e) {
			form.getMessageManager().addMessage("kValue", "k Value should be an integer", null, IMessageProvider.ERROR);
			return false;
		} catch (NullPointerException e) {
			form.getMessageManager().addMessage("kValue", "k Value should be an integer", null, IMessageProvider.ERROR);
			return false;
		}
		if (value < 0) {
			form.getMessageManager().addMessage("kValue", "k Value should be greater than or equal to 0", null,
					IMessageProvider.ERROR);
			return false;
		}
		form.getMessageManager().removeMessage("kValue");

		String message = OutputPathValidation.getInstance()
				.validateOutputDirectory(layoutData.getOutputLabel().getText(), "Output");
		if (message != null) {

			message = layoutData.getOutputLabel().getText() + " " + message;
			form.getMessageManager().addMessage("output", message, null, IMessageProvider.ERROR);
			return false;
		}
		form.getMessageManager().removeMessage("output");

		return true;
	}

	private void createPreprocessLink(Composite client) {

		Composite clientLink = toolkit.createComposite(client);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2).applyTo(clientLink);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(clientLink);

		preprocessButton = toolkit.createButton(clientLink, "", SWT.CHECK);
		preprocessButton.setSelection(true);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(preprocessButton);
		final Hyperlink link = toolkit.createHyperlink(clientLink, "Preprocess", SWT.NONE);
		link.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		link.addHyperlinkListener(new IHyperlinkListener() {
			public void linkEntered(HyperlinkEvent e) {
			}

			public void linkExited(HyperlinkEvent e) {
			}

			public void linkActivated(HyperlinkEvent e) {
				String id = "edu.usc.cssl.tacit.common.ui.prepocessorsettings";
				PreferencesUtil.createPreferenceDialogOn(link.getShell(), id, new String[] { id }, null).open();
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(link);

	}

	private void createInputParams(Composite body) {
		Section inputParamsSection = toolkit.createSection(body,
				Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(inputParamsSection);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(inputParamsSection);
		inputParamsSection.setText("Class Labels");

		ScrolledComposite sc = new ScrolledComposite(inputParamsSection, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);

		Composite sectionClient = toolkit.createComposite(inputParamsSection);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(false).applyTo(sectionClient);
		inputParamsSection.setClient(sectionClient);

		class1Label = toolkit.createLabel(sectionClient, "Enter Class 1 Label:", SWT.None);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(class1Label);
		class1Name = toolkit.createText(sectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(class1Name);
		class2Label = toolkit.createLabel(sectionClient, "Enter Class 2 Label:", SWT.None);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(class2Label);
		class2Name = toolkit.createText(sectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(class2Name);

	}

	private FormToolkit createFormBodySection(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);

		toolkit.decorateFormHeading(form.getForm());
		form.setText("SVM Classifier");
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true).applyTo(form.getBody());
		return toolkit;
	}

	@Override
	public void setFocus() {
		form.setFocus();
	}

}
