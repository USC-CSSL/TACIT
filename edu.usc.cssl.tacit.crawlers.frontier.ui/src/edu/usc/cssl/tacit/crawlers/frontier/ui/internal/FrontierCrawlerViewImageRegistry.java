package edu.usc.cssl.tacit.crawlers.frontier.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class FrontierCrawlerViewImageRegistry {

	ImageRegistry imgReg = new ImageRegistry();
	static FrontierCrawlerViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return imgReg.getDescriptor(key);
	}

	public FrontierCrawlerViewImageRegistry() {

		imgReg.put(IFrontierCrawlerUIConstants.IMAGE_LRUN_OBJ,
				ImageDescriptor.createFromFile(
						FrontierCrawlerViewImageRegistry.class,
						"/icons/lrun_obj.gif"));

		imgReg.put(IFrontierCrawlerUIConstants.IMAGE_HELP_CO,
				ImageDescriptor.createFromFile(
						FrontierCrawlerViewImageRegistry.class,
						"/icons/help_contents.gif"));
		
		imgReg.put(IFrontierCrawlerUIConstants.IMAGE_STACK_OBJ,
				ImageDescriptor.createFromFile(
						FrontierCrawlerViewImageRegistry.class,
						"/icons/stackexchange.png"));
	}

	public static FrontierCrawlerViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new FrontierCrawlerViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return imgReg.get(imageName);
	}
}
