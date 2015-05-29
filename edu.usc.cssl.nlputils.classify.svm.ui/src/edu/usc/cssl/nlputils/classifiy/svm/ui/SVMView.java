package edu.usc.cssl.nlputils.classifiy.svm.ui;

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.nlputils.classify.svm.ui.internal.ISVMViewConstants;
import edu.usc.cssl.nlputils.classify.svm.ui.internal.SVMViewImageRegistry;
import edu.usc.cssl.nlputils.common.ui.composite.from.NlputilsFormComposite;
import edu.usc.cssl.nlputils.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.nlputils.common.ui.outputdata.TableLayoutData;

public class SVMView extends ViewPart implements ISVMViewConstants {

	private FormToolkit toolkit;
	private ScrolledForm form;
	public static String ID = "edu.usc.cssl.nlputils.classify.svm.ui.view1";
	private TableLayoutData class1LayoutData;
	private TableLayoutData class2LayoutData;
	private OutputLayoutData layoutData;
	private Text class1Name;
	private Text class2Name;
	private Label class1Label;
	private Label class2Label;
	private Label kValueLabel;
	private Text kValue;
	private Button preprocessButton;
	private Button featureFileButton;
	
	@Override
	public void createPartControl(Composite parent) {
		toolkit = createFormBodySection(parent);
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(section);
		section.setExpanded(true);
		
		String description = "This sections gives details about the SVM Classifier";
		FormText descriptionFrm = toolkit.createFormText(section, false);
		descriptionFrm.setText("<form><p>" + description + "</p></form>", true,false);
		section.setDescriptionControl(descriptionFrm);

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);

		NlputilsFormComposite.createEmptyRow(toolkit, sc);
		
		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(2).applyTo(client);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(client);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		class1LayoutData = NlputilsFormComposite.createTableSection(client,toolkit, layout, "Class 1 Details","Add File(s) or Folder(s) which contains data", true);
		class2LayoutData = NlputilsFormComposite.createTableSection(client,toolkit, layout, "Class 2 Details","Add File(s) or Folder(s) which contains data", true);
	
		createPreprocessLink(form.getBody());
		createInputParams(form.getBody());
		//Output Data
		Composite client1 = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1).applyTo(client1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(client1);

		layoutData = NlputilsFormComposite.createOutputSection(toolkit,client1, form.getMessageManager());
		Composite output = layoutData.getSectionClient();
		
		featureFileButton = toolkit.createButton(output, "Create feature weights file", SWT.CHECK);
		featureFileButton.setBounds(10, 35, 10, 10);
		featureFileButton.pack();
		
		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter());
		// form.setMessage("Invalid path", IMessageProvider.ERROR);
		this.setPartName("SVM Classification");
		addButtonsToToolBar();
		toolkit.paintBordersFor(form.getBody());
	}
	
	private void addButtonsToToolBar() {
		IToolBarManager mgr = form.getToolBarManager();
		
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor(){
				return (SVMViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_LRUN_OBJ));
			}
			
			@Override
			public String getToolTipText() {
				return "Run";
			}
			
			public void run() {
				
			};
		});
		
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor(){
				return (SVMViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_HELP_CO));
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
	
	private void createPreprocessLink(Composite client) {
		
		Composite clientLink = toolkit.createComposite(client);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2)
				.applyTo(clientLink);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(clientLink);

		preprocessButton = toolkit.createButton(clientLink,
				"", SWT.CHECK);
		preprocessButton.setSelection(true);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(preprocessButton);
		final Hyperlink link = toolkit
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
	
	private void createInputParams(Composite body){
		Section inputParamsSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(inputParamsSection);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(inputParamsSection);
		inputParamsSection.setText("Input Parameters");
		
		ScrolledComposite sc = new ScrolledComposite(inputParamsSection, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);
		
		Composite sectionClient = toolkit.createComposite(inputParamsSection);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(sectionClient);
		inputParamsSection.setClient(sectionClient);
		
		//Empty row to add gap
		/*Label dummy = toolkit.createLabel(sectionClient, "", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(2, 0).applyTo(dummy);*/
		
		class1Label =  toolkit.createLabel(sectionClient, "Class 1 Label:",SWT.None);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(class1Label);
		class1Name = toolkit.createText(sectionClient, "",SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(class1Name);
		class2Label =  toolkit.createLabel(sectionClient, "Class 2 Label:",SWT.None);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(class2Label);
		class2Name = toolkit.createText(sectionClient, "",SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(class2Name);
		kValueLabel = toolkit.createLabel(sectionClient, "k Value for Cross Validation:",SWT.None);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(kValueLabel);
		kValue = toolkit.createText(sectionClient, "",SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(kValue);
		
	}
	
	private FormToolkit createFormBodySection(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);

		toolkit.decorateFormHeading(form.getForm());
		form.setText("SVM Classifier"); //$NON-NLS-1$
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true)
				.applyTo(form.getBody());
		return toolkit;
	}
	
	@Override
	public void setFocus() {
		form.setFocus();
	}

}
