package edu.usc.cssl.tacit.wordcount.weighted.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.usc.cssl.tacit.wordcount.weighted.ui.WeightedWordCountView;

public class OpenWeightedWordCountViewHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			HandlerUtil.getActiveWorkbenchWindowChecked(event).
			getActivePage().showView(WeightedWordCountView.ID);

		} catch (PartInitException e) {
			e.printStackTrace();
		}
		return null;
	}

}
