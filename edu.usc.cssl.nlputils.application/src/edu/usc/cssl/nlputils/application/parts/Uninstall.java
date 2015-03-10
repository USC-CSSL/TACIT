/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */
package edu.usc.cssl.nlputils.application.parts;


import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.JOptionPane;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;

//import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.ramo.klevis.p2.core.iservice.IUninstallSoftwareService;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Combo;

public class Uninstall {
	IUninstallSoftwareService uninstallSoftwareService;

	IWorkbench workbench;
	List<IInstallableUnit> installedSoftware;

	private Tree tree;
	int selectedRepresent = 0;
	IProvisioningAgent agent;

	public Uninstall(
			IUninstallSoftwareService uninstallSoftwareService,
			IProvisioningAgent agen, IWorkbench workbench) {

		this.agent = agen;
		this.workbench = workbench;
		this.uninstallSoftwareService = uninstallSoftwareService;
		installedSoftware = uninstallSoftwareService.listInstalledSoftware(
				agen, selectedRepresent);

	}

	/**
	 * Create contents of the view part.
	 */
	@PostConstruct
	public void createControls(final Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		parent.setSize(500, 310);
		if (installedSoftware == null) {
			MessageDialog.openWarning((Shell) parent, "",
					"Export .product file first!");

			return;

		}
		Composite composite = new Composite(parent, SWT.NONE);
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 2, 1);
		gd_composite.widthHint = 683;
		composite.setLayoutData(gd_composite);

		Button btnRadioButton = new Button(composite, SWT.RADIO);
		btnRadioButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				installedSoftware = uninstallSoftwareService
						.listInstalledSoftware(agent,
								IUninstallSoftwareService.GROUP);
				addToTree();
			}
		});
		btnRadioButton.setBounds(10, 10, 90, 16);
		btnRadioButton.setText("Group");

		Button btnRadioButton_1 = new Button(composite, SWT.RADIO);
		btnRadioButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				installedSoftware = uninstallSoftwareService
						.listInstalledSoftware(agent,
								IUninstallSoftwareService.CATEGORY);
				addToTree();
			}
		});
		btnRadioButton_1.setBounds(107, 10, 90, 16);
		btnRadioButton_1.setText("Category");

		Button btnRadioButton_2 = new Button(composite, SWT.RADIO);
		btnRadioButton_2.setEnabled(false);
		btnRadioButton_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				installedSoftware = uninstallSoftwareService
						.listInstalledSoftware(agent,
								IUninstallSoftwareService.ANY);
				addToTree();
			}
		});
		btnRadioButton_2.setBounds(203, 10, 90, 16);
		btnRadioButton_2.setText("Any");

		btnRadioButton.setSelection(true);

		Label lblListOfInstalled = new Label(parent, SWT.NONE);
		//lblListOfInstalled.setFont(SWTResourceManager.getFont("Lucida Grande", 12, SWT.NORMAL));
		lblListOfInstalled.setText("List of installed software");
		new Label(parent, SWT.NONE);

		tree = new Tree(parent, SWT.BORDER | SWT.MULTI);
		GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_tree.widthHint = 214;
		tree.setLayoutData(gd_tree);

		addToTree();
		new Label(parent, SWT.NONE);
		Button btnUninstall = new Button(parent, SWT.NONE);
		/*
		 * btnUninstall.addMouseListener(new MouseAdapter() {
		 * 
		 * @Override public void mouseDown(MouseEvent e) { uninstall(parent); }
		 * });
		 */
		GridData gd_btnUninstall = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_btnUninstall.widthHint = 103;
		btnUninstall.setLayoutData(gd_btnUninstall);
		btnUninstall.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				uninstall(parent);
			}

		});
		//btnUninstall.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.NORMAL));
		btnUninstall.setText("Uninstall");
		new Label(parent, SWT.NONE);
	}

	private void addToTree() {
		tree.removeAll();
		for (IInstallableUnit installed : installedSoftware) {

			TreeItem treeItem = new TreeItem(tree, 0);
			if(!(installed.getId().equals("NLPUtils") || 
					installed.getId().equals("edu.usc.cssl.nlputils.application.product")))
				treeItem.setText(installed.getId());
		}
	}

	@PreDestroy
	public void dispose() {
	}

	@Focus
	public void setFocus() {
		// TODO Set the focus to control
	}

	private void uninstall(final Composite parent) {

		TreeItem[] selection = tree.getSelection();

		if (selection.length == 0) {
			MessageDialog.openWarning((Shell) parent, "Warning",
					"Please select at least one");
			return;
		} else {

			List<IInstallableUnit> listToUninstall = new ArrayList<IInstallableUnit>();
			for (TreeItem tree : selection) {

				for (IInstallableUnit in : installedSoftware) {

					if (tree.getText().equals(in.getId())) {
						listToUninstall.add(in);

					}

				}
			}
			String uninstallSelected = "OK";
			try {
				uninstallSelected = uninstallSoftwareService
						.uninstallSelected(listToUninstall);
			} catch (Exception em) {
				JOptionPane.showMessageDialog(null, em.getMessage());
				return;
			}

			if (uninstallSelected == null) {
				boolean openConfirm = MessageDialog
						.openConfirm((Shell) parent, "",
								"Software Uninstalled!Do you want to restart so changes may take effect?");

				if (openConfirm) {

					workbench.restart();
				}
			} else {
				MessageDialog.openWarning((Shell) parent, "Info",
						uninstallSelected);
			}
		}
	}
}
