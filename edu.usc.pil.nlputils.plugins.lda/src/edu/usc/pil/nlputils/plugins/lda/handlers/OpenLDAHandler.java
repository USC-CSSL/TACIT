/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */ 
package edu.usc.pil.nlputils.plugins.lda.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

public class OpenLDAHandler {
	@Inject
	private EPartService partService;
	
	@Execute
	public void execute() {
		System.out.println("Opening LDA Plugin");
		MPart part = partService.findPart("edu.usc.pil.nlputils.plugins.lda.part.LDASettings");
		part.setVisible(true);
		partService.showPart(part, PartState.VISIBLE);
	}
		
}