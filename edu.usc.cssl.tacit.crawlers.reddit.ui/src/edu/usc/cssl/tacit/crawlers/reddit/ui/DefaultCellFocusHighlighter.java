package edu.usc.cssl.tacit.crawlers.reddit.ui;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

public class DefaultCellFocusHighlighter extends FocusCellOwnerDrawHighlighter {

    public DefaultCellFocusHighlighter(ColumnViewer viewer) {
        super(viewer);
    }

    @Override
	protected boolean onlyTextHighlighting(ViewerCell cell) {
        return false;
    }

    @Override
	protected Color getSelectedCellBackgroundColor(ViewerCell cell) {
        return cell.getControl().getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE);
    }

    @Override
	protected Color getSelectedCellForegroundColor(ViewerCell cell) {
        return cell.getControl().getDisplay().getSystemColor(SWT.COLOR_WHITE);
    }

    @Override
	protected Color getSelectedCellForegroundColorNoFocus(ViewerCell cell) {
        return cell.getControl().getDisplay().getSystemColor(SWT.COLOR_WHITE);
    }

    @Override
	protected Color getSelectedCellBackgroundColorNoFocus(ViewerCell cell) {
        return cell.getControl().getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE);
    }

    @Override
	protected void focusCellChanged(ViewerCell newCell, ViewerCell oldCell) {
        super.focusCellChanged(newCell, oldCell);
    }


}