package edu.usc.cssl.tacit.crawlers.latin.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class LatinCrawlerImageRegistry {

	ImageRegistry ir = new ImageRegistry();
	static LatinCrawlerImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}

	private LatinCrawlerImageRegistry() {
		ir.put(ILatinCrawlerUIConstants.IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(LatinCrawlerImageRegistry.class, "/icons/lrun_obj.gif"));
		ir.put(ILatinCrawlerUIConstants.IMAGE_LATIN, ImageDescriptor
				.createFromFile(LatinCrawlerImageRegistry.class, "/icons/latin.ico"));
		ir.put(ILatinCrawlerUIConstants.IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(LatinCrawlerImageRegistry.class, "/icons/help_contents.gif"));
	}

	public static LatinCrawlerImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new LatinCrawlerImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return ir.get(imageName);
	}

}
