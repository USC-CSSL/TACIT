package edu.usc.cssl.tacit.wordcount.standard.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IMessageManager;
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
import edu.usc.cssl.tacit.wordcount.standard.services.WordCountPlugin;
import edu.usc.cssl.tacit.wordcount.standard.ui.internal.IStandardWordCountViewConstants;
import edu.usc.cssl.tacit.wordcount.standard.ui.internal.StandardWordCountImageRegistry;

public class StandardWordCountView extends ViewPart implements
		IStandardWordCountViewConstants {
	public static String ID = "edu.usc.cssl.tacit.wordcount.standard.ui.view1";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private OutputLayoutData layoutData;
	private TableLayoutData inputLayoutData;
	private TableLayoutData dictLayoutData;
	private Button preprocessButton;
	private Button standardWordCountButton;
	private Button weightedWordCountButton;
	private Button defaultTags;
	private Button wordDistribution;
	private Button splitInputFileLines;
	private Button createDATFile;
	private Button createPOSTags;
	protected Job wordCountJob;
	private List<Object> inputObjs;
	private boolean checkType = true;
	private String stemmedFilesLoc = System.getProperty("user.dir") + System.getProperty("file.separator")
									+ "tacit_temp_files" + System.getProperty("file.separator") + "stemmedFiles";
	@Override
	public Image getTitleImage() {
		return StandardWordCountImageRegistry.getImageIconFactory().getImage(
				IMAGE_WORD_TITLE);
	}

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

		TacitFormComposite.createEmptyRow(toolkit, sc);

		Composite wcTypeComposite = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2)
				.applyTo(wcTypeComposite);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1)
				.applyTo(wcTypeComposite);
		TacitFormComposite.addErrorPopup(form.getForm(), toolkit);
		createWordCountType(toolkit, wcTypeComposite, form.getMessageManager());

		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(2)
				.applyTo(client);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		inputLayoutData = TacitFormComposite.createTableSection(client,
				toolkit, layout, "Input Details",
				"Add File(s) and Folder(s) to include in analysis.", true,
				true, true, true);
		dictLayoutData = TacitFormComposite.createTableSection(client, toolkit,
				layout, "Dictionary", "Add location of Dictionary", false,
				true, false, false);

		Composite compInput = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(2)
				.applyTo(compInput);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(compInput);
		layout = new GridLayout();
		layout.numColumns = 2;

		createPreprocessLink(compInput);

		// createAdditionalOptions(toolkit, form.getBody());

		// TacitFormComposite.createEmptyRow(toolkit, form.getBody());

		Composite client1 = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1)
				.applyTo(client1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client1);

		// createCorpusSection(client1);
		// TacitFormComposite.createEmptyRow(toolkit, form.getBody());

		layoutData = TacitFormComposite.createOutputSection(toolkit, client1,
				form.getMessageManager());
		createAdditionalOptions(toolkit, client1);
		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter());
		addButtonsToToolBar();
		toolkit.paintBordersFor(form.getBody());
		form.setImage(StandardWordCountImageRegistry.getImageIconFactory().getImage(IStandardWordCountViewConstants.IMAGE_WORD_TITLE));

	}

	private void createAdditionalOptions(FormToolkit toolkit, Composite output) {
		defaultTags = toolkit.createButton(output,
				"Create output for default tags", SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(defaultTags);
		createPOSTags = toolkit.createButton(output,
				"Create Part of Speech tagged files", SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(createPOSTags);
		createDATFile = toolkit.createButton(output, "Create .DAT File",
				SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(createDATFile);
		wordDistribution = toolkit.createButton(output,
				"Create category-wise word distribution files", SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(wordDistribution);
		splitInputFileLines = toolkit.createButton(output,
				"Create a new file for each line of the input files", SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(splitInputFileLines);

	}

	private void createPreprocessLink(Composite client) {

		Composite clientLink = toolkit.createComposite(client);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2)
				.applyTo(clientLink);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(clientLink);

		preprocessButton = toolkit.createButton(clientLink, "", SWT.CHECK);
		preprocessButton.setEnabled(true);
		preprocessButton.setSelection(false);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(preprocessButton);
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
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(link);

	}

	private void createWordCountType(FormToolkit toolkit2, Composite parent,
			IMessageManager messageManager) {

		Group buttonComposite = new Group(parent, SWT.LEFT);
		buttonComposite.setText("Word Count Technique");
		buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonComposite.setLayout(layout);
		buttonComposite.setForeground(parent.getForeground());

		standardWordCountButton = new Button(buttonComposite, SWT.RADIO);
		standardWordCountButton.setText("Standard Word Count");
		standardWordCountButton.setSelection(true);
		standardWordCountButton.setForeground(parent.getForeground());

		weightedWordCountButton = new Button(buttonComposite, SWT.RADIO);
		weightedWordCountButton.setText("Weighted Word Count");
		weightedWordCountButton.setSelection(false);
		weightedWordCountButton.setForeground(parent.getForeground());

		Label lblEmpty = new Label(buttonComposite, SWT.None);
		// TacitFormComposite.createEmptyRow(toolkit, parent);

	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == Job.class) {
			return wordCountJob;
		}
		return super.getAdapter(adapter);
	}

	private FormToolkit createFormBodySection(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);

		toolkit.decorateFormHeading(form.getForm());
		form.setText("TACIT Word Count"); //$NON-NLS-1$
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true)
				.applyTo(form.getBody());
		return toolkit;
	}

	private void addButtonsToToolBar() {
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (StandardWordCountImageRegistry.getImageIconFactory()
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
				TacitFormComposite.writeConsoleHeaderBegining("Word Count ");
				final String outputPath = layoutData.getOutputLabel().getText();
				TacitUtil tacitHelper = new TacitUtil();
				
				try {
					inputObjs = inputLayoutData
							.getTypeCheckedSelectedFiles(checkType);
				} catch (Exception e1) {
					return;
				}
				tacitHelper.writeSummaryFile(outputPath);
				final List<String> dictionaryFiles = dictLayoutData
						.getSelectedFiles(false);
				final boolean doPennCounts = defaultTags.getSelection();
				final boolean doWordDistribution = wordDistribution
						.getSelection();
				final boolean ppValue = preprocessButton.getSelection();
				if(ppValue){
					checkType = false;
				}
				final boolean wcType = weightedWordCountButton.getSelection();
				final boolean datFile = createDATFile.getSelection();
				final boolean doPOSTags = createPOSTags.getSelection();
				final Date dateObj = new Date();
				final boolean splitFiles = splitInputFileLines.getSelection();
				// Creating a new Job to do Word Count so that the UI will not
				// freeze
				wordCountJob = new Job("Word Count Plugin Job") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("TACIT Word Count",
								(inputObjs.size() * 15) + 15);
						//added false for stem dict
						WordCountPlugin wc = new WordCountPlugin(wcType,
								dateObj, false, doPennCounts,
								doWordDistribution, datFile, doPOSTags,
								outputPath, monitor);

						TacitFormComposite.setConsoleViewInFocus();
						TacitFormComposite.updateStatusMessage(getViewSite(),
								"", null, form);

						Preprocessor ppObj;
						try {
							ppObj = new Preprocessor("TACIT_word_count",dictionaryFiles,ppValue);
							List<String> stemmedDictionaryFiles = ppObj.getStemmedDictionaryFiles(); //Will be stemmed only if selected in preprocessor settings else will return unchanged files 
							
							List<String> inputFiles = ppObj.processData(
									"wc_files", inputObjs, monitor);
							monitor.worked(5);
							if(splitFiles) {
								inputFiles = splitFiles("StandardWordCount", inputFiles);
							}
							wc.countWords(inputFiles, stemmedDictionaryFiles);
							if(splitFiles){
								File toDel = new File("StandardWordCount");
								FileUtils.deleteDirectory(toDel);
							}
							File stemmedFileDir = new File(stemmedFilesLoc);
							
							if (stemmedFileDir.exists()){
							//	FileUtils.deleteDirectory(stemmedFileDir);
							}
							
							ppObj.clean();
							monitor.worked(1);
						} catch (IOException e) {
							e.printStackTrace();
							return Status.CANCEL_STATUS;
						} catch (OperationCanceledException e){
							e.printStackTrace();
							return Status.CANCEL_STATUS;
						} catch (Exception e) {
							e.printStackTrace();
							return Status.CANCEL_STATUS;
						}
						monitor.done();
						return Status.OK_STATUS;
					}
				};
				wordCountJob.setUser(true);
				if (canProceed()) {
					wordCountJob.schedule();		
					wordCountJob.addJobChangeListener(new JobChangeAdapter() {

						@Override
						public void done(IJobChangeEvent event) {
							if (!event.getResult().isOK()) {
								TacitFormComposite
										.writeConsoleHeaderBegining("Error: <Terminated> Word count analysis");
								ConsoleView
										.printlInConsoleln("Word count analysis met with error.");
								ConsoleView
										.printlInConsoleln("Take appropriate action to resolve the issues and try again.");
							} else {
								TacitFormComposite.updateStatusMessage(
										getViewSite(),
										"Word count analysis completed",
										IStatus.OK, form);
								TacitFormComposite
										.writeConsoleHeaderBegining("Success: <Completed> Word count analysis");

							}
						}
					});

				}

			};
		});
		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (StandardWordCountImageRegistry.getImageIconFactory()
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
								"edu.usc.cssl.tacit.wordcount.standard.ui.standard");
			};
		};
		mgr.add(helpAction);
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(helpAction,
						"edu.usc.cssl.tacit.wordcount.standard.ui.standard");
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(form,
						"edu.usc.cssl.tacit.wordcount.standard.ui.standard");
		form.getToolBarManager().update(true);
	}

	private boolean canProceed() {
		TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
		form.getMessageManager().removeMessage("location");
		form.getMessageManager().removeMessage("input");
		form.getMessageManager().removeMessage("dict");
		String message = OutputPathValidation.getInstance()
				.validateOutputDirectory(layoutData.getOutputLabel().getText(),
						"Output");
		if (message != null) {
			message = layoutData.getOutputLabel().getText() + " " + message;
			form.getMessageManager().addMessage("location", message, null,
					IMessageProvider.ERROR);
			return false;
		}
		// check input
		try {
			if (inputLayoutData.getTypeCheckedSelectedFiles(checkType).size() < 1) {
				form.getMessageManager().addMessage("input",
						"Select/Add at least one input file", null,
						IMessageProvider.ERROR);
				return false;
			}
			
		} catch (Exception e) {
			return false;
		}
		try {
			if (dictLayoutData.getTypeCheckedSelectedFiles(checkType).size() < 1) {
				form.getMessageManager().addMessage("dict",
						"Select/Add at least one Dictionary file", null,
						IMessageProvider.ERROR);
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		

//		try{
//			List<Object> dictPaths = dictLayoutData.getTypeCheckedSelectedFiles(checkType);
//			Iterator<Object> dictPathIterator  = dictPaths.iterator();
//			while(dictPathIterator.hasNext()){
//				String dictPath  = (String)dictPathIterator.next();
//				if (!dictPath.endsWith(".txt")){
//					form.getMessageManager().addMessage("dict", "Error:The dictionary is not in .txt format", null, IMessageProvider.ERROR);
//					return false;
//				}
//			}
//		}catch(Exception e){
//			return false;
//		}
		
		return true;
	}

	@Override
	public String getPartName() {
		// TODO Auto-generated method stub
		return "TACIT Word Count";
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
