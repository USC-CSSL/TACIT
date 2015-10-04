package edu.usc.cssl.tacit.common.ui.utility;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class IconRegistry {

	ImageRegistry ir = new ImageRegistry();
	static IconRegistry imgIcon;

	public ImageDescriptor getImageDescriptor(String key) {
		return ir.getDescriptor(key);
	}

	private IconRegistry() {
		ir.put(INlpCommonUiConstants.IMAGE_CEAR_CO, ImageDescriptor
				.createFromFile(IconRegistry.class, "/icons/clear_co.gif"));
		ir.put(INlpCommonUiConstants.IMAGE_LRUN_OBJ, ImageDescriptor
				.createFromFile(IconRegistry.class, "/icons/lrun_obj.gif"));
		ir.put(INlpCommonUiConstants.IMAGE_PRG_STOP, ImageDescriptor
				.createFromFile(IconRegistry.class, "/icons/progress_stop.gif"));
		ir.put(INlpCommonUiConstants.IMAGE_CONSOLE_VIEW, ImageDescriptor
				.createFromFile(IconRegistry.class, "/icons/console_view.gif"));
		ir.put(INlpCommonUiConstants.IMAGE_HELP_CO, ImageDescriptor
				.createFromFile(IconRegistry.class, "/icons/help_contents.gif"));
		ir.put(INlpCommonUiConstants.IMAGE_CRAWL, ImageDescriptor
				.createFromFile(IconRegistry.class, "/icons/toplogo_sm.jpg"));
		ir.put(INlpCommonUiConstants.IMAGE_CRAWL_TITLE, ImageDescriptor
				.createFromFile(IconRegistry.class, "/icons/toplogo_16.jpg"));

		ir.put(INlpCommonUiConstants.FILE_OBJ, ImageDescriptor.createFromFile(
				IconRegistry.class, "/icons/file_obj.gif"));

		ir.put(INlpCommonUiConstants.FLDR_OBJ, ImageDescriptor.createFromFile(
				IconRegistry.class, "/icons/fldr_obj.gif"));
		ir.put(INlpCommonUiConstants.IMAGE_SAVE_CO, ImageDescriptor.createFromFile(
				IconRegistry.class, "/icons/save.gif"));
		ir.put(INlpCommonUiConstants.CORPUS, ImageDescriptor.createFromFile(
				IconRegistry.class, "/icons/corpus.gif"));
		ir.put(INlpCommonUiConstants.CORPUS_CLASS, ImageDescriptor.createFromFile(
				IconRegistry.class, "/icons/corpusclass.gif"));

	}

	public static IconRegistry getImageIconFactory() {
		if (imgIcon == null) {
			imgIcon = new IconRegistry();
		}
		return imgIcon;

	}

	public Image getImage(String imageName) {
		return ir.get(imageName);
	}

}
