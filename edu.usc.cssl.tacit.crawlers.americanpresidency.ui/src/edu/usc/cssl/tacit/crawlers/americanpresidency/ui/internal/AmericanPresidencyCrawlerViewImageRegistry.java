package edu.usc.cssl.tacit.crawlers.americanpresidency.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class AmericanPresidencyCrawlerViewImageRegistry {

	ImageRegistry imgReg = new ImageRegistry();
	static AmericanPresidencyCrawlerViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return imgReg.getDescriptor(key);
	}

	private AmericanPresidencyCrawlerViewImageRegistry() {

		imgReg.put(IAmericanPresidencyCrawlerViewConstants.IMAGE_LRUN_OBJ,
				ImageDescriptor.createFromFile(
						AmericanPresidencyCrawlerViewImageRegistry.class,
						"/icons/lrun_obj.gif"));

		imgReg.put(IAmericanPresidencyCrawlerViewConstants.IMAGE_HELP_CO,
				ImageDescriptor.createFromFile(
						AmericanPresidencyCrawlerViewImageRegistry.class,
						"/icons/help_contents.gif"));
		
		imgReg.put(IAmericanPresidencyCrawlerViewConstants.IMAGE_AMERICAN_PRESIDENCY_OBJ,
				ImageDescriptor.createFromFile(
						AmericanPresidencyCrawlerViewImageRegistry.class,
						"/icons/latin.ico"));
	}

	public static AmericanPresidencyCrawlerViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new AmericanPresidencyCrawlerViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return imgReg.get(imageName);
	}
}
