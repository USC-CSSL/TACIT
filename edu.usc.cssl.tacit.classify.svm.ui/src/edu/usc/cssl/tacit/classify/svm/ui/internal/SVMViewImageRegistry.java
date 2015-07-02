package edu.usc.cssl.tacit.classify.svm.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class SVMViewImageRegistry {

	ImageRegistry ir = new ImageRegistry();
	static SVMViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}

	private SVMViewImageRegistry() {
		
		ir.put(ISVMViewConstants.IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(SVMViewImageRegistry.class, "/icons/lrun_obj.gif"));
		
		ir.put(ISVMViewConstants.IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(SVMViewImageRegistry.class, "/icons/help_contents.gif"));

		

	}

	public static SVMViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new SVMViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return ir.get(imageName);
	}
}
