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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;

import edu.usc.cssl.tacit.crawlers.typepad.utils.TypePadWebConstants;


public class TypePadCrawler {
	public static String OUTPUT_PATH = "/Users/CSSLadmin/Desktop/TypePadOutput";

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
			con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("GET");
			con.setConnectTimeout(60000);
			int responseCode = 500; //To enter the while loop
			int i = 0;
			while (i <= 10 && responseCode != HttpURLConnection.HTTP_OK){
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
		}finally{
			if (con != null){
				con.disconnect();
			}
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
	 * @throws Exception
	 */
	public void getQueryResults(ArrayList<String> contentKeywords,ArrayList<String> titleKeywords,long maxLimit,int sortParam, IProgressMonitor monitor)throws Exception{

		//Initial Query Results
		String url = TypePadWebConstants.BASE_URL+TypePadWebConstants.ASSETS+TypePadWebConstants.QUERY_SEPARATOR;
		//Adding the query string
		url = url + buildQuery(contentKeywords,titleKeywords,maxLimit,sortParam,monitor);
		
		//Default Variable Declaration 
		JSONObject resultJSONObject = null; 
		JSONArray retrievedEntriesArray = null;
		String moreResultsToken = "";
		String httpResponse = "";
		FileWriter fw = null;
		int blogCount = 1;
		
		//Generating common output file name 
		Date currentDate = new Date();
		String commonFileName = new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(currentDate);
		
		do{
			
			httpResponse = getHTTPResponse(url);
			if (!httpResponse.equals("")){
				resultJSONObject = new JSONObject(httpResponse);
				retrievedEntriesArray = resultJSONObject.getJSONArray("entries");
				try{
					moreResultsToken = resultJSONObject.getString("moreResultsToken");
				}catch(JSONException e){
					moreResultsToken = null;
				}
				
				
				JSONObject retrievedEntryObject = null;
				String retrievedEntryContent = "";
				String finalEntryContent = "";
				
				for (int i= 0; i<retrievedEntriesArray.length();i++ ){
					fw = new FileWriter(new File(OUTPUT_PATH + File.separator + "blog_"+blogCount+"_"+commonFileName));
					retrievedEntryObject = (JSONObject)retrievedEntriesArray.get(i);
					retrievedEntryContent = retrievedEntryObject.getString("content");
					finalEntryContent = Jsoup.parse(retrievedEntryContent).text();
					fw.write(finalEntryContent);
					fw.close();
					blogCount++;
				}
				
				url = TypePadWebConstants.BASE_URL+TypePadWebConstants.ASSETS+TypePadWebConstants.QUERY_SEPARATOR+TypePadWebConstants.START_TOKEN + moreResultsToken;
			}else{
				break;
			}

		}while(moreResultsToken != null);
	}

}
