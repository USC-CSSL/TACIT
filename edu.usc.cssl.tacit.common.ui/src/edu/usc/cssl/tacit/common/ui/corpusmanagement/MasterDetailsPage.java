package edu.usc.cssl.tacit.common.ui.corpusmanagement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.Corpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;

public class MasterDetailsPage extends MasterDetailsBlock {

	List<ICorpus> corpusList; 
	MasterDetailsPage() {
		corpusList = new ArrayList<ICorpus>();
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
			if(inputElement instanceof List) return ((List)inputElement).toArray();
			else if(inputElement instanceof ICorpus) return  ((ICorpus)inputElement).getClasses().toArray();
			return new Object[] {};
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			
			return getElements(parentElement);
		}

		@Override
		public Object getParent(Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			// TODO Auto-generated method stub
			return true;
		}

	}

	class MasterLabelProvider extends LabelProvider implements
			ILabelProvider {
		@Override
		public String getText(Object element) {
			if(element instanceof ICorpus)
				return ((ICorpus) element).getCorpusId();
			else if(element instanceof ICorpusClass)
				return ((ICorpusClass) element).getClassName();
			return null;
		}
	}

	@Override
	protected void createMasterPart(final IManagedForm managedForm,
			Composite parent) {
		FormToolkit toolkit = managedForm.getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setText("Corpora"); //$NON-NLS-1$
 		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		client.setLayout(layout);
		
		//Create a tree to hold all corpuses
		toolkit.paintBordersFor(client);
		
		final TreeViewer corpuses = new TreeViewer(client, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 400; gd.widthHint = 100;
		corpuses.getTree().setLayoutData(gd);
		
		//Add all required buttons in the composite
		Composite buttonComposite = new Composite(client, SWT.NONE);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.marginWidth = buttonLayout.marginHeight = 0;
		buttonLayout.makeColumnsEqualWidth = true;
		buttonComposite.setLayout(buttonLayout);
		buttonComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		
		Button addDir = toolkit.createButton(buttonComposite, "Add Corpora", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(addDir);

		Button addClass = toolkit.createButton(buttonComposite, "Add Class", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(addClass);
		
		Button removeItem = toolkit.createButton(buttonComposite, "Remove", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(removeItem);
		
		section.setClient(client);
		final SectionPart spart = new SectionPart(section);
		managedForm.addPart(spart);
		
		corpuses.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				managedForm.fireSelectionChanged(spart, event.getSelection());
			}
		});
		corpuses.setContentProvider(new MasterContentProvider());
		corpuses.setLabelProvider(new MasterLabelProvider());
		corpuses.setInput(corpusList.toArray());
		
		addDir.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Corpus c = new Corpus("Corpus1", "JSON");
				c.addClass(new CorpusClass("Class1", ""));;
				corpusList.add(c);
				corpuses.setInput(corpusList);
			}
		});	
		addClass.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)corpuses.getSelection();
				 try {
					 	ICorpus corpusSelected = (ICorpus)selection.getFirstElement();
					 	int corpusIndex = corpusList.indexOf(corpusSelected);
		            	((Corpus)corpusSelected).addClass(new CorpusClass("Class2", ""));
					 	corpusList.set(corpusIndex,corpusSelected);
		             }catch(ClassCastException except) { }
				corpuses.refresh();   				 
			}
		});
		removeItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)corpuses.getSelection();
				 try {
					 	ICorpus corpusSelected = (ICorpus)selection.getFirstElement(); 
		            	corpusList.remove(corpusSelected);
		             }catch(ClassCastException except){ //exception means item selected is not a corpus but a class.
		            	ITreeSelection classSelection = (ITreeSelection)selection;
		            	ICorpusClass classObj = (ICorpusClass)selection.getFirstElement();
		     			Corpus parentCorpus = (Corpus)classSelection.getPaths()[0].getParentPath().getLastSegment();
		            	parentCorpus.removeClass(classObj);
		        }
				corpuses.refresh();   				 
			}
		});			
	}

	protected Object[] expandNewCorpus(TreeViewer corpuses, Corpus c) {
		Object[] expanded = corpuses.getExpandedElements();
		Object[] newExpandedSet = new Object[expanded.length+1];
		int index = 0;
		for(Object o : expanded) {
			newExpandedSet[index++] = o;
		}
		newExpandedSet[index] = c;
		return newExpandedSet;
	}

	@Override
	protected void registerPages(DetailsPart detailsPart) {
		detailsPart.registerPage(Corpus.class, new TypeOneDetailsPage());
		detailsPart.registerPage(CorpusClass.class, new TypeTwoDetailsPage());
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
