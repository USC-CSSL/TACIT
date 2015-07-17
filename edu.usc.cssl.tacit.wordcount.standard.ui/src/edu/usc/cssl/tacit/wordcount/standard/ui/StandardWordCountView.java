package edu.usc.cssl.tacit.wordcount.standard.ui;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.swt.events.SelectionListener;
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

import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.common.ui.IPreprocessorSettingsConstant;
import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.tacit.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.tacit.common.ui.validation.OutputPathValidation;
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
	private Button stemEnabled;
	private Button stopWordPathEnabled;
	private Button standardWordCountButton;
	private Button weightedWordCountButton;
	private Button defaultTags;
	protected Job wordCountJob;

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
		inputLayoutData = TacitFormComposite
				.createTableSection(client, toolkit, layout, "Input",
						"Add File(s) and Folder(s) to include in analysis.",
						true, true);
		dictLayoutData = TacitFormComposite
				.createTableSection(client, toolkit, layout, "Dictionary",
						"Add location of Dictionary", false, true);

		Composite compInput = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(2)
				.applyTo(compInput);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(compInput);
		layout = new GridLayout();
		layout.numColumns = 2;

		createPreprocessLink(compInput);

		createStemmingOptions(compInput);

		createAdditionalOptions(toolkit, form.getBody());

		TacitFormComposite.createEmptyRow(toolkit, form.getBody());

		Composite client1 = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1)
				.applyTo(client1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client1);

		layoutData = TacitFormComposite.createOutputSection(toolkit, client1,
				form.getMessageManager());

		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter());
		addButtonsToToolBar();
		toolkit.paintBordersFor(form.getBody());

	}

	private void createAdditionalOptions(FormToolkit toolkit, Composite output) {
		defaultTags = toolkit.createButton(output,
				"Create output for default tags", SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(defaultTags);
	}

	private void createPreprocessLink(Composite client) {

		Composite clientLink = toolkit.createComposite(client);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2)
				.applyTo(clientLink);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(clientLink);

		stopWordPathEnabled = toolkit.createButton(clientLink, "", SWT.CHECK);
		stopWordPathEnabled.setEnabled(false);
		stopWordPathEnabled.setSelection(false);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(stopWordPathEnabled);
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
		TacitFormComposite.createEmptyRow(toolkit, parent);

	}

	private void createStemmingOptions(Composite body) {
		Composite downloadGroup = toolkit.createComposite(body, SWT.NONE);
		downloadGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		downloadGroup.setLayout(layout);

		stemEnabled = toolkit.createButton(downloadGroup, "Stem Dictionary",
				SWT.CHECK);
		stemEnabled.pack();
		stemEnabled.setEnabled(true);

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

			public void run() {
				TacitFormComposite.writeConsoleHeaderBegining("Word Count ");
				final String outputPath = layoutData.getOutputLabel().getText();
				final List<String> inputFiles = inputLayoutData
						.getSelectedFiles();
				final List<String> dictionaryFiles = dictLayoutData
						.getSelectedFiles();
				final boolean isStemDic = stemEnabled.getSelection();
				final boolean doPennCounts = defaultTags.getSelection();
				Date dateObj = new Date();
				final WordCountPlugin wc = new WordCountPlugin(false, dateObj,
						isStemDic, doPennCounts);

				// Creating a new Job to do Word Count so that the UI will not
				// freeze
				wordCountJob = new Job("Word Count Plugin Job") {
					protected IStatus run(IProgressMonitor monitor) {
						TacitFormComposite.setConsoleViewInFocus();
						TacitFormComposite.updateStatusMessage(getViewSite(),
								"", null, form);

						try {
							wc.countWords(inputFiles, dictionaryFiles,
									outputPath);
						} catch (Exception ioe) {
							ioe.printStackTrace();
							return Status.CANCEL_STATUS;
						}
						TacitFormComposite.updateStatusMessage(getViewSite(),
								"Word count analysis completed", IStatus.OK,
								form);
						TacitFormComposite
								.writeConsoleHeaderBegining("<terminated> Word count analysis");
						return Status.OK_STATUS;
					}
				};
				wordCountJob.setUser(true);
				if (canProceed()) {
					wordCountJob.schedule();
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
		boolean canPerform = true;
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
			canPerform = false;
		}
		// check input
		if (inputLayoutData.getSelectedFiles().size() < 1) {
			form.getMessageManager().addMessage("input",
					"Select/Add atleast one input file", null,
					IMessageProvider.ERROR);
			canPerform = false;
		}
		if (dictLayoutData.getSelectedFiles().size() < 1) {
			form.getMessageManager().addMessage("dict",
					"Select/Add atleast one Dictionary file", null,
					IMessageProvider.ERROR);
			canPerform = false;
		}
		return canPerform;
	}

	@Override
	public String getPartName() {
		// TODO Auto-generated method stub
		return "Standard Word Count";
	}

	@Override
	public void setFocus() {
		form.setFocus();
	}
}
