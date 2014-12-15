 
package edu.usc.cssl.nlputils.plugins.supremeCrawler.parts;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import edu.usc.cssl.nlputils.plugins.supremeCrawler.process.SupremeCrawler;

public class SupremeGUI {
	private Text txtOutput;
	@Inject
	public SupremeGUI() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		parent.setLayout(new GridLayout(9, false));
		
		Label lblSortBy = new Label(parent, SWT.NONE);
		lblSortBy.setText("Filter Type");
		new Label(parent, SWT.NONE);;
		
		Button btnCases = new Button(parent, SWT.RADIO);
		btnCases.setSelection(true);
		btnCases.setText("Term");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Button btnIssues = new Button(parent, SWT.RADIO);
		btnIssues.setText("Issues");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Label lblFilter = new Label(parent, SWT.NONE);
		lblFilter.setText("Filter Range");
		new Label(parent, SWT.NONE);
		
		final Combo combo=new Combo(parent, SWT.NONE);
		GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, false, false, 6, 1);
		gd_combo.widthHint = 177;
		combo.setLayoutData(gd_combo);
		combo.setItems(SupremeCrawler.filters("cases"));
		combo.select(0);
		new Label(parent, SWT.NONE);
		
		Label lblOutputDirectory = new Label(parent, SWT.NONE);
		lblOutputDirectory.setText("Output Directory");
		new Label(parent, SWT.NONE);
		
		txtOutput = new Text(parent, SWT.BORDER);
		txtOutput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 6, 1));
		
		Button button = new Button(parent, SWT.NONE);
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
		
		Button btnTruncate = new Button(parent, SWT.CHECK);
		btnTruncate.setText("Truncate MP3 (1MB)");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		btnCases.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Term is "+btnCases.getSelection());
				if(btnCases.getSelection())
					combo.setItems(SupremeCrawler.filters("cases"));
				else
					combo.setItems(SupremeCrawler.filters("issues"));
				combo.select(0);
			}
		});
		
		
	}
	
	
	
	
}