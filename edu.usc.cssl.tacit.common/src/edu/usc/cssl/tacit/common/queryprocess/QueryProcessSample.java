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
import java.util.Set;

import net.minidev.json.JSONArray;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.helper.StringUtil;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

public class QueryProcessSample {
	
	private static void applyFiltersSequential(List<Filter> filters, String jsonFilePath) throws FileNotFoundException, IOException, ParseException {
		Set<Object> filteredResults = processFilter(filters, jsonFilePath);
		writeToFile(jsonFilePath, filteredResults);
	}
	
	private static void applySmartFilters(List<Filter> filters, String jsonFilePath) throws FileNotFoundException, IOException, ParseException {
		Set<Object> filteredResults = new HashSet<Object>();
		HashMap<String, List<Filter>> groupedFilters = groupFilters(filters);
		
		// Read JSON doc
		JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(jsonFilePath));
        JSONObject jsonObject = (JSONObject) obj;
        
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(jsonObject.toJSONString());
		for(String parentFilters : groupedFilters.keySet()) {
			String smartQuery = createSmartFilters(parentFilters, groupedFilters, "&&");
			System.out.println(parentFilters + ":" +smartQuery);
			Object result = JsonPath.parse(document).read(smartQuery);
			if(result instanceof LinkedHashMap<?, ?>) {
				LinkedHashMap<?, ?> records = (LinkedHashMap<?, ?>) result;
				filteredResults.add(records);
			} else if(result instanceof JSONArray) {
				JSONArray records =  (JSONArray) result;
				for(Object ob : records)
					filteredResults.add(ob);
			}
		}
		writeToFile(jsonFilePath, filteredResults);
	}
	
	/*
	 * Create smart queries and apply them on JSON document
	 */
	private static String createSmartFilters(String parentFilters, HashMap<String, List<Filter>> groupedFilters, String condition) {
		StringBuilder query = new StringBuilder();
		query.append("$.");
		query.append(parentFilters);
		List<String> predicates = new ArrayList<String>();
		for(Filter f : groupedFilters.get(parentFilters))
			predicates.add(constructSmartQuery(f));
		if(predicates.size()>1) {
			query.append("[?(");
			query.append(StringUtil.join(predicates, " " + condition + " "));
			query.append(")]");
		}	
		return new String(query);
	}

	/*
	 * Group filters based on the parents
	 */
	private static HashMap<String, List<Filter>> groupFilters(List<Filter> filters) {
		HashMap<String, List<Filter>> groupedFilters = new HashMap<String, List<Filter>>();
		if(null == filters) return groupedFilters;		
		
		for(Filter filter : filters) {
			String targetName = filter.getTargetName();			
			String parentAttr = (targetName.indexOf('.') == -1) ? targetName : targetName.substring(0, targetName.lastIndexOf('.'));
			List<Filter> filterList;
			if(groupedFilters.containsKey(parentAttr))
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
		if(f.getFilterValue() == null) 
			return new String(query);
		
		String queryAttr = f.getTargetName().substring(f.getTargetName().lastIndexOf('.')+1);
		if(f.getTargetType() == QueryDataType.INTEGER || f.getTargetType() == QueryDataType.DOUBLE) {
			if(f.getOperationType().equals(QueryOperatorType.INTEGER_EQUALS) || f.getOperationType().equals(QueryOperatorType.DOUBLE_EQUALS))
				query.append("@."+ queryAttr + " == '" + f.getFilterValue() + "'");	
			else if(f.getOperationType().equals(QueryOperatorType.INTEGER_GREATER_THAN) || f.getOperationType().equals(QueryOperatorType.DOUBLE_GREATER_THAN))
				query.append("@."+ queryAttr+ " > '" + f.getFilterValue() + "'");
			else if(f.getOperationType().equals(QueryOperatorType.INTEGER_LESS_THAN) || f.getOperationType().equals(QueryOperatorType.DOUBLE_LESS_THAN))
				query.append("@."+ queryAttr + " < '" + f.getFilterValue() + "'");
		} else if(f.getTargetType() == QueryDataType.STRING) {
			if(f.getOperationType().equals(QueryOperatorType.STRING_EQUALS))
				query.append("@."+ queryAttr + " == '" + f.getFilterValue() + "'");		
			else if(f.getOperationType().equals(QueryOperatorType.STRING_CONTAINS)) //$.store.book[?(@.title =~ /^.*sword.*$/i)]
				query.append("@."+ queryAttr + " =~ /^.*" + f.getFilterValue() + ".*$/i");
			else if(f.getOperationType().equals(QueryOperatorType.STRING_STARS_WITH))
				query.append("@."+ queryAttr + " =~ /^" + f.getFilterValue() + ".*$/i");
			else if(f.getOperationType().equals(QueryOperatorType.STRING_ENDS_WITH))
				query.append("@."+ queryAttr + " =~ /^.*" + f.getFilterValue() + "$/i");
		}
		return new String(query);
		
	}

	private static void writeToFile(String jsonFilePath, Set<Object> filteredResults) {
        try {
	    	JSONArray results = new JSONArray(); // convert JSON objects to array //
	        for(Object ob : filteredResults) 
	        	results.add(ob);
        	String filePath = jsonFilePath + ".test.txt";
        	if(new File(filePath).exists())
        		new File(filePath).delete();
			FileWriter file = new FileWriter(filePath, true);
	    	Writer writer = new JSONWriter(); // for pretty-printing 	
        	results.writeJSONString(writer);
        	file.write(writer.toString());
    		file.flush();
            file.close();        	
	        System.out.println("Filtered results are stored in " + filePath);
        } catch (IOException e) { }        
	}
	

	private static Set<Object> processFilter(List<Filter> filters, String jsonFilePath) throws FileNotFoundException, IOException, ParseException {
		Set<Object> filteredResults = new HashSet<Object>();
		JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(jsonFilePath));
        JSONObject jsonObject = (JSONObject) obj;
		for(Filter f : filters) {
			Object document = Configuration.defaultConfiguration().jsonProvider().parse(jsonObject.toJSONString());
			String filterQuery = constructJSONPathQuery(f);
			Object result = JsonPath.parse(document).read(filterQuery);
			//System.out.println(filterQuery + " : "+ result.toString());
			if(result instanceof LinkedHashMap<?, ?>) {
				LinkedHashMap<?, ?> records = (LinkedHashMap<?, ?>) result;
				filteredResults.add(records);
			} else if(result instanceof JSONArray) {
				JSONArray records =  (JSONArray) result;
				for(Object ob : records)
					filteredResults.add(ob);
			}
		}
		return filteredResults;
	}

	private static String constructJSONPathQuery(Filter f) {
		if(f.getFilterValue() == null) { // parent query construction
			return constructParentQuery(f.getTargetName());
		} else {
			StringBuilder query = new StringBuilder();
			String[] queryComponents = f.getTargetName().split("\\.");
			query.append("$.");
			for(int i = 0; i<queryComponents.length-1; i++) { // except the last component
				query.append(queryComponents[i]);
				if(i!= queryComponents.length-2) query.append(".");
			}
			if(f.getTargetType() == QueryDataType.INTEGER || f.getTargetType() == QueryDataType.DOUBLE) {
				if(f.getOperationType().equals(QueryOperatorType.INTEGER_EQUALS) || f.getOperationType().equals(QueryOperatorType.DOUBLE_EQUALS))
					query.append("[?(@."+ queryComponents[queryComponents.length-1] + " == '" + f.getFilterValue() + "')]");	
				else if(f.getOperationType().equals(QueryOperatorType.INTEGER_GREATER_THAN) || f.getOperationType().equals(QueryOperatorType.DOUBLE_GREATER_THAN))
					query.append("[?(@."+ queryComponents[queryComponents.length-1] + " > '" + f.getFilterValue() + "')]");
				else if(f.getOperationType().equals(QueryOperatorType.INTEGER_LESS_THAN) || f.getOperationType().equals(QueryOperatorType.DOUBLE_LESS_THAN))
					query.append("[?(@."+ queryComponents[queryComponents.length-1] + " < '" + f.getFilterValue() + "')]");
			} else if(f.getTargetType() == QueryDataType.STRING) {
				if(f.getOperationType().equals(QueryOperatorType.STRING_EQUALS))
					query.append("[?(@."+ queryComponents[queryComponents.length-1] + " == '" + f.getFilterValue() + "')]");		
				else if(f.getOperationType().equals(QueryOperatorType.STRING_CONTAINS)) //$.store.book[?(@.title =~ /^.*sword.*$/i)]
					query.append("[?(@."+ queryComponents[queryComponents.length-1] + " =~ /^.*" + f.getFilterValue() + ".*$/i)]");
				else if(f.getOperationType().equals(QueryOperatorType.STRING_STARS_WITH))
					query.append("[?(@."+ queryComponents[queryComponents.length-1] + " =~ /^" + f.getFilterValue() + ".*$/i)]");
				else if(f.getOperationType().equals(QueryOperatorType.STRING_ENDS_WITH))
					query.append("[?(@."+ queryComponents[queryComponents.length-1] + " =~ /^.*" + f.getFilterValue() + "$/i)]");
			}
			return new String(query);
		}
	}
	
	private static String constructParentQuery(String parentKey) {
		StringBuilder query = new StringBuilder();
		query.append("$.");
		query.append(parentKey);
		return new String(query);
	}	

	
	private static List<Filter> createSampleFilters() {
		List<Filter> filters = new ArrayList<Filter>();
		Filter f1 = new Filter("comments.score", QueryDataType.supportedOperations(QueryDataType.DOUBLE).get(0), "100", QueryDataType.DOUBLE); // target names should always be valid
		//Filter f2 = new Filter("post.score", QueryDataType.supportedOperations(QueryDataType.DOUBLE).get(0), "50", QueryDataType.DOUBLE);
		//Filter f3 = new Filter("post.score", QueryDataType.supportedOperations(QueryDataType.DOUBLE).get(0), "60", QueryDataType.DOUBLE);
		//Filter f4 = new Filter("comments.author", QueryDataType.supportedOperations(QueryDataType.STRING).get(1), "Devi", QueryDataType.STRING); // CONTAINS
		Filter f5 = new Filter("comments.author", QueryDataType.supportedOperations(QueryDataType.STRING).get(2), "SL", QueryDataType.STRING); //STARS WITH
		Filter f6 = new Filter("comments.body", QueryDataType.supportedOperations(QueryDataType.STRING).get(3), "g", QueryDataType.STRING); //ENDS WITH
		filters.add(f1);
		//filters.add(f2);
		//filters.add(f3);
		//filters.add(f4);
		filters.add(f5);
		filters.add(f6);
		return filters;
	}	
	
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
		String jsonFilePath = "C:\\Program Files (x86)\\eclipse\\json_corpuses\\reddit\\REDDIT_1443138695389\\Dummy\\long.json";
		System.out.println(jsonFilePath);
		List<String> parentKeys = JsonParser.getParentKeys(jsonFilePath);
		List<Filter> filters = createSampleFilters();
		createParentFilters(filters, parentKeys);
		applyFiltersSequential(filters, jsonFilePath);
		
		//smart filters
		System.out.println("Smart Filters!");
		applySmartFilters(filters, jsonFilePath);
	}

	private static void createParentFilters(List<Filter> filters, List<String> parentKeys) {
		List<String> parentFilters = new ArrayList<String>();
		for(Filter f : filters) 
			parentFilters.add(f.getTargetName().split("\\.")[0]);
		parentKeys.removeAll(parentFilters);
		for(String key : parentKeys) {
			filters.add(new Filter(key, null, null, null));
		}
	}
}
