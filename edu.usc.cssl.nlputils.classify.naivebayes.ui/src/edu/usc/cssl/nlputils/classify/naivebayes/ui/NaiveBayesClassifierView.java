package edu.usc.cssl.nlputils.classify.naivebayes.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import bsh.EvalError;
import edu.usc.cssl.nlputils.classify.naivebayes.services.NaiveBayesClassifier;
import edu.usc.cssl.nlputils.classify.naivebayes.ui.internal.INaiveBayesClassifierViewConstants;
import edu.usc.cssl.nlputils.classify.naivebayes.ui.internal.NaiveBayesClassifierViewImageRegistry;
import edu.usc.cssl.nlputils.common.ui.composite.from.NlputilsFormComposite;

public class NaiveBayesClassifierView extends ViewPart implements
		INaiveBayesClassifierViewConstants {
	public static String ID = "edu.usc.cssl.nlputils.classify.naivebayes.ui.naivebayesview";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private int classPathCount = 0;

	// Classification parameters
	private Text classifyInputText;
	private Text classifyOutputText;
	private Text testOutputPath;

	// Training and Testing data class paths
	Tree trainingClassPathTree;
	Tree testingClassPathTree;

	@Override
	public void createPartControl(Composite parent) {
		classPathCount = 0;
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

		// Create testing section
		createTestSection(client, toolkit, layout, "Test",
				"Choose the output path for testing");

		// Create dispatchable output section
		createOutputSection(client, toolkit, layout, "Classify",
				"Choose the input and output path for classification");

		// Add run and help button on the toolbar
		addButtonsToToolBar();
	}

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
				"Output Path:", "Browse");

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
				ArrayList<String> trainingDataPaths = new ArrayList<String>();
				TreeItem trainingDataset = trainingClassPathTree.getItem(0);
				for (TreeItem ti : trainingDataset.getItems()) {
					trainingDataPaths.add(ti.getData().toString());
				}

				// Set of testing data class paths
				ArrayList<String> testingDataPaths = new ArrayList<String>();
				TreeItem testingDataset = testingClassPathTree.getItem(0);
				for (TreeItem ti : testingDataset.getItems()) {
					testingDataPaths.add(ti.getData().toString());
				}

				String testTrainOutputPath = testOutputPath.getText();
				Job job = new Job("Testing...") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						if (monitor.isCanceled()) {
							return Status.CANCEL_STATUS;
						}
						NaiveBayesClassifier nbc = new NaiveBayesClassifier();
						// Train and test the dataset
						try {
							nbc.train_Test(trainingDataPaths, testingDataPaths,
									testTrainOutputPath, false, false);
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (EvalError e1) {
							e1.printStackTrace();
						}
						System.out.println("Done!");
						monitor.done();
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
				"Input Path:", "Browse");
		classifyOutputText = createBrowseButton(toolkit, sectionClient,
				"Output Path:", "Browse");

	}

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
				ArrayList<String> trainingDataPaths = new ArrayList<String>();
				TreeItem trainingDataset = trainingClassPathTree.getItem(0);
				for (TreeItem ti : trainingDataset.getItems()) {
					trainingDataPaths.add(ti.getData().toString());
				}

				// Classification i/p and o/p paths
				String classificationInputDir = classifyInputText.getText();
				String classificationOutputDir = classifyOutputText.getText();

				Job job = new Job("Classifying...") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						// monitor.beginTask("NLPUtils started classifying...",100);
						if (monitor.isCanceled()) {
							return Status.CANCEL_STATUS;
						}

						NaiveBayesClassifier nbc = new NaiveBayesClassifier();
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
						System.out.println("Done!");
						monitor.done();
						return Status.OK_STATUS;
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

	private Text createBrowseButton(FormToolkit toolkit, Composite parent,
			String labelString, String buttonString) {
		Label outputPathLbl = toolkit
				.createLabel(parent, labelString, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(outputPathLbl);
		Text outputLocationTxt = toolkit.createText(parent, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
				.applyTo(outputLocationTxt);
		Button browseBtn = toolkit.createButton(parent, buttonString, SWT.PUSH);
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
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		return outputLocationTxt;
	}

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
		TreeItem trainingItem = new TreeItem(trainingClassPathTree, SWT.NULL);
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
					/* Use a dummy item to force the '+' */
					if (files[i].isDirectory()) {
						new TreeItem(item, SWT.NULL);
					}
				}
			}
		});

		testingClassPathTree = new Tree(client, SWT.MULTI);
		testingClassPathTree.setLayoutData(gd_tree);
		TreeItem testingItem = new TreeItem(testingClassPathTree, SWT.NULL);
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
					/* Use a dummy item to force the '+' */
					if (files[i].isDirectory()) {
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
					classPathCount++;
					TreeItem trainingSubItem = new TreeItem(trainingItem,
							SWT.NULL);
					trainingSubItem.setText("Class" + " : "
							+ cDialog.getTrainDataPath());
					trainingSubItem.setData(cDialog.getTrainDataPath());
					// helps for lazy expansion
					file = new File(cDialog.getTrainDataPath());
					if (file.isDirectory()) {
						new TreeItem(trainingSubItem, SWT.NULL);
					}

					TreeItem testingSubItem = new TreeItem(testingItem,
							SWT.NONE);
					testingSubItem.setText("Class" + " : "
							+ cDialog.getTestDataPath());
					testingSubItem.setData(cDialog.getTestDataPath());
					file = new File(cDialog.getTestDataPath());
					if (file.isDirectory()) {
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
