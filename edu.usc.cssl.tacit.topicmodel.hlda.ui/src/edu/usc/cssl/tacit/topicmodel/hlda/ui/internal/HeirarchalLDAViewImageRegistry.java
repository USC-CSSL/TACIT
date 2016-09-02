package edu.usc.cssl.tacit.topicmodel.hlda.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class HeirarchalLDAViewImageRegistry {
	ImageRegistry ir = new ImageRegistry();
	static HeirarchalLDAViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}

	private HeirarchalLDAViewImageRegistry() {
		
		ir.put(IHeirarchicalLDAViewConstants.IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(HeirarchalLDAViewImageRegistry.class, "/icons/lrun_obj.gif"));
		
		ir.put(IHeirarchicalLDAViewConstants.IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(HeirarchalLDAViewImageRegistry.class, "/icons/help_contents.gif"));
		
		ir.put(IHeirarchicalLDAViewConstants.IMAGE_HEIRARCHICAL_LDA_OBJ,
				ImageDescriptor.createFromFile(HeirarchalLDAViewImageRegistry.class,
						"/icons/HeirarchicalLDAIcon.png"));
	}

	public static HeirarchalLDAViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new HeirarchalLDAViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return ir.get(imageName);
	}
}
