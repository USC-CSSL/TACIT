 
package edu.usc.cssl.nlputils.plugins.supremeCrawler.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

public class OpenSupreme {
	@Inject
	private EPartService partService;
	@Execute
	public void execute() {
		System.out.println("Opening Supreme Crawler Plugin");
		MPart part = partService.findPart("edu.usc.cssl.nlputils.plugins.supremecrawler.part.supremeCourtCrawlerSettings");
		part.setVisible(true);
		partService.showPart(part, PartState.VISIBLE);
	}
		
}