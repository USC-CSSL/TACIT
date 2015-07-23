package edu.usc.cssl.tacit.crawlers.supremecourt.ui.internal;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Shell;

public class SupremeCourtCaseListDialog extends ElementListSelectionDialog {

	public SupremeCourtCaseListDialog(Shell parent, ILabelProvider renderer) {
		super(parent, renderer);
	}
	
	public void refresh(Object[] elements){
		setListElements(elements);
	}

}
