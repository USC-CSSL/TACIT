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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
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
import cc.mallet.types.MatrixOps;
import edu.usc.cssl.nlputils.classify.naivebayes.services.CrossValidator;
import edu.usc.cssl.nlputils.classify.naivebayes.services.NaiveBayesClassifier;
import edu.usc.cssl.nlputils.classify.naivebayes.ui.internal.INaiveBayesClassifierViewConstants;
import edu.usc.cssl.nlputils.classify.naivebayes.ui.internal.NaiveBayesClassifierViewImageRegistry;
import edu.usc.cssl.nlputils.common.ui.CommonUiActivator;
import edu.usc.cssl.nlputils.common.ui.composite.from.NlputilsFormComposite;
import edu.usc.cssl.nlputils.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.nlputils.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.nlputils.common.ui.validation.OutputPathValidation;
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
	private boolean isClassificationEnabled = false;
	boolean canProceed = false;
	
	private Button classificationEnabled;

	HashMap<String, List<String>> classPaths;

	private OutputLayoutData outputLayout;
	
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
		//createOutputSection(client, layout, "Classify", "Choose the input and output path for classification"); // Create dispatchable output section
		outputLayout = NlputilsFormComposite.createOutputSection(toolkit, client, form.getMessageManager());
		// Add run and help button on the toolbar
		addButtonsToToolBar();
		
		client.addListener(SWT.FOCUSED, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				System.out.println("Focussed");
				consolidateSelectedFiles(classLayoutData, classPaths);
				canItProceed(classPaths);
			}
		});
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
				inputPathListener(classifyInputText, "Classification Input path must be a valid diretory location");
			}

			@Override
			public void keyPressed(KeyEvent e) {
				inputPathListener(classifyInputText, "Classification Input path must be a valid diretory location");
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
				outputPathListener(classifyOutputText, "Classification Output path must be a valid diretory location");
			}

			@Override
			public void keyPressed(KeyEvent e) {
				outputPathListener(classifyOutputText, "Classification Output path must be a valid diretory location");
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
		if(classificationEnabled.getSelection()) {
			if (classifyInputText.getText().isEmpty()) {
				form.getMessageManager().addMessage("classifyInput", errorMessage, null, IMessageProvider.ERROR);
				return;
			}
			File tempFile = new File(classifyInputText.getText());
			if (!tempFile.exists() || !tempFile.isDirectory()) {
				form.getMessageManager().addMessage("classifyInput", errorMessage, null, IMessageProvider.ERROR);
			} else {
				form.getMessageManager().removeMessage("classifyInput");
				String message = validateInputDirectory(classifyInputText.getText().toString());
				if(null != message) {
					form.getMessageManager().addMessage("classifyInput", message, null, IMessageProvider.ERROR);
				}
			}		
		} else {
			form.getMessageManager().removeMessage("classifyInput");
		}
	}
	
	public String validateInputDirectory(String location) {
		File locationFile = new File(location);
		if (locationFile.canRead()) {
			return null;
		} else {
			return "Classification Input Path : Permission Denied";
		}
	}
	

	public String validateOutputDirectory(String location) {
		File locationFile = new File(location);
		if (locationFile.canWrite()) {
			return null;
		} else {
			return "Classification Output Path : Permission Denied";
		}
	}
	
	protected void outputPathListener(Text classifyOutputText, String errorMessage) {
		if(classificationEnabled.getSelection()) {
			if (classifyOutputText.getText().isEmpty()) {
				form.getMessageManager().addMessage("classifyOutput", errorMessage, null, IMessageProvider.ERROR);
				return;
			}
			File tempFile = new File(classifyOutputText.getText());
			if (!tempFile.exists() || !tempFile.isDirectory()) {
				form.getMessageManager().addMessage("classifyOutput", errorMessage, null, IMessageProvider.ERROR);
			} else {
				form.getMessageManager().removeMessage("classifyOutput");
				String message = validateOutputDirectory(classifyOutputText.getText().toString());
				if(null != message) {
					form.getMessageManager().addMessage("classifyOutput", message, null, IMessageProvider.ERROR);
				}			
			}
		}
		else {
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
	            	 form.getMessageManager() .addMessage( "kvalue", "Provide valid K-Value for cross validation", null, IMessageProvider.ERROR);
	            	 kValueText.setText(""); 
	             } else {
	            	 form.getMessageManager().removeMessage("kvalue");
	             }
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub			
			}
		});
		
		NlputilsFormComposite.createEmptyRow(toolkit, client);
		createClassificationParameters(client);
		
	}

	private void createClassificationParameters(Composite client) {
		Group group = new Group(client, SWT.SHADOW_IN);
		group.setText("Classification");

		group.setBackground(client.getBackground());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);

		classificationEnabled = toolkit.createButton(group, "Classify Data", SWT.CHECK);
		classificationEnabled.setBounds(10, 10, 10, 10);
		classificationEnabled.pack();
			
		NlputilsFormComposite.createEmptyRow(toolkit, group);
		
		/*
		ScrolledComposite sc = new ScrolledComposite(group, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);
		sc.pack();
		sc.setEnabled(false);*/
		
		final Composite sectionClient = toolkit.createComposite(group);
		GridDataFactory.fillDefaults().grab(true, false).span(1,1).applyTo(sectionClient);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sectionClient);
		sectionClient.setEnabled(false);
		sectionClient.pack();
		
	
		// Create a row that holds the textbox and browse button
		final Label inputPathLabel = toolkit.createLabel(sectionClient, "Input Path:", SWT.NONE);
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
		inputPathLabel.setEnabled(false);
		classifyInputText.setEnabled(false);
		browseBtn1.setEnabled(false);
		
		//Output path
		final Label outputPathLabel = toolkit.createLabel(sectionClient, "Output Path:", SWT.NONE);
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
		outputPathLabel.setEnabled(false);
		classifyOutputText.setEnabled(false);
		browseBtn2.setEnabled(false);
		
		classificationEnabled.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (classificationEnabled.getSelection()) {					
					sectionClient.setEnabled(true);
					
					inputPathLabel.setEnabled(true);
					classifyInputText.setEnabled(true);
					browseBtn1.setEnabled(true);
					
					inputPathLabel.setEnabled(true);
					outputPathLabel.setEnabled(true);
					classifyOutputText.setEnabled(true);
					browseBtn2.setEnabled(true);
					
					inputPathListener(classifyInputText, "Input path must be a valid diretory location");
					outputPathListener(classifyOutputText, "Output path must be a valid diretory location");
					
				} else {					
					sectionClient.setEnabled(false);
					
					inputPathLabel.setEnabled(false);
					classifyInputText.setEnabled(false);
					browseBtn1.setEnabled(false);
					
					outputPathLabel.setEnabled(false);
					classifyOutputText.setEnabled(false);
					browseBtn2.setEnabled(false);
					
					form.getMessageManager().removeMessage("classifyInput");
					form.getMessageManager().removeMessage("classifyOutput");
				}
			}
		}); 
		
		NlputilsFormComposite.createEmptyRow(toolkit, group);
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

			String classificationInputDir;
			String outputDir;
			@Override
			public void run() {
				// Classification i/p and o/p paths
				final String classificationOutputDir = classifyOutputText.getText();
				classificationInputDir = classifyInputText.getText();
				final ArrayList<String> trainingDataPaths = new ArrayList<String>();
				final String tempkValue = kValueText.getText();
				
				classPaths = new HashMap<String, List<String>>();
				consolidateSelectedFiles(classLayoutData, classPaths);
				final HashMap<String, List<String>> tempClassPaths = new HashMap<String, List<String>>();
				final NaiveBayesClassifier nbc = new NaiveBayesClassifier();
				final CrossValidator cv = new CrossValidator();				
				outputDir = outputLayout.getOutputLabel().getText();
				
				NlputilsFormComposite.updateStatusMessage(getViewSite(), null,null);
				final Job job = new Job("Naive Bayes Classification") {
					
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("Running Naive Bayes Classification..." , 100);
						
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								isPreprocessEnabled = preprocessEnabled.getSelection();
								isClassificationEnabled = classificationEnabled.getSelection();
							}
						});
						
						HashMap<Integer, String> perf;
						if(monitor.isCanceled()) 
							handledCancelRequest("Cancelled");
						
						int kValue = Integer.parseInt(tempkValue);
						monitor.worked(1); // done with the validation
						if(isPreprocessEnabled) {							
							monitor.subTask("Preprocessing...");
							try {
								preprocessTask = new Preprocess("NB_Classifier");
								for(String dirPath : classPaths.keySet()) {
									if(monitor.isCanceled()) {
										handledCancelRequest("Cancelled");
									}
									
									List<String> selectedFiles = classPaths.get(dirPath);
									String preprocessedDirPath = preprocessTask.doPreprocessing(selectedFiles, new File(dirPath).getName());
									trainingDataPaths.add(preprocessedDirPath);									
									List<String> temp = new ArrayList<String>();
									for(File f: new File(preprocessedDirPath).listFiles()) {
										temp.add(f.getAbsolutePath());
									}
									tempClassPaths.put(preprocessedDirPath, temp);
									monitor.worked(1); // for the pre-processing of each directory
									if(monitor.isCanceled())
										handledCancelRequest("Cancelled");
								}
								
								if(monitor.isCanceled())
									handledCancelRequest("Cancelled");								
								// preprocess the inputDir if required
								if(isClassificationEnabled) {
									ArrayList<String> files = new ArrayList<String>(); 
									nbc.selectAllFiles(classificationInputDir, files);
									classificationInputDir = preprocessTask.doPreprocessing(files, new File(classificationInputDir).getName());
									monitor.worked(1); 
								}
								if(monitor.isCanceled()) 
									handledCancelRequest("Cancelled");
							} catch (Exception e) {
								return handleException(monitor, e, "Preprocessing failed. Provide valid data");
							}
							monitor.worked(10);
						} else { // consolidate the files into respective classes
							if(monitor.isCanceled()) 
								handledCancelRequest("Cancelled");
							try {
								nbc.createTempDirectories(classPaths, trainingDataPaths, monitor);
							} catch (IOException e) {
								return handleException(monitor, e, "Naive Bayes Classifier failed. Provide valid data");
							}
							monitor.worked(10); // after creating temp directories
							if(monitor.isCanceled()) 
								handledCancelRequest("Cancelled");	
						}
						try {
							if(monitor.isCanceled()) 
								handledCancelRequest("Cancelled");
							monitor.subTask("Cross validating...");
							perf = (!isPreprocessEnabled) ? cv.doCross(nbc, classPaths, kValue, monitor, outputDir) :  cv.doCross(nbc, tempClassPaths, kValue, monitor, outputDir);
							monitor.worked(40);
							if(monitor.isCanceled()) 
								handledCancelRequest("Cancelled");
							//nbc.doCross(trainingDataPaths, classificationOutputDir, false, false, kValue); // perform cross validation
							if(isClassificationEnabled) {
								monitor.subTask("Classifying...");
								ConsoleView.printlInConsoleln("---------- Classification Starts ------------");
								nbc.classify(trainingDataPaths, classificationInputDir, classificationOutputDir, false, false);
								ConsoleView.printlInConsoleln("---------- Classification Finished ------------");
							}
							monitor.worked(15);
							if(monitor.isCanceled())
								handledCancelRequest("Cancelled");							
							double avgAccuracy = 0.0;
							double accuracies[] = new double[kValue];
							ConsoleView.printlInConsoleln("------Cross Validation Results------");
							if(null != perf) {
								for(Integer trialNum : perf.keySet()) {
									ConsoleView.printlInConsoleln("Fold "+ trialNum + " Results");
									if(null != perf.get(trialNum)) {
										String[] results = perf.get(trialNum).split("=");
										for(int i = 0; i<results.length; i++) {
											if(results[i].contains("test accuracy")) {
												avgAccuracy+=Double.parseDouble(results[i+1].split(" ")[1]);
												accuracies[i] = Double.parseDouble(results[i+1].split(" ")[1]);
											}		
										}
										ConsoleView.printlInConsoleln(perf.get(trialNum));
										ConsoleView.printlInConsoleln();
									}
								}
							}
							monitor.worked(10);
							ConsoleView.printlInConsoleln("Average test accuracy = "+ avgAccuracy/kValue);
							ConsoleView.printlInConsoleln("Standard Deviation = "+ MatrixOps.stddev(accuracies));
							ConsoleView.printlInConsoleln("Standard Error = "+ MatrixOps.stderr(accuracies));
							
							if(monitor.isCanceled())
								handledCancelRequest("Cancelled");
							if(!isPreprocessEnabled) 
								nbc.deleteTempDirectories(trainingDataPaths);
							monitor.worked(10);
							if(monitor.isCanceled())
								handledCancelRequest("Cancelled");
						} catch (IOException e) {
							return handleException(monitor, e, "Naive Bayes Classifier failed. Provide valid data");
						} catch (EvalError e) {
							return handleException(monitor, e, "Naive Bayes Classifier failed. Provide valid data");
						}
						if(monitor.isCanceled())
							handledCancelRequest("Cancelled");
						monitor.worked(100);
						monitor.done();
						NlputilsFormComposite.updateStatusMessage(getViewSite(), " Naive Bayes Classifier Completed Successfully!", IStatus.INFO);
						return Status.OK_STATUS;						
					}
					@Override
					protected void canceling() {
						//done(Status.CANCEL_STATUS);						
					}
					
				};
				job.setUser(true);
				canProceed = canItProceed(classPaths);
				if(canProceed) {
					job.schedule(); // schedule the job
				}
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
	
	private IStatus handledCancelRequest(String message) {
		NlputilsFormComposite.updateStatusMessage(getViewSite(), message, IStatus.ERROR);
		return Status.CANCEL_STATUS;
		
	}
	
	
	private boolean canItProceed(HashMap<String, List<String>> classPaths) {
	
		// Class paths 
		if(classPaths.size() <2 && classLayoutData.getTree().getItemCount() < 2) {
			form.getMessageManager().addMessage("classes", "Provide atleast 2 valid class paths", null, IMessageProvider.ERROR);
			return false;
		} else if(classLayoutData.getTree().getItemCount() > 1 && classPaths.size() < 2){
			form.getMessageManager().addMessage("classes", "Select the required classes", null, IMessageProvider.ERROR);
			return false;
		} else {
			form.getMessageManager().removeMessage("classes");
		}
		
		// Preprocessing
		isPreprocessEnabled = preprocessEnabled.getSelection();
		String tempPPOutputPath = CommonUiActivator.getDefault().getPreferenceStore().getString("pp_output_path");
		if (isPreprocessEnabled && tempPPOutputPath.isEmpty()) {
			form.getMessageManager() .addMessage("pp_location", "Pre-Processed output location is required for pre-processing", null, IMessageProvider.ERROR);
			return false;
		} else {
			form.getMessageManager().removeMessage("pp_location");
		}
		
		// K-Vlaue
		if(kValueText.getText().isEmpty()) {
			form.getMessageManager() .addMessage( "kvalue", "Provide valid K-Value for cross validation", null, IMessageProvider.ERROR);
			return false;
		} else {
			form.getMessageManager().removeMessage("kvalue");
		}
		
		String message = OutputPathValidation.getInstance().validateOutputDirectory(outputLayout.getOutputLabel().getText(), "Output");
		if (message != null) {
			message = outputLayout.getOutputLabel().getText() + " " + message;
			form.getMessageManager().addMessage("output", message, null,IMessageProvider.ERROR);
			return false;
		} else {
			form.getMessageManager().removeMessage("output");
		}
		
		// Classification parameters
		isClassificationEnabled = classificationEnabled.getSelection();
		if(isClassificationEnabled) {
			inputPathListener(classifyInputText, "Classifciation Input path must be a valid diretory location");
			outputPathListener(classifyOutputText, "Classifciation Output path must be a valid diretory location");
		} else {
			form.getMessageManager().removeMessage("classifyInputText");
			form.getMessageManager().removeMessage("classifyOutputText");
		}
		
		return true;
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
		System.out.println(message);
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
