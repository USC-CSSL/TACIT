package edu.usc.cssl.nlputils.common.ui.outputdata;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.widgets.Composite;

public class TableLayoutData {
	private Composite sectionClient;
	private CheckboxTreeViewer treeViewer;
	public List<String> getSelectedFiles() {

		Object[] checkedElements = treeViewer.getCheckedElements();
		List<String> files = new ArrayList<String>();
		for (int i = 0; i < checkedElements.length; i++) {
			files.add((String) checkedElements[i]);
		}
		return files;

	}
	public void setTreeViewer(CheckboxTreeViewer treeViewer) {
		this.treeViewer = (treeViewer);
	}

	/**
	 * @return the sectionClient
	 */
	public Composite getSectionClient() {
		return sectionClient;
	}

	/**
	 * @param sectionClient
	 *            the sectionClient to set
	 */
	public void setSectionClient(Composite sectionClient) {
		this.sectionClient = sectionClient;
	}
}
