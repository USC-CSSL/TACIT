package edu.usc.cssl.nlputils.classify.naivebayes.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import bsh.EvalError;
import edu.usc.cssl.nlputils.classify.naivebayes.services.NaiveBayesClassifier;
import edu.usc.cssl.nlputils.classify.naivebayes.ui.internal.INaiveBayesClassifierViewConstants;
import edu.usc.cssl.nlputils.classify.naivebayes.ui.internal.NaiveBayesClassifierViewImageRegistry;
import edu.usc.cssl.nlputils.common.ui.CommonUiActivator;
import edu.usc.cssl.nlputils.common.ui.composite.from.NlputilsFormComposite;
import edu.usc.cssl.nlputils.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.nlputils.common.ui.views.ConsoleView;
import edu.usc.nlputils.common.Preprocess;

public class NaiveBayesClassifierView extends ViewPart implements
		INaiveBayesClassifierViewConstants {
	public static String ID = "edu.usc.cssl.nlputils.classify.naivebayes.ui.naivebayesview";

	private ScrolledForm form;
	private FormToolkit toolkit;
	private TableLayoutData classLayoutData;
	// Classification parameters
	private Text kValueText;
	private Text classifyInputText;
	private Text classifyOutputText;
	private Button preprocessEnabled;

	private Preprocess preprocessTask;
	private boolean isPreprocessEnabled = false;
	private String pp_outputPath = null;

	@Override
	public void createPartControl(Composite parent) {
		// Creates toolkit and form
		toolkit = createFormBodySection(parent, "Naive Bayes Classifier");
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(section);
		section.setExpanded(true);

		// Create a composite to hold the other widgets
		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);
	
		// Creates an empty to create a empty space
		NlputilsFormComposite.createEmptyRow(toolkit, sc);

		// Create a composite that can hold the other widgets
		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1).applyTo(client); // Align the composite section to one column
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(client);		
		GridLayout layout = new GridLayout();// Layout creation
		layout.numColumns = 2;
		
		//Create table layout to hold the input data
		classLayoutData = NlputilsFormComposite.createTableSection(client,toolkit, layout, "Class Details","Add File(s) or Folder(s) which contains data", true);		
		//Create preprocess link
		createPreprocessLink(client);
		createNBClassifierInputParameters(client); // to get k-value
		// Create ouput section
		createOutputSection(client, layout, "Classify", "Choose the input and output path for classification"); // Create dispatchable output section
		// Add run and help button on the toolbar
		addButtonsToToolBar();
	}

	/**
	 * Creates a output section for the Naive Bayes Classifier
	 * 
	 * @param parent
	 *            - Parent composite
	 * @param toolkit
	 * @param layout
	 *            - layout to be applied on the section
	 * @param title
	 *            - title for the section
	 * @param description
	 *            - description to be displayed on the section
	 */
	private void createOutputSection(final Composite parent, GridLayout layout, String title, String description) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(section);
		section.setText(title);
		section.setDescription(description);

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL| SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);

		// Create composite to hold other widgets
		Composite sectionClient = toolkit.createComposite(section);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sectionClient);
		section.setClient(sectionClient);

		// Create an empty row to create space
		NlputilsFormComposite.createEmptyRow(toolkit, sectionClient);
		// Create a row that holds the textbox and browse button
		Label inputPathLabel = toolkit.createLabel(sectionClient, "Input Path:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(inputPathLabel);
		classifyInputText = toolkit.createText(sectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(classifyInputText);
		classifyInputText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				inputPathListener(classifyInputText, "Input path must be a valid diretory location");
			}

			@Override
			public void keyPressed(KeyEvent e) {
				inputPathListener(classifyInputText, "Input path must be a valid diretory location");
			}
		});
		final Button browseBtn1 = toolkit.createButton(sectionClient, "Browse", SWT.PUSH);
		browseBtn1.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String path = openBrowseDialog(browseBtn1);
				if(null == path) return;
				classifyInputText.setText(path);
				form.getMessageManager().removeMessage("classifyInput");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		
		//Output path
		Label outputPathLabel = toolkit.createLabel(sectionClient, "Output Path:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(outputPathLabel);
		classifyOutputText = toolkit.createText(sectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(classifyOutputText);
		classifyOutputText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				outputPathListener(classifyOutputText, "Output path must be a valid diretory location");
			}

			@Override
			public void keyPressed(KeyEvent e) {
				outputPathListener(classifyOutputText, "Output path must be a valid diretory location");
			}
		});
		final Button browseBtn2 = toolkit.createButton(sectionClient, "Browse", SWT.PUSH);
		browseBtn2.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String path = openBrowseDialog(browseBtn2);
				if(null == path) return;
				classifyOutputText.setText(path);
				form.getMessageManager().removeMessage("classifyOutput");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});	
	}

	
	protected String openBrowseDialog(Button browseBtn) {
		DirectoryDialog dlg = new DirectoryDialog(browseBtn.getShell(), SWT.OPEN);
		dlg.setText("Open");
		String path = dlg.open();
		return path;
		
	}

	protected void inputPathListener(Text classifyInputText, String errorMessage) {
		if (classifyInputText.getText().isEmpty()) {
			form.getMessageManager().removeMessage("classifyInput");
			return;
		}
		File tempFile = new File(classifyInputText.getText());
		if (!tempFile.exists() || !tempFile.isDirectory()) {
			form.getMessageManager().addMessage("classifyInput", errorMessage, null, IMessageProvider.ERROR);
		} else {
			form.getMessageManager().removeMessage("classifyInput");
		}
		
	}

	protected void outputPathListener(Text classifyOutputText, String errorMessage) {
		if (classifyOutputText.getText().isEmpty()) {
			form.getMessageManager().removeMessage("classifyOutput");
			return;
		}
		File tempFile = new File(classifyOutputText.getText());
		if (!tempFile.exists() || !tempFile.isDirectory()) {
			form.getMessageManager().addMessage("classifyOutput", errorMessage, null, IMessageProvider.ERROR);
		} else {
			form.getMessageManager().removeMessage("classifyOutput");
		}		
	}

	private void createNBClassifierInputParameters(Composite client) {
		Label kValueLabel;
		Section inputParamsSection = toolkit.createSection(client, Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(inputParamsSection);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(inputParamsSection);
		inputParamsSection.setText("Input Parameters");
		
		ScrolledComposite sc = new ScrolledComposite(inputParamsSection, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);
		
		Composite sectionClient = toolkit.createComposite(inputParamsSection);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(sectionClient);
		inputParamsSection.setClient(sectionClient);
		
		kValueLabel = toolkit.createLabel(sectionClient, "k Value for Cross Validation:",SWT.None);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(kValueLabel);
		kValueText = toolkit.createText(sectionClient, "",SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(kValueText);
		
		kValueText.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
	             if(!(e.character>='0' && e.character<='9')) {
	            	 kValueText.setText(""); 
	             }				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub			
			}
		});
	}

	/**
	 * To create hyperlink
	 * 
	 * @param client
	 */
	private void createPreprocessLink(Composite client) {
		Composite clientLink = toolkit.createComposite(client);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2).applyTo(clientLink);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(clientLink);

		preprocessEnabled = toolkit.createButton(clientLink, "", SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(preprocessEnabled);
		final Hyperlink link = toolkit.createHyperlink(clientLink,"Preprocess", SWT.NONE);
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
				String id = "edu.usc.cssl.nlputils.common.ui.prepocessorsettings";
				PreferencesUtil.createPreferenceDialogOn(link.getShell(), id,new String[] { id }, null).open();
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
				return (NaiveBayesClassifierViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Classify";
			}

			@Override
			public void run() {
				// Classification i/p and o/p paths
				final String classificationOutputDir = classifyOutputText.getText();
				final String classificationInputDir = classifyInputText.getText();
				final ArrayList<String> trainingDataPaths = new ArrayList<String>();
				final int kValue = Integer.parseInt(kValueText.getText());
				
				final HashMap<String, List<String>> classPaths = new HashMap<String, List<String>>();
				consolidateSelectedFiles(classLayoutData, classPaths);
				
				final boolean canItProceed = false;
				final NaiveBayesClassifier nbc = new NaiveBayesClassifier();
				
				NlputilsFormComposite.updateStatusMessage(getViewSite(), null,null);
				Job job = new Job("Classifying...") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {						
						
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								isPreprocessEnabled = preprocessEnabled.getSelection();
								pp_outputPath = CommonUiActivator.getDefault().getPreferenceStore().getString("pp_output_path");
								if (isPreprocessEnabled && pp_outputPath.isEmpty()) {
									form.getMessageManager() .addMessage( "pp_location", "Pre-Processed output location is required for pre-processing", null, IMessageProvider.ERROR);
									return;
								} else {
									form.getMessageManager().removeMessage("pp_location");
								}
							}
						});
						if(isPreprocessEnabled) {
							// check whether the location is specified
							if (pp_outputPath.isEmpty()) {
								return Status.CANCEL_STATUS;
							}
							monitor.subTask("Preprocessing...");
							try {
								preprocessTask = new Preprocess("NB_Classifier");
								for(String dirPath : classPaths.keySet()) {
									List<String> selectedFiles = classPaths.get(dirPath);
									String preprocessedDirPath = preprocessTask.doPreprocessing(selectedFiles, new File(dirPath).getName());
									trainingDataPaths.add(preprocessedDirPath);
								}
							} catch (Exception e) {
								return handleException(monitor, e, "Preprocessing failed. Provide valid data");
							}							
						} else { // consolidate the files into respective classes
							try {
								nbc.createTempDirectories(classPaths, trainingDataPaths);
							} catch (IOException e) {
								return handleException(monitor, e, "Classification failed. Provide valid data");
							}
						}
						try {
							nbc.doCross(trainingDataPaths, classificationOutputDir, false, false, kValue); // perform cross validation
							//nbc.classify(trainingDataPaths, classificationInputDir, classificationOutputDir, false, false);
						} catch (IOException e) {
							e.printStackTrace();
						} catch (EvalError e) {
							e.printStackTrace();
						}
						monitor.done();
						return Status.CANCEL_STATUS;
					}
				};
				job.setUser(true);
				job.schedule(); // schedule the job
			};

		});

		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (NaiveBayesClassifierViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_HELP_CO));
			}

			@Override
			public String getToolTipText() {
				return "Help";
			}

			@Override
			public void run() {
			};
		});
		form.getToolBarManager().update(true);
	}
	
	protected void consolidateSelectedFiles(TableLayoutData classLayoutData, HashMap<String, List<String>> classPaths) {
		Tree tree = classLayoutData.getTree();
		for(int i = 0; i < tree.getItemCount(); i++) {
			TreeItem temp = tree.getItem(i);
			if(temp.getChecked()) {
				classPaths.put(temp.getData().toString(), classLayoutData.getSelectedItems(temp));
			}
		}
		// the final results will nbe in classPaths
	}

	private IStatus handleException(IProgressMonitor monitor, Exception e, String message) {
		monitor.done();
		ConsoleView.writeInConsole(message);
		e.printStackTrace();
		NlputilsFormComposite.updateStatusMessage(getViewSite(), message, IStatus.ERROR);
		return Status.CANCEL_STATUS;
	}


	@Override
	public void setFocus() {
		form.setFocus();
	}

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
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true)
				.applyTo(form.getBody());
		return toolkit;
	}

}
