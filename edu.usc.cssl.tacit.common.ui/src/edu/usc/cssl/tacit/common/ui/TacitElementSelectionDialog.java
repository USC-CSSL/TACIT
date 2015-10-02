package edu.usc.cssl.tacit.common.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
			return  element.toString();
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
	
	public Set<Object> getSelectionObjects() {
		
		Object[] result = this.getResult();
		Set<Object> mySet = new HashSet<Object>(Arrays.asList(result));
        return mySet;
	}
}
