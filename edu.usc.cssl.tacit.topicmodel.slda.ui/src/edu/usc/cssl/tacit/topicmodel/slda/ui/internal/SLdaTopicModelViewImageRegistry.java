package edu.usc.cssl.tacit.topicmodel.slda.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class SLdaTopicModelViewImageRegistry {

	ImageRegistry ir = new ImageRegistry();
	static SLdaTopicModelViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}

	private SLdaTopicModelViewImageRegistry() {
		
		ir.put(ISLdaTopicModelClusterViewConstants.IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(SLdaTopicModelViewImageRegistry.class, "/icons/lrun_obj.gif"));
		
		ir.put(ISLdaTopicModelClusterViewConstants.IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(SLdaTopicModelViewImageRegistry.class, "/icons/help_contents.gif"));

		ir.put(ISLdaTopicModelClusterViewConstants.IMAGE_SLDA_OBJ,
				ImageDescriptor.createFromFile(SLdaTopicModelViewImageRegistry.class,
						"/icons/SLDAIcon.png"));


	}

	public static SLdaTopicModelViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new SLdaTopicModelViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return ir.get(imageName);
	}

}
