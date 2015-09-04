package edu.usc.cssl.tacit.common.ui.corpusmanagement.internal;

import java.util.List;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.DataType;

public interface ICorpus {
	public String getCorpusId();
	
	public DataType getDatatype();
	
	public List<ICorpusClass> getClasses();
}
