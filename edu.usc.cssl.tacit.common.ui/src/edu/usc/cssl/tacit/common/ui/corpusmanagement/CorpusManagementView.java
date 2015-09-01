package edu.usc.cssl.tacit.common.ui.corpusmanagement;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;

public class CorpusManagementView extends ViewPart {

	public static final String ID = "edu.usc.cssl.tacit.common.ui.corpusmanagement.view1";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private MasterDetailsPage block;

	@Override
	public void createPartControl(Composite parent) {
		toolkit = createFormBodySection(parent, "Corpus Management");
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(section);
		section.setExpanded(true);
		//set image for the corpus management plugin using form.setImage
		
		// Create a composite to hold the other widgets
		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(sc);
	
		// Creates an empty to create a empty space
		TacitFormComposite.createEmptyRow(toolkit, sc);

		Composite corpusClient = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(2).applyTo(corpusClient);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(corpusClient);

		Section formData = toolkit.createSection(corpusClient, Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(formData);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(formData);
		formData.setText("Corpus Information");
		//formData.setDescriptionControl(descriptionControl);;

		ScrolledForm blocksc = toolkit.createScrolledForm(formData);
		blocksc.setExpandHorizontal(true);
		blocksc.setExpandVertical(true);
		IManagedForm managedForm = new ManagedForm(toolkit, blocksc);
		block = new MasterDetailsPage();
		block.createContent(managedForm, corpusClient);
	}


	private FormToolkit createFormBodySection(Composite parent, String title) {
		// Every interface requires a toolkit(Display) and form to store the components
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(form.getForm());
		form.setText(title);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true).applyTo(form.getBody());
		return toolkit;
	}

	@Override
	public void setFocus() {
		form.setFocus();
	}
}
