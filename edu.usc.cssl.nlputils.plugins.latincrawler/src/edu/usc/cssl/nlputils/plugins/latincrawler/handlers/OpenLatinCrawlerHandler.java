/**
 * @author Niki Parmar nikijitp@usc.edu
 */ 
package edu.usc.cssl.nlputils.plugins.latincrawler.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

public class OpenLatinCrawlerHandler {
	@Inject
	private EPartService partService;
	@Execute
	public void execute() {
		System.out.println("Opening Senate Crawler Plugin");
		MPart part = partService.findPart("edu.usc.pil.nlputils.plugins.latincrawler.part.LatinCrawlerSettings");
		part.setVisible(true);
		partService.showPart(part, PartState.VISIBLE);
	}
		
}