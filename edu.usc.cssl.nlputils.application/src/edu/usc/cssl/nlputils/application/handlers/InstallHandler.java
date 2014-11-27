 
package edu.usc.cssl.nlputils.application.handlers;


import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.ramo.klevis.p2.core.iservice.IInstallNewSoftwareService;

import edu.usc.cssl.nlputils.application.parts.Install;

public class InstallHandler {
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell,IProvisioningAgent agem,IInstallNewSoftwareService installNewSoftwareService,IWorkbench workbench) {
		System.out.println("Opening Installer");
		Shell shell2 = new Shell(shell,SWT.DIALOG_TRIM);
		new Install(installNewSoftwareService, agem,workbench).postConstruct(shell2);
		shell2.open();
	}
		
}