/**
 * @author Niki Parmar {nikijitp@usc.edu}
 */ 
package edu.usc.cssl.nlputils.plugins.latincrawler.parts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
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

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

public class LatinCrawlerSettings {
	
	@Inject
	public LatinCrawlerSettings() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		appendLog("Loading Latin Library...");
		/*
		String[] booksArray = null;

		try {
			booksArray = AvailableRecords.getAllBooks(authors);
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		
		*/
		
		appendLog("Loading Complete.");
	
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
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
		
		Button btnExtract = new Button(composite, SWT.NONE);
		btnExtract.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {

				
				LatinCrawler crawler = new LatinCrawler();
				// Injecting the context into Senatecrawler object so that the appendLog function can modify the Context Parameter consoleMessage
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(crawler,iEclipseContext);

			
				try {
					
					long startTime = System.currentTimeMillis();
					crawler.initialize(txtOutput.getText());
				
					appendLog("Extraction completed in "+(System.currentTimeMillis()-startTime)/(float)1000+" seconds");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		btnExtract.setText("Extract");
		
	}
	
	@Inject IEclipseContext context;
	private Text txtOutput;
	private void appendLog(String message){
		IEclipseContext parent = context.getParent();
		//System.out.println(parent.get("consoleMessage"));
		String currentMessage = (String) parent.get("consoleMessage"); 
		if (currentMessage==null)
			parent.set("consoleMessage", message);
		else {
			if (currentMessage.equals(message)) {
				// Set the param to null before writing the message if it is the same as the previous message. 
				// Else, the change handler will not be called.
				parent.set("consoleMessage", null);
				parent.set("consoleMessage", message);
			}
			else
				parent.set("consoleMessage", message);
		}
	}
}