package edu.usc.cssl.tacit.chinesecount.service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;


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
		File dest = new File("."+File.separator+"data");
		try {
			FileUtils.copyFileToDirectory(source , dest);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	  public SegDemo(){
		  try {
				System.setOut(new PrintStream(System.out, true, "utf-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		    Properties props = new Properties();
		    props.setProperty("sighanCorporaDict", basedir);
		    props.setProperty("serDictionary", basedir + File.separator+ "dict-chris6.ser.gz");
		    props.setProperty("testFile", "test.simp.utf8");
		    props.setProperty("inputEncoding", "UTF-8");
		    props.setProperty("sighanPostProcessing", "true");

		    segmenter = new CRFClassifier<>(props);
		    segmenter.loadClassifierNoExceptions(basedir + File.separator+ "ctb.gz", props);
	  }
	  
	  public SegDemo(boolean val){
		  
	  }
	  
public boolean dictExists(){
//	File f = new File(DEFAULT_CORPUS_LOCATION+File.separator+"ctb.gz");
	File f = new File(basedir + File.separator+ "ctb.gz");
	if(f.isFile())
		return true;
	else
		return false;

}


  
  private static final String basedir = System.getProperty("SegDemo", "data");

//  public static void main(String[] args) throws Exception {
////	  chineseCount();
//    System.setOut(new PrintStream(System.out, true, "utf-8"));
//
//    Properties props = new Properties();
//    props.setProperty("sighanCorporaDict", basedir);
//    // props.setProperty("NormalizationTable", "data/norm.simp.utf8");
//    // props.setProperty("normTableEncoding", "UTF-8");
//    // below is needed because CTBSegDocumentIteratorFactory accesses it
////    props.setProperty("serDictionary", basedir + "/dict-chris6.ser.gz");
//     props.setProperty("serDictionary", "dict-chris6.ser.gz");
////    if (args.length > 0) {
//      props.setProperty("testFile", "test.simp.utf8");
////    }
//    props.setProperty("inputEncoding", "UTF-8");
//    props.setProperty("sighanPostProcessing", "true");
//
//    CRFClassifier<CoreLabel> segmenter = new CRFClassifier<>(props);
//    segmenter.loadClassifierNoExceptions("ctb.gz", props);
////    for (String filename : args) {
////      segmenter.classifyAndWriteAnswers(filename);
////    }
//
//    String sample = "é�¢å¯¹æ–°ä¸–çºªï¼Œä¸–ç•Œå�„å›½äººæ°‘çš„å…±å�Œæ„¿æœ›æ˜¯ï¼šç»§ç»­å�‘å±•äººç±»ä»¥å¾€åˆ›é€ çš„ä¸€åˆ‡æ–‡æ˜Žæˆ�æžœ";
//    String sample3 = "ï¼Œå…‹æœ�20ä¸–çºªå›°æ‰°ç�€äººç±»çš„æˆ˜äº‰å’Œè´«å›°é—®é¢˜ï¼ŒæŽ¨è¿›å’Œå¹³ä¸Žå�‘å±•çš„å´‡é«˜äº‹ä¸šï¼Œåˆ›é€ ä¸€ä¸ªç¾Žå¥½çš„ä¸–ç•Œ é�¢å¯¹";
//    List<String> segmented = segmenter.segmentString(sample);
//    System.out.println(segmented);
//    System.out.println(segmented.size());
//    segmented = segmenter.segmentString(sample3);
//    System.out.println(segmented);
//    System.out.println(segmented.size());
//  }


//public static void main(String args[]){
//	String str = "é�¢å¯¹æ–°";
//        int length = str.length();
//        for (int i = 0; i < length; i++){
//            char ch = str.charAt(i);
//            Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
//            if (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block)|| 
//                Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(block)|| 
//                Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block)){
//                System.out.println("True");
//            }
//        }
//        System.out.println("False");
//		
//}



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
