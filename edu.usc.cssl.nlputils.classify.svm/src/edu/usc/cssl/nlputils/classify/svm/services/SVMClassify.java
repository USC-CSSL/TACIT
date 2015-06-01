package edu.usc.cssl.nlputils.classify.svm.services;

import java.util.Calendar;

public class SVMClassify {
	private String dateString;
	private String intermediatePath;
	
	public SVMClassify(String class1Name, String class2Name, String outputFolder){
		Calendar cal = Calendar.getInstance();
		this.dateString = ""+(cal.get(Calendar.MONTH)+1)+"-"+cal.get(Calendar.DATE)+"-"+cal.get(Calendar.YEAR);
		this.intermediatePath = outputFolder+System.getProperty("file.separator")+class1Name+"_"+class2Name+"_"+dateString+"-"+System.currentTimeMillis();
	}
}
