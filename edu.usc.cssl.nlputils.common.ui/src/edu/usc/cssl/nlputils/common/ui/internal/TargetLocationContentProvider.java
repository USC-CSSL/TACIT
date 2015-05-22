package edu.usc.cssl.nlputils.common.ui.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TargetLocationContentProvider implements ITreeContentProvider {

	 /**
	   * Gets the children of the specified object
	   * 
	   * @param arg0
	   *            the parent object
	   * @return Object[]
	   */
	  public Object[] getChildren(Object arg0) {
	    return getElements(arg0);
	  }

	  /**
	   * Gets the parent of the specified object
	   * 
	   * @param arg0
	   *            the object
	   * @return Object
	   */
	  public Object getParent(Object arg0) {
	    return null;
	  }

	  /**
	   * Returns whether the passed object has children
	   * 
	   * @param arg0
	   *            the parent object
	   * @return boolean
	   */
	  public boolean hasChildren(Object arg0) {
	  
		// Get the children
		    Object[] obj = getChildren(arg0);

		    // Return whether the parent has children
		    return obj == null ? false : obj.length > 0;
	  }
	  

	  /**
	   * Gets the root element(s) of the tree
	   * 
	   * @param arg0
	   *            the input data
	   * @return Object[]
	   */
	  public Object[] getElements(Object arg0) {
		  List elementList = new ArrayList<String>();
		  if(arg0 instanceof String){
			  File locationObj = new File((String) arg0);
			  if(locationObj.isDirectory()){
				  File[] listFiles = locationObj.listFiles();
				  for (File file : listFiles) {
					elementList.add(file.toString());
				}
			  }
		  }
			  else {
				  if(arg0 instanceof List){
					  elementList.addAll((List) arg0);
				  }
				  else{
				  elementList.add(arg0);
				  }
			  }
		  
		  return elementList.toArray();
		  
	  }

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		
		
	}

}
