package edu.usc.cssl.tacit.crawlers.latin.ui;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CMDataType;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.Corpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;
import edu.usc.cssl.tacit.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import edu.usc.cssl.tacit.crawlers.latin.services.LatinCrawler;
import edu.usc.cssl.tacit.crawlers.latin.ui.internal.ILatinCrawlerUIConstants;
import edu.usc.cssl.tacit.crawlers.latin.ui.internal.LatinCrawlerImageRegistry;

public class LatinCrawlerView extends ViewPart implements ILatinCrawlerUIConstants {
	public static final String ID = "edu.usc.cssl.tacit.crawlers.latin.ui.view1";
	private ScrolledForm form;
	Corpus corpus;
	private AuthorListDialog dialog;
	private SortedSet<String> authors;
	private Table authorTable;
	private Text corpusNameTxt;
	private List<String> selectedAuthors;
	private Set<String> authorListFromWeb;
	private OutputLayoutData layoutData;
	private LatinCrawler latinCrawler;
	private Button removeAuthor;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		latinCrawler = new LatinCrawler();
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(form.getForm());
		form.setText("Latin Crawler"); //$NON-NLS-1$
		form.setImage(LatinCrawlerImageRegistry.getImageIconFactory()
				.getImage(ILatinCrawlerUIConstants.IMAGE_LATIN));
		GridLayoutFactory.fillDefaults().applyTo(form.getBody());

		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);

		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(section);
		section.setExpanded(true);
		String description = "This sections gives details about the crawler settigs";
		FormText descriptionFrm = toolkit.createFormText(section, false);
		descriptionFrm.setText("<form><p>" + description + "</p></form>", true, false);
		section.setDescriptionControl(descriptionFrm);

		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);
		TacitFormComposite.addErrorPopup(form.getForm(), toolkit);

		Section authorSection = toolkit.createSection(form.getBody(),
				Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);

		GridDataFactory.fillDefaults().span(3, 1).applyTo(authorSection);
		authorSection.setExpanded(true);
		authorSection.setText("Author Details"); //$NON-NLS-1$
		authorSection.setDescription("Add list of authors that need to be crawled");

		ScrolledComposite authorsc = new ScrolledComposite(authorSection, SWT.H_SCROLL | SWT.V_SCROLL);
		authorsc.setExpandHorizontal(true);
		authorsc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(authorsc);

		Composite authorSectionClient = toolkit.createComposite(authorSection);
		authorsc.setContent(authorSectionClient);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(authorsc);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(authorSectionClient);
		authorSection.setClient(authorSectionClient);

		Label dummy1 = toolkit.createLabel(authorSectionClient, "", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0).applyTo(dummy1);

		authorTable = toolkit.createTable(authorSectionClient, SWT.BORDER | SWT.MULTI);
		GridDataFactory.fillDefaults().grab(true, true).span(2, 3).hint(100, 200).applyTo(authorTable);
		authorTable.setBounds(100, 100, 100, 500);

		final Button addAuthorBtn = toolkit.createButton(authorSectionClient, "Add...", SWT.PUSH); //$NON-NLS-1$
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(addAuthorBtn);

		addAuthorBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				handleAdd(addAuthorBtn.getShell());
			}
		});

		removeAuthor = toolkit.createButton(authorSectionClient, "Remove...", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(removeAuthor);

		removeAuthor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				for (TableItem item : authorTable.getSelection()) {

					selectedAuthors.remove(item.getText());
					item.dispose();

				}

			}
		});
		TacitFormComposite.createEmptyRow(toolkit, form.getBody());

//		layoutData = TacitFormComposite.createOutputSection(toolkit, form.getBody(), form.getMessageManager());
		corpusNameTxt = TacitFormComposite.createCorpusSection(toolkit, form.getBody(), form.getMessageManager());
		
		
		Button btnRun = TacitFormComposite.createRunButton(form.getBody(), toolkit);
		
		btnRun.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				runModule();
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			
		});
		
		
		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter());
		// form.setMessage("Invalid path", IMessageProvider.ERROR);
		this.setPartName("Latin Crawler");
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (LatinCrawlerImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Crawl";
			}

			public void run() {
				TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
				latinCrawler.initialize(corpusNameTxt.getText());
				runModule();

			};
		});
		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (LatinCrawlerImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_HELP_CO));
			}

			@Override
			public String getToolTipText() {
				return "Help";
			}

			@Override
			public void run() {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp("edu.usc.cssl.tacit.crawlers.latin.ui.latin");
			};
		};
		mgr.add(helpAction);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(helpAction, "edu.usc.cssl.tacit.crawlers.latin.ui.latin");
		PlatformUI.getWorkbench().getHelpSystem().setHelp(form, "edu.usc.cssl.tacit.crawlers.latin.ui.latin");
		form.getToolBarManager().update(true);
		toolkit.paintBordersFor(form.getBody());
	}

	public void processElementSelectionDialog(Shell shell) {

		ILabelProvider lp = new ArrayLabelProvider();
		dialog = new AuthorListDialog(shell, lp);
		dialog.setTitle("Select the Authors from the list");
		dialog.setMessage("Enter Author name to search");

	}

	private boolean canProceedCrawl() {
		TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
		boolean canProceed = true;
		form.getMessageManager().removeMessage("location");
		form.getMessageManager().removeMessage("author");
		if (selectedAuthors == null || selectedAuthors.size() < 1) {
			form.getMessageManager().addMessage("author", "Author details cannot be empty", null,
					IMessageProvider.ERROR);
			return false;
		}
		// Validate corpus name
		String corpusName = corpusNameTxt.getText();
		if (null == corpusName || corpusName.isEmpty()) {
			form.getMessageManager().addMessage("corpusName", "Provide corpus name", null, IMessageProvider.ERROR);
			return false;
		} else {
			String outputDir = ILatinCrawlerUIConstants.DEFAULT_CORPUS_LOCATION + File.separator + corpusName;
			if (new File(outputDir).exists()) {
				form.getMessageManager().addMessage("corpusName", "Corpus already exists", null,
						IMessageProvider.ERROR);
				return false;
			} else {
				form.getMessageManager().removeMessage("corpusName");
				return true;
			}
		}
	}

	private void handleAdd(Shell shell) {

		processElementSelectionDialog(shell);

		authors = new TreeSet<String>();
		Job listAuthors = new Job("Retrieving author list ...") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				authors.clear();
				try {
					if (authorListFromWeb == null) {
						authorListFromWeb = new LatinCrawler().getAuthorNames().keySet();
					}
					authors.addAll(authorListFromWeb);
					if (selectedAuthors != null)
						authors.removeAll(selectedAuthors);
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							dialog.refresh(authors.toArray());

						}
					});
				} catch (final IOException exception) {
					ConsoleView.printlInConsole(exception.toString());
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							ErrorDialog.openError(Display.getDefault().getActiveShell(), "Problem Occurred",
									"Please Check your connectivity to server",
									new Status(IStatus.ERROR, CommonUiActivator.PLUGIN_ID, "Network is not reachable"));
							
						}
					});
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;

			}
		};

		listAuthors.schedule();
		authors.add("Loading...");
		dialog.setElements(authors.toArray());
		dialog.setMultipleSelection(true);

		if (dialog.open() == Window.OK) {
			updateAuthorTable(dialog.getResult());
		}

	}

	private void updateAuthorTable(Object[] result) {
		if (selectedAuthors == null) {
			selectedAuthors = new ArrayList<String>();
		}

		for (Object object : result) {
			selectedAuthors.add((String) object);
		}
		Collections.sort(selectedAuthors);
		authorTable.removeAll();
		for (String itemName : selectedAuthors) {

			TableItem item = new TableItem(authorTable, 0);
			item.setText(itemName);
		}

	}

	String corpusName;
	private void runModule() {
		Job job = new Job("Crawling...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						corpusName = corpusNameTxt.getText();
						
					}
					
				});
				
				
				corpus = new Corpus(corpusName, CMDataType.LATIN_JSON);
				final DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, HH:mm:ss aaa");
				final Calendar cal = Calendar.getInstance();
				ConsoleView
						.writeInConsoleHeader("Latin Crawling started " + (dateFormat.format(cal.getTime())));
				Iterator<String> authorItr;
				int totalWork = 1;
				try {
					authorItr = selectedAuthors.iterator();
					System.out.println(selectedAuthors);
					totalWork = selectedAuthors.size();
					monitor.beginTask("TACIT started crawling...", totalWork);
					int totalFilesCreated = 0;
					
					while (authorItr.hasNext()) {
						if (monitor.isCanceled()) {
							monitor.subTask("Crawling is cancelled...");
							return Status.CANCEL_STATUS;
						}
						String author = authorItr.next();
						monitor.subTask("crawling " + author + "...");
						ConsoleView.printlInConsoleln("Crawling " + author);
						totalFilesCreated += latinCrawler.getBooksByAuthor(author,
								latinCrawler.getAuthorNames().get(author), monitor);
						monitor.worked(1);
						
						CorpusClass cc = new CorpusClass(author, ILatinCrawlerUIConstants.DEFAULT_CORPUS_LOCATION + File.separator+ corpusName+ File.separator+ author);
						cc.setParent(corpus);
						corpus.addClass(cc);
						
					}
					ConsoleView.printlInConsole("Total number of files downloaded : " + totalFilesCreated);

				} catch (final IOException exception) {
					ConsoleView.printlInConsole(exception.toString());
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							ErrorDialog.openError(Display.getDefault().getActiveShell(), "Problem Occurred",
									"Please Check your connectivity to server", new Status(IStatus.ERROR,
											CommonUiActivator.PLUGIN_ID, exception.getMessage()));

						}
					});

					return Status.CANCEL_STATUS;
				}
				ManageCorpora.saveCorpus(corpus);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		if (canProceedCrawl()) {
			job.schedule();
			job.addJobChangeListener(new JobChangeAdapter() {

				public void done(IJobChangeEvent event) {
					final DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, HH:mm:ss aaa");
					final Calendar cal = Calendar.getInstance();

					if (!event.getResult().isOK()) {
						TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling is stopped ",
								IStatus.INFO, form);
						ConsoleView.writeInConsoleHeader(
								"Error: <Terminated> Latin crawler" + (dateFormat.format(cal.getTime())));

					} else {
						TacitFormComposite.updateStatusMessage(getViewSite(), "Crawling completed", IStatus.OK,
								form);
						ConsoleView.writeInConsoleHeader(
								"Success: <Completed> Latin crawling  " + (dateFormat.format(cal.getTime())));

					}
				}
			});
		}
	}
	static class ArrayLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return (String) element;
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		form.setFocus();
	}
}