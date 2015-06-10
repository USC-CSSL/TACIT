package edu.usc.nlputils.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class JsonStructure {

	public HashMap<String, String> findJsonStructure(String dirpath) throws JsonSyntaxException, JsonIOException, FileNotFoundException{
		HashMap<String,String> resultsHash = new HashMap<String, String>();
		File[] fileList = (new File(dirpath)).listFiles();
		int numFiles = fileList.length;
		Random rand = new Random();
		int randIndex = rand.nextInt(numFiles-1);
		
		resultsHash = getKeysFromJson(fileList[randIndex].getAbsolutePath(),resultsHash);
		return resultsHash;
	}
	
	@SuppressWarnings("rawtypes")
	private HashMap<String, String> getKeysFromJson(String fileName,HashMap<String, String> resultsHash) throws JsonSyntaxException, JsonIOException, FileNotFoundException  {
	    Object things = new Gson().fromJson(new FileReader(fileName), Object.class);
		List keys = new ArrayList();
	    List currentPos = new ArrayList();
	    collectAllTheKeys(keys, things,currentPos,0,resultsHash);
	    return resultsHash;
	 }

  	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void collectAllTheKeys(List keys, Object o,List currentPos,int listPos, HashMap<String, String> resultsHash) {
	    Collection values = null;
	    
	    if (o instanceof Map)
	    {
	      Map map = (Map) o;
	      keys.addAll(map.keySet()); // collect keys at current level in hierarchy
	      values = map.values();
	    }
	    else if (o instanceof Collection)
	      values = (Collection) o;
	    else // nothing further to collect keys from
	      return;
	
	    Integer collectionIndex = 0;
	    for (Object value : values){
	    	if ((o instanceof Collection)){
	    		currentPos.add(collectionIndex.toString());
	    		collectionIndex++;
	    	}
	    	else currentPos.add(keys.get(listPos).toString());
	    	if (!(value instanceof Map) && !(value instanceof Collection)){
	    		if (value == null) resultsHash.put(currentPos.toString(), "null");
	    		else resultsHash.put(currentPos.toString(), value.toString());
	    	}
	    	listPos++;
	      collectAllTheKeys(keys, value,currentPos,keys.size(),resultsHash);
	      currentPos.remove(currentPos.size()-1);
	    }
	 }
}
