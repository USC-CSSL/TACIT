package edu.usc.cssl.nlputils.common.ui.internal;

import java.io.File;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import edu.usc.cssl.nlputils.common.ui.utility.INlpCommonUiConstants;
import edu.usc.cssl.nlputils.common.ui.utility.IconRegistry;

public class TargetLocationLabelProvider implements  ILabelProvider{

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

	  public Image getImage(Object arg0) {
		  if(arg0 instanceof String){
			  File locationObj = new File((String) arg0);
			  if(locationObj.isDirectory()){
				  return IconRegistry.getImageIconFactory().getImage(INlpCommonUiConstants.FLDR_OBJ);
				
			  }	
		  }
		  return IconRegistry.getImageIconFactory().getImage(INlpCommonUiConstants.FILE_OBJ);
			  
	  }

	@Override
	public String getText(Object element) {
		return (String) element;
	}
	
	


}