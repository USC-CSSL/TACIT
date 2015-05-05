/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */ 
 
package edu.usc.cssl.nlputils.plugins.twitterStreamer.parts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Scanner;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;




//import bsh.EvalError;
import edu.usc.cssl.nlputils.application.handlers.GlobalPresserSettings;
import edu.usc.cssl.nlputils.utilities.Log;
import edu.usc.cssl.nlputils.plugins.twitterStreamer.process.TTStream;
import edu.usc.cssl.nlputils.plugins.twitterStreamer.process.QueryProcess;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.custom.CTabItem;
import org.osgi.framework.FrameworkUtil;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class TwitterStreamerSettings {
	
	//////////////////////////////
	/////Variables
	//////////////////////////////
	
	// Text variables store text box values and use where necessary
	private Text txtConsumerKey;
	private Text txtConsumerSecret;
	private Text txtAccessToken;
	private Text txtAccessTokenSecret;
	
	private Text txtWordFilter;
	private Text txtGeoFilter;
	
	private Text txtLimitDay;
	private Text txtLimitHour;
	private Text txtLimitMin;
	private Text txtLimitNumber;
	
	private Text txtQuery;
	private Text txtQueryInput;
	private Text txtQueryOutput;
	
	boolean validUser; // shows if a valid user is loaded into the system
	
	boolean storedAtt[]; // Which attributes user wants to store in it's file
	long finishTime; // How long streamer is going to stream?
	boolean noWordFilter; 
	boolean noLocationFilter;
	
	long maxTweetLimit = 0;
	
	private String username = "";
	
	
	///////////////////////////////
	//////Functions
	///////////////////////////////
	
	// If there is valid user setup in the system it will return the user otherwise throws exception
	private User UserCheck () throws FileNotFoundException, TwitterException{
		File fi = new File("AuthKey.txt");
		ConfigurationBuilder cb = new ConfigurationBuilder();
		
		String consumerKey = "";
		String consumerSecret = "";
		String accessToken = "";
		String accessTokenSecret = "";
	    
		Scanner sc = new Scanner(fi);
			
		if(sc.hasNext())
			consumerKey = sc.nextLine();
		if(sc.hasNext())
			consumerSecret = sc.nextLine();
		if(sc.hasNext())
			accessToken = sc.nextLine();
		if(sc.hasNext())
			accessTokenSecret = sc.nextLine();
		
		
		cb.setDebugEnabled(true)
			.setOAuthConsumerKey(consumerKey)
			.setOAuthConsumerSecret(consumerSecret)
			.setOAuthAccessToken(accessToken)
			.setOAuthAccessTokenSecret(accessTokenSecret);
		
		Twitter twitter = new TwitterFactory(cb.build()).getInstance();
		sc.close();
		
		return twitter.verifyCredentials();
	}
	
	
	@Inject
	public TwitterStreamerSettings() {
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
		validUser = false;
		final Shell shell = parent.getShell();
		parent.setLayout(new GridLayout(6, false));
		Label header = new Label(parent, SWT.NONE);
		header.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 6, 1));
		header.setImage(ImageDescriptor.createFromURL(
				FileLocator.find(FrameworkUtil.getBundle(this.getClass()),
						new Path("plugin_icon/icon.png"), null)).createImage());
		
		Group grpUser = new Group(parent, SWT.NONE);
		GridData gd_grpUser = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd_grpUser.heightHint = 241;
		gd_grpUser.widthHint = 535;
		grpUser.setLayoutData(gd_grpUser);
		grpUser.setText("User");
				
		// user name field
		Label lblUserName = new Label(grpUser, SWT.NONE);
		lblUserName.setBounds(10, 26, 125, 20);
		lblUserName.setText("Username");
				
		final Label lblUser = new Label(grpUser, SWT.NONE);
		lblUser.setBounds(172, 26, 332, 20);
		lblUser.setText(username);
		
		// consumer key field
		Label lblConsumerKey = new Label(grpUser, SWT.NONE);
		lblConsumerKey.setBounds(10, 62, 125, 20);
		lblConsumerKey.setText("Consumer Key");
		
		txtConsumerKey = new Text(grpUser, SWT.BORDER);
		txtConsumerKey.setBounds(172, 62, 332, 21);
		
		// consumer secret field
		
		Label lblConsumerSecret = new Label(grpUser, SWT.NONE);
		lblConsumerSecret.setBounds(10, 98, 125, 20);
		lblConsumerSecret.setText("Consumer Secret");
		
		txtConsumerSecret = new Text(grpUser, SWT.BORDER);
		txtConsumerSecret.setBounds(172, 98, 332, 21);
		
		// access token field
		
		Label lblAccessToken = new Label(grpUser, SWT.NONE);
		lblAccessToken.setBounds(10, 134, 125, 20);
		lblAccessToken.setText("Access Token");
		
		txtAccessToken = new Text(grpUser, SWT.BORDER);
		txtAccessToken.setBounds(172, 134, 332, 21);
		
		// access token secret field
		
		Label lblAccessTokenSecret = new Label(grpUser, SWT.NONE);
		lblAccessTokenSecret.setBounds(10, 170, 125, 20);
		lblAccessTokenSecret.setText("Access Token Secret");
		
		txtAccessTokenSecret = new Text(grpUser, SWT.BORDER);
		txtAccessTokenSecret.setBounds(172, 170, 332, 21);
		
		
		// warning Field
		
		final Label lblWarning = new Label(grpUser, SWT.NONE);
		lblWarning.setBounds(172, 206, 332, 40);
		lblWarning.setText("");
		lblUser.setText(username);
		
		
		/////////////////////////
		///Update user button
		/////////////////////////
		
		Button buttonUser = new Button(grpUser, SWT.NONE);
		buttonUser.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		buttonUser.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				
				// setup streamer configurations
				ConfigurationBuilder cb = new ConfigurationBuilder();
				
				// to string is for whitespace removal
				cb.setDebugEnabled(true)
					.setOAuthConsumerKey(txtConsumerKey.getText().toString())
					.setOAuthConsumerSecret(txtConsumerSecret.getText().toString())
					.setOAuthAccessToken(txtAccessToken.getText().toString())
					.setOAuthAccessTokenSecret(txtAccessTokenSecret.getText().toString());
				
				// Streamer initialization
				Twitter twitter = new TwitterFactory(cb.build()).getInstance();
				try {
					User user = twitter.verifyCredentials();
					username = user.getName();
					lblUser.setText(username);
					lblWarning.setText("");
					// Where should the file be stored?
					File fileAuth = new File("AuthKey.txt");
					BufferedWriter output = new BufferedWriter(new FileWriter(fileAuth));
					output.write(txtConsumerKey.getText().concat("\n"));
					output.write(txtConsumerSecret.getText().concat("\n"));
					output.write(txtAccessToken.getText().concat("\n"));
					output.write(txtAccessTokenSecret.getText().concat("\n"));
					output.flush();
					output.close();
					lblWarning.setText("User has successfully been updated.");
					
				} catch (TwitterException e2) {
					lblWarning.setText("Error: Invalid update, user has remained unchanged if exists.");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		buttonUser.setBounds(0, 213, 71, 18);
		buttonUser.setText("Update");
		
		
		
		// test if there is a valid user setup
		try {
			User user = UserCheck();
			username = user.getName();
			lblUser.setText(username);
			validUser = true;
			
		} catch (IOException e1){
			lblWarning.setText("Error: Invalid user or no predefined user selected. Update with valid keys.");
		}
		catch (TwitterException e2) {
			lblWarning.setText("Error: Invalid user or no predefined user selected. Update with valid keys.");
		}
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Group grpOptions = new Group(parent, SWT.NONE);
		GridData gd_grpOptions = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd_grpOptions.heightHint = 237;
		gd_grpOptions.widthHint = 545;
		grpOptions.setLayoutData(gd_grpOptions);
		grpOptions.setText("Options");
		
		CTabFolder tabFolder_1 = new CTabFolder(grpOptions, SWT.BORDER);
		tabFolder_1.setBounds(10, 10, 524, 199);
		tabFolder_1.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		// Filter Tab
		CTabItem tbtmFilter = new CTabItem(tabFolder_1, SWT.NONE);
		tbtmFilter.setText("filter");
		tabFolder_1.setSelection(tbtmFilter);
		
		Group grpFilter = new Group(tabFolder_1, SWT.NONE);
		tbtmFilter.setControl(grpFilter);
		
		Label lblWordFilter = new Label(grpFilter, SWT.NONE);
		lblWordFilter.setBounds(10, 28, 105, 20);
		lblWordFilter.setText("Word Filter");
		
		txtWordFilter = new Text(grpFilter, SWT.BORDER);
		txtWordFilter.setBounds(134, 28, 337, 21);
		
		Label lblWordFilterDescript = new Label(grpFilter, SWT.NONE);
		lblWordFilterDescript.setBounds(10, 64, 430, 20);
		lblWordFilterDescript.setText("Use semicolon as delimiter. For example: hipster;#dragon;mustache");
		
		Label lblGeoFilter = new Label(grpFilter, SWT.NONE);
		lblGeoFilter.setBounds(10, 100, 105, 20);
		lblGeoFilter.setText("Geo Filter");
		
		txtGeoFilter = new Text(grpFilter, SWT.BORDER);
		txtGeoFilter.setBounds(134, 100, 337, 21);
		
		Label lblGeoFilterDescript = new Label(grpFilter, SWT.NONE);
		lblGeoFilterDescript.setBounds(10, 136, 450, 20);
		lblGeoFilterDescript.setText("Use colon then semicolon as delimiters. For example: 1,3.4;5.61,72.8");
		
		
		// Limit tab
		
		CTabItem tbtmLimit = new CTabItem(tabFolder_1, SWT.NONE);
		tbtmLimit.setText("Limit ");
		
		Group grpLimit = new Group(tabFolder_1, SWT.NONE);
		tbtmLimit.setControl(grpLimit);
		
		final Button btnTimeLimit = new Button(grpLimit, SWT.CHECK);
		btnTimeLimit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnTimeLimit.getSelection()){
					txtLimitDay.setEnabled(true);
					txtLimitHour.setEnabled(true);
					txtLimitMin.setEnabled(true);
					} else {
					txtLimitDay.setEnabled(false);
					txtLimitHour.setEnabled(false);
					txtLimitMin.setEnabled(false);
					
				}
			}
		});
		btnTimeLimit.setBounds(10, 29, 93, 20);
		btnTimeLimit.setText("Time Limit");
		
		txtLimitDay = new Text(grpLimit, SWT.BORDER);
		txtLimitDay.setText("0");
		txtLimitDay.setEnabled(false);
		txtLimitDay.setBounds(139, 30, 50, 21);
		
		Label lblLimitDay = new Label(grpLimit, SWT.NONE);
		lblLimitDay.setBounds(195, 29, 65, 20);
		lblLimitDay.setText("Days");
		
		txtLimitHour = new Text(grpLimit, SWT.BORDER);
		txtLimitHour.setText("0");
		txtLimitHour.setEnabled(false);
		txtLimitHour.setBounds(260, 29, 50, 21);
		
		Label lblLimitHour = new Label(grpLimit, SWT.NONE);
		lblLimitHour.setBounds(320, 29, 65, 20);
		lblLimitHour.setText("Hours");
		
		txtLimitMin = new Text(grpLimit, SWT.BORDER);
		txtLimitMin.setText("1");
		txtLimitMin.setEnabled(false);
		txtLimitMin.setBounds(385, 29, 50, 21);
		
		Label lblLimitMin = new Label(grpLimit, SWT.NONE);
		lblLimitMin.setBounds(445, 29, 65, 20);
		lblLimitMin.setText("Minutes");
		
		final Button btnTweetLimit = new Button(grpLimit, SWT.CHECK);
		btnTweetLimit.setSelection(true);
		btnTweetLimit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnTweetLimit.getSelection()){
					txtLimitNumber.setEnabled(true);
					} else {
					txtLimitNumber.setEnabled(false);
					
				}
			}
		});
		btnTweetLimit.setBounds(10, 65, 93, 20);
		btnTweetLimit.setText("Max Tweet");
		
		txtLimitNumber = new Text(grpLimit, SWT.BORDER);
		txtLimitNumber.setText("100");
		txtLimitNumber.setBounds(135, 65, 50, 21);
		
		CTabItem tbtmAttribute = new CTabItem(tabFolder_1, SWT.NONE);
		tbtmAttribute.setText("Stored Attributes");
		
		Group grpAttribute = new Group(tabFolder_1, SWT.NONE);
		tbtmAttribute.setControl(grpAttribute);
		
		// Username
		
		final Button btnUserName = new Button(grpAttribute, SWT.CHECK);
		btnUserName.setBounds(9, 29, 123, 16);
		btnUserName.setText("User Name");
		
		// Text
		final Button btnText = new Button(grpAttribute, SWT.CHECK);
		btnText.setSelection(true);
		btnText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnText.setBounds(9, 65, 123, 16);
		btnText.setText("Text");
				
		
		// Retweet
		final Button btnRetweet = new Button(grpAttribute, SWT.CHECK);
		btnRetweet.setBounds(9, 101, 123, 16);
		btnRetweet.setText("Retweet number");
				
						
		// Geolocation
		final Button btnGeo = new Button(grpAttribute, SWT.CHECK);
		btnGeo.setBounds(139, 29, 123, 16);
		btnGeo.setText("Geolocation");
						
		// Created At
		final Button btnCreated = new Button(grpAttribute, SWT.CHECK);
		btnCreated.setBounds(139, 65, 123, 16);
		btnCreated.setText("Created at");
										
		// Favorite count
		final Button btnFavCount = new Button(grpAttribute, SWT.CHECK);
		btnFavCount.setBounds(139, 101, 123, 16);
		btnFavCount.setText("Favorite Count");
														
		// ID
		final Button btnID = new Button(grpAttribute, SWT.CHECK);
		btnID.setBounds(269, 29, 123, 16);
		btnID.setText("Status ID");
																		
		// Language
		final Button btnLang = new Button(grpAttribute, SWT.CHECK);
		btnLang.setBounds(269, 65, 123, 16);
		btnLang.setText("Language");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Label label = new Label(parent, SWT.NONE);
		label.setText("Filename");
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		txtOutputFileName = new Text(parent, SWT.BORDER);
		txtOutputFileName.setText("Stream.txt");
		GridData gd_txtOutputFileName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtOutputFileName.widthHint = 436;
		txtOutputFileName.setLayoutData(gd_txtOutputFileName);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Label lblFilePath = new Label(parent, SWT.NONE);
		lblFilePath.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFilePath.setText("File Path");
		
		txtOutputFilePath = new Text(parent, SWT.BORDER);
		GridData gd_txtOutputFilePath = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtOutputFilePath.widthHint = 437;
		txtOutputFilePath.setLayoutData(gd_txtOutputFilePath);
		
		Button button = new Button(parent, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog od = new DirectoryDialog(shell);
				od.open();
				String oDirectory = od.getFilterPath();
				txtOutputFilePath.setText(oDirectory);
			}
		});
		button.setText("...");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		////////////////////////////////
		////Stream button
		////////////////////////////////
		
		Button btnStream = new Button(parent, SWT.NONE);
		btnStream.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				
				// Make sure input values are in valid state then launch streamer

				boolean validGeoFilter = true;
				boolean validLimitParse = true;
				boolean validLimitState = true;
				boolean validFile = true;
				
				noWordFilter = false;
				noLocationFilter = false;
				
				long dayLimit;
				long hourLimit;
				long minLimit;
				
				// Is there any word filter?
				final String keyWords[] = txtWordFilter.getText().split(";");
				if (keyWords.length == 0 || txtWordFilter.getText().isEmpty())
					noWordFilter = true;
				
				
				// check geofilter string validity
				String geoWords[] = txtGeoFilter.getText().split(";");
				final double[][] geoLocations = new double[geoWords.length][2];
				if ( (geoWords.length%2 == 1) && (geoWords.length != 1) ) // check valid number of pairs
					validGeoFilter = false;
				else{
					for (int i=0; i<geoWords.length && !txtGeoFilter.getText().isEmpty(); i++){
						String geoTemp[] = geoWords[i].split(",");
						if (geoTemp.length != 2) // check if there are only two values in the pair
							validGeoFilter = false;
						else{
							if(geoTemp[0].isEmpty() || geoTemp[1].isEmpty()) 
								validGeoFilter = false; // check if there is something to be read
							else{
								try{ // check if there is a valid number
									geoLocations[i][0] = Double.parseDouble(geoTemp[0]);
									geoLocations[i][1] = Double.parseDouble(geoTemp[1]);
								}
								catch (NumberFormatException e2){
									validGeoFilter = false;
								}
							}
					
					
						}
					}
				}
				// is there any geofilter?
				if (geoLocations.length == 0 || txtGeoFilter.getText().isEmpty())
					noLocationFilter = true;
				
				// check limit tab attributes validity
				if (txtLimitDay.isEnabled()){
					try { 
						dayLimit = Long.parseLong(txtLimitDay.getText().toString()); 
						hourLimit = Long.parseLong(txtLimitHour.getText().toString()); 
						minLimit = Long.parseLong(txtLimitMin.getText().toString()); 
						// Is streaming time positive interger?
						if (dayLimit < 0 || hourLimit<0 || minLimit < 0 || (dayLimit == 0 && hourLimit == 0 && minLimit == 0))
							validLimitParse = false;
						finishTime = (minLimit+ (hourLimit+dayLimit*24)*60)*1000*60;
					} catch(NumberFormatException e1) { 
						validLimitParse = false; 
					}
				}
				// Is there valid max tweet number?
				if (txtLimitNumber.isEnabled()){
					try { 
						maxTweetLimit = Long.parseLong(txtLimitNumber.getText().toString());
						if (maxTweetLimit <= 0)
							validLimitParse = false;
					} catch(NumberFormatException e1) { 
						validLimitParse = false; 
					}
				}
				// either maxTweet or maxTime must be activated
				if (!txtLimitDay.isEnabled() && !txtLimitNumber.isEnabled()){
					validLimitState = false;
				}
				
				// check if the output address is correct and writable
				final String outputFile = txtOutputFilePath.getText().toString().concat("/").concat(txtOutputFileName.getText().toString());
				System.out.println(outputFile);
				File fileOut = new File(outputFile);
				try {
					BufferedWriter output = new BufferedWriter(new FileWriter(fileOut));
					output.close();
				} catch (IOException e1) {
					validFile = false;
				}
				
				storedAtt = new boolean[8];
				
				// Get stored attribute values
				storedAtt[0] = btnUserName.getSelection();
				storedAtt[1] = btnText.getSelection();
				storedAtt[2] = btnRetweet.getSelection();
				storedAtt[3] = btnGeo.getSelection();
				storedAtt[4] = btnCreated.getSelection();
				storedAtt[5] = btnFavCount.getSelection();
				storedAtt[6] = btnID.getSelection();
				storedAtt[7] = btnLang.getSelection();
				
				
				
				// first check if everything works then stream
				if (!validUser){
					MessageBox mBox = new MessageBox(shell,SWT.OK);
					mBox.setMessage("Error: Invalid username. Please update a valid username.");
					mBox.open();
				}
				else if (!validGeoFilter){
					MessageBox mBox = new MessageBox(shell,SWT.OK);
					mBox.setMessage("Error: Invalid gelocation query. Please compare with example.");
					mBox.open();
					
				}
				else if (!validLimitParse){
					MessageBox mBox = new MessageBox(shell,SWT.OK);
					mBox.setMessage("Error: Invalid parsing. Please makes sure all enabled fields are positive discrete numbers.");
					mBox.open();
					
				}
				else if (!validLimitState){
					MessageBox mBox = new MessageBox(shell,SWT.OK);
					mBox.setMessage("Error: Invalid Limit state. Please makes sure at least one limit is checked.");
					mBox.open();
					
				}
				else if (!validFile){
					MessageBox mBox = new MessageBox(shell,SWT.OK);
					mBox.setMessage("Error: cannot write in the file mentioned. Please makes sure the address is valid and writable.");
					mBox.open();
					
				}
				else{
					final TTStream ttStream = new TTStream();
					IEclipseContext iEclipseContext = context;
					ContextInjectionFactory.inject(ttStream,iEclipseContext);
					/*Job job = new Job("Twitter Stream Job"){
						protected IStatus run(IProgressMonitor monitor){ 
						*/
							try {
								ttStream.Stream( outputFile, txtLimitNumber.isEnabled(), maxTweetLimit, txtLimitDay.isEnabled(), finishTime, 
									noWordFilter, keyWords, noLocationFilter, geoLocations, storedAtt);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}/*
						return Status.OK_STATUS;
						}
					};
					job.setUser(true);
					job.schedule();*/
				}
				
				
			}
		});
		btnStream.setText("Stream");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Label lblQueryProcessing = new Label(parent, SWT.NONE);
		lblQueryProcessing.setText("Query Processing");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Label lblQuery = new Label(parent, SWT.NONE);
		lblQuery.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblQuery.setText("Query");
		
		txtQuery = new Text(parent, SWT.BORDER);
		GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_text.widthHint = 430;
		txtQuery.setLayoutData(gd_text);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Label lblInput = new Label(parent, SWT.NONE);
		lblInput.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblInput.setText("input");
		
		txtQueryInput = new Text(parent, SWT.BORDER);
		GridData gd_text_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_text_1.widthHint = 430;
		txtQueryInput.setLayoutData(gd_text_1);
		
		Button button_1 = new Button(parent, SWT.NONE);
		button_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog ifDialog = new FileDialog(shell, SWT.MULTI);
				ifDialog.open();
				txtQueryInput.setText(ifDialog.getFilterPath()+System.getProperty("file.separator")+ifDialog.getFileName());
			}
		});
		button_1.setText("...");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Label lblFilename = new Label(parent, SWT.NONE);
		lblFilename.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFilename.setText("output Path");
		
		txtQueryOutput = new Text(parent, SWT.BORDER);
		GridData gd_text1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_text1.widthHint = 430;
		gd_text.widthHint = 434;
		txtQueryOutput.setLayoutData(gd_text1);
		
		Button button_2 = new Button(parent, SWT.NONE);
		button_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog od = new DirectoryDialog(shell);
				od.open();
				String oDirectory = od.getFilterPath();
				txtQueryOutput.setText(oDirectory);
			}
		});
		button_2.setText("...");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Button btnQuery = new Button(parent, SWT.NONE);
		btnQuery.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnQuery.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				try{
					final QueryProcess queryProcess = new QueryProcess();
					queryProcess.Query(txtQueryInput.getText(),txtQueryOutput.getText(),txtQuery.getText());
					} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnQuery.setText("Query");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		
	}
	
	/*
	private String doPp(String inputPath) throws IOException{
		return ppService.doPreprocessing(inputPath);
	}
	 */
	
	
	@Inject IEclipseContext context;
	private Text txtOutputFileName;
	private Text txtOutputFilePath;
	private Text text;
	private void appendLog(String message){
		Log.append(context,message);
	}
	
	private void showError(Shell shell){
		MessageBox message = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		message.setMessage("Please select input and output paths");
		message.setText("Error");
		message.open();
	}
}