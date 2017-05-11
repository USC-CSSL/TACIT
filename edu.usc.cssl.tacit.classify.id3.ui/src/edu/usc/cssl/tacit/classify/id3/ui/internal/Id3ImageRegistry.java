package edu.usc.cssl.tacit.classify.id3.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class Id3ImageRegistry {

	ImageRegistry ir = new ImageRegistry();
	static Id3ImageRegistry imgIcon;
	
	//Returns the descriptor associated with the given key in this registry, or null if none.
	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}
	
	private Id3ImageRegistry(){
		
		
		ir.put(IId3ViewConstants.IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(Id3ImageRegistry.class, "/icons/lrun_obj.gif"));
		
		ir.put(IId3ViewConstants.IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(Id3ImageRegistry.class, "/icons/help_contents.gif"));

		ir.put(IId3ViewConstants.IMAGE_ID3_TITLE, ImageDescriptor
				.createFromFile(Id3ImageRegistry.class, "/icons/TACITId3Icon.png"));
	}
	
	public static Id3ImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new Id3ImageRegistry();
		}
		return imgIcon;

	}

	
	public Image getImage(String imageName) {
		return ir.get(imageName);
	}
	
}


/* This file handles creation of images from the gif files we provide and allocating OS resources for image to get displayed */