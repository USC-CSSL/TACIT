package edu.usc.cssl.tacit.common.ui.corpusmanagement;

import java.util.List;

import edu.usc.cssl.tacit.common.queryprocess.Filter;

public interface IWizardData {
	public void getData(List<Filter> a);
	public void getPath(String path);
	public void getDivision(boolean seperate);
}
