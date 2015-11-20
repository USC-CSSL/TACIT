package edu.usc.cssl.tacit.common.ui.preferencepage;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.common.ui.ICommonUiConstants;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;

public class MainPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage,ICommonUiConstants {

	private Button readMe;
	private Text corpusLocation;
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
		corpusLocation = createCorpusLocation(sectionClient);
		
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
		getPreferenceStore().setDefault(CORPUS_LOCATION, System.getProperty("user.dir")
				+ System.getProperty("file.separator") + "tacit_corpora");
	}
	
	private void setDefaultValues() {
		readMe.setSelection(Boolean.valueOf(getPreferenceStore().getDefaultString(CREATE_RUNREPORT)));
		corpusLocation.setText(getPreferenceStore().getDefaultString(CORPUS_LOCATION));
	}
	
	private void loadValues(){
		readMe.setSelection(Boolean.valueOf(load(CREATE_RUNREPORT)));
		corpusLocation.setText(load(CORPUS_LOCATION));
	}
	
	private String load(String name) {
		return getPreferenceStore().getString(name);
	}
	
	
	private Button createReadMeSection(Composite sectionClient) {

		final Button readMe = new Button(sectionClient, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(readMe);
		readMe.setText("Generate README file");
		readMe.setSelection(true);

		return readMe;
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
	
	@Override
	public boolean performOk() {

		store(INITIAL, Boolean.toString(false));
		store(CREATE_RUNREPORT, Boolean.toString(readMe.getSelection()));
		store(CORPUS_LOCATION,corpusLocation.getText());
		ManageCorpora.moveCorpora();
		return super.performOk();
	}

	private void store(String name, String value) {
		getPreferenceStore().setValue(name, value);

	}

}
