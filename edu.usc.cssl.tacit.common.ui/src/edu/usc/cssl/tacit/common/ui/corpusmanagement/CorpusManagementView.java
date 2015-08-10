package edu.usc.cssl.tacit.common.ui.corpusmanagement;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormText;
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
		toolkit = createFormBodySection(parent);
		Section section = toolkit.createSection(form.getBody(),
				Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1)
				.applyTo(section);
		section.setExpanded(true);

		String description = "This sections lets you manage existing corpora and create new ones";
		FormText descriptionFrm = toolkit.createFormText(section, false);
		descriptionFrm.setText("<form><p>" + description + "</p></form>", true,
				false);
		section.setDescriptionControl(descriptionFrm);

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL
				| SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
				.applyTo(sc);

		TacitFormComposite.createEmptyRow(toolkit, sc);

		Composite client1 = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(4)
				.applyTo(client1);
		GridDataFactory.fillDefaults().grab(true, false).span(4, 1)
				.applyTo(client1);

		Section formData = toolkit.createSection(client1, Section.TITLE_BAR
				| Section.EXPANDED | Section.DESCRIPTION);
		GridDataFactory.fillDefaults().grab(true, false).span(4, 1)
				.applyTo(formData);
		GridLayoutFactory.fillDefaults().numColumns(4).applyTo(formData);
		formData.setText("Corpus Information");
		formData.setDescription("Enter the relevant information for the corpus");

		ScrolledForm blocksc = toolkit.createScrolledForm(formData);
		blocksc.setExpandHorizontal(true);
		blocksc.setExpandVertical(true);
		IManagedForm form1 = new ManagedForm(toolkit, blocksc);
		block = new MasterDetailsPage();
		block.createContent(form1, client1);

	}

	private FormToolkit createFormBodySection(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);

		toolkit.decorateFormHeading(form.getForm());
		form.setText("Corpus Management");
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true)
				.applyTo(form.getBody());
		return toolkit;
	}

	@Override
	public void setFocus() {

	}

}
