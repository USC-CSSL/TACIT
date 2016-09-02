package edu.usc.cssl.tacit.classify.naivebayes.ui;

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

import bsh.EvalError;
import cc.mallet.types.MatrixOps;
import edu.usc.cssl.tacit.classify.naivebayes.services.NaiveBayesClassifier;
import edu.usc.cssl.tacit.classify.naivebayes.ui.internal.INaiveBayesClassifierViewConstants;
import edu.usc.cssl.tacit.classify.naivebayes.ui.internal.NaiveBayesClassifierViewImageRegistry;
import edu.usc.cssl.tacit.classify.naivebayes.weka.NaiveBayesClassifierWeka;
import edu.usc.cssl.tacit.common.Preprocessor;
import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.internal.TargetLocationsGroup;
import edu.usc.cssl.tacit.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

/**
 * Naive Bayes Classifier View
 */
public class NaiveBayesClassifierView extends ViewPart implements INaiveBayesClassifierViewConstants {
	public static String ID = "edu.usc.cssl.tacit.classify.naivebayes.ui.naivebayesview";

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
		form.setImage(NaiveBayesClassifierViewImageRegistry.getImageIconFactory().getImage(INaiveBayesClassifierViewConstants.IMAGE_NAIVE_BAYES_CLASSIFIER_OBJ));

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
				return (NaiveBayesClassifierViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Classify";
			}

			String classificationInputDir;

			String outputDir;
			String tempkValue;
			List<Object> selectedFiles;

			@Override
			public void run() {
				final ArrayList<String> trainingDataPaths = new ArrayList<String>();
				final HashMap<String, List<String>> tempClassPaths = new HashMap<String, List<String>>();
				final NaiveBayesClassifier nbc = new NaiveBayesClassifier();

				classPaths = new HashMap<String, List<String>>();
				consolidateSelectedFiles(classLayoutData, classPaths);

				TacitFormComposite.writeConsoleHeaderBegining("Naive Bayes Classification started ");

				TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
				job = new Job("Naive Bayes Classification") {

					@Override
					protected IStatus run(final IProgressMonitor monitor) {
						TacitFormComposite.setConsoleViewInFocus();
						TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
						monitor.beginTask("Running Naive Bayes Classification...", 100);

						Date dateObj = new Date();
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								// Classification i/p and o/p paths
								outputDir = outputPath.getText();
								classificationInputDir = classifyInputText.getText();
								tempkValue = kValueText.getText();
								isPreprocessEnabled = preprocessEnabled.getSelection();
								if(isPreprocessEnabled)
									checkType = false;	//Because file type check is already being performed in the PreprocessorView class 
								
								isClassificationEnabled = classificationEnabled.getSelection();
								try{
									selectedFiles = classLayoutData.getTypeCheckedSelectedFiles(checkType);
								}
								catch(Exception e){
									if(e.getMessage().equals("User has requested cancel"))
										breakFlag = true;
									else handleException(monitor, e, "Some error occurred");
								}

							}
						});

						if(breakFlag){
							return Status.CANCEL_STATUS;
						}
						
						HashMap<Integer, String> perf;
						if (monitor.isCanceled()) {
							TacitFormComposite.writeConsoleHeaderBegining("<terminated> Naive Bayes Classifier ");
							return handledCancelRequest("Cancelled");
						}
						NaiveBayesClassifierWeka cv;
						int kValue = Integer.parseInt(tempkValue);
						monitor.worked(1); // done with the validation

						if (isPreprocessEnabled) {
							// New Pre-processing!
							monitor.subTask("Preprocessing...");
							try {
								preprocessTask = new Preprocessor("NB_Classifier", isPreprocessEnabled);
								for (String dirPath : classPaths.keySet()) {
									if (monitor.isCanceled()) {
										TacitFormComposite
												.writeConsoleHeaderBegining("<terminated> Naive Bayes Classifier ");
										return handledCancelRequest("Cancelled");
									}
									List<String> allSelectedClassFiles = classPaths.get(dirPath);
									List<Object> tempFiles = new ArrayList<Object>();
									tempFiles.addAll(allSelectedClassFiles);

									List<String> preprocessedFilePaths = preprocessTask
											.processData(new File(dirPath).getName(), tempFiles);
									
									String preProcessedClassDir = null;

									if (!preprocessedFilePaths.isEmpty())
										preProcessedClassDir = new File(preprocessedFilePaths.get(0)).getParent();
									trainingDataPaths.add(preProcessedClassDir);
									tempClassPaths.put(preProcessedClassDir, preprocessedFilePaths);
									monitor.worked(1);
								}

								if (monitor.isCanceled()) {
									TacitFormComposite
											.writeConsoleHeaderBegining("<terminated> Naive Bayes Classifier ");
									return handledCancelRequest("Cancelled");
								}

								if (isClassificationEnabled) {
									List<Object> files = new ArrayList<Object>();
									nbc.selectAllFiles(classificationInputDir, files);
									List<String> preprocessedFilePaths = preprocessTask
											.processData(new File(classificationInputDir).getName(), files);

									if (!preprocessedFilePaths.isEmpty())
										classificationInputDir = new File(preprocessedFilePaths.get(0)).getParent();
									monitor.worked(1);
								}
							} catch(Exception e){
								if (e.getMessage().equals("User has requested cancel"))
									return Status.CANCEL_STATUS;
								else
									return handleException(monitor, e, "Preprocessing failed. Provide valid data");
							}
							monitor.worked(10);

						} else {
							try {
								preprocessTask = null;
								Preprocessor ppObj = null;
								try {
									ppObj = new Preprocessor("Hierarchical_Clustering", true);
									List<String> inFiles;
									// classPaths.clear();

									for (Object i : selectedFiles) {
										if (i instanceof CorpusClass) {
											List<Object> tempSelection = new ArrayList<Object>();
											tempSelection.add(i);
											inFiles = ppObj.processData("Hierarchical_Clustering", tempSelection);
											
											CorpusClass j = (CorpusClass) i;
											classPaths.put(j.getClassName(), inFiles);
											if (classPaths.containsKey(((CorpusClass) i).getParent().getCorpusName())) {
												classPaths.remove(((CorpusClass) i).getParent().getCorpusName());
											}
										}
									}
								} catch (IOException e) {
									e.printStackTrace();
									return Status.CANCEL_STATUS;
								} catch (Exception e) {
									e.printStackTrace();
									return Status.CANCEL_STATUS;
								}
								// classPaths
								// export data here
								nbc.createTempDirectories(classPaths, trainingDataPaths, monitor);
							} catch (IOException e) {
								return handleException(monitor, e, "Naive Bayes Classifier failed. Provide valid data");
							}
							monitor.worked(10);
							if (monitor.isCanceled()) {
								TacitFormComposite.writeConsoleHeaderBegining("<terminated> Naive Bayes Classifier ");
								return handledCancelRequest("Cancelled");
							}
						}
						try {
							if (monitor.isCanceled()) {
								TacitFormComposite.writeConsoleHeaderBegining("<terminated> Naive Bayes Classifier ");
								return handledCancelRequest("Cancelled");
							}
							monitor.subTask("Cross validating...");
							if (!isPreprocessEnabled) {
								cv = new NaiveBayesClassifierWeka(classPaths);
							} else {
								cv = new NaiveBayesClassifierWeka(tempClassPaths);
							}

							try {
								cv.initializeInstances();
								cv.doCrossValidate(kValue, monitor, dateObj);
							} catch (IllegalArgumentException iae) {
								TacitFormComposite
										.writeConsoleHeaderBegining("Error: <Terminated> Naive Bayes Classifier ");
								return handleException(monitor, iae,
										"Naive Bayes Classifier failed. Provide valid data.");
							}
							monitor.worked(40);

							if (monitor.isCanceled()) {
								TacitFormComposite.writeConsoleHeaderBegining("<terminated> Naive Bayes Classifier ");
								return handledCancelRequest("Cancelled");
							}

							// perform cross validation
							if (isClassificationEnabled) {
								monitor.subTask("Classifying...");
								try {
									cv.doClassify(classificationInputDir, outputDir, monitor, dateObj);
								} catch (IllegalArgumentException iae) {
									TacitFormComposite
											.writeConsoleHeaderBegining("Error: <Terminated> Naive Bayes Classifier ");
									return handleException(monitor, iae,
											"Naive Bayes Classifier failed. Provide valid data.");
								}
							}
							monitor.worked(15);
							if (monitor.isCanceled()) {
								TacitFormComposite.writeConsoleHeaderBegining("<terminated> Naive Bayes Classifier ");
								return handledCancelRequest("Cancelled");
							}

							if (monitor.isCanceled()) {
								TacitFormComposite.writeConsoleHeaderBegining("<terminated> Naive Bayes Classifier ");
								return handledCancelRequest("Cancelled");
							}
							if (!isPreprocessEnabled)
								nbc.deleteTempDirectories(trainingDataPaths);
							monitor.worked(10);
							if (monitor.isCanceled()) {
								TacitFormComposite.writeConsoleHeaderBegining("<terminated> Naive Bayes Classifier ");
								return handledCancelRequest("Cancelled");
							}
						} catch (IOException e) {
							TacitFormComposite
									.writeConsoleHeaderBegining("Error: <Terminated> Naive Bayes Classifier ");
							return handleException(monitor, e, "Naive Bayes Classifier failed. Provide valid data");
						} catch (EvalError e) {
							TacitFormComposite
									.writeConsoleHeaderBegining("Error: <Terminated> Naive Bayes Classifier ");
							return handleException(monitor, e, "Naive Bayes Classifier failed. Provide valid data");

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (monitor.isCanceled()) {
							TacitFormComposite.writeConsoleHeaderBegining("<terminated> Naive Bayes Classifier ");
							return handledCancelRequest("Cancelled");
						}
						if (isPreprocessEnabled) {
							preprocessTask.clean();
						}
						monitor.worked(100);
						monitor.done();

						return Status.OK_STATUS;
					}

				};
				job.setUser(true);
				canProceed = canItProceed(classPaths);
				if (canProceed) {
					job.schedule(); // schedule the job
					job.addJobChangeListener(new JobChangeAdapter() {

						public void done(IJobChangeEvent event) {
							if (!event.getResult().isOK()) {
								TacitFormComposite
										.writeConsoleHeaderBegining("Error: <Terminated> Naive Bayes Classifier");
							} else {
								TacitFormComposite.updateStatusMessage(getViewSite(),
										"Naive Bayes classification completed", IStatus.OK, form);
								ConsoleView.printlInConsoleln("Naive Bayes classifier completed successfully.");

								TacitFormComposite
										.writeConsoleHeaderBegining("Success: <Completed> Naive Bayes Classification ");
							}
						}
					});
				}
			};

		});

		Action helpAction = new Action() {
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
	public static void createNBOutputReport(String location, String title, Date dateObj, HashMap<Integer, String> perf,
			int kValue, IProgressMonitor monitor) {
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
		File readme = new File(location + System.getProperty("file.separator") + title.replace(" ", "-")
				+ "-output-report-" + df.format(dateObj) + ".txt");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(readme));
			Date date = new Date();
			bw.write(title + " Output");
			bw.newLine();
			bw.write("--------------------------");
			bw.newLine();
			bw.newLine();
			bw.write("Date: " + date.toString());
			bw.newLine();
			bw.newLine();
			bw.write("Cross Validation Results");
			bw.newLine();
			bw.write("--------------------------");
			bw.newLine();

			double avgAccuracy = 0.0;
			double totalPvalue = 0.0;
			double accuracies[] = new double[kValue];
			ConsoleView.printlInConsoleln("------Cross Validation Results------");
			if (null != perf) {
				for (Integer trialNum : perf.keySet()) {
					bw.write("Fold " + trialNum + " Results");
					bw.newLine();
					ConsoleView.printlInConsoleln("Fold " + trialNum + " Results");
					if (null != perf.get(trialNum)) {
						String[] results = perf.get(trialNum).split("=");
						for (int i = 0; i < results.length; i++) {
							if (results[i].contains("test accuracy")) {
								avgAccuracy += Double.parseDouble(results[i + 1].split(" ")[1]);
								accuracies[i] = Double.parseDouble(results[i + 1].split(" ")[1]);
							} else if (results[i].contains("Pvalue")) {
								totalPvalue += Double.parseDouble(results[i + 1].split(" ")[1]);
							}
						}
						bw.write(perf.get(trialNum));
						bw.newLine();
						bw.newLine();
						ConsoleView.printlInConsoleln(perf.get(trialNum));
						ConsoleView.printlInConsoleln();
					}
				}
			}
			monitor.worked(10);
			bw.newLine();
			bw.write("Average test accuracy = " + avgAccuracy / kValue);
			bw.newLine();
			ConsoleView.printlInConsoleln("Average test accuracy = " + avgAccuracy / kValue);
			bw.write("Average Binomial 2-Sided Pvalue = " + totalPvalue / kValue);
			bw.newLine();
			ConsoleView.printlInConsoleln("Average Binomial 2-Sided Pvalue = " + totalPvalue / kValue);
			bw.write("Standard Deviation = " + MatrixOps.stddev(accuracies));
			bw.newLine();
			ConsoleView.printlInConsoleln("Standard Deviation = " + MatrixOps.stddev(accuracies));
			bw.write("Standard Error = " + MatrixOps.stderr(accuracies));
			bw.newLine();
			ConsoleView.printlInConsoleln("Standard Error = " + MatrixOps.stderr(accuracies));
			bw.close();
			ConsoleView.printlInConsoleln("Finished creating output report file :" + readme.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true).applyTo(form.getBody());
		return toolkit;
	}

}
