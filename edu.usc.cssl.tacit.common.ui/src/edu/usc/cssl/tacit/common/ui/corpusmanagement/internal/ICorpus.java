package edu.usc.cssl.tacit.common.ui.corpusmanagement.internal;

import java.util.List;

public interface ICorpus {
	public String getCorpusId();
	
	public String getDatatype();
	
	public List<ICorpusClass> getClasses();
}
