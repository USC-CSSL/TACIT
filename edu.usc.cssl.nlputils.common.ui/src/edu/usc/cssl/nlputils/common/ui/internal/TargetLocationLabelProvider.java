package edu.usc.cssl.nlputils.common.ui.internal;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import edu.usc.cssl.nlputils.common.ui.utility.INlpCommonUiConstants;
import edu.usc.cssl.nlputils.common.ui.utility.IconRegistry;

public class TargetLocationLabelProvider implements ILabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	public Image getImage(Object arg0) {
		if (arg0 instanceof String) {
			return IconRegistry.getImageIconFactory().getImage(
					INlpCommonUiConstants.FILE_OBJ);

		} else if (arg0 instanceof TreeParent) {
			TreeParent parent = (TreeParent) arg0;
			if (parent.getFiles().size() == 0 && parent.getFolder().size() == 0) {
				return IconRegistry.getImageIconFactory().getImage(
						INlpCommonUiConstants.FILE_OBJ);
			}
		}

		return IconRegistry.getImageIconFactory().getImage(
				INlpCommonUiConstants.FLDR_OBJ);

	}

	@Override
	public String getText(Object element) {
		return element.toString();
	}

}