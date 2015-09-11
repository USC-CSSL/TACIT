package edu.usc.cssl.tacit.repository;

import java.io.File;

import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	@Override
	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowProgressIndicator(true);
		configurer.setTitle("TACIT");
		
		String tempDir = System.getProperty("user.dir")+System.getProperty("file.separator")+"tacit_corpora";
		
		if (!(new File(tempDir).exists())){
			new File(tempDir).mkdir();
		}
		
		tempDir = System.getProperty("user.dir")+System.getProperty("file.separator")+"tacit_temp_files";
		
		if (!(new File(tempDir).exists())){
			new File(tempDir).mkdir();
		}		
		
	}
	
	@Override
	public void postWindowClose() {
		String tempDir = System.getProperty("user.dir")+System.getProperty("file.separator")+"tacit_temp_files";
		if (new File(tempDir).exists()){
			File dir = new File(tempDir);
			File files[] = dir.listFiles();
			
			for (File file : files) {
				file.delete();
			}
			
			dir.delete();
		}
		super.postWindowClose();
	}
}
