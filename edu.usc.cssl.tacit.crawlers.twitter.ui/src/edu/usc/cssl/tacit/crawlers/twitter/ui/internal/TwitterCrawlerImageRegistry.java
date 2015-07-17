package edu.usc.cssl.tacit.crawlers.twitter.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class TwitterCrawlerImageRegistry {

	ImageRegistry ir = new ImageRegistry();
	static TwitterCrawlerImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}

	private TwitterCrawlerImageRegistry() {
		ir.put(ITwitterCrawlerUIConstants.IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(TwitterCrawlerImageRegistry.class, "/icons/lrun_obj.gif"));
		ir.put(ITwitterCrawlerUIConstants.IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(TwitterCrawlerImageRegistry.class, "/icons/help_contents.gif"));
		ir.put(ITwitterCrawlerUIConstants.IMAGE_CRAWL_TWITTER, ImageDescriptor
				.createFromFile(TwitterCrawlerImageRegistry.class, "/icons/twitter.gif"));
	}

	public static TwitterCrawlerImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new TwitterCrawlerImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return ir.get(imageName);
	}

}
