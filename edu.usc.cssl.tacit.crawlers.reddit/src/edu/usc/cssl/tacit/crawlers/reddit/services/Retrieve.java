package edu.usc.cssl.tacit.crawlers.reddit.services;

import static com.github.jreddit.utils.restclient.JsonUtils.safeJsonToString;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.github.jreddit.entity.Kind;
import com.github.jreddit.entity.User;
import com.github.jreddit.utils.restclient.RestClient;


public class Retrieve {
	private RestClient restClient;
    
    /**
     * Constructor.
     * @param restClient REST Client instance
     * @param actor User instance
     */
    public Retrieve(RestClient restClient, User actor) {
    	this.restClient = restClient;
    }
    
    //Params must be added for more specific retrieval and also for many listing responses
    
    public JSONObject GetListingPageObject(String url) throws IllegalArgumentException  {
    	Object response = restClient.get(url.concat("/.json?&sort=new"), null).getResponseObject();
	    if (response instanceof JSONObject) {
	    	JSONObject object =  (JSONObject) response;
	       	return object;
	    } else {
	       	throw new IllegalArgumentException("Parsing failed because JSON input is not from a submission.");
        }
    }
    
    public JSONObject GetSearchPageObject(String query) throws IllegalArgumentException  {
    	Object response = restClient.get("/search.json?sort=new&q=".concat(query), null).getResponseObject();
	    if (response instanceof JSONObject) {
	        JSONObject object =  (JSONObject) response;
	       	return object;
	    } else {
	       	throw new IllegalArgumentException("Parsing failed because JSON input is not from a submission.");
        }
    }
    
    public JSONObject GetCommentPageObject(String url) throws IllegalArgumentException  {
    	Object response = restClient.get(url.concat("/.json"), null).getResponseObject();
	    if (response instanceof JSONArray) {
	    	JSONObject object =  (JSONObject)((JSONArray) response).get(1);
	       	return object;
	    } else {
	       	throw new IllegalArgumentException("Parsing failed because JSON input is not from a submission.");
        }
    }
    
    @SuppressWarnings("unchecked")
	public void SaveLink(JSONObject obj, String filePath) throws IOException {
    	assert obj != null : "JSON Object must be instantiated.";
    	JSONArray objArr = (JSONArray)((JSONObject)obj.get("data")).get("children");
    	JSONArray objArrSave = new JSONArray();
    	JSONObject data;
    	for (Object anArray : objArr) {
            data = (JSONObject) anArray;
            // Make sure it is of the correct kind
            String kind = safeJsonToString(data.get("kind"));
			if (kind != null) {
				if (kind.equals(Kind.LINK.value())) {
                    // Contents of the link
                    data = ((JSONObject) data.get("data"));
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
                    objArrSave.add(simplifiedData);
                } else if (kind.equals(Kind.MORE.value())) {
                	// These are not being saved!!! (to be edited)
                    data = (JSONObject) data.get("data");
                    JSONArray children = (JSONArray) data.get("children");
                    System.out.println("\t+ More children: " + children);
                }
			}	
		}
    	FileWriter file = new FileWriter(filePath);
		file.write(objArrSave.toJSONString());
		file.flush();
        file.close();
    }
    
    public void SaveComment(JSONObject obj, String filePath) throws IOException {
    	assert obj != null : "JSON Object must be instantiated.";
    	JSONArray objArr = RecursiveSimplification(obj);
    	FileWriter file = new FileWriter(filePath);
		file.write(objArr.toJSONString());
		file.flush();
        file.close();    	
    }
    
    @SuppressWarnings("unchecked")
	protected JSONArray RecursiveSimplification(JSONObject obj){
    	JSONArray objArr = (JSONArray)((JSONObject)obj.get("data")).get("children");
    	JSONArray objArrSave = new JSONArray();
    	
    	JSONObject data;
    	for (Object anArray : objArr) {
            data = (JSONObject) anArray;
            
            // Make sure it is of the correct kind
            String kind = safeJsonToString(data.get("kind"));
			if (kind != null) {
				if (kind.equals(Kind.COMMENT.value())) {

                    // Contents of the link
                    data = ((JSONObject) data.get("data"));
                    
                    JSONObject simplifiedData = new JSONObject();
                    
                    simplifiedData.put("body", data.get("body"));
                    simplifiedData.put("gilded", data.get("gilded"));
                    simplifiedData.put("subreddit", data.get("subreddit"));
                    simplifiedData.put("score", data.get("score"));
                    simplifiedData.put("created_utc", data.get("created_utc"));
                    simplifiedData.put("downs", data.get("downs"));
                    simplifiedData.put("author", data.get("author"));
                    Object o = data.get("replies");
                    if (o instanceof JSONObject) {
                    	// Dig towards the replies
                        JSONObject replies = (JSONObject) o;
                        simplifiedData.put("replies", RecursiveSimplification(replies));                     
                    }
                    objArrSave.add(simplifiedData);
                    
                } else if (kind.equals(Kind.MORE.value())) {

                	// These are not being saved!!! (to be edited)
                    data = (JSONObject) data.get("data");
                    JSONArray children = (JSONArray) data.get("children");
                    System.out.println("\t+ More children: " + children);
                    
                }
			}
			
		}

    	return objArrSave;
    }
    
    protected HashMap<String, String> fetchRedditCategories(int limit) {
    	HashMap<String, String> redditCategories = new HashMap<String, String>();    	
    	Object response = restClient.get("/subreddits/.json?limit=1000", null).getResponseObject();
    	int count = 0;
	    while(true) {
    	if (response instanceof JSONObject) {
	    	JSONObject subReddits =  (JSONObject)(response);
	    	int doBreak = 0;
	    	if(subReddits.containsKey("data")) {
	    		JSONObject subRedditDetails = (JSONObject) subReddits.get("data");
	    		JSONArray subscriptions = (JSONArray) subRedditDetails.get("children");
	    		for (Object subscription : subscriptions) {
	    			JSONObject data = (JSONObject)((JSONObject) subscription).get("data");
	    			String subscriptionUrl = (String) data.get("url");
	    			String subscriptionName = (String) data.get("display_name");
	    			redditCategories.put(subscriptionName, subscriptionUrl);
	    			count++;
		    		if(limit!=-1 && count == limit) {
		    			doBreak = 1;
		    			break;
		    		}
	    		}
	    		if(1 == doBreak)
	    			break;
	    		

	    		//crawl consequetive pages 
		    	if(subRedditDetails.containsKey("after")) {
		    		if(null == subRedditDetails.get("after")) 
		    			break;
		    		System.out.println("After :" + (String)subRedditDetails.get("after") );
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

	@SuppressWarnings("unchecked")
	public void getAllUsersPosts(String username) { // As of now fetches only links
    	Object response = restClient.get("/user/".concat(username).concat("/.json?sort=new"), null).getResponseObject();
    	System.out.println(response);
    	while(true) {
		    if (response instanceof JSONObject) {
		        JSONObject object =  (JSONObject) response;
		    	JSONArray objArr = (JSONArray)((JSONObject)object.get("data")).get("children");
		    	JSONArray objArrSave = new JSONArray();
		    	JSONObject data;
		    	for (Object anArray : objArr) {
		            data = (JSONObject) anArray;
		            // Make sure it is of the correct kind
		            String kind = safeJsonToString(data.get("kind"));
					if (kind != null) {
						if (kind.equals(Kind.LINK.value())) { // only links are save, not comments, etc.
		                    // Contents of the link
		                    data = ((JSONObject) data.get("data"));
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
		                    objArrSave.add(simplifiedData);
		                    
		                    // look thru the URL and find the comments 
		                    /*
		                     * 1. get link id, to look up the comments
		                     * 2. get the subreddit to fetch the comments from
		                     */		                    
		                }
					}	
		    	}
		        
		    } else {
		       	throw new IllegalArgumentException("Parsing failed because JSON input is not from a submission.");
	        }
    	}
	}
    
}
