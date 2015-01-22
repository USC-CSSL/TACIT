/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */
package edu.usc.cssl.nlputils.application.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;

import edu.usc.cssl.nlputils.application.handlers.GlobalPresserSettings;



public class Welcome {

	Browser browser = null;
	
	@Inject
	private MDirtyable dirty;
	private Text txtAddress;

	@PostConstruct
	public void createComposite(Composite parent) {
		Shell currentShell = parent.getShell();
		currentShell.setMaximized(Boolean.TRUE);
		parent.setLayout(new GridLayout(4, false));
		
		Button btnBack = new Button(parent, SWT.NONE);
		btnBack.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				browser.back();
			}
		});
		btnBack.setText("<");
		
		Button btnForward = new Button(parent, SWT.NONE);
		btnForward.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				browser.forward();
			}
		});
		btnForward.setText(">");
		
		txtAddress = new Text(parent, SWT.BORDER);
		txtAddress.setText("https://github.com/neo-anderson/NLPUtilsWiki/wiki/What's-New!");
		txtAddress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnGo = new Button(parent, SWT.NONE);
		btnGo.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				browser.setUrl(txtAddress.getText());
			}
		});
		btnGo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				browser.setUrl(txtAddress.getText());
			}
		});
		currentShell.setDefaultButton(btnGo);
		btnGo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				browser.setUrl(txtAddress.getText());
			}
		});
		btnGo.setText("Go");
		browser = new Browser(parent, SWT.NONE);
		GridData gd_browser = new GridData(SWT.LEFT, SWT.CENTER, true, true, 4, 1);
		gd_browser.heightHint = 800;
		gd_browser.widthHint = 800;
		browser.setLayoutData(gd_browser);
		browser.setUrl("https://github.com/neo-anderson/NLPUtilsWiki/wiki/What's-New!");
		
	}
	@Focus
	public void setFocus() {
		//tableViewer.getTable().setFocus();
	}

	@Persist
	public void save() {
		dirty.setDirty(false);
	}
}