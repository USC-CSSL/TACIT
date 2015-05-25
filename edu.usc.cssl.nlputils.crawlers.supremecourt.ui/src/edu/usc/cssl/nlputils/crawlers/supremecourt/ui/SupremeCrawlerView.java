package edu.usc.cssl.nlputils.crawlers.supremecourt.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.uc.cssl.nlputils.crawlers.supremecourt.services.SupremCrawlerFilter;
import edu.uc.cssl.nlputils.crawlers.supremecourt.services.SupremeCourtCrawler;
import edu.usc.cssl.nlputils.common.ui.composite.from.NlputilsFormComposite;
import edu.usc.cssl.nlputils.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.nlputils.common.ui.validation.OutputPathValidation;
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
		addErrorPopup(form.getForm());
		Section section = toolkit.createSection(form.getBody(),
				Section.TITLE_BAR | Section.EXPANDED );

		GridDataFactory.fillDefaults().grab(true, false).span(3, 1)
				.applyTo(section);
		section.setExpanded(true);
		section.setText("Crawler Details"); //$NON-NLS-1$
		String description = "This sections gives details about the crawler settigs";
		FormText descriptionFrm = toolkit.createFormText(section, false);
		descriptionFrm.setText("<form><p>" + description + "</p></form>", true,
				false);
		section.setDescriptionControl(descriptionFrm);
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

		Label dummyLabel = toolkit.createLabel(sectionClient, "", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(dummyLabel);
		layoutData = NlputilsFormComposite
				.createOutputSection(toolkit, form.getBody(),
						form.getMessageManager());
		Composite outputSectionClient = layoutData.getSectionClient();
		createDownloadGroupSection(outputSectionClient);
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

	private void addCrawlButton(final IMessageManager mmng, final Text outputPath,
			IToolBarManager mgr) {
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

				Job job = new Job("Crawling...") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("NLPUtils started crawling...", 100);

						if (monitor.isCanceled()) {
							return Status.CANCEL_STATUS;
						}

						try {

							sc.looper(new SubProgressMonitor(monitor, 100));
						} catch (IOException e1) {
							return Status.CANCEL_STATUS;
						}
                        System.out.println("Done!!");
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
		String message = OutputPathValidation.getInstance().validateOutputDirectory(layoutData.getOutputLabel().getText());
		if (message != null) {

			message = layoutData.getOutputLabel().getText() + " " + message;
			form.getMessageManager().addMessage("location", message, null,
					IMessageProvider.ERROR);
			return false;
		} else {
			form.getMessageManager().removeMessage("location");
			return true;
		}
	}

	private void createDownloadGroupSection(Composite outputSectionClient) {
		Group downloadGroup = new Group(outputSectionClient, SWT.SHADOW_IN);
		downloadGroup.setText("Audio");
		GridDataFactory.fillDefaults().grab(true, true).span(3, 4)
				.applyTo(downloadGroup);
		toolkit.adapt(downloadGroup);

		downloadAudio = new Button(downloadGroup, SWT.CHECK);
		downloadAudio.setText("Download Audio");
		downloadAudio.setBounds(10, 10, 10, 10);
		downloadAudio.pack();

		truncateAudio = new Button(downloadGroup, SWT.CHECK);
		truncateAudio.setText("Truncate (1 MB) ");
		truncateAudio.setBounds(10, 35, 10, 10);
		truncateAudio.pack();
		truncateAudio.setEnabled(false);

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

		Label dummyDownload = toolkit.createLabel(downloadGroup, "", SWT.NONE);
		dummyDownload.setBounds(10, 45, 10, 10);
		dummyDownload.pack();
	}

	private void addErrorPopup(Form form2) {
		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				String title = e.getLabel();
				Object href = e.getHref();
				if (href instanceof IMessage[]) {
					// details =
					// managedForm.getMessageManager().createSummary((IMessage[])href);
				}

				Point hl = ((Control) e.widget).toDisplay(0, 0);
				hl.x += 10;
				hl.y += 10;
				Shell shell = new Shell(form.getShell(), SWT.ON_TOP | SWT.TOOL);
				shell.setImage(getImage(form.getMessageType()));
				shell.setText(title);
				shell.setLayout(new FillLayout());
				// ScrolledFormText stext = new ScrolledFormText(shell, false);
				// stext.setBackground(toolkit.getColors().getBackground());
				FormText text = toolkit.createFormText(shell, true);
				configureFormText(form.getForm(), text);
				// stext.setFormText(text);
				if (href instanceof IMessage[])
					text.setText(createFormTextContent((IMessage[]) href),
							true, false);
				shell.setLocation(hl);
				shell.pack();
				shell.open();
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if (form != null) {
			form.setFocus();
		}
	}

	private Image getImage(int type) {
		switch (type) {
		case IMessageProvider.ERROR:
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
		case IMessageProvider.WARNING:
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
		case IMessageProvider.INFORMATION:
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
		}
		return null;
	}

	private void configureFormText(final Form form, FormText text) {
		text.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				String is = (String) e.getHref();
				try {
					int index = Integer.parseInt(is);
					IMessage[] messages = form.getChildrenMessages();
					IMessage message = messages[index];
					Control c = message.getControl();
					((FormText) e.widget).getShell().dispose();
					if (c != null)
						c.setFocus();
				} catch (NumberFormatException ex) {
				}
			}
		});
		text.setImage("error", getImage(IMessageProvider.ERROR));
		text.setImage("warning", getImage(IMessageProvider.WARNING));
		text.setImage("info", getImage(IMessageProvider.INFORMATION));
	}

	private String createFormTextContent(IMessage[] messages) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("<form>");
		for (int i = 0; i < messages.length; i++) {
			IMessage message = messages[i];
			pw.print("<li vspace=\"false\" style=\"image\" indent=\"16\" value=\"");
			switch (message.getMessageType()) {
			case IMessageProvider.ERROR:
				pw.print("error");
				break;
			case IMessageProvider.WARNING:
				pw.print("warning");
				break;
			case IMessageProvider.INFORMATION:
				pw.print("info");
				break;
			}
			pw.print("\"> <a href=\"");
			pw.print(i + "");
			pw.print("\">");
			if (message.getPrefix() != null)
				pw.print(message.getPrefix());
			pw.print(message.getMessage());
			pw.println("</a>");
			pw.println("</li>");
		}
		pw.println("</form>");
		pw.flush();
		return sw.toString();
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