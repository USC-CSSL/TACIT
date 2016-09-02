package edu.usc.cssl.tacit.cluster.kmeans.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class KmeansClusterViewImageRegistry {

	ImageRegistry ir = new ImageRegistry();
	static KmeansClusterViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}

	private KmeansClusterViewImageRegistry() {
		
		ir.put(IKmeansClusterViewConstants.IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(KmeansClusterViewImageRegistry.class, "/icons/lrun_obj.gif"));
		
		ir.put(IKmeansClusterViewConstants.IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(KmeansClusterViewImageRegistry.class, "/icons/help_contents.gif"));

		ir.put(IKmeansClusterViewConstants.IMAGE_KMEANS_CLUSTERING_OBJ,
				ImageDescriptor.createFromFile(
						KmeansClusterViewImageRegistry.class,
						"/icons/KMeansClusteringIcon.png"));


	}

	public static KmeansClusterViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new KmeansClusterViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return ir.get(imageName);
	}

}
