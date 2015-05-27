package edu.usc.cssl.nlputils.classify.naivebayes.ui;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ClassifierDialog extends TitleAreaDialog {

	private Text trainDataPath;
	private Text testDataPath;

	private String trainDatasetPath;
	private String testDatasetPath;

	public ClassifierDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		setTitle("Naive Bayes Classifier");
		setMessage("Select folders which contain the train and test data",
				IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
				.applyTo(container);

		Label dummy = new Label(container, SWT.NONE); // create empty space
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(dummy);

		// To get the training dataset path
		Label trainDataLabel = new Label(container, SWT.NONE);
		trainDataLabel.setText("Training Data Class Path:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(trainDataLabel);

		trainDataPath = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
				.applyTo(trainDataPath); // allows the column to grow
											// horizontally
		Button trainBrowseBtn = new Button(container, SWT.PUSH);
		trainBrowseBtn.setText("Browse");

		trainBrowseBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(trainBrowseBtn
						.getShell(), SWT.OPEN);
				dlg.setText("Open");
				String path = dlg.open();
				if (path == null)
					return;
				trainDataPath.setText(path);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}
		});

		// To get the testing dataset path
		Label testDataLabel = new Label(container, SWT.NONE);
		testDataLabel.setText("Testing Data Class Path:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(testDataLabel);

		testDataPath = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
				.applyTo(testDataPath); // allows the column to grow
										// horizontally
		Button testBrowseBtn = new Button(container, SWT.PUSH);
		testBrowseBtn.setText("Browse");

		testBrowseBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(testBrowseBtn
						.getShell(), SWT.OPEN);
				dlg.setText("Open");
				String path = dlg.open();
				if (path == null)
					return;
				testDataPath.setText(path);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}
		});

		return area;
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	// save content of the Text fields because they get disposed
	// as soon as the Dialog closes
	private void saveInput() {
		trainDatasetPath = trainDataPath.getText();
		testDatasetPath = testDataPath.getText();
	}

	public String getTrainDataPath() {
		return trainDatasetPath;
	}

	public String getTestDataPath() {
		return testDatasetPath;
	}

}
