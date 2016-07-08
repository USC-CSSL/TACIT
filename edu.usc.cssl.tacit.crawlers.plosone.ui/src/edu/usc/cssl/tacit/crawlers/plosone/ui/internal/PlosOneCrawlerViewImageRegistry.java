package edu.usc.cssl.tacit.crawlers.plosone.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class PlosOneCrawlerViewImageRegistry {
	private ImageRegistry imgReg = new ImageRegistry();
	
	public static PlosOneCrawlerViewImageRegistry imgIcon;
	
	public ImageDescriptor getImageDescriptor(String key) {
		return imgReg.getDescriptor(key);
	}
	
	public PlosOneCrawlerViewImageRegistry() {
		imgReg.put(IPlosOneCrawlerUIConstants.IMAGE_LRUN_OBJ,ImageDescriptor.createFromFile(PlosOneCrawlerViewImageRegistry.class,"/icons/lrun_obj.gif"));
	
		imgReg.put(IPlosOneCrawlerUIConstants.IMAGE_HELP_CO,ImageDescriptor.createFromFile(PlosOneCrawlerViewImageRegistry.class,"/icons/help_contents.gif"));
	
		imgReg.put(IPlosOneCrawlerUIConstants.IMAGE_PLOSONE_OBJ,ImageDescriptor.createFromFile(PlosOneCrawlerViewImageRegistry.class,"/icons/plosone.png"));
		
	} 
	
	public static PlosOneCrawlerViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new PlosOneCrawlerViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return imgReg.get(imageName);
	}
}
