package edu.usc.cssl.tacit.classify.naivebayes.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class NaiveBayesClassifierViewImageRegistry {

	ImageRegistry imgReg = new ImageRegistry();
	static NaiveBayesClassifierViewImageRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return imgReg.getDescriptor(key);
	}

	private NaiveBayesClassifierViewImageRegistry() {

		imgReg.put(INaiveBayesClassifierViewConstants.IMAGE_LRUN_OBJ,
				ImageDescriptor.createFromFile(
						NaiveBayesClassifierViewImageRegistry.class,
						"/icons/lrun_obj.gif"));

		imgReg.put(INaiveBayesClassifierViewConstants.IMAGE_HELP_CO,
				ImageDescriptor.createFromFile(
						NaiveBayesClassifierViewImageRegistry.class,
						"/icons/help_contents.gif"));

		imgReg.put(INaiveBayesClassifierViewConstants.IMAGE_FILE_OBJ,
				ImageDescriptor.createFromFile(
						NaiveBayesClassifierViewImageRegistry.class,
						"/icons/file_obj.gif"));

		imgReg.put(INaiveBayesClassifierViewConstants.IMAGE_FOLDER_OBJ,
				ImageDescriptor.createFromFile(
						NaiveBayesClassifierViewImageRegistry.class,
						"/icons/fldr_obj.gif"));
		imgReg.put(INaiveBayesClassifierViewConstants.IMAGE_NAIVE_BAYES_CLASSIFIER_OBJ,
				ImageDescriptor.createFromFile(
						NaiveBayesClassifierViewImageRegistry.class,
						"/icons/NaiveBayesClassifierIcon.png"));

	}

	public static NaiveBayesClassifierViewImageRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new NaiveBayesClassifierViewImageRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return imgReg.get(imageName);
	}
}
