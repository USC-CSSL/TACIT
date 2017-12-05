package edu.usc.cssl.tacit.crawlers.govtrack.ui.preferencepage;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.crawlers.govtrack.ui.internal.IGovTrackCrawlerViewConstants;
import edu.usc.cssl.tacit.crawlers.govtrack.ui.internal.GovTrackCrawlerViewImageRegistry;

public class ProPublicaConfiguration extends PreferencePage implements IWorkbenchPreferencePage,IProPublicaConstants{
	
	private Text proPublicaApiKey;
	
	@Override
	protected Control createContents(Composite parent) {
		Composite sectionClient = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(6).equalWidth(false).applyTo(sectionClient);

		proPublicaApiKey = createTextFields(sectionClient, true, "ProPublica Key:");
		String value = getPreferenceStore().getString(PROPUBLICA_API_KEY);
		if(value!=null && !value.equals("")){
			proPublicaApiKey.setText(value);
		}
		
		Button help = new Button(sectionClient, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(help);
		help.setImage(GovTrackCrawlerViewImageRegistry.getImageIconFactory().getImage(IGovTrackCrawlerViewConstants.IMAGE_HELP_CO));
		help.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp("edu.usc.cssl.tacit.crawlers.govtrack.ui.govtrack");
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});

		return sectionClient;
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(CommonUiActivator.getDefault().getPreferenceStore());
		
	}
	
	public boolean performOk() {
		try {

			store(PROPUBLICA_API_KEY, proPublicaApiKey.getText());
			System.out.println(getPreferenceStore().getString(PROPUBLICA_API_KEY));
		} catch (Exception exception) {

			ErrorDialog.openError(Display.getDefault().getActiveShell(), "Problem Occurred", exception.getMessage(),
					new Status(IStatus.ERROR, CommonUiActivator.PLUGIN_ID, exception.getMessage()));

		}

		return super.performOk();
	}

	private Text createTextFields(Composite sectionClient, boolean editable, String lbl) {
		Label locationLbl = new Label(sectionClient, SWT.NONE);
		locationLbl.setText(lbl);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(locationLbl);

		final Text outputLocationTxt = new Text(sectionClient, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(4, 0).applyTo(outputLocationTxt);
		outputLocationTxt.setEditable(editable);
		return outputLocationTxt;
	}

	private void store(String name, String value) {
		getPreferenceStore().setValue(name, value);

	}

	private String load(String name) {
		return getPreferenceStore().getString(name);

	}
	

}
