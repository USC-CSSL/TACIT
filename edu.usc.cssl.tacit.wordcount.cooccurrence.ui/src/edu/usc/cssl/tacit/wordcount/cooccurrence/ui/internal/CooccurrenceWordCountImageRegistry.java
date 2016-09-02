package edu.usc.cssl.tacit.wordcount.cooccurrence.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class CooccurrenceWordCountImageRegistry {

	ImageRegistry ir = new ImageRegistry();
	static CooccurrenceWordCountImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}

	private CooccurrenceWordCountImageRegistry() {
		
		ir.put(ICooccurrenceWordCountViewConstants.IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(CooccurrenceWordCountImageRegistry.class, "/icons/lrun_obj.gif"));
		
		ir.put(ICooccurrenceWordCountViewConstants.IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(CooccurrenceWordCountImageRegistry.class, "/icons/help_contents.gif"));

		ir.put(ICooccurrenceWordCountViewConstants.IMAGE_COOCCURENCE_ANALYSIS_OBJ,
				ImageDescriptor.createFromFile(
						CooccurrenceWordCountImageRegistry.class,
						"/icons/CooccurenceAnalysisIcon.png"));


	}

	public static CooccurrenceWordCountImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new CooccurrenceWordCountImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return ir.get(imageName);
	}

}
