package edu.usc.cssl.nlputils.plugins.twitterStreamer.process;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Scanner;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.auth.OAuthAuthorization;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.util.Timer;


public class TTStream {
	
	@Inject IEclipseContext context;
	
	private int statusNum = 0;
	private final Object lock = new Object();
	
	public void Stream(String fileName,final boolean isNum, final long numTweet, final boolean isTime, final long deadLine, 
			final boolean noWord, String keyWords[], final boolean noLocation, double[][] locations, final boolean att[]) throws IOException{
		
		final long startTime = System.currentTimeMillis();
		File fi = new File("AuthKey.txt");
		ConfigurationBuilder cb = new ConfigurationBuilder();
		
		String consumerKey;
		String consumerSecret;
		String accessToken;
		String accessTokenSecret;
	    
		Scanner sc = new Scanner(fi);
			
		consumerKey = sc.nextLine();
		consumerSecret = sc.nextLine();
		accessToken = sc.nextLine();
		accessTokenSecret = sc.nextLine();
		
		System.out.println("I'm here");
		cb.setDebugEnabled(true)
			.setOAuthConsumerKey(consumerKey)
			.setOAuthConsumerSecret(consumerSecret)
			.setOAuthAccessToken(accessToken)
			.setOAuthAccessTokenSecret(accessTokenSecret);
		
		// Setup configurations
		TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
				
		// Create File
		File file1 = new File(fileName);
				
		        // Instantiate JSON writer
				JsonFactory jsonfactory = new JsonFactory();
				final JsonGenerator jsonGenerator = jsonfactory.createGenerator(file1, JsonEncoding.UTF8);
				jsonGenerator.useDefaultPrettyPrinter();
				jsonGenerator.writeStartArray();

					
				// Setup Listener	
				StatusListener listener = new StatusListener() {
		            @Override
		            public void onStatus(Status status) {
		            	
		            	statusNum++;
		            	// Get GeoLocation of Status if it is private, make it NULL
		            	Double latitude, longitude;
		            	String sLatitude, sLongitude;
		            	try{
		            	latitude = status.getGeoLocation().getLatitude();
		            	sLatitude = latitude.toString();
		            	longitude = status.getGeoLocation().getLongitude();
		            	sLongitude = longitude.toString();
		            	}
		            	catch (NullPointerException e){
		            		sLatitude = "NULL";
		            		sLongitude = "NULL";
		            	}
		            	// Write user name and the status on system output
		            	System.out.println(status.getUser().getScreenName() + ":" + status.getText());
		                    
		            	// Append the new status instance to the file
		            	try {
		            		jsonGenerator.writeStartObject();
		            		if(att[0])
		            			jsonGenerator.writeStringField("Name", status.getUser().getScreenName());
		            		if(att[1])
			            		jsonGenerator.writeStringField("Text", status.getText());
		            		if(att[2])
		            			jsonGenerator.writeStringField("Retweet", Integer.toString(status.getRetweetCount()));
		            		if(att[3]){
			            		jsonGenerator.writeStringField("Latitude", sLatitude);
			            		jsonGenerator.writeStringField("Longitude", sLongitude);
		            		}
		            		if(att[4])
			            		jsonGenerator.writeStringField("CreatedAt", status.getCreatedAt().toString());
		            		if(att[5])
			            		jsonGenerator.writeStringField("FavCount", Integer.toString(status.getFavoriteCount()));
		            		if(att[6])
			            		jsonGenerator.writeStringField("Id", Long.toString(status.getId()));
		            		if(att[7])
			            		jsonGenerator.writeStringField("Language", status.getLang());
							
							jsonGenerator.writeEndObject();
						} catch (IOException e) {
						e.printStackTrace();
						} 
		            		
		            	if(isNum){	
		            		if (statusNum >= numTweet) {
		            			synchronized (lock) {
		            				lock.notify();
		            			}
		            			System.out.println("unlocked");
		            		}
		            	}
		            	if(isTime){	
		            		if (startTime + deadLine < System.currentTimeMillis()) {
		            			synchronized (lock) {
		            				lock.notify();
		            			}
		            			System.out.println("unlocked");
		            		}
		            	}
		            }

		                @Override
		                public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		                    System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
		                }

		                @Override
		                public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		                    System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
		                }

		                @Override
		                public void onScrubGeo(long userId, long upToStatusId) {
		                    System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
		                }

		                @Override
		                public void onStallWarning(StallWarning warning) {
		                    System.out.println("Got stall warning:" + warning);
		                }

		                @Override
		                public void onException(Exception ex) {
		                    ex.printStackTrace();
		                }
		            };
		            
		            //
		            FilterQuery fq = new FilterQuery();
		            if(!noWord)
		            	fq.track(keyWords);
		            if(!noLocation)
		            	fq.locations(locations);
		            twitterStream.addListener(listener);
		            if(!noWord || !noLocation)
		            	twitterStream.filter(fq);
		            else
		            	twitterStream.sample(); // in case there is no filter just sample from all tweets
		            
		            try {
		                synchronized (lock) {
		                  lock.wait();
		                }
		              } catch (InterruptedException e) {
		                // TODO Auto-generated catch block
		                e.printStackTrace();
		              }
		              System.out.println("returning statuses");
		              twitterStream.shutdown();
		              
		              jsonGenerator.writeEndArray();
		              jsonGenerator.close();
		              sc.close();
	}
}