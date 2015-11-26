package edu.usc.cssl.tacit.common.queryprocess;

import java.io.FileNotFoundException;
import java.util.Map;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public interface IQueryProcessor {

	public Map<String, QueryDataType> getJsonKeys() throws JsonSyntaxException,
			JsonIOException, FileNotFoundException;

	//public String processJson(List<Filter> corpusFilters, String jsonFilepath) throws JsonSyntaxException, JsonIOException, IOException, ParseException;

}
