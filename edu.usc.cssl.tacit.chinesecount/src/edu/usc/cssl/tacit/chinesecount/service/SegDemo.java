package edu.usc.cssl.tacit.chinesecount.service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
  public SegDemo(){
	  try {
			System.setOut(new PrintStream(System.out, true, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    Properties props = new Properties();
	    props.setProperty("sighanCorporaDict", basedir);
	    props.setProperty("serDictionary", basedir + "/dict-chris6.ser.gz");
	    props.setProperty("testFile", "test.simp.utf8");
	    props.setProperty("inputEncoding", "UTF-8");
	    props.setProperty("sighanPostProcessing", "true");

	    segmenter = new CRFClassifier<>(props);
	    segmenter.loadClassifierNoExceptions(basedir + "/ctb.gz", props);
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
//    String sample = "面对新世纪，世界各国人民的共同愿望是：继续发展人类以往创造的一切文明成果";
//    String sample3 = "，克服20世纪困扰着人类的战争和贫困问题，推进和平与发展的崇高事业，创造一个美好的世界 面对";
//    List<String> segmented = segmenter.segmentString(sample);
//    System.out.println(segmented);
//    System.out.println(segmented.size());
//    segmented = segmenter.segmentString(sample3);
//    System.out.println(segmented);
//    System.out.println(segmented.size());
//  }


//public static void main(String args[]){
//	String str = "面对新";
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
   
//    String sample = "面对新世纪，世界各国人民的共同愿望是：继续发展人类以往创造的一切文明成果，克服20世纪困扰着人类的战争和贫困问题，推进和平与发展的崇高事业，创造一个美好的世界 面对";
    List<String> segmented =  segmenter.segmentString(sentence);
    System.out.println(segmented);
    System.out.println(segmented.size());
    return segmented;
  }


  
}
