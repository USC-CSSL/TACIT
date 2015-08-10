package edu.usc.cssl.tacit.wordcount.standard.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class StandardWordCountImageRegistry {

	ImageRegistry ir = new ImageRegistry();
	static StandardWordCountImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}

	private StandardWordCountImageRegistry() {
		
		ir.put(IStandardWordCountViewConstants.IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(StandardWordCountImageRegistry.class, "/icons/lrun_obj.gif"));
		
		ir.put(IStandardWordCountViewConstants.IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(StandardWordCountImageRegistry.class, "/icons/help_contents.gif"));

		ir.put(IStandardWordCountViewConstants.IMAGE_WORD_TITLE, ImageDescriptor
				.createFromFile(IStandardWordCountViewConstants.class, "/icons/wc.png"));

	}

	public static StandardWordCountImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new StandardWordCountImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return ir.get(imageName);
	}

}
