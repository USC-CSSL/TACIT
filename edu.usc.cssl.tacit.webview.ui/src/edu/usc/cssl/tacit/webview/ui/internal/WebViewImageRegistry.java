package edu.usc.cssl.tacit.webview.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class WebViewImageRegistry {

	ImageRegistry ir = new ImageRegistry();
	static WebViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}

	private WebViewImageRegistry() {
		
		ir.put(IWebViewConstants.IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(WebViewImageRegistry.class, "/icons/lrun_obj.gif"));
		
		ir.put(IWebViewConstants.IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(WebViewImageRegistry.class, "/icons/help_contents.gif"));

		ir.put(IWebViewConstants.IMAGE_WEBVIEW_OBJ,
				ImageDescriptor.createFromFile(
						WebViewImageRegistry.class,
						"/icons/WebViewIcon.png"));


	}

	public static WebViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new WebViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return ir.get(imageName);
	}

}
