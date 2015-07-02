package edu.usc.cssl.tacit.topicmodel.lda.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class LdaTopicModelViewImageRegistry {

	ImageRegistry ir = new ImageRegistry();
	static LdaTopicModelViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}

	private LdaTopicModelViewImageRegistry() {
		
		ir.put(ILdaTopicModelClusterViewConstants.IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(LdaTopicModelViewImageRegistry.class, "/icons/lrun_obj.gif"));
		
		ir.put(ILdaTopicModelClusterViewConstants.IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(LdaTopicModelViewImageRegistry.class, "/icons/help_contents.gif"));

		

	}

	public static LdaTopicModelViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new LdaTopicModelViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return ir.get(imageName);
	}

}
