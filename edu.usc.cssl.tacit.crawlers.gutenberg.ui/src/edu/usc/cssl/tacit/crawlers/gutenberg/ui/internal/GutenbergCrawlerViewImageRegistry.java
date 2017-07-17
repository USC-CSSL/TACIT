package edu.usc.cssl.tacit.crawlers.gutenberg.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class GutenbergCrawlerViewImageRegistry{

	ImageRegistry ir = new ImageRegistry();
	static GutenbergCrawlerViewImageRegistry imgIcon;
	
	//Returns the descriptor associated with the given key in this registry, or null if none.
	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}
	
	private GutenbergCrawlerViewImageRegistry(){
		
		
		ir.put(IGutenbergCrawlerViewConstants .IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(GutenbergCrawlerViewImageRegistry.class, "/icons/lrun_obj.gif"));
		
		ir.put(IGutenbergCrawlerViewConstants .IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(GutenbergCrawlerViewImageRegistry.class, "/icons/help_contents.gif"));

		ir.put(IGutenbergCrawlerViewConstants .IMAGE_GUTENBERG_OBJ, ImageDescriptor
				.createFromFile(GutenbergCrawlerViewImageRegistry.class, "/icons/GutenbergCrawlerIcon.png"));
	}
	
	public static GutenbergCrawlerViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new GutenbergCrawlerViewImageRegistry();
		}
		return imgIcon;

	}

	
	public Image getImage(String imageName) {
		return ir.get(imageName);
	}
	
}


/* This file handles creation of images from the gif files we provide and allocating OS resources for image to get displayed */