package edu.usc.cssl.tacit.crawlers.reddit.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;


public class RedditCrawlerViewImageRegistry {

	ImageRegistry imgReg = new ImageRegistry();
	static RedditCrawlerViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return imgReg.getDescriptor(key);
	}

	private RedditCrawlerViewImageRegistry() {

		imgReg.put(IRedditCrawlerViewConstants.IMAGE_LRUN_OBJ,
				ImageDescriptor.createFromFile(
						RedditCrawlerViewImageRegistry.class,
						"/icons/lrun_obj.gif"));

		imgReg.put(IRedditCrawlerViewConstants.IMAGE_HELP_CO,
				ImageDescriptor.createFromFile(
						RedditCrawlerViewImageRegistry.class,
						"/icons/help_contents.gif"));
		
		imgReg.put(IRedditCrawlerViewConstants.IMAGE_REDDIT_OBJ,
				ImageDescriptor.createFromFile(
						RedditCrawlerViewImageRegistry.class,
						"/icons/reddit.png"));
	}

	public static RedditCrawlerViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new RedditCrawlerViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return imgReg.get(imageName);
	}
}
