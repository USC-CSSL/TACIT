package edu.usc.cssl.tacit.crawlers.typepad.utils;

public class TypePadWebConstants {

	public static final String BASE_URL = "http://api.typepad.com";
	
	//Assets URL Constants
	public static final String ASSETS = "/assets.json";
	
	public static final String START_TOKEN = "start-token=";
	
	public static final String QUERY = "q=";
	
	public static final String MAX_RESULTS = "max-results=";
	
	public static final String SORT = "sort=";
	
	public static final String PARAM_SEPARATOR = "&";
	
	public static final String QUERY_SEPARATOR = "?";
	
	
	// Sort Params
	public static final String PUBLISHED_TIME_RELEVANCE = "published_time_relevance";
	
	public static final String RELEVANCE = "relevance";
	
	public static final String PUBLISHED_TIME_ASC = "published_time_asc";
	
	public static final String PUBLISHED_TIME_DESC = "published_time_desc";
	
	public static final String[] SORT_PARAMS = {PUBLISHED_TIME_RELEVANCE, RELEVANCE, PUBLISHED_TIME_ASC, PUBLISHED_TIME_DESC};

}
