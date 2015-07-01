package edu.usc.cssl.tacit.common.ui.internal;

import java.util.ArrayList;
import java.util.List;

public class TreeParent {

	List<String> children;
	List<TreeParent> folder;
	String name;

	public TreeParent(String name) {
		this.name = name;
		children = new ArrayList<String>();
		folder = new ArrayList<TreeParent>();
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

	public List<TreeParent> getFolder() {
		return folder;
	}

	public List<String> getFiles() {
		return children;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name;
	}
	
}
