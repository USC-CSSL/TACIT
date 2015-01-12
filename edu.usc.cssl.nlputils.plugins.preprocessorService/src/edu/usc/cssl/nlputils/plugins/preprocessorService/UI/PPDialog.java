/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */
package edu.usc.cssl.nlputils.plugins.preprocessorService.UI;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;

import edu.usc.cssl.nlputils.plugins.preprocessorService.services.OptionObject;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

public class PPDialog extends Dialog {
	private Text txtStopFile;
	private Text txtDelimiters;
	private Button btnConvertToLowercase;
	private Button btnStemming;
	private Combo cmbStemLang;
	private OptionObject oo;
	public boolean doPP = false;
	private Button btnCleanUp;
	
	
  public PPDialog(Shell parentShell) {
    super(parentShell);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
	  final Shell shell = parent.getShell();
    Composite container = (Composite) super.createDialogArea(parent);
    GridLayout gridLayout = (GridLayout) container.getLayout();
    gridLayout.numColumns = 5;
    
    Label lblStopFilePath = new Label(container, SWT.NONE);
    lblStopFilePath.setText("Stop Words File Path");
    new Label(container, SWT.NONE);
    new Label(container, SWT.NONE);
    
    
    txtStopFile = new Text(container, SWT.BORDER);
    txtStopFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    
    Button button = new Button(container, SWT.NONE);
    button.addMouseListener(new MouseAdapter() {
    	@Override
    	public void mouseUp(MouseEvent e) {
    		txtStopFile.setText("");
			FileDialog fd = new FileDialog(shell,SWT.OPEN);
			fd.open();
			String oFile = fd.getFileName();
			String dir = fd.getFilterPath();
			txtStopFile.setText(dir+System.getProperty("file.separator")+oFile);
		
    	}
    });
    button.setText("...");
    
    Label lblDelimiters = new Label(container, SWT.NONE);
    lblDelimiters.setText("Delimiters");
    new Label(container, SWT.NONE);
    new Label(container, SWT.NONE);
    
    txtDelimiters = new Text(container, SWT.BORDER);
    txtDelimiters.setText(" .,;'\\\"!-()[]{}:?/@");
    txtDelimiters.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    new Label(container, SWT.NONE);
        
    btnConvertToLowercase = new Button(container, SWT.CHECK);
    btnConvertToLowercase.setText("Convert to Lowercase");
    new Label(container, SWT.NONE);
    new Label(container, SWT.NONE);
    new Label(container, SWT.NONE);
    new Label(container, SWT.NONE);
    
    btnStemming = new Button(container, SWT.CHECK);
    btnStemming.addSelectionListener(new SelectionAdapter() {
    	@Override
    	public void widgetSelected(SelectionEvent e) {
    		if (btnStemming.getSelection())
				cmbStemLang.setEnabled(true);
			else
				cmbStemLang.setEnabled(false);
    	}
    });
    btnStemming.setText("Stemming");
    new Label(container, SWT.NONE);
    new Label(container, SWT.NONE);
    
    
    cmbStemLang = new Combo(container, SWT.NONE);
    cmbStemLang.setItems(new String[] {"Auto Detect Language", "EN", "DE", "FR", "IT", "DA", "NL", "FI", "HU", "NO", "TR"});
    cmbStemLang.setEnabled(false);
    cmbStemLang.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    cmbStemLang.setText("Auto Detect Language");
    new Label(container, SWT.NONE);
    
    btnCleanUp = new Button(container, SWT.CHECK);
    btnCleanUp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
    btnCleanUp.setText("Clean Up Preprocessed Files when Done");
    new Label(container, SWT.NONE);
    
    if (oo!=null){
        //System.out.println(oo.getDelimiters());
    	System.out.println(oo.getStopFile());
        txtStopFile.setText(oo.getStopFile());
    	System.out.println(oo.getDelimiters());
        txtDelimiters.setText(oo.getDelimiters());
    	btnConvertToLowercase.setSelection(oo.isDoLowercase());
    	btnStemming.setSelection(oo.isDoStemming());
        cmbStemLang.setText(oo.getStemLang());
        cmbStemLang.setEnabled(true);
        btnCleanUp.setSelection(oo.doCleanUp());
    }

    return container;
  }
  
  // overriding this methods allows you to set the
  // title of the custom dialog
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Preprocessing Options");
  }

  @Override
  protected void okPressed() {
	    saveInput();
	    super.okPressed();
	  }


  private void saveInput() {
	  oo = new OptionObject(txtDelimiters.getText(), txtStopFile.getText(), btnConvertToLowercase.getSelection(), btnStemming.getSelection(), cmbStemLang.getText(), btnCleanUp.getSelection());
	  doPP = true;
  }
  
  public void setOptions(OptionObject oo){
	  System.out.println(oo.getDelimiters());
	  txtDelimiters.setText(oo.getDelimiters());
	  txtStopFile.setText(oo.getStopFile());
	  btnConvertToLowercase.setEnabled(oo.isDoLowercase());
	  btnStemming.setEnabled(oo.isDoStemming());
	  cmbStemLang.setText(oo.getStemLang());
  }
  
  public OptionObject getOptions(){
	  return oo;
  }
  
  @Override
  protected Point getInitialSize() {
    return new Point(450, 225);
  }

} 