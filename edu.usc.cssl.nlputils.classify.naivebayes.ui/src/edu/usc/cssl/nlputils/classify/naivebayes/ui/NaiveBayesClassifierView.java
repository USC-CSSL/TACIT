package edu.usc.cssl.nlputils.classify.naivebayes.ui;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
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

import edu.usc.cssl.nlputils.classify.naivebayes.ui.internal.INaiveBayesClassifierViewConstants;
import edu.usc.cssl.nlputils.classify.naivebayes.ui.internal.NaiveBayesClassifierViewImageRegistry;
import edu.usc.cssl.nlputils.common.ui.composite.from.NlputilsFormComposite;

public class NaiveBayesClassifierView extends ViewPart implements
		INaiveBayesClassifierViewConstants {
	public static String ID = "edu.usc.cssl.nlputils.classify.naivebayes.ui.naivebayesview";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private int classPathCount = 0;

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
				"Add folders which contains training and test data");

		// Create dispatchable output section
		createOutputSection(client, toolkit, layout, "Classify",
				"Choose the input and output path for classification");

		// Add run and help button on the toolbar
		addButtonsToToolBar();
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
		createBrowseButton(toolkit, sectionClient, "Input Path:", "Browse");
		createBrowseButton(toolkit, sectionClient, "Output Path:", "Browse");

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

				Job job = new Job("Classifying...") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("NLPUtils started classifying...",
								100);

						int i = 0;
						while (i < 1000000000) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							if (monitor.isCanceled()) {
								throw new OperationCanceledException();

							}
							i++;
							monitor.worked(1);
						}
						monitor.done();
						return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				job.schedule();

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

	private void createBrowseButton(FormToolkit toolkit, Composite parent,
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

		final Tree trainingClassPathTree = new Tree(client, SWT.NONE);
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

		final Tree testingClassPathTree = new Tree(client, SWT.NONE);
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

		Button button = new Button(buttonComposite, SWT.PUSH);
		button.setText("Add class path");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(button);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = parent.getShell();
				ClassifierDialog cDialog = new ClassifierDialog(shell);
				cDialog.create();
				// cDialog.open(); // To open the created dialog
				if (cDialog.open() == Window.OK) {
					// System.out.println(cDialog.getTrainDataPath());
					// System.out.println(cDialog.getTestDataPath());
					File file = null;
					classPathCount++;
					TreeItem trainingSubItem = new TreeItem(trainingItem,
							SWT.NULL);
					trainingSubItem.setText("Class " + classPathCount
							+ " Path: " + cDialog.getTrainDataPath());
					trainingSubItem.setData(cDialog.getTrainDataPath());
					// helps for lazy expansion
					file = new File(cDialog.getTrainDataPath());
					if (file.isDirectory()) {
						new TreeItem(trainingSubItem, SWT.NULL);
					}

					TreeItem testingSubItem = new TreeItem(testingItem,
							SWT.NONE);
					testingSubItem.setText("Class " + classPathCount
							+ " Path: " + cDialog.getTestDataPath());
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

		Button remove = new Button(buttonComposite, SWT.PUSH);
		remove.setText("Remove class path");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(remove);
		section.setClient(client);
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
