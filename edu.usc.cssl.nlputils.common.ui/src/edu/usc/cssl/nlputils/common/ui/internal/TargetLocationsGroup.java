package edu.usc.cssl.nlputils.common.ui.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.widgets.FormToolkit;

import edu.usc.cssl.nlputils.common.ui.CommonUiActivator;
import edu.usc.cssl.nlputils.common.ui.views.ConsoleView;

public class TargetLocationsGroup {

	private CheckboxTreeViewer fTreeViewer;
	private Button fAddButton;
	private Button fAddFileButton;
	private Button fRemoveButton;
	private List<TreeParent> locationPaths;
	@SuppressWarnings("unused")
	private FormToolkit toolKit;
	private Label dummy;

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
		dummy = toolkit.createLabel(parent, "");
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(dummy);
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

	private void updateSelectionText() {
		int totalFiles = calculateFiles(fTreeViewer.getCheckedElements());
		if (locationPaths.size() > 0)
			dummy.setText("No of files selected : "
					+ String.valueOf(totalFiles));
		else {
			dummy.setText("");
		}
	}

	/**
	 * Sets up the tree viewer using the given tree
	 * 
	 * @param tree
	 */
	private void initializeTreeViewer(Composite tree) {

		fTreeViewer = new CheckboxTreeViewer(tree, SWT.NONE | SWT.MULTI);
		fTreeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		fTreeViewer.setContentProvider(new TargetLocationContentProvider());
		fTreeViewer.setLabelProvider(new TargetLocationLabelProvider());
		if (this.locationPaths == null) {
			this.locationPaths = new ArrayList<TreeParent>();
		}
		this.fTreeViewer.setInput(this.locationPaths);

		fTreeViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						updateButtons();
						updateSelectionText();
					}

				});

		fTreeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				fTreeViewer.setSubtreeChecked(event.getElement(),
						event.getChecked());
				updateSelectionText();
			}
		});

	}

	private int calculateFiles(Object[] objects) {
		int select = 0;
		String fileName = "";
		for (Object file : objects) {
			if (file instanceof String) {
				fileName = (String) file;
			} else {
				fileName = ((TreeParent) file).getName();
			}
			if (new File(fileName).isFile()) {
				select++;
			}
		}
		return select;

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
					dlg.setText("Select Folder");
					String path = null;
					String message = "";
					boolean canExit = false;
					while (!canExit) {
						path = dlg.open();
						if (path == null)
							return;

						message = updateLocationTree(new String[] { path });
						if (!message.equals("")) {
							ErrorDialog.openError(dlg.getParent(),
									"Select Different Folder",
									"Please select different Folder", new Status(
											IStatus.ERROR,
											CommonUiActivator.PLUGIN_ID,
											message));
						} else {
							canExit = true;
						}

					}
				}
			});
		}
		fAddFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(fAddFileButton.getShell(),
						SWT.OPEN | SWT.MULTI);
				dlg.setText("Select File");
				String message = "";
				String path = null;
				boolean canExit = false;
				while (!canExit) {
					path = dlg.open();
					if (path == null)
						return;
					else {
						String[] listFile = dlg.getFileNames();
						String[] fullFile = new String[listFile.length];
						for (int i = 0; i < listFile.length; i++) {
							fullFile[i] = dlg.getFilterPath() + File.separator
									+ listFile[i];
						}

						message = updateLocationTree(fullFile);
						if (!message.equals("")) {
							ErrorDialog.openError(dlg.getParent(),
									"Select Different File",
									"Please select different File", new Status(
											IStatus.ERROR,
											CommonUiActivator.PLUGIN_ID,
											message));
						} else {
							canExit = true;
						}
					}
				}
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

	private String updateLocationTree(String[] path) {

		if (this.locationPaths == null) {
			this.locationPaths = new ArrayList<TreeParent>();
		}
		if (!path.equals("root")) {
			if (checkExistensence(path)) {
				return "The selected File is already added to the location";
			} else {
				String sizeMessage = sizeCheck(path);
				if (!sizeMessage.equals("")) {
					return "The selected file(s) " + sizeMessage + " is empty";
				}

			}
			for (String file : path) {
				TreeParent node = new TreeParent(file);
				if (new File(file).isDirectory()) {
					if(FileUtils.sizeOfDirectory(new File(file)) <= 0){
						return "The selected Folder " + file + " is empty";
					}
					processSubFiles(node);
				}
				this.locationPaths.add(node);
				this.fTreeViewer.refresh();
				this.fTreeViewer.setChecked(node, true);
				fTreeViewer.setSubtreeChecked(node, true);
			}
			// }

		}
		updateSelectionText();
		return "";
	}

	private String sizeCheck(String[] path) {
		String result = "";
		for (String file : path) {
			if (new File(file).isFile()) {
				if (FileUtils.sizeOf(new File(file)) < 1) {
					if(result.equals("")){
						result =  " "+file;
					}else{
					result = result + "," + file;
					}
				}
			}
		}
		return result;
	}

	private boolean checkExistensence(String[] path) {

		for (TreeParent node : locationPaths) {

			for (String file : path) {
				if (file.equals(node.getName())) {
					return true;
				}
			}

		}
		return false;
	}

	private void processSubFiles(TreeParent node) {

		for (File input : new File(node.getName()).listFiles()) {

			if (input.isFile() && FileUtils.sizeOf(input)>0) {
				node.addChildren(input.getAbsolutePath());
			} else if(input.isDirectory()) {
				TreeParent subFolder = new TreeParent(input.getAbsolutePath());
				processSubFiles(subFolder);
				node.addChildren(subFolder);
			}
			else{
				ConsoleView.printlInConsoleln(input.getName() + " is ignored since it is empty ");
			}
		}

	}

	private void handleRemove() {
		TreeItem[] items = fTreeViewer.getTree().getSelection();
		for (TreeItem treeItem : items) {
			this.locationPaths.remove(treeItem.getData());
		}
		fTreeViewer.refresh();
		updateSelectionText();
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