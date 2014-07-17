 
package edu.usc.pil.nlputils.application.parts;

import javax.inject.Inject;
import javax.annotation.PostConstruct;
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
		//TODO Your code here
	}
	
	
	
	
}