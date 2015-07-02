package edu.usc.cssl.tacit.crawlers.supremecourt.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class SupremeCrawlerImageRegistry {

	ImageRegistry ir = new ImageRegistry();
	static SupremeCrawlerImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}

	private SupremeCrawlerImageRegistry() {
		
		ir.put(ISupremeCrawlerUIConstants.IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(SupremeCrawlerImageRegistry.class, "/icons/lrun_obj.gif"));
		
		ir.put(ISupremeCrawlerUIConstants.IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(SupremeCrawlerImageRegistry.class, "/icons/help_contents.gif"));


	}

	public static SupremeCrawlerImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new SupremeCrawlerImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return ir.get(imageName);
	}

}
