package edu.usc.cssl.tacit.common.ui.corpusmanagement.services;

import org.eclipse.jface.viewers.TreeViewer;

import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusClass;

public class CorpusClass implements ICorpusClass{
	private String className;
	private String classPath;
	private TreeViewer viewer;
	private String tacitLocation;
	private String parentId;
	
	public CorpusClass(String className, String classPath) {
		this.className = className;
		this.classPath = classPath;
	}
	
	public CorpusClass(String className, String classPath, TreeViewer viewer) {
		this.className = className;
		this.classPath = classPath;
		this.viewer = viewer;
	}
	
	public CorpusClass() {
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
	
	public void setTacitLocation(String location) {
		this.tacitLocation = location;
	}

	@Override
	public String getTacitLocation() {
		return this.tacitLocation;
	}

	@Override
	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	
	
}
