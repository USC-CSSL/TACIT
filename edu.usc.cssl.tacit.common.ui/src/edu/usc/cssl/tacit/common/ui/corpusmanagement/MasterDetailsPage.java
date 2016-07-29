package edu.usc.cssl.tacit.common.ui.corpusmanagement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import edu.usc.cssl.tacit.common.queryprocess.Filter;
import edu.usc.cssl.tacit.common.queryprocess.IQueryProcessor;
import edu.usc.cssl.tacit.common.queryprocess.QueryDataType;
import edu.usc.cssl.tacit.common.queryprocess.QueryProcesser;
import edu.usc.cssl.tacit.common.ui.TacitCorpusFilterDialog;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusManagementConstants;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CMDataType;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.Corpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;
import edu.usc.cssl.tacit.common.ui.preprocessor.Preprocessor;
import edu.usc.cssl.tacit.common.ui.utility.INlpCommonUiConstants;
import edu.usc.cssl.tacit.common.ui.utility.IconRegistry;

public class MasterDetailsPage extends MasterDetailsBlock {
	private ScrolledForm corpusMgmtViewform;
	List<ICorpus> corpusList;
	ManageCorpora corpusManagement;
	IViewSite viewSite;

	MasterDetailsPage(ScrolledForm form, IViewSite viewSite) throws IOException, ParseException {
		corpusList = new ArrayList<ICorpus>();
		corpusManagement = new ManageCorpora();
		corpusList = corpusManagement.getAllCorpusDetails();
		this.corpusMgmtViewform = form;
		this.viewSite = viewSite;
	}

	class MasterContentProvider implements ITreeContentProvider {
		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof List)
				return ((List) inputElement).toArray();
			else if (inputElement instanceof ICorpus)
				return ((ICorpus) inputElement).getClasses().toArray();
			else if (inputElement instanceof String) {
				File tacitLocationFiles = new File((String) inputElement);
				if (tacitLocationFiles.isDirectory()) {
					return tacitLocationFiles.listFiles();
				}
			}
			return new Object[] {};
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof ICorpus)
				return true;
			return false;
		}

	}

	class MasterLabelProvider extends LabelProvider implements ILabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof ICorpus)
				if (((ICorpus) element).getNoOfFiles() != null)
					return ((ICorpus) element).getCorpusName() + " (Total " + ((ICorpus) element).getNoOfFiles()
							+ " files)";
				else
					return ((ICorpus) element).getCorpusName();
			else if (element instanceof ICorpusClass)
				if (((ICorpusClass) element).getNoOfFiles() != null)
					return ((ICorpusClass) element).getClassName() + " (" + ((ICorpusClass) element).getNoOfFiles()
							+ " files)";
				else
					return ((ICorpusClass) element).getClassName();
			else if (element instanceof String) {
				File tacitLocationFiles = new File((String) element);
				if (tacitLocationFiles.exists())
					return tacitLocationFiles.getAbsolutePath();
			}
			return null;
		}

		@Override
		public Image getImage(Object element) {

			if (element instanceof ICorpusClass)
				return IconRegistry.getImageIconFactory().getImage(INlpCommonUiConstants.CORPUS_CLASS);
			else if (element instanceof ICorpus)
				return IconRegistry.getImageIconFactory().getImage(INlpCommonUiConstants.CORPUS);
			else if (element instanceof String) {
				return IconRegistry.getImageIconFactory().getImage(INlpCommonUiConstants.FILE_OBJ);
			}
			return null;
		}
	}

	String outputLoc;
	boolean seperateFiles;

	@Override
	protected void createMasterPart(final IManagedForm managedForm, Composite parent) {
		FormToolkit toolkit = managedForm.getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setText("Corpora"); //$NON-NLS-1$
		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		client.setLayout(layout);

		// Create a tree to hold all corpuses
		toolkit.paintBordersFor(client);

		final TreeViewer corpusViewer = new TreeViewer(client, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 400;
		gd.widthHint = 100;
		corpusViewer.getTree().setLayoutData(gd);

		// Add all required buttons in the composite
		Composite buttonComposite = new Composite(client, SWT.NONE);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.marginWidth = buttonLayout.marginHeight = 0;
		buttonLayout.makeColumnsEqualWidth = true;
		buttonComposite.setLayout(buttonLayout);
		buttonComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		Button addCorpus = toolkit.createButton(buttonComposite, "Add Corpus", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(addCorpus);

		final Button addClass = toolkit.createButton(buttonComposite, "Add Class", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(addClass);

		Button remove = toolkit.createButton(buttonComposite, "Remove", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(remove);

		final Button export = toolkit.createButton(buttonComposite, "Export", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(export);
		export.setEnabled(false);
		
		Button refresh = toolkit.createButton(buttonComposite, "Refresh", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(refresh);
		
		section.setClient(client);
		final SectionPart spart = new SectionPart(section);
		managedForm.addPart(spart);

		corpusViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				managedForm.fireSelectionChanged(spart, event.getSelection());
				/*
				 * // Not needed as add class is always selected try {
				 * //corpusMgmtViewform.getMessageManager().removeAllMessages();
				 * // removes all error messages IStructuredSelection selection
				 * = (IStructuredSelection) event.getSelection(); Object
				 * selectedObj = selection.getFirstElement(); if(selectedObj
				 * instanceof ICorpus) { addClass.setEnabled(true); } else
				 * if(selectedObj instanceof ICorpusClass) {
				 * addClass.setEnabled(false); } } catch(Exception exp) {
				 * //exception means item selected is not a corpus but a class.
				 * }
				 */
				Object c = ((IStructuredSelection)event.getSelection()).getFirstElement();
				if(c instanceof ICorpusClass){
					if(((CorpusClass)c).getParent().getDatatype() != CMDataType.PLAIN_TEXT)
						export.setEnabled(true);
					else
						export.setEnabled(false);
				}
				else					
					export.setEnabled(false);
			}
		});

		corpusViewer.setContentProvider(new MasterContentProvider());
		corpusViewer.setLabelProvider(new MasterLabelProvider());
		for (ICorpus corpus : corpusList) { // set the viewer for the old
											// corpuses loaded form disk
			((Corpus) corpus).setViewer(corpusViewer);
			for (ICorpusClass cc : corpus.getClasses()) {
				((CorpusClass) cc).setViewer(corpusViewer);
			}
		}
		corpusViewer.setInput(corpusList);
		
		refresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				corpusManagement = new ManageCorpora();
				corpusList = corpusManagement.getAllCorpusDetails();
				corpusViewer.setInput(corpusList);
			}
		});
		
		export.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				seperateFiles = false;
				final TacitCorpusFilterDialog filterDialog = new TacitCorpusFilterDialog(export.getShell());
				final CorpusClass cls = (CorpusClass) ((IStructuredSelection) corpusViewer.getSelection())
						.getFirstElement();

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
				// if (filterDialog.open() == Window.OK) {
				// cls.refreshFilters(filterDialog.getSelectionObjects());
				// }
				IWizardData w = new IWizardData() {

					@Override
					public void getData(List<Filter> filter) {
						cls.refreshFilters(filter);
					}

					@Override
					public void getPath(String path) {
						outputLoc = path;
					}

					@Override
					public void getDivision(boolean seperate) {
						seperateFiles = true;

					}
				};

				WizardDialog wizardDialog = new WizardDialog(export.getShell(), new ExportWizard(filterDialog, w));
				if (wizardDialog.open() == Window.OK) {
					System.out.println("Ok pressed");
					Preprocessor ppObj = null;
					List<String> inFiles = null;
					List<Object> inputFiles = new ArrayList<Object>();
					inputFiles.add(cls);
					File delFile = null;
					try {
						ppObj = new Preprocessor("Liwc", true);
						inFiles = ppObj.processData("tempData", inputFiles, seperateFiles);
						if(!inFiles.isEmpty()){
							delFile = new File(inFiles.get(0));
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					String outputDir = outputLoc + File.separator + cls.getParent().getCorpusName() + "-"
							+ cls.getClassName();
					System.out.println(outputDir);
					File dir = new File(outputDir);
					if (!dir.exists()) {
						dir.mkdir();
					}
						for (String i : inFiles) {
							try {
								int l = i.lastIndexOf(File.separator);
								String export = outputDir + File.separator + i.substring(l + 1);
								FileUtils.copyFile(new File(i), new File(export));

							} catch (FileNotFoundException e1) {
								e1.printStackTrace();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							
					}
					if(!inFiles.isEmpty()){
						try {
							FileUtils.deleteDirectory(delFile.getParentFile().getParentFile());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}

				} else {
					System.out.println("Cancel pressed");
				}

			}

		});

		addCorpus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StringBuilder corpusTempName = new StringBuilder("Corpus ");
				corpusTempName.append(corpusList.size() + 1);
				Corpus c = new Corpus(new String(corpusTempName), CMDataType.PLAIN_TEXT, corpusViewer);
				c.addClass(new CorpusClass("Class 1", ICorpusManagementConstants.DEFAULT_CLASSPATH, corpusViewer));
				corpusList.add(c);
				Object[] expandedItems = corpusViewer.getExpandedElements();
				corpusViewer.setInput(corpusList);
				corpusViewer.setExpandedElements(expandNewCorpus(expandedItems, c));
				corpusViewer.setSelection(new StructuredSelection(c), true);
			}
		});

		addClass.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) corpusViewer.getSelection();
				try {
					ICorpus corpusSelected = (selection.getFirstElement() instanceof ICorpus)
							? (ICorpus) selection.getFirstElement()
							: (ICorpus) ((ICorpusClass) selection.getFirstElement()).getParent();
					int corpusIndex = corpusList.indexOf(corpusSelected);
					StringBuilder classTempName = new StringBuilder("Class ");
					classTempName.append(corpusList.get(corpusIndex).getClasses().size() + 1);
					CorpusClass newClass = new CorpusClass(new String(classTempName),
							ICorpusManagementConstants.DEFAULT_CLASSPATH, corpusViewer);
					((Corpus) corpusSelected).addClass(newClass);
					corpusList.set(corpusIndex, corpusSelected);
					corpusViewer.refresh();
					corpusViewer.setExpandedElements(
							expandNewCorpus(corpusViewer.getExpandedElements(), (Corpus) corpusList.get(corpusIndex)));
					corpusViewer.setSelection(new StructuredSelection(newClass), true);
				} catch (Exception exp) {
				}
			}
		});

		final MessageDialog dg = new MessageDialog(corpusMgmtViewform.getShell(), "Delete Corpus/Class", null,
				"Are you sure you want to delete?", MessageDialog.QUESTION_WITH_CANCEL,
				new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);

		remove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) corpusViewer.getSelection();
				try {
					switch (dg.open()) {
					case 1:
						return;
					}
					Object selectedObj = selection.getFirstElement();
					if (selectedObj instanceof ICorpus) {
						ICorpus selectedCorpus = (ICorpus) selection.getFirstElement();
						corpusList.remove(selectedCorpus);
						ManageCorpora.removeCorpus((Corpus) selectedCorpus, true);
					} else if (selectedObj instanceof ICorpusClass) {
						ITreeSelection classSelection = (ITreeSelection) selection;
						ICorpusClass selectedClass = (ICorpusClass) selection.getFirstElement();
						Corpus parentCorpus = (Corpus) classSelection.getPaths()[0].getParentPath().getLastSegment();
						parentCorpus.removeClass(selectedClass);
						ManageCorpora.removeCorpus(parentCorpus, false);
					}
					corpusViewer.refresh();
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							corpusViewer.refresh();
						};
					});
				} catch (Exception exp) { // exception means item selected is
											// not a corpus but a class.
				}
			}
		});

	}

	protected Object[] expandNewCorpus(Object[] expanded, Corpus c) {
		Object[] newExpandedSet = new Object[expanded.length + 1];
		int index = 0;
		for (Object o : expanded) {
			newExpandedSet[index++] = o;
		}
		newExpandedSet[index] = c;
		return newExpandedSet;
	}

	@Override
	protected void registerPages(DetailsPart detailsPart) {
		detailsPart.registerPage(Corpus.class, new CorpusDetailsPage(corpusMgmtViewform, corpusList, viewSite));
		detailsPart.registerPage(CorpusClass.class, new ClassDetailsPage(corpusMgmtViewform));
	}

	@Override
	protected void createToolBarActions(IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		Action haction = new Action("hor", Action.AS_RADIO_BUTTON) { //$NON-NLS-1$
			@Override
			public void run() {
				sashForm.setOrientation(SWT.HORIZONTAL);
				form.reflow(true);
			}
		};
		haction.setChecked(true);
		Action vaction = new Action("ver", Action.AS_RADIO_BUTTON) { //$NON-NLS-1$
			@Override
			public void run() {
				sashForm.setOrientation(SWT.VERTICAL);
				form.reflow(true);
			}
		};
		vaction.setChecked(false);
		form.getToolBarManager().add(haction);
		form.getToolBarManager().add(vaction);
	}
}
