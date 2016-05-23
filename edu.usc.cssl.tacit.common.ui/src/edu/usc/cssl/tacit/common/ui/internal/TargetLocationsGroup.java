package edu.usc.cssl.tacit.common.ui.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import edu.usc.cssl.tacit.common.queryprocess.IQueryProcessor;
import edu.usc.cssl.tacit.common.queryprocess.QueryDataType;
import edu.usc.cssl.tacit.common.queryprocess.QueryProcesser;
import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.common.ui.TacitCorpusFilterDialog;
import edu.usc.cssl.tacit.common.ui.TacitElementSelectionDialog;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CMDataType;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.Corpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class TargetLocationsGroup {

	private CheckboxTreeViewer fTreeViewer;
	private Button fAddButton;
	private Button fAddFileButton;
	private Button fAddCorpusButton;
	private Button fRemoveButton;
	private Button fFilterCorpusButton;
	private Set<TreeParent> locationPaths;
	@SuppressWarnings("unused")	
	private FormToolkit toolKit;
	private Label dummy;
	private Label dummyCorpus;
	private ManageCorpora corporaManagement;
	private TreeParent node;
	private Button fAddCorpusClassButton;
	private static Composite cmp;
	
	private static String inputFilterPath = ""; 
	public static long FILE_CHECK_THRESHOLD = 20000;

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
			boolean isCorpus, boolean isClass) {
		cmp = parent;
		TargetLocationsGroup contentTable = new TargetLocationsGroup(toolkit,
				parent);
		contentTable.createFormContents(parent, toolkit, isFolder, isFile,
				isCorpus, isClass);
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
			boolean isFolder, boolean isFile, boolean isCorpus, boolean isClass) {
		// Create the ScrolledComposite to scroll horizontally and vertically
		
		Composite comp = toolkit.createComposite(parent);
		comp.setLayout(createSectionClientGridLayout(false, 2));
		comp.setLayoutData(new GridData(GridData.FILL_BOTH
				| GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL
				));
		initializeTreeViewer(comp);
		ScrolledComposite sc = new ScrolledComposite(comp, SWT.H_SCROLL
				| SWT.V_SCROLL);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true)
		.applyTo(sc);
		Composite buttonComp = toolkit.createComposite(sc);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.makeColumnsEqualWidth = false;
		buttonComp.setLayout(layout);
		buttonComp.setLayoutData(new GridData(GridData.FILL_BOTH));
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
		if (isClass) {
			fAddCorpusClassButton = toolkit.createButton(buttonComp,
					"Add Corpus Class...", SWT.PUSH);
			GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
					.applyTo(fAddCorpusClassButton);
		}
		if (isCorpus) { // query filter component needs to appear only when
						// corpus is selected
			fFilterCorpusButton = toolkit.createButton(buttonComp,
					"Filter Corpus...", SWT.PUSH);
			GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
					.applyTo(fFilterCorpusButton);
		}
		fRemoveButton = toolkit.createButton(buttonComp, "Remove...", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(fRemoveButton);
		initializeButtons();
		dummy = toolkit.createLabel(parent, "");
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(dummy);
		if (isClass) {
			dummyCorpus = toolkit.createLabel(parent, "");
			GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
					.applyTo(dummyCorpus);
		}
		toolkit.paintBordersFor(comp);
		sc.setContent(buttonComp);
		// Expand both horizontally and vertically
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
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
		if (dummyCorpus != null) {
			int totalCorpusClass = calculateCorpus(fTreeViewer
					.getCheckedElements());
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
				if (file instanceof TreeParent) {
					if (((TreeParent) file).getCorpusClass() != null) {
						continue;
					} else {

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

			if (file instanceof TreeParent) {
				if (((TreeParent) file).getCorpusClass() != null) {
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
					if (!inputFilterPath.isEmpty()){
						if (inputFilterPath.indexOf(File.separator) != -1 && !(inputFilterPath.substring(0, inputFilterPath.lastIndexOf(File.separator))).isEmpty()){
							dlg.setFilterPath(inputFilterPath.substring(0, inputFilterPath.lastIndexOf(File.separator)));
						}
						else{
							dlg.setFilterPath(inputFilterPath);
						}
						
					}
					dlg.setText("Select Folder");
					String path = null;
					String message = "";
					boolean canExit = false;
					while (!canExit) {
						path = dlg.open();
						if (path == null)
							return;
						
					MessageDialog msgDialog = null;

					try {
						//Count the number of files in the directory selected.
						long fileCount = getFilesCount(new File(path),0);

						//If the number of files in the directory is larger than file check threshold value then show a show message dialog. 
						if (fileCount >= FILE_CHECK_THRESHOLD){
							msgDialog = new MessageDialog(null, "Notification",null, "Adding large number of files may take a while....\nPress OK to proceed.", MessageDialog.INFORMATION, new String[]{"Cancel","OK"}, 1);

							int result = msgDialog.open();
								
							//If user selects the cancel button then return
							if (result <= 0){
								msgDialog.close();
								return;
							}
						}
							
					} catch (Exception e1) {
						e1.printStackTrace();
					}	
					
						
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
							inputFilterPath = path.toString();
						}

					}
				}
			});
		}

		if (fAddCorpusClassButton != null) {
			fAddCorpusClassButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final TacitElementSelectionDialog CorpusDialog = new TacitElementSelectionDialog(
							fAddCorpusButton.getShell());
					CorpusDialog
							.setTitle("Select the Corpus Class from the list");
					CorpusDialog
							.setMessage("Enter Corpus Class name to search");
					final List<ICorpusClass> allCorpus = new ArrayList<ICorpusClass>();
					List<ICorpus> corpusList = corporaManagement
							.getAllCorpusDetails();
					List<ICorpusClass> corpusClass = new ArrayList<ICorpusClass>();
					for (ICorpus iCorpusClass : corpusList) {
						corpusClass.addAll(iCorpusClass.getClasses());
					}
					allCorpus.addAll(corpusClass);
					CorpusDialog.setElements(allCorpus.toArray());
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

		if (fAddCorpusButton != null) {
			fAddCorpusButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final TacitElementSelectionDialog CorpusDialog = new TacitElementSelectionDialog(
							fAddCorpusButton.getShell());
					CorpusDialog.setTitle("Select the Corpus from the list");
					CorpusDialog.setMessage("Enter Corpus name to search");
					final SortedSet<ICorpus> allCorpus = new TreeSet<ICorpus>();
					// Job getCorpus = new Job("Retrieving corpus list ...") {
					// @Override
					// protected IStatus run(IProgressMonitor monitor) {
					// allCorpus.clear();
					// try {
					// List<ICorpus> corpusList = corporaManagement
					// .getAllCorpusDetails();
					// allCorpus.addAll(corpusList);
					// CorpusDialog.setElements(allCorpus.toArray());
					// Display.getDefault().syncExec(new Runnable() {
					// @Override
					// public void run() {
					// CorpusDialog.refresh(allCorpus
					// .toArray());
					//
					// }
					// });
					// } catch (final Exception ex) {
					// }
					// return Status.OK_STATUS;
					// }
					// };
					// getCorpus.schedule();
					List<ICorpus> corpusList = corporaManagement
							.getAllCorpusDetails();
					// allCorpus.addAll(corpusList);
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

		if (fFilterCorpusButton != null) {
			fFilterCorpusButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final TacitCorpusFilterDialog filterDialog = new TacitCorpusFilterDialog(
							fFilterCorpusButton.getShell());

					TreeParent sel = (TreeParent) ((IStructuredSelection) fTreeViewer
							.getSelection()).getFirstElement();
					CorpusClass cls = sel.getCorpusClass();
					IQueryProcessor qp = new QueryProcesser(cls);
					Map<String, QueryDataType> keys = null;
					try {
						keys = qp.getJsonKeys();
						filterDialog.setFilterDetails(keys);
						filterDialog.addExistingFilters(cls.getFilters());
						// set the already existing filters

					} catch (JsonSyntaxException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (JsonIOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					if (filterDialog.open() == Window.OK) {
						cls.refreshFilters(filterDialog.getSelectionObjects());
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
								inputFilterPath = dlg.getFilterPath();
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
				} else if (file instanceof ICorpusClass) {
					if (checkExisting((ICorpusClass) file)) {
						continue;
					}
					node = new TreeParent((ICorpusClass) file);
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
			if (node.getCorpus() != null
					&& node.getCorpus().getCorpusId()
							.equals(file.getCorpusId())) {
				return true;
			}
		}
		return false;
	}

	private boolean checkExisting(ICorpusClass file) {
		for (TreeParent node : locationPaths) {
			if (node.getCorpusClass() != null
					&& node.getCorpusClass()
							.getParent()
							.getCorpusId()
							.equals(((ICorpusClass) file).getParent()
									.getCorpusId())
					&& node.getName().equals(
							((ICorpusClass) file).getClassName())) {
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
		Boolean removeEnabled = true;
		Boolean filterEnabled = false;
		if (this.locationPaths == null || this.locationPaths.size() < 1) {
			removeEnabled = false;
		}
		if (!this.locationPaths.contains(sel.getFirstElement())) {
			removeEnabled = false;
		}
		if (sel.size() == 1
				&& (sel.getFirstElement() instanceof TreeParent && ((TreeParent) sel
						.getFirstElement()).getCorpusClass() != null)) {
			CMDataType corpusDataType = ((TreeParent) sel.getFirstElement())
					.getCorpusClass().getParent().getDatatype();
			if (corpusDataType == CMDataType.JSON
					|| corpusDataType == CMDataType.TWITTER_JSON
					|| corpusDataType == CMDataType.REDDIT_JSON
					|| corpusDataType == CMDataType.STACKEXCHANGE_JSON
					|| corpusDataType == CMDataType.CONGRESS_JSON) {
				filterEnabled = true;
			}
		}
		if (fFilterCorpusButton != null)
			fFilterCorpusButton.setEnabled(filterEnabled);
		fRemoveButton.setEnabled(removeEnabled);

	}

	public static int getButtonWidthHint(Button button) {
		PixelConverter converter = new PixelConverter(button);
		int widthHint = converter
				.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint,
				button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}
	
	/*
	 * Calculates the number of files in the given input directory.
	 * Returns number of files if the number is less than the file check threshold else returns the threshold number 
	 */
	public static long getFilesCount(File file, long parentCount) throws Exception{
		
		if (parentCount >= FILE_CHECK_THRESHOLD){
			return FILE_CHECK_THRESHOLD;
		}
		
		File[] files = file.listFiles();
		long count = 0;
		for (File f : files)
			if (f.isDirectory()){
				count += getFilesCount(f,count);
				if (parentCount+count >= FILE_CHECK_THRESHOLD){
					return FILE_CHECK_THRESHOLD;
				}
			}				
			else{
				count++;
				if (parentCount+count > FILE_CHECK_THRESHOLD){
					return FILE_CHECK_THRESHOLD;
				}
			}
				
		files = null;
		return count;
		}

}