/**
 * @author Niki Parmar {nikijitp@usc.edu}
 */ 
package edu.usc.cssl.nlputils.plugins.latincrawler.parts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Combo;

import edu.usc.cssl.nlputils.plugins.latincrawler.process.AvailableRecords;
import edu.usc.cssl.nlputils.plugins.latincrawler.process.LatinCrawler;
import edu.usc.cssl.nlputils.utilities.Log;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.osgi.framework.FrameworkUtil;

public class LatinCrawlerSettings {
	private Text txtOutput;
	List<String> selectedAuthors;
	Combo cmbAuth;
	
	@Inject
	public LatinCrawlerSettings() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent){
		final Shell shell = parent.getShell();
		appendLog("Loading Latin Library...");

		appendLog("Loading Authors...");
		final LatinCrawler crawler = new LatinCrawler();
		try {
			crawler.getAuthorsList("http://www.thelatinlibrary.com/");
		}catch(Exception e){
			e.printStackTrace();
		}
		appendLog("Loading Complete.");
		
		/* Selected list of authors to crawl */
		selectedAuthors = new ArrayList<String>();
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		Label header = new Label(composite, SWT.NONE);
		header.setImage(ImageDescriptor.createFromURL(
				FileLocator.find(FrameworkUtil.getBundle(this.getClass()),
						new Path("plugin_icon/icon.png"), null)).createImage());
		for (int i=1; i<=5; i++){
			new Label(composite, SWT.NONE);
		}
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite.widthHint = 431;
		gd_composite.heightHint = 477;
		composite.setLayoutData(gd_composite);
		
		Label lblOutput = new Label(composite, SWT.NONE);
		lblOutput.setText("Output Path");
		
		txtOutput = new Text(composite, SWT.BORDER);
		GridData gd_txtOutputDir = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtOutputDir.widthHint = 244;
		txtOutput.setLayoutData(gd_txtOutputDir);
		
		Button button = new Button(composite, SWT.NONE);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog od = new DirectoryDialog(shell);
				od.open();
				String oDirectory = od.getFilterPath();
				txtOutput.setText(oDirectory);
			}
		});
		button.setText("...");
		
		appendLog("Selected Authors: ");
		Label lblAuthor = new Label(composite, SWT.NONE);
		lblAuthor.setText("Select Author");
		cmbAuth = new Combo(composite, SWT.NONE);
		cmbAuth.setEnabled(true);
		cmbAuth.add("All");
		System.out.println(crawler.authorNames.size());
		SortedSet<String> authors = new TreeSet<String>(crawler.authorNames.keySet());
		for(String auth: authors){
			cmbAuth.add(auth);
		}
		cmbAuth.setText("All");
		GridData gd_cmbAuth = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		cmbAuth.setLayoutData(gd_cmbAuth);
	
		Button bnSelAuth = new Button(composite, SWT.NONE);
		bnSelAuth.setText("Add Author");
		bnSelAuth.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				int index = cmbAuth.getSelectionIndex();
				String selection = cmbAuth.getItem(index);
				if(selection.equals("All")){
					selectedAuthors.clear();
					cmbAuth.setEnabled(false);
				}else{
					selectedAuthors.add(selection);
					appendLog("Author " + selection);
					cmbAuth.remove(index);
				}
			}
		});
		
		Button btnExtract = new Button(composite, SWT.NONE);
		btnExtract.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				
				
				// Injecting the context into Latincrawler object so that the appendLog function can modify the Context Parameter consoleMessage
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(crawler,iEclipseContext);
				crawler.initialize(txtOutput.getText());
				
			
				// Creating a new Job to do crawling so that the UI will not freeze
				Job job = new Job("Crawler Job"){
					protected IStatus run(IProgressMonitor monitor){ 
					
						appendLog("PROCESSING...(Latin Crawler)");
						
						try {
						
							long startTime = System.currentTimeMillis();
							if(selectedAuthors.size() <= 0){
								appendLog("Running Latin Crawler for all authors ");
								crawler.crawl();
							}
							else{
								for(String auth: selectedAuthors){
									crawler.getSingleAuthor(auth, crawler.authorNames.get(auth));
								}
							}
							appendLog("Extraction completed in "+(System.currentTimeMillis()-startTime)/(float)1000+" seconds");
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						
						appendLog("DONE (Latin Crawler)");
						
					return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				job.schedule();
				
				
			}
		});
		btnExtract.setText("Extract");
		shell.setDefaultButton(btnExtract);
		
	}
	
	@Inject IEclipseContext context;
	private void appendLog(String message){
		Log.append(context,message);
	}
}