package edu.usc.cssl.tacit.wordcount.weighted.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class WeightedWordCountImageRegistry {

	ImageRegistry ir = new ImageRegistry();
	static WeightedWordCountImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}

	private WeightedWordCountImageRegistry() {
		
		ir.put(IWeightedWordCountViewConstants.IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(WeightedWordCountImageRegistry.class, "/icons/lrun_obj.gif"));
		
		ir.put(IWeightedWordCountViewConstants.IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(WeightedWordCountImageRegistry.class, "/icons/help_contents.gif"));

		ir.put(IWeightedWordCountViewConstants.IMAGE_LIWC_WORD_COUNT_OBJ,
				ImageDescriptor.createFromFile(
						WeightedWordCountImageRegistry.class,
						"/icons/LIWCWordCountIcon.png"));


	}

	public static WeightedWordCountImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new WeightedWordCountImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return ir.get(imageName);
	}

}
