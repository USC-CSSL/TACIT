package edu.usc.cssl.tacit.common.ui.preferencepage;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.common.ui.IPreprocessorSettingsConstant;

public class PrepocessorSettings extends PreferencePage implements
		IWorkbenchPreferencePage, IPreprocessorSettingsConstant {

	private Button lowerCase;
	private Button stemming;

	private Combo language;
	private Button cleanup;
	private Button removeStopWords;
	private Text location;
	private Text delimeters;
	private Text output;
	//private Text LatinStemmerLocation;

	public PrepocessorSettings() {
	}

	public PrepocessorSettings(String title) {
		super(title);
		// TODO Auto-generated constructor stub
	}

	public PrepocessorSettings(String title, ImageDescriptor image) {
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
		location = createStopWordPathLocation(sectionClient);

		delimeters = createDelimeterSection(sectionClient);

		stemming = createStemmingSection(sectionClient);
		lowerCase = createCheckBox(sectionClient, "Convert to Lowercase");

		output = createOutputPathLocation(sectionClient);
		cleanup = createCheckBox(sectionClient, "Clean up Pre-Processed Files ");
		initializeDefaultValues();
		loadValues();
		if (Boolean.valueOf(load(INITIAL))) {
			performDefaults();
		}

		/*
		 * Composite composite_textField = createComposite(parent, 2); Label
		 * label_textField = createLabel(composite_textField,
		 * MessageUtil.getString("Text_Field")); textField =
		 * createTextField(composite_textField); pushButton_textField =
		 * createPushButton(composite_textField,
		 * MessageUtil.getString("Change"));
		 * 
		 * //composite_tab << parent Composite composite_tab =
		 * createComposite(parent, 2); Label label1 = createLabel(composite_tab,
		 * MessageUtil.getString("Radio_Button_Options"));
		 * 
		 * // tabForward(composite_tab); //radio button composite << tab
		 * composite Composite composite_radioButton =
		 * createComposite(composite_tab, 1); radioButton1 =
		 * createRadioButton(composite_radioButton,
		 * MessageUtil.getString("Radio_button_1")); radioButton2 =
		 * createRadioButton(composite_radioButton,
		 * MessageUtil.getString("Radio_button_2")); radioButton3 =
		 * createRadioButton(composite_radioButton,
		 * MessageUtil.getString("Radio_button_3"));
		 * 
		 * 
		 * //composite_tab2 << parent Composite composite_tab2 =
		 * createComposite(parent, 2); Label label2 =
		 * createLabel(composite_tab2,
		 * MessageUtil.getString("Check_Box_Options")); //$NON-NLS-1$
		 * 
		 * // tabForward(composite_tab2); //composite_checkBox << composite_tab2
		 * Composite composite_checkBox = createComposite(composite_tab2, 1);
		 * checkBox1 = createCheckBox(composite_checkBox,
		 * MessageUtil.getString("Check_box_1")); checkBox2 =
		 * createCheckBox(composite_checkBox,
		 * MessageUtil.getString("Check_box_2")); checkBox3 =
		 * createCheckBox(composite_checkBox,
		 * MessageUtil.getString("Check_box_3"));
		 * 
		 * initializeValues();
		 */

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

	private void initializeDefaultValues() {
		getPreferenceStore().setDefault(DELIMETERS, ".,;'\\\"!-()[]{}:?/@");
		getPreferenceStore().setDefault(STOP_PATH, "");
		getPreferenceStore().setDefault(REMOVE_STOPS, "false");
		getPreferenceStore().setDefault(LOWER_CASE, "true");
		getPreferenceStore().setDefault(LANGUAGE,
				ELanguageType.EN.toString()); //changed autodetect
		getPreferenceStore().setDefault(STEMMING, "false");
		getPreferenceStore().setDefault(PRE_PROCESSED, "false");
		getPreferenceStore().setDefault(INITIAL, "true");
		getPreferenceStore().setDefault(OUTPUT_PATH,
				System.getProperty("user.dir"));
		getPreferenceStore().setDefault(LATIN_STEMMER, "");
	}

	private void setDefaultValues() {

		delimeters.setText(getPreferenceStore().getDefaultString(DELIMETERS));
		location.setText(getPreferenceStore().getDefaultString(STOP_PATH));
		lowerCase.setSelection(Boolean.valueOf(getPreferenceStore()
				.getDefaultString(LOWER_CASE)));
		stemming.setSelection(Boolean.valueOf(getPreferenceStore()
				.getDefaultString(STEMMING)));
		removeStopWords.setSelection(Boolean.valueOf(getPreferenceStore()
				.getDefaultString(REMOVE_STOPS)));
		language.setText(getPreferenceStore().getDefaultString(LANGUAGE));
		cleanup.setSelection(Boolean.valueOf(getPreferenceStore()
				.getDefaultString(PRE_PROCESSED)));
		output.setText(getPreferenceStore().getDefaultString(OUTPUT_PATH));
		//LatinStemmerLocation.setText(getPreferenceStore().getDefaultString(LATIN_STEMMER));
		language.setEnabled(false);
	}

	private void loadValues() {

		delimeters.setText(load(DELIMETERS));
		location.setText(load(STOP_PATH));
		lowerCase.setSelection(Boolean.valueOf(load(LOWER_CASE)));
		stemming.setSelection(Boolean.valueOf(load(STEMMING)));
		removeStopWords.setSelection(Boolean.valueOf(load(REMOVE_STOPS)));
		language.setText(load(LANGUAGE));
		cleanup.setSelection(Boolean.valueOf(load(PRE_PROCESSED)));
		if (stemming.getSelection()) {
			language.setEnabled(true);
		}
		output.setText(load(OUTPUT_PATH));
		//LatinStemmerLocation.setText(load(LATIN_STEMMER));

	}

	private Button createStemmingSection(Composite sectionClient) {

		final Button stemming = new Button(sectionClient, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(stemming);
		stemming.setText("Stemming");

		language = new Combo(sectionClient, SWT.BORDER | SWT.SINGLE);
		List<String> langs = new ArrayList<String>();
		String disp = "";
		for (ELanguageType enumVal : ELanguageType.values()) {
			disp = enumVal.toString();
			if (enumVal.getText().length() > 0) {
				disp = disp + " ( " + enumVal.getText() + " )";
			}
			langs.add(disp);
		}
		language.setEnabled(false);
		language.select(0);
		language.setItems(langs.toArray(new String[0]));
		GridDataFactory.fillDefaults().grab(false, false).span(2, 0)
				.applyTo(language);
		stemming.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (stemming.getSelection()) {
					language.setEnabled(true);
					//LatinStemmerLocation.setEnabled(true);
					language.select(0);
				} else {
					language.setEnabled(false);
					//LatinStemmerLocation.setEnabled(false);
				}
			}
		});
		
		/*Label locationLbl = new Label(sectionClient, SWT.NONE);
		locationLbl.setText("Latin Stemmer Location:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(locationLbl);

		LatinStemmerLocation = new Text(sectionClient, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
				.applyTo(LatinStemmerLocation);
		
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
				LatinStemmerLocation.setText(path);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});*/
		
		return stemming;
	}

	private Button createCheckBox(Composite sectionClient, String description) {

		Button option = new Button(sectionClient, SWT.CHECK);
		option.setText(description);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(option);
		Label dummyLbl = new Label(sectionClient, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0)
				.applyTo(dummyLbl);

		return option;
	}

	private Text createStopWordPathLocation(Composite sectionClient) {
		removeStopWords = createCheckBox(sectionClient, "Remove Stop Words");

		Label locationLbl = new Label(sectionClient, SWT.NONE);
		locationLbl.setText("Stop Words Location:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(locationLbl);

		final Text outputLocationTxt = new Text(sectionClient, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
				.applyTo(outputLocationTxt);
		outputLocationTxt.setEditable(false);
		outputLocationTxt.setEnabled(false);

		final Button browseBtn = new Button(sectionClient, SWT.PUSH);
		browseBtn.setText("Browse...");
		browseBtn.setEnabled(false);
		browseBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(browseBtn.getShell(), SWT.OPEN);
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

		removeStopWords.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (removeStopWords.getSelection()) {
					outputLocationTxt.setEnabled(true);
					browseBtn.setEnabled(true);

				} else {
					outputLocationTxt.setEnabled(false);
					browseBtn.setEnabled(false);
				}
			}
		});

		return outputLocationTxt;
	}

	private Text createOutputPathLocation(Composite sectionClient) {
		Label locationLbl = new Label(sectionClient, SWT.NONE);
		locationLbl.setText("Pre-Processed Files Location:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(locationLbl);

		final Text outputLocationTxt = new Text(sectionClient, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
				.applyTo(outputLocationTxt);

		// String cwd = System.getProperty("user.dir");
		// outputLocationTxt.setText(cwd);
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

	private Text createDelimeterSection(Composite sectionClient) {
		Label delimeterLbl = new Label(sectionClient, SWT.NONE);
		delimeterLbl.setText("Delimiters:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(delimeterLbl);

		Text delimetersTxt = new Text(sectionClient, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0)
				.applyTo(delimetersTxt);
		delimetersTxt.setText(getPreferenceStore().getString("delimiters"));
		return delimetersTxt;
	}

	@Override
	public boolean performOk() {

		store(INITIAL, Boolean.toString(false));
		store(DELIMETERS, delimeters.getText());
		store(STOP_PATH, location.getText());
		store(REMOVE_STOPS, Boolean.toString(removeStopWords.getSelection()));

		store(LOWER_CASE, Boolean.toString(lowerCase.getSelection()));
		store(STEMMING, Boolean.toString(stemming.getSelection()));
		store(LANGUAGE, language.getText());
		store(OUTPUT_PATH, output.getText());
		//store(LATIN_STEMMER,LatinStemmerLocation.getText());
		store(PRE_PROCESSED, Boolean.toString(cleanup.getSelection()));
		super.performApply();
		return super.performOk();
	}

	private void store(String name, String value) {
		getPreferenceStore().setValue(name, value);

	}

	private String load(String name) {
		return getPreferenceStore().getString(name);

	}

}
