package edu.usc.cssl.tacit.common.ui.corpusmanagement;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import edu.usc.cssl.tacit.common.ui.TacitCorpusFilterDialog;

public class ExportWizard extends Wizard{
	public boolean queryCorpus;
	public WizardPage0 zero; 
	public WizardPage1 one;
	public WizardPage2 two;
	public WizardPage4 four;
	TacitCorpusFilterDialog filterDialog;
	ElementListSelectionDialog dialog;
	IWizardData data;

	  public ExportWizard(TacitCorpusFilterDialog filterDialog, IWizardData data) {
	    super();
	    this.data = data;
	    this.filterDialog = filterDialog;
	    setNeedsProgressMonitor(true);
	  }
	  
	  public ExportWizard(TacitCorpusFilterDialog filterDialog, IWizardData data, ElementListSelectionDialog dialog, boolean query) {
		    super();
		    this.data = data;
		    this.dialog = dialog;
		    this.filterDialog = filterDialog;
		    queryCorpus = query;
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
		four = new WizardPage4(dialog, data);
		if(queryCorpus){
			addPage(four);
			addPage(one);
		}
		else{
		addPage(zero);
		addPage(one);
		addPage(two);
		}
	  }

	  
	  @Override
	public boolean canFinish() {
		  IWizardPage currentPage = getContainer().getCurrentPage();
		  if(currentPage instanceof WizardPage2){
			  return true;
		  }
		  else if (currentPage instanceof WizardPage1 && queryCorpus){
				  return true;
		  }
		  return false;
	}

	@Override
	public boolean performFinish() {
		return true;
	}
	  

}
