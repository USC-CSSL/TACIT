 
package edu.usc.cssl.nlputils.plugins.zlda2.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

public class openZeldaHandler {
	@Inject
	EPartService partService;
	@Execute
	public void execute() {
		System.out.println("Opening Zelda...");
		MPart part = partService.findPart("edu.usc.pil.nlputils.plugins.zlda2.part.zeldaSettings");
		part.setVisible(true);
		partService.showPart(part, PartState.VISIBLE);
	}
		
}