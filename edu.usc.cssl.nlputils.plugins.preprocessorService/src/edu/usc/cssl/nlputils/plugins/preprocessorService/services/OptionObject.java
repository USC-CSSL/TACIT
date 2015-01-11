/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */
package edu.usc.cssl.nlputils.plugins.preprocessorService.services;

public class OptionObject {
	private String delimiters;
	private String stopFile;
	private boolean doLowercase;
	private boolean doStemming;
	private String stemLang;
	private boolean cleanUp;
	
	public OptionObject(String delimiters, String stopFile, boolean doLowercase, boolean doStemming, String stemLang, boolean cleanUp){
		this.delimiters = delimiters;
		this.stopFile = stopFile;
		this.doLowercase = doLowercase;
		this.doStemming = doStemming;
		this.stemLang = stemLang;
		this.cleanUp = cleanUp;
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
	
	public boolean doCleanUp(){
		return cleanUp;
	}
	
}
