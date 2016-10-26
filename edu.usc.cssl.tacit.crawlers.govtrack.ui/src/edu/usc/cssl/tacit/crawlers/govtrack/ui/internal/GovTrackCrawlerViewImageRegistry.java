package edu.usc.cssl.tacit.crawlers.govtrack.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class GovTrackCrawlerViewImageRegistry {

	ImageRegistry imgReg = new ImageRegistry();
	static GovTrackCrawlerViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return imgReg.getDescriptor(key);
	}

	private GovTrackCrawlerViewImageRegistry() {

		imgReg.put(IGovTrackCrawlerViewConstants.IMAGE_LRUN_OBJ,
				ImageDescriptor.createFromFile(
						GovTrackCrawlerViewImageRegistry.class,
						"/icons/lrun_obj.gif"));

		imgReg.put(IGovTrackCrawlerViewConstants.IMAGE_HELP_CO,
				ImageDescriptor.createFromFile(
						GovTrackCrawlerViewImageRegistry.class,
						"/icons/help_contents.gif"));
		
		imgReg.put(IGovTrackCrawlerViewConstants.IMAGE_GOVTRACK_OBJ,
				ImageDescriptor.createFromFile(
						GovTrackCrawlerViewImageRegistry.class,
						"/icons/govtrack_icon.png"));
	}

	public static GovTrackCrawlerViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new GovTrackCrawlerViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return imgReg.get(imageName);
	}
}
