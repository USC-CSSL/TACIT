package edu.usc.cssl.tacit.crawlers.reddit.services;

import static com.github.jreddit.utils.restclient.JsonUtils.safeJsonToString;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.github.jreddit.entity.Kind;
import com.github.jreddit.utils.restclient.RestClient;

public class RedditPlugin {
	private RestClient restClient;
	private String outputPath;
	private int limit; // link the number of records to be saved
	private String sortType;
    private ArrayList<String> subReddits;
    private boolean limitToBestComments;
    private String timeFrame;

    HashMap<String, String> redditCategories;

    /**
     * Constructor.
     * @param restClient REST Client instance
     * @param actor User instance
     */
    public RedditPlugin(RestClient restClient) {
    	this.restClient = restClient;
    	this.outputPath = "F:\\NLP\\TEMP_OUTPUT\\Reddit";
    	this.limit = 100;
    	this.sortType = "relevance";
    	this.subReddits = new ArrayList<String>();
    	subReddits.add("television");
    	this.limitToBestComments = true; // limited to best comments
    	this.timeFrame = "all";
    }
        
    protected HashMap<String, String> fetchRedditCategories(int limit) {
    	redditCategories = new HashMap<String, String>();    	
    	Object response = restClient.get("/subreddits/.json?limit=1000&sort=".concat(sortType), null).getResponseObject();
    	int count = 0;
    	
    	breakEverything:
	    while(true) {
	    	if (response instanceof JSONObject) {
		    	JSONObject subReddits =  (JSONObject)(response);
		    	if(subReddits.containsKey("data")) {
		    		JSONObject subRedditDetails = (JSONObject) subReddits.get("data");
		    		JSONArray subscriptions = (JSONArray) subRedditDetails.get("children");
		    		for (Object subscription : subscriptions) {
		    			JSONObject data = (JSONObject)((JSONObject) subscription).get("data");
		    			String subscriptionUrl = (String) data.get("url");
		    			String subscriptionName = (String) data.get("display_name");
		    			redditCategories.put(subscriptionName, subscriptionUrl);
		    			count++;
			    		if(count == limit) 
			    			break breakEverything;
		    		}
		    		//crawl consecutive pages 
			    	if(subRedditDetails.containsKey("after")) {
			    		if(null == subRedditDetails.get("after")) 
			    			break;
			    		response = restClient.get("/subreddits/.json?limit=1000&after=".concat((String)subRedditDetails.get("after")), null).getResponseObject();
			    	} else
			    		break;
		    	} else 
		    		break;
		    }	
	    }
	    
	    System.out.println(redditCategories.keySet().size());
	    for(String name: redditCategories.keySet()) {
	    	System.out.println(name + ":" + redditCategories.get(name));
	    }
    	return redditCategories;
    }

    /*
     * To crawl trending posts (hot, new, rising)
     */
	public void crawlTrendingPosts(String trendType) throws IOException, URISyntaxException {
	    String filePath = this.outputPath + File.separator + trendType + ".txt";
		JSONArray resultData = new JSONArray(); // to store the results
		getSimplifiedLinkData(resultData, "/".concat(trendType).concat("/").concat(".json"));
    	FileWriter file = new FileWriter(filePath);
		file.write(resultData.toJSONString());
		file.flush();
        file.close();		
	}
	
	/*
	 * To crawl all the user posts
	 */
	public void crawlUsersPosts(String username) throws IOException, URISyntaxException { // As of now fetches only links
	    String filePath = this.outputPath + File.separator + "UserPosts.txt";
		JSONArray resultData = new JSONArray(); // to store the results
		getSimplifiedLinkData(resultData, "/user/".concat(username).concat("/.json?sort=").concat(sortType));
    	FileWriter file = new FileWriter(filePath);
		file.write(resultData.toJSONString());
		file.flush();
        file.close();
	}
	
	/*
	 * To crawl all labeled posts (controversial, top)
	 */
	public void crawlLabeledPosts(String label) throws IOException, URISyntaxException { // As of now fetches only links
	    String filePath = this.outputPath + File.separator + label + ".txt";
		JSONArray resultData = new JSONArray(); // to store the results
		getSimplifiedLinkData(resultData, "/".concat(label).concat("/").concat(".json?t=").concat(timeFrame));
    	FileWriter file = new FileWriter(filePath);
		file.write(resultData.toJSONString());
		file.flush();
        file.close();
	}
	
	
	/*
	 * To crawl the given query results (title:cats subreddit:movies)
	 */
	public void crawlQueryResults(String query) throws IOException, URISyntaxException { // As of now fetches only links
		String filePath = this.outputPath + File.separator + query + ".txt";
		JSONArray resultData = new JSONArray(); // to store the results
		getSimplifiedLinkData(resultData, "/search/.json?sort=".concat(sortType).concat("&q=").concat(query));
    	FileWriter file = new FileWriter(filePath);
		file.write(resultData.toJSONString());
		file.flush();
        file.close();
	}
	
    @SuppressWarnings("unchecked")
	private void getSimplifiedLinkData(JSONArray resultData, String url) throws IOException, URISyntaxException {
    	Object response = restClient.get(url, null).getResponseObject();
        int count = 0;
        
        breakEverything:
        while(true) {
        	if (response instanceof JSONObject) {
        		JSONObject respObject = (JSONObject) response;
	        	JSONObject dataObject = (JSONObject) respObject.get("data");
	            JSONArray userPosts = (JSONArray) dataObject.get("children");
		    	for (Object post : userPosts) {
		    		JSONObject data = (JSONObject) post;
		            String kind = safeJsonToString(data.get("kind"));
					if (kind != null) {
						if (kind.equals(Kind.LINK.value())) { // only links are saved, not comments, etc.
		                    data = ((JSONObject) data.get("data"));		                    
		                    resultData.add(getSimplifiedLinkData(data)); // add the simplified link data to resultant object array
		                    saveLinkComments(data); // save the link comments
		                    count++;
		                    if(this.limit == count)  break breakEverything;
		                }
					}
		    	}
				if(dataObject.containsKey("after") && null != dataObject.get("after")) {
					if(url.contains("?")) {
						response = restClient.get(url.concat("&after=").concat(String.valueOf(dataObject.get("after"))), null).getResponseObject();
					} else {
						response = restClient.get(url.concat("?after=").concat(String.valueOf(dataObject.get("after"))), null).getResponseObject();
					}
				}
        	} else {
    	       	throw new IllegalArgumentException("Parsing failed because JSON input is not from a submission.");
            }
        }
	}

    /* To Look thru the link and find the related comments
     * 1. Get permalink which is a direct link to comments
     * 2. As of now, stores only first page of comments
     * 3. There are comments for comments, crawl only the top level comments
     */
	@SuppressWarnings("unchecked")
	private void saveLinkComments(JSONObject obj) throws IOException, URISyntaxException {
		String permalink = String.valueOf(obj.get("permalink")); // direct link to comments
		if(-1 != permalink.indexOf("?")) {
			String temp[] = permalink.split("\\?");
			permalink = temp[0];
		}
		System.out.println("Crawling comments :" + permalink);
	    String filePath = this.outputPath + File.separator + getLastURLComponent(permalink) +".txt";
	    
		JSONArray linkComments = new JSONArray();
		
		Object response = restClient.get(permalink.concat("/.json?sort=best"), null).getResponseObject(); // sorts by best
		
		if (response instanceof JSONArray) {	    	
		    	JSONObject respObject =  (JSONObject)((JSONArray) response).get(1); 
		    	JSONObject dataObject = (JSONObject) respObject.get("data");
		        JSONArray userComments = (JSONArray) dataObject.get("children");	
		    	for (Object post : userComments) {
		    		JSONObject data = (JSONObject) post;
		            String kind = safeJsonToString(data.get("kind"));
					if (kind != null) {
						if (kind.equals(Kind.COMMENT.value())) { // only links are save, not comments, etc.
		                    data = ((JSONObject) data.get("data"));
		                    linkComments.add(getSimplifiedCommentData(data));
		                } else if (kind.equals(Kind.MORE.value()) && !limitToBestComments) {
		                	// handle more comments
		        	    	dataObject = (JSONObject) data.get("data");
		        	        userComments = (JSONArray) dataObject.get("children");
		        	        for (Object morePost : userComments) {
		        	        	JSONObject result = fetchThisComment(morePost, permalink);
		        	        	if(null != result)
		        	        		linkComments.add(result);
		        	        }
		                }
					}	
		    	}
		    } 
		else {
		       	throw new IllegalArgumentException("Parsing failed because JSON input is not from a submission.");
	    }
		   
    	FileWriter file = new FileWriter(filePath);
		file.write(linkComments.toJSONString());
		file.flush();
        file.close();
	}

	private JSONObject fetchThisComment(Object morePost, String permalink) {
    	Object response = restClient.get((permalink + morePost).concat("/.json?sort=best"), null).getResponseObject();
    	JSONObject respObject =  (JSONObject)((JSONArray) response).get(1); 
    	JSONObject dataObject = (JSONObject) respObject.get("data");
    	if(0 == ((JSONArray) dataObject.get("children")).size()) 
    		return null;
        JSONObject comment = (JSONObject) ((JSONArray) dataObject.get("children")).get(0);
        String JSONKind = safeJsonToString(comment.get("kind"));
        if (JSONKind != null && JSONKind.equals(Kind.COMMENT.value())) { // only links are save, not comments, etc.
        	comment = ((JSONObject) comment.get("data"));
            return getSimplifiedCommentData(comment);	
		}
        return null;
	}

	/*
	 * Returns the last component of the given URL
	 */
	private String getLastURLComponent(String permalink) throws URISyntaxException {
		URI uri = new URI(permalink);
		String[] segments = uri.getPath().split("/");	
		return segments[segments.length-1];
	}

	/*
	 * Returns the newly constructed JSONObject 
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getSimplifiedLinkData(JSONObject data) {
        JSONObject simplifiedData = new JSONObject();
        simplifiedData.put("gilded", data.get("gilded"));
        simplifiedData.put("title", data.get("title"));
        simplifiedData.put("score", data.get("score"));
        simplifiedData.put("num_comments", data.get("num_comments"));
        simplifiedData.put("created_utc", data.get("created_utc"));
        simplifiedData.put("selftext", data.get("selftext"));
        simplifiedData.put("thumbnail", data.get("thumbnail"));
        simplifiedData.put("author", data.get("author"));
        simplifiedData.put("url", data.get("url"));
        return simplifiedData;		
	}
	
	/*
	 * Returns the newly constructued JSONObject 
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getSimplifiedCommentData(JSONObject data) {
        JSONObject simplifiedData = new JSONObject();
        simplifiedData.put("gilded", data.get("gilded"));
        simplifiedData.put("score", data.get("score"));
        simplifiedData.put("created_utc", data.get("created_utc"));
        simplifiedData.put("author", data.get("author"));
        simplifiedData.put("body", data.get("body"));
        simplifiedData.put("replies", data.get("replies"));
        simplifiedData.put("ups", data.get("ups"));
        simplifiedData.put("downs", data.get("downs"));
        return simplifiedData;		
	}
	
}
