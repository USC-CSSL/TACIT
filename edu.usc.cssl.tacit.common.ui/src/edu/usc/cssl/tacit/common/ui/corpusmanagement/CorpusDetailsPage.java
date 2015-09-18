package edu.usc.cssl.tacit.common.ui.corpusmanagement;

import java.io.File;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

//import edu.usc.cssl.tacit.common.corpusmanagement;
import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.Corpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.DataType;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;

public class CorpusDetailsPage implements IDetailsPage {
	private IManagedForm mform;
	private Text corpusIDTxt;
	private Corpus selectedCorpus;
	private ScrolledForm corpusMgmtViewform;
	FormToolkit toolkit;
	private Button plainText = null;
	private Button twitterJSON = null;
	private Button redditJSON = null;
	private Button xmlData = null;
	private Button wordData = null;
	
	ManageCorpora corpusManagement;
	List<ICorpus> corpusList;
	public CorpusDetailsPage(ScrolledForm corpusMgmtViewform, List<ICorpus> corpusList) {
		this.corpusMgmtViewform = corpusMgmtViewform;
		corpusManagement = new ManageCorpora();
		corpusList = this.corpusList;
	}

	@Override
	public void initialize(IManagedForm mform) {
		this.mform = mform;
	}

	@Override
	public void createContents(Composite parent) {
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true).applyTo(parent);
		toolkit = mform.getToolkit();
		//form = toolkit.createScrolledForm(parent);
		//toolkit.decorateFormHeading(form.getForm());
		//GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true).applyTo(form.getBody());
		//GridDataFactory.fillDefaults().grab(true, true).span(1, 1).applyTo(form.getBody());
		
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(section);
		section.setText("Corpus Details");
		section.setDescription("Enter the details for the corpus.");

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

		final Label corpusIDLbl = toolkit.createLabel(sectionClient, "Corpus ID:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(corpusIDLbl);
		corpusIDTxt = toolkit.createText(sectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(corpusIDTxt);
		if(null != selectedCorpus) corpusIDTxt.setText(selectedCorpus.getCorpusId());

		Group dataTypes = new Group(sectionClient, SWT.LEFT);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true).applyTo(dataTypes);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(dataTypes);
		dataTypes.setText("Data Type");
		
		createDataTypeOptions(dataTypes);
		if(null != selectedCorpus) {
			corpusIDTxt.setText(selectedCorpus.getCorpusId());
		}
		
		//Add save button
		Composite buttonComposite = new Composite(sectionClient, SWT.NONE);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.marginWidth = buttonLayout.marginHeight = 0;
		buttonComposite.setLayout(buttonLayout);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0).applyTo(buttonComposite);
		
		Button saveCorpus = new Button(buttonComposite, SWT.PUSH);
		saveCorpus.setText("Save Corpus");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(saveCorpus);		
		
		saveCorpus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(validateData(corpusIDTxt.getText())) {
					if(null != selectedCorpus) selectedCorpus.getViewer().refresh();
					ManageCorpora.saveCorpus(selectedCorpus);
				}
			}
		});	
		toolkit.paintBordersFor(mform.getForm().getForm().getBody());
		
		corpusIDTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				if(isCorpusIdValid(corpusIDTxt.getText())) {
					selectedCorpus.setCorpusId(corpusIDTxt.getText());
					selectedCorpus.getViewer().refresh();
				}
				
				if(!corpusIDTxt.getText().isEmpty())
					corpusMgmtViewform.getMessageManager().removeMessage("corpusIdEmpty");
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(isCorpusIdValid(corpusIDTxt.getText())) {
					selectedCorpus.setCorpusId(corpusIDTxt.getText());
					selectedCorpus.getViewer().refresh();
				}
				
				if(!corpusIDTxt.getText().isEmpty())
					corpusMgmtViewform.getMessageManager().removeMessage("corpusIdEmpty");
			}
		});
		
	}
	
	protected boolean isCorpusIdValid(String corpusId) {
		if(corpusId.isEmpty()) {
			corpusMgmtViewform.getMessageManager().addMessage("corpusIdEmpty", "Provide valid corpus ID", null, IMessageProvider.ERROR);
			return false;
		} else 
			corpusMgmtViewform.getMessageManager().removeMessage("corpusIdEmpty");
		
		if(corpusIdExists(corpusId)) {
			corpusMgmtViewform.getMessageManager().addMessage("corpusId", "Corpus ID \""+ corpusId +"\"already exists. Provide different ID", null, IMessageProvider.ERROR);
			return false;
		} else 
			corpusMgmtViewform.getMessageManager().removeMessage("corpusId");
		
		return true;	
	}

	public void printCorpusDetails() {
		System.out.println("Corupus ID :" + selectedCorpus.getCorpusId());
		for(ICorpusClass cc : selectedCorpus.getClasses()) {
			System.out.println("Classes " + cc.getClassName() + "," + cc.getClassPath());
		}
	}
	
	public boolean validateData(String corpusId) {
		if(isCorpusIdValid(corpusId)) {
			List<ICorpusClass> classes = selectedCorpus.getClasses(); // validate the class details as well
			for(ICorpusClass cc : classes) {
				if(!validateClassData(cc)) return false;
			}
		} else 
			return false; // corpusId is not valid
		return true;
	}
	
	
	private boolean isClassnameValid(String className, ICorpusClass selectedCorpusClass) {
		if(className.isEmpty()) {
			corpusMgmtViewform.getMessageManager().addMessage("classNameEmpty", "Class name cannot be empty", null, IMessageProvider.ERROR);
			return false;
		} else {
			corpusMgmtViewform.getMessageManager().removeMessage("classNameEmpty");
		}
		String parentCorpusId = selectedCorpusClass.getParentId();
		ICorpus parentCorpus = corpusManagement.readCorpusById(parentCorpusId);
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

	private boolean validateClassData(ICorpusClass cc) {
		return isClassnameValid(cc.getClassName(), cc) && isClassPathValid(cc.getClassPath());
	}	

	private boolean corpusIdExists(String corpusId) {
		List<ICorpus> corpuses = corpusManagement.getAllCorpusDetails();
		for(ICorpus corpus : corpuses) {
			if(!corpus.equals(selectedCorpus) && corpus.getCorpusId().equals(corpusId)) return true;
		}
		return false;
	}

	private void createDataTypeOptions(Composite dataTypeGroup) {
		plainText = toolkit.createButton(dataTypeGroup, "Plain Text", SWT.RADIO);
		plainText.addSelectionListener(new SelectionAdapter(){
		    @Override
			public void widgetSelected(final SelectionEvent e){
		        super.widgetSelected(e);
		        if(plainText.getSelection()){
		            selectedCorpus.setDataType(DataType.PLAIN_TEXT);
		        }
		    }
		});
		plainText.setSelection(true);

		twitterJSON = toolkit.createButton(dataTypeGroup, "Twitter JSON", SWT.RADIO);
		twitterJSON.addSelectionListener(new SelectionAdapter(){
		    @Override
			public void widgetSelected(final SelectionEvent e){
		        super.widgetSelected(e);
		        if(twitterJSON.getSelection()){
		            selectedCorpus.setDataType(DataType.TWITTER_JSON);
		        }
		    }
		});
		twitterJSON.setSelection(false);
		
		redditJSON = toolkit.createButton(dataTypeGroup, "Reddit JSON", SWT.RADIO);
		redditJSON.setSelection(false);
		redditJSON.addSelectionListener(new SelectionAdapter(){
		    @Override
			public void widgetSelected(final SelectionEvent e){
		        super.widgetSelected(e);
		        if(redditJSON.getSelection()){
		            selectedCorpus.setDataType(DataType.REDDIT_JSON);
		        }
		    }
		});
		
		xmlData = toolkit.createButton(dataTypeGroup, "XML", SWT.RADIO);
		xmlData.setSelection(false);
		xmlData.addSelectionListener(new SelectionAdapter(){
		    @Override
			public void widgetSelected(final SelectionEvent e){
		        super.widgetSelected(e);
		        if(xmlData.getSelection()){
		            selectedCorpus.setDataType(DataType.XML);
		        }
		    }
		});
		xmlData.setEnabled(false);
		
		wordData = toolkit.createButton(dataTypeGroup, "Microsoft Word", SWT.RADIO);
		wordData.setSelection(false);
		wordData.addSelectionListener(new SelectionAdapter(){
		    @Override
			public void widgetSelected(final SelectionEvent e){
		        super.widgetSelected(e);
		        if(wordData.getSelection()){
		            selectedCorpus.setDataType(DataType.MICROSOFT_WORD);
		        }
		    }
		});
		wordData.setEnabled(false);
		
	}
	
	private void setDataTypeOption(DataType type) {
		plainText.setSelection(false);
		twitterJSON.setSelection(false);
		redditJSON.setSelection(false);
		xmlData.setSelection(false);
		wordData.setSelection(false);
		if(null == type) return;
		switch(type) { 
				case PLAIN_TEXT: plainText.setSelection(true);
								 break;
				case TWITTER_JSON: twitterJSON.setSelection(true);
								 break;
				case REDDIT_JSON: redditJSON.setSelection(true);
								 break;
				case XML: xmlData.setSelection(true);
								 break;
				case MICROSOFT_WORD: wordData.setSelection(true);
								 break;
		}
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
		selectedCorpus = (Corpus) ((IStructuredSelection) selection).getFirstElement();	
		corpusIDTxt.setText(selectedCorpus.getCorpusId());
		setDataTypeOption(selectedCorpus.getDatatype());
		validateData(selectedCorpus.getCorpusId());
	}

}
