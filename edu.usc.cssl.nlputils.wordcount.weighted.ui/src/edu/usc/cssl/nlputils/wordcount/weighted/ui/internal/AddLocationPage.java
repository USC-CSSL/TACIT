package edu.usc.cssl.nlputils.wordcount.weighted.ui.internal;


import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class AddLocationPage extends WizardPage {

	private Composite container;
	private Text locationTxtControl;
	private Button browseBtn;
	

	protected AddLocationPage(String pageName) {
		super(pageName);
		setTitle("Add Folder to the input section");
		setDescription("Select Directory of files that needs to be used for word count");
		setControl(locationTxtControl);
	}

	@Override
	public void createControl(Composite parent) {
		
		container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(container);
        Label locationLbl = new Label(container,SWT.NONE); 
        locationLbl.setText("Location");
        GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(locationLbl);
        
        locationTxtControl = new Text(container, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(locationTxtControl);

        browseBtn = new Button(container, SWT.PUSH);
        browseBtn.setText("Browse...");
        GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(browseBtn);
        
        attachListener();
        setControl(container);
        setPageComplete(true);

	}

	private void attachListener() {
		browseBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(browseBtn.getShell(), SWT.OPEN);
				dlg.setText("Open");
				String path = dlg.open();
				if (path == null)
					return;
				locationTxtControl.setText(path);

			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		
		
	}

}
