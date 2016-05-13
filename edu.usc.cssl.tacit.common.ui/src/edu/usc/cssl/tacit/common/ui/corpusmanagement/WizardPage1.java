package edu.usc.cssl.tacit.common.ui.corpusmanagement;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
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
	Button b1, b2, b3, b4, b5, b6;
	TacitCorpusFilterDialog filterDialog;
	List<Filter> returnFilter;
	IWizardData data;
	
	protected WizardPage1(TacitCorpusFilterDialog filterDialog, IWizardData data) {
		super("Export Files");
		this.filterDialog = filterDialog;
		this.data = data;
		setTitle("Export Files");
		setDescription("Select the files to be exported");
		// TODO Auto-generated constructor stub
	}

	SelectionListener finalSelection = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (b6.isEnabled() || b5.isEnabled())
				setPageComplete(true);
				
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub

		}
	};

	SelectionListener initial1 = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (b1.isEnabled() || b2.isEnabled()) {
				b5.setEnabled(true);
				b6.setEnabled(true);
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub

		}
	};

	SelectionListener initial2 = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (b3.isEnabled() || b4.isEnabled()) {
				b5.setEnabled(true);
				b6.setEnabled(true);
			}

		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub

		}
	};

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
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
		b2 = new Button(c1, SWT.RADIO);
		b2.setText("Export by query");
		b1.addSelectionListener(initial1);
		b2.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (b2.isEnabled()) {
					if(filterDialog.open() == Window.OK){
						returnFilter = filterDialog.getSelectionObjects();
						data.getData(returnFilter);
					}
					else{
						b1.setSelection(true);
					}
					b5.setEnabled(true);
					b6.setEnabled(true);
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
//		Label label2 = new Label(container, SWT.NONE);
//		label2.setText("Select export file type");
//		GridDataFactory.fillDefaults().grab(false, false).span(2, 0).applyTo(label2);
//		Composite c2 = new Composite(container, SWT.NONE);
//		c2.setLayout(layout);
//		b3 = new Button(c2, SWT.RADIO);
//		b3.setEnabled(false);
//		b3.setText("Output as text");
//		b4 = new Button(c2, SWT.RADIO);
//		b4.setEnabled(false);
//		b4.setText("Output as CSV");
//		b3.addSelectionListener(initial2);
//		b4.addSelectionListener(initial2);

		Label label3 = new Label(container, SWT.NONE);
		label3.setText("Select number of files");
		GridDataFactory.fillDefaults().grab(false, false).span(2, 0).applyTo(label3);
		Composite c3 = new Composite(container, SWT.NONE);
		c3.setLayout(layout);
		b5 = new Button(c3, SWT.RADIO);
		b5.setEnabled(false);
		b5.setText("Export as one file");
		b6 = new Button(c3, SWT.RADIO);
		b6.setEnabled(false);
		b6.setText("Export seperate file for each entry");

		b5.addSelectionListener(finalSelection);
		b6.addSelectionListener(new SelectionListener() {	
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

}
