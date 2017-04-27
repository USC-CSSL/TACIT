package edu.usc.cssl.tacit.wordcount.cooccurrence.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.tacit.common.Preprocessor;
import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.tacit.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.tacit.common.ui.utility.TacitUtil;
import edu.usc.cssl.tacit.common.ui.validation.OutputPathValidation;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import edu.usc.cssl.tacit.wordcount.cooccurrence.services.CooccurrenceAnalysis;
import edu.usc.cssl.tacit.wordcount.cooccurrence.ui.internal.CooccurrenceWordCountImageRegistry;
import edu.usc.cssl.tacit.wordcount.cooccurrence.ui.internal.ICooccurrenceWordCountViewConstants;

public class CooccurrenceWordCountView extends ViewPart implements
		ICooccurrenceWordCountViewConstants {
	public static String ID = "edu.usc.cssl.tacit.wordcount.cooccurrence.ui.view1";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private OutputLayoutData layoutData;
	private Button buildMAtrix;
	private Button fAddFileButton;
	private Button splitInputFileLines;
	private TableLayoutData inputLayoutData;
	private Button preprocessEnabled;
	private Text seedFile;
	private Text windowSize;
	private Text thresholdValue;
	private Job cooccurrenceAnalysisJob;
	private List<Object> selectedFiles;
	private boolean checkType = true;
	private String stemmedFilesLoc = System.getProperty("user.dir") + System.getProperty("file.separator")
	+ "tacit_temp_files" + System.getProperty("file.separator") + "stemmedFiles";
	@Override
	public void createPartControl(Composite parent) {
		toolkit = createFormBodySection(parent);
		Section section = toolkit.createSection(form.getBody(),
				Section.TITLE_BAR | Section.EXPANDED);

		GridDataFactory.fillDefaults().grab(true, false).span(3, 1)
				.applyTo(section);
		section.setExpanded(true);
		/*
		 * String description =
		 * "This section gives details about Heirarchical clustering "; FormText
		 * descriptionFrm = toolkit.createFormText(section, false);
		 * descriptionFrm.setText("<form><p>" + description + "</p></form>",
		 * true, false); section.setDescriptionControl(descriptionFrm);
		 */
		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL
				| SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
				.applyTo(sc);
		TacitFormComposite.addErrorPopup(form.getForm(), toolkit);
		TacitFormComposite.createEmptyRow(toolkit, sc);
		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1)
				.applyTo(client);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;

		inputLayoutData = TacitFormComposite.createTableSection(client,
				toolkit, layout, "Input Details",
				"Add File(s) and Folder(s) to include in analysis.", true,
				true, true, true);

		Composite compInput;
		compInput = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(3)
				.applyTo(compInput);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 1)
				.applyTo(compInput);
		createPreprocessLink(compInput);
		seedFile = createWordFileControl(compInput, "Dictionary :");
		windowSize = createAdditionalOptions(compInput, "Window Size :", "1");
		thresholdValue = createAdditionalOptions(compInput, "Threshold :", "");

		Composite client1 = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1)
				.applyTo(client1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client1);

		layoutData = TacitFormComposite.createOutputSection(toolkit, client1,
				form.getMessageManager());

		buildCooccurrenceMatrix(form.getBody());

		splitInputFileLines = toolkit.createButton(form.getBody(),
				"Create a new file for each line of the input files", SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(splitInputFileLines);
		
		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter());
		addButtonsToToolBar();
		toolkit.paintBordersFor(form.getBody());
		form.setImage(CooccurrenceWordCountImageRegistry.getImageIconFactory().getImage(ICooccurrenceWordCountViewConstants.IMAGE_COOCCURENCE_ANALYSIS_OBJ));

	}

	private Text createAdditionalOptions(Composite sectionClient,
			String numLabel, String defaultValue) {
		Text numTxt;
		Label noLabel = toolkit.createLabel(sectionClient, numLabel, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(noLabel);
		numTxt = toolkit.createText(sectionClient, "", SWT.BORDER);
		numTxt.setText(defaultValue);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0)
				.applyTo(numTxt);
		
		return numTxt;
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == Job.class) {
			return cooccurrenceAnalysisJob;
		}
		return super.getAdapter(adapter);
	}

	private void createPreprocessLink(Composite client) {

		Composite clientLink = toolkit.createComposite(client);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(3)
				.applyTo(clientLink);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 1)
				.applyTo(clientLink);

		preprocessEnabled = toolkit.createButton(clientLink, "", SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(preprocessEnabled);
		final Hyperlink link = toolkit.createHyperlink(clientLink,
				"Preprocess", SWT.NONE);
		link.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		link.addHyperlinkListener(new IHyperlinkListener() {
			@Override
			public void linkEntered(HyperlinkEvent e) {
			}

			@Override
			public void linkExited(HyperlinkEvent e) {
			}

			@Override
			public void linkActivated(HyperlinkEvent e) {
				String id = "edu.usc.cssl.tacit.common.ui.prepocessorsettings";
				PreferencesUtil.createPreferenceDialogOn(link.getShell(), id,
						new String[] { id }, null).open();
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1)
				.applyTo(link);

	}

	private void buildCooccurrenceMatrix(Composite body) {

		buildMAtrix = toolkit.createButton(body,
				"Build Co-occurrence Matrices", SWT.CHECK);

		GridDataFactory.fillDefaults().grab(false, false).span(3, 1)
				.applyTo(buildMAtrix);

	}

	private Text createWordFileControl(Composite sectionClient, String lblText) {
		Label simpleTxtLbl = toolkit.createLabel(sectionClient, lblText,
				SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(simpleTxtLbl);
		final Text simpleTxt = toolkit
				.createText(sectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(simpleTxt);
		fAddFileButton = toolkit.createButton(sectionClient, "Browse...",
				SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(fAddFileButton);
		fAddFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(fAddFileButton.getShell(),
						SWT.OPEN);
				dlg.setText("Select File");
				String path = dlg.open();
				if (path == null)
					return;
				simpleTxt.setText(path);
			}
		});

		return simpleTxt;
	}

	private FormToolkit createFormBodySection(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);

		toolkit.decorateFormHeading(form.getForm());
		form.setText("Co-occurrence Analysis"); //$NON-NLS-1$
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true)
				.applyTo(form.getBody());
		return toolkit;
	}

	private void addButtonsToToolBar() {
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {

			@Override
			public ImageDescriptor getImageDescriptor() {
				return (CooccurrenceWordCountImageRegistry
						.getImageIconFactory()
						.getImageDescriptor(IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Analyze";
			}

			@Override
			public void run() {
				if (!canProceed()) {
					return;
				}
				final String outputPath = layoutData.getOutputLabel().getText();
				final boolean isPreprocess = preprocessEnabled.getSelection();
				TacitUtil tacitHelper = new TacitUtil();
				
				try {
					selectedFiles = inputLayoutData
							.getTypeCheckedSelectedFiles(checkType);
					checkType = false;	//if control reaches here, that means the user ignored the warning, so no more checks will be made
							
				} catch (Exception e1) {
					return;
				}
				tacitHelper.writeSummaryFile(outputPath);
				final boolean isBuildMatrix = buildMAtrix.getSelection();
				final String windowSizeStr = windowSize.getText();
				final String thresholdLimit = thresholdValue.getText();
				final boolean splitFiles = splitInputFileLines.getSelection();
				TacitFormComposite
						.writeConsoleHeaderBegining("Co-occurrence Analysis started  ");
				cooccurrenceAnalysisJob = new Job("Co-occurrence Analysis...") {

					private String seedFilePath = seedFile.getText();

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						TacitFormComposite.setConsoleViewInFocus();
						TacitFormComposite.updateStatusMessage(getViewSite(),
								null, null, form);
						monitor.beginTask(
								"TACIT started co-occurence analysis...",
								selectedFiles.size() + 40);
						Preprocessor ppObj = null;
						List<String> inFiles = null;
						List<String> seedList = null;
						try {
							List<String> dictionaryFiles = new ArrayList();
							dictionaryFiles.add(seedFilePath);
							ppObj = new Preprocessor("Cooccurence",dictionaryFiles,isPreprocess);
							List<String> stemmedDictionaryFiles = ppObj.getStemmedDictionaryFiles();
							
							if (!dictionaryFiles.get(0).equals(stemmedDictionaryFiles.get(0))){ //If stemming is performed, then chose the stemmed seed file.
								seedFilePath = stemmedDictionaryFiles.get(0);
							}
							
							
							inFiles = ppObj.processData("ppFiles",
									selectedFiles, monitor);
							if(splitFiles) {
								inFiles = splitFiles("Cooccurrence", inFiles);
							}
							List<Object> seedObjs = new ArrayList<Object>();
							seedObjs.add(seedFilePath);
							seedList = ppObj
									.processData("seed_files", seedObjs, monitor);
							
							File stemmedFileDir = new File(stemmedFilesLoc);
							
							if (stemmedFileDir.exists()){
								FileUtils.deleteDirectory(stemmedFileDir);
							}
						} catch (IOException e) {
							return Status.CANCEL_STATUS;
						} catch (Exception e) {
							return Status.CANCEL_STATUS;
						}
						;

						long startTime = System.currentTimeMillis();
						boolean result = new CooccurrenceAnalysis()
								.invokeCooccurrence(inFiles, seedList.get(0),
										outputPath, windowSizeStr,
										thresholdLimit, isBuildMatrix, monitor);
						
						if (splitFiles) {
							File toDel = new File("Cooccurrence");
							try {
								FileUtils.deleteDirectory(toDel);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						ppObj.clean();
						monitor.worked(5);

						if (result) {
							ConsoleView
									.printlInConsoleln("Co-occurrence Analysis completed in "
											+ (System.currentTimeMillis() - startTime)
											+ " milliseconds.");
							return Status.OK_STATUS;
						} else {
							return Status.CANCEL_STATUS;
						}
					}
				};
				cooccurrenceAnalysisJob.setUser(true);
				if (canProceed()) {
					cooccurrenceAnalysisJob.schedule();
					cooccurrenceAnalysisJob
							.addJobChangeListener(new JobChangeAdapter() {

								@Override
								public void done(IJobChangeEvent event) {
									if (!event.getResult().isOK()) {
										TacitFormComposite
												.updateStatusMessage(
														getViewSite(),
														"Co-occurrence Analysis is met with error. Please check the log in the console",
														IStatus.ERROR, form);

										TacitFormComposite
												.writeConsoleHeaderBegining("Error: <Terminated> Co-occurrence Analysis");
										ConsoleView
												.printlInConsoleln("Take appropriate action to resolve the issues and try again.");
									} else {
										TacitFormComposite
												.updateStatusMessage(
														getViewSite(),
														"Cooccurence analysis completed",
														IStatus.OK, form);

										TacitFormComposite
												.writeConsoleHeaderBegining("Success: <Completed> Co-occurrence Analysis");

									}
								}
							});
				}
			};
		});
		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (CooccurrenceWordCountImageRegistry
						.getImageIconFactory()
						.getImageDescriptor(IMAGE_HELP_CO));
			}

			@Override
			public String getToolTipText() {
				return "Help";
			}

			@Override
			public void run() {
				PlatformUI
						.getWorkbench()
						.getHelpSystem()
						.displayHelp(
								"edu.usc.cssl.tacit.wordcount.cooccurrence.ui.cooccurrence");
			};
		};
		mgr.add(helpAction);
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(helpAction,
						"edu.usc.cssl.tacit.wordcount.cooccurrence.ui.cooccurrence");
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(form,
						"edu.usc.cssl.tacit.wordcount.cooccurrence.ui.cooccurrence");
		form.getToolBarManager().update(true);
	}

	private boolean canProceed() {
		TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
		//Remove all previously added error messages
		form.getMessageManager().removeMessage("location");
		form.getMessageManager().removeMessage("input");
		form.getMessageManager().removeMessage("noDictionary");
		form.getMessageManager().removeMessage("invalidDictionary");
		form.getMessageManager().removeMessage("windowsize");
		form.getMessageManager().removeMessage("threshold");

		// validate input
		try {
			if (inputLayoutData.getTypeCheckedSelectedFiles(checkType).size() < 1) {
				form.getMessageManager().addMessage("input","Select/Add atleast one input file", null,IMessageProvider.ERROR);
				return false;
			}
			checkType = false;	//if control reaches here, that means the user ignored the warning, so no more checks will be made
			
		} catch (Exception e1) {
			return false;
		}
				
		//Check if empty dictionary
		if (seedFile == null || seedFile.getText().isEmpty()){
			form.getMessageManager().addMessage("noDictionary","Select/Add Dictionary file", null,IMessageProvider.ERROR);
			return false;
		}
		
		//Validate Dictionary
		/*
		 * Validation rules for dictionary:
		 * 1. File must be with the extension of .txt
		 * 2. Each line must contain at least one alphanumeric word with no spaces. Special characters are allowed.
		 * 3.At least one word must be present in the dictionary.(Dictionary cannot be empty)
		 */
		StringBuffer errorMessage = new StringBuffer("");
		if (!isValidDictionary(seedFile.getText(),errorMessage)){
			form.getMessageManager().addMessage("invalidDictionary",errorMessage.toString(), null,IMessageProvider.ERROR);
			return false;
		}
		
		//Check window size
		try{
			if (windowSize.getText().isEmpty() || Integer.parseInt(windowSize.getText()) < 1) {
				form.getMessageManager().addMessage("windowsize","Window should be an integer value greater than 1", null,IMessageProvider.ERROR);
				return false;
			}
		}catch(NumberFormatException e){
			form.getMessageManager().addMessage("windowsize","Window should be an integer value greater than 1", null,IMessageProvider.ERROR);
			return false;
		}
	

		//Check threshold value
		try{
			if (thresholdValue.getText().isEmpty() || Integer.parseInt(thresholdValue.getText()) < 1) {
				form.getMessageManager().addMessage("threshold","Threshold should be an integer value greater than or equal to 1",null, IMessageProvider.ERROR);
				return false;
			}	
		}catch(NumberFormatException e){
			form.getMessageManager().addMessage("threshold","Threshold should be an integer value greater than or equal to 1",null, IMessageProvider.ERROR);
			return false;
		}
		
		
		//Compare window size with threshold
		if (Integer.parseInt(thresholdValue.getText()) > Integer.parseInt(windowSize.getText())) {
			form.getMessageManager().addMessage("threshold","Window size should not be less than threshold", null,IMessageProvider.ERROR);
			return false;
		}
		
		//Validate output path
		String message = OutputPathValidation.getInstance().validateOutputDirectory(layoutData.getOutputLabel().getText(),"Output");
		if (message != null) {
			message = layoutData.getOutputLabel().getText() + " " + message;
			form.getMessageManager().addMessage("location", message, null,IMessageProvider.ERROR);
			return false;
		}

		return true;
	}

	@Override
	public String getPartName() {
		// TODO Auto-generated method stub
		return "Co-occurrence Analysis";
	}
	
	/**
	 * Validation method for the input dictionary file
	 * @param dictionaryPath The string input path of the dictionary file 
	 * @return isValid boolean
	 */
	public boolean isValidDictionary(String dictionaryPath,StringBuffer errorMessage){
		if (!dictionaryPath.contains(".txt")){
			errorMessage.append("Invalid Dictionary Format. Reason: Dictionary format is not .txt");
			return false;
		}
		File dictionaryFile = new File(dictionaryPath);
		
		if (!dictionaryFile.exists()){
			errorMessage.append("Dictionary file not found");
			return false;
		}else{
			try {
				BufferedReader br = new BufferedReader(new FileReader(dictionaryFile));
				long size = 0l;
				String currentLine  = "";
				int lineNum = 1;
				while ((currentLine = br.readLine() )!= null){
					currentLine = currentLine.trim();
					if (currentLine.equals("")){
						errorMessage.append("Invalid Dictionary Format. Reason: Empty string at line "+lineNum);
						return false;
					}
					if (currentLine.contains(" ")){
						errorMessage.append("Invalid Dictionary Format. Reason: Multiple words found in a same line at line "+lineNum);
						return false;
					}else{
						size++;
					}
					lineNum++;
				}
				if (size == 0l){
					errorMessage.append("Invalid Dictionary Format. Reason: Empty Dictionary ");
					return false;
				}
			} catch (Exception e) {
				errorMessage.append("Exception occured while reading dictionary.");
				return false;
			}
			
			return true;
		}
	}

	@Override
	public void setFocus() {
		form.setFocus();
	}

	List<String> splitFiles(String outputdir, List<String> files) {
		File dir = new File(outputdir);
		dir.mkdir();
		FileReader fr = null;
		int count = 0;
		List<String> outputFiles = new ArrayList<String>();
		for (String strFile:files) {
			try {
				fr = new FileReader(new File(strFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			BufferedReader br = new BufferedReader(fr);
		
			String line;
			try {
				while((line = br.readLine())!=null) {
					String outputFile = outputdir + System.getProperty("file.separator") +"file_"+count;
					outputFiles.add(outputFile);
					File file = new File(outputFile);
					FileWriter fw = new FileWriter(file);
					fw.write(line);
					fw.close();
					count+=1;
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return outputFiles;
		
	  }
}
