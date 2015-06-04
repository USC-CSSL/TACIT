package edu.usc.cssl.nlputils.classify.naivebayes.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

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
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
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
import edu.usc.nlputils.common.Preprocess;

public class NaiveBayesClassifierView extends ViewPart implements
		INaiveBayesClassifierViewConstants {
	public static String ID = "edu.usc.cssl.nlputils.classify.naivebayes.ui.naivebayesview";
	private ScrolledForm form;
	private FormToolkit toolkit;

	// Classification parameters
	private Text classifyInputText;
	private Text classifyOutputText;
	private Text testOutputPath;
	private Button preprocessEnabled;

	private Preprocess preprocessTask;
	private boolean isPreprocessEnabled = false;
	private String pp_outputPath = null;

	// Training and Testing data class paths
	Tree trainingClassPathTree;
	Tree testingClassPathTree;
	boolean isAnyValidationFailed = false;

	@Override
	public void createPartControl(Composite parent) {
		// Creates toolkit and form
		toolkit = createFormBodySection(parent, "Naive Bayes Classifier");

		// Create a section in the form and apply the required layout options
		Section section = toolkit.createSection(form.getBody(),
				Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1)
				.applyTo(section);
		section.setExpanded(true);

		// Create a composite to hold the other widgets
		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL
				| SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false)
				.applyTo(sc);

		// Creates an empty to create a empty space
		NlputilsFormComposite.createEmptyRow(toolkit, sc);

		// Create a composite that can hold the other widgets
		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1)
				.applyTo(client); // Align the composite section to one column
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client);

		// Layout creation
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;

		// Create dispatchable input section
		createInputSection(client, toolkit, layout, "Input Details",
				"Add folders which contains training and testing data");

		createPreprocessLink(client);

		// Create testing section
		createTestSection(client, toolkit, layout, "Test",
				"Choose the output path for testing");

		// Create dispatchable output section
		createOutputSection(client, toolkit, layout, "Classify",
				"Choose the input and output path for classification");

		// Add run and help button on the toolbar
		addButtonsToToolBar();
	}

	/**
	 * 
	 * @param client
	 */
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

	/**
	 * Creates a test section for Naive Bayes Classifier
	 * 
	 * @param parent
	 *            - Parent composite
	 * @param toolkit
	 * @param layout
	 *            - The layout to be applied on the section
	 * @param title
	 *            - Title for the section
	 * @param description
	 *            - Description for the section.
	 * 
	 */
	private void createTestSection(final Composite parent, FormToolkit toolkit,
			GridLayout layout, String title, String description) {
		// Create output section
		Section section = toolkit.createSection(parent, Section.TITLE_BAR
				| Section.EXPANDED | Section.DESCRIPTION);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(section);
		section.setText(title);
		section.setDescription(description);

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL
				| SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
				.applyTo(sc);

		// Create composite to hold other widgets
		Composite sectionClient = toolkit.createComposite(section);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
				.applyTo(sectionClient);
		section.setClient(sectionClient);

		// Create an empty row to create space
		NlputilsFormComposite.createEmptyRow(toolkit, sectionClient);
		// Create a row that holds the textbox and browse button
		testOutputPath = createBrowseButton(toolkit, sectionClient,
				"Output Path", "Browse");

		// Create test button
		Label temp1 = toolkit.createLabel(sectionClient, "", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(temp1);
		Label temp2 = toolkit.createLabel(sectionClient, "", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(temp2);
		Button testButton = new Button(sectionClient, SWT.PUSH);
		testButton.setText("Test Classes");
		testButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Set of training data class paths
				final ArrayList<String> trainingDataPaths = new ArrayList<String>();
				final ArrayList<String> tempTrainingDataPaths = new ArrayList<String>();
				TreeItem trainingDataset = trainingClassPathTree.getItem(0);
				for (TreeItem ti : trainingDataset.getItems()) {
					tempTrainingDataPaths.add(ti.getData().toString());
				}

				// Set of testing data class paths
				final ArrayList<String> tempTestingDataPaths = new ArrayList<String>();
				final ArrayList<String> testingDataPaths = new ArrayList<String>();
				TreeItem testingDataset = testingClassPathTree.getItem(0);
				for (TreeItem ti : testingDataset.getItems()) {
					tempTestingDataPaths.add(ti.getData().toString());
				}

				final String testTrainOutputPath = testOutputPath.getText();
				Job job = new Job("Testing...") {
					private boolean isPreprocessEnabled = false;
					private String pp_outputPath = null;

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						NaiveBayesClassifier nbc = new NaiveBayesClassifier();
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								isPreprocessEnabled = preprocessEnabled
										.getSelection();
								pp_outputPath = CommonUiActivator.getDefault()
										.getPreferenceStore()
										.getString("pp_output_path");
								if (pp_outputPath.isEmpty()) {
									form.getMessageManager()
											.addMessage(
													"pp_location",
													"Pre-Processed output location is required for pre-processing",
													null,
													IMessageProvider.ERROR);
								} else {
									form.getMessageManager().removeMessage(
											"pp_location");
								}

								if (!isPreprocessEnabled) {
									form.getMessageManager().removeMessage(
											"pp_location"); // just in case if
															// there was
															// error earlier
								}

							}
						});

						if (isPreprocessEnabled) {
							// check whether the location is specified
							if (pp_outputPath.isEmpty()) {
								return Status.CANCEL_STATUS;
							}
							monitor.subTask("Preprocessing...");
							try {
								preprocessTask = new Preprocess(
										"NB_Classifier_Training_Data");
								// Preprocess training data
								for (String dir : tempTrainingDataPaths) {
									String preprocessedDirPath = preprocessDirectory(dir);
									// dirPath, Directory path of the
									// preprocessed files
									trainingDataPaths.add(preprocessedDirPath);
								}
								preprocessTask = new Preprocess(
										"NB_Classifier_Testing_Data");
								// Preprocess testing data
								for (String dir : tempTestingDataPaths) {
									String preprocessedDirPath = preprocessDirectory(dir);
									testingDataPaths.add(preprocessedDirPath);
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							for (String path : tempTrainingDataPaths) {
								trainingDataPaths.add(path);
							}
							for (String path : tempTestingDataPaths) {
								testingDataPaths.add(path);
							}
						}

						long startTime = System.currentTimeMillis();
						// Train and test the dataset
						try {
							monitor.subTask("Testing the model...");
							nbc.train_Test(trainingDataPaths, testingDataPaths,
									testTrainOutputPath, false, false);
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (EvalError e1) {
							e1.printStackTrace();
						}

						if (monitor.isCanceled()) {
							return Status.CANCEL_STATUS;
						}

						System.out.println("Done!");
						monitor.done();
						System.out
								.println("Naive Bayes classifier testing completed successfully in "
										+ (System.currentTimeMillis() - startTime)
										+ " milliseconds.");
						NlputilsFormComposite.updateStatusMessage(
								getViewSite(),
								"Testing completed successfully!", IStatus.OK);

						return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				job.schedule(); // schedule the job
			};
		});
		// Create an empty row to create space
		NlputilsFormComposite.createEmptyRow(toolkit, sectionClient);
	}

	private String preprocessDirectory(String dirPath) throws IOException {
		// preprocessTask = new Preprocess(dirName);
		Queue<String> dirs = new LinkedList<String>();
		dirs.add(dirPath);

		if (!dirs.isEmpty()) {
			String tempDirPath = dirs.poll();
			File[] files = new File(tempDirPath).listFiles();
			ArrayList<String> fileList = new ArrayList<String>();
			for (File f : files) {
				if (f.isDirectory()) {
					dirs.add(f.getAbsolutePath());
				} else {
					fileList.add(f.getAbsolutePath());
				}
			}
			if (!fileList.isEmpty()) {
				String preprocessedDirPath = preprocessTask.doPreprocessing(
						fileList, new File(dirPath).getName());
				preprocessSubfolders(dirs, new File(dirPath).getName());
				return preprocessedDirPath;
			}
		}
		return dirPath;
	}

	private void preprocessSubfolders(Queue<String> subfolders,
			String classDirPath) throws IOException {
		while (!subfolders.isEmpty()) {
			String tempDirPath = subfolders.poll();
			File[] files = new File(tempDirPath).listFiles();
			ArrayList<String> fileList = new ArrayList<String>();
			for (File f : files) {
				if (f.isDirectory()) {
					subfolders.add(f.getAbsolutePath());
				} else {
					fileList.add(f.getAbsolutePath());
				}
			}
			if (!fileList.isEmpty()) {
				String subfolderName = tempDirPath
						.substring(tempDirPath.indexOf(classDirPath),
								tempDirPath.length());
				preprocessTask.doPreprocessing(fileList, subfolderName);
			}
		}
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
	private void createOutputSection(final Composite parent,
			FormToolkit toolkit, GridLayout layout, String title,
			String description) {
		// Create output section
		Section section = toolkit.createSection(parent, Section.TITLE_BAR
				| Section.EXPANDED | Section.DESCRIPTION);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(section);
		section.setText(title);
		section.setDescription(description);

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL
				| SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
				.applyTo(sc);

		// Create composite to hold other widgets
		Composite sectionClient = toolkit.createComposite(section);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
				.applyTo(sectionClient);
		section.setClient(sectionClient);

		// Create an empty row to create space
		NlputilsFormComposite.createEmptyRow(toolkit, sectionClient);
		// Create a row that holds the textbox and browse button
		classifyInputText = createBrowseButton(toolkit, sectionClient,
				"Input Path", "Browse");
		classifyOutputText = createBrowseButton(toolkit, sectionClient,
				"Output Path", "Browse");

	}

	/**
	 * Adds "Classify" and "Help" buttons on the Naive Bayes Classifier form
	 */
	private void addButtonsToToolBar() {
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (NaiveBayesClassifierViewImageRegistry
						.getImageIconFactory()
						.getImageDescriptor(IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Classify";
			}

			public void run() {
				// Set of inputs that needs to be passed
				// Set of training data class paths
				final ArrayList<String> trainingDataPaths = new ArrayList<String>();
				final ArrayList<String> tempTrainingDataPaths = new ArrayList<String>();

				TreeItem trainingDataset = trainingClassPathTree.getItem(0);
				for (TreeItem ti : trainingDataset.getItems()) {
					tempTrainingDataPaths.add(ti.getData().toString());
				}

				Job job = new Job("Classifying...") {

					// Classification i/p and o/p paths
					final String classificationOutputDir = classifyOutputText
							.getText();
					private String classificationInputDir = classifyInputText
							.getText();
					private boolean canItProceed = false;

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						NaiveBayesClassifier nbc = new NaiveBayesClassifier();
						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								isPreprocessEnabled = preprocessEnabled
										.getSelection();
								pp_outputPath = CommonUiActivator.getDefault()
										.getPreferenceStore()
										.getString("pp_output_path");
								if (isPreprocessEnabled
										&& pp_outputPath.isEmpty()) {
									form.getMessageManager()
											.addMessage(
													"pp_location",
													"Pre-Processed output location is required for pre-processing",
													null,
													IMessageProvider.ERROR);
									return;
								} else {
									form.getMessageManager().removeMessage(
											"pp_location");
								}
								canItProceed = canItProceedClassification(
										trainingDataPaths,
										classificationInputDir,
										classificationOutputDir);
							}
						});
						if (isPreprocessEnabled) {
							if (pp_outputPath.isEmpty()) {
								return Status.CANCEL_STATUS;
							}

							monitor.subTask("Preprocessing...");
							try {
								// Preprocess training data
								preprocessTask = new Preprocess(
										"NB_Classifier_Training_Data");
								for (String dir : tempTrainingDataPaths) {
									String preprocessedDirPath = preprocessDirectory(dir);
									// dirPath, Directory path of the
									// preprocessed files
									trainingDataPaths.add(preprocessedDirPath);
								}
								// Preprocess the input
								preprocessTask = new Preprocess(
										"NB_Classifier_Input_Data");
								classificationInputDir = preprocessDirectory(classificationInputDir);
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							for (String path : tempTrainingDataPaths) {
								trainingDataPaths.add(path);
							}
						}

						if (canItProceed) {
							long startTime = System.currentTimeMillis();
							monitor.subTask("Classifying the input data...");
							try {
								// Classify the data
								nbc.classify(trainingDataPaths,
										classificationInputDir,
										classificationOutputDir, false, false);
							} catch (FileNotFoundException e1) {
								e1.printStackTrace();
							} catch (IOException e1) {
								e1.printStackTrace();
							} catch (EvalError e1) {
								e1.printStackTrace();
							}

							if (monitor.isCanceled()) {
								return Status.CANCEL_STATUS;
							}
							System.out.println("Done!");
							monitor.done();
							System.out
									.println("Naive Bayes classifier completed successfully in "
											+ (System.currentTimeMillis() - startTime)
											+ " milliseconds.");
							NlputilsFormComposite.updateStatusMessage(
									getViewSite(),
									"Classification completed successfully!",
									IStatus.OK);
							return Status.OK_STATUS;
						}
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
				return (NaiveBayesClassifierViewImageRegistry
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

	private boolean canItProceedClassification(
			ArrayList<String> trainingDataPaths, String classificationInputDir,
			String classificationOutputDir) {
		// Validate Training data paths, Classification input and output
		if (trainingDataPaths.size() == 0) {
			form.getMessageManager().addMessage("trainingClasses",
					"Training classes cannot be empty", null,
					IMessageProvider.ERROR);
			return false;
		} else if (classificationInputDir.isEmpty()
				|| !isDirectoryValid(classificationInputDir)) {
			form.getMessageManager().addMessage("classifyInputDir",
					"Classification input directory must be a valid location",
					null, IMessageProvider.ERROR);
			return false;
		} else if (classificationOutputDir.isEmpty()
				|| !isDirectoryValid(classificationOutputDir)) {
			form.getMessageManager().addMessage("classifyInputDir",
					"Classification output directory must be a valid location",
					null, IMessageProvider.ERROR);
			return false;
		} else {
			form.getMessageManager().removeMessages();
			return true;
		}
	}

	private void validateTextbox(Text textBox, String label) {
		if (textBox.getText().isEmpty()) {
			form.getMessageManager().removeMessage(label);
			return;
		}
		File tempFile = new File(textBox.getText());
		if (!tempFile.exists() || !tempFile.isDirectory()) {
			form.getMessageManager().addMessage(label,
					label + " must be a valid diretory location", null,
					IMessageProvider.ERROR);
		} else {
			form.getMessageManager().removeMessage(label);
		}
	}

	private boolean isDirectoryValid(String path) {
		if (path.isEmpty())
			return false;
		File tempFile = new File(path);
		if (!tempFile.exists() || !tempFile.isDirectory())
			return false;
		else
			return true;
	}

	/**
	 * 
	 * @param toolkit
	 * @param parent
	 *            - Parent Composite
	 * @param labelString
	 *            - String for the label
	 * @param buttonString
	 *            - String value for the button
	 * @return - Creates a row with a label, text and button to browse the files
	 */
	private Text createBrowseButton(FormToolkit toolkit, Composite parent,
			final String labelString, String buttonString) {
		Label outputPathLbl = toolkit
				.createLabel(parent, labelString, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(outputPathLbl);
		final Text outputLocationTxt = toolkit.createText(parent, "",
				SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
				.applyTo(outputLocationTxt);

		outputLocationTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				validateTextbox(outputLocationTxt, labelString);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				validateTextbox(outputLocationTxt, labelString);
			}
		});
		final Button browseBtn = toolkit.createButton(parent, buttonString,
				SWT.PUSH);
		browseBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(browseBtn.getShell(),
						SWT.OPEN);
				dlg.setText("Open");
				String path = dlg.open();
				if (path == null)
					return;
				outputLocationTxt.setText(path);
				form.getMessageManager().removeMessage(labelString);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		return outputLocationTxt;
	}

	/**
	 * Creates a input section for Naive Bayes Classifier
	 * 
	 * @param parent
	 *            - Parent composite
	 * @param toolkit
	 * @param layout
	 *            - The layout to be applied on the section
	 * @param title
	 *            - Title for the section
	 * @param description
	 *            - Description for the section.
	 */
	private void createInputSection(final Composite parent,
			FormToolkit toolkit, GridLayout layout, String title,
			String description) {
		// Create a section to hold the table
		Section section = toolkit.createSection(parent, Section.TITLE_BAR
				| Section.EXPANDED | Section.DESCRIPTION);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(section);
		section.setText(title);
		section.setDescription(description);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		section.setLayoutData(gd);

		// Create a composite to hold the widgets
		Composite client = toolkit.createComposite(section, SWT.NONE);
		client.setLayout(layout);
		NlputilsFormComposite.createEmptyRow(toolkit, client);

		trainingClassPathTree = new Tree(client, SWT.MULTI);
		GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 0);
		gd_tree.heightHint = 100;
		trainingClassPathTree.setLayoutData(gd_tree);
		final TreeItem trainingItem = new TreeItem(trainingClassPathTree,
				SWT.NULL);
		trainingItem.setText("Train");
		trainingItem.setData("Train");
		trainingClassPathTree.addListener(SWT.Expand, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TreeItem root = (TreeItem) event.item;
				File file = new File(root.getData().toString());
				File[] files = file.listFiles();
				if (files == null) {
					return;
				} else { // remove the dummy item added,
					// TODO: check whether this way of doing things is right
					// with Vijayan
					root.getItems()[0].dispose();
				}
				for (int i = 0; i < files.length; i++) {
					TreeItem item = new TreeItem(root, SWT.NULL);
					item.setText(files[i].getName());
					item.setData(files[i]);
					item.setImage(NaiveBayesClassifierViewImageRegistry
							.getImageIconFactory().getImage(IMAGE_FILE_OBJ));
					/* Use a dummy item to force the '+' */
					if (files[i].isDirectory()) {
						item.setImage(NaiveBayesClassifierViewImageRegistry
								.getImageIconFactory().getImage(
										IMAGE_FOLDER_OBJ));
						new TreeItem(item, SWT.NULL);
					}
				}
			}
		});

		testingClassPathTree = new Tree(client, SWT.MULTI);
		testingClassPathTree.setLayoutData(gd_tree);
		final TreeItem testingItem = new TreeItem(testingClassPathTree,
				SWT.NULL);
		testingItem.setText("Test");
		testingItem.setData("Test");
		testingClassPathTree.addListener(SWT.Expand, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TreeItem root = (TreeItem) event.item;
				File file = new File(root.getData().toString());
				File[] files = file.listFiles();
				if (files == null) {
					return;
				} else { // remove the dummy item added,
					// TODO: check whether this way of doing things is right
					// with Vijayan
					root.getItems()[0].dispose();
				}

				for (int i = 0; i < files.length; i++) {
					TreeItem item = new TreeItem(root, SWT.NULL);
					item.setText(files[i].getName());
					item.setData(files[i]);
					item.setImage(NaiveBayesClassifierViewImageRegistry
							.getImageIconFactory().getImage(IMAGE_FILE_OBJ));
					/* Use a dummy item to force the '+' */
					if (files[i].isDirectory()) {
						item.setImage(NaiveBayesClassifierViewImageRegistry
								.getImageIconFactory().getImage(
										IMAGE_FOLDER_OBJ));
						new TreeItem(item, SWT.NULL);
					}
				}
			}
		});

		Composite buttonComposite = new Composite(client, SWT.NONE);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.marginWidth = buttonLayout.marginHeight = 0;
		buttonLayout.makeColumnsEqualWidth = true;
		buttonComposite.setLayout(buttonLayout);
		buttonComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		Button addClassButton = new Button(buttonComposite, SWT.PUSH);
		addClassButton.setText("Add class path");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(addClassButton);
		addClassButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = parent.getShell();
				ClassifierDialog cDialog = new ClassifierDialog(shell);
				cDialog.create();
				if (cDialog.open() == Window.OK) {
					File file = null;
					TreeItem trainingSubItem = new TreeItem(trainingItem,
							SWT.NULL);
					trainingSubItem.setText("Class" + " : "
							+ cDialog.getTrainDataPath());
					trainingSubItem.setData(cDialog.getTrainDataPath());
					trainingSubItem
							.setImage(NaiveBayesClassifierViewImageRegistry
									.getImageIconFactory().getImage(
											IMAGE_FILE_OBJ));
					// helps for lazy expansion
					file = new File(cDialog.getTrainDataPath());
					if (file.isDirectory()) {
						trainingSubItem
								.setImage(NaiveBayesClassifierViewImageRegistry
										.getImageIconFactory().getImage(
												IMAGE_FOLDER_OBJ));
						new TreeItem(trainingSubItem, SWT.NULL);
					}

					TreeItem testingSubItem = new TreeItem(testingItem,
							SWT.NONE);
					testingSubItem.setText("Class" + " : "
							+ cDialog.getTestDataPath());
					testingSubItem.setData(cDialog.getTestDataPath());
					testingSubItem
							.setImage(NaiveBayesClassifierViewImageRegistry
									.getImageIconFactory().getImage(
											IMAGE_FILE_OBJ));
					file = new File(cDialog.getTestDataPath());
					if (file.isDirectory()) {
						testingSubItem
								.setImage(NaiveBayesClassifierViewImageRegistry
										.getImageIconFactory().getImage(
												IMAGE_FOLDER_OBJ));
						new TreeItem(testingSubItem, SWT.NULL);
					}
					trainingClassPathTree.getItems()[0].setExpanded(true);
					testingClassPathTree.getItems()[0].setExpanded(true);
				}
			}
		});

		Button removeClassButton = new Button(buttonComposite, SWT.PUSH);
		removeClassButton.setText("Remove class path");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(removeClassButton);
		removeClassButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] tempSelected = trainingClassPathTree.getSelection();
				for (TreeItem ti : tempSelected) {
					int index = getIndex(ti);
					if (-1 != index) {
						testingClassPathTree.getItems()[0].getItem(index)
								.dispose();
						ti.dispose();
					}
				}
			}
		});
		section.setClient(client);
	}

	/**
	 * Gets the index of the given item from "Training class tree"
	 * 
	 * @param ti
	 * @return
	 */
	private int getIndex(TreeItem ti) {
		int count = 0;
		for (TreeItem tempItem : trainingClassPathTree.getItems()[0].getItems()) {
			if (tempItem.getData().equals(ti.getData())) {
				return count;
			}
			count++;
		}
		return -1; // not possible at all
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
