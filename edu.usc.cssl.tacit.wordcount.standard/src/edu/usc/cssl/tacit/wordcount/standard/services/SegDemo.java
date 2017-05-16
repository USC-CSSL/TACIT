package edu.usc.cssl.tacit.wordcount.standard.services;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;


/** This is a very simple demo of calling the Chinese Word Segmenter
 *  programmatically.  It assumes an input file in UTF8.
 *  <p/>
 * compile with 
    javac -cp stanford-segmenter-3.7.0.jar SegDemo.java
 *  <code>
 *  Usage: java -mx1g -cp seg.jar SegDemo fileName
 *  </code>
 *  This will run correctly in the distribution home directory.  To
 *  run in general, the properties for where to find dictionaries or
 *  normalizations have to be set.
 *
 *  @author Christopher Manning
 */

public class SegDemo {
	CRFClassifier<CoreLabel> segmenter;
	String DEFAULT_CORPUS_LOCATION = System.getProperty("user.dir");
	
	
//	public static void main(String args[])throws IOException{
//		File f = new File("ctb.gz");
//		if(f.exists())
//			System.out.println("Yes, exists!");
//		else
//			System.out.println("No,does not exist!");
//	}
	
	public void addDict(String sourceFile){
		File source = new File(sourceFile);
		File dest = new File(DEFAULT_CORPUS_LOCATION);
		try {
			FileUtils.copyFileToDirectory(source , dest);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	  public SegDemo(){
		  try { 
			    ConsoleView.printlInConsoleln("1");
				System.setOut(new PrintStream(System.out, true, "utf-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  	ConsoleView.printlInConsoleln("2");
		    Properties props = new Properties();
		    props.setProperty("sighanCorporaDict", basedir);
		    props.setProperty("serDictionary", basedir + File.separator+ "dict-chris6.ser.gz");
		    props.setProperty("testFile", "test.simp.utf8");
		    props.setProperty("inputEncoding", "UTF-8");
		    props.setProperty("sighanPostProcessing", "true");
		    ConsoleView.printlInConsoleln("3");
		    segmenter = new CRFClassifier<>(props);
		    try {
		    	ConsoleView.printlInConsoleln("Loading chinese model");
		    	String env = "/Users/nishant/Desktop/ctb.gz";
				segmenter.loadClassifier(env, props);
			} catch (ClassCastException e) {
				// TODO Auto-generated catch block
				ConsoleView.printlInConsoleln(e.getMessage());
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				ConsoleView.printlInConsoleln(e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				ConsoleView.printlInConsoleln(e.getMessage());
				e.printStackTrace();
			}
		    ConsoleView.printlInConsoleln("Done and Dusted");
	  }
	  
	  public SegDemo(boolean val){
		  
	  }
	  
public boolean dictExists(){
//	File f = new File(DEFAULT_CORPUS_LOCATION+File.separator+"ctb.gz");
	File f = new File(DEFAULT_CORPUS_LOCATION + File.separator+ "ctb.gz");
	if(f.isFile())
		return true;
	else
		return false;

}


  
  private static final String basedir = System.getProperty("SegDemo", "data");




public String[] sentDetect(String currentLine){
	int index = currentLine.indexOf('。');
	if(index==-1)
		return null;
	ArrayList<String> ans= new ArrayList<>();
	System.out.println(currentLine.substring(0, index+1));
	ans.add(currentLine.substring(0, index));
	int last = index+1;
	while (index >= 0) {
	    System.out.println(index);
	    index = currentLine.indexOf('。', index + 1);
	    if(index != -1){
	    	System.out.println(currentLine.substring(last, index+1));
	    	ans.add(currentLine.substring(last, index+1));
	    }
	    else{
	    	System.out.println(currentLine.substring(last));
	    	ans.add(currentLine.substring(last));
	    }
	    last = index+1;
	}
	return ans.toArray(new String[ans.size()]);
}

public List<String> chineseCount(String sentence) {
   
//    String sample = "é�¢å¯¹æ–°ä¸–çºªï¼Œä¸–ç•Œå�„å›½äººæ°‘çš„å…±å�Œæ„¿æœ›æ˜¯ï¼šç»§ç»­å�‘å±•äººç±»ä»¥å¾€åˆ›é€ çš„ä¸€åˆ‡æ–‡æ˜Žæˆ�æžœï¼Œå…‹æœ�20ä¸–çºªå›°æ‰°ç�€äººç±»çš„æˆ˜äº‰å’Œè´«å›°é—®é¢˜ï¼ŒæŽ¨è¿›å’Œå¹³ä¸Žå�‘å±•çš„å´‡é«˜äº‹ä¸šï¼Œåˆ›é€ ä¸€ä¸ªç¾Žå¥½çš„ä¸–ç•Œ é�¢å¯¹";
    List<String> segmented =  segmenter.segmentString(sentence);
    System.out.println(segmented);
    System.out.println(segmented.size());
    return segmented;
  }


  
}
