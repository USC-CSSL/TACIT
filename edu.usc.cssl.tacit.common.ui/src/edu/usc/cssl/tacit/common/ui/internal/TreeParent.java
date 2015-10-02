package edu.usc.cssl.tacit.common.ui.internal;

import java.util.ArrayList;
import java.util.List;

import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.Corpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;

public class TreeParent {

	List<Object> children;
	List<Object> folder;
	String name;
	private Corpus corpus;
	private CorpusClass corpusClass;
	public Corpus getCorpus(){
		return this.corpus;
	}
	public CorpusClass getCorpusClass() {
		return corpusClass;
	}
	
	public TreeParent(Corpus corpus){
		this.corpus = corpus;
		this.name = corpus.getCorpusName();
		folder = new ArrayList<Object>();
		children = new ArrayList<Object>();
	}
	
	public TreeParent(ICorpusClass corpusClass){
		this.name = corpusClass.getClassName();
		this.corpusClass = (CorpusClass) corpusClass;
		children = new ArrayList<Object>();
		folder = new ArrayList<Object>();
	}


	public TreeParent(String name) {
		this.name = name;
		children = new ArrayList<Object>();
		folder = new ArrayList<Object>();
	}
	
	public String getName() {
		return name;
	}

	public void addChildren(String child) {
		children.add(child);

	}

	public void addChildren(TreeParent child) {
		folder.add(child);

	}

	public List<Object> getFolder() {
		return folder;
	}

	public List<Object> getFiles() {
		return children;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name;
	}
	
}
