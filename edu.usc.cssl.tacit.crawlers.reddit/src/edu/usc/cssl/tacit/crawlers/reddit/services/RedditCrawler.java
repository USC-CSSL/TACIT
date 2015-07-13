package edu.usc.cssl.tacit.crawlers.reddit.services;
import java.io.IOException;
import java.net.URISyntaxException;

import com.github.jreddit.exception.RedditError;
import com.github.jreddit.exception.RetrievalFailedException;
import com.github.jreddit.utils.restclient.HttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;

public class RedditCrawler {

	public static void main(String[] args) throws IOException, URISyntaxException {
		// Initialize REST Client
	    RestClient restClient = new HttpRestClient();
	    restClient.setUserAgent("bot/1.0 by name");
		try {
			
			// Send request to reddit server via REST client
	        RedditPlugin ret = new RedditPlugin(restClient);
	        //ret.fetchRedditCategories("/subreddits/mine");
	        ret.fetchRedditCategories(99); // returns all the available reddit groups
	        try{
	        	/*JSONObject object = ret.GetListingPageObject("/user/yknjsnow"); // gives all the user posts
	        	ret.SaveLink(object, "pagelisting.txt");
	        	System.out.println(object.toString());
	        	*/
	        	//ret.getAllUsersPosts("yknjsnow");
	        	ret.getQueryResults("apple");
	       /*
	        	object = ret.GetSearchPageObject("apple");
	        	ret.SaveLink(object, "pageobject.txt");
	        	System.out.println(object.toString());
	        */	
	        	/*object = ret.GetCommentPageObject("/r/pics/comments/34rpkm/my_son_and_i_got_to_meet_a_really_nice_man_today");
	        	ret.SaveComment(object, "pagecomments.txt");
	        	System.out.println(object.toString());
	        	*/
	        	
	        }
	        catch (IllegalArgumentException e1){
	        	e1.printStackTrace();;
	        }		
		} catch (RetrievalFailedException e) {
			e.printStackTrace();
		} catch (RedditError e) {
			e.printStackTrace();
		}

	}
	
}
