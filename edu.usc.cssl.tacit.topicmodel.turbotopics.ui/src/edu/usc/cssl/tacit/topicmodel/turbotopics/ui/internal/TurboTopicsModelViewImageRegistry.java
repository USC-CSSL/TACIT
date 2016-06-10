package edu.usc.cssl.tacit.topicmodel.turbotopics.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class TurboTopicsModelViewImageRegistry {

	ImageRegistry ir = new ImageRegistry();
	static TurboTopicsModelViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}

	private TurboTopicsModelViewImageRegistry() {
		
		ir.put(ITurboTopicsModelClusterViewConstants.IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(TurboTopicsModelViewImageRegistry.class, "/icons/lrun_obj.gif"));
		
		ir.put(ITurboTopicsModelClusterViewConstants.IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(TurboTopicsModelViewImageRegistry.class, "/icons/help_contents.gif"));

		

	}

	public static TurboTopicsModelViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new TurboTopicsModelViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return ir.get(imageName);
	}

}
