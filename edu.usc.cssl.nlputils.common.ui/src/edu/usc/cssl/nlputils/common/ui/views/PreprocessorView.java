package edu.usc.cssl.nlputils.common.ui.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
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

import edu.usc.cssl.nlputils.common.ui.ICommonUiConstants;
import edu.usc.cssl.nlputils.common.ui.composite.from.NlputilsFormComposite;
import edu.usc.cssl.nlputils.common.ui.internal.CommonUiViewImageRegistry;
import edu.usc.cssl.nlputils.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.nlputils.common.ui.outputdata.TableLayoutData;

public class PreprocessorView extends ViewPart {

	public static final String ID = "edu.usc.cssl.nlputils.common.ui.preprocess.view";
	private ScrolledForm form;
	private FormToolkit toolkit;
	public PreprocessorView() {
		// TODO Auto-generated constructor stub
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

		NlputilsFormComposite.createEmptyRow(toolkit, sc);
		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1)
				.applyTo(client);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
	
		TableLayoutData layData = NlputilsFormComposite.createTableSection(client, toolkit,
				layout, "Input Dtails", "Add file(s) or Folder(s) which contains data", true);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(layData.getSectionClient());

		createPreprocessLink(layData.getSectionClient());

		Composite client1 = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1)
				.applyTo(client1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client1);

		OutputLayoutData layoutData = NlputilsFormComposite
				.createOutputSection(toolkit, client1, form.getMessageManager());

		createAdditionalOptions(layoutData.getSectionClient());
		// we dont need stop word's as it will be taken from the preprocessor
		// settings

	}
	
	private void createAdditionalOptions(Composite sectionClient) {
		Label outputPathLbl = toolkit.createLabel(sectionClient,
				"Output File Suffix:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(outputPathLbl);
		Text outputLocationTxt = toolkit.createText(sectionClient, "",
				SWT.BORDER);
		outputLocationTxt.setText("_preprocessed");
		GridDataFactory.fillDefaults().grab(true, false).span(2, 0)
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
		Hyperlink link = toolkit
				.createHyperlink(clientLink, "Preprocess", SWT.NONE);
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

		public void run() {

			Job job = new Job("PreProcessing...") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("NLPUtils started preprocessing...", 100);

					int i = 0;
					while (i < 1000000000) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (monitor.isCanceled()) {
							throw new OperationCanceledException();

						}
						i++;
						monitor.worked(1);
					}
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			job.setUser(true);
			job.schedule();

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

		public void run() {

		};
	});
	form.getToolBarManager().update(true);
}


	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
