package edu.usc.cssl.tacit.crawlers.typepad.ui;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CMDataType;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.Corpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;
import edu.usc.cssl.tacit.common.ui.validation.OutputPathValidation;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import edu.usc.cssl.tacit.crawlers.typepad.services.TypePadCrawler;
import edu.usc.cssl.tacit.crawlers.typepad.ui.internal.ITypePadCrawlerUIConstants;
import edu.usc.cssl.tacit.crawlers.typepad.ui.internal.TypePadCrawlerViewImageRegistry;

public class TypePadCrawlerView extends ViewPart {
	public static String ID = "edu.usc.cssl.tacit.crawlers.typepad.ui.typePadView";
	
	private ScrolledForm form;
	private FormToolkit toolkit;
	
	private static Label contentWordFilterLbl;
	private static Text contentWordFilterText;
	private static Label titleWordFilterLbl;
	private static Text titleWordFilterText;
	
	private static Button limitBlogs;
	private static Label maxLimit;
	private static Text maxText;
	
	private static Button publishedTimeRelevanceBtn;
	private static Button relevanceBtn;
	private static Button publishedTimeAscBtn;
	private static Button publishedTimeDescBtn;

	private int sortParameter = -1;

	private ArrayList<String> contentKeywords= null;
	private ArrayList<String> titleKeywords = null;
	
	private Text corpusNameTxt;

	private long maxBlogLimit = 10l; //Default value is 10


	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(form.getForm());
		form.setText("TypePad Crawler"); //$NON-NLS-1$
		form.setImage(TypePadCrawlerViewImageRegistry.getImageIconFactory().getImage(ITypePadCrawlerUIConstants.IMAGE_TYPEPAD_OBJ));

		GridLayoutFactory.fillDefaults().applyTo(form.getBody());

		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);

		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(section);
		section.setExpanded(true);

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);
		TacitFormComposite.addErrorPopup(form.getForm(), toolkit);

		// Output Data
		Composite client1 = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(2).applyTo(client1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(client1);

		createFilterSection(toolkit, client1, form.getMessageManager());
		createLimitSection(toolkit, client1, form.getMessageManager());

		Label dummy = toolkit.createLabel(form.getBody(), "", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(2, 0).applyTo(dummy);

		createSortAttributesSection(toolkit, form.getBody(), form.getMessageManager());

		Label filler = toolkit.createLabel(form.getBody(), "", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(2, 0).applyTo(filler);

		corpusNameTxt = TacitFormComposite.createCorpusSection(toolkit, form.getBody(), form.getMessageManager());

		this.setPartName("TypePad Crawler");
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {

			private Job job;

			@Override
			public ImageDescriptor getImageDescriptor() {
				return (TypePadCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(ITypePadCrawlerUIConstants.IMAGE_LRUN_OBJ));
	
			}

			@Override
			public String getToolTipText() {
				return "Crawl";
			}

			@Override
			public void run() {
				TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
				TacitFormComposite.writeConsoleHeaderBegining("Crawling TypePad started ");


				// check if the output address is correct and writable

				final String corpusName = corpusNameTxt.getText();

				// Get sort attribute values
				if (publishedTimeRelevanceBtn.getSelection()){
					sortParameter = 0;
				}else{
					if(relevanceBtn.getSelection()){
						sortParameter = 1;
					}else{
						if(publishedTimeAscBtn.getSelection()){
							sortParameter = 2;
						}else{
							sortParameter = 3;
						}
					}
				}
	
				//Get max blog limit
				if (limitBlogs.getSelection()) {
					maxBlogLimit = Long.parseLong(maxText.getText());
						
				}else{
					maxBlogLimit = -1; //Indicates max limit is not imposed
				}
				
				//Get the content word filters
				if (!contentWordFilterText.getText().isEmpty()){
					contentKeywords = getKeywords(contentWordFilterText.getText());
				}else{
					contentKeywords = null;
				}
				
				//Get the title word filters
				if (!titleWordFilterText.getText().isEmpty()){
					titleKeywords = getKeywords(titleWordFilterText.getText());
				}else{
					titleKeywords = null;
				}
				
				job = new Job("Typepad Crawl Job") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {

						try {
							TacitFormComposite.setConsoleViewInFocus();
							TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
							TacitFormComposite.writeConsoleHeaderBegining("TypePad Crawling Started... ");
	
							TypePadCrawler typePadCrawler = new TypePadCrawler();
							if (maxBlogLimit != -1){
								monitor.beginTask("Crawling typepad..",(int)maxBlogLimit+20);
							}else{
								monitor.beginTask("Crawling typepad..",5000);
							}
							
							
							String corpusClassDir = ITypePadCrawlerUIConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName + File.separator + corpusName + "_class";
							
							if (!new File(corpusClassDir).exists()) {
								new File(corpusClassDir).mkdirs();
							}
							
							//Creating the corpus and corpus class
							ConsoleView.printlInConsoleln("Creating Corpus " + corpusName + "...");
							Corpus typepadCorpus = new Corpus(corpusName, CMDataType.TYPEPAD_JSON);
							CorpusClass typepadCorpusClass = new CorpusClass();
							typepadCorpusClass.setClassName(corpusName + "_class");
							typepadCorpusClass.setClassPath(corpusClassDir);
							typepadCorpus.addClass(typepadCorpusClass);
							
							if (monitor.isCanceled()){
								throw new OperationCanceledException();
							}
							
							ConsoleView.printlInConsoleln("Saving Corpus " + corpusName + "...");
							monitor.worked(10);
							typePadCrawler.getQueryResults(contentKeywords,titleKeywords,maxBlogLimit,sortParameter,corpusClassDir,corpusName,monitor); //This method starts the crawling 
							ManageCorpora.saveCorpus(typepadCorpus);
							monitor.worked(10);
							monitor.done();

							return Status.OK_STATUS;
						} catch (OperationCanceledException e) {
							ConsoleView.printlInConsoleln("Operation Cancelled by the user.");
							return Status.CANCEL_STATUS;
						}catch (Exception e){
							e.printStackTrace();
							return Status.CANCEL_STATUS;
						}

					}
				};
				if (canProceedCrawl()) {
					job.setUser(true);
					job.schedule();
					job.addJobChangeListener(new JobChangeAdapter() {

						public void done(IJobChangeEvent event) {
							if (!event.getResult().isOK()) {
								TacitFormComposite.writeConsoleHeaderBegining("Error: <Terminated> TypePad Crawler  ");
								TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is stopped",IStatus.INFO, form);

							} else {
								TacitFormComposite.writeConsoleHeaderBegining("Success: <Completed> TypePad Crawler  ");
								TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is completed",IStatus.INFO, form);
								ConsoleView.printlInConsoleln("TypePad crawler completed successfully.");
								ConsoleView.printlInConsoleln("Done");

							}
						}
					});
				}
			}

		});
		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (TypePadCrawlerViewImageRegistry.getImageIconFactory().getImageDescriptor(ITypePadCrawlerUIConstants.IMAGE_HELP_CO));
			}

			@Override
			public String getToolTipText() {
				return "Help";
			}

			@Override
			public void run() {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp("edu.usc.cssl.tacit.crawlers.twitter.ui.twitter");
			};
		};
		mgr.add(helpAction);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(helpAction, "edu.usc.cssl.tacit.crawlers.twitter.ui.twitter");
		PlatformUI.getWorkbench().getHelpSystem().setHelp(form, "edu.usc.cssl.tacit.crawlers.twitter.ui.twitter");
		form.getToolBarManager().update(true);
		toolkit.paintBordersFor(form.getBody());
	}
	
	
	
	
	
	
	

	/**
	 * This method performs the final validation checks before execution.
	 * @return valid boolean value
	 */
	private boolean canProceedCrawl() {
		
		String contentKeywords[] = {};
		String titleKeywords[] = {};
		
		TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);

		//Removing any other previous messages with the same key.
		form.getMessageManager().removeMessage("maxlimit");
		form.getMessageManager().removeMessage("location");
		form.getMessageManager().removeMessage("keywordLength");
		form.getMessageManager().removeMessage("keywordState");
		form.getMessageManager().removeMessage("corpusName");
		
		
		//Check 1: Check if the the keyword field is not empty
		if (!contentWordFilterText.getText().isEmpty()){
			contentKeywords = contentWordFilterText.getText().split(";");
		}
			
		if (!titleWordFilterText.getText().isEmpty()){
			titleKeywords = titleWordFilterText.getText().split(";");
		}
		
		if (contentKeywords.length == 0 && titleKeywords.length == 0){
			form.getMessageManager().addMessage("keywordLength", "Word Filter cannot be empty.Add atleast one content or title word filter.", null,IMessageProvider.ERROR);
			return false;
			
		}else{
			//Check 2: Check if the keyword entered are valid or not
			if (contentKeywords.length != 0 && !isValidKeywordState(contentWordFilterText.getText())){
				form.getMessageManager().addMessage("keywordState", "Either Content or Title Word Filter is not properly formed.", null,IMessageProvider.ERROR);
				return false;
			}
			
			if (titleKeywords.length != 0 && !isValidKeywordState(titleWordFilterText.getText())){
				form.getMessageManager().addMessage("keywordState", "Either Content or Title Word Filter is not properly formed.", null,IMessageProvider.ERROR);
				return false;
			}
		}


		//Check 3: Check if the max limit for blogs is valid or not
		if (limitBlogs.getSelection()) {
			
			try {
				maxBlogLimit = Long.parseLong(maxText.getText());
				if (maxBlogLimit <= 0){
					form.getMessageManager().addMessage("maxlimit","Error: Invalid Max Limit for blogs. Please enter valid positive number.", null,IMessageProvider.ERROR);
					return false;
				}

			} catch (NumberFormatException e1) {
				form.getMessageManager().addMessage("maxlimit","Error: Invalid Max Limit for blogs. Please enter valid positive number.", null,IMessageProvider.ERROR);
				return false;
			}
		}
		
		//Check 4: Check if the output corpus is valid.
		String corpusName = corpusNameTxt.getText();
		
		if(corpusName == null|| corpusName.isEmpty()) {
			form.getMessageManager().addMessage("corpusName", "Provide corpus name", null, IMessageProvider.ERROR);
			return false;
		} else {
			String outputDir = ITypePadCrawlerUIConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
			if(new File(outputDir).exists()){
				form.getMessageManager().addMessage("corpusName", "Corpus already exists", null, IMessageProvider.ERROR);
				return false;
			}

		}
		
		return true;
	}
	

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		form.setFocus();
	}

	
	/**
	 * This method creates the Filter section on the UI.
	 * @param toolkit
	 * @param parent
	 * @param mmng
	 */
	public static void createFilterSection(FormToolkit toolkit, Composite parent, final IMessageManager mmng) {
		Section section = toolkit.createSection(parent,Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION | Section.TWISTIE);

		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(section);
		section.setText("Filter Settings "); 
		section.setDescription("Use Semicolon to separate word filters.");

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(sc);

		Composite sectionClient = toolkit.createComposite(section);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(sectionClient);
		section.setClient(sectionClient);

		TacitFormComposite.createEmptyRow(toolkit, sectionClient);

		//Adding the components of Filter Section
		
		//Content word filter
		contentWordFilterLbl = toolkit.createLabel(sectionClient, "Content Word Filter", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(contentWordFilterLbl);

		
		contentWordFilterText = toolkit.createText(sectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(contentWordFilterText);
		contentWordFilterText.setMessage("For example: movie;\"star wars\"");
		
		//Title word filter
		titleWordFilterLbl = toolkit.createLabel(sectionClient, "Title Word Filter", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(titleWordFilterLbl);
		
		titleWordFilterText = toolkit.createText(sectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(titleWordFilterText);
		titleWordFilterText.setMessage("For example: movie;\"star wars\"");


	}

	/**
	 * This method creates the Blog Limit section on the UI.
	 * @param toolkit
	 * @param parent
	 * @param mmng
	 */
	public static void createLimitSection(FormToolkit toolkit, Composite parent, final IMessageManager mmng) {
		Section section = toolkit.createSection(parent,Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION | Section.TWISTIE);

		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).applyTo(section);
		section.setText("Limit Blogs"); //$NON-NLS-1$

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(6).equalWidth(false).applyTo(sc);

		Composite sectionClient = toolkit.createComposite(section);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(sectionClient);
		section.setClient(sectionClient);

		TacitFormComposite.createEmptyRow(toolkit, sectionClient);

		//Adding the components of Filter Section
		
		limitBlogs = new Button(sectionClient, SWT.CHECK);
		limitBlogs.setText("Limit number of blogs to be crawled");
		GridDataFactory.fillDefaults().grab(false, false).span(2, 0).applyTo(limitBlogs);
		
		//Max Limit
		maxLimit = toolkit.createLabel(sectionClient, "Max Limit:");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(maxLimit);

		maxText = toolkit.createText(sectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(maxText);
		maxText.setText("10");
		maxText.setEnabled(false);
		
		limitBlogs.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (limitBlogs.getSelection()){
					maxText.setEnabled(true);
				}else{
					maxText.setEnabled(false);
					
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		

	}

	/**
	 * This method creates the Sort Attributes section on the UI
	 * @param toolkit
	 * @param parent
	 * @param mmng
	 */
	public static void createSortAttributesSection(FormToolkit toolkit, Composite parent,final IMessageManager mmng) {
		Section section = toolkit.createSection(parent,Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION | Section.TWISTIE);

		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(section);
		GridLayoutFactory.fillDefaults().numColumns(4).applyTo(section);
		section.setText("Sort Attributes "); //$NON-NLS-1$
		section.setDescription("Choose values for Filter");

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(false).applyTo(sc);

		Composite sectionClient = toolkit.createComposite(section);
		sc.setContent(sectionClient);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sc);
		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(false).applyTo(sectionClient);
		section.setClient(sectionClient);

		Label dummy = toolkit.createLabel(sectionClient, "", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(4, 0).applyTo(dummy);

		publishedTimeRelevanceBtn = new Button(sectionClient, SWT.RADIO);
		publishedTimeRelevanceBtn.setText("Published Time Relevance");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(publishedTimeRelevanceBtn);

		relevanceBtn = new Button(sectionClient, SWT.RADIO);
		relevanceBtn.setText("Relevance");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(relevanceBtn);
		relevanceBtn.setSelection(true);

		publishedTimeAscBtn = new Button(sectionClient, SWT.RADIO);
		publishedTimeAscBtn.setText("Published Time Ascending");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(publishedTimeAscBtn);

		publishedTimeDescBtn = new Button(sectionClient, SWT.RADIO);
		publishedTimeDescBtn.setText("Published Time Descending");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(publishedTimeDescBtn);
	}
	
	
	/**
	 * This method returns the keywords from the raw content or title word filter string. 
	 * @param rawKeywords 
	 * @return List of keywords if rawKeywords is not empty else returns null 
	 */
	private ArrayList<String> getKeywords(String rawKeywords){
		ArrayList<String> keywords = new ArrayList<String>();
		if (rawKeywords != null && !rawKeywords.isEmpty()){
			String[] splitKeywords = rawKeywords.split(";");
			
			for (String keyword: splitKeywords){
				if (keyword.length() != 0){
					if (keyword.contains(" ")){
						keyword = "\"" + keyword + "\"";
					}
					keywords.add(keyword.trim().toLowerCase());
				}
			}
			
			return keywords;
		}

		return null;
	}
	
	/**
	 * This method returns the boolean indicating whether the raw input keyword string is valid or not.
	 * @param rawKeywords user input raw keyword string
	 * @return
	 */
	private static boolean isValidKeywordState(String rawKeywords){
		boolean isValid = true;
		Pattern wordPattern = Pattern.compile("\\p{Punct}");
		Matcher matcher;
		
		if (rawKeywords != null && !rawKeywords.isEmpty()){
			String[] splitKeywords = rawKeywords.split(";");
			
			for (String keyword: splitKeywords){
				keyword = keyword.trim();
				if (keyword.length() != 0){

					matcher = wordPattern.matcher(keyword);
					
					if (matcher.find()){
						isValid &= false; //Indicates that keywords do not match the specified pattern
						break; 
					}
				}else{
					isValid &= false; //Indicates that there is an empty string as a keyword
					break;
				}
			}
		}
		else{
			isValid &= false; //Indicates that there is no keyword is entered  
		}
		
		return isValid;
	}

}
