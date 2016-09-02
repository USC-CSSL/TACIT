package edu.usc.cssl.tacit.common.ui.corpusmanagement.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
public class CorpusManagementUIViewImageRegistry {

	ImageRegistry ir = new ImageRegistry();
	static CorpusManagementUIViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}

	private CorpusManagementUIViewImageRegistry() {

		ir.put(ICorpusManagementConstants.IMAGE_CORPUS_MANAGEMENT_OBJ, ImageDescriptor
				.createFromFile(CorpusManagementUIViewImageRegistry.class,
						"/icons/CorpusManagementIcon.png"));

	}

	public static CorpusManagementUIViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new CorpusManagementUIViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return ir.get(imageName);
	}

}
