 
package edu.usc.cssl.nlputils.plugins.twitterStreamer.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

public class OpenTwitterStreamerHandler {
		
		@Inject
		EPartService partService;
		
		@Execute
		public void execute() {
			System.out.println("Opening Twitter Streamer");
			MPart part = partService.findPart("edu.usc.cssl.nlputils.plugins.twitterstreamer.part.TStreamerSettings");
			part.setVisible(true);
			partService.showPart(part, PartState.VISIBLE);
		}

		
		
}