/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */ 
package edu.usc.cssl.nlputils.plugins.weightedCount.handlers;

import javax.inject.Inject;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

public class OpenWWCHandler {
	@Inject
	private EPartService partService;
	
	@Execute
	public void execute() {
		System.out.println("Opened WWC");
		MPart part = partService.findPart("edu.usc.cssl.nlputils.plugins.weightedcount.part.WWCSettings");
		part.setVisible(true);
		partService.showPart(part, PartState.VISIBLE);
	}
		
}