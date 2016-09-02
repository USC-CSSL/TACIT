package edu.usc.cssl.tacit.topicmodel.zlda.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class ZlabelLdaTopicModelViewImageRegistry {

	ImageRegistry ir = new ImageRegistry();
	static ZlabelLdaTopicModelViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}

	private ZlabelLdaTopicModelViewImageRegistry() {
		
		ir.put(IZlabelLdaTopicModelClusterViewConstants.IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(ZlabelLdaTopicModelViewImageRegistry.class, "/icons/lrun_obj.gif"));
		
		ir.put(IZlabelLdaTopicModelClusterViewConstants.IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(ZlabelLdaTopicModelViewImageRegistry.class, "/icons/help_contents.gif"));

		ir.put(IZlabelLdaTopicModelClusterViewConstants.IMAGE_ZLDA_OBJ,
				ImageDescriptor.createFromFile(
						ZlabelLdaTopicModelViewImageRegistry.class,
						"/icons/ZLDAIcon.png"));


	}

	public static ZlabelLdaTopicModelViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new ZlabelLdaTopicModelViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return ir.get(imageName);
	}

}
