package edu.usc.cssl.tacit.common.ui.composite.from;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import edu.usc.cssl.tacit.common.ui.ICommonUiConstants;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;
import edu.usc.cssl.tacit.common.ui.internal.CommonUiViewImageRegistry;
import edu.usc.cssl.tacit.common.ui.internal.TargetLocationsGroup;
import edu.usc.cssl.tacit.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.tacit.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.tacit.common.ui.validation.OutputPathValidation;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class TacitFormComposite {

	private static TargetLocationsGroup targetLocationContent;
	
	private static String outputFilterPath = "";

	public static void createEmptyRow(FormToolkit toolkit,
			Composite sectionClient) {
		Label dummy = new Label(sectionClient, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(dummy);
	}

	public static Text createCorpusSection(FormToolkit toolkit,
			Composite parent, final IMessageManager mmng) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR
				| Section.EXPANDED | Section.DESCRIPTION);

		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(section);
		section.setText("Output Details"); //$NON-NLS-1$
		section.setDescription("Provide output details for storing the results");

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL
				| SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false)
				.applyTo(sc);

		Composite sectionClient = toolkit.createComposite(section);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
				.applyTo(sectionClient);
		section.setClient(sectionClient);

		createEmptyRow(toolkit, sectionClient);

		final Label corpusNameLbl = toolkit.createLabel(sectionClient,
				"Corpus Name:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(corpusNameLbl);
		final Text corpusNameTxt = toolkit.createText(sectionClient, "",
				SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
				.applyTo(corpusNameTxt);
		corpusNameTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				corpusPathListener(corpusNameTxt, mmng);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				corpusPathListener(corpusNameTxt, mmng);
			}
		});

		return corpusNameTxt;
	}

	protected static void corpusPathListener(Text corpusNameTxt,
			IMessageManager mmng) {
		mmng.removeMessage("output");
		if (corpusNameTxt.getText().isEmpty()) {
			mmng.addMessage("output", "Corpus name must not be empty", null,
					IMessageProvider.ERROR);
			return;
		} else if (corpusNameExists(corpusNameTxt.getText())) {
			mmng.addMessage("output", "Corpus name already exist", null,
					IMessageProvider.ERROR);
			return;
		}

	}

	public static boolean corpusNameExists(String corpusName) {
		List<ICorpus> corpuses = new ManageCorpora().getAllCorpusDetails();
		for (ICorpus corpus : corpuses) {
			if (corpus.getCorpusName().equals(corpusName))
				return true;
		}
		return false;
	}

	public static OutputLayoutData createOutputSection(FormToolkit toolkit,
			Composite parent, final IMessageManager mmng) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR
				| Section.EXPANDED | Section.DESCRIPTION);

		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(section);
		section.setText("Output Details"); //$NON-NLS-1$
		section.setDescription("Choose output details for storing the results");

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

		createEmptyRow(toolkit, sectionClient);

		final Label outputPathLbl = toolkit.createLabel(sectionClient,
				"Output Location:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(outputPathLbl);
		final Text outputLocationTxt = toolkit.createText(sectionClient, "",
				SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
				.applyTo(outputLocationTxt);

		final Button browseBtn = toolkit.createButton(sectionClient,
				"Browse...", SWT.PUSH);
		browseBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(browseBtn.getShell(),
						SWT.OPEN);
				if (!outputFilterPath.isEmpty()){
					dlg.setFilterPath(outputFilterPath);
				}
				dlg.setText("Open");
				String path = dlg.open();
				if (path == null)
					return;
				outputLocationTxt.setText(path);
				outputFilterPath = path.toString();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		/*
		 * outputLocationTxt.addFocusListener(new FocusAdapter() {
		 * 
		 * @Override public void focusLost(FocusEvent e) { super.focusLost(e);
		 * String message = OutputPathValidation.getInstance()
		 * .validateOutputDirectory(outputLocationTxt.getText(),"Output"); if
		 * (message != null) {
		 * 
		 * message = outputPathLbl.getText() + " " + message;
		 * mmng.addMessage("location", message, null, IMessageProvider.ERROR); }
		 * else { mmng.removeMessage("location"); } } });
		 */

		outputLocationTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				outputPathListener(outputLocationTxt, mmng, outputPathLbl);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				outputPathListener(outputLocationTxt, mmng, outputPathLbl);
			}
		});

		OutputLayoutData layoutData = new OutputLayoutData();
		layoutData.setOutputLabel(outputLocationTxt);
		layoutData.setSectionClient(sectionClient);

		return layoutData;
	}

	protected static void outputPathListener(Text outputLocationTxt,
			IMessageManager mmng, Label outputPathLbl) {
		if (outputLocationTxt.getText().isEmpty()) {
			mmng.addMessage("output",
					"Output path must be a valid diretory location", null,
					IMessageProvider.ERROR);
			return;
		}
		File tempFile = new File(outputLocationTxt.getText());
		if (!tempFile.exists() || !tempFile.isDirectory()) {
			mmng.addMessage("output",
					"Output path must be a valid diretory location", null,
					IMessageProvider.ERROR);
		} else {
			String message = OutputPathValidation.getInstance()
					.validateOutputDirectory(outputLocationTxt.getText(),
							"Output");
			if (message != null) {
				message = outputPathLbl.getText() + " " + message;
				mmng.addMessage("output", message, null, IMessageProvider.ERROR);
			} else {
				mmng.removeMessage("output");
			}
		}
	}

	public static OutputLayoutData createInputSection(FormToolkit toolkit,
			Composite parent, final IMessageManager mmng) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR
				| Section.EXPANDED | Section.DESCRIPTION);

		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(section);
		section.setText("Input Details "); //$NON-NLS-1$
		section.setDescription("Choose input folder for analysis.");

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

		createEmptyRow(toolkit, sectionClient);

		final Label outputPathLbl = toolkit.createLabel(sectionClient,
				"Input Location:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(outputPathLbl);
		final Text outputLocationTxt = toolkit.createText(sectionClient, "",
				SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
				.applyTo(outputLocationTxt);

		final Button browseBtn = toolkit.createButton(sectionClient,
				"Browse...", SWT.PUSH);
		browseBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(browseBtn.getShell(),
						SWT.OPEN);
				dlg.setText("Open");
				String path = dlg.open();
				if (path == null)
					return;
				outputLocationTxt.setText(path);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		outputLocationTxt.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				super.focusLost(e);
				String message = OutputPathValidation.getInstance()
						.validateOutputDirectory(outputLocationTxt.getText(),
								"Input");
				if (message != null) {

					message = outputPathLbl.getText() + " " + message;
					mmng.addMessage("inputlocation", message, null,
							IMessageProvider.ERROR);
				} else {
					mmng.removeMessage("inputlocation");
				}
			}
		});

		OutputLayoutData layoutData = new OutputLayoutData();
		layoutData.setOutputLabel(outputLocationTxt);
		layoutData.setSectionClient(sectionClient);

		return layoutData;
	}

	public static TableLayoutData createTableSection(final Composite parent,
			FormToolkit toolkit, GridLayout layout, String title,
			String description, boolean isFolder, boolean isFile,
			boolean isCorpus,boolean isClass) {
		Section section = toolkit.createSection(parent, Section.TWISTIE
				| Section.TITLE_BAR);
		section.setActiveToggleColor(toolkit.getHyperlinkGroup()
				.getActiveForeground());
		section.setToggleColor(toolkit.getColors().getColor(
				IFormColors.SEPARATOR));

		FormText descriptionFrm = toolkit.createFormText(section, false);
		descriptionFrm.setText("<form><p>" + description + "</p></form>", true,
				false);
		section.setDescriptionControl(descriptionFrm);

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		client.setLayout(layout);

		TacitFormComposite.createEmptyRow(toolkit, client);

		ScrolledComposite pluginTabContainer = new ScrolledComposite(client,
				SWT.H_SCROLL | SWT.V_SCROLL);
		pluginTabContainer.setExpandHorizontal(true);
		pluginTabContainer.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false)
				.applyTo(pluginTabContainer);

		Composite scInput = toolkit.createComposite(pluginTabContainer);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false)
				.applyTo(scInput);

		pluginTabContainer.setContent(scInput);

		GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 200)
				.span(1, 1).applyTo(pluginTabContainer);
		targetLocationContent = TargetLocationsGroup.createInForm(scInput,
				toolkit, isFolder, isFile, isCorpus,isClass);
		Label dummyLb1 = toolkit.createLabel(scInput, "", SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
				.applyTo(dummyLb1);

		section.setText(title);
		section.setDescription("<form><p>This section has a <b>tree</b> and a button. It also has <a>a link</a> in the description.</p></form>");
		section.setClient(client);
		section.setExpanded(true);
		GridData gd = new GridData(GridData.FILL_BOTH);
		section.setLayoutData(gd);
		TableLayoutData layoutData = new TableLayoutData();
		layoutData.setSectionClient(client);
		layoutData.setTreeViewer(targetLocationContent.getTreeViewer());
		layoutData.setTargetLocationGroups(targetLocationContent);
		return layoutData;
	}

	public static void updateStatusMessage(final IViewSite site,
			final String message, final Integer error, final ScrolledForm form) {
		// update status bar
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (null == error) {
					site.getActionBars().getStatusLineManager()
							.setErrorMessage(null);
					site.getActionBars().getStatusLineManager()
							.setMessage(null);
				} else if (error == IStatus.ERROR) {
					site.getActionBars()
							.getStatusLineManager()
							.setErrorMessage(
									CommonUiViewImageRegistry
											.getImageIconFactory()
											.getImage(
													ICommonUiConstants.IMAGE_ERROR_SB),
									message);
				} else if (error == IStatus.INFO) {
					site.getActionBars()
							.getStatusLineManager()
							.setMessage(
									CommonUiViewImageRegistry
											.getImageIconFactory()
											.getImage(
													ICommonUiConstants.IMAGE_INFO_SB),
									message);

				} else if (error == IStatus.OK) {
					site.getActionBars().getStatusLineManager()
							.setErrorMessage("");
					site.getActionBars()
							.getStatusLineManager()
							.setMessage(
									CommonUiViewImageRegistry
											.getImageIconFactory()
											.getImage(
													ICommonUiConstants.IMAGE_SUCESS_SB),
									message);
				}

				form.setFocus();

			}
		});

	}

	public static Button createPreprocessLink(Composite client, FormToolkit toolkit) {
		Button preprocessEnabled;
		Composite clientLink = toolkit.createComposite(client);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2)
				.applyTo(clientLink);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
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
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(link);
		return preprocessEnabled;
	}

	public static void addErrorPopup(final Form form, final FormToolkit toolkit) {
		// form.addMessageHyperlinkListener(new HyperlinkAdapter() {
		// @Override
		// public void linkActivated(HyperlinkEvent e) {
		// String title = e.getLabel();
		// Object href = e.getHref();
		// if (href instanceof IMessage[]) {
		// // details =
		// // managedForm.getMessageManager().createSummary((IMessage[])href);
		// }
		//
		// Point hl = ((Control) e.widget).toDisplay(0, 0);
		// hl.x += 10;
		// hl.y += 10;
		// Shell shell = new Shell(form.getShell(), SWT.ON_TOP | SWT.TOOL);
		// shell.setImage(getImage(form.getMessageType()));
		// shell.setText(title);
		// shell.setLayout(new FillLayout());
		// // ScrolledFormText stext = new ScrolledFormText(shell, false);
		// // stext.setBackground(toolkit.getColors().getBackground());
		// FormText text = toolkit.createFormText(shell, true);
		// configureFormText(form, text);
		// // stext.setFormText(text);
		// if (href instanceof IMessage[])
		// text.setText(createFormTextContent((IMessage[]) href),
		// true, false);
		// shell.setLocation(hl);
		// shell.pack();
		// shell.open();
		// }
		// });
	}

	private static Image getImage(int type) {
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

	private static void configureFormText(final Form form, FormText text) {
		text.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
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

	private static String createFormTextContent(IMessage[] messages) {
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

	public static void setConsoleViewInFocus() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().showView(ConsoleView.ID);
				} catch (PartInitException e1) {

				}

			}
		});

	}

	public static void writeConsoleHeaderBegining(String statusText) {
		final DateFormat dateFormat = new SimpleDateFormat(
				"MMM dd, yyyy, HH:mm:ss aaa");
		final Calendar cal = Calendar.getInstance();
		ConsoleView.writeInConsoleHeader(statusText + ", "
				+ (dateFormat.format(cal.getTime())));
	}
}
