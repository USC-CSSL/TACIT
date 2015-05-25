package edu.usc.cssl.nlputils.wordcount.weighted.ui.internal;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;

public class EditDirectoryContainerPage extends Wizard{

	protected AddLocationPage locationPage;
	@Override
	public boolean performFinish() {
		return true;
	}
	
	@Override
	public String getWindowTitle() {
		
		return "Add Folder to the input section";
	}
	
	@Override
	public void addPages() {
		locationPage = new AddLocationPage("Location");
		addPage(locationPage);
	}

}
