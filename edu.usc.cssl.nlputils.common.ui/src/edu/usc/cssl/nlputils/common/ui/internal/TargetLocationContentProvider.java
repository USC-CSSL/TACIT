package edu.usc.cssl.nlputils.common.ui.internal;

import java.util.ArrayList;
import java.util.List;

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
		  List<Object> elements = new ArrayList<Object>();
		  if(arg0 instanceof List){
			  List<Object> elementList = (List<Object>) arg0;
			  elements.addAll(elementList);
		  }
			  else {
				  if(arg0 instanceof TreeParent){
					  TreeParent parent = (TreeParent) arg0;
					  for (String file : parent.getFiles()) {
						  elements.add(file);
					}
					  for(TreeParent child : parent.getFolder()){
						  elements.add(child);
					  }
				  }
				 
			  }
		  
		  return elements.toArray();
		  
	  }

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return this.equals(obj);
	}
}
