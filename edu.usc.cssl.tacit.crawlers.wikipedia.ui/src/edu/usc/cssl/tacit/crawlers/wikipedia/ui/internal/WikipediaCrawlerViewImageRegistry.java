package edu.usc.cssl.tacit.crawlers.wikipedia.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class WikipediaCrawlerViewImageRegistry {

	ImageRegistry imgReg = new ImageRegistry();
	static WikipediaCrawlerViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return imgReg.getDescriptor(key);
	}

	public WikipediaCrawlerViewImageRegistry() {

		imgReg.put(IWikipediaCrawlerUIConstants.IMAGE_LRUN_OBJ,
				ImageDescriptor.createFromFile(
						WikipediaCrawlerViewImageRegistry.class,
						"/icons/lrun_obj.gif"));

		imgReg.put(IWikipediaCrawlerUIConstants.IMAGE_HELP_CO,
				ImageDescriptor.createFromFile(
						WikipediaCrawlerViewImageRegistry.class,
						"/icons/help_contents.gif"));
		
		imgReg.put(IWikipediaCrawlerUIConstants.IMAGE_STACK_OBJ,
				ImageDescriptor.createFromFile(
						WikipediaCrawlerViewImageRegistry.class,
						"/icons/reddit.png"));
	}

	public static WikipediaCrawlerViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new WikipediaCrawlerViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return imgReg.get(imageName);
	}
}
