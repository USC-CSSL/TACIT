package edu.usc.cssl.tacit.common.ui.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.widgets.FormToolkit;

import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.common.ui.TacitElementSelectionDialog;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.Corpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class TargetLocationsGroup {

	private CheckboxTreeViewer fTreeViewer;
	private Button fAddButton;
	private Button fAddFileButton;
	private Button fAddCorpusButton;
	private Button fRemoveButton;
	private Set<TreeParent> locationPaths;
	@SuppressWarnings("unused")
	private FormToolkit toolKit;
	private Label dummy;
	private Label dummyCorpus;
	private ManageCorpora corporaManagement;
	private TreeParent node;

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
			FormToolkit toolkit, boolean isFolder, boolean isFile,
			boolean isCorpus) {
		TargetLocationsGroup contentTable = new TargetLocationsGroup(toolkit,
				parent);
		contentTable.createFormContents(parent, toolkit, isFolder, isFile,
				isCorpus);
		return contentTable;
	}

	private TargetLocationsGroup(FormToolkit toolKit, Composite parent) {
		this.toolKit = toolKit;
		corporaManagement = new ManageCorpora();
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
			boolean isFolder, boolean isFile, boolean isCorpus) {
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
		if (isFile) {
			fAddFileButton = toolkit.createButton(buttonComp, "Add File(s)...",
					SWT.PUSH);
			GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
					.applyTo(fAddFileButton);
		}
		if (isCorpus) {
			fAddCorpusButton = toolkit.createButton(buttonComp,
					"Add Corpus...", SWT.PUSH);
			GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
					.applyTo(fAddCorpusButton);
		}
		fRemoveButton = toolkit.createButton(buttonComp, "Remove...", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(fRemoveButton);
		initializeButtons();
		dummy = toolkit.createLabel(parent, "");
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(dummy);
		if(isCorpus){
		dummyCorpus = toolkit.createLabel(parent, "");
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(dummyCorpus);
		}
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
		if (totalFiles > 0)
			dummy.setText("No. of files selected : "
					+ String.valueOf(totalFiles));
		else {
			dummy.setText("");
		}
		if(dummyCorpus != null){
			int totalCorpusClass = calculateCorpus(fTreeViewer.getCheckedElements());
			if (totalCorpusClass > 0)
				dummyCorpus.setText("No. of corpus class selected : "
						+ String.valueOf(totalCorpusClass));
			else {
				dummyCorpus.setText("");
			}
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
			this.locationPaths = new LinkedHashSet<TreeParent>();
		}
		this.fTreeViewer.setInput(this.locationPaths);

		fTreeViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						updateButtons();
						updateSelectionText();
					}

				});

		fTreeViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
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
				if(file instanceof TreeParent){
					if(((TreeParent) file).getCorpusClass()!= null){
						continue;
					}
					else{
						
						fileName = ((TreeParent) file).getName();
					}
					
				}
			}
			if (new File(fileName).isFile()) {
				select++;
			}
		}
		return select;

	}
	
	private int calculateCorpus(Object[] objects) {
		int select = 0;
		for (Object file : objects) {
			
				if(file instanceof TreeParent){
					if(((TreeParent) file).getCorpusClass()!= null){
						select++;
						}
					
					
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
									"Please select different Folder",
									new Status(IStatus.ERROR,
											CommonUiActivator.PLUGIN_ID,
											message));
						} else {
							canExit = true;
						}

					}
				}
			});
		}

		if (fAddCorpusButton != null) {
			fAddCorpusButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final TacitElementSelectionDialog CorpusDialog = new TacitElementSelectionDialog(
							fAddCorpusButton.getShell());
					CorpusDialog.setTitle("Select the Corpus from the list");
					CorpusDialog.setMessage("Enter Corpus name to search");
					final SortedSet<ICorpus> allCorpus = new TreeSet<ICorpus>();
//					Job getCorpus = new Job("Retrieving corpus list ...") {
//						@Override
//						protected IStatus run(IProgressMonitor monitor) {
//							allCorpus.clear();
//							try {
//								List<ICorpus> corpusList = corporaManagement
//										.getAllCorpusDetails();
//								allCorpus.addAll(corpusList);
//								CorpusDialog.setElements(allCorpus.toArray());
//								Display.getDefault().syncExec(new Runnable() {
//									@Override
//									public void run() {
//										CorpusDialog.refresh(allCorpus
//												.toArray());
//										
//									}
//								});
//							} catch (final Exception ex) {
//							}
//							return Status.OK_STATUS;
//						}
//					};
//					getCorpus.schedule();
					List<ICorpus> corpusList = corporaManagement
							.getAllCorpusDetails();
				//	allCorpus.addAll(corpusList);
					CorpusDialog.setElements(corpusList.toArray());
					CorpusDialog.setMultipleSelection(true);

					if (CorpusDialog.open() == Window.OK) {

						updateLocationTree((Object[]) CorpusDialog
								.getSelectionObjects().toArray(
										new Object[CorpusDialog
												.getSelectionObjects().size()]));
					}
				}
			});
		}

		if (fAddFileButton != null) {
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
								fullFile[i] = dlg.getFilterPath()
										+ File.separator + listFile[i];
							}

							message = updateLocationTree(fullFile);
							if (!message.equals("")) {
								ErrorDialog.openError(dlg.getParent(),
										"Select Different File",
										"Please select different File",
										new Status(IStatus.ERROR,
												CommonUiActivator.PLUGIN_ID,
												message));
							} else {
								canExit = true;
							}
						}
					}
				}
			});
		}

		fRemoveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleRemove();
			}
		});
		fRemoveButton.setEnabled(true);
		updateButtons();
	}

	public String updateLocationTree(Object[] path) {
		if (this.locationPaths == null) {
			this.locationPaths = new LinkedHashSet<TreeParent>();
		}
		if (!path.equals("root")) {
			for (Object file : path) {
				node = null;
				boolean isCorpus = false;
				if (file instanceof ICorpus) {
					if (checkExisting((ICorpus) file)) {
						continue;
					}
					node = new TreeParent((Corpus) file);
					processCorpusFiles(node);
				} else {
					if (checkExisting((String) file)) {
						continue;
					}
					File fileHandler = new File((String) file); // length
																// returns the
																// size in
					Long availalbeJVMSpace = Runtime.getRuntime().freeMemory();

					if (fileHandler.exists()
							&& fileHandler.length() > availalbeJVMSpace)
						return "No memory available to upload the file/folder";

					if (((String) file).contains(".DS_Store"))
						continue;
					node = new TreeParent((String) file);
					if (isCorpus) {
						processCorpusFiles(node);
					} else if (new File((String) file).isDirectory()) {
						if (FileUtils.sizeOfDirectory(new File((String) file)) <= 0) {
							return "The selected Folder "
									+ file
									+ " is empty . Hence, it is not added to the list";
						}
						processSubFiles(node);
					} else {
						if (!sizeCheck(new String[] { (String) file }).equals(
								"")) {
							ConsoleView
									.printlInConsoleln("File "
											+ file
											+ " is empty. Hence, it is not added to the list");
							continue;
						}
					}
				}
				if (node != null)
					this.locationPaths.add(node);
				this.fTreeViewer.refresh();
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						fTreeViewer.setChecked(node, true);
						fTreeViewer.setSubtreeChecked(node, true);

					}
				});
			}
			// }

		}
		updateSelectionText();
		return "";
	}

	private boolean checkExisting(ICorpus file) {
		for (TreeParent node : locationPaths) {
			if(node.getCorpus() != null && node.getCorpus().getCorpusId().equals(file.getCorpusId())){
				return true;
			}
		}
		return false;
	}

	private String sizeCheck(String[] path) {
		String result = "";
		for (String file : path) {
			if (new File(file).isFile()) {
				if (FileUtils.sizeOf(new File(file)) < 1) {
					if (result.equals("")) {
						result = " " + file;
					} else {
						result = result + "," + file;
					}
				}
			}
		}
		return result;
	}

	private boolean checkExisting(String newNode) {
		for (TreeParent node : locationPaths) {
			if (newNode.equals(node.getName())) {
				return true;
			}
		}
		return false;
	}

	private void processCorpusFiles(TreeParent corpusNode) {
		ICorpus myCorpus = corpusNode.getCorpus();
		List<ICorpusClass> myCorpusClasses = myCorpus.getClasses();
		for (ICorpusClass class_ : myCorpusClasses) {
			TreeParent classFolder = new TreeParent(class_);
			corpusNode.addChildren(classFolder);
		}
	}

	private void processSubFiles(TreeParent node) {
		File[] files = new File(node.getName()).listFiles();
		if (null == files || files.length == 0)
			return;

		for (File input : files) {
			if (input.getAbsolutePath().contains(".DS_Store")
					|| input.getAbsolutePath().contains("$RECYCLE.BIN"))
				continue;
			if (input.isFile() && FileUtils.sizeOf(input) > 0) {
				node.addChildren(input.getAbsolutePath());
			} else if (input.isDirectory() && null != input.listFiles()
					&& input.listFiles().length > 0) {
				TreeParent subFolder = new TreeParent(input.getAbsolutePath());
				processSubFiles(subFolder);
				node.addChildren(subFolder);
			} else {
				ConsoleView.printlInConsoleln("File " + input.getAbsolutePath()
						+ " is empty. Hence, it is not added to the list");
				continue;
			}
		}

	}

	private void handleRemove() {
		TreeItem[] items = fTreeViewer.getTree().getSelection();
		boolean result = MessageDialog
				.openConfirm(fRemoveButton.getShell(), "Remove",
						"Remove will remove the object from Tree. Do you still want to Continue?");
		if (!result) {
			return;
		}
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