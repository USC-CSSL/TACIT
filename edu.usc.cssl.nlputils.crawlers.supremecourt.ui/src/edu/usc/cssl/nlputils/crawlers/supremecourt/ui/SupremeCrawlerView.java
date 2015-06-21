package edu.usc.cssl.nlputils.crawlers.supremecourt.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.uc.cssl.nlputils.crawlers.supremecourt.services.SupremCrawlerFilter;
import edu.uc.cssl.nlputils.crawlers.supremecourt.services.SupremeCourtCrawler;
import edu.usc.cssl.nlputils.common.ui.CommonUiActivator;
import edu.usc.cssl.nlputils.common.ui.composite.from.NlputilsFormComposite;
import edu.usc.cssl.nlputils.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.nlputils.common.ui.validation.OutputPathValidation;
import edu.usc.cssl.nlputils.common.ui.views.ConsoleView;
import edu.usc.cssl.nlputils.crawlers.supremecourt.ui.internal.ISupremeCrawlerUIConstants;
import edu.usc.cssl.nlputils.crawlers.supremecourt.ui.internal.SupremeCrawlerImageRegistry;

public class SupremeCrawlerView extends ViewPart implements
		ISupremeCrawlerUIConstants {
	public static final String ID = "edu.usc.cssl.nlputils.crawlers.supremecourt.ui.view1";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private Button downloadAudio;
	private Button truncateAudio;
	private Button termBtn;
	private Combo rangeCombo;
	private OutputLayoutData layoutData;
	private IToolBarManager mgr;
	protected Job job;

	@Override
	public Image getTitleImage() {

		return SupremeCrawlerImageRegistry.getImageIconFactory().getImage(
				IMAGE_CRAWL_TITLE);
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(form.getForm());
		toolkit.getHyperlinkGroup().setHyperlinkUnderlineMode(
				HyperlinkSettings.UNDERLINE_HOVER);
		form.setText("Supreme Court Crawler"); //$NON-NLS-1$
		final IMessageManager mmng = form.getMessageManager();
		GridLayoutFactory.fillDefaults().applyTo(form.getBody());
		NlputilsFormComposite.addErrorPopup(form.getForm(), toolkit);
		Section section = toolkit.createSection(form.getBody(),
				Section.TITLE_BAR | Section.EXPANDED);

		GridDataFactory.fillDefaults().grab(true, false).span(3, 1)
				.applyTo(section);
		section.setExpanded(true);
		section.setText("Crawler Details"); //$NON-NLS-1$
		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL
				| SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
				.applyTo(sc);

		Composite sectionClient = toolkit.createComposite(section);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
				.applyTo(sectionClient);
		section.setClient(sectionClient);

		Label dummy = toolkit.createLabel(sectionClient, "", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(dummy);
		Label lblFilterType = toolkit.createLabel(sectionClient,
				"Filter Type:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(lblFilterType);

		termBtn = toolkit.createButton(sectionClient, "Term", SWT.RADIO);
		termBtn.setSelection(true);
		termBtn.setData("cases");

		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(termBtn);
		final Button issuesBtn = toolkit.createButton(sectionClient, "Issues",
				SWT.RADIO);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
				.applyTo(issuesBtn);
		issuesBtn.setData("issues");
		Label filterRangeLbl = toolkit.createLabel(sectionClient,
				"Filter Range:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(filterRangeLbl);
		rangeCombo = new Combo(sectionClient, SWT.FLAT);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0)
				.applyTo(rangeCombo);
		toolkit.adapt(rangeCombo);

		fireFilterEvent((String) termBtn.getData(), rangeCombo);

		termBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (termBtn.getSelection())
					fireFilterEvent((String) termBtn.getData(), rangeCombo);
				else
					fireFilterEvent((String) issuesBtn.getData(), rangeCombo);
				rangeCombo.select(0);
			}
		});
		NlputilsFormComposite.createEmptyRow(toolkit, sectionClient);
		layoutData = NlputilsFormComposite.createOutputSection(toolkit,
				form.getBody(), form.getMessageManager());
		Composite outputSectionClient = layoutData.getSectionClient();
		createDownloadGroupSection(form.getBody());
		form.setImage(SupremeCrawlerImageRegistry.getImageIconFactory()
				.getImage(IMAGE_CRAWL));
		// form.setMessage("Invalid path", IMessageProvider.ERROR);
		this.setPartName("Supreme Crawler");
		mgr = form.getToolBarManager();
		addCrawlButton(mmng, layoutData.getOutputLabel(), mgr);
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (SupremeCrawlerImageRegistry.getImageIconFactory()
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
		toolkit.paintBordersFor(form.getBody());
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == Job.class) {
			return job;
		}
		return super.getAdapter(adapter);
	}

	private void addCrawlButton(final IMessageManager mmng,
			final Text outputPath, IToolBarManager mgr) {
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (SupremeCrawlerImageRegistry.getImageIconFactory()
						.getImageDescriptor(IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Crawl";
			}

			public void run() {
				String selectedFilterValue = rangeCombo.getText();
				if (selectedFilterValue.equals("All")) {
					if (termBtn.getSelection())
						selectedFilterValue = "/cases";
					else
						selectedFilterValue = "/issues";
				}
				final SupremeCourtCrawler sc = new SupremeCourtCrawler(
						selectedFilterValue, outputPath.getText(),
						ISupremeCrawlerUIConstants.CRAWLER_URL);
				sc.setDownloadAudio(downloadAudio.getSelection());
				sc.setTruncate(truncateAudio.getSelection());

				job = new Job("Crawling...") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						NlputilsFormComposite.setConsoleViewInFocus();
						monitor.beginTask("NLPUtils started crawling...", 100);

						if (monitor.isCanceled()) {
							return Status.CANCEL_STATUS;
						}

						try {

							sc.looper(new SubProgressMonitor(monitor, 100));
						} catch (final IOException exception) {
							ConsoleView.printlInConsole(exception.toString());
							Display.getDefault().syncExec(new Runnable() {

								@Override
								public void run() {
									ErrorDialog
											.openError(
													Display.getDefault().getActiveShell(),
													"Problem Occurred",
													"Please Check your connectivity to server",
													new Status(IStatus.ERROR,
															CommonUiActivator.PLUGIN_ID,
															exception.getMessage()));

								}
							});
							NlputilsFormComposite.updateStatusMessage(
									getViewSite(), "Crawling is stopped ",
									IStatus.INFO);
							return Status.CANCEL_STATUS;
						}
						ConsoleView.printlInConsoleln("Crawling is sucessfully completed");
						NlputilsFormComposite.updateStatusMessage(getViewSite(), "Crawling completed", IStatus.OK);
						return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				if (canProceedCrawl()) {

					job.schedule();

				} else {

				}
			};
		});
	}

	protected boolean canProceedCrawl() {
		boolean canProceed = true;
		NlputilsFormComposite.updateStatusMessage(getViewSite(), null, null);
		form.getMessageManager().removeMessage("location");
		String message = OutputPathValidation.getInstance()
				.validateOutputDirectory(layoutData.getOutputLabel().getText(),
						"Output");
		if (message != null) {

			message = layoutData.getOutputLabel().getText() + " " + message;
			form.getMessageManager().addMessage("location", message, null,
					IMessageProvider.ERROR);
			canProceed = false;
		}
		return canProceed;
	}

	private void createDownloadGroupSection(Composite outputSectionClient) {

		Group downloadGroup = new Group(outputSectionClient, SWT.LEFT);
		downloadGroup.setText("Audio");
		downloadGroup.setBackground(outputSectionClient.getBackground());
		downloadGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		downloadGroup.setLayout(layout);
		downloadGroup.setForeground(outputSectionClient.getForeground());

		downloadAudio = new Button(downloadGroup, SWT.CHECK);
		downloadAudio.setText("Download Audio");
		downloadAudio.setBackground(outputSectionClient.getBackground());

		truncateAudio = new Button(downloadGroup, SWT.CHECK);
		truncateAudio.setText("Truncate (1 MB) ");
		truncateAudio.setEnabled(false);
		truncateAudio.setBackground(outputSectionClient.getBackground());

		downloadAudio.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (downloadAudio.getSelection()) {
					truncateAudio.setEnabled(true);
				} else {
					truncateAudio.setEnabled(false);
				}
			}
		});

		Label lblEmpty = new Label(downloadGroup, SWT.None);
		NlputilsFormComposite.createEmptyRow(toolkit, outputSectionClient);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if (form != null) {
			form.setFocus();
		}
	}

	private void fireFilterEvent(final String segment, final Combo combo) {
		Job loadFilters = new Job("Load Filter values") {

			private String[] comboLists;

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				SupremCrawlerFilter sFilter = new SupremCrawlerFilter(
						ISupremeCrawlerUIConstants.CRAWLER_URL);
				List<String> items;
				try {
					items = sFilter.filters(segment);
				} catch (IOException e) {
					items = new ArrayList<String>();
					items.add("Not able to retrieve list !!");
				}
				comboLists = new String[items.size()];
				comboLists = items.toArray(comboLists);
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						combo.setItems(comboLists);
						combo.select(0);

					}
				});
				return Status.OK_STATUS;

			}
		};
		loadFilters.schedule();
	}

	/*
	 
	 */

}