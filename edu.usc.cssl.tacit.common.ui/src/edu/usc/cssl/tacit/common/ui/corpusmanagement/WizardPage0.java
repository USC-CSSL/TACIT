package edu.usc.cssl.tacit.common.ui.corpusmanagement;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import edu.usc.cssl.tacit.common.ui.TacitCorpusFilterDialog;

public class WizardPage0 extends WizardPage {
	Composite container;
	Button txtSelectionBtn, robjSelectionBtn, csvSelectionBtn;
	TacitCorpusFilterDialog filterDialog;
	IWizardData data;
	WizardPage1 wizardPage1;
	WizardPage2 wizardPage2;

	public WizardPage0(TacitCorpusFilterDialog filterDialog, IWizardData data) {
		super("Export Files");
		setTitle("Export Format Selection");
		setDescription("Select the format in which the corpus is to be exported.");
		this.filterDialog = filterDialog;
		this.data = data;
	}

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		container.setLayout(layout);
		
		//Text format button
		txtSelectionBtn = new Button(container, SWT.RADIO);
		txtSelectionBtn.setText("Text Format");
		txtSelectionBtn.setSelection(true);

		//CSV format button
		csvSelectionBtn = new Button(container, SWT.RADIO);
		csvSelectionBtn.setText("CSV Format");
		csvSelectionBtn.setSelection(false);

		
		//RObject format button
		robjSelectionBtn = new Button(container, SWT.RADIO);
		robjSelectionBtn.setText("R Dataframe Format");
		robjSelectionBtn.setSelection(false);

		setControl(container);
		setPageComplete(true);
		
	}

	@Override
	public IWizardPage getNextPage() {
		if (txtSelectionBtn.getSelection()){
			data.getExportSelection(ExportSelectionConstants.EXPORT_TXT_FORMAT);
			wizardPage1 = ((ExportWizard)getWizard()).one;
			return wizardPage1;
		}else if (csvSelectionBtn.getSelection()){
			data.getExportSelection(ExportSelectionConstants.EXPORT_CSV_FORMAT);
			wizardPage2 = ((ExportWizard)getWizard()).two;
			return wizardPage2;
		}else if (robjSelectionBtn.getSelection()){
			data.getExportSelection(ExportSelectionConstants.EXPORT_ROBJ_FORMAT);
			wizardPage2 = ((ExportWizard)getWizard()).two;
			return wizardPage2;	
		}else{
			return null;
		}
		
	}
	
	
	
	

}
