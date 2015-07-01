package edu.usc.cssl.tacit.common.ui.outputdata;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class OutputLayoutData {

	private Composite sectionClient;
	private Text outputLabelText;

	public Text getOutputLabel() {
		return outputLabelText;
	}

	public void setOutputLabel(Text outputLabelText) {
		this.outputLabelText = (outputLabelText);
	}

	/**
	 * @return the sectionClient
	 */
	public Composite getSectionClient() {
		return sectionClient;
	}

	/**
	 * @param sectionClient
	 *            the sectionClient to set
	 */
	public void setSectionClient(Composite sectionClient) {
		this.sectionClient = sectionClient;
	}

}
