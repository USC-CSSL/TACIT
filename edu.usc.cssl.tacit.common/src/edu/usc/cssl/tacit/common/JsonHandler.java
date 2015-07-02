package edu.usc.cssl.tacit.common;

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

public class JsonHandler {

	private String[] keyList;
	public JsonHandler() {
		
	}
	
	public JsonHandler(String[] keyList) {
		for (int i = 0; i < keyList.length; i++) {
			keyList[i] = keyList[i].trim();
		}
		
		this.keyList = new String[keyList.length];
		this.keyList = keyList.clone();
	}
	
	public HashMap<String, String> findJsonStructure(String dirpath) {
		HashMap<String,String> resultsHash = new HashMap<String, String>();
		File[] fileList = (new File(dirpath)).listFiles();
		int numFiles = fileList.length;
		Random rand = new Random();
		int randIndex = rand.nextInt(numFiles-1);
		
		try {
			resultsHash = getKeysFromJson(fileList[randIndex].getAbsolutePath(),resultsHash);
		} catch (JsonSyntaxException e) {
			
			e.printStackTrace();
		} catch (JsonIOException e) {
			
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
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
	      keys.addAll(map.keySet()); 
	      values = map.values();
	    }
	    else if (o instanceof Collection)
	      values = (Collection) o;
	    else 
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
  	
  	public String findVal(String fileName) {
  		Object things = null;
		try {
			things = new Gson().fromJson(new FileReader(fileName), Object.class);
		} catch (JsonSyntaxException e) {
			
			e.printStackTrace();
		} catch (JsonIOException e) {
			
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
		String value = findVal_R(things, keyList, 0);
  		return value;
  	}
  	
  	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String findVal_R(Object json, String[] keyList, int index) {
		
		if (json instanceof Map) {
			Map map = (Map) json;
			return findVal_R(map.get(keyList[index]), keyList, index+1);
		}
		else if (json instanceof Collection) {
			Collection values = (Collection) json;
			Object[] valuearray = (Object[]) values.toArray(new Object[values.size()]);
			return findVal_R(valuearray[Integer.parseInt(keyList[index])],keyList,index+1);
		}
		else {
			String retVal = json.toString();
			return retVal;
		}
		
		//return null;
	}
}
