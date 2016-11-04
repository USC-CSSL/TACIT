package edu.usc.cssl.tacit.common.ui.corpusmanagement;

import java.util.List;

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

import edu.usc.cssl.tacit.common.queryprocess.Filter;
import edu.usc.cssl.tacit.common.ui.TacitCorpusFilterDialog;

public class WizardPage1 extends WizardPage {
	Composite container;
	Button b1, b2, b3, b4;
	TacitCorpusFilterDialog filterDialog;
	List<Filter> returnFilter;
	IWizardData data;
	
	protected WizardPage1(TacitCorpusFilterDialog filterDialog, IWizardData data) {
		super("Export Files");
		this.filterDialog = filterDialog;
		this.data = data;
		setTitle("Export Files");
		setDescription("Select the files to be exported");
	}


	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);

		Label label1 = new Label(container, SWT.NONE);
		label1.setText("Select how to export files");
		GridDataFactory.fillDefaults().grab(false, false).span(2, 0).applyTo(label1);
		Composite c1 = new Composite(container, SWT.NONE);
		c1.setLayout(layout);
		
		b1 = new Button(c1, SWT.RADIO);
		b1.setText("Export all");
		b1.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (b1.getSelection()) {
					b2.setSelection(false);
					b3.setEnabled(true);
					b4.setEnabled(true);
				}else if(b2.getSelection()){
					b1.setSelection(false);
					if(filterDialog.open() == Window.OK){
						returnFilter = filterDialog.getSelectionObjects();
						data.getData(returnFilter);
					}
					else{
						b1.setSelection(true);
						b2.setSelection(false);
					}
					b3.setEnabled(true);
					b4.setEnabled(true);
				}else{
					b3.setEnabled(false);
					b4.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		
		b2 = new Button(c1, SWT.RADIO);
		b2.setText("Export by query");
		b2.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (b2.getSelection()) {
					b1.setSelection(false);
					if(filterDialog.open() == Window.OK){
						returnFilter = filterDialog.getSelectionObjects();
						data.getData(returnFilter);
					}
					else{
						b1.setSelection(true);
						b2.setSelection(false);
					}
					b3.setEnabled(true);
					b4.setEnabled(true);
				}else if(b1.getSelection()){
					b2.setSelection(false);
					b3.setEnabled(true);
					b4.setEnabled(true);
				}else{
					b3.setEnabled(false);
					b4.setEnabled(false);
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		

		Label label3 = new Label(container, SWT.NONE);
		label3.setText("Select number of files");
		GridDataFactory.fillDefaults().grab(false, false).span(2, 0).applyTo(label3);
		Composite c3 = new Composite(container, SWT.NONE);
		c3.setLayout(layout);
		
		
		b3 = new Button(c3, SWT.RADIO);
		b3.setEnabled(false);
		b3.setText("Export as one file");
		b3.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (b4.getSelection() || b3.getSelection())
					setPageComplete(true);
					
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		
		
		b4 = new Button(c3, SWT.RADIO);
		b4.setEnabled(false);
		b4.setText("Export seperate file for each entry");
		b4.addSelectionListener(new SelectionListener() {	
			@Override
			public void widgetSelected(SelectionEvent e) {
				data.getDivision(true);
				setPageComplete(true);		
			}
		
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
		
			}
		});

		setControl(container);
		setPageComplete(false);
	}


	@Override
	public IWizardPage getNextPage() {
		if(!((ExportWizard)getWizard()).queryCorpus){
		WizardPage2 wizardPage2 = ((ExportWizard)getWizard()).two;
		return wizardPage2;
		}
		return null;
	}
	
	

}
