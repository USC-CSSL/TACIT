package edu.usc.cssl.tacit.topicmodel.hdp.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class HDPTopicModelViewImageRegistry {
	ImageRegistry ir = new ImageRegistry();
	static HDPTopicModelViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}

	public HDPTopicModelViewImageRegistry(){
		
		ir.put(IHdpTopicModelViewConstants.IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(HDPTopicModelViewImageRegistry.class, "/icons/lrun_obj.gif"));
		
		ir.put(IHdpTopicModelViewConstants.IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(HDPTopicModelViewImageRegistry.class, "/icons/help_contents.gif"));

		

	}

	public static HDPTopicModelViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new HDPTopicModelViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return ir.get(imageName);
	}

}
