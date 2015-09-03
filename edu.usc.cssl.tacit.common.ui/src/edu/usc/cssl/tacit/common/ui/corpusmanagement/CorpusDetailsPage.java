package edu.usc.cssl.tacit.common.ui.corpusmanagement;

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

import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.Corpus;

public class CorpusDetailsPage implements IDetailsPage {
	private IManagedForm mform;
	private Text corpusIDTxt;
	private Corpus selectedCorpus;
	private ScrolledForm form;
	FormToolkit toolkit;
	
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
		
		//TacitFormComposite.createEmptyRow(toolkit, dataTypes);
		createDataTypeOptions(dataTypes);
		//TacitFormComposite.createEmptyRow(toolkit, dataTypes);
		
		//Add save button
		Composite buttonComposite = new Composite(sectionClient, SWT.NONE);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.marginWidth = buttonLayout.marginHeight = 0;
		buttonComposite.setLayout(buttonLayout);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0).applyTo(buttonComposite);
		
		Button saveCorpora = new Button(buttonComposite, SWT.PUSH);
		saveCorpora.setText("Save Corpora");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(saveCorpora);		
		
		saveCorpora.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validateData();
				selectedCorpus.getViewer().refresh();
			}
		});	
		toolkit.paintBordersFor(mform.getForm().getForm().getBody());
		
		corpusIDTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				selectedCorpus.setCorpusId(corpusIDTxt.getText());
				//selectedCorpus.getViewer().refresh();
				//printCorpusDetails();
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				selectedCorpus.setCorpusId(corpusIDTxt.getText());
				//selectedCorpus.getViewer().refresh();
				//printCorpusDetails();
			}
		});
		
	}
	
	public void printCorpusDetails() {
		System.out.println("Corupus ID :" + selectedCorpus.getCorpusId());
		for(ICorpusClass cc : selectedCorpus.getClasses()) {
			System.out.println("Classes " + cc.getClassName() + "," + cc.getClassPath());
		}
	}
	
	public boolean validateData() {
		String corpusId = corpusIDTxt.getText();
		if(corpusId.isEmpty()) {
			mform.getForm().getForm().getMessageManager().addMessage("corpusId", "Provide valid corpus ID", null, IMessageProvider.ERROR);
			return false;
		}
		return true;
	}
	
	private void createDataTypeOptions(Composite dataTypeGroup) {
		Button plainText = new Button(dataTypeGroup, SWT.RADIO);
		plainText.setText("Plain Text");
		plainText.setSelection(true);

		Button twitterJSON = new Button(dataTypeGroup, SWT.RADIO);
		twitterJSON.setText("Twitter JSON");
		twitterJSON.setSelection(false);
		
		Button redditJson = new Button(dataTypeGroup, SWT.RADIO);
		redditJson.setText("Reddit JSON");
		redditJson.setSelection(false);

		Button xmlData = new Button(dataTypeGroup, SWT.RADIO);
		xmlData.setText("XML");
		xmlData.setSelection(false);
		xmlData.setEnabled(false);

		Button wordData = new Button(dataTypeGroup, SWT.RADIO);
		wordData.setText("Microsoft Word");
		wordData.setSelection(false);
		wordData.setEnabled(false);
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
		selectedCorpus = (Corpus) ((IStructuredSelection) selection).getFirstElement();	
		corpusIDTxt.setText(selectedCorpus.getCorpusId());
	}

}
