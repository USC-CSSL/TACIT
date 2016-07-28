package edu.usc.cssl.tacit.common.ui.outputdata;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.MessageDialog;
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
	private boolean checkType = true;
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
		List<String> selectedItems = new ArrayList<String>();
		TreeItem[] children = tree.getItems();
		for(TreeItem ti : children) {
			if(null != ti.getData()) {
				Object classObj = ti.getData();
				
				if (ti.getChecked()) {
					if (classObj instanceof TreeParent) {
						if (((TreeParent) classObj).getCorpusClass() != null) {
							String classPath = ((TreeParent) classObj).getCorpusClass().getClassPath();
							processClasses(classPath, selectedItems);
						} else  // folder, sub folder
							subfolders.put(new File(classObj.toString()).getAbsolutePath(), ti);
					} else {
						String filename = classObj.toString();
						if (!(new File(filename).isDirectory())) {
							
							selectedItems.add(filename);
						}
					}
				}
			}
		}
		return processSubfolders(subfolders, selectedItems);
	}

	
	private void processClasses(String classPath, List<String> selectedItems) {
		File[] fileList = new File(classPath).listFiles();
		if(null == fileList) return;
		for(File f : fileList) {
			if(f.isDirectory()) 
				processClasses(f.getAbsolutePath(), selectedItems);
			else
				selectedItems.add(f.getAbsolutePath());
		}
	}

	public List<String> processSubfolders(Map<String, TreeItem> subfolders, List<String> selectedItems) {		
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
	
	public List<String> getSelectedFiles(boolean corpus) {
		
		Object[] checkedElements = treeViewer.getCheckedElements();
		List<String> files = new ArrayList<String>();
		for (int i = 0; i < checkedElements.length; i++) {
			files.add(checkedElements[i].toString());
		}
		return files;

	}
	
	public List<Object> getTypeCheckedSelectedFiles(boolean check) throws Exception {
		
		//check is passed by the plugins to indicate whether type is to be checked
		//typecheck is stored by TableLayoutData right from the start, and if the user ever indicates that no type check is needed, this flag will be reset.
		
		Object[] checkedElements = treeViewer.getCheckedElements();
		List<Object> files = new ArrayList<Object>();
		for (int i = 0; i < checkedElements.length; i++) {
			if(checkedElements[i] instanceof TreeParent){
				if(((TreeParent)checkedElements[i]).getCorpus() != null) {
					continue;
				}
				if(((TreeParent)checkedElements[i]).getCorpusClass() != null) {
					files.add(((TreeParent)checkedElements[i]).getCorpusClass());
					continue;
				}
			}
			files.add(checkedElements[i].toString());
			String filename = checkedElements[i].toString();
			File file = new File(checkedElements[i].toString());
			if(!file.isDirectory()){
				
				if(checkType && check && !(filename.endsWith(".txt")||filename.endsWith(".rtf")||filename.endsWith(".pdf")))
				{
					MessageDialog dialog = new MessageDialog(null, "Alert", null, "The input contains one or more files with unsupported formats (other than .pdf, .txt, and .rtf ). Hence the analysis may include extraneous information may be included in the analysis with text. To separate text in json files before analysis, see corpus management. Would you like to continue?", MessageDialog.INFORMATION, new String[]{"Cancel","OK"}, 1);
					int result = dialog.open();
					
					//If user selects the cancel button then return
					if (result <= 0){
						dialog.close();
						throw new Exception("User has requested cancel");
					}
					else{
						checkType = false;
					}
				}
				
				
			}
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
