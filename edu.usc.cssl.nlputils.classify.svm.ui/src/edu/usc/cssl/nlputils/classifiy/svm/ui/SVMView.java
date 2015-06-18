package edu.usc.cssl.nlputils.classifiy.svm.ui;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
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

import edu.usc.cssl.nlputils.classify.svm.services.CrossValidator;
import edu.usc.cssl.nlputils.classify.svm.services.SVMClassify;
import edu.usc.cssl.nlputils.classify.svm.ui.internal.ISVMViewConstants;
import edu.usc.cssl.nlputils.classify.svm.ui.internal.SVMViewImageRegistry;
import edu.usc.cssl.nlputils.common.ui.composite.from.NlputilsFormComposite;
import edu.usc.cssl.nlputils.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.nlputils.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.nlputils.common.ui.validation.OutputPathValidation;
import edu.usc.nlputils.common.Preprocess;

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
	protected Job job;
	
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
		class1LayoutData = NlputilsFormComposite.createTableSection(client,toolkit, layout, "Class 1 Details","Add File(s) and Folder(s) to include in analysis.", true);
		class2LayoutData = NlputilsFormComposite.createTableSection(client,toolkit, layout, "Class 2 Details","Add File(s) and Folder(s) to include in analysis.", true);
	
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
		this.setPartName("SVM Classification");
		addButtonsToToolBar();
		toolkit.paintBordersFor(form.getBody());
	}
	
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == Job.class) {
			return job;
		}
		return super.getAdapter(adapter);
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
				if (!canProceed()) return;
				final List<String> class1Files = class1LayoutData.getSelectedFiles();
				final List<String> class2Files = class2LayoutData.getSelectedFiles();
				final String class1NameStr = class1Name.getText();
				final String class2NameStr = class2Name.getText();
				final int kValueInt = Integer.parseInt(kValue.getText());;
				final String outputPath = layoutData.getOutputLabel().getText();
				final boolean featureFile = featureFileButton.getSelection();
				final boolean ppValue = preprocessButton.getSelection();
				final Preprocess preprocessor = new Preprocess("SVM_Classifier");
				
				final SVMClassify svm = new SVMClassify(class1NameStr, class2NameStr, outputPath);
				final CrossValidator cv = new CrossValidator();
				 job = new Job("Classifier Job"){
					protected IStatus run(IProgressMonitor monitor){ 
						monitor.beginTask("SVM Classification", kValueInt+4);
						String ppClass1 = "";
						String ppClass2 = "";
						File[] class1FilesL;
						File[] class2FilesL;
						if (ppValue){
							try {
								ppClass1 = preprocessor.doPreprocessing(class1Files,class1NameStr);
								ppClass2 = preprocessor.doPreprocessing(class2Files,class2NameStr);
							} catch (IOException e) {
								return null;
							}
							
							class1FilesL = (new File(ppClass1)).listFiles();
							class2FilesL = (new File(ppClass2)).listFiles();
						}
						else {
							for (Iterator<String> iter = class1Files.listIterator(); iter.hasNext(); ) {
							    String string = iter.next();
							    if ((new File(string)).isDirectory() || string.contains("DS_Store")) {
							        iter.remove();
							    }
							}
//							for (String string : class1Files) {
//								if ((new File(string)).isDirectory() || string.contains("DS_Store")){
//									class1Files.remove(string);
//								}
//							}
							class1FilesL = new File[class1Files.size()];
							for (int i=0; i < class1Files.size(); i++) {
								class1FilesL[i] = new File(class1Files.get(i));
							}
							
							for (Iterator<String> iter = class2Files.listIterator(); iter.hasNext(); ) {
							    String string = iter.next();
							    if ((new File(string)).isDirectory() || string.contains("DS_Store")) {
							        iter.remove();
							    }
							}
//							for (String string : class2Files) {
//								if ((new File(string)).isDirectory() || string.contains("DS_Store")){
//									class2Files.remove(string);
//								}
//							}
							class2FilesL = new File[class2Files.size()];
							for (int i=0; i < class2Files.size(); i++) {
								class2FilesL[i] = new File(class2Files.get(i));
							}
						}
						monitor.worked(2);
						
						try {
							
							cv.doCross(svm, class1NameStr, class1FilesL, class2NameStr, class2FilesL, kValueInt, featureFile,monitor);
							//monitor.worked(5);
							if (ppValue && preprocessor.doCleanUp()){
								preprocessor.clean();
								//monitor.worked(5);
							
							}

							monitor.worked(2);
						} catch (NumberFormatException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						monitor.done();
						NlputilsFormComposite.updateStatusMessage(getViewSite(), "SVM analysis completed", IStatus.OK);
						return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				job.schedule();
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
	
	protected boolean canProceed() {
		//Remove all errors from any previous tries
		NlputilsFormComposite.updateStatusMessage(getViewSite(), null,null);
		form.getMessageManager().removeMessage("class1");
		form.getMessageManager().removeMessage("class2");
		form.getMessageManager().removeMessage("class1NoProper");
		form.getMessageManager().removeMessage("class2NoProper");
		form.getMessageManager().removeMessage("class1Name");
		form.getMessageManager().removeMessage("class2Name");
		form.getMessageManager().removeMessage("sameName");
		form.getMessageManager().removeMessage("kValueEmpty");
		form.getMessageManager().removeMessage("kValue");
		form.getMessageManager().removeMessage("output");
		
		List<String> class1Files = class1LayoutData.getSelectedFiles();
		List<String> class2Files = class2LayoutData.getSelectedFiles();
		boolean noProperFiles = true;
		
		if (class1Files.size() < 1) {
			form.getMessageManager().addMessage("class1","Select/Add atleast one Class 1 file", null,IMessageProvider.ERROR);
			return false;
		}
		form.getMessageManager().removeMessage("class1");
		for (String string : class1Files) {
			if (new File(string).isFile() && !string.contains("DS_Store")){
				noProperFiles = false;
				break;
			}
		}
		if (noProperFiles){
			form.getMessageManager().addMessage("class1NoProper","Select/Add atleast one Proper Class 1 file", null,IMessageProvider.ERROR);
			return false;
		}
		
		noProperFiles = true;
		if (class2Files.size() < 1) {
			form.getMessageManager().addMessage("class2","Select/Add atleast one Class 2 file", null,IMessageProvider.ERROR);
			return false;
		}
		form.getMessageManager().removeMessage("class2");
		for (String string : class2Files) {
			if (new File(string).isFile() && !string.contains("DS_Store")){
				noProperFiles = false;
				break;
			}
		}
		if (noProperFiles){
			form.getMessageManager().addMessage("class2NoProper","Select/Add atleast one Proper Class 2 file", null,IMessageProvider.ERROR);
			return false;
		}
		
		if (class1Name.getText().trim().length() == 0) {
			form.getMessageManager().addMessage("class1Name","Class 1 name cannot be empty", null,IMessageProvider.ERROR);
			return false;
		}
		form.getMessageManager().removeMessage("class1Name");
		
		if (class2Name.getText().trim().length() == 0) {
			form.getMessageManager().addMessage("class2Name","Class 2 name cannot be empty", null,IMessageProvider.ERROR);
			return false;
		}
		form.getMessageManager().removeMessage("class2Name");
		
		if (class2Name.getText().trim().equals(class1Name.getText().trim())) {
			form.getMessageManager().addMessage("sameName","Class 1 and Class 2 cannot have the same name", null,IMessageProvider.ERROR);
			return false;
		}
		form.getMessageManager().removeMessage("sameName");
		
		String kValueText = kValue.getText();
		if (kValueText.trim().length() == 0) {
			form.getMessageManager().addMessage("kValueEmpty","k Value cannot be empty", null,IMessageProvider.ERROR);
			return false;
		}
		form.getMessageManager().removeMessage("kValueEmpty");
		
		int value = 0;
		try {
			value = Integer.parseInt(kValueText);
		}catch(NumberFormatException e) { 
			form.getMessageManager().addMessage("kValue","k Value should be an integer", null,IMessageProvider.ERROR);
	        return false; 
	    } catch(NullPointerException e) {
	    	form.getMessageManager().addMessage("kValue","k Value should be an integer", null,IMessageProvider.ERROR);
	        return false;
	    }
		if (value < 0){
			form.getMessageManager().addMessage("kValue","k Value should be greater than or equal to 0", null,IMessageProvider.ERROR);
	        return false;
		}
		form.getMessageManager().removeMessage("kValue");
		
		String message = OutputPathValidation.getInstance().validateOutputDirectory(layoutData.getOutputLabel().getText(),"Output");
		if (message != null) {

			message = layoutData.getOutputLabel().getText() + " " + message;
			form.getMessageManager().addMessage("output", message, null,IMessageProvider.ERROR);
			return false;
		} 
		form.getMessageManager().removeMessage("output");
		
		return true;
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
		form.setText("SVM Classifier"); 
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true)
				.applyTo(form.getBody());
		return toolkit;
	}
	
	@Override
	public void setFocus() {
		form.setFocus();
	}

}
