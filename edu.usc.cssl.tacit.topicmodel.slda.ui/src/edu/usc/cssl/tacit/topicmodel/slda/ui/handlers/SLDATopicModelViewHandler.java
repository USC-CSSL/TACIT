package edu.usc.cssl.tacit.topicmodel.slda.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.usc.cssl.tacit.topicmodel.slda.ui.SLDATopicModelView;

public class SLDATopicModelViewHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			HandlerUtil.getActiveWorkbenchWindowChecked(event).
			getActivePage().showView(SLDATopicModelView.ID);

		} catch (PartInitException e) {
			e.printStackTrace();
		}
		return null;
	}

}
