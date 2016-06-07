package edu.usc.cssl.tacit.crawlers.typepad.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;

import edu.usc.cssl.tacit.crawlers.typepad.utils.TypePadJSONKeys;
import edu.usc.cssl.tacit.crawlers.typepad.utils.TypePadWebConstants;


public class TypePadCrawler {
	
	public final static int MAXIMUM_NETWORK_CALL_REATTEMPTS = 20;

	/**
	 * This method returns the JSON HTTP response string for the input string URL 
	 * @param url Connection URL
	 * @return JSON response string if the connection was successful and response was received else it returns an empty string.
	 */
	private String getHTTPResponse(String url) {
		
		BufferedReader br = null;
		HttpURLConnection con = null;
		String inputLine;
		StringBuffer response = new StringBuffer();
		try{
			URL obj = new URL(url);
			
			int responseCode = 500; //To enter the while loop
			int i = 0;
			//This loop continuous to hit the URL until the maximum number of network call re-attempts are done or the response code 200 is received, whichever is first. 
			while (i <= MAXIMUM_NETWORK_CALL_REATTEMPTS && responseCode != HttpURLConnection.HTTP_OK){
				
				con = (HttpURLConnection) obj.openConnection();

				con.setRequestMethod("GET");
				con.setReadTimeout(120000);
				con.setConnectTimeout(60000);
				con.setDoInput(true);
				
				responseCode = con.getResponseCode();
				System.out.println("\nSending 'GET' request to URL : " + url);
				System.out.println("Response Code : " + responseCode);
			
				if(responseCode == HttpURLConnection.HTTP_OK){

					br = new BufferedReader(new InputStreamReader(con.getInputStream()));
					while ((inputLine = br.readLine()) != null) {
						response.append(inputLine);
					}
					br.close();
					return response.toString();
				}else{
					System.out.println("Internal Connection Error.");
					i++;
					
				}	
				if (con != null){
					con.disconnect();
				}
			}
			return "";
			
		}catch(SocketTimeoutException e){
			System.out.println("Connection is taking too long.There is something wrong with the server.");
			System.out.println(e.getMessage());
			return "";
		}catch(UnknownHostException e){
			System.out.println("There seems to be no internet connection.");
			System.out.println(e.getMessage());
			return "";
		}catch(Exception e){
			System.out.println(e.getMessage());
			return "";
		}
		

	}
	
	/**
	 * This method builds a query string from the raw query string.The returned query can be directly added to the URL to interact with the API and retrieve results.
	 * @param contentKeywords list of content keywords
	 * @param titleKeywords list of title keywords
	 * @param maxLimit maximum number of blogs
	 * @param sortParam 0: published_time_relevance, 1: relevance, 2: published_time_asc, 3: published_time_desc, -1: no sort 
	 * @return buildQuery (eg. q=star%20wars)
	 */
	private String buildQuery(ArrayList<String> contentKeywords,ArrayList<String> titleKeywords,long maxLimit,int sortParam, IProgressMonitor monitor){

		
		StringBuilder buildQuery = new StringBuilder();
		String finalBuildQuery = "";
		
		//Building the content keyword query string 
		if (contentKeywords != null && contentKeywords.size() > 0){
			for (String queryKeyword : contentKeywords){
				queryKeyword = queryKeyword.replaceAll(" ", "%20");
				buildQuery.append(queryKeyword+"%20");
			}
			buildQuery.delete(buildQuery.length()-3, buildQuery.length());
		}
		
		//Building the title keyword query string
		if (titleKeywords != null && titleKeywords.size() > 0){
			buildQuery.append("%20title:");
			for (String queryKeyword : titleKeywords){
				queryKeyword = queryKeyword.replaceAll(" ", "%20");
				buildQuery.append(queryKeyword+"%20");
			}
			buildQuery.delete(buildQuery.length()-3, buildQuery.length());
		}
		
		finalBuildQuery = buildQuery.toString();
		
		finalBuildQuery = TypePadWebConstants.QUERY+finalBuildQuery;
		
		if (sortParam >= 0){
			finalBuildQuery = finalBuildQuery + TypePadWebConstants.PARAM_SEPARATOR + TypePadWebConstants.SORT + TypePadWebConstants.SORT_PARAMS[sortParam];
		}
		
		return finalBuildQuery;
		
	}
	
	/**
	 * This method outputs the query result blogs into files. 
	 * @param contentKeywords list of content keywords
	 * @param titleKeywords list of title keywords
	 * @param maxLimit maximum number of blogs
	 * @param sortParam 0: published_time_relevance, 1: relevance, 2: published_time_asc, 3: published_time_desc, -1: no sort
	 * @param corpusLocation The output location for the corpus
	 * @param corpusName Name of the corpus
	 * @param monitor IProgessMonitor to update the progress bar
	 * @return No. of blogs crawled.
	 * @throws Exception
	 */
	public int getQueryResults(ArrayList<String> contentKeywords,ArrayList<String> titleKeywords,long maxLimit,int sortParam, String corpusLocation, String corpusName,IProgressMonitor monitor)throws Exception{
		
		FileWriter fw =  new FileWriter(new File(corpusLocation + File.separator + corpusName +".json"));
		
		//Building overall custom JSON object for all the search results
		//JSONObject overallJSONObject = new JSONObject();
		JSONArray entryList = new JSONArray();
		JSONObject entry = null;
		
		//Initial Query Results
		String url = TypePadWebConstants.BASE_URL+TypePadWebConstants.ASSETS+TypePadWebConstants.QUERY_SEPARATOR;
		//Adding the query string
		url = url + buildQuery(contentKeywords,titleKeywords,maxLimit,sortParam,monitor);
		
		//Default Variable Declaration 
		JSONObject resultJSONObject = null; 
		JSONArray retrievedEntriesArray = null;
		String moreResultsToken = "";
		String httpResponse = "";
		String blogTitle = "";

		int blogCount = 1;
		
		one:do{
			
			httpResponse = getHTTPResponse(url);
			if (!httpResponse.equals("")){
				resultJSONObject = new JSONObject(httpResponse);
				retrievedEntriesArray = resultJSONObject.getJSONArray(TypePadJSONKeys.ENTRIES);
				try{
					moreResultsToken = resultJSONObject.getString(TypePadJSONKeys.MORE_RESULTS_TOKEN);
				}catch(JSONException e){
					moreResultsToken = null;
				}
				
				
				JSONObject retrievedEntryObject = null;				
				for (int i= 0; i<retrievedEntriesArray.length();i++ ){
					retrievedEntryObject = (JSONObject)retrievedEntriesArray.get(i);
					
					//Making custom JSON object for each entry and adding it to entry list
					entry = new JSONObject();
					
					try{
						entry.put(TypePadJSONKeys.AUTHOR_DISPLAY_NAME, ((JSONObject)retrievedEntryObject.get(TypePadJSONKeys.AUTHOR)).getString(TypePadJSONKeys.AUTHOR_DISPLAY_NAME));
					}catch(JSONException e){
						entry.put(TypePadJSONKeys.AUTHOR_DISPLAY_NAME,"");
					}
					
					try{
						entry.put(TypePadJSONKeys.AUTHOR_PREFERRED_NAME, ((JSONObject)retrievedEntryObject.get(TypePadJSONKeys.AUTHOR)).getString(TypePadJSONKeys.AUTHOR_PREFERRED_NAME));
					}catch(JSONException e ){
						entry.put(TypePadJSONKeys.AUTHOR_PREFERRED_NAME,"");
					}
					
					try{
						entry.put(TypePadJSONKeys.LOCATION, ((JSONObject)retrievedEntryObject.get(TypePadJSONKeys.AUTHOR)).getString(TypePadJSONKeys.LOCATION));
					}catch(JSONException e ){
						entry.put(TypePadJSONKeys.LOCATION, "");
					}
					
					try{
						entry.put(TypePadJSONKeys.CATEGORIES, (JSONArray)retrievedEntryObject.get(TypePadJSONKeys.CATEGORIES));
					}catch(JSONException e ){
						entry.put(TypePadJSONKeys.CATEGORIES, new JSONArray());
					}
					
					try{
						entry.put(TypePadJSONKeys.COMMENT_COUNT, retrievedEntryObject.getInt(TypePadJSONKeys.COMMENT_COUNT));
					}catch(JSONException e ){
						entry.put(TypePadJSONKeys.COMMENT_COUNT, 0);
					}
					
					try{
						String content = retrievedEntryObject.getString("content");
						content = Jsoup.parse(content).text();
						entry.put(TypePadJSONKeys.CONTENT, content);
					}catch(JSONException e ){
						entry.put(TypePadJSONKeys.CONTENT, "");
					}
					
					try{
						entry.put(TypePadJSONKeys.EXCERPT, retrievedEntryObject.getString(TypePadJSONKeys.EXCERPT));
					}catch(JSONException e ){
						entry.put(TypePadJSONKeys.EXCERPT, "");
					}
					
					
					try{
						entry.put(TypePadJSONKeys.PUBLISHED, retrievedEntryObject.getString(TypePadJSONKeys.PUBLISHED));
					}catch(JSONException e ){
						entry.put(TypePadJSONKeys.PUBLISHED, "");
					}
					
					try{
						entry.put(TypePadJSONKeys.TITLE, retrievedEntryObject.getString(TypePadJSONKeys.TITLE));
						blogTitle = retrievedEntryObject.getString(TypePadJSONKeys.TITLE);
					}catch(JSONException e ){
						entry.put(TypePadJSONKeys.TITLE, "");
						blogTitle = "";
					}
					
					entryList.put(entry);

					if (maxLimit != -1 && blogCount == maxLimit){
						blogCount++;
						break one;
					}
					blogCount++;
					monitor.worked(1);
					
					monitor.subTask("Crawling Blog #"+(blogCount-1)+" "+blogTitle);
					
					//Check to break the operation if the user has cancelled.
					if (monitor.isCanceled()){
						throw new OperationCanceledException();
					}
				}
				
				url = TypePadWebConstants.BASE_URL+TypePadWebConstants.ASSETS+TypePadWebConstants.QUERY_SEPARATOR+TypePadWebConstants.START_TOKEN + moreResultsToken;
				
				//Check to break the operation if the user has cancelled.
				if (monitor.isCanceled()){
					throw new OperationCanceledException();
				}
			}else{
				break;
			}

		}while(moreResultsToken != null);
		
		//Adding the entry list to the overall json object
		//overallJSONObject.put(TypePadJSONKeys.ENTRIES, entryList);
		
		fw.write(entryList.toString());
		fw.close();
		return blogCount-1;
	}
	
	

}
