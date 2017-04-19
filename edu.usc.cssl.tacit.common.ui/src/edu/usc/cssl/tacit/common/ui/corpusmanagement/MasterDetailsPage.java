package edu.usc.cssl.tacit.common.ui.corpusmanagement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ListDialog;
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

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import edu.usc.cssl.tacit.common.queryprocess.Filter;
import edu.usc.cssl.tacit.common.queryprocess.IQueryProcessor;
import edu.usc.cssl.tacit.common.queryprocess.QueryDataType;
import edu.usc.cssl.tacit.common.queryprocess.QueryProcesser;
import edu.usc.cssl.tacit.common.ui.TacitCorpusFilterDialog;
import edu.usc.cssl.tacit.common.ui.composite.from.RedditJsonHandler;
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
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class MasterDetailsPage extends MasterDetailsBlock {	
	private ScrolledForm corpusMgmtViewform;
	List<ICorpus> corpusList;
	ManageCorpora corpusManagement;
	IViewSite viewSite;
	String exportSelection;
	String corpusClassName, corpusName;

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
				if (((ICorpus) element).getNoOfFiles() != null) {
					
					Long cases = 0l;
					if(((ICorpus) element).getNoOfCases()!=null){
						cases = ((ICorpus) element).getNoOfCases();
					}
					return ((ICorpus) element).getCorpusName() + " (Total " + ((ICorpus) element).getNoOfFiles()
							+ " files, Total "+ cases+" cases)";
				}
				else
					return ((ICorpus) element).getCorpusName();
			else if (element instanceof ICorpusClass)
				if (((ICorpusClass) element).getNoOfFiles() != null) {
					Long cases = 0l;
					if(((ICorpusClass) element).getNoOfCases()!=null) {
						cases = ((ICorpusClass) element).getNoOfCases();
					}
					return ((ICorpusClass) element).getClassName() + " (" + ((ICorpusClass) element).getNoOfFiles()
							+ " files, Total "+ cases +" cases)";
				}
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
		
		final Button queryCorpus = toolkit.createButton(buttonComposite, "Query Corpus", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(queryCorpus);
		queryCorpus.setEnabled(false);
		
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
					queryCorpus.setEnabled(true);
					if(((CorpusClass)c).getParent().getDatatype() != CMDataType.PLAIN_TEXT)
						export.setEnabled(true);
					else
						export.setEnabled(false);
				}
				else{					
					export.setEnabled(false);
					queryCorpus.setEnabled(false);
				}
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
		
		queryCorpus.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(queryCorpus.getShell(), new LabelProvider());
				dialog.setTitle("List of corpora");
				dialog.setMessage("Select an existing corpus");
				dialog.setElements(corpusList.toArray());

				seperateFiles = false;
				final TacitCorpusFilterDialog filterDialog = new TacitCorpusFilterDialog(queryCorpus.getShell());
				final CorpusClass cls = (CorpusClass) ((IStructuredSelection) corpusViewer.getSelection()).getFirstElement();
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
				
				
				IWizardData wizardDataModel = new IWizardData() {

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

					@Override
					public void getExportSelection(String selection) {
						exportSelection = selection;
						
					}

					@Override
					public void getCorpusClass(String corpusClass) {
						corpusClassName = corpusClass; 
						
					}

					@Override
					public void getCorpus(String corpus) {
						corpusName = corpus;
						
					}
				};
				
				WizardDialog wizardDialog = new WizardDialog(export.getShell(), new ExportWizard(filterDialog, wizardDataModel, dialog, true));
				if(wizardDialog.open() == Window.OK){
					Preprocessor ppObj = null;
					List<String> inFiles = null;
					List<Object> inputFiles = new ArrayList<Object>();
					inputFiles.add(cls);
					File delFile = null;
					try {
						ppObj = new Preprocessor("Liwc", false);
						inFiles = ppObj.processData("tempData", inputFiles, seperateFiles);
						if(!inFiles.isEmpty()){
							delFile = new File(inFiles.get(0));
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					
					String outputDir = System.getProperty("user.dir") + System.getProperty("file.separator") + "json_corpuses" + System.getProperty("file.separator") + "misc"+ System.getProperty("file.separator") + corpusName + System.getProperty("file.separator") + corpusClassName;
					System.out.println(outputDir);
					File dir = new File(outputDir);
					if (!dir.exists()) {
						dir.mkdir();
					}
					

						for (String i : inFiles) {
							if(i.contains(".csv"))
								continue;
							try {
							int l = i.lastIndexOf(File.separator);
							String export = outputDir + File.separator + i.substring(l + 1);
							File exportFile =new File(export);
							FileUtils.copyFile(new File(i), exportFile);
							ConsoleView.printlInConsoleln("Successfully exported filename:<"+exportFile.getName()+"> file to " + outputDir);

							} catch (FileNotFoundException e1) {
								e1.printStackTrace();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							
					}
						Corpus corpus = null;
						boolean found = false;
							//The list of corpus is not too large so iterate to check for existing corpus
							int corpusIndex=0;
							for(ICorpus c: corpusList){
								if(c.getCorpusName().equals(corpusName)){
									CorpusClass newClass = new CorpusClass(corpusClassName,outputDir);
									((Corpus) c).addClass(newClass);
									corpusList.set(corpusIndex, c);
									corpusViewer.refresh();
									corpusViewer.setExpandedElements(expandNewCorpus(corpusViewer.getExpandedElements(), (Corpus) corpusList.get(corpusIndex)));
									corpusViewer.setSelection(new StructuredSelection(newClass), true);
									ManageCorpora.saveCorpus((Corpus) c);
									found = true;
									break;
								}
								corpusIndex++;
							}
							if(!found){
							corpus = new Corpus(corpusName, CMDataType.PLAIN_TEXT);
							CorpusClass cc = new CorpusClass(corpusClassName, outputDir);
							cc.setParent(corpus);
							corpus.addClass(cc);
							ManageCorpora.saveCorpus(corpus);
							}


				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		export.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				seperateFiles = false;
				final TacitCorpusFilterDialog filterDialog = new TacitCorpusFilterDialog(export.getShell());
				final CorpusClass cls = (CorpusClass) ((IStructuredSelection) corpusViewer.getSelection()).getFirstElement();
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
				
				//Initializing the data model to collect data from the wizard selection.
				IWizardData wizardDataModel = new IWizardData() {

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

					@Override
					public void getExportSelection(String selection) {
						exportSelection = selection;
						
					}

					@Override
					public void getCorpusClass(String corpusClass) {
						corpusClassName = corpusClass; 
						
					}

					@Override
					public void getCorpus(String corpus) {
						corpusName = corpus;						
					}
				};

				WizardDialog wizardDialog = new WizardDialog(export.getShell(), new ExportWizard(filterDialog, wizardDataModel));
				if (wizardDialog.open() == Window.OK) {
					System.out.println("Ok pressed");
					
					//Check for different export selections and 
					if (exportSelection.equals(ExportSelectionConstants.EXPORT_CSV_FORMAT)){
						System.out.println("CSV format selected");
						try {
							if (cls.getParent().getDatatype() == CMDataType.REDDIT_JSON){
								writeCSVforReddit(outputLoc, cls);
							}else{
								writeCSV(outputLoc, cls);
							}
							
						} catch (Exception e1) {
							ConsoleView.printlInConsoleln("Could not create CSV File.");
							e1.printStackTrace();
						}
					}else if (exportSelection.equals(ExportSelectionConstants.EXPORT_ROBJ_FORMAT)){
						System.out.println("ROBJ format selected");
						
						try {
							//Separate export for reddit
							if (cls.getParent().getDatatype() == CMDataType.REDDIT_JSON){
								writeRObjforReddit(outputLoc, cls);
							}else{
								writeRObj(outputLoc, cls);
							}
							
						} catch (Exception e1) {
							ConsoleView.printlInConsoleln("Could not create R Dataframe.");
							e1.printStackTrace();
						}
						
						
					}else if (exportSelection.equals(ExportSelectionConstants.EXPORT_PJSON_FORMAT)){
						System.out.println("Python-compatible JSON format selected");
						
						try {
							//Separate export for reddit
							if (cls.getParent().getDatatype() == CMDataType.REDDIT_JSON){
								writePJsonforReddit(outputLoc, cls);
							}else{
								writePJson(outputLoc, cls);
							}
							
						} catch (Exception e1) {
							ConsoleView.printlInConsoleln("Could not create Python-compatible JSON.");
							e1.printStackTrace();
							}
						} else if (exportSelection.equals(ExportSelectionConstants.EXPORT_TXT_FORMAT)){
						
						Preprocessor ppObj = null;
						List<String> inFiles = null;
						List<Object> inputFiles = new ArrayList<Object>();
						inputFiles.add(cls);
						File delFile = null;
						try {
							ppObj = new Preprocessor("Liwc", false);
							inFiles = ppObj.processData("tempData", inputFiles, seperateFiles);
							if(!inFiles.isEmpty()){
								delFile = new File(inFiles.get(0));
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						String outputDir = outputLoc + File.separator + cls.getParent().getCorpusName() + "-" + cls.getClassName();
						System.out.println(outputDir);
						File dir = new File(outputDir);
						if (!dir.exists()) {
							dir.mkdir();
						}
							for (String i : inFiles) {
								try {
								int l = i.lastIndexOf(File.separator);
								String export = outputDir + File.separator + i.substring(l + 1);
								File exportFile =new File(export);
								FileUtils.copyFile(new File(i), exportFile);
								ConsoleView.printlInConsoleln("Successfully exported filename:<"+exportFile.getName()+"> file to " + outputDir);

								} catch (FileNotFoundException e1) {
									e1.printStackTrace();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								
						}

						
						
//						if(!inFiles.isEmpty()){
//							try {
//								FileUtils.deleteDirectory(delFile.getParentFile().getParentFile());
//							} catch (IOException e1) {
//								e1.printStackTrace();
//							}
//						}
							if (inFiles == null || inFiles.size() == 0 ){
								ConsoleView.printlInConsoleln("No File exported");
							}else{
								ConsoleView.printlInConsoleln(inFiles.size() + " File(s) exported");
							}
							ConsoleView.printlInConsoleln("Done");
							
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
						deleteCorpus((Corpus) selectedCorpus);
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
	
	protected void deleteCorpus(Corpus corpus) throws IOException{
		CMDataType type = corpus.getDatatype();
		String location = System.getProperty("user.dir") + System.getProperty("file.separator") + "json_corpuses" + System.getProperty("file.separator");
		if(type == CMDataType.FRONTIER_JSON){
			File f = new File(location+"frontier"+File.separator+corpus.getCorpusName());
			FileUtils.deleteDirectory(f);
		}else if(type == CMDataType.STACKEXCHANGE_JSON){
			File f = new File(location+"stackexchange"+File.separator+corpus.getCorpusName());
			FileUtils.deleteDirectory(f);
		}else if(type == CMDataType.PLOSONE_JSON){
			File f = new File(location+"plosone"+File.separator+corpus.getCorpusName());
			FileUtils.deleteDirectory(f);
		}else if(type == CMDataType.CONGRESS_JSON){
			File f = new File(location+"congress"+File.separator+corpus.getCorpusName());
			FileUtils.deleteDirectory(f);
		}else if(type == CMDataType.TYPEPAD_JSON){
			File f = new File(location+"typepad"+File.separator+corpus.getCorpusName());
			FileUtils.deleteDirectory(f);
		}else if(type == CMDataType.REDDIT_JSON){
			File f = new File(location+"reddit"+File.separator+corpus.getCorpusName());
			FileUtils.deleteDirectory(f);
		}else if(type == CMDataType.TWITTER_JSON){
			File f = new File(location+"twitter"+File.separator+corpus.getCorpusName());
			FileUtils.deleteDirectory(f);
		}else if(type == CMDataType.PRESIDENCY_JSON){
			File f = new File(location+"americanpresidency"+File.separator+corpus.getCorpusName());
			FileUtils.deleteDirectory(f);
		}else if(type == CMDataType.HANSARD_JSON){
			File f = new File(location+"hansard"+File.separator+corpus.getCorpusName());
			FileUtils.deleteDirectory(f);
		}else if(type == CMDataType.GOVTRACK_JSON){
			File f = new File(location+"govtrack"+File.separator+corpus.getCorpusName());
			FileUtils.deleteDirectory(f);
		}
		else if(type == CMDataType.IMPORTED_CSV){
			File f = new File(location+corpus.getCorpusName());
			FileUtils.deleteDirectory(f);
		}
		
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
	
	/**
	 * This method writes the corpus into a R Dataframe and stores it to the specified output location.
	 * @param outputLoc
	 * @param cls
	 * @throws Exception
	 */
	private static void writeRObj(String outputLoc, CorpusClass cls) throws Exception{
		
		// Location where the R Object needs to be saved.
		String saveLocation = "\"" + outputLoc + File.separator + cls.getParent().getCorpusName().replaceAll("[^A-Za-z0-9 ]", "").replace(" ", "_") + "-" + cls.getClassName().replaceAll("[^A-Za-z0-9 ]", "").replace(" ", "_") + ".RData" + "\"";
		saveLocation = saveLocation.replace("\\", "\\\\");
		// Location of the corpus
		String corpusLocation = cls.getTacitLocation();
		File corpusDirectory = new File(corpusLocation);
		
		
		String corpusClassLocation = "" ; 
		String[] jsonFiles = corpusDirectory.list();
		for(int i=0; i<jsonFiles.length ;i++){
			if(jsonFiles[i].endsWith(".json")){
				corpusClassLocation = corpusLocation + File.separator + jsonFiles[i]; 
				break;
			}
		}
		
		//Name of the corpus
		String corpusName = cls.getClassName();
		corpusName = corpusName.replaceAll("[^A-Za-z0-9 ]", "").replace(" ", "_");
		
		// Create a Script engine manager:
	    ScriptEngineManager manager = new ScriptEngineManager();
	    
	    // Create a Renjin engine:
	    ScriptEngine engine = manager.getEngineByName("Renjin");
	    
	    // Check if the engine has loaded correctly:
	    if(engine == null) {
	        throw new Exception("Renjin Script Engine not found on the classpath.");
	    }
	    
	    try {
			JSONParser jsonParser = new JSONParser();
			JSONArray entireJsonArray = (JSONArray)jsonParser.parse(new FileReader(new File(corpusClassLocation)));
			
			//Get the first JSONObject and extract all the keys 
			JSONObject firstJsonObject = (JSONObject)entireJsonArray.get(0);
			Set<String> keySet = firstJsonObject.keySet();
			
			//Generate the R data frame columns
			Iterator<String> keyIterator = keySet.iterator();
			String key;
			String dataFrameHeader = "";
			while(keyIterator.hasNext()){
				key = keyIterator.next();
				engine.eval(key+" <- c() ");
				dataFrameHeader = dataFrameHeader + key + ","; 
			}
			dataFrameHeader = dataFrameHeader.substring(0,dataFrameHeader.length()-1);
			
			//Iterate over JSONArray and build the columns for the data frame
			Iterator<Object> arrayIterator = entireJsonArray.iterator();
			while(arrayIterator.hasNext()){
				JSONObject singleJsonObject = (JSONObject)arrayIterator.next();
				
				keyIterator = keySet.iterator();
				while(keyIterator.hasNext()){
					
					key = keyIterator.next();
					
					if(singleJsonObject.containsKey(key)){
						String data = singleJsonObject.get(key).toString();
						
						//Cleaning the data before inserting in the dataframe.
						data = data.toLowerCase()
								.replaceAll("-", " ")
								.replaceAll("[^a-z0-9. ]", "")
								.replaceAll("\\s+", " ")
								.trim();
						
						data = data.replaceAll("\"", " ").replaceAll("\'", " ").replaceAll("\\n", " ").replaceAll("\\r", " ").trim();
						
						//Insert into dataframe
						engine.eval("data <- \"" +data.toString()+"\"");
						engine.eval(key + " <- c("+key+",data)");
					}else{
						engine.eval(key + " <- c("+key+",\"\")");
					}
				}
			}
			
			engine.eval(corpusName+" <- data.frame("+dataFrameHeader+")");	
		    engine.eval("save(list = c(\""+corpusName+"\"), file = "+saveLocation+")");
		    
		} catch (Exception e) {
			e.printStackTrace();
			ConsoleView.printlInConsoleln("Could not create R Dataframe due to an internal error.");
			return;
		}

	    ConsoleView.printlInConsoleln("R Dataframe successfully exported.");
	    ConsoleView.printlInConsoleln("R Dataframe saved at : " + saveLocation);

	}
	
	/**
	 * This method writes the corpus into a CSV file and stores it to the specified output location.
	 * @param outputLoc
	 * @param cls
	 * @throws Exception
	 */
	private static void writeCSV(String outputLoc, CorpusClass cls)throws Exception{
		
		// Location where the CSV needs to be saved.
		String saveLocation = outputLoc + File.separator + cls.getParent().getCorpusName().replaceAll("[^A-Za-z0-9 ]", "").replace(" ", "_") + "-" + cls.getClassName().replaceAll("[^A-Za-z0-9 ]", "").replace(" ", "_") + ".csv";
		
		FileWriter fileWriter = new FileWriter(new File(saveLocation));
		
		// Location of the corpus
		String corpusLocation = cls.getTacitLocation();
		File corpusDirectory = new File(corpusLocation);
		
		
		String corpusClassLocation = "" ; 
		String[] jsonFiles = corpusDirectory.list();
		for(int i=0; i<jsonFiles.length ;i++){
			if(jsonFiles[i].endsWith(".json")){
				corpusClassLocation = corpusLocation + File.separator + jsonFiles[i]; 
				break;
			}
		}
		
		//Name of the corpus
		String corpusName = cls.getClassName().replaceAll("[^A-Za-z0-9 ]", "").replace(" ", "_");
	    
	    try {
			JSONParser jsonParser = new JSONParser();
			JSONArray entireJsonArray = (JSONArray)jsonParser.parse(new FileReader(new File(corpusClassLocation)));
			
			//Get the first JSONObject and extract all the keys 
			JSONObject firstJsonObject = (JSONObject)entireJsonArray.get(0);
			Set<String> keySet = firstJsonObject.keySet();
			
			//Generate the R data frame columns
			Iterator<String> keyIterator = keySet.iterator();
			String key;
			String dataFrameHeader = "";
			while(keyIterator.hasNext()){
				key = keyIterator.next();
				dataFrameHeader = dataFrameHeader + key + ","; 
			}
			dataFrameHeader = dataFrameHeader.substring(0,dataFrameHeader.length()-1);
			fileWriter.write(dataFrameHeader + "\n");
			
			
			//Iterate over JSONArray and build the columns for the data frame
			Iterator<Object> arrayIterator = entireJsonArray.iterator();
			while(arrayIterator.hasNext()){
				JSONObject singleJsonObject = (JSONObject)arrayIterator.next();
				
				//Building single csv entry for each object
				StringBuffer singleCsvEntry=  new StringBuffer();
				keyIterator = keySet.iterator();
				while(keyIterator.hasNext()){
					
					key = keyIterator.next();
					
					if(singleJsonObject.containsKey(key)){
						String data = singleJsonObject.get(key).toString();
						
						//Cleaning the data before inserting in the dataframe.
						data = data.toLowerCase()
								.replaceAll("-", " ")
								.replaceAll("[^a-z0-9. ]", "")
								.replaceAll("\\s+", " ")
								.trim();
						
						data = data.replaceAll("\"", " ").replaceAll("\'", " ").replaceAll("\\n", " ").replaceAll(",", " ").replaceAll("\\r", " ").trim();
						singleCsvEntry.append(data + ",");

					}else{
						singleCsvEntry.append(",");
					}
				}
				singleCsvEntry.deleteCharAt(singleCsvEntry.length()-1);
				fileWriter.write(singleCsvEntry.toString() + "\n");
			}
			
		    
		} catch (Exception e) {
			e.printStackTrace();
			ConsoleView.printlInConsoleln("Could not create CSV file due to an internal error.");
			return;
		}
	    fileWriter.close();
	    ConsoleView.printlInConsoleln("CSV successfully exported.");
	    ConsoleView.printlInConsoleln("CSV file saved at : " + saveLocation);
	}
	
	//TODO: To be implemented
	private static void writeCSVforReddit(String outputLoc, CorpusClass cls)throws Exception{
		JSONParser jsonParser = new JSONParser();
		RedditJsonHandler redditJsonHandler = new RedditJsonHandler();
		
		// Location where the CSV needs to be saved.
		String saveLocation =outputLoc + File.separator + cls.getParent().getCorpusName().replaceAll("[^A-Za-z0-9 ]", "").replace(" ", "_") + "-" + cls.getClassName().replaceAll("[^A-Za-z0-9 ]", "").replace(" ", "_") + ".csv";
		
		FileWriter fileWriter = new FileWriter(new File(saveLocation));
		
		// Location of the corpus
		String corpusLocation = cls.getTacitLocation();
		File corpusDirectory = new File(corpusLocation);
		
		//List of JSON Reddit Post
		String[] jsonFiles = corpusDirectory.list();
		
		//Name of the corpus
		String corpusName = cls.getClassName().replaceAll("[^A-Za-z0-9 ]", "").replace(" ", "_");
		
		
	    try {
	    	
			//Finding the maximum number of comments to be included as a columns.
			String singleRedditLocation = "" ; 
			JSONObject singleReddit = null;
			JSONArray commentArray;
			int maxComments = 0;
			for(int i=0; i<jsonFiles.length ;i++){
				if(jsonFiles[i].endsWith(".json")){
					singleRedditLocation = corpusLocation + File.separator + jsonFiles[i]; 
					singleReddit = (JSONObject)jsonParser.parse(new FileReader(new File(singleRedditLocation)));
					commentArray = (JSONArray)singleReddit.get("comments");
					if (commentArray.size() > maxComments){
						maxComments = commentArray.size();
					}
					
				}
			}
			
			//Generate the R data frame columns
			String dataFrameHeader = "";
			JSONObject singleRedditContent = (JSONObject)singleReddit.get("post"); //Take the last stored single reddit from the above loop to generate the keyset
			Set<String> keySet = singleRedditContent.keySet();
			Iterator<String> keyIterator = keySet.iterator();
			String key;
			
			while(keyIterator.hasNext()){
				key = keyIterator.next();
				dataFrameHeader = dataFrameHeader + key + ","; 
			}
			
			for (int i = 0; i < maxComments; i++){
				dataFrameHeader = dataFrameHeader + "comment" + (i+1) + ",";
			}
			dataFrameHeader = dataFrameHeader.substring(0,dataFrameHeader.length()-1);
			fileWriter.write(dataFrameHeader + "\n");
			
			
			//Iterate over all the Reddit posts to generate the columns in dataframe.
			
			for(int i=0; i<jsonFiles.length ;i++){
				if(jsonFiles[i].endsWith(".json")){
					singleRedditLocation = corpusLocation + File.separator + jsonFiles[i]; 
					singleReddit = (JSONObject)jsonParser.parse(new FileReader(new File(singleRedditLocation)));
					StringBuffer singleCsvEntry=  new StringBuffer();
					
					
					//Inserting the post content
					JSONObject post = (JSONObject)singleReddit.get("post");
					keyIterator = keySet.iterator();
					
					while(keyIterator.hasNext()){
						
						key = keyIterator.next();
						
						if(post.containsKey(key)){
							String data = post.get(key).toString();
							
							//Cleaning the data before inserting in the dataframe.
							data = data.toLowerCase()
									.replaceAll("-", " ")
									.replaceAll("[^a-z0-9. ]", "")
									.replaceAll("\\s+", " ")
									.trim();
							
							data = data.replaceAll("\"", " ").replaceAll("\'", " ").replaceAll("\\n", " ").replaceAll(",", " ").replace("\\r", " ").trim();
							singleCsvEntry.append(data + ",");
						}else{
							singleCsvEntry.append(",");
						}
					}

					
					//Inserting the comment content.
					
					String[] redditPostComments = redditJsonHandler.getPostComments(singleReddit);
					int remainingComments = maxComments;
					//When there are one or more comments.
					if (redditPostComments != null){
						remainingComments = maxComments - redditPostComments.length;
						int commentCountStamp = 1;
						
						for(String redditPostComment : redditPostComments){
							
							//Cleaning the data before inserting in the dataframe.
							redditPostComment = redditPostComment.toLowerCase()
									.replaceAll("-", " ")
									.replaceAll("[^a-z0-9. ]", "")
									.replaceAll("\\s+", " ")
									.trim();
							
							redditPostComment = redditPostComment.replaceAll("\"", " ").replaceAll("\'", " ").replaceAll("\\n", " ").replaceAll(",", " ").replace("\\r", " ").trim();
							singleCsvEntry.append(redditPostComment + ",");
							commentCountStamp++;
						}
						
						for(int j=0; j<remainingComments; j++){
							singleCsvEntry.append(",");
							commentCountStamp++;
						}
						
					}else{//When there is no comment
						for(int j=0; j<remainingComments; j++){
							singleCsvEntry.append(",");
						}
					}
					
					singleCsvEntry.deleteCharAt(singleCsvEntry.length()-1);
					fileWriter.write(singleCsvEntry.toString() + "\n");
						
				}
			}
			
		    
		} catch (Exception e) {
			e.printStackTrace();
			ConsoleView.printlInConsoleln("Could not create CSV file due to an internal error.");
			return;
		}

	    fileWriter.close();
	    ConsoleView.printlInConsoleln("CSV successfully exported.");
	    ConsoleView.printlInConsoleln("CSV file saved at : " + saveLocation);
		
	}
	

	private static void writeRObjforReddit(String outputLoc, CorpusClass cls)throws Exception{
		
		JSONParser jsonParser = new JSONParser();
		RedditJsonHandler redditJsonHandler = new RedditJsonHandler();
		
		// Location where the R Object needs to be saved.
		String saveLocation = "\"" + outputLoc + File.separator + cls.getParent().getCorpusName().replaceAll("[^A-Za-z0-9 ]", "").replace(" ", "_") + "-" + cls.getClassName().replaceAll("[^A-Za-z0-9 ]", "").replace(" ", "_") + ".RData" + "\"";
		saveLocation = saveLocation.replace("\\", "\\\\");
		// Location of the corpus
		String corpusLocation = cls.getTacitLocation();
		File corpusDirectory = new File(corpusLocation);
		
		//List of JSON Reddit Post
		String[] jsonFiles = corpusDirectory.list();
		
		//Name of the corpus
		String corpusName = cls.getClassName().replaceAll("[^A-Za-z0-9 ]", "").replaceAll(" ","_");
		
		// Create a Script engine manager:
	    ScriptEngineManager manager = new ScriptEngineManager();
	    
	    // Create a Renjin engine:
	    ScriptEngine engine = manager.getEngineByName("Renjin");
	    
	    // Check if the engine has loaded correctly:
	    if(engine == null) {
	        throw new Exception("Renjin Script Engine not found on the classpath.");
	    }
		
	    try {
	    	
			//Finding the maximum number of comments to be included as a columns.
			String singleRedditLocation = "" ; 
			JSONObject singleReddit = null;
			JSONArray commentArray;
			int maxComments = 0;
			for(int i=0; i<jsonFiles.length ;i++){
				if(jsonFiles[i].endsWith(".json")){
					singleRedditLocation = corpusLocation + File.separator + jsonFiles[i]; 
					singleReddit = (JSONObject)jsonParser.parse(new FileReader(new File(singleRedditLocation)));
					commentArray = (JSONArray)singleReddit.get("comments");
					if (commentArray.size() > maxComments){
						maxComments = commentArray.size();
					}
					
				}
			}
			
			//Generate the R data frame columns
			String dataFrameHeader = "";
			JSONObject singleRedditContent = (JSONObject)singleReddit.get("post"); //Take the last stored single reddit from the above loop to generate the keyset
			Set<String> keySet = singleRedditContent.keySet();
			Iterator<String> keyIterator = keySet.iterator();
			String key;
			
			while(keyIterator.hasNext()){
				key = keyIterator.next();
				engine.eval(key+" <- c() ");
				dataFrameHeader = dataFrameHeader + key + ","; 
			}
			
			for (int i = 0; i < maxComments; i++){
				engine.eval("comment" + (i+1) +" <- c() ");
				dataFrameHeader = dataFrameHeader + "comment" + (i+1) + ",";
			}
			dataFrameHeader = dataFrameHeader.substring(0,dataFrameHeader.length()-1);
			
			
			//Iterate over all the Reddit posts to generate the columns in dataframe.
			
			for(int i=0; i<jsonFiles.length ;i++){
				if(jsonFiles[i].endsWith(".json")){
					singleRedditLocation = corpusLocation + File.separator + jsonFiles[i]; 
					singleReddit = (JSONObject)jsonParser.parse(new FileReader(new File(singleRedditLocation)));
					
					
					
					//Inserting the post content
					JSONObject post = (JSONObject)singleReddit.get("post");
					keyIterator = keySet.iterator();
					while(keyIterator.hasNext()){
						
						key = keyIterator.next();
						
						if(post.containsKey(key)){
							String data = post.get(key).toString();
							
							//Cleaning the data before inserting in the dataframe.
							data = data.toLowerCase()
									.replaceAll("-", " ")
									.replaceAll("[^a-z0-9. ]", "")
									.replaceAll("\\s+", " ")
									.trim();
							
							data = data.replaceAll("\"", " ").replaceAll("\'", " ").replaceAll("\\n", " ").replace("\\r", " ").trim();
							
							//Insert into dataframe
							engine.eval("data <- \"" +data.toString()+"\"");
							engine.eval(key + " <- c("+key+",data)");
						}else{
							engine.eval(key + " <- c("+key+",\"\")");
						}
					}					
					
					
					
					
					//Inserting the comment content.
					
					String[] redditPostComments = redditJsonHandler.getPostComments(singleReddit);
					int remainingComments = maxComments;
					//When there are one or more comments.
					if (redditPostComments != null){
						remainingComments = maxComments - redditPostComments.length;
						int commentCountStamp = 1;
						
						for(String redditPostComment : redditPostComments){
							
							//Cleaning the data before inserting in the dataframe.
							redditPostComment = redditPostComment.toLowerCase()
									.replaceAll("-", " ")
									.replaceAll("[^a-z0-9. ]", "")
									.replaceAll("\\s+", " ")
									.trim();
							
							redditPostComment = redditPostComment.replaceAll("\"", " ").replaceAll("\'", " ").replaceAll("\\n", " ").replace("\\r", " ").trim();
							engine.eval("data <- \"" +redditPostComment+"\"");
							engine.eval("comment" + commentCountStamp + " <- c(comment" + commentCountStamp + ",data)");
							commentCountStamp++;
						}
						
						for(int j=0; j<remainingComments; j++){
							engine.eval("comment" + commentCountStamp + " <- c(comment" + commentCountStamp + ",\"\")");
							commentCountStamp++;
						}
						
					}else{//When there is no comment
						for(int j=0; j<remainingComments; j++){
							engine.eval("comment" + (j+1) + " <- c(comment" + (j+1) + ",\"\")");
						}
					}
						
				}
			}
			
			
			
			engine.eval(corpusName+" <- data.frame("+dataFrameHeader+")");	
		    engine.eval("save(list = c(\""+corpusName+"\"), file = "+saveLocation+")");
		    
		} catch (Exception e) {
			e.printStackTrace();
			ConsoleView.printlInConsoleln("Could not create R Dataframe due to an internal error.");
			return;
		}

	    ConsoleView.printlInConsoleln("R Dataframe successfully exported.");
	    ConsoleView.printlInConsoleln("R Dataframe saved at : " + saveLocation);
	}
	
	private static void writePJson(String outputLoc, CorpusClass cls)throws Exception{
		String saveLocation = outputLoc + File.separator + cls.getParent().getCorpusName().replaceAll("[^A-Za-z0-9 ]", "").replace(" ", "_") + "-" + cls.getClassName().replaceAll("[^A-Za-z0-9 ]", "").replace(" ", "_") + ".json";
		
		File destFile = new File(saveLocation);
		
		// Location of the corpus
		String corpusLocation = cls.getTacitLocation();
		File corpusDirectory = new File(corpusLocation);
		
		
		String corpusClassLocation = "" ; 
		String[] jsonFiles = corpusDirectory.list();
		for(int i=0; i<jsonFiles.length ;i++){
			if(jsonFiles[i].endsWith(".json")){
				corpusClassLocation = corpusLocation + File.separator + jsonFiles[i]; 
				break;
			}
		}
		
		File srcFile = new File(corpusClassLocation);
		
		FileUtils.copyFile(srcFile, destFile);
		
	    ConsoleView.printlInConsoleln("Python-compatible JSON successfully exported.");
	    ConsoleView.printlInConsoleln("Python-compatible JSON saved at : " + saveLocation);
		
	}
	
	private static void writePJsonforReddit(String outputLoc, CorpusClass cls)throws Exception{
		JSONParser jsonParser = new JSONParser();
		
		String saveLocation =outputLoc + File.separator + cls.getParent().getCorpusName().replaceAll("[^A-Za-z0-9 ]", "").replace(" ", "_") + "-" + cls.getClassName().replaceAll("[^A-Za-z0-9 ]", "").replace(" ", "_") + ".json";
	
		FileWriter fileWriter = new FileWriter(new File(saveLocation));
		
		// Location of the corpus
		String corpusLocation = cls.getTacitLocation();
		File corpusDirectory = new File(corpusLocation);
		
		//List of JSON Reddit Post
		String[] jsonFiles = corpusDirectory.list();
		
		String singleRedditLocation = "" ; 
		JSONObject singleReddit = null;
		JSONArray resultArray =  new JSONArray();
		for(int i=0; i<jsonFiles.length ;i++){
			if(jsonFiles[i].endsWith(".json")){
				singleRedditLocation = corpusLocation + File.separator + jsonFiles[i]; 
				singleReddit = (JSONObject)jsonParser.parse(new FileReader(new File(singleRedditLocation)));
				resultArray.add(singleReddit);

			}
		}
		
		fileWriter.write(resultArray.toJSONString());
		fileWriter.close();
		
		ConsoleView.printlInConsoleln("Python-compatible JSON successfully exported.");
	    ConsoleView.printlInConsoleln("Python-compatible JSON saved at : " + saveLocation);

	
	}


}
