package edu.usc.cssl.tacit.common.ui.corpusmanagement.services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.jface.viewers.TreeViewer;

import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusClass;

public class Corpus implements ICorpus {
	private String corpusName;
	private String corpusId; // unique id for corpus
	private CMDataType dataType;
	private List<ICorpusClass> classes;
	private TreeViewer viewer;
	private Long noOfFiles;
	private Long noOfCases;
	
	

	public Long getNoOfCases() {
		return noOfCases;
	}

	public void setNoOfCases(Long noOfCases) {
		this.noOfCases = noOfCases;
	}

	public Long getNoOfFiles() {
		return noOfFiles;
	}

	public void setNoOfFiles(Long long1) {
		this.noOfFiles = long1;
	}

	public Corpus(String corpusName, CMDataType dataType) {
		this.corpusName = corpusName;
		this.dataType = dataType;
		this.classes = new ArrayList<ICorpusClass>();
		setCorpusId(UUID.randomUUID().toString());
	}

	public Corpus(String corpusName, CMDataType dataType, TreeViewer viewer) {
		this.corpusName = corpusName;
		this.dataType = dataType;
		this.classes = new ArrayList<ICorpusClass>();
		this.viewer = viewer;
		setCorpusId(UUID.randomUUID().toString());
	}

	public Corpus() {
		this.classes = new ArrayList<ICorpusClass>();
	}

	@Override
	public String getCorpusName() {
		return this.corpusName;
	}

	@Override
	public CMDataType getDatatype() {
		return this.dataType;
	}

	@Override
	public List<ICorpusClass> getClasses() {
		return this.classes;
	}

	@Override
	public String getCorpusId() {
		return this.corpusId;
	}

	public void setCorpusName(String corpusName) {
		if (null == corpusName)
			return;
		this.corpusName = corpusName;
	}

	public void setCorpusId(String corpusId) {
		if (null == corpusId)
			return;
		this.corpusId = corpusId;
	}

	public void setClasses(List<ICorpusClass> classes) {
		if (null == classes)
			return;
		this.classes = classes;
	}

	public void addClass(ICorpusClass c) {
		if (null == c)
			return;
		((CorpusClass) c).setParent(this);
		this.classes.add(c);
	}

	public void removeClass(ICorpusClass c) {
		if (this.classes.contains(c))
			this.classes.remove(c);
	}

	public void setDataType(CMDataType dataType) {
		this.dataType = dataType;
	}

	public void setViewer(TreeViewer viewer) {
		this.viewer = viewer;
	}

	public TreeViewer getViewer() {
		return this.viewer;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return corpusName;
	}

}
