 
package edu.usc.pil.nlputils.plugins.classifier.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

public class OpenClassifier {
	
	@Inject
	private EPartService partService;
	
	@Execute
	public void execute() {
		System.out.println("Opening Classifier");
		
		MPart part = partService.findPart("edu.usc.pil.nlputils.plugins.classifier.part.0");
		part.setVisible(true);
		partService.showPart(part, PartState.VISIBLE);
		
	}
		
}