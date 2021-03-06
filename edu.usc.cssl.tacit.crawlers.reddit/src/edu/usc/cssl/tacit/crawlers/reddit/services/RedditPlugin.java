package edu.usc.cssl.tacit.crawlers.reddit.services;

import static com.github.jreddit.utils.restclient.JsonUtils.safeJsonToString;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.github.jreddit.entity.Kind;
import com.github.jreddit.utils.restclient.RestClient;

import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class RedditPlugin {
	private RestClient restClient;
	private String outputPath;
	private int limitLinks; // link the number of records to be saved
	private int limitComments;
	private String sortType;
    int filesDownloaded;
    IProgressMonitor monitor;
    HashMap<String, String> redditCategories;
    Date dateObj;

    /**
     * Constructor.
     * @param restClient REST Client instance
     * @param monitor 
     * @param actor User instance
     */        
    public RedditPlugin(RestClient restClient, String outputDir, int limitLinks, int limitComments, IProgressMonitor monitor) {
    	this.restClient = restClient;
    	this.outputPath = outputDir;
    	this.limitLinks = limitLinks;
    	this.limitComments = limitComments; 
    	this.monitor = monitor; 
    	this.filesDownloaded = 0;
    	dateObj = new Date();   
    	
	}

	protected HashMap<String, String> fetchRedditCategories(int limit) {
    	redditCategories = new HashMap<String, String>();    	
    	Object response =  null;
    	try{
    		response = restClient.get("/subreddits/.json?limit=1000&sort=".concat(sortType), null).getResponseObject();
		}catch(com.github.jreddit.exception.RetrievalFailedException re) {
			ConsoleView.printlInConsoleln("Retrieval failed for the url: " + "/subreddits/.json?limit=1000&sort="+sortType);
			ConsoleView.printlInConsoleln("Exception returned from Reddit : " + re.toString());
			return null;
		}
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
			    		try{
			    			response = restClient.get("/subreddits/.json?limit=1000&after=".concat((String)subRedditDetails.get("after")), null).getResponseObject();
			    		}catch(com.github.jreddit.exception.RetrievalFailedException re) {
			    			ConsoleView.printlInConsoleln("Retrieval failed for the url");
			    			ConsoleView.printlInConsoleln("Exception returned from Reddit : " + re.toString());
			    			break;
			    		}
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
		filesDownloaded = 0;
		if(monitor.isCanceled()) {
			ConsoleView.printlInConsoleln("Total no.of.files downloaded :"+ filesDownloaded);
			monitor.subTask("Cancelling...");
			return;
		}	
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
	    String filePath = this.outputPath + File.separator + trendType + "-" + df.format(dateObj);
		JSONArray resultData = new JSONArray(); // to store the results
		getSimplifiedLinkData(resultData, "/".concat(trendType).concat("/").concat(".json"));
    	FileWriter file = new FileWriter(filePath);
    	Writer writer = new JSONWriter(); 
    	resultData.writeJSONString(writer);
		file.write(writer.toString());
		file.flush();
        file.close();
		if(monitor.isCanceled()) {
			ConsoleView.printlInConsoleln("Total no.of.files downloaded :"+ filesDownloaded);
			monitor.subTask("Cancelling...");
			return;
		} 
		//filesDownloaded++; for summary file
		ConsoleView.printlInConsoleln("Total no.of.files downloaded :"+ filesDownloaded);
        monitor.worked(5);
	}
	
	/*
	 * To crawl all the user posts
	 */
	public void crawlUsersPosts(String username) throws IOException, URISyntaxException { // As of now fetches only links
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
	    String filePath = this.outputPath + File.separator + "UserPosts-" + df.format(dateObj) + ".json";
		JSONArray resultData = new JSONArray(); // to store the results
		getSimplifiedLinkData(resultData, "/user/".concat(username).concat("/.json?sort=").concat(sortType));
    	FileWriter file = new FileWriter(filePath);
    	Writer writer = new JSONWriter(); 
    	resultData.writeJSONString(writer);
		file.write(writer.toString());
		file.flush();
        file.close();
	}
	
	/*
	 * To crawl all labeled posts (controversial, top)
	 */
	public void crawlLabeledPosts(String url, String label) throws IOException, URISyntaxException { // As of now fetches only links
		filesDownloaded = 0;
		if(monitor.isCanceled()) {
			ConsoleView.printlInConsoleln("Total no.of.files downloaded :"+ filesDownloaded);
			monitor.subTask("Cancelling...");
			return;
		}
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
		String filePath = this.outputPath + File.separator + label + "-" + df.format(dateObj) + ".txt";
		JSONArray resultData = new JSONArray(); // to store the results
		getSimplifiedLinkData(resultData, url);
    	FileWriter file = new FileWriter(filePath);
    	Writer writer = new JSONWriter(); 
    	resultData.writeJSONString(writer);
		file.write(writer.toString());
		file.flush();
        file.close();
		if(monitor.isCanceled()) {
			ConsoleView.printlInConsoleln("Total no.of.files downloaded :"+ filesDownloaded);
			monitor.subTask("Cancelling...");
			return;
		}
		//filesDownloaded++; for summary files
		ConsoleView.printlInConsoleln("Total no.of.files downloaded :"+ filesDownloaded);		
        monitor.worked(5);
	}
	
	
	/*
	 * To crawl the given query results (title:cats subreddit:movies)
	 */
	public void crawlQueryResults(String query, String subreddit) throws IOException, URISyntaxException { // As of now fetches only links
		filesDownloaded = 0;
		if(monitor.isCanceled()) {
			ConsoleView.printlInConsoleln("Total no.of.files downloaded :"+ filesDownloaded);
			monitor.subTask("Cancelling...");
			return;
		}		
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
		String filePath = this.outputPath + File.separator;
		if(null != subreddit && !subreddit.isEmpty())
			filePath+= "SearchResults-" + subreddit + "-" + df.format(dateObj) +".txt";
		else
			filePath+= "SearchResults-" + df.format(dateObj) + ".txt";
		
		JSONArray resultData = new JSONArray(); // to store the results
		getSimplifiedLinkData(resultData, "/search/.json?".concat(query));
		if (filesDownloaded != 0){
			ConsoleView.printlInConsoleln("Writing "+ filePath);
		}
				
		FileWriter file = new FileWriter(filePath);
    	Writer writer = new JSONWriter(); // for pretty-printing 
    	resultData.writeJSONString(writer);
		file.write(writer.toString());
		file.flush();
        file.close();
		if(monitor.isCanceled()) {
			ConsoleView.printlInConsoleln("Total no.of.files downloaded :"+ filesDownloaded);
			monitor.subTask("Cancelling...");
			return;
		}
		//filesDownloaded++; // for the summary file
		ConsoleView.printlInConsoleln("Total no.of.files downloaded :"+ filesDownloaded);			
        monitor.worked(5);
	}
	
    @SuppressWarnings("unchecked")
	private void getSimplifiedLinkData(JSONArray resultData, String url) throws IOException, URISyntaxException {
    	Object response = null;
    	try {
    		response = restClient.get(url, null).getResponseObject();
    	}catch(com.github.jreddit.exception.RetrievalFailedException re) {
    		ConsoleView.printlInConsoleln("Retrieval failed for the url: " + url);
    		ConsoleView.printlInConsoleln("Exception returned from Reddit : " + re.toString());
    		return;
    	}
    	
    	int count = 0;
        
        breakEverything:
        while(true) {
        	if (response instanceof JSONObject) {
        		JSONObject respObject = (JSONObject) response;
	        	JSONObject dataObject = (JSONObject) respObject.get("data");
	            JSONArray userPosts = (JSONArray) dataObject.get("children");
		    	for (Object post : userPosts) {
					if(monitor.isCanceled()) {
						ConsoleView.printlInConsoleln("Total no.of.files downloaded :"+ filesDownloaded);
						monitor.subTask("Cancelling...");
						return;
					}
		    		JSONObject data = (JSONObject) post;
		            String kind = safeJsonToString(data.get("kind"));
					if (kind != null) {
						if (kind.equals(Kind.LINK.value())) { // only links are saved, not comments, etc.
		                    data = ((JSONObject) data.get("data"));	
		                    JSONObject simplifiedLinkData = getSimplifiedLinkData(data);
		                    resultData.add(simplifiedLinkData); // add the simplified link data to resultant object array
		                    saveLinkComments(data, simplifiedLinkData); // save the link comments
		                    count++;
		                    monitor.worked(1);
		                    if(this.limitLinks == count)  break breakEverything;
		                }
					}
		    	}
				if(dataObject.containsKey("after") && null != dataObject.get("after")) {
					try{
						if(url.contains("?")) {
							response = restClient.get(url.concat("&after=").concat(String.valueOf(dataObject.get("after"))), null).getResponseObject();
						} else {
							response = restClient.get(url.concat("?after=").concat(String.valueOf(dataObject.get("after"))), null).getResponseObject();
						}
					}catch(com.github.jreddit.exception.RetrievalFailedException re) {
			    		ConsoleView.printlInConsoleln("Retrieval failed for the url: " + url);
			    		ConsoleView.printlInConsoleln("Exception returned from Reddit : " + re.toString());
			    		return;
			    	}
				} else { // doesnt contain any further data					
					break breakEverything;
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
	private void saveLinkComments(JSONObject obj, JSONObject linkData) throws IOException, URISyntaxException {
		String permalink = String.valueOf(obj.get("permalink")); // direct link to comments
		if(-1 != permalink.indexOf("?")) {
			String temp[] = permalink.split("\\?");
			permalink = temp[0];
		}
		System.out.println("Crawling comments :" + permalink);
		DateFormat df = new SimpleDateFormat("MM-dd-yy-HH-mm-ss");
		String filePath = this.outputPath + File.separator + getLastURLComponent(permalink) + "-" + df.format(dateObj) +  UUID.randomUUID().toString() + ".json";	    
		JSONArray linkComments = new JSONArray();
		Object response = null;
		try{
			response = restClient.get(permalink.concat("/.json?sort=best"), null).getResponseObject(); // sorts by best
		}catch(com.github.jreddit.exception.RetrievalFailedException re) {
    		ConsoleView.printlInConsoleln("Retrieval failed for the url: " + permalink + "/.json?sort=best");
    		ConsoleView.printlInConsoleln("Exception returned from Reddit : " + re.toString());
    		return;
    	}
		int count = 0;
		
		breakCommentFetch:
		while(true) {
			if (response instanceof JSONArray) {	    	
			    	JSONObject respObject =  (JSONObject)((JSONArray) response).get(1); 
			    	if(null == respObject || respObject.isEmpty()) return;
			    	JSONObject dataObject = (JSONObject) respObject.get("data");
			    	if(null == dataObject || dataObject.isEmpty()) return;
			        JSONArray userComments = (JSONArray) dataObject.get("children");
			        if(null == userComments || userComments.isEmpty()) return;
			    	for (Object post : userComments) {
			    		JSONObject data = (JSONObject) post;
			            String kind = safeJsonToString(data.get("kind"));
						if (kind != null) {
							if (kind.equals(Kind.COMMENT.value())) { 
			                    data = ((JSONObject) data.get("data"));
			                    linkComments.add(getSimplifiedCommentData(data));
			                    count++;
			                    if(count == this.limitComments) break breakCommentFetch;
			                } else if (kind.equals(Kind.MORE.value())) {
			                	// handle more comments
			        	    	dataObject = (JSONObject) data.get("data");
			        	        userComments = (JSONArray) dataObject.get("children");
			        	        for (Object morePost : userComments) {
			        	        	JSONObject result = fetchThisComment(morePost, permalink);
			        	        	if(null != result)
			        	        		linkComments.add(result);
			        	        	count++;
			        	        	if(count == this.limitComments) break breakCommentFetch;
			        	        }
			                }
						} 
			    	}
			    	break breakCommentFetch; // no more comments
			    } 
			else {
			       	throw new IllegalArgumentException("Parsing failed because JSON input is not from a submission.");
		    }
		}
		
		JSONObject consolidatedData  = new JSONObject();
		consolidatedData.put("post", linkData);
		consolidatedData.put("comments", linkComments);
		ConsoleView.printlInConsoleln("Writing "+ filePath);
    	FileWriter file = new FileWriter(filePath);
    	Writer writer = new JSONWriter(); 
    	consolidatedData.writeJSONString(writer);
		file.write(writer.toString());
		file.flush();
        file.close();
        filesDownloaded++;
	}

	private JSONObject fetchThisComment(Object morePost, String permalink) {
		Object response = null;
		try{
			response = restClient.get((permalink + morePost).concat("/.json?sort=best"), null).getResponseObject();
		}catch(com.github.jreddit.exception.RetrievalFailedException re) {
			ConsoleView.printlInConsoleln("Retrieval failed for the url: " + permalink + morePost + "/.json?sort=best");
			ConsoleView.printlInConsoleln("Exception returned from Reddit : " + re.toString());
			return null;
		}
		
		if(null == response) return null;
    	JSONObject respObject =  (JSONObject)((JSONArray) response).get(1);
    	if(null == respObject || respObject.isEmpty()) return null;
    	JSONObject dataObject = (JSONObject) respObject.get("data");
    	if(null == dataObject || dataObject.isEmpty()) return null;
    	if(0 == ((JSONArray) dataObject.get("children")).size()) 
    		return null;
        JSONObject comment = (JSONObject) ((JSONArray) dataObject.get("children")).get(0);
        if(null == comment) return null;
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
        simplifiedData.put("subreddit", data.get("subreddit"));
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

	public void updateOutputDirectory(String subRedditPath) {
		this.outputPath = subRedditPath;		
	}
	
}
