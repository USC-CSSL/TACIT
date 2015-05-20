package edu.usc.cssl.nlputils.common.ui.composite.from;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import edu.usc.cssl.nlputils.common.ui.internal.TargetLocationsGroup;
import edu.usc.cssl.nlputils.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.nlputils.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.nlputils.common.ui.validation.OutputPathValidation;

public class NlputilsFormComposite {

	private static TargetLocationsGroup targetLocationContent;

	public static void createEmptyRow(FormToolkit toolkit,
			Composite sectionClient) {
		Label dummy = toolkit.createLabel(sectionClient, "", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(dummy);
	}

	public static OutputLayoutData createOutputSection(FormToolkit toolkit,
			Composite parent, IMessageManager mmng) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR
				| Section.EXPANDED | Section.DESCRIPTION);

		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(section);
		section.setText("Output Path Details"); //$NON-NLS-1$
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

		Label outputPathLbl = toolkit.createLabel(sectionClient,
				"Output Location:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(outputPathLbl);
		Text outputLocationTxt = toolkit.createText(sectionClient, "",
				SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
				.applyTo(outputLocationTxt);

		Button browseBtn = toolkit.createButton(sectionClient, "Browse...",
				SWT.PUSH);
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
						.validateOutputDirectory(outputLocationTxt.getText());
				if (message != null) {

					message = outputPathLbl.getText() + " " + message;
					mmng.addMessage("location", message, null,
							IMessageProvider.ERROR);
				} else {
					mmng.removeMessage("location");
				}
			}
		});

		OutputLayoutData layoutData = new OutputLayoutData();
		layoutData.setOutputLabel(outputLocationTxt);
		layoutData.setSectionClient(sectionClient);
		
		return layoutData;
	}

	public static TableLayoutData createTableSection(final Composite parent,
			FormToolkit toolkit, GridLayout layout, String title, String description) {
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

		NlputilsFormComposite.createEmptyRow(toolkit, client);

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
		targetLocationContent = TargetLocationsGroup.createInForm(scInput, toolkit);
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
}
