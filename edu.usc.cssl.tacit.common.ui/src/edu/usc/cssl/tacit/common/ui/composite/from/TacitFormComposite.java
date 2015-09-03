package edu.usc.cssl.tacit.common.ui.composite.from;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import edu.usc.cssl.tacit.common.ui.ICommonUiConstants;
import edu.usc.cssl.tacit.common.ui.internal.CommonUiViewImageRegistry;
import edu.usc.cssl.tacit.common.ui.internal.TargetLocationsGroup;
import edu.usc.cssl.tacit.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.tacit.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.tacit.common.ui.validation.OutputPathValidation;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class TacitFormComposite {

	private static TargetLocationsGroup targetLocationContent;

	public static void createEmptyRow(FormToolkit toolkit,
			Composite sectionClient) {
		Label dummy =  new Label(sectionClient, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(dummy);
	}
	
	
	public static void createCorpusSection(Composite client){

		Group group = new Group(client, SWT.SHADOW_IN);
		group.setText("Input Type");

		// group.setBackground(client.getBackground());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);

		final Button corpusEnabled = new Button(group, SWT.CHECK);
		corpusEnabled.setText("Use Corpus");
		corpusEnabled.setBounds(10, 10, 10, 10);
		corpusEnabled.pack();

		// TacitFormComposite.createEmptyRow(toolkit, group);

		final Composite sectionClient = new Composite(group, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(sectionClient);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
				.applyTo(sectionClient);
		sectionClient.setEnabled(false);
		sectionClient.pack();

		// Create a row that holds the textbox and browse button
		final Label inputPathLabel = new Label(sectionClient, SWT.NONE);
		inputPathLabel.setText("Select Corpus:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(inputPathLabel);
		final Text corpusNameText = new Text(sectionClient, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
				.applyTo(corpusNameText);

		inputPathLabel.setEnabled(false);
		corpusNameText.setEnabled(false);
		Combo cmbSortType = new Combo(client, SWT.FLAT | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(cmbSortType);
		cmbSortType.setItems(new String[]{"one","two"});
		cmbSortType.select(0);	

		corpusEnabled.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (corpusEnabled.getSelection()) {
					sectionClient.setEnabled(true);

					inputPathLabel.setEnabled(true);
					corpusNameText.setEnabled(true);

				} else {
					sectionClient.setEnabled(false);
					inputPathLabel.setEnabled(false);
					corpusNameText.setEnabled(false);
				}
			}
		});

		// TacitFormComposite.createEmptyRow(sectionClient, group);
	
	}

	public static OutputLayoutData createOutputSection(FormToolkit toolkit,
			Composite parent, final IMessageManager mmng) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR
				| Section.EXPANDED | Section.DESCRIPTION);

		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(section);
		section.setText("Output Path "); //$NON-NLS-1$
		section.setDescription("Choose output path for storing the results");

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
		section.setText("Input Path "); //$NON-NLS-1$
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
			String description, boolean isFolder, boolean isFile) {
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
				toolkit, isFolder, isFile);
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

	public static void addErrorPopup(final Form form, final FormToolkit toolkit) {
//		form.addMessageHyperlinkListener(new HyperlinkAdapter() {
//			@Override
//			public void linkActivated(HyperlinkEvent e) {
//				String title = e.getLabel();
//				Object href = e.getHref();
//				if (href instanceof IMessage[]) {
//					// details =
//					// managedForm.getMessageManager().createSummary((IMessage[])href);
//				}
//
//				Point hl = ((Control) e.widget).toDisplay(0, 0);
//				hl.x += 10;
//				hl.y += 10;
//				Shell shell = new Shell(form.getShell(), SWT.ON_TOP | SWT.TOOL);
//				shell.setImage(getImage(form.getMessageType()));
//				shell.setText(title);
//				shell.setLayout(new FillLayout());
//				// ScrolledFormText stext = new ScrolledFormText(shell, false);
//				// stext.setBackground(toolkit.getColors().getBackground());
//				FormText text = toolkit.createFormText(shell, true);
//				configureFormText(form, text);
//				// stext.setFormText(text);
//				if (href instanceof IMessage[])
//					text.setText(createFormTextContent((IMessage[]) href),
//							true, false);
//				shell.setLocation(hl);
//				shell.pack();
//				shell.open();
//			}
//		});
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
		ConsoleView.writeInConsoleHeader(statusText
				+ (dateFormat.format(cal.getTime())));
	}
}
