package edu.usc.cssl.tacit.common.queryprocess;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

class Attribute {
	String key = null;
	SupportedDataTypes dataType =  null;
	public Attribute(String key, SupportedDataTypes dataType) {
		this.key = key;
		this.dataType = dataType;
	}
	public Attribute() {		
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public SupportedDataTypes getDataType() {
		return dataType;
	}
	public void setDataType(SupportedDataTypes d) {
		this.dataType = d;
	}	
}
public class JsonParser {
	public JsonParser(String[] keyList) {
		for (int i = 0; i < keyList.length; i++) {
			keyList[i] = keyList[i].trim();
		}
		keyList.clone();
	}
	public JsonParser() {
	}

	public ArrayList<Attribute> findJsonStructure(String filePath) {
		ArrayList<Attribute> resultAttr = new ArrayList<Attribute>();
		try {
			getKeysFromJson(filePath, resultAttr);
		} catch (JsonSyntaxException e) {			
			e.printStackTrace();
		} catch (JsonIOException e) {			
			e.printStackTrace();
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}
		return resultAttr;
	}
	
	private void getKeysFromJson(String fileName, ArrayList<Attribute> resultAttr) throws JsonSyntaxException, JsonIOException, FileNotFoundException  {
	    Object things = new Gson().fromJson(new FileReader(fileName), Object.class);
	    collectAllTheKeys(things, resultAttr, null);
	 }

	private ArrayList<Attribute> collectAllTheKeys(Object o, ArrayList<Attribute> resultAttr, String parent) {
		if (o instanceof Map)
	    {
	    	Map<?, ?> map = (Map<?,?>) o;
	    	for (Object key : map.keySet()) {
		    	if (!(map.get(key) instanceof Map) && !(map.get(key) instanceof Collection)){
		    		Attribute attr = new Attribute();
		    		if (map.get(key) instanceof Double) 
						attr.setDataType(SupportedDataTypes.DOUBLE);
		    		else if(map.get(key) instanceof String)
		    			attr.setDataType(SupportedDataTypes.STRING);
		    		else if(map.get(key) instanceof Integer)
		    			attr.setDataType(SupportedDataTypes.INTEGER);
		    		else
		    			attr.setDataType(SupportedDataTypes.STRING); // TODO : as of now, for default cases
		    		if(null != parent) attr.setKey(parent + "." + key.toString());
		    		else attr.setKey(key.toString());
		    		resultAttr.add(attr);
		    		
		    	} else if(map.get(key) instanceof Map) {
		    		if(null!= parent) parent+="."+key.toString();
		    		else parent = key.toString();
		    		collectAllTheKeys(map.get(key), resultAttr, parent);
		    	} else if(map.get(key) instanceof Collection) {
		    		if(null!= parent) parent+="."+key.toString();
		    		else parent = key.toString();
		    		collectAllTheKeys(map.get(key), resultAttr, parent);
		    	}
	    	}
	    } else if(o instanceof Collection) {
	    	for (Object key : (Collection<?>)o) {
		    	if (!(key instanceof Map) && !(key instanceof Collection)){
		    		Attribute attr = new Attribute();
		    		if(key instanceof Double) 
						attr.setDataType(SupportedDataTypes.DOUBLE);
		    		else if(key instanceof String)
		    			attr.setDataType(SupportedDataTypes.STRING);
		    		else if(key instanceof Integer)
		    			attr.setDataType(SupportedDataTypes.INTEGER);
		    		else 
		    			attr.setDataType(SupportedDataTypes.STRING); // TODO : as of now, for default cases
		    		if(null != parent) attr.setKey(parent + "." + key.toString());
		    		else attr.setKey(key.toString());
		    		resultAttr.add(attr);
		    	} else if(key instanceof Map) {
		    		collectAllTheKeys(key, resultAttr, parent);
		    	} else if(key instanceof Collection) {
		    		if(null!= parent) parent+="."+key.toString();
		    		else parent = key.toString();
		    		collectAllTheKeys(key, resultAttr, parent);
		    	}
		    	break;
	    	}	    	
	    } else {
	    	// TODO: ?? 
	    }
	    return resultAttr;
	 }
  	
	public static void main(String[] args) {
		JsonParser jh = new JsonParser();
		//HashMap<String, String> jsonKeys = jh.findJsonStructure("C:\\Program Files (x86)\\eclipse\\json_corpuses\\reddit\\REDDIT_1443138695389\\Dummy\\test.json");
		ArrayList<Attribute> jsonKeys = jh.findJsonStructure("C:\\Program Files (x86)\\eclipse\\json_corpuses\\reddit\\REDDIT_1443138695389\\Dummy\\random.json.txt");
		for(Attribute attr : jsonKeys) {
			System.out.println(attr.key + "->"+ attr.dataType);
		}
	}
}
