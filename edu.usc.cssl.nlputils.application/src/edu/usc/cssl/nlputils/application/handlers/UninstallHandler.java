package edu.usc.cssl.nlputils.application.handlers;
 


import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.ramo.klevis.p2.core.iservice.IUninstallSoftwareService;

import edu.usc.cssl.nlputils.application.parts.Uninstall;

public class UninstallHandler {
	@Execute
	public void execute(IProvisioningAgent agent,
			IUninstallSoftwareService uninstallSoftwareService, Shell shell,IWorkbench workbench) {
		// TODO Your code goes here

		Shell shell2 = new Shell(shell, SWT.DIALOG_TRIM);
		new Uninstall(uninstallSoftwareService, agent,workbench)
				.createControls(shell2);
		shell2.open();
	}

}