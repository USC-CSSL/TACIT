package edu.usc.cssl.tacit.topicmodel.turbotopics.ui;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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
import edu.usc.cssl.tacit.topicmodel.lda.services.LdaAnalysis;
import edu.usc.cssl.tacit.topicmodel.turbotopics.services.LDAtopics;
import edu.usc.cssl.tacit.topicmodel.turbotopics.ui.internal.ITurboTopicsModelViewConstants;
import edu.usc.cssl.tacit.topicmodel.turbotopics.ui.internal.TurboTopicsModelViewImageRegistry;

public class TurboTopicsModelView extends ViewPart implements
		ITurboTopicsModelViewConstants {
	public static final String ID = "edu.usc.cssl.tacit.topicmodel.turbotopics.ui.view1";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private Button preprocessEnabled;
	private Button usePerm;

	private OutputLayoutData layoutData;
	private TableLayoutData inputLayoutData;
	private Text numberOfTopics;
	private LdaAnalysis lda = null;
	protected Job job;
	Text pValueTxt;
	Text minCountTxt;
	private boolean checkType = true;
	private List<Object> selectedFiles;
	
	@Override
	public void createPartControl(Composite parent) {
		
		toolkit = createFormBodySection(parent);
		Section section = toolkit.createSection(form.getBody(),
				Section.TITLE_BAR | Section.EXPANDED);

		GridDataFactory.fillDefaults().grab(true, false).span(3, 1)
				.applyTo(section);
		section.setExpanded(true);
		
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
		// create input layout
		inputLayoutData = TacitFormComposite.createTableSection(client,
				toolkit, layout, "Input Details",
				"Add File(s) and Folder(s) to include in analysis.", true,
				true, true, true);
		Composite compInput;
		compInput = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(3)
				.applyTo(compInput);
		GridDataFactory.fillDefaults().grab(false, false).indent(10,0).span(3, 1)
				.applyTo(compInput);
		createPreprocessLink(compInput);
		numberOfTopics = createAdditionalOptions(compInput, "No. of Topics :",
				"2");

		Composite client1 = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1)
				.applyTo(client1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client1);

		layoutData = TacitFormComposite.createOutputSection(toolkit, client1,
				form.getMessageManager());

		// we dont need stop word's as it will be taken from the preprocessor
		// settings

		Composite output = layoutData.getSectionClient();


		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter());
		// form.setMessage("Invalid path", IMessageProvider.ERROR);
		this.setPartName("Turbo Topics");
		addButtonsToToolBar();
		toolkit.paintBordersFor(form.getBody());
		form.setImage(TurboTopicsModelViewImageRegistry.getImageIconFactory().getImage(ITurboTopicsModelViewConstants.IMAGE_TURBO_TOPICS_OBJ));

	}

	private void createPreprocessLink(Composite client) {
		Composite clientLink = toolkit.createComposite(client);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(3)
				.applyTo(clientLink);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 1)
				.applyTo(clientLink);
		
		

		Composite preprocessComposite = toolkit.createComposite(clientLink);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(3)
		.applyTo(preprocessComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1)
		.applyTo(preprocessComposite);
		preprocessEnabled = toolkit.createButton(preprocessComposite, "", SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(preprocessEnabled);
		final Hyperlink link = toolkit.createHyperlink(preprocessComposite,
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
		usePerm = toolkit.createButton(clientLink, "Use Permutation: Check if likelihood ratio score should be generated using permutations.", SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 1)
		.applyTo(usePerm);

		Label minCountLbl = toolkit.createLabel(clientLink, "Minimum count of word occurences to be used:",	SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
		.applyTo(minCountLbl);
		minCountTxt = toolkit.createText(clientLink, "25", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1)
		.applyTo(minCountTxt);
		Composite pComposite = toolkit.createComposite(clientLink);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(3)
		.applyTo(pComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1)
		.applyTo(pComposite);
		Label pValuetLbl = toolkit.createLabel(pComposite, "P-value:",	SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
		.applyTo(pValuetLbl);
		pValueTxt = toolkit.createText(pComposite, "0.001", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1)
		.applyTo(pValueTxt);
	}

	private Text createAdditionalOptions(Composite sectionClient,
			String lblText, String defaultText) {
		Label simpleTxtLbl = toolkit.createLabel(sectionClient, lblText,
				SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(simpleTxtLbl);
		Text simpleTxt = toolkit.createText(sectionClient, "", SWT.BORDER);
		simpleTxt.setText(defaultText);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0)
				.applyTo(simpleTxt);
		return simpleTxt;
	}

	private FormToolkit createFormBodySection(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);

		toolkit.decorateFormHeading(form.getForm());
		form.setText("Turbo Topics"); //$NON-NLS-1$
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true)
				.applyTo(form.getBody());
		return toolkit;
	}

	private void addButtonsToToolBar() {
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (TurboTopicsModelViewImageRegistry.getImageIconFactory()
						.getImageDescriptor(IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Analyze";
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.Action#run()
			 */
			@Override
			public void run() {
				if (!canProceedCluster()) {
					return;
				}
				final int noOfTopics = Integer
						.valueOf(numberOfTopics.getText()).intValue();
				final int minCount = Integer.parseInt(minCountTxt.getText());
				final double pValue = Double.parseDouble(pValueTxt.getText());
				final boolean usePermBool = usePerm.getSelection();
				final boolean isPreprocess = preprocessEnabled.getSelection();
				if(isPreprocess)
					checkType = false;
				final String outputPath = layoutData.getOutputLabel().getText();
				TacitUtil tacitHelper = new TacitUtil();
				
				try {
					selectedFiles = inputLayoutData
							.getTypeCheckedSelectedFiles(checkType);
				} catch (Exception e2) {
					return;
				}
				lda = new LdaAnalysis(){
					@Override
					protected void createRunReport(Date date){
						
					}
				};
				tacitHelper.writeSummaryFile(outputPath);

				TacitFormComposite
						.writeConsoleHeaderBegining("Topic Modelling started  ");
				job = new Job("Analyzing...") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						TacitFormComposite.setConsoleViewInFocus();
						TacitFormComposite.updateStatusMessage(getViewSite(),
								null, null, form);
						monitor.beginTask("TACIT started analyzing...", 100);
						String topicModelDirPath = System
								.getProperty("user.dir")
								+ File.separator
								+ "TopicModel"
								+ "_"
								+ String.valueOf(System.currentTimeMillis());
						Preprocessor ppObj = null;
						List<String> inFiles;
						try {
							ppObj = new Preprocessor("LDA", isPreprocess);
							inFiles = ppObj.processData("LDA", selectedFiles);

							for (String filename : inFiles) {
								File srcFile = new File(filename);
								File destDir = new File(topicModelDirPath);
								try {
									FileUtils.copyFileToDirectory(srcFile,
											destDir, false);
								} catch (IOException e) {
									e.printStackTrace();
									return Status.CANCEL_STATUS;
								}
							}
						} catch (IOException e1) {
							e1.printStackTrace();
							return Status.CANCEL_STATUS;
						} catch (Exception e1) {
							e1.printStackTrace();
							return Status.CANCEL_STATUS;
						}

						lda.initialize(topicModelDirPath, noOfTopics,
								topicModelDirPath,true);

						// lda processsing
						long startTime = System.currentTimeMillis();
						Date dateObj = new Date();
						monitor.subTask("Topic Modelling...");
						try {
							lda.doLDA(monitor, dateObj);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
							monitor.done();
							return Status.CANCEL_STATUS;
						} catch (IOException e) {
							e.printStackTrace();
							monitor.done();
							return Status.CANCEL_STATUS;
						}
						monitor.worked(20);
						if (monitor.isCanceled()) {
							return Status.CANCEL_STATUS;
						}
						
						
						//-----Generating the corpus file-------
						
						FileWriter fw = null;
						String corpusFile = topicModelDirPath + System.getProperty("file.separator") + "corpus";
						try {
							fw = new FileWriter(corpusFile);
							for (String file : inFiles){
								
								String text = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);
								fw.write(text.replace("\n\r", " ")+"\n");
							}
							fw.close();
						} catch (IOException e3) {
						}
						
						//------Generating the vocab file------
						
						String wordWeightsFile = topicModelDirPath + System.getProperty("file.separator") +"word-weights.txt";
						String vocabFile = topicModelDirPath + System.getProperty("file.separator") + "vocab";
						HashMap<String, Integer> vocab = new HashMap<String, Integer>();
						BufferedReader br = null;
						try {
							
							br = new BufferedReader(new FileReader(wordWeightsFile));
							fw = new FileWriter(vocabFile);
							String line = "";
							while ((line = br.readLine())!=null){
								vocab.put(line.split("\t")[1], 1);
							}
							br.close();

							String vocabKeys[] = vocab.keySet().toArray(new String[1]); 

							Arrays.sort(vocabKeys);

							for(String word : vocabKeys){
								fw.write(word + "\n");
							};
							fw.close();
						} catch (IOException e3) {
						}
						
						//--------Word Topic File----------
						String wordTopicFile = topicModelDirPath + System.getProperty("file.separator") + "word_topic";
						try {
							HashMap<String, Double> currAssign = null;
							HashMap<String, Integer> vocabMap = new HashMap<String, Integer>();
							int count = 0;
							HashMap<String, HashMap<String, Double>> wordTopic = new HashMap<String, HashMap<String, Double>>();
							br = new BufferedReader(new FileReader(wordWeightsFile));
							String line = "";
							while ((line = br.readLine())!=null){
								String components[] = line.split("\t");
								int topic = Integer.parseInt(components[0]);
								String word = components[1];
								double weight = Double.parseDouble(components[2]);
								currAssign = wordTopic.get(word)!=null?wordTopic.get(word): new HashMap<String, Double>();
								double currWeight = currAssign.get("weight")!=null?currAssign.get("weight"):0.0;
								if(currWeight<weight)
								{
									HashMap<String, Double> map = new HashMap<String, Double>();
									map.put("topic", topic+0.0);
									map.put("weight", weight);
									wordTopic.put(word, map);
								}
							}
							br.close();
							String topicKeys[] = wordTopic.keySet().toArray(new String[1]);
							Arrays.sort(topicKeys);
							for(String word:topicKeys){
								vocabMap.put(word, count);
								count = count + 1;
							}
							fw = new FileWriter(wordTopicFile);
							br = new BufferedReader(new FileReader(corpusFile));
							int i = 0;
							while ((line = br.readLine())!=null){
								String outputStr = i+"";
								for(String word:line.split(" ")){
									if(vocabMap.containsKey(word)&&wordTopic.containsKey(word))
										outputStr = outputStr+" "+vocabMap.get(word)+":"+(int)Double.parseDouble(wordTopic.get(word).get("topic")+"");
								};
								i+=1;
								fw.write(outputStr+"\n");
							}
							fw.close();
							br.close();
						} catch (IOException e3) {
							e3.printStackTrace();
						}
						
						LDAtopics lda = new LDAtopics(corpusFile, wordTopicFile, vocabFile, outputPath,noOfTopics,minCount,pValue,usePermBool);
						try {
							lda.generateTurboTopics();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						ppObj.clean();
						try {
							FileUtils.deleteDirectory(new File(
									topicModelDirPath));
						} catch (IOException e) {
							e.printStackTrace();
						}
						monitor.worked(10);
						monitor.done();

ConsoleView
						.printlInConsoleln("Turbo Topics Modelling completed successfully in "
								+ (System.currentTimeMillis() - startTime)
								+ " milliseconds.");
				
						return Status.OK_STATUS;
					}
				};
				
				job.setUser(true);
				if (canProceedCluster()) {
					job.schedule();
					job.addJobChangeListener(new JobChangeAdapter() {

						@Override
						public void done(IJobChangeEvent event) {
							if (!event.getResult().isOK()) {
								TacitFormComposite
										.writeConsoleHeaderBegining("Error: <Terminated> Turbo Topics Modelling");
								ConsoleView
										.printlInConsoleln("Turbo Topics Modelling not successful.");
							} else {
								TacitFormComposite.updateStatusMessage(
										getViewSite(),
										"Turbo Topics Modelling completed",
										IStatus.OK, form);

								TacitFormComposite
										.writeConsoleHeaderBegining("Success: <Completed> Turbo Topics Modelling ");

							}
						}
					});
				} else {
					TacitFormComposite
							.updateStatusMessage(
									getViewSite(),
									"Turbo Topics Modelling cannot be started. Please check the Form status to correct the errors",
									IStatus.ERROR, form);
				}

			}

		});
		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (TurboTopicsModelViewImageRegistry.getImageIconFactory()
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
						.displayHelp("edu.usc.cssl.tacit.topicmodel.turbotopics.ui.turbotopics");
			};
		};
		mgr.add(helpAction);
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(helpAction, "edu.usc.cssl.tacit.topicmodel.turbotopics.ui.turbotopics");
		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(form, "edu.usc.cssl.tacit.topicmodel.turbotopics.ui.turbotopics");
		form.getToolBarManager().update(true);
	}

	@Override
	public void setFocus() {
		form.setFocus();
	}

	private boolean canProceedCluster() {
		TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
		boolean canProceed = true;
		form.getMessageManager().removeMessage("location");
		form.getMessageManager().removeMessage("topic");
		form.getMessageManager().removeMessage("minCount");
		form.getMessageManager().removeMessage("pValue");
		form.getMessageManager().removeMessage("input");
		String message = OutputPathValidation.getInstance()
				.validateOutputDirectory(layoutData.getOutputLabel().getText(),
						"Output");
		if (message != null) {

			message = layoutData.getOutputLabel().getText() + " " + message;
			form.getMessageManager().addMessage("location", message, null,
					IMessageProvider.ERROR);
			canProceed = false;
		}
		// validate input
		try {
			if (inputLayoutData.getTypeCheckedSelectedFiles(checkType).size() < 1) {
				form.getMessageManager().addMessage("input",
						"Select/Add atleast one input file", null,
						IMessageProvider.ERROR);
				canProceed = false;
			}
		} catch (Exception e1) {
			canProceed = false;
		}

		if (numberOfTopics.getText().isEmpty()
				|| Integer.parseInt(numberOfTopics.getText()) < 1) {
			form.getMessageManager().addMessage("topic",
					"Number of topics cannot be empty or less than 1", null,
					IMessageProvider.ERROR);
			canProceed = false;
		}
		pValueTxt.getText();
		try{
			Integer.parseInt(minCountTxt.getText().trim());
		} catch(Exception e){
			form.getMessageManager().addMessage("minCount",
					"Min count value must be an integer. You may set to the default value of 25.", null,
					IMessageProvider.ERROR);
			minCountTxt.setText("");
			canProceed = false;
		}
		try{
			Double.parseDouble(pValueTxt.getText().trim());
		} catch(Exception e){
			form.getMessageManager().addMessage("pValue",
					"P-value must be decimal. You may set to the default value of 0.001.", null,
					IMessageProvider.ERROR);
			pValueTxt.setText("");
			canProceed = false;
		}
		usePerm.getSelection();
		
		return canProceed;
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == Job.class) {
			return job;
		}
		return super.getAdapter(adapter);
	}

}
