package edu.usc.cssl.tacit.common.ui.corpusmanagement.services;

import java.util.ArrayList;
import java.util.List;

import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusClass;

public class Corpus implements ICorpus {
	String corpusId;
	String dataType;
	List<ICorpusClass> classes;
	
	public Corpus(String corpusId, String dataType) {
		this.corpusId = corpusId;
		this.dataType = dataType;
		this.classes = new ArrayList<ICorpusClass>();
	}
	
	@Override
	public String getCorpusId() {
		return this.corpusId;
	}

	@Override
	public String getDatatype() {
		return this.dataType;
	}

	@Override
	public List<ICorpusClass> getClasses() {
		return this.classes;
	}
	
	public void setClasses(List<ICorpusClass> classes) {
		if(null == classes) return;
		this.classes = classes;
	}	
	
	public void addClass(ICorpusClass c) {
		if(null == c) return;
		this.classes.add(c);
	}
	
	public void removeClass(ICorpusClass c) {
		if(this.classes.contains(c)) 
			this.classes.remove(c);
	}
}
