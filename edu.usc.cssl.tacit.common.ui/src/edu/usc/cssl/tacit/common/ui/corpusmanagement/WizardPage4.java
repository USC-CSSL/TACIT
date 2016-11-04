package edu.usc.cssl.tacit.common.ui.corpusmanagement;


import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class WizardPage4 extends WizardPage{
	Composite container;
	Text corpusTxt;
	Text corpusClassTxt;
	ElementListSelectionDialog dialog;
	IWizardData data;
	protected WizardPage4(ElementListSelectionDialog dialog, IWizardData data) {
		super("Add Corpus Class");
		setTitle("Corpus Details");
		setMessage("Enter details for new corpus");
		this.dialog = dialog;
		this.data = data;
	}

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		
		final Button checkBtn = new Button(container, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(checkBtn);
		checkBtn.setText("Add data to existing corpus");
		checkBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(checkBtn.getSelection()){
					if(dialog.open()==Window.OK){
						Object[] result = dialog.getResult();
						corpusTxt.setText(result[0].toString());
						corpusTxt.setEnabled(false);
						corpusClassTxt.setFocus();
					}
				}else{
					corpusTxt.setText("");
					corpusTxt.setEnabled(true);
					corpusTxt.setFocus();
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		Label corpusLbl = new Label(container, SWT.NONE);
		corpusLbl.setText("Corpus Name: ");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(corpusLbl);
		
		corpusTxt = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(corpusTxt);
		
		Label corpusClassLbl = new Label(container, SWT.NONE);
		corpusClassLbl.setText("Corpus class: ");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(corpusClassLbl);
		
		corpusClassTxt = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(corpusClassTxt);
		
		setControl(container);
	}
	

		@Override
		public IWizardPage getNextPage() {
			data.getCorpus(corpusTxt.getText());
			data.getCorpusClass(corpusClassTxt.getText());
			WizardPage1 wizardPage1 = ((ExportWizard)getWizard()).one;
			return wizardPage1;
		
	}

}
