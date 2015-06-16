package edu.usc.cssl.nlputils.crawlers.senate.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class SenateCrawlerViewImageRegistry {

	ImageRegistry imgReg = new ImageRegistry();
	static SenateCrawlerViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return imgReg.getDescriptor(key);
	}

	private SenateCrawlerViewImageRegistry() {

		imgReg.put(ISenateCrawlerViewConstants.IMAGE_LRUN_OBJ,
				ImageDescriptor.createFromFile(
						SenateCrawlerViewImageRegistry.class,
						"/icons/lrun_obj.gif"));

		imgReg.put(ISenateCrawlerViewConstants.IMAGE_HELP_CO,
				ImageDescriptor.createFromFile(
						SenateCrawlerViewImageRegistry.class,
						"/icons/help_contents.gif"));
	}

	public static SenateCrawlerViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new SenateCrawlerViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return imgReg.get(imageName);
	}
}
