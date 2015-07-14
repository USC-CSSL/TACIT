package edu.usc.cssl.tacit.crawlers.twitter.services;

import java.util.ArrayList;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterTestProgram {

	
	public static void main(String[] args) {
	
	ConfigurationBuilder cb = new ConfigurationBuilder();
	  cb.setOAuthConsumerKey("92Oyts2p7ReEoX685HrkRtyKf");
	  cb.setOAuthConsumerSecret("w0WKnERAzXgEIZamk7ZFGsoJpEQIaOIu7unHRSm70KCdcGKj9k");
	  cb.setOAuthAccessToken("92947251-heF2D7kiE4HqCsVU30dRIwPYnT8lYUC4WqwJ2p21o");
	  cb.setOAuthAccessTokenSecret("vriNhyoecjOYZTM58Eew4ti2cMQH2Bdd9b320WNPmDBYT");

	  Twitter twitter = new TwitterFactory(cb.build()).getInstance();
	  Query query = new Query("obama");
	  int numberOfTweets = 512;
	  long lastID = Long.MAX_VALUE;
	  ArrayList<Status> tweets = new ArrayList<Status>();
	  while (tweets.size () < numberOfTweets) {
	    if (numberOfTweets - tweets.size() > 100)
	      query.setCount(100);
	    else 
	      query.setCount(numberOfTweets - tweets.size());
	    try {
	      QueryResult result = twitter.search(query);
	      tweets.addAll(result.getTweets());
	      System.out.println("Gathered " + tweets.size() + " tweets");
	      for (Status t: tweets) 
	        if(t.getId() < lastID) lastID = t.getId();

	    }

	    catch (TwitterException te) {
	      System.out.println("Couldn't connect: " + te);
	    }; 
	    query.setMaxId(lastID-1);
	  }

	  for (int i = 0; i < tweets.size(); i++) {
	    Status t = (Status) tweets.get(i);

	    GeoLocation loc = t.getGeoLocation();

	    String user = t.getUser().getScreenName();
	    String msg = t.getText();
	    String time = "";
	    if (loc!=null) {
	      Double lat = t.getGeoLocation().getLatitude();
	      Double lon = t.getGeoLocation().getLongitude();
	      System.out.println(i + " USER: " + user + " wrote: " + msg + " located at " + lat + ", " + lon);
	    } 
	    else 
	    	System.out.println(i + " USER: " + user + " wrote: " + msg);
	  }
	}

}
