/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */

package edu.usc.cssl.nlputils.plugins.svmClassifier.process;

public class WordDetails {
	private int ID=0;
	private int docCount=0;
	
	public void setID(int ID){
		this.ID = ID;
	}
	public void incrementDocCount(){
		this.docCount = this.docCount + 1;
	}
	public int getID(){
		return this.ID;
	}
	public int getDocCount(){
		return this.docCount;
	}
}
