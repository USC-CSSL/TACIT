package edu.usc.cssl.tacit.common.ui.corpusmanagement;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;

public class TypeOneDetailsPage implements IDetailsPage {
	private IManagedForm mform;

	public void initialize(IManagedForm mform) {
		this.mform = mform;
	}

	public void createContents(Composite parent) {

		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false)
				.applyTo(parent);

		FormToolkit toolkit = mform.getToolkit();

		Section section = toolkit.createSection(parent, Section.DESCRIPTION
				| Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(section);
		section.setText("Corpus Details");
		section.setDescription("Enter the details for the corpus.");

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL
				| SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
				.applyTo(sc);

		Composite sectionClient = toolkit.createComposite(section);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
				.applyTo(sectionClient);
		section.setClient(sectionClient);

		TacitFormComposite.createEmptyRow(toolkit, sectionClient);

		final Label corpusIDLbl = toolkit.createLabel(sectionClient,
				"Corpus ID:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(corpusIDLbl);
		final Text corpusIDTxt = toolkit.createText(sectionClient, "",
				SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0)
				.applyTo(corpusIDTxt);

		final Label rootPathLbl = toolkit.createLabel(sectionClient,
				"Root Path:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(rootPathLbl);
		final Text rootLocationTxt = toolkit.createText(sectionClient, "",
				SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
				.applyTo(rootLocationTxt);
		final Button browseBtn = toolkit.createButton(sectionClient,
				"Browse...", SWT.PUSH);
		browseBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(browseBtn.getShell(),
						SWT.OPEN);
				dlg.setText("Open");
				String path = dlg.open();
				if (path == null)
					return;
				rootLocationTxt.setText(path);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		final Label dataTypeLbl = toolkit.createLabel(sectionClient,
				"Data Type:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(dataTypeLbl);
		Composite dataType = toolkit.createComposite(parent);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2)
				.applyTo(dataType);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1)
				.applyTo(dataType);
		createDataTypeOptions(toolkit, dataType);

		Composite specialInput = toolkit.createComposite(parent);
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(2)
				.applyTo(specialInput);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(specialInput);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		createSpecialOptions(toolkit, specialInput);

	}

	private void createSpecialOptions(FormToolkit toolkit, Composite parent) {

	}

	private void createDataTypeOptions(FormToolkit toolkit, Composite parent) {
		Group buttonComposite = new Group(parent, SWT.LEFT);
		// buttonComposite.setText("Data Type");
		buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		buttonComposite.setLayout(layout);
		buttonComposite.setForeground(parent.getForeground());

		Button plainText = new Button(buttonComposite, SWT.RADIO);
		plainText.setText("Plain Text");
		plainText.setSelection(true);
		plainText.setForeground(parent.getForeground());

		Button jsonData = new Button(buttonComposite, SWT.RADIO);
		jsonData.setText("JSON");
		jsonData.setSelection(false);
		jsonData.setForeground(parent.getForeground());

		Button xmlData = new Button(buttonComposite, SWT.RADIO);
		xmlData.setText("XML");
		xmlData.setSelection(false);
		xmlData.setForeground(parent.getForeground());
		xmlData.setEnabled(false);

		Button wordData = new Button(buttonComposite, SWT.RADIO);
		wordData.setText("Microsoft Word");
		wordData.setSelection(false);
		wordData.setForeground(parent.getForeground());
		wordData.setEnabled(false);

		Label lblEmpty = new Label(buttonComposite, SWT.None);
		TacitFormComposite.createEmptyRow(toolkit, parent);
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

	}

}
