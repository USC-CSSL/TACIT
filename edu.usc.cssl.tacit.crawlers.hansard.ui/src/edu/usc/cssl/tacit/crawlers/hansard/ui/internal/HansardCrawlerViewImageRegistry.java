package edu.usc.cssl.tacit.crawlers.hansard.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class HansardCrawlerViewImageRegistry {

	ImageRegistry imgReg = new ImageRegistry();
	static HansardCrawlerViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return imgReg.getDescriptor(key);
	}

	private HansardCrawlerViewImageRegistry() {

		imgReg.put(IHansardCrawlerViewConstants.IMAGE_LRUN_OBJ,
				ImageDescriptor.createFromFile(
						HansardCrawlerViewImageRegistry.class,
						"/icons/lrun_obj.gif"));

		imgReg.put(IHansardCrawlerViewConstants.IMAGE_HELP_CO,
				ImageDescriptor.createFromFile(
						HansardCrawlerViewImageRegistry.class,
						"/icons/help_contents.gif"));
		
		imgReg.put(IHansardCrawlerViewConstants.IMAGE_HANSARD_OBJ,
				ImageDescriptor.createFromFile(
						HansardCrawlerViewImageRegistry.class,
						"/icons/HansardCrawlerIcon.ico"));
	}

	public static HansardCrawlerViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new HansardCrawlerViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return imgReg.get(imageName);
	}
}
