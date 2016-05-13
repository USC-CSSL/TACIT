package edu.usc.cssl.tacit.common.ui.corpusmanagement;
import org.eclipse.jface.wizard.Wizard;

import edu.usc.cssl.tacit.common.ui.TacitCorpusFilterDialog;

public class ExportWizard extends Wizard{

	WizardPage1 one ;
	WizardPage2 two;
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
		 one = new WizardPage1(filterDialog, data); 
		  two = new WizardPage2(data);
		  addPage(one);
		  addPage(two);
	  }

	  
	  @Override
	public boolean canFinish() {
		  if(getContainer().getCurrentPage() == one)
			  return false;
		  else 
			  return true;
	}
	  
	  @Override
	  public boolean performFinish() {
		  data.getPath(two.exportLocation);
	    return true;
	  }


}
