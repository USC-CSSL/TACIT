package edu.usc.cssl.nlputils.common.ui.outputdata;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TableLayoutData {
	private Composite sectionClient;
	private CheckboxTreeViewer treeViewer;
	
	public Tree getTree(){
		return this.treeViewer.getTree();
	}
	
	public List<String> getSelectedItems(TreeItem tree) {		
		Map<String, TreeItem> subfolders = Collections.synchronizedMap(new LinkedHashMap<String, TreeItem>()); //LinkedHashMap - helps to iterate the key in which it is inserted
		ArrayList<String> selectedItems = new ArrayList<String>();
		TreeItem[] children = tree.getItems();
		for(TreeItem ti : children) {
			if(null != ti.getData()) {
				String filename = ti.getData().toString();				
				if(ti.getChecked()) {
					if(!(new File(filename).isDirectory())) {
						selectedItems.add(filename);
					} else {
						subfolders.put(new File(filename).getAbsolutePath(), ti);
					}
				}
			}
		}
		return processSubfolders(subfolders, selectedItems);
	}
	
	public List<String> processSubfolders(Map<String, TreeItem> subfolders, ArrayList<String> selectedItems) {		
		Iterator<Entry<String, TreeItem>> iterator = subfolders.entrySet().iterator();
		while(iterator.hasNext()) {
		   Entry<String, TreeItem> entry = iterator.next();
		   TreeItem[] children = entry.getValue().getItems();
		   for(TreeItem ti : children) {
				if(null != ti.getData()) {
					String filename = ti.getData().toString();				
					if(ti.getChecked()) {
						if(!(new File(filename).isDirectory())) {
							selectedItems.add(filename);
						}
						else
							subfolders.put(new File(filename).getAbsolutePath(), ti);
					}
				}
			}
		}
		return selectedItems;
	}
	
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
