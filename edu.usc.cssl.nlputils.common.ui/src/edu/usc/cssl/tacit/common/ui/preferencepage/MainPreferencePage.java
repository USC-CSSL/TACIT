package edu.usc.cssl.tacit.common.ui.preferencepage;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.common.ui.ICommonUiConstants;

public class MainPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage,ICommonUiConstants {

	private Button readMe;
	public MainPreferencePage() {
		// TODO Auto-generated constructor stub
	}

	public MainPreferencePage(String title) {
		super(title);
		// TODO Auto-generated constructor stub
	}

	public MainPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
		// TODO Auto-generated constructor stub
	}

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
		
		readMe = createReadMeSection(sectionClient);
		
		initializeDefaultValues();
		loadValues();
		if (Boolean.valueOf(load(INITIAL))) {
			performDefaults();
		}
		
		return sectionClient;
	}
	
	@Override
	protected void performApply() {
		performOk();
		super.performApply();
	}

	@Override
	protected void performDefaults() {
		setDefaultValues();
		super.performDefaults();
	}
	
	private void initializeDefaultValues(){
		getPreferenceStore().setDefault(INITIAL, "true");
		getPreferenceStore().setDefault(CREATE_RUNREPORT, "true");
	}
	
	private void setDefaultValues() {
		readMe.setSelection(Boolean.valueOf(getPreferenceStore().getDefaultString(CREATE_RUNREPORT)));
	}
	
	private void loadValues(){
		readMe.setSelection(Boolean.valueOf(load(CREATE_RUNREPORT)));
	}
	
	private String load(String name) {
		return getPreferenceStore().getString(name);
	}
	
	
	private Button createReadMeSection(Composite sectionClient) {

		final Button readMe = new Button(sectionClient, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(readMe);
		readMe.setText("Generate README file");
		readMe.setSelection(true);

		return readMe;
	}
	
	@Override
	public boolean performOk() {

		store(INITIAL, Boolean.toString(false));
		store(CREATE_RUNREPORT, Boolean.toString(readMe.getSelection()));
		return super.performOk();
	}

	private void store(String name, String value) {
		getPreferenceStore().setValue(name, value);

	}

}
