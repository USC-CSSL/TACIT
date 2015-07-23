package edu.usc.cssl.tacit.crawlers.supremecourt.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.uc.cssl.tacit.crawlers.supremecourt.services.SupremCrawlerFilter;
import edu.uc.cssl.tacit.crawlers.supremecourt.services.SupremeCourtCrawler;
import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.tacit.common.ui.validation.OutputPathValidation;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import edu.usc.cssl.tacit.crawlers.supremecourt.ui.internal.ISupremeCrawlerUIConstants;
import edu.usc.cssl.tacit.crawlers.supremecourt.ui.internal.SupremeCourtCaseListDialog;
import edu.usc.cssl.tacit.crawlers.supremecourt.ui.internal.SupremeCrawlerImageRegistry;

public class SupremeCrawlerView extends ViewPart implements
		ISupremeCrawlerUIConstants {
	public static final String ID = "edu.usc.cssl.tacit.crawlers.supremecourt.ui.view1";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private Button downloadAudio;
	private Button truncateAudio;
	private Button termBtn;
	private OutputLayoutData layoutData;
	private IToolBarManager mgr;
	protected Job job;
	private Table filterListTable;
	private List<String> selectedFilterList;
	private SupremeCourtCaseListDialog dialog;
	private String segment;
	private List<String> items;
	private List<String> issueList;
	private List<String> caseList;

	@Override
	public Image getTitleImage() {

		return SupremeCrawlerImageRegistry.getImageIconFactory().getImage(
				IMAGE_CRAWL_TITLE);
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(form.getForm());
		toolkit.getHyperlinkGroup().setHyperlinkUnderlineMode(
				HyperlinkSettings.UNDERLINE_HOVER);
		form.setText("Supreme Court Crawler"); //$NON-NLS-1$
		final IMessageManager mmng = form.getMessageManager();
		GridLayoutFactory.fillDefaults().applyTo(form.getBody());
		TacitFormComposite.addErrorPopup(form.getForm(), toolkit);
		Section section = toolkit.createSection(form.getBody(),
				Section.TITLE_BAR | Section.EXPANDED);

		GridDataFactory.fillDefaults().grab(true, false).span(3, 1)
				.applyTo(section);
		section.setExpanded(true);
		//	section.setText("Crawler Details"); //$NON-NLS-1$
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
		Composite selectionType = toolkit.createComposite(sectionClient);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(true)
				.applyTo(selectionType);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 0)
				.applyTo(selectionType);
		Label lblFilterType = toolkit.createLabel(selectionType,
				"Filter Type:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(lblFilterType);
		termBtn = toolkit.createButton(selectionType, "Term", SWT.RADIO);
		termBtn.setSelection(true);
		termBtn.setData("cases");
		segment = (String) termBtn.getData();
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(termBtn);
		final Button issuesBtn = toolkit.createButton(selectionType, "Issues",
				SWT.RADIO);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(issuesBtn);
		issuesBtn.setData("issues");
		Label filterRangeLbl = toolkit.createLabel(sectionClient,
				"Filter Range:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(filterRangeLbl);
		createMultiSelectRange(sectionClient);
		termBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (termBtn.getSelection()) {
					segment = (String) termBtn.getData();
				} else {
					segment = (String) issuesBtn.getData();
				}
				refreshFilterRangeTable();
			}
		});
		TacitFormComposite.createEmptyRow(toolkit, sectionClient);
		layoutData = TacitFormComposite.createOutputSection(toolkit,
				form.getBody(), form.getMessageManager());
		Composite outputSectionClient = layoutData.getSectionClient();
		createDownloadGroupSection(form.getBody());
		form.setImage(SupremeCrawlerImageRegistry.getImageIconFactory()
				.getImage(IMAGE_CRAWL));
		// form.setMessage("Invalid path", IMessageProvider.ERROR);
		this.setPartName("Supreme Crawler");
		mgr = form.getToolBarManager();
		addCrawlButton(mmng, layoutData.getOutputLabel(), mgr);
		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (SupremeCrawlerImageRegistry.getImageIconFactory()
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
								"edu.usc.cssl.tacit.crawlers.supremecourt.ui.supremecourt");
			};
		};
		mgr.add(helpAction);
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(helpAction,
						"edu.usc.cssl.tacit.crawlers.supremecourt.ui.supremecourt");
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(form,
						"edu.usc.cssl.tacit.crawlers.supremecourt.ui.supremecourt");
		form.getToolBarManager().update(true);
		toolkit.paintBordersFor(form.getBody());
	}

	private void createMultiSelectRange(Composite sectionClient) {

		filterListTable = toolkit.createTable(sectionClient, SWT.BORDER
				| SWT.MULTI);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 3)
				.hint(100, 200).applyTo(filterListTable);
		filterListTable.setBounds(100, 100, 100, 500);

		Composite buttonComp = new Composite(sectionClient, SWT.NONE);
		GridLayout btnLayout = new GridLayout();
		btnLayout.marginWidth = btnLayout.marginHeight = 0;
		btnLayout.makeColumnsEqualWidth = true;
		buttonComp.setLayout(btnLayout);
		buttonComp.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		final Button addfilterBtn = new Button(buttonComp, SWT.PUSH); //$NON-NLS-1$
		addfilterBtn.setText("Add...");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(addfilterBtn);

		addfilterBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				handleAdd(addfilterBtn.getShell(), segment);
			}
		});

		final Button removeFileButton = new Button(buttonComp, SWT.PUSH);
		removeFileButton.setText("Remove...");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(removeFileButton);

		removeFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				for (TableItem item : filterListTable.getSelection()) {

					selectedFilterList.remove(item.getText());
					if(termBtn.getSelection()){
						caseList.remove(item.getText());
					}
					else{
						issueList.remove(item.getText());
					}
					item.dispose();

				}

			}
		});
		selectedFilterList = new ArrayList<String>();
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

			@Override
			public void run() {
				List<String> selectedFilterValue = selectedFilterList;
				final SupremeCourtCrawler sc = new SupremeCourtCrawler(
						selectedFilterValue, outputPath.getText(),
						ISupremeCrawlerUIConstants.CRAWLER_URL);
				sc.setDownloadAudio(downloadAudio.getSelection());
				sc.setTruncate(truncateAudio.getSelection());
				TacitFormComposite
						.writeConsoleHeaderBegining("Crawling started  ");

				job = new Job("Crawling...") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						TacitFormComposite.setConsoleViewInFocus();
						TacitFormComposite.updateStatusMessage(getViewSite(),
								null, null, form);
						monitor.beginTask("TACIT started crawling...", 10000);

						if (monitor.isCanceled()) {
							TacitFormComposite
									.writeConsoleHeaderBegining("<terminated> Crawling  ");
							return Status.CANCEL_STATUS;
						}

						try {

							sc.looper(monitor);
						} catch (final IOException exception) {
							ConsoleView.printlInConsole(exception.toString());
							Display.getDefault().syncExec(new Runnable() {

								@Override
								public void run() {
									ErrorDialog
											.openError(
													Display.getDefault()
															.getActiveShell(),
													"Problem Occurred",
													"Please Check your connectivity to server",
													new Status(
															IStatus.ERROR,
															CommonUiActivator.PLUGIN_ID,
															exception
																	.getMessage()));

								}
							});
							TacitFormComposite
									.writeConsoleHeaderBegining("<terminated> Crawling  ");
							TacitFormComposite.updateStatusMessage(
									getViewSite(), "Crawling is stopped ",
									IStatus.INFO, form);
							return Status.CANCEL_STATUS;
						}
						TacitFormComposite
								.writeConsoleHeaderBegining("<terminated> Crawling  ");
						ConsoleView
								.printlInConsoleln("Crawling is sucessfully completed.");
						TacitFormComposite.updateStatusMessage(getViewSite(),
								"Crawling completed", IStatus.OK, form);
						return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				if (canProceedCrawl()) {

					job.schedule();

				}
			};
		});
	}

	protected boolean canProceedCrawl() {
		boolean canProceed = true;
		TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
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
		// downloadGroup.setBackground(outputSectionClient.getBackground());
		downloadGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		downloadGroup.setLayout(layout);
		downloadGroup.setForeground(outputSectionClient.getForeground());

		downloadAudio = new Button(downloadGroup, SWT.CHECK);
		downloadAudio.setText("Download Audio");
		// downloadAudio.setBackground(outputSectionClient.getBackground());

		truncateAudio = new Button(downloadGroup, SWT.CHECK);
		truncateAudio.setText("Truncate (1 MB) ");
		truncateAudio.setEnabled(false);
		// truncateAudio.setBackground(outputSectionClient.getBackground());

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
		TacitFormComposite.createEmptyRow(toolkit, outputSectionClient);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		if (form != null) {
			form.setFocus();
		}
	}

	private void handleAdd(Shell shell, final String segment) {

		processElementSelectionDialog(shell, segment);

		Job listAuthors = new Job("Load Filter values...") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				SupremCrawlerFilter sFilter = new SupremCrawlerFilter(
						ISupremeCrawlerUIConstants.CRAWLER_URL);
				try {
					items = sFilter.filters(segment);
					if (selectedFilterList != null)
						items.removeAll(selectedFilterList);

				} catch (IOException exception) {
					items = new ArrayList<String>();
					items.add("Not able to retrieve list !!");
					ConsoleView.printlInConsole(exception.toString());
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							ErrorDialog.openError(Display.getDefault()
									.getActiveShell(), "Problem Occurred",
									"Please Check your connectivity to server",
									new Status(IStatus.ERROR,
											CommonUiActivator.PLUGIN_ID,
											"Network is not reachable"));

						}
					});
				}

				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						dialog.refresh(items.toArray());

					}
				});
				return Status.OK_STATUS;
			}

		};

		listAuthors.schedule();
		dialog.setElements(new String[]{"Loading..."});
		dialog.setMultipleSelection(true);

		if (dialog.open() == Window.OK) {
			updateFilterRangeTable(dialog.getResult());
		}

	}

	private void refreshFilterRangeTable() {
		if (caseList == null) { // term
			caseList = new ArrayList<String>();
		}
		if (issueList == null) {
			issueList = new ArrayList<String>();
		}
		selectedFilterList.clear();
		if (termBtn.getSelection()) {
			selectedFilterList.addAll(caseList);
		} else {
			selectedFilterList.addAll(issueList);
		}
		Collections.sort(selectedFilterList);
		filterListTable.removeAll();
		for (String itemName : selectedFilterList) {
			TableItem item = new TableItem(filterListTable, 0);
			item.setText(itemName);
		}

	}

	private void updateFilterRangeTable(Object[] result) {
		if (caseList == null) { // term
			caseList = new ArrayList<String>();
		}
		if (issueList == null) {
			issueList = new ArrayList<String>();
		}
		selectedFilterList.clear();
		if (termBtn.getSelection()) {
			selectedFilterList.addAll(caseList);
		} else {
			selectedFilterList.addAll(issueList);
		}

		for (Object object : result) {
			selectedFilterList.add((String) object);
			if (termBtn.getSelection()) {
				caseList.add((String) object);
			} else {
				issueList.add((String) object);
			}
		}
		Collections.sort(selectedFilterList, Collections.reverseOrder());
		filterListTable.removeAll();
		for (String itemName : selectedFilterList) {
			TableItem item = new TableItem(filterListTable, 0);
			item.setText(itemName);
		}

	}

	static class ArrayLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return (String) element;
		}
	}

	public void processElementSelectionDialog(Shell shell, String title) {

		ILabelProvider lp = new ArrayLabelProvider();
		dialog = new SupremeCourtCaseListDialog(shell, lp);
		dialog.setTitle("Select the " + title + " from the list");
		dialog.setMessage("Enter " + title + " to search");

	}

}
