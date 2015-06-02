package edu.usc.cssl.nlputils.common.ui.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class TargetLocationsGroup {

	private CheckboxTreeViewer fTreeViewer;
	private Button fAddButton;
	private Button fAddFileButton;
	private Button fRemoveButton;
	private List<String> locationPaths;
	@SuppressWarnings("unused")
	private FormToolkit toolKit;

	/**
	 * Creates this part using the form toolkit and adds it to the given
	 * composite.
	 * 
	 * @param parent
	 *            parent composite
	 * @param toolkit
	 *            toolkit to create the widgets with
	 * @param isFolder
	 * @return generated instance of the table part
	 */
	public static TargetLocationsGroup createInForm(Composite parent,
			FormToolkit toolkit, boolean isFolder) {
		TargetLocationsGroup contentTable = new TargetLocationsGroup(toolkit,
				parent);
		contentTable.createFormContents(parent, toolkit, isFolder);
		return contentTable;
	}

	private TargetLocationsGroup(FormToolkit toolKit, Composite parent) {
		this.toolKit = toolKit;
	}

	public CheckboxTreeViewer getTreeViewer() {
		return fTreeViewer;
	}

	private GridLayout createSectionClientGridLayout(
			boolean makeColumnsEqualWidth, int numColumns) {
		GridLayout layout = new GridLayout();

		layout.marginHeight = 0;
		layout.marginWidth = 0;

		layout.marginTop = 2;
		layout.marginBottom = 5;
		layout.marginLeft = 2;
		layout.marginRight = 2;

		layout.horizontalSpacing = 5;
		layout.verticalSpacing = 5;

		layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
		layout.numColumns = numColumns;

		return layout;
	}

	/**
	 * Creates the part contents from a toolkit
	 * 
	 * @param parent
	 *            parent composite
	 * @param toolkit
	 *            form toolkit to create widgets
	 * @param isFolder
	 */
	private void createFormContents(Composite parent, FormToolkit toolkit,
			boolean isFolder) {
		Composite comp = toolkit.createComposite(parent);
		comp.setLayout(createSectionClientGridLayout(false, 2));
		comp.setLayoutData(new GridData(GridData.FILL_BOTH
				| GridData.GRAB_VERTICAL));

		initializeTreeViewer(comp);

		Composite buttonComp = toolkit.createComposite(comp);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.makeColumnsEqualWidth = false;
		buttonComp.setLayout(layout);
		buttonComp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		if (isFolder) {
			fAddButton = toolkit.createButton(buttonComp, "Add Folder...",
					SWT.PUSH);
			GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
					.applyTo(fAddButton);
		}
		fAddFileButton = toolkit.createButton(buttonComp, "Add File...",
				SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(fAddFileButton);
		fRemoveButton = toolkit.createButton(buttonComp, "Remove...", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(fRemoveButton);
		initializeButtons();
		toolkit.paintBordersFor(comp);
	}

	Composite createComposite(Composite parent, Font font, int columns,
			int hspan, int fill) {
		Composite g = new Composite(parent, SWT.NONE);
		g.setLayout(new GridLayout(columns, false));
		g.setFont(font);
		GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
		g.setLayoutData(gd);
		return g;
	}

	/**
	 * Sets up the tree viewer using the given tree
	 * 
	 * @param tree
	 */
	private void initializeTreeViewer(Composite tree) {

		fTreeViewer = new CheckboxTreeViewer(tree, SWT.NONE);
		fTreeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		fTreeViewer.setContentProvider(new TargetLocationContentProvider());
		fTreeViewer.setLabelProvider(new TargetLocationLabelProvider());

		fTreeViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						updateButtons();
					}
				});
		fTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (!event.getSelection().isEmpty()) {
					handleEdit();
				}
			}
		});

		fTreeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				fTreeViewer.setSubtreeChecked(event.getElement(),
						event.getChecked());
			}
		});

	}

	/**
	 * Sets up the buttons, the button fields must already be created before
	 * calling this method
	 */
	private void initializeButtons() {
		if (fAddButton != null) {
			fAddButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					DirectoryDialog dlg = new DirectoryDialog(fAddButton
							.getShell(), SWT.OPEN);
					dlg.setText("Select Directory");
					String path = dlg.open();
					if (path == null)
						return;
					updateLocationTree(path);
				}
			});
		}
		fAddFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(fAddFileButton.getShell(),
						SWT.OPEN);
				dlg.setText("Select File");
				String path = dlg.open();
				if (path == null)
					return;
				updateLocationTree(path);
			}
		});

		fRemoveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleRemove();
			}
		});
		fRemoveButton.setEnabled(true);
		updateButtons();
	}

	protected void updateLocationTree(String path) {

		if (this.locationPaths == null) {
			this.locationPaths = new ArrayList<String>();
		}
		if (!path.equals("root")) {
			this.locationPaths.add(path);
			this.fTreeViewer.setInput(this.locationPaths);
			if (new File(path).isFile())
				this.fTreeViewer.setChecked(path, true);

		}
	}

	private void handleEdit() {

	}

	private void handleRemove() {
		List<String> modifiedList = new ArrayList<String>();
		modifiedList.addAll(this.locationPaths);
		IStructuredSelection sel = (IStructuredSelection) this.fTreeViewer
				.getSelection();
		List<String> removeFiles = sel.toList();
		if (removeFiles.size() > 0) {
			for (String selFile : removeFiles) {
				modifiedList.remove(selFile);
			}
		}
		this.locationPaths = modifiedList;
		// this.fTreeViewer.getTree().removeAll();
		this.fTreeViewer.setInput(modifiedList);
	}

	private void updateButtons() {
		IStructuredSelection sel = (IStructuredSelection) this.fTreeViewer
				.getSelection();
		if (this.locationPaths == null || this.locationPaths.size() < 1) {
			fRemoveButton.setEnabled(false);
			return;
		}
		if (!this.locationPaths.contains(sel.getFirstElement())) {
			fRemoveButton.setEnabled(false);
			return;
		}
		fRemoveButton.setEnabled(true);

	}

	public static int getButtonWidthHint(Button button) {
		PixelConverter converter = new PixelConverter(button);
		int widthHint = converter
				.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint,
				button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}

}