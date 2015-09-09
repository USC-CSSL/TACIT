package edu.usc.cssl.tacit.common.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class TacitElementSelectionDialog extends ElementListSelectionDialog {
	public TacitElementSelectionDialog(Shell parent) {
		super(parent, new ArrayLabelProvider());
	}
	
	public void refresh(Object[] elements){
		setListElements(elements);
	}
	
	static class ArrayLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return (String) element;
		}
	}
	
	public String[] getSelections() {
	
		Object[] result = this.getResult();
		List<String> selectedElements = new ArrayList<String>();
		for (Object object : result) {
			selectedElements.add((String) object);
		}
		Collections.sort(selectedElements);
		return selectedElements.toArray(new String[selectedElements.size()]);//return string array of selected elements
	}
}
