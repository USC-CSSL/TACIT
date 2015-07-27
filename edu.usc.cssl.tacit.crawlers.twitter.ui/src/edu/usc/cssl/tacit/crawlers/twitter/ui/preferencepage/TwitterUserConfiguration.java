package edu.usc.cssl.tacit.crawlers.twitter.ui.preferencepage;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;
import edu.usc.cssl.tacit.common.ui.CommonUiActivator;

public class TwitterUserConfiguration extends PreferencePage implements
		IWorkbenchPreferencePage, ITwitterConstant {

	private Text userName;
	private Text consumerKey;
	private Text consumerSecret;
	private Text accessToken;
	private Text accessTokenSecret;

	public TwitterUserConfiguration() {
	}

	public TwitterUserConfiguration(String title) {
		super(title);
	}

	public TwitterUserConfiguration(String title, ImageDescriptor image) {
		super(title, image);
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
		
		userName = createHyperLink(sectionClient,  "User Name :");
		userName.setEnabled(false);
		userName.setEditable(false);
		consumerKey = createTextFields(sectionClient, true, "Consumer Key :");

		consumerSecret = createTextFields(sectionClient, true,
				"Consumer Secret :");
		accessToken = createTextFields(sectionClient, true, "Access Token :");
		accessTokenSecret = createTextFields(sectionClient, true,
				"Access Token Secret :");

		loadValues();

		return sectionClient;

	}

	@Override
	protected void performApply() {
		performOk();
		super.performApply();
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
	}

	private void loadValues() {

		consumerKey.setText(load(CONSUMER_KEY));
		userName.setText(load(USER));
		consumerSecret.setText(load(CONSUMER_SECRET));
		accessToken.setText(load(ACCESS_TOKEN));
		accessTokenSecret.setText(load(ACCESS_TOKEN_SECRET));

	}

	private Text createTextFields(Composite sectionClient, boolean editable,
			String lbl) {
		Label locationLbl = new Label(sectionClient, SWT.NONE);
		locationLbl.setText(lbl);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(locationLbl);

		final Text outputLocationTxt = new Text(sectionClient, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0)
				.applyTo(outputLocationTxt);
		outputLocationTxt.setEditable(editable);
		return outputLocationTxt;
	}
	
	private Text createHyperLink(Composite sectionClient,
			String lbl) {
		FormToolkit toolkit = new FormToolkit(sectionClient.getDisplay());
		Hyperlink link = toolkit.createHyperlink(sectionClient, "Click here.",
				SWT.WRAP);
		link.setBackground(sectionClient.getBackground());
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				Program.launch("https://blog.twitter.com/developer"); // replace with url
			}
		});
		link.setText("User Name");
	
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(link);

		final Text outputLocationTxt = new Text(sectionClient, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0)
				.applyTo(outputLocationTxt);
		outputLocationTxt.setEditable(false);
		outputLocationTxt.setEnabled(false);
		outputLocationTxt.setMessage("Click User Name to view how to fill the Consumer key, values, tokens ...");
		return outputLocationTxt;
	}


	@Override
	public boolean performOk() {

		try {
			updateUser();
			store(INITIAL, Boolean.toString(false));
			store(CONSUMER_KEY, consumerKey.getText());
			store(USER, userName.getText());
			store(CONSUMER_SECRET, consumerSecret.getText());
			store(ACCESS_TOKEN, accessToken.getText());
			store(ACCESS_TOKEN_SECRET, accessTokenSecret.getText());
		} catch (TwitterException exception) {

			ErrorDialog.openError(Display.getDefault().getActiveShell(),
					"Problem Occurred",
					exception.getErrorMessage(), new Status(
							IStatus.ERROR, CommonUiActivator.PLUGIN_ID,
							exception.getMessage()));

		}

		return super.performOk();
	}

	private void updateUser() throws TwitterException {

		// setup streamer configurations
		ConfigurationBuilder cb = new ConfigurationBuilder();

		// to string is for whitespace removal
		cb.setDebugEnabled(true)
				.setOAuthConsumerKey(consumerKey.getText().toString())
				.setOAuthConsumerSecret(consumerSecret.getText().toString())
				.setOAuthAccessToken(accessToken.getText().toString())
				.setOAuthAccessTokenSecret(
						accessTokenSecret.getText().toString());

		// Streamer initialization
		Twitter twitter = new TwitterFactory(cb.build()).getInstance();
		User user = twitter.verifyCredentials();
		String username = user.getName();
		store(USER, username);
		userName.setText(username);
	}

	private void store(String name, String value) {
		getPreferenceStore().setValue(name, value);

	}

	private String load(String name) {
		return getPreferenceStore().getString(name);

	}

}
