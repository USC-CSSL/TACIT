/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */ 
package edu.usc.cssl.nlputils.application.parts;

import java.util.List;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.ramo.klevis.p2.core.iservice.IInstallNewSoftwareService;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.custom.StyledText;

public class Install {
	private Text text;
	IProvisioningAgent agent;
	IInstallNewSoftwareService installService;
	List<IInstallableUnit> loadRepository;
	IWorkbench workbench;
	private Tree tree;
	
	@Inject
	public Install() {
		
	}
	public Install(IInstallNewSoftwareService installService,
			IProvisioningAgent agent, IWorkbench workbench) {
		this.installService = installService;
		this.agent = agent;
		this.workbench = workbench;
	}

	
	@PostConstruct
	public void postConstruct(final Composite parent) {
		final Shell shell = parent.getShell();
		parent.setLayout(new GridLayout(3, false));
		parent.setSize(500, 325);
		Label lblInstallSitePath = new Label(parent, SWT.NONE);
		lblInstallSitePath.setText("Install Site Path Location");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		text = new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button button = new Button(parent, SWT.NONE);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog location = new DirectoryDialog(shell);
				location.open();
				String locationPath = location.getFilterPath();
				text.setText(locationPath);
			}
		});
		button.setText("...");
		
				Button btnOk = new Button(parent, SWT.NONE);
				btnOk.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseUp(MouseEvent e) {
						String text2 = text.getText();

						loadRepository = installService.loadRepository(text2, agent);

						tree.removeAll();
						for (IInstallableUnit install : loadRepository) {

							TreeItem treeItem = new TreeItem(tree, 0);
							treeItem.setText(install.getId());

							boolean category = installService.isCategory(install);
							if (category) {

								List<IInstallableUnit> extractFromCategory = installService
										.extractFromCategory(install);

								for (IInstallableUnit iInstallableUnit : extractFromCategory) {

									new TreeItem(treeItem, 0).setText(iInstallableUnit
											.getId());
								}
							}

						}
					}
				});
				btnOk.setText("Scan");
		
		Label lblAvailablePlugins = new Label(parent, SWT.NONE);
		lblAvailablePlugins.setText("Available Plugins");
				new Label(parent, SWT.NONE);
				new Label(parent, SWT.NONE);
		
				tree = new Tree(parent, SWT.BORDER);
				GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
				gd_tree.heightHint = 83;
				tree.setLayoutData(gd_tree);
		
		Label lblLog = new Label(parent, SWT.NONE);
		lblLog.setText("Log");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		final StyledText styledText = new StyledText(parent, SWT.BORDER);
		GridData gd_styledText = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 4);
		gd_styledText.heightHint = 63;
		styledText.setLayoutData(gd_styledText);
		
		
		Button btnNewButton = new Button(parent, SWT.NONE);
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				String installNewSoftware = "";

				try {

					if (loadRepository == null || loadRepository.isEmpty()) {

						styledText.setText("You must select at least one plugin");

					}
					installNewSoftware = installService
							.installNewSoftware(loadRepository);

				} catch (Exception exception) {

					exception.printStackTrace();
					if (exception.getMessage().contains(
							"Profile id _SELF_ is not registered"))

						styledText
								.setText("You must export via .product file first");
					else
						styledText.setText(exception.getMessage()
								+ "Something bad happened");

				}

				if (installNewSoftware != null) {
					styledText.setText(installNewSoftware);

					if (installNewSoftware
							.equals(IInstallNewSoftwareService.SUCESS_INSTALL)) {
						boolean openConfirm = MessageDialog.openConfirm(
								(Shell) parent, "",
								"Plugins installed successfully. Do you want to restart the application in order to see changes?");
						if (openConfirm) {
							workbench.restart();
						}

					}
				} else
					styledText
							.setText("Plugins installed! Pres Esc and restart");
			}
		});
		btnNewButton.setText("Install");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
	}
	
	
	
	
}