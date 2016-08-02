package edu.usc.cssl.tacit.common.ui.corpusmanagement;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import edu.usc.cssl.tacit.common.ui.TacitCorpusFilterDialog;

public class ExportWizard extends Wizard{
	
	public WizardPage0 zero; 
	public WizardPage1 one;
	public WizardPage2 two;
	TacitCorpusFilterDialog filterDialog;
	IWizardData data;

	  public ExportWizard(TacitCorpusFilterDialog filterDialog, IWizardData data) {
	    super();
	    this.data = data;
	    this.filterDialog = filterDialog;
	    setNeedsProgressMonitor(true);
	  }

	  @Override
	  public String getWindowTitle() {
	    return "Export My Data";
	  }

	  @Override
	  public void addPages() {
		zero = new WizardPage0(filterDialog, data);
		one = new WizardPage1(filterDialog, data); 
		two = new WizardPage2(data);
		addPage(zero);
		addPage(one);
		addPage(two);
	  }

	  
	  @Override
	public boolean canFinish() {
		  IWizardPage currentPage = getContainer().getCurrentPage();
		  if(currentPage instanceof WizardPage0 || currentPage instanceof WizardPage1 )
			  return false;
		  else 
			  return true;

	}

	@Override
	public boolean performFinish() {
		return true;
	}
	  

}
