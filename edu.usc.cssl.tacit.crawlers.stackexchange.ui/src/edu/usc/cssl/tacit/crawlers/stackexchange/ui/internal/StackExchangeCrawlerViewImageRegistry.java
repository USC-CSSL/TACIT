package edu.usc.cssl.tacit.crawlers.stackexchange.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class StackExchangeCrawlerViewImageRegistry {

	ImageRegistry imgReg = new ImageRegistry();
	static StackExchangeCrawlerViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return imgReg.getDescriptor(key);
	}

	public StackExchangeCrawlerViewImageRegistry() {

		imgReg.put(IStackExchangeCrawlerUIConstants.IMAGE_LRUN_OBJ,
				ImageDescriptor.createFromFile(
						StackExchangeCrawlerViewImageRegistry.class,
						"/icons/lrun_obj.gif"));

		imgReg.put(IStackExchangeCrawlerUIConstants.IMAGE_HELP_CO,
				ImageDescriptor.createFromFile(
						StackExchangeCrawlerViewImageRegistry.class,
						"/icons/help_contents.gif"));
		
		imgReg.put(IStackExchangeCrawlerUIConstants.IMAGE_STACK_OBJ,
				ImageDescriptor.createFromFile(
						StackExchangeCrawlerViewImageRegistry.class,
						"/icons/stackexchange.png"));
	}

	public static StackExchangeCrawlerViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new StackExchangeCrawlerViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return imgReg.get(imageName);
	}
}
