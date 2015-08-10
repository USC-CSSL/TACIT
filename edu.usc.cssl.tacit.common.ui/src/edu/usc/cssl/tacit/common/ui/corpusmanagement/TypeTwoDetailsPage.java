package edu.usc.cssl.tacit.common.ui.corpusmanagement;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.widgets.*;

/**
 * @author dejan
 *
 *         To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Generation - Code and Comments
 */
public class TypeTwoDetailsPage implements IDetailsPage {
	private IManagedForm mform;
	private TypeTwo input;
	private Button flag1;
	private Button flag2;

	public TypeTwoDetailsPage() {
	}

	public void initialize(IManagedForm mform) {
		this.mform = mform;
	}

	public void createContents(Composite parent) {
		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = 5;
		layout.leftMargin = 5;
		layout.rightMargin = 2;
		layout.bottomMargin = 2;
		parent.setLayout(layout);

		FormToolkit toolkit = mform.getToolkit();
		Section s1 = toolkit.createSection(parent, Section.DESCRIPTION
				| Section.TITLE_BAR);
		s1.marginWidth = 10;
		TableWrapData td = new TableWrapData(TableWrapData.FILL,
				TableWrapData.TOP);
		td.grabHorizontal = true;
		s1.setLayoutData(td);
		Composite client = toolkit.createComposite(s1);
		GridLayout glayout = new GridLayout();
		glayout.marginWidth = glayout.marginHeight = 0;
		client.setLayout(glayout);

		flag1 = toolkit.createButton(client, "tete", SWT.CHECK); //$NON-NLS-1$

		s1.setClient(client);
	}

	private void update() {

	}

	public void selectionChanged(IFormPart part, ISelection selection) {
		IStructuredSelection ssel = (IStructuredSelection) selection;
		if (ssel.size() == 1) {
			input = (TypeTwo) ssel.getFirstElement();
		} else
			input = null;
		update();
	}

	public void commit(boolean onSave) {
	}

	public void setFocus() {
		flag1.setFocus();
	}

	public void dispose() {
	}

	public boolean isDirty() {
		return false;
	}

	public boolean isStale() {
		return false;
	}

	public void refresh() {
		update();
	}

	public boolean setFormInput(Object input) {
		return false;
	}
}
