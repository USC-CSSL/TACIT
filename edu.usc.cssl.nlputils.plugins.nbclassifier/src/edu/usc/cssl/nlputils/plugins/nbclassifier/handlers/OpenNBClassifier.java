/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */ 
package edu.usc.cssl.nlputils.plugins.nbclassifier.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

public class OpenNBClassifier {
	
	@Inject
	private EPartService partService;
	
	@Execute
	public void execute() {
		System.out.println("Opening Classifier");
		
		MPart part = partService.findPart("edu.usc.pil.nlputils.plugins.nbclassifier.part.0");
		part.setVisible(true);
		partService.showPart(part, PartState.VISIBLE);
		
	}
		
}