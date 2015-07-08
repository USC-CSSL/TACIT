package edu.usc.cssl.tacit.crawlers.senate.ui;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Shell;

import edu.usc.cssl.tacit.crawlers.senate.ui.internal.ElementListSelectionDialog;

public class SenatorListDialog extends ElementListSelectionDialog {

	public SenatorListDialog(Shell parent, ILabelProvider renderer) {
		super(parent, renderer);
	}
	
	public void refresh(Object[] elements){
		setListElements(elements);
	}
}
