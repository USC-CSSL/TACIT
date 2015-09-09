package edu.usc.cssl.tacit.common.ui.utility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class TacitUtil {
  public static List<String> refineInput(List<String> selectedInputs) {
	  List<String> refinedInputList = new ArrayList<String>();
	  Pattern corpusDetector = Pattern.compile(".* [(](.*)[)]");
	 
	  for (String input : selectedInputs) {
		  Matcher m = corpusDetector.matcher(input);
    	  if (m.find())  input = m.group(1); 
    	  
    	  File inputFile = new File(input); 
    	  if (!inputFile.exists()) continue;
    	  if (!inputFile.isDirectory()) 
    		  refinedInputList.add(inputFile.getAbsolutePath());
    	  else {
    		  refinedInputList.addAll(getFilesFromFolder(inputFile.getAbsolutePath()));
    	  }
      }
      return refinedInputList;
  }
  
  public static List<String> getFilesFromFolder(String folderPath) {
	  File folder = new File(folderPath);
	  List<String> subFiles = new ArrayList<String>();
	  if (!folder.exists() || !folder.isDirectory()) return subFiles;
	  for (File f : folder.listFiles()) {
		  if (!f.isDirectory()) 
			  subFiles.add(f.getAbsolutePath());
		  else 
			  subFiles.addAll(getFilesFromFolder(f.getAbsolutePath()));			  
	  }
	  return subFiles;  
  }
  
}