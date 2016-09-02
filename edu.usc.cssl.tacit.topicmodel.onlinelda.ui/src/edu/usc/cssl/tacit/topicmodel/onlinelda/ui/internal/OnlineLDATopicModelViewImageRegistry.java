package edu.usc.cssl.tacit.topicmodel.onlinelda.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class OnlineLDATopicModelViewImageRegistry {
	ImageRegistry ir = new ImageRegistry();
	static OnlineLDATopicModelViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}

	private OnlineLDATopicModelViewImageRegistry() {
		
		ir.put(IOnlineLDATopicModelViewConstants.IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(OnlineLDATopicModelViewImageRegistry.class, "/icons/lrun_obj.gif"));
		
		ir.put(IOnlineLDATopicModelViewConstants.IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(OnlineLDATopicModelViewImageRegistry.class, "/icons/help_contents.gif"));	
		ir.put(IOnlineLDATopicModelViewConstants.IMAGE_ONLINE_LDA_OBJ,
				ImageDescriptor.createFromFile(OnlineLDATopicModelViewImageRegistry.class,
						"/icons/OnlineLDAIcon.png"));

	}

	public static OnlineLDATopicModelViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new OnlineLDATopicModelViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return ir.get(imageName);
	}
}
