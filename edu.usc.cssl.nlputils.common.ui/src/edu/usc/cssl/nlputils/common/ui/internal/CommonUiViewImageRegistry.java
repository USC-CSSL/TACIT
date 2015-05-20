package edu.usc.cssl.nlputils.common.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import edu.usc.cssl.nlputils.common.ui.ICommonUiConstants;

public class CommonUiViewImageRegistry {

	ImageRegistry ir = new ImageRegistry();
	static CommonUiViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}

	private CommonUiViewImageRegistry() {
		
		ir.put(ICommonUiConstants.IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(CommonUiViewImageRegistry.class, "/icons/lrun_obj.gif"));
		
		ir.put(ICommonUiConstants.IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(CommonUiViewImageRegistry.class, "/icons/help_contents.gif"));

		

	}

	public static CommonUiViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new CommonUiViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return ir.get(imageName);
	}

}
