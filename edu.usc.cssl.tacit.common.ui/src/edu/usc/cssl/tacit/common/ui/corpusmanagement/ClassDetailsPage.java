package edu.usc.cssl.tacit.common.ui.corpusmanagement;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;

public class ClassDetailsPage implements IDetailsPage {
	private IManagedForm mform;
	private CorpusClass selectedCorpusClass;
	private Text classPathTxt;
	private Text classNameTxt;

	public ClassDetailsPage() {
	}

	@Override
	public void initialize(IManagedForm mform) {
		this.mform = mform;
	}

	@Override
	public void createContents(Composite parent) {
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).applyTo(parent);
		FormToolkit toolkit = mform.getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(section);
		section.setText("Class Details");
		section.setDescription("Enter the details of class.");

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);

		Composite sectionClient = toolkit.createComposite(section);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sectionClient);
		section.setClient(sectionClient);

		TacitFormComposite.createEmptyRow(toolkit, sectionClient);

		final Label classPathLbl = toolkit.createLabel(sectionClient, "Class Path:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(classPathLbl);
		classPathTxt = toolkit.createText(sectionClient, "", SWT.BORDER);
		if(null!=selectedCorpusClass) classPathTxt.setText(selectedCorpusClass.getClassPath());
		
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(classPathTxt);
		final Button browseBtn = toolkit.createButton(sectionClient, "Browse...", SWT.PUSH);
		browseBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(browseBtn.getShell(), SWT.OPEN);
				dlg.setText("Open");
				String path = dlg.open();
				if (path == null)
					return; 
				classPathTxt.setText(path);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		final Label classLbl = toolkit.createLabel(sectionClient, "Class Name:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(classLbl);
		classNameTxt = toolkit.createText(sectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(classNameTxt);
		if(null != selectedCorpusClass) classNameTxt.setText(selectedCorpusClass.getClassName());
		
		classPathTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				selectedCorpusClass.setClassPath(classPathTxt.getText());
				//selectedCorpusClass.getViewer().refresh();
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				selectedCorpusClass.setClassPath(classPathTxt.getText());
				//selectedCorpusClass.getViewer().refresh();
			}
		});	
		
		classNameTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				selectedCorpusClass.setClassName(classNameTxt.getText());
				//selectedCorpusClass.getViewer().refresh();
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				selectedCorpusClass.setClassName(classNameTxt.getText());
				//selectedCorpusClass.getViewer().refresh();
			}
		});		
	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public void commit(boolean onSave) {

	}

	@Override
	public boolean setFormInput(Object input) {
		return false;
	}

	@Override
	public void setFocus() {

	}

	@Override
	public boolean isStale() {
		return false;
	}

	@Override
	public void refresh() {

	}

	@Override
	public void selectionChanged(IFormPart part, ISelection selection) {
		selectedCorpusClass = (CorpusClass) ((IStructuredSelection) selection).getFirstElement();	
		classNameTxt.setText(selectedCorpusClass.getClassName());
		classPathTxt.setText(selectedCorpusClass.getClassPath());
	}

}
