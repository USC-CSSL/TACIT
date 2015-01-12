 
package edu.usc.cssl.nlputils.application.handlers;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.swt.widgets.Shell;

import edu.usc.cssl.nlputils.plugins.preprocessorService.services.PreprocessorService;

public class GlobalPresserSettings {

	public static PreprocessorService ppService = new PreprocessorService();
	
	@Execute
	//@Inject
	//@Optional @Named("globalPreprocessSettings")String settings
	public void execute(Shell shell) {
		System.out.println("Opening Global Preprocessor Settings");
		printSettings(shell);
	}
	
	@Inject IEclipseContext context;
	public void printSettings(Shell shell){
		IEclipseContext parent = context.getParent();
		
		String currSettings = (String) parent.get("globalPreprocessSettings");
		if (currSettings!=null){
			System.out.println(currSettings);
			System.out.println(ppService.getOptions());
			ppService.setOptions(shell);
		} else {
			//System.out.println("Empty. Setting Defaults.");
			System.out.println("Setting Options.");
			ppService.setOptions(shell);
			parent.set("globalPreprocessSettings", "Set");
		}
	}

		
}