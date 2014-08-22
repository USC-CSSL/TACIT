/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */

package edu.usc.pil.nlputils.plugins.preprocessorService.activator;

import javax.inject.Inject;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.usc.pil.nlputils.plugins.preprocessorService.services.PreprocessorService;

public class PreprocessorActivator extends AbstractUIPlugin {

	@Inject BundleContext context;
	public PreprocessorActivator() {
		System.out.println("Activating PreprocessorService");
		PreprocessorService ppService = new PreprocessorService();
		context.registerService(PreprocessorService.class.getName(), ppService, null);
		
	}

}
