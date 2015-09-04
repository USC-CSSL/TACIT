package edu.usc.cssl.tacit.common.ui.corpusmanagement.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;

import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusClass;

public class Corpus implements ICorpus {
	String corpusId;
	DataType dataType;
	List<ICorpusClass> classes;
	TreeViewer viewer;
	
	public Corpus(String corpusId, DataType dataType) {
		this.corpusId = corpusId;
		this.dataType = dataType;
		this.classes = new ArrayList<ICorpusClass>();
	}
	
	public Corpus(String corpusId, DataType dataType, TreeViewer viewer) {
		this.corpusId = corpusId;
		this.dataType = dataType;
		this.classes = new ArrayList<ICorpusClass>();
		this.viewer = viewer;
	}	
	
	@Override
	public String getCorpusId() {
		return this.corpusId;
	}

	@Override
	public DataType getDatatype() {
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
	
	public void setCorpusId(String corpusId) {
		this.corpusId = corpusId;
	}
	
	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public void setViewer(TreeViewer viewer) {
		this.viewer = viewer;
	}
	
	public TreeViewer getViewer() {
		return this.viewer;
	}	
}
