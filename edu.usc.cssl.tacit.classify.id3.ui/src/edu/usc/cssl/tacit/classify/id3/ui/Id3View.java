package edu.usc.cssl.tacit.classify.id3.ui;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.tacit.classify.id3.ui.internal.IId3ViewConstants;
import edu.usc.cssl.tacit.classify.id3.ui.internal.Id3ImageRegistry;
import edu.usc.cssl.tacit.common.Preprocessor;
import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.internal.TargetLocationsGroup;
import edu.usc.cssl.tacit.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

/**
 * Naive Bayes Classifier View
 */
public class Id3View extends ViewPart {
	public static String ID = "edu.usc.cssl.tacit.classify.id3.ui.view1";

	private ScrolledForm form;
	private FormToolkit toolkit;
	private TableLayoutData classLayoutData;

	// Classification parameters
	private Text kValueText;
	private Text classifyInputText;
	private Text outputPath;
	private Button preprocessEnabled;
	private Preprocessor preprocessTask;
	private boolean isPreprocessEnabled = false;
	private boolean isClassificationEnabled = false;
	private boolean canProceed = false;
	private Button classificationEnabled;
	Map<String, List<String>> classPaths;
	protected Job job;

	private boolean checkType = true;
	boolean breakFlag = false;
	
	public org.eclipse.swt.graphics.Image getTitleImage() {
		return Id3ImageRegistry.getImageIconFactory().getImage(IId3ViewConstants.IMAGE_ID3_TITLE);
	}
	@Override
	public void createPartControl(Composite parent) {
		// Creates toolkit and form
		toolkit = createFormBodySection(parent, "ID3 DECISION TREES");
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

		// Create table layout to hold the input data
		classLayoutData = TacitFormComposite.createTableSection(client, toolkit, layout, "Input Details",
				"Add Folder(s) or Corpus Classes to include in analysis.", true, false, true, true);

		// Create preprocess link
		createPreprocessLink(client);
		createClassificationParameters(client);
		createNBClassifierInputParameters(client); // to get k-value

		
		// Add run and help button on the toolbar 
		addButtonsToToolBar();
		form.setImage(Id3ImageRegistry.getImageIconFactory().getImage(IId3ViewConstants.IMAGE_ID3_TITLE));

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
	 * Validation for "Classification path"
	 * 
	 * @param classifyInputText
	 *            - path
	 * @param errorMessage
	 *            - message to be displayed in case of error
	 * @return
	 */
	protected boolean inputPathListener(Text classifyInputText, String errorMessage) {
		if (classificationEnabled.getSelection()) {
			if (classifyInputText.getText().isEmpty()) {
				form.getMessageManager().addMessage("classifyInput", errorMessage, null, IMessageProvider.ERROR);
				return false;
			}
			File tempFile = new File(classifyInputText.getText());
			if (!tempFile.exists() || !tempFile.isDirectory()) {
				form.getMessageManager().addMessage("classifyInput", errorMessage, null, IMessageProvider.ERROR);
				return false;
			} else {
				form.getMessageManager().removeMessage("classifyInput");
				String message = validateInputDirectory(classifyInputText.getText().toString());
				if (null != message) {
					form.getMessageManager().addMessage("classifyInput", message, null, IMessageProvider.ERROR);
					return false;
				}
			}
		} else
			form.getMessageManager().removeMessage("classifyInput");
		return true;
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

	/**
	 * Creates appropriate UI components for Naive NBayes
	 * 
	 * @param client
	 */
	private void createNBClassifierInputParameters(Composite client) {
		Label kValueLabel;
		Section inputParamsSection = toolkit.createSection(client,
				Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(inputParamsSection);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(inputParamsSection);
		inputParamsSection.setText("Output Details");
		inputParamsSection.setDescription("Choose output details for storing the results");

		ScrolledComposite sc = new ScrolledComposite(inputParamsSection, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);

		Composite sectionClient = toolkit.createComposite(inputParamsSection);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sectionClient);
		inputParamsSection.setClient(sectionClient);

		TacitFormComposite.createEmptyRow(toolkit, sectionClient);

		// Output path
		final Label outputPathLabel = toolkit.createLabel(sectionClient, "Output Path:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(outputPathLabel);
		outputPath = toolkit.createText(sectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(outputPath);
		outputPath.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				outputPathListener(outputPath, "Output path must be a valid diretory location");
			}

			@Override
			public void keyPressed(KeyEvent e) {
				outputPathListener(outputPath, "Output path must be a valid diretory location");
			}
		});
		final Button browseBtn2 = toolkit.createButton(sectionClient, "Browse", SWT.PUSH);
		browseBtn2.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String path = openBrowseDialog(browseBtn2);
				if (null == path)
					return;
				outputPath.setText(path);
				form.getMessageManager().removeMessage("outputPath");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		kValueLabel = toolkit.createLabel(sectionClient, "k Value for Cross Validation:", SWT.None);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(kValueLabel);
		kValueText = toolkit.createText(sectionClient, "10", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(kValueText);

		kValueText.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (!(e.character >= '0' && e.character <= '9')) {
					form.getMessageManager().addMessage("kvalue", "Provide valid K-Value for cross validation", null,
							IMessageProvider.ERROR);
					kValueText.setText("");
				} else {
					form.getMessageManager().removeMessage("kvalue");
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

	}

	/**
	 * Creates appropriate classification parameters
	 * 
	 * @param client
	 */
	private void createClassificationParameters(Composite client) {
		Group group = new Group(client, SWT.SHADOW_IN);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);

		classificationEnabled = new Button(group, SWT.CHECK);
		classificationEnabled.setText("Classify Unlabeled Data");
		classificationEnabled.setBounds(10, 10, 10, 10);
		classificationEnabled.pack();

		final Composite sectionClient = new Composite(group, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(sectionClient);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sectionClient);
		sectionClient.pack();

		// Create a row that holds the textbox and browse button
		final Label inputPathLabel = new Label(sectionClient, SWT.NONE);
		inputPathLabel.setText("Classification Input Path:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(inputPathLabel);
		classifyInputText = new Text(sectionClient, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(classifyInputText);
		classifyInputText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				inputPathListener(classifyInputText, "Classification input path must be a valid diretory location");
			}

			@Override
			public void keyPressed(KeyEvent e) {
				inputPathListener(classifyInputText, "Classification input path must be a valid diretory location");
			}
		});
		final Button browseBtn1 = new Button(sectionClient, SWT.PUSH);
		browseBtn1.setText("Browse");
		browseBtn1.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String path = openBrowseDialog(browseBtn1);
				if (null == path)
					return;
				classifyInputText.setText(path);
				form.getMessageManager().removeMessage("classifyInput");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		inputPathLabel.setEnabled(false);
		classifyInputText.setEnabled(false);
		browseBtn1.setEnabled(false);

		classificationEnabled.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (classificationEnabled.getSelection()) {
					sectionClient.setEnabled(true);
					inputPathLabel.setEnabled(true);
					classifyInputText.setEnabled(true);
					browseBtn1.setEnabled(true);
					inputPathListener(classifyInputText, "Classification input path must be a valid diretory location");
				} else {
					sectionClient.setEnabled(false);
					inputPathLabel.setEnabled(false);
					classifyInputText.setEnabled(false);
					browseBtn1.setEnabled(false);
					form.getMessageManager().removeMessage("classifyInput");
				}
			}
		});
	}

	/**
	 * To create preprocess hyperlink
	 * 
	 * @param client
	 */
	private void createPreprocessLink(Composite client) {
		Composite clientLink = toolkit.createComposite(client);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2).applyTo(clientLink);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(clientLink);

		preprocessEnabled = toolkit.createButton(clientLink, "", SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(preprocessEnabled);
		final Hyperlink link = toolkit.createHyperlink(clientLink, "Preprocess", SWT.NONE);
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
				PreferencesUtil.createPreferenceDialogOn(link.getShell(), id, new String[] { id }, null).open();
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(link);
	}

	/**
	 * Adds "Classify" and "Help" buttons on the Naive Bayes Classifier form
	 */
	private void addButtonsToToolBar() {
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (Id3ImageRegistry.getImageIconFactory().getImageDescriptor(IId3ViewConstants.IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Classify";
			}

			String classificationInputDir;

			String outputDir;
			String tempkValue;
			List<Object> selectedFiles;

		
			

		});
	
		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (Id3ImageRegistry.getImageIconFactory().getImageDescriptor(IId3ViewConstants.IMAGE_HELP_CO));
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
		ConsoleView.printlInConsoleln("Naive Bayes classifier cancelled.");
		if (isPreprocessEnabled) {
			preprocessTask.clean();
		}
		return Status.CANCEL_STATUS;

	}

	/**
	 * Validates the input form to ensure correctness
	 * 
	 * @param classPaths
	 * @return
	 */
	private boolean canItProceed(Map<String, List<String>> classPaths) {
		
		// Class paths
		if (TargetLocationsGroup.corpusClass < 2 && classLayoutData.getTree().getItemCount() < 2) {
			form.getMessageManager().addMessage("classes", "Provide at least 2 valid class paths", null,
					IMessageProvider.ERROR);
			return false;
		} else if (classLayoutData.getTree().getItemCount() > 1 && TargetLocationsGroup.corpusClass < 2) {
			form.getMessageManager().addMessage("classes", "Select the required classes", null, IMessageProvider.ERROR);
			return false;
		} else {
			form.getMessageManager().removeMessage("classes");
		}

		// K-Vlaue
		if (kValueText.getText().isEmpty() || Integer.parseInt(kValueText.getText()) < 2) {
			form.getMessageManager().addMessage("kvalue",
					"Provide valid K-Value for cross validation, K must be atleast 2", null, IMessageProvider.ERROR);
			return false;
		} else {
			form.getMessageManager().removeMessage("kvalue");
		}

		if (!outputPathListener(outputPath, "Output path must be a valid diretory location")) {
			return false;
		}

		// Classification parameters
		isClassificationEnabled = classificationEnabled.getSelection();
		if (isClassificationEnabled) {
			if (!inputPathListener(classifyInputText, "Classifciation Input path must be a valid diretory location")) {
				return false;
			}
		} else {
			form.getMessageManager().removeMessage("classifyInputText");
		}
		return true;
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
