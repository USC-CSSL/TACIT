package edu.usc.cssl.nlputils.crawlers.latin.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.nlputils.common.ui.composite.from.NlputilsFormComposite;
import edu.usc.cssl.nlputils.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.nlputils.crawlers.latin.services.LatinCrawler;
import edu.usc.cssl.nlputils.crawlers.latin.ui.internal.ILatinCrawlerUIConstants;
import edu.usc.cssl.nlputils.crawlers.latin.ui.internal.LatinCrawlerImageRegistry;

public class LatinCrawlerView extends ViewPart implements
		ILatinCrawlerUIConstants {
	public static final String ID = "edu.usc.cssl.nlputils.crawlers.latin.ui.view1";
	private ScrolledForm form;
	private AuthorListDialog dialog;
	private SortedSet<String> authors;
	private Table authorTable;
	private List<String> selectedAuthors;
	private Set<String> authorListFromWeb;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(form.getForm());
		form.setText("Latin Crawler"); //$NON-NLS-1$
		GridLayoutFactory.fillDefaults().applyTo(form.getBody());
		
		Section section = toolkit.createSection(form.getBody(),
				Section.TITLE_BAR | Section.EXPANDED);

		GridDataFactory.fillDefaults().grab(true, false).span(3, 1)
				.applyTo(section);
		section.setExpanded(true);
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

		OutputLayoutData layoutData = NlputilsFormComposite.createOutputSection(
				toolkit, form.getBody(),form.getMessageManager());
		Composite sectionClient = layoutData.getSectionClient();
		NlputilsFormComposite.createEmptyRow(toolkit, sectionClient);

		Section authorSection = toolkit.createSection(form.getBody(),
				Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);
		
		GridDataFactory.fillDefaults().span(3, 1).applyTo(authorSection);
		authorSection.setExpanded(true);
		authorSection.setText("Author Details"); //$NON-NLS-1$
		authorSection.setDescription("Add list of author needs to be crawled");

		ScrolledComposite authorsc = new ScrolledComposite(authorSection,
				SWT.H_SCROLL | SWT.V_SCROLL);
		authorsc.setExpandHorizontal(true);
		authorsc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
				.applyTo(authorsc);

		Composite authorSectionClient = toolkit.createComposite(authorSection);
		authorsc.setContent(authorSectionClient);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(authorsc);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
				.applyTo(authorSectionClient);
		authorSection.setClient(authorSectionClient);

		Label dummy1 = toolkit.createLabel(authorSectionClient, "", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 0)
				.applyTo(dummy1);

		authorTable = toolkit.createTable(authorSectionClient, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).span(2, 3)
				.hint(100, 200).applyTo(authorTable);
		authorTable.setBounds(100, 100, 100, 500);

		final Button b = toolkit
				.createButton(authorSectionClient, "Add...", SWT.PUSH); //$NON-NLS-1$
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(b);

		b.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					handleAdd(b.getShell());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		Button removeAuthor = toolkit.createButton(authorSectionClient,
				"Remove...", SWT.PUSH); //$NON-NLS-1$
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(removeAuthor);
       
		removeAuthor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				for (int itemIndex : authorTable.getSelectionIndices()) {
					
					selectedAuthors.remove(authorTable.getItem(itemIndex).getText());
					authorTable.remove(itemIndex);
					
				}
				
			}
		});
		
		
		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter());
		// form.setMessage("Invalid path", IMessageProvider.ERROR);
		this.setPartName("Latin Crawler");
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (LatinCrawlerImageRegistry.getImageIconFactory()
						.getImageDescriptor(IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Crawl";
			}

			public void run() {

				Job job = new Job("Crawling...") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("NLPUtils started crawling...", 100);
						try {
							if (monitor.isCanceled()) {
								monitor.done();
							}
							Thread.sleep(100000);

						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						int i = 0;
						while (i < 1000000000) {
							i++;
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
				return (LatinCrawlerImageRegistry.getImageIconFactory()
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

	public void processElementSelectionDialog(Shell shell) {

		ILabelProvider lp = new ArrayLabelProvider();
		dialog = new AuthorListDialog(shell, lp);
		dialog.setTitle("Select the Authors from the list");
		dialog.setMessage("Enter Author name to search");

	}

	private void handleAdd(Shell shell) throws Exception {

		processElementSelectionDialog(shell);

		authors = new TreeSet<String>();
		Job listAuthors = new Job("tt") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				authors.clear();
				try {
					if (authorListFromWeb == null) {
						authorListFromWeb = new LatinCrawler().getAuthorNames()
								.keySet();
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
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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