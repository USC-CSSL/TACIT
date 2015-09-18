package edu.usc.cssl.tacit.common.ui.corpusmanagement.internal;

import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.Corpus;


public interface ICorpusClass {
	public String getClassName();
	
	public String getClassPath();
	
	public String getTacitLocation();
	
	public Corpus getParent();
}
