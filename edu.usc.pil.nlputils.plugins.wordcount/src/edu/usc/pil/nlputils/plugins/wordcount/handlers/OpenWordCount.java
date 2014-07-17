 
package edu.usc.pil.nlputils.plugins.wordcount.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

public class OpenWordCount {
	
	@Inject
	private EPartService partService;
	
	@Execute
	public void execute() {
		//TODO Your code goes here
		System.out.println("Opening WordCount");
		
		MPart part = partService.findPart("edu.usc.pil.nlputils.plugins.wordcount.part.0");
		part.setVisible(true);
		partService.showPart(part, PartState.VISIBLE);
	}
		
}