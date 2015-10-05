package edu.usc.cssl.tacit.common.ui.outputdata;

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

import edu.usc.cssl.tacit.common.ui.internal.TargetLocationsGroup;
import edu.usc.cssl.tacit.common.ui.internal.TreeParent;

public class TableLayoutData {
	private Composite sectionClient;
	private CheckboxTreeViewer treeViewer;
	private TargetLocationsGroup lgroup;
	public Tree getTree(){
		return this.treeViewer.getTree();
	}
	
	public void setTargetLocationGroups(TargetLocationsGroup tgroup){
		this.lgroup = tgroup;
	}
	
	public void refreshInternalTree(List<String> files){
		if(this.lgroup!= null){
			this.lgroup.updateLocationTree(files.toArray(new String[files.size()]));
		
		}
	}
	/*
	 * To maintain class and its selected files association
	 */
	public List<String> getSelectedItems(TreeItem tree) {		
		Map<String, TreeItem> subfolders = Collections.synchronizedMap(new LinkedHashMap<String, TreeItem>()); //LinkedHashMap - helps to iterate the key in which it is inserted
		ArrayList<String> selectedItems = new ArrayList<String>();
		TreeItem[] children = tree.getItems();
		for(TreeItem ti : children) {
			if(null != ti.getData()) {
				String filename = ti.getData().toString();
				if(ti.getChecked()) {
					if(!new File(filename).exists()) { // corpus class
						String classPath = getCorpusClass(tree.getData().toString(), filename);
						if(null != classPath)
							//subfolders.put(classPath, ti);
							selectedItems.add(classPath);
					} 
					else if(!(new File(filename).isDirectory())) {
						selectedItems.add(filename);
					}
					else {
						subfolders.put(new File(filename).getAbsolutePath(), ti);
					}
				}
			}
		}
		return processSubfolders(subfolders, selectedItems);
	}

	/*
	 * Returns the corpus class path of the given corpus class string and its parent
	 */
	private String getCorpusClass(String parent, String filename) {
		Object[] checkedElements = treeViewer.getCheckedElements();
		for (int i = 0; i < checkedElements.length; i++) {
			if(checkedElements[i].toString().equals(parent)){ // its in the order as displayed in the tree
				for(int j = i+1; j<checkedElements.length; j++)
					if(checkedElements[j].toString().equals(filename))
						return ((TreeParent)checkedElements[j]).getCorpusClass().getClassPath();
			}
		}
		return null;
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
			if(checkedElements[i] instanceof TreeParent){
				if(((TreeParent)checkedElements[i]).getCorpus() != null) {
					continue;
				}
				if(((TreeParent)checkedElements[i]).getCorpusClass() != null) {
					files.add(((TreeParent)checkedElements[i]).getCorpusClass().getClassPath());
					continue;
				}
			}
			files.add(checkedElements[i].toString());
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
