package edu.usc.cssl.tacit.common.ui.corpusmanagement;

import java.io.File;

import org.eclipse.jface.dialogs.IMessageProvider;
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
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;

public class ClassDetailsPage implements IDetailsPage {
	private IManagedForm mform;
	private CorpusClass selectedCorpusClass;
	private Text classPathTxt;
	private Text classNameTxt;
	private ScrolledForm corpusMgmtViewform;
	private ManageCorpora corpusManagement;

	public ClassDetailsPage() {
		corpusManagement = new ManageCorpora();
	}

	public ClassDetailsPage(ScrolledForm corpusMgmtViewform) {
		this.corpusMgmtViewform = corpusMgmtViewform;
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

		final Label classLbl = toolkit.createLabel(sectionClient, "Class Name:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(classLbl);
		classNameTxt = toolkit.createText(sectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(classNameTxt);
		if(null != selectedCorpusClass) {
			classNameTxt.setText(selectedCorpusClass.getClassName());
		}
		classNameTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				if(isClassnameValid(classNameTxt.getText())) 
					selectedCorpusClass.setClassName(classNameTxt.getText());				
				
				if(!selectedCorpusClass.getClassName().isEmpty()) 
					corpusMgmtViewform.getMessageManager().removeMessage("classNameEmpty");
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if(isClassnameValid(classNameTxt.getText())) 
					selectedCorpusClass.setClassName(classNameTxt.getText());
				
				if(!selectedCorpusClass.getClassName().isEmpty()) 
					corpusMgmtViewform.getMessageManager().removeMessage("classNameEmpty");
			}
		});	
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
				selectedCorpusClass.setClassPath(classPathTxt.getText());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		classPathTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				if(isClassPathValid(classPathTxt.getText())) 
					selectedCorpusClass.setClassPath(classPathTxt.getText());				
				
				if(!selectedCorpusClass.getClassPath().isEmpty()) 
					corpusMgmtViewform.getMessageManager().removeMessage("classPathEmpty");			
				}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(isClassPathValid(classPathTxt.getText())) 
					selectedCorpusClass.setClassPath(classPathTxt.getText());				
				
				if(!selectedCorpusClass.getClassPath().isEmpty()) 
					corpusMgmtViewform.getMessageManager().removeMessage("classPathEmpty");				}
		});	
		
	}
	
	private boolean isClassnameValid(String className) {
		if(className.isEmpty()) {
			corpusMgmtViewform.getMessageManager().addMessage("classNameEmpty", "Class name cannot be empty", null, IMessageProvider.ERROR);
			return false;
		} else {
			corpusMgmtViewform.getMessageManager().removeMessage("classNameEmpty");
		}
		String parentCorpusId = selectedCorpusClass.getParentId();
		ICorpus parentCorpus = corpusManagement.readCorpusById(parentCorpusId);
		//String corpusId = corpus.getCorpusId();
		for(ICorpusClass cc : parentCorpus.getClasses()) {
			if((CorpusClass)cc != selectedCorpusClass) {
				if(cc.getClassName().equals(className)) {
					corpusMgmtViewform.getMessageManager().addMessage("className", "Class name \""+ className +"\"already exists in corpus "+ parentCorpusId, null, IMessageProvider.ERROR);
					return false;
				}
			}
		}
		corpusMgmtViewform.getMessageManager().removeMessage("className");
		return true;
	}

	protected boolean isClassPathValid(String classPathText) {
		if (classPathText.isEmpty()) {
			corpusMgmtViewform.getMessageManager().addMessage("classPath", "Class path must be a valid diretory location", null, IMessageProvider.ERROR);
			return false;
		}
		File tempFile = new File(classPathText);
		if (!tempFile.exists() || !tempFile.isDirectory()) {
			corpusMgmtViewform.getMessageManager().addMessage("classPath", "Class path must be a valid diretory location", null, IMessageProvider.ERROR);
			return false;
		} else {
			corpusMgmtViewform.getMessageManager().removeMessage("classPath");
			String message = validateOutputDirectory(classPathText);
			if (null != message) {
				corpusMgmtViewform.getMessageManager().addMessage("classPath", message, null, IMessageProvider.ERROR);
				return false;
			}
		}
		corpusMgmtViewform.getMessageManager().removeMessage("classPath");
		return true;
	}
	
	
	private String validateOutputDirectory(String location) {
		File locationFile = new File(location);
		if (locationFile.canRead()) {
			return null;
		} else {
			return "Class path does not have read permission";
		}
	}

	private boolean validateData() {
		return isClassnameValid(classNameTxt.getText()) && isClassPathValid(classPathTxt.getText());
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
		corpusMgmtViewform.getMessageManager().removeAllMessages();
		selectedCorpusClass = (CorpusClass) ((IStructuredSelection) selection).getFirstElement();	
		classNameTxt.setText(selectedCorpusClass.getClassName());
		classPathTxt.setText(selectedCorpusClass.getClassPath());
		validateData();
	}

}
