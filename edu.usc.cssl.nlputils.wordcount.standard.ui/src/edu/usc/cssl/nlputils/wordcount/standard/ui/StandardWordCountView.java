package edu.usc.cssl.nlputils.wordcount.standard.ui;

import java.io.File;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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

import edu.usc.cssl.nlputils.common.ui.CommonUiActivator;
import edu.usc.cssl.nlputils.common.ui.IPreprocessorSettingsConstant;
import edu.usc.cssl.nlputils.common.ui.composite.from.NlputilsFormComposite;
import edu.usc.cssl.nlputils.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.nlputils.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.nlputils.common.ui.validation.OutputPathValidation;
import edu.usc.cssl.nlputils.wordcount.standard.services.WordCountPlugin;
import edu.usc.cssl.nlputils.wordcount.standard.ui.internal.IStandardWordCountViewConstants;
import edu.usc.cssl.nlputils.wordcount.standard.ui.internal.StandardWordCountImageRegistry;

public class StandardWordCountView extends ViewPart implements
		IStandardWordCountViewConstants {
	public static String ID = "edu.usc.cssl.nlputils.wordcount.standard.ui.view1";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private OutputLayoutData layoutData;
	private TableLayoutData inputLayoutData;
	private TableLayoutData dictLayoutData;
	private Button stemEnabled;
	private Button stopWordPathEnabled;

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

		NlputilsFormComposite.createEmptyRow(toolkit, sc);

		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(2)
				.applyTo(client);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		inputLayoutData = NlputilsFormComposite.createTableSection(client,
				toolkit, layout, "Input",
				"Add file(s) or Folder(s) which contains data", true);
		dictLayoutData = NlputilsFormComposite.createTableSection(client,
				toolkit, layout, "Dictionary", "Add location of Dictionary",
				false);

		Composite client1 = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1)
				.applyTo(client1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client1);

		// we dont need stop word's as it will be taken from the preprocessor
		// settings

		createStemmingOptions(form.getBody());

		createStopWordPathLink(form.getBody());

		layoutData = NlputilsFormComposite.createOutputSection(toolkit,
				client1, form.getMessageManager());

		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter());
		addButtonsToToolBar();
		toolkit.paintBordersFor(form.getBody());

	}

	private void createStemmingOptions(Composite body) {

		stemEnabled = toolkit.createButton(body, "Stem Dictionary", SWT.CHECK);

		GridDataFactory.fillDefaults().grab(false, false).span(3, 1)
				.applyTo(stemEnabled);

	}

	private void createStopWordPathLink(Composite client) {

		Composite clientLink = toolkit.createComposite(client);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2)
				.applyTo(clientLink);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(clientLink);

		stopWordPathEnabled = toolkit.createButton(clientLink, "", SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(stopWordPathEnabled);
		final Hyperlink link = toolkit.createHyperlink(clientLink,
				"Stop Word Path", SWT.NONE);
		link.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		link.addHyperlinkListener(new IHyperlinkListener() {
			public void linkEntered(HyperlinkEvent e) {
			}

			public void linkExited(HyperlinkEvent e) {
			}

			public void linkActivated(HyperlinkEvent e) {
				String id = "edu.usc.cssl.nlputils.common.ui.prepocessorsettings";
				PreferencesUtil.createPreferenceDialogOn(link.getShell(), id,
						new String[] { id }, null).open();
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(link);

	}

	private FormToolkit createFormBodySection(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);

		toolkit.decorateFormHeading(form.getForm());
		form.setText("Standard Word Count"); //$NON-NLS-1$
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
				final String stopWordPath = CommonUiActivator.getDefault()
						.getPreferenceStore()
						.getString(IPreprocessorSettingsConstant.STOP_PATH);
				// lindapulickal: handling case where user types in a file
				// without extension
				final String outputPath = layoutData.getOutputLabel().getText();
				String fileName = "standard_wordcount";
				
				final File oFile = new File(outputPath + File.separator
						+ fileName + ".csv");
				final File sFile = new File(outputPath + File.separator
						+ fileName + ".dat");

				final List<String> inputFiles = inputLayoutData
						.getSelectedFiles();
				final List<String> dictionaryFiles = dictLayoutData
						.getSelectedFiles();
				final boolean isStemDic = stemEnabled.getSelection();
				final WordCountPlugin wc = new WordCountPlugin();
				
				// Creating a new Job to do Word Count so that the UI will not freeze
				Job wordCountJob = new Job("Word Count Plugin Job"){
					protected IStatus run(IProgressMonitor monitor){ 
				
				int rc = -1;
				
				try {
						//Niki Change here
					rc=wc.invokeWordCount(inputFiles, dictionaryFiles, stopWordPath, outputPath, isStemDic);
				} catch (Exception ioe) {
					ioe.printStackTrace();
					return Status.CANCEL_STATUS;
				}
				NlputilsFormComposite.updateStatusMessage(
						 getViewSite(),
						 "Standard Word Count is Sucessfully Completed",
						 IStatus.OK);
				return Status.OK_STATUS;
					}
				};
				wordCountJob.setUser(true);
				 if (canProceed()) {
				 wordCountJob.schedule();
				 }
			};
		});
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (StandardWordCountImageRegistry.getImageIconFactory()
						.getImageDescriptor(IMAGE_HELP_CO));
			}

			@Override
			public String getToolTipText() {
				return "Help";
			}

			public void run() {

			};
		});
		form.getToolBarManager().update(true);
	}

	private boolean canProceed() {
		NlputilsFormComposite.updateStatusMessage(getViewSite(), null,null);
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
