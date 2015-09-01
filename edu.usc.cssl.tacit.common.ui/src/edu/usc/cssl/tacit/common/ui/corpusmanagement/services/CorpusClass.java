package edu.usc.cssl.tacit.common.ui.corpusmanagement.services;

import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusClass;

public class CorpusClass implements ICorpusClass{
	String className;
	String classPath;
	
	public CorpusClass(String className, String classPath) {
		this.className = className;
		this.classPath = classPath;
	}
	
	@Override
	public String getClassName() {
		return this.className;
	}

	@Override
	public String getClassPath() {
		return this.classPath;
	}

}
