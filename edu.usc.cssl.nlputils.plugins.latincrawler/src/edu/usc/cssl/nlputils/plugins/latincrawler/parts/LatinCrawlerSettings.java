/**
 * @author Niki Parmar {nikijitp@usc.edu}
 */ 
package edu.usc.cssl.nlputils.plugins.latincrawler.parts;

import java.io.IOException;

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

public class LatinCrawlerSettings {
	
	@Inject
	public LatinCrawlerSettings() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		appendLog("Loading Latin Library...");
		String[] authors = null;

		try {
			 authors = AvailableRecords.getAllAuthors();
		} catch (IOException e) {
			e.printStackTrace();
		}

		appendLog("Loading Authors...");
		final String[] allAuthors = authors;
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
		composite.setSize(588, 300);
		composite.setLocation(0, 0);
		
		
		
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
					crawler.initialize( allAuthors,  txtOutput.getText());
				
					appendLog("Extraction completed in "+(System.currentTimeMillis()-startTime)/(float)1000+" seconds");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		btnExtract.setBounds(10, 263, 75, 25);
		btnExtract.setText("Extract");
		
		Label lblOutput = new Label(composite, SWT.NONE);
		lblOutput.setBounds(10, 231, 80, 15);
		lblOutput.setText("Output Path");
		
		txtOutput = new Text(composite, SWT.BORDER);
		txtOutput.setBounds(104, 225, 258, 21);
		
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
		button.setBounds(358, 224, 40, 25);
		button.setText("...");
	
		
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