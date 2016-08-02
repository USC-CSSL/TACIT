package edu.usc.cssl.tacit.common.ui.corpusmanagement;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Text;

public class WizardPage2 extends WizardPage {

	Composite container;
	Text outputLocationTxt;
	IWizardData data;
	protected WizardPage2(IWizardData data) {
		super("Select Location");
		this.data = data;
		setTitle("Select Location");
		setDescription("Select the location to export the file(s)");
		setControl(outputLocationTxt);
	}

	String exportLocation;
	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		
		outputLocationTxt = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(outputLocationTxt);
		outputLocationTxt.setEditable(false);
		outputLocationTxt.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if(outputLocationTxt.getText().toString()!=null && !outputLocationTxt.getText().toString().equals("")){
					exportLocation = outputLocationTxt.getText();
					setPageComplete(true);
				}
				else{
					setPageComplete(false);
				}
				
			}
		});
		
		

		final Button browseBtn = new Button(container, SWT.PUSH);
		browseBtn.setText("Browse...");
		browseBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(browseBtn.getShell(),
						SWT.OPEN);
				dlg.setText("Open");
				String path = dlg.open();
				if (path == null)
					return;
				outputLocationTxt.setText(path);
				exportLocation = outputLocationTxt.getText();
				data.getPath(exportLocation);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		setControl(container);
	}
	
	
	
	

}
