package edu.usc.cssl.tacit.crawlers.twitter.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.crawlers.twitter.services.TwitterStreamApi;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class Twitter_Crawler_Test{
	final TwitterStreamApi ttStream = new TwitterStreamApi(){
		protected Configuration twitterConfigurationGenerator(){
			ConfigurationBuilder cb = new ConfigurationBuilder();
			String consumerKey;
			String consumerSecret;
			String accessToken;
			String accessTokenSecret;
			consumerKey = "fz1N1p3dP7Bw7q9NMt498DpPx";
			consumerSecret = "Ruk0T6Zgi7qWAnCfbK4sUIA4dWy3UvPM22jggmcajbZCzxPw5b";
			accessToken = "727637707598368768-hfAWJtJpxFSAPqbhNewN69czvIFKjVt";
			accessTokenSecret = "AjC95RI9k4NRfXp9fKxVF3a1J4kO7mKVIkb3TgdgoZqEY";
			cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret)
					.setOAuthAccessToken(accessToken).setOAuthAccessTokenSecret(accessTokenSecret);
			return cb.build();
		}
	};
	final String directoryPath = new File("TestData").getAbsolutePath();
	@Test
	public void twitterGeoFilterCrawlTest(){
		
		String fileName = "Test.txt";
		
		String outputFile = directoryPath + System.getProperty("file.separator") +  fileName;
		Exception exceptionObj = null;
		double geoLocations[][] = {{-118.442, 33.72}, {-117.86, 34.12}};
		boolean attributes[] = {true, true, true, true, true, true, true, true};
		try {
			ttStream.stream(outputFile, true, 5, true, 60000,
					true, null, false, geoLocations, attributes, new NullProgressMonitor(), null);
			new File(outputFile).delete(); 
		}
		catch (Exception e){
			exceptionObj = e;
		}
		assertEquals("Checking if the crawling completed successfully", exceptionObj, null);
	}
	@Test
	public void twitterRandomFilterCrawlTest(){
		
		String fileName = "Test.txt";
		
		String outputFile = directoryPath + System.getProperty("file.separator") +  fileName;
		Exception exceptionObj = null;
		boolean attributes[] = {true, true, true, true, false, true, true, true};
		try {
			ttStream.stream(outputFile, true, 5, false, 0,
					true, null, true, null, attributes, new NullProgressMonitor(), null);
			 new File(outputFile).delete(); 
		}
		catch (Exception e){
			exceptionObj = e;
		}
		assertEquals("Checking if the crawling completed successfully", exceptionObj, null);
	}	
	@Test
	public void twitterWordFilterCrawlTest(){
		
		String fileName = "Test.txt";

		String outputFile = directoryPath + System.getProperty("file.separator") +  fileName;
		Exception exceptionObj = null;
		boolean attributes[] = {true, true, true, true, true, true, true, true};
		String keyWords[] = {"NLP"};
		try {
			ttStream.stream(outputFile, true, 5, true, 6000,
					false, keyWords, true, null, attributes, new NullProgressMonitor(), null);
			new File(outputFile).delete(); 
		}
		catch (Exception e){
			exceptionObj = e;
		}
		assertEquals("Checking if the crawling completed successfully", exceptionObj, null);
	}
}