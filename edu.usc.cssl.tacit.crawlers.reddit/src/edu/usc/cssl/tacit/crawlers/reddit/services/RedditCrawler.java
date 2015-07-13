package edu.usc.cssl.tacit.crawlers.reddit.services;
import java.io.IOException;

import org.json.simple.parser.ParseException;

import com.github.jreddit.entity.User;
import com.github.jreddit.exception.RedditError;
import com.github.jreddit.exception.RetrievalFailedException;
import com.github.jreddit.utils.restclient.HttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;

import examples.Authentication;

public class RedditPlugin {

	public static void main(String[] args) throws IOException {
		// Initialize REST Client
	    RestClient restClient = new HttpRestClient();
	    restClient.setUserAgent("bot/1.0 by name");

		// Connect the user 
	    User user = new User(restClient, Authentication.getUsername(), Authentication.getPassword());
		try {
			user.connect();
		} catch (IOException e1) {
			System.err.println("I/O Exception occured when attempting to connect user.");
			e1.printStackTrace();
			return;
		} catch (ParseException e1) {
			System.err.println("I/O Exception occured when attempting to connect user.");
			e1.printStackTrace();
			return;
		}

		try {
			
			// Send request to reddit server via REST client
	        Retrieve ret = new Retrieve(restClient, user);
	        //ret.fetchRedditCategories("/subreddits/mine");
	        ret.fetchRedditCategories(99); // returns all the available reddit groups
	        try{
	        	/*JSONObject object = ret.GetListingPageObject("/user/yknjsnow"); // gives all the user posts
	        	ret.SaveLink(object, "pagelisting.txt");
	        	System.out.println(object.toString());
	        	*/
	        	ret.getAllUsersPosts("yknjsnow");
	        	
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
