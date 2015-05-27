package edu.usc.cssl.nlputils.classifiy.svm.ui;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.nlputils.classify.svm.ui.internal.ISVMViewConstants;

public class SVMView extends ViewPart implements ISVMViewConstants {

	private FormToolkit toolkit;
	private ScrolledForm form;
	public static String ID = "edu.usc.cssl.nlputils.classify.svm.ui.view1";
	@Override
	public void createPartControl(Composite parent) {
		toolkit = createFormBodySection(parent);
		
	}

	private FormToolkit createFormBodySection(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);

		toolkit.decorateFormHeading(form.getForm());
		form.setText("SVM Classifier"); //$NON-NLS-1$
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true)
				.applyTo(form.getBody());
		return toolkit;
	}
	
	@Override
	public void setFocus() {
		
	}

}
