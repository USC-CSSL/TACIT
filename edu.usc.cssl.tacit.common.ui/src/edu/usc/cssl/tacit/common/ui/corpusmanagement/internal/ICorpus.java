package edu.usc.cssl.tacit.common.ui.corpusmanagement.internal;

import java.util.List;

import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CMDataType;

public interface ICorpus {
	public String getCorpusName();
	
	public Long getNoOfFiles();
	
	public CMDataType getDatatype();
	
	public List<ICorpusClass> getClasses();
	
	public String getCorpusId();
}
