package edu.usc.cssl.tacit.crawlers.uscongress.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class UsCongressCrawlerViewImageRegistry {

	ImageRegistry imgReg = new ImageRegistry();
	static UsCongressCrawlerViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return imgReg.getDescriptor(key);
	}

	private UsCongressCrawlerViewImageRegistry() {

		imgReg.put(IUsCongressCrawlerViewConstants.IMAGE_LRUN_OBJ,
				ImageDescriptor.createFromFile(
						UsCongressCrawlerViewImageRegistry.class,
						"/icons/lrun_obj.gif"));

		imgReg.put(IUsCongressCrawlerViewConstants.IMAGE_HELP_CO,
				ImageDescriptor.createFromFile(
						UsCongressCrawlerViewImageRegistry.class,
						"/icons/help_contents.gif"));
		
		imgReg.put(IUsCongressCrawlerViewConstants.IMAGE_US_CONGRESS_OBJ,
				ImageDescriptor.createFromFile(
						UsCongressCrawlerViewImageRegistry.class,
						"/icons/uscongress.png"));
	}

	public static UsCongressCrawlerViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new UsCongressCrawlerViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return imgReg.get(imageName);
	}
}
