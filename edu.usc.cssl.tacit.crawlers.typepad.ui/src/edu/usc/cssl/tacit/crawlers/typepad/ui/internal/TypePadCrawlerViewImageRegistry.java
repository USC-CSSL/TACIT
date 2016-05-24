package edu.usc.cssl.tacit.crawlers.typepad.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class TypePadCrawlerViewImageRegistry {
	
	private ImageRegistry imgReg = new ImageRegistry();
	
	public static TypePadCrawlerViewImageRegistry imgIcon;
	
	public ImageDescriptor getImageDescriptor(String key) {
		return imgReg.getDescriptor(key);
	}
	
	public TypePadCrawlerViewImageRegistry() {
		imgReg.put(ITypePadCrawlerUIConstants.IMAGE_LRUN_OBJ,ImageDescriptor.createFromFile(TypePadCrawlerViewImageRegistry.class,"/icons/lrun_obj.gif"));
	
		imgReg.put(ITypePadCrawlerUIConstants.IMAGE_HELP_CO,ImageDescriptor.createFromFile(TypePadCrawlerViewImageRegistry.class,"/icons/help_contents.gif"));
	
		imgReg.put(ITypePadCrawlerUIConstants.IMAGE_TYPEPAD_OBJ,ImageDescriptor.createFromFile(TypePadCrawlerViewImageRegistry.class,"/icons/typepad.png"));
		
	} 
	
	public static TypePadCrawlerViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new TypePadCrawlerViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return imgReg.get(imageName);
	}
	

}
