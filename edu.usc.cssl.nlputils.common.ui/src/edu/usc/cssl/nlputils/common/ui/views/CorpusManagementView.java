package edu.usc.cssl.nlputils.common.ui.views;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.nlputils.common.ui.composite.from.NlputilsFormComposite;

public class CorpusManagementView extends ViewPart {

	public static final String ID = "edu.usc.cssl.nlputils.common.ui.corpusmanagement.view1";
	private ScrolledForm form;
	private FormToolkit toolkit;
	
	@Override
	public void createPartControl(Composite parent) {
		toolkit = createFormBodySection(parent);
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(section);
		section.setExpanded(true);
		
		String description = "This sections lets you manage existing corpora and create new ones";
		FormText descriptionFrm = toolkit.createFormText(section, false);
		descriptionFrm.setText("<form><p>" + description + "</p></form>", true,false);
		section.setDescriptionControl(descriptionFrm);

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);

		NlputilsFormComposite.createEmptyRow(toolkit, sc);
		
		Composite client1 = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(4).applyTo(client1);
		GridDataFactory.fillDefaults().grab(true, false).span(4, 1).applyTo(client1);

		Section formData = toolkit.createSection(client1, Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);
		GridDataFactory.fillDefaults().grab(true, false).span(4, 1).applyTo(formData);
		GridLayoutFactory.fillDefaults().numColumns(4).applyTo(formData);
		formData.setText("Corpus Information");
		formData.setDescription("Enter the relevant information for the corpus");
		
		ScrolledComposite formDatasc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
		formDatasc.setExpandHorizontal(true);
		formDatasc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(false).applyTo(formDatasc);
		
		//layoutData = NlputilsFormComposite.createOutputSection(toolkit,client1, form.getMessageManager());
		//Composite output = layoutData.getSectionClient();
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
	
//	public static OutputLayoutData createInputSection(FormToolkit toolkit,
//			Composite parent, final IMessageManager mmng) {
//		Section section = toolkit.createSection(parent, Section.TITLE_BAR
//				| Section.EXPANDED | Section.DESCRIPTION);
//
//		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
//				.applyTo(section);
//		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(section);
//		section.setText("Input Path "); //$NON-NLS-1$
//		section.setDescription("Choose input path for storing the results");
//
//		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL
//				| SWT.V_SCROLL);
//		sc.setExpandHorizontal(true);
//		sc.setExpandVertical(true);
//
//		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
//				.applyTo(sc);
//
//		Composite sectionClient = toolkit.createComposite(section);
//		sc.setContent(sectionClient);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
//		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
//				.applyTo(sectionClient);
//		section.setClient(sectionClient);
//
//		createEmptyRow(toolkit, sectionClient);
//
//		final Label outputPathLbl = toolkit.createLabel(sectionClient,
//				"Input Location:", SWT.NONE);
//		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
//				.applyTo(outputPathLbl);
//		final Text outputLocationTxt = toolkit.createText(sectionClient, "",
//				SWT.BORDER);
//		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
//				.applyTo(outputLocationTxt);
//
//		final Button browseBtn = toolkit.createButton(sectionClient, "Browse...",
//				SWT.PUSH);
//		browseBtn.addSelectionListener(new SelectionListener() {
//
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				DirectoryDialog dlg = new DirectoryDialog(browseBtn.getShell(),
//						SWT.OPEN);
//				dlg.setText("Open");
//				String path = dlg.open();
//				if (path == null)
//					return;
//				outputLocationTxt.setText(path);
//			}
//
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//		});
//
//		outputLocationTxt.addFocusListener(new FocusAdapter() {
//
//			@Override
//			public void focusLost(FocusEvent e) {
//				super.focusLost(e);
//				String message = OutputPathValidation.getInstance()
//						.validateOutputDirectory(outputLocationTxt.getText(),"Input");
//				if (message != null) {
//
//					message = outputPathLbl.getText() + " " + message;
//					mmng.addMessage("inputlocation", message, null,
//							IMessageProvider.ERROR);
//				} else {
//					mmng.removeMessage("inputlocation");
//				}
//			}
//		});
//
//		OutputLayoutData layoutData = new OutputLayoutData();
//		layoutData.setOutputLabel(outputLocationTxt);
//		layoutData.setSectionClient(sectionClient);
//
//		return layoutData;
//	}

	
	@Override
	public void setFocus() {
		
	}

}
