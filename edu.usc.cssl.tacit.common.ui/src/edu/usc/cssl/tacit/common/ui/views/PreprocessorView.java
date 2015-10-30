package edu.usc.cssl.tacit.common.ui.views;

import java.io.File;
import java.util.List;

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
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.tacit.common.ui.ICommonUiConstants;
import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.internal.CommonUiViewImageRegistry;
import edu.usc.cssl.tacit.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.tacit.common.ui.utility.TacitUtil;

public class PreprocessorView extends ViewPart {

	public static final String ID = "edu.usc.cssl.tacit.common.ui.preprocess.view";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private Text outputLocationTxt;
	private TableLayoutData layData;
	public PreprocessorView() {
		
	}

	@Override
	public void createPartControl(Composite parent) {
		toolkit = createFormBodySection(parent);
		Section section = toolkit.createSection(form.getBody(),
				Section.TITLE_BAR | Section.EXPANDED );
          
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1)
				.applyTo(section);
		section.setExpanded(true);
		String description = "This section gives details about general preprocessor ";
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

		TacitFormComposite.createEmptyRow(toolkit, sc);
		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1)
				.applyTo(client);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
	
		layData = TacitFormComposite.createTableSection(client, toolkit,
				layout, "Input Details", "Add File(s) and Folder(s) to include in analysis.", true, true, false,true);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(layData.getSectionClient());

		createPreprocessLink(layData.getSectionClient());

		Composite client1 = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2)
				.applyTo(client1);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1)
				.applyTo(client1);

		createAdditionalOptions(client1);
		addButtonsToToolBar();
//		OutputLayoutData layoutData = TacitFormComposite
//				.createOutputSection(toolkit, client1, form.getMessageManager());
//
//		createAdditionalOptions(layoutData.getSectionClient());

	}
	
	private void createAdditionalOptions(Composite sectionClient) {
		Label outputPathLbl = toolkit.createLabel(sectionClient,
				"Output Folder Name:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(outputPathLbl);
		outputLocationTxt = toolkit.createText(sectionClient, "",
				SWT.BORDER);
		outputLocationTxt.setText("preprocessed");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0)
				.applyTo(outputLocationTxt);
	}
	
	private void createPreprocessLink(Composite client) {
			
			Composite clientLink = toolkit.createComposite(client);
			GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2)
					.applyTo(clientLink);
			GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
					.applyTo(clientLink);
	
			Button stemEnabled = toolkit.createButton(clientLink,
					"", SWT.CHECK);
			stemEnabled.setEnabled(false);
			stemEnabled.setSelection(true);
			GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(stemEnabled);
			final Hyperlink link = toolkit
					.createHyperlink(clientLink, "Preprocessing Settings", SWT.NONE);
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
			GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(link);
	
		}

	private FormToolkit createFormBodySection(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
	
		toolkit.decorateFormHeading(form.getForm());
		form.setText("General PreProcessor"); //$NON-NLS-1$
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true)
				.applyTo(form.getBody());
		return toolkit;
	}

	private void addButtonsToToolBar() {
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (CommonUiViewImageRegistry
						.getImageIconFactory()
						.getImageDescriptor(ICommonUiConstants.IMAGE_LRUN_OBJ));
			}
	
			@Override
			public String getToolTipText() {
				return "Analyze";
			}
	
			@Override
			public void run() {
				if (!canProceed()) return;
	
			};
		});
		
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (CommonUiViewImageRegistry
						.getImageIconFactory()
						.getImageDescriptor(ICommonUiConstants.IMAGE_HELP_CO));
			}
	
			@Override
			public String getToolTipText() {
				return "Help";
			}
	
			@Override
			public void run() {
	
			};
		});
		form.getToolBarManager().update(true);
	}


	private boolean canProceed() {
		TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
		form.getMessageManager().removeMessage("input");
		form.getMessageManager().removeMessage("inputNoProper");
		form.getMessageManager().removeMessage("noOutput");
		
		List<String> inputFiles = TacitUtil.refineInput(layData.getSelectedFiles());
		boolean noProperFiles = true;

		if (inputFiles.size() < 1) {
			form.getMessageManager().addMessage("input",
					"Select/Add atleast one Class 1 file", null,
					IMessageProvider.ERROR);
			return false;
		}
		
		for (String string : inputFiles) {
			if (new File(string).isFile() && !string.contains("DS_Store")) {
				noProperFiles = false;
				break;
			}
		}
		if (noProperFiles) {
			form.getMessageManager().addMessage("inputNoProper",
					"Select/Add atleast one Proper Class 1 file", null,
					IMessageProvider.ERROR);
			return false;
		}
		
		if (outputLocationTxt.getText().trim().length() == 0) {
			form.getMessageManager().addMessage("noOutput",
					"Add output location", null,
					IMessageProvider.ERROR);
			return false;
		}
		return true;
	}
	
	@Override
	public void setFocus() {
		

	}

}
