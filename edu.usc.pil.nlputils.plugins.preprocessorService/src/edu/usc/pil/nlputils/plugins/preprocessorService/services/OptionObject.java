/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */
package edu.usc.pil.nlputils.plugins.preprocessorService.services;

public class OptionObject {
	private String delimiters;
	private String stopFile;
	private boolean doLowercase;
	private boolean doStemming;
	private String stemLang;
	
	public OptionObject(String delimiters, String stopFile, boolean doLowercase, boolean doStemming, String stemLang){
		this.delimiters = delimiters;
		this.stopFile = stopFile;
		this.doLowercase = doLowercase;
		this.doStemming = doStemming;
		this.stemLang = stemLang;
	}

	public String getDelimiters() {
		return delimiters;
	}

	public String getStopFile() {
		return stopFile;
	}

	public boolean isDoLowercase() {
		return doLowercase;
	}

	public boolean isDoStemming() {
		return doStemming;
	}

	public String getStemLang() {
		return stemLang;
	}
	
	
}
