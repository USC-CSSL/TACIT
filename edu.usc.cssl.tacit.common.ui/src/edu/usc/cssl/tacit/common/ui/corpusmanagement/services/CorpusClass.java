package edu.usc.cssl.tacit.common.ui.corpusmanagement.services;

import org.eclipse.jface.viewers.TreeViewer;

import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusClass;

public class CorpusClass implements ICorpusClass{
	String className;
	String classPath;
	TreeViewer viewer;
	
	public CorpusClass(String className, String classPath) {
		this.className = className;
		this.classPath = classPath;
	}
	
	public CorpusClass(String className, String classPath, TreeViewer viewer) {
		this.className = className;
		this.classPath = classPath;
		this.viewer = viewer;
	}
	
	@Override
	public String getClassName() {
		return this.className;
	}

	@Override
	public String getClassPath() {
		return this.classPath;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public void setClassPath(String classPath) {
		this.classPath = classPath;
	}	
	
	public void setViewer(TreeViewer viewer) {
		this.viewer = viewer;
	}
	
	public TreeViewer getViewer() {
		return this.viewer;
	}
}
