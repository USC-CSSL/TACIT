package edu.usc.cssl.tacit.common.queryprocess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.minidev.json.JSONArray;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.helper.StringUtil;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CMDataType;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class QueryProcesser implements IQueryProcessor {

	private CorpusClass corpusClass;
	private Map<String, QueryDataType> jsonKeys;

	public QueryProcesser() {
	}
	
	/* Instantiate corpus class */
	public QueryProcesser(CorpusClass corpusClass) {
		this.corpusClass = corpusClass;
	}
	
	private List<String> applySmartFilters(List<Filter> filters, String jsonFilePath, String operator, String keyFields, CMDataType corpusType ) throws FileNotFoundException, IOException, ParseException {
		HashMap<String, List<String>> keys = null;
		List<String> resultText = new ArrayList<String>();
		Set<JSONObject> filteredResults = new HashSet<JSONObject>();
		
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader(jsonFilePath));
		Object document = null;
		JSONObject jsonObject = new JSONObject();
		if(!(obj instanceof JSONObject)) {
			jsonObject.put("contents", obj);
			for(Filter f : filters)
				f.setTargetName("contents."+f.getTargetName());
			keyFields = reconstructKeyfields(keyFields, "contents");
		} else  {
			jsonObject = (JSONObject) obj;
		}
		keys = processKeyfields(keyFields);
		HashMap<String, List<Filter>> groupedFilters = groupFilters(filters);

		document = Configuration.defaultConfiguration().jsonProvider().parse(jsonObject.toJSONString());
		
		for (String parentFilters : groupedFilters.keySet()) {
			try {	
				String smartQuery;
				smartQuery = createSmartFilters(parentFilters, groupedFilters, operator);				
				System.out.println(parentFilters + ":" + smartQuery);
				Object result = JsonPath.parse(document).read(smartQuery);
				if (result instanceof LinkedHashMap<?, ?>) {
					LinkedHashMap<?, ?> records = (LinkedHashMap<?, ?>) result;
					JSONObject temp = new JSONObject();
					temp.put(parentFilters, records);
					filteredResults.add(temp);
					if(keys.containsKey(parentFilters)) {//fetchdata
						for(String k : keys.get(parentFilters)) 
							resultText.add((String) ((LinkedHashMap) result).get(k));
					}
				} else if (result instanceof JSONArray) {
					JSONArray records = (JSONArray) result;
					for (Object ob : records) {
						LinkedHashMap<?, ?> res = (LinkedHashMap<?, ?>) ob;
						//temp.put(key, value)
						//filteredResults.add((LinkedHashMap<?, ?>)ob);
						if(keys.containsKey(parentFilters)) {
							for(String k: keys.get(parentFilters))
								resultText.add((String) ((LinkedHashMap) res).get(k));
						}
					}
				}
			} catch(ClassCastException e) {
				continue; // incase if one key fails, continue with others
			}
		}
		
		if(!corpusType.equals(CMDataType.TWITTER_JSON))
			return processedResult(resultText);
		return resultText;
		//return writeToFile(jsonFilePath, filteredResults);
	}
	


	private String reconstructKeyfields(String keyFields, String string) {
		StringBuilder result = new StringBuilder();
		for(String key : keyFields.split("\\,")) {
			result.append("contents." + key + ",");
		}
		return new String(result.substring(0, result.length()-1));
	}		

	private List<String> processedResult(List<String> resultText) {
		StringBuilder sb = new StringBuilder();
		for(String s : resultText)
			sb.append(s);
		resultText.clear();
		resultText.add(new String(sb));
		return resultText;
	}

	private HashMap<String, List<String>> processKeyfields(String keyFields) {
		HashMap<String, List<String>> keyAttr = new HashMap<String, List<String>>();
		for(String key : keyFields.split("\\,")) {
			String[] temp = key.split("\\.");
			List<String> keyChilds;
			if(keyAttr.containsKey(temp[0])) 
				keyChilds = keyAttr.get(temp[0]);
			else
				keyChilds = new ArrayList<String>();
			
			StringBuilder tempKey = new StringBuilder();
			for(int i = 1; i<temp.length; i++) {
				tempKey.append(temp[i]);
				if(i != temp.length-1) tempKey.append(".");
			}
			keyChilds.add(new String(tempKey));
			keyAttr.put(temp[0], keyChilds);
		}
		return keyAttr;
	}

	private List<String> filterResults(Set<JSONObject> filteredResults) {
		return null;
	}

	/* Create smart queries and apply them on JSON document */
	private static String createSmartFilters(String parentFilters, HashMap<String, List<Filter>> groupedFilters, String condition) {
		StringBuilder query = new StringBuilder();
		query.append("$.");
		query.append(parentFilters);
		List<String> predicates = new ArrayList<String>();
		for (Filter f : groupedFilters.get(parentFilters)) {
			if(f.getFilterValue() != null) // only if there is a proper filter
				predicates.add(constructSmartQuery(f));
		}
		if (predicates.size() > 0) {
			query.append("[?(");
			query.append(StringUtil.join(predicates, " " + condition + " "));
			query.append(")]");
		}
		return new String(query);
	}

	/* Group filters based on the parents */
	private static HashMap<String, List<Filter>> groupFilters(List<Filter> filters) {
		HashMap<String, List<Filter>> groupedFilters = new HashMap<String, List<Filter>>();
		if (null == filters)
			return groupedFilters;
		for (Filter filter : filters) {
			String targetName = filter.getTargetName();
			String parentAttr = (targetName.indexOf('.') == -1) ? targetName : targetName.substring(0, targetName.lastIndexOf('.'));
			List<Filter> filterList;
			if (groupedFilters.containsKey(parentAttr))
				filterList = groupedFilters.get(parentAttr);
			else
				filterList = new ArrayList<Filter>();
			filterList.add(filter);
			groupedFilters.put(parentAttr, filterList);
		}
		return groupedFilters;
	}

	private static String constructSmartQuery(Filter f) {
		StringBuilder query = new StringBuilder();
		if (f.getFilterValue() == null)
			return new String(query);
		String queryAttr = f.getTargetName().substring(f.getTargetName().lastIndexOf('.') + 1);
		if (f.getTargetType() == QueryDataType.INTEGER || f.getTargetType() == QueryDataType.DOUBLE) {
			if (f.getOperationType().equals(QueryOperatorType.INTEGER_EQUALS) || f.getOperationType().equals(QueryOperatorType.DOUBLE_EQUALS))
				query.append("@." + queryAttr + " == '" + f.getFilterValue() + "'");
			else if (f.getOperationType().equals(QueryOperatorType.INTEGER_GREATER_THAN) || f.getOperationType().equals(QueryOperatorType.DOUBLE_GREATER_THAN))
				query.append("@." + queryAttr + " > '" + f.getFilterValue() + "'");
			else if (f.getOperationType().equals(QueryOperatorType.INTEGER_LESS_THAN) || f.getOperationType().equals(QueryOperatorType.DOUBLE_LESS_THAN))
				query.append("@." + queryAttr + " < '" + f.getFilterValue() + "'");
		} else if (f.getTargetType() == QueryDataType.STRING) {
			if (f.getOperationType().equals(QueryOperatorType.STRING_EQUALS))
				query.append("@." + queryAttr + " == '" + f.getFilterValue() + "'");
			else if (f.getOperationType().equals(QueryOperatorType.STRING_CONTAINS))
				query.append("@." + queryAttr + " =~ /^.*" + f.getFilterValue() + ".*$/i");
			else if (f.getOperationType().equals(QueryOperatorType.STRING_STARS_WITH))
				query.append("@." + queryAttr + " =~ /^" + f.getFilterValue() + ".*$/i");
			else if (f.getOperationType().equals(QueryOperatorType.STRING_ENDS_WITH))
				query.append("@." + queryAttr + " =~ /^.*" + f.getFilterValue() + "$/i");
		}
		return new String(query);
	}

	private String writeToFile(String jsonFilePath, Set<JSONObject> filteredResults) throws IOException {		
		String filePath = System.getProperty("user.dir") + System.getProperty("file.separator") + "json_processing.temp.json";
		JSONArray results = new JSONArray(); // convert JSON objects to array
		for (Object ob : filteredResults)
			results.add(ob);
		if (new File(filePath).exists())
			new File(filePath).delete();
		FileWriter file = new FileWriter(filePath, true);
		Writer writer = new JSONWriter(); // for pretty-printing
		results.writeJSONString(writer);
		file.write(writer.toString());
		file.flush();
		file.close();
		ConsoleView.printlInConsoleln("Filtered results are stored in " + filePath);
		return filePath;
	}

	private static void createParentFilters(List<Filter> filters, Set<String> parentKeys) {
		//if(null == filters) return;
//		if(null == filters)
//			filters = new ArrayList<Filter>();
		
		List<String> parentFilters = new ArrayList<String>();
		for (Filter f : filters)
			parentFilters.add(f.getTargetName().split("\\.")[0]);
		
		parentKeys.removeAll(parentFilters);
		for (String key : parentKeys) {
			filters.add(new Filter(key, null, null, null));
		}
	}
	
	@Override
	public Map<String, QueryDataType> getJsonKeys() throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		if (this.jsonKeys == null) {
			this.jsonKeys = new TreeMap<String, QueryDataType>();
			Set<Attribute> jsonKeys = new JsonParser().findJsonStructure(this.corpusClass.getTacitLocation());
			for (Attribute attr : jsonKeys) 
				this.jsonKeys.put(attr.key, attr.dataType);
		}
		return this.jsonKeys;
	}
	

	public List<String> processJson(CorpusClass corpusClass, String jsonFilepath, String keyFields) throws JsonSyntaxException, JsonIOException, IOException, ParseException {
		List<Filter> corpusFilters = corpusClass.getFilters();
		if(null == corpusFilters) corpusFilters = new ArrayList<Filter>();
		Set<String> parentKeys = JsonParser.getParentKeys(jsonFilepath);
		createParentFilters(corpusFilters, parentKeys);	
		return applySmartFilters(corpusFilters, jsonFilepath, "&&", keyFields, corpusClass.getParent().getDatatype());
	}

}
