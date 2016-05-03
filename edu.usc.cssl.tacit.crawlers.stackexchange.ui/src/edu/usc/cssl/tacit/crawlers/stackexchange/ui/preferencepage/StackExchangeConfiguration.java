package edu.usc.cssl.tacit.crawlers.stackexchange.ui.preferencepage;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.usc.cssl.tacit.common.ui.CommonUiActivator;

public class StackExchangeConfiguration extends PreferencePage
		implements IWorkbenchPreferencePage, IStackExchangeConstants {
	private Text consumerKey;

	public StackExchangeConfiguration() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(CommonUiActivator.getDefault().getPreferenceStore());

	}

	@Override
	protected Control createContents(Composite parent) {
		Composite sectionClient = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sectionClient);

		Label dummy = new Label(sectionClient, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0).applyTo(dummy);

		consumerKey = createTextFields(sectionClient, true, "Consumer Key :");
		String value = getPreferenceStore().getString(CONSUMER_KEY);
		if(value!=null && !value.equals("")){
			consumerKey.setText(value);
		}
		// loadValues();

		return sectionClient;
	}

	public boolean performOk() {
		try {

			store(CONSUMER_KEY, consumerKey.getText());
			System.out.println(getPreferenceStore().getString(CONSUMER_KEY));
		} catch (Exception exception) {

			ErrorDialog.openError(Display.getDefault().getActiveShell(), "Problem Occurred", exception.getMessage(),
					new Status(IStatus.ERROR, CommonUiActivator.PLUGIN_ID, exception.getMessage()));

		}

		return super.performOk();
	}

	private Text createTextFields(Composite sectionClient, boolean editable, String lbl) {
		Label locationLbl = new Label(sectionClient, SWT.NONE);
		locationLbl.setText(lbl);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(locationLbl);

		final Text outputLocationTxt = new Text(sectionClient, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(outputLocationTxt);
		outputLocationTxt.setEditable(editable);
		return outputLocationTxt;
	}

	private void store(String name, String value) {
		getPreferenceStore().setValue(name, value);

	}

	private String load(String name) {
		return getPreferenceStore().getString(name);

	}

}
