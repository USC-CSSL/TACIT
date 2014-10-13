/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */ 
package edu.usc.cssl.nlputils.application.parts;

import javax.inject.Inject;
import javax.inject.Named;
import javax.annotation.PostConstruct;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;

public class Console {
	private Text txtConsoleOutput;
	@Inject
	public Console() {
		//TODO Your code here
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
		txtConsoleOutput = new Text(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		//txtConsoleOutput.setText("Helloon!");
	}
	
	@Inject
	public void setLog(@Optional @Named("consoleMessage")String message){
		System.out.println("Appending to console - "+message);
		if (message!=null){
			txtConsoleOutput.append(message+"\n");
		}
	}
	
	
	
	
}