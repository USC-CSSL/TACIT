package edu.usc.cssl.tacit.common.ui.preferencepage;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.common.ui.ICommonUiConstants;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;

public class CorpusLocation extends PreferencePage implements
IWorkbenchPreferencePage, ICommonUiConstants  {
	private Text corpusLocation;
	private String oldCorpusLocation;
@Override
public void init(IWorkbench workbench) {
	setPreferenceStore(CommonUiActivator.getDefault().getPreferenceStore());
	
}

@Override
protected Control createContents(Composite parent) {
	Composite sectionClient = new Composite(parent, SWT.NONE);
	GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
			.applyTo(sectionClient);

	Label dummy = new Label(sectionClient, SWT.NONE);
	GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
			.applyTo(dummy);
	corpusLocation = createCorpusLocation(sectionClient);
	initializeDefaultValues();
	loadValues();
	if (Boolean.valueOf(load(INITIAL))) {
		performDefaults();
	}
	return sectionClient;
}
private Text createCorpusLocation(Composite sectionClient) {
	Label locationLbl = new Label(sectionClient, SWT.NONE);
	locationLbl.setText("TACIT Corpora Location:");
	GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
			.applyTo(locationLbl);

	final Text outputLocationTxt = new Text(sectionClient, SWT.BORDER);
	GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
			.applyTo(outputLocationTxt);
	outputLocationTxt.setEditable(false);
	oldCorpusLocation = outputLocationTxt.getText();

	final Button browseBtn = new Button(sectionClient, SWT.PUSH);
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
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	});
	return outputLocationTxt;
}

private void initializeDefaultValues() {
	getPreferenceStore().setDefault(INITIAL, "true");
	oldCorpusLocation = System.getProperty("user.dir")
			+ System.getProperty("file.separator") + "tacit_corpora";
	getPreferenceStore().setDefault(CORPUS_LOCATION, oldCorpusLocation);
}

@Override
protected void performDefaults() {
	setDefaultValues();
	super.performDefaults();
}


private void setDefaultValues() {
	oldCorpusLocation = getPreferenceStore().getDefaultString(
			CORPUS_LOCATION);
	corpusLocation.setText(oldCorpusLocation);
}

private void loadValues() {
	corpusLocation.setText(load(CORPUS_LOCATION));
	oldCorpusLocation = load(CORPUS_LOCATION);
}

private String load(String name) {
	return getPreferenceStore().getString(name);
}

@Override
protected void performApply() {
	//performOk();
	super.performApply();
}

@Override
public boolean performOk() {

	store(INITIAL, Boolean.toString(false));
	// store(CORPUS_LOCATION, corpusLocation.getText());
	if (ManageCorpora.isCorpusLocChanged(corpusLocation.getText())) {
		MessageBox msg = new MessageBox(getShell(), SWT.ICON_WARNING
				| SWT.YES | SWT.NO);
		msg.setMessage("You are attempting to move a large amount of data. This will cause TACIT to hang. Do you want to continue?");
		if (msg.open() == SWT.YES) {
			store(CORPUS_LOCATION, corpusLocation.getText());
			ManageCorpora.moveCorpora();
		} else {
			corpusLocation.setText(oldCorpusLocation);
		}
	}
	return true;
}

private void store(String name, String value) {
	getPreferenceStore().setValue(name, value);

}

}
