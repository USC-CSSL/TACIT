/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */
package edu.usc.cssl.nlputils.application.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import edu.usc.cssl.nlputils.application.utility.NlpUtilsProductConstant;

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
        // Instantiate Image registry
		ImageRegistry ir = new ImageRegistry();
		ir.put(NlpUtilsProductConstant.IMAGE_BACK, ImageDescriptor
				.createFromFile(Welcome.class, "/icons/backward_nav.gif"));
		ir.put(NlpUtilsProductConstant.IMAGE_FRONT, ImageDescriptor
				.createFromFile(Welcome.class, "/icons/step_current.gif"));

		/*
		 * Create toolbar to group items
		 */
		ToolBar toolbar = new ToolBar(parent, SWT.FLAT);
		ToolItem backward = new ToolItem(toolbar, SWT.NONE);
		backward.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browser.back();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				browser.back();

			}
		});
		backward.setImage(ir.get(NlpUtilsProductConstant.IMAGE_BACK));
	
		ToolItem forward = new ToolItem(toolbar, SWT.NONE);
		forward.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browser.forward();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				browser.forward();

			}
		});
		forward.setImage(ir.get(NlpUtilsProductConstant.IMAGE_FRONT));
	
		txtAddress = new Text(parent, SWT.BORDER);
		txtAddress.setText(NlpUtilsProductConstant.INIT_URL);
		txtAddress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

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
		GridData gd_browser = new GridData(SWT.LEFT, SWT.CENTER, true, true, 4,
				1);
		gd_browser.heightHint = 800;
		gd_browser.widthHint = 800;
		browser.setLayoutData(gd_browser);
		browser.setUrl(NlpUtilsProductConstant.INIT_URL);

	}

	@Focus
	public void setFocus() {
		// tableViewer.getTable().setFocus();
	}

	@Persist
	public void save() {
		dirty.setDirty(false);
	}
}