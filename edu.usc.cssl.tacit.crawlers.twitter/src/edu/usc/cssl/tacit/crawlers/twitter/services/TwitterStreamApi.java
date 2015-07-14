package edu.usc.cssl.tacit.crawlers.twitter.services;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class TwitterStreamApi {

	private int statusNum = 0;
	private final Object lock = new Object();

	public void stream(String fileName, final boolean isNum,
			final long numTweet, final boolean isTime, final long deadLine,
			final boolean noWord, String keyWords[], final boolean noLocation,
			double[][] locations, final boolean att[],
			final IProgressMonitor monitor) throws IOException {

		final long startTime = System.currentTimeMillis();

		ConfigurationBuilder cb = new ConfigurationBuilder();

		String consumerKey;
		String consumerSecret;
		String accessToken;
		String accessTokenSecret;
		monitor.subTask("Accessing User key");
		consumerKey = CommonUiActivator.getDefault().getPreferenceStore()
				.getString("ckey");
		consumerSecret = CommonUiActivator.getDefault().getPreferenceStore()
				.getString("csecret");
		accessToken = CommonUiActivator.getDefault().getPreferenceStore()
				.getString("accesstoken");
		accessTokenSecret = CommonUiActivator.getDefault().getPreferenceStore()
				.getString("atokensecret");

		cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey)
				.setOAuthConsumerSecret(consumerSecret)
				.setOAuthAccessToken(accessToken)
				.setOAuthAccessTokenSecret(accessTokenSecret);
		monitor.worked(2);
		// Setup configurations
		TwitterStream twitterStream = new TwitterStreamFactory(cb.build())
				.getInstance();

		// Create File
		File streamFile = new File(fileName);

		// Instantiate JSON writer
		JsonFactory jsonfactory = new JsonFactory();
		final JsonGenerator jsonGenerator = jsonfactory.createGenerator(
				streamFile, JsonEncoding.UTF8);
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
				try {
					latitude = status.getGeoLocation().getLatitude();
					sLatitude = latitude.toString();
					longitude = status.getGeoLocation().getLongitude();
					sLongitude = longitude.toString();
				} catch (NullPointerException e) {
					sLatitude = "NULL";
					sLongitude = "NULL";
				}

				monitor.subTask("Crawling Twitter from User : "
						+ status.getUser().getScreenName());
				monitor.worked(1);

				// Append the new status instance to the file
				try {
					jsonGenerator.writeStartObject();
					if (att[0])
						jsonGenerator.writeStringField("Name", status.getUser()
								.getScreenName());
					if (att[1])
						jsonGenerator
								.writeStringField("Text", status.getText());
					if (att[2])
						jsonGenerator.writeStringField("Retweet",
								Integer.toString(status.getRetweetCount()));
					if (att[3]) {
						jsonGenerator.writeStringField("Latitude", sLatitude);
						jsonGenerator.writeStringField("Longitude", sLongitude);
					}
					if (att[4])
						jsonGenerator.writeStringField("CreatedAt", status
								.getCreatedAt().toString());
					if (att[5])
						jsonGenerator.writeStringField("FavCount",
								Integer.toString(status.getFavoriteCount()));
					if (att[6])
						jsonGenerator.writeStringField("Id",
								Long.toString(status.getId()));
					if (att[7])
						jsonGenerator.writeStringField("Language",
								status.getLang());

					jsonGenerator.writeEndObject();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (isNum) {
					if (statusNum >= numTweet) {
						synchronized (lock) {
							lock.notify();
						}
					}
				}
				if (isTime) {
					if (startTime + deadLine < System.currentTimeMillis()) {
						synchronized (lock) {
							lock.notify();
						}
					}
				}

			}

			@Override
			public void onDeletionNotice(
					StatusDeletionNotice statusDeletionNotice) {
				ConsoleView
						.printlInConsoleln("Got a status deletion notice id:"
								+ statusDeletionNotice.getStatusId());
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				ConsoleView.printlInConsoleln("Got track limitation notice:"
						+ numberOfLimitedStatuses);
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				ConsoleView.printlInConsoleln("Got scrub_geo event userId:"
						+ userId + " upToStatusId:" + upToStatusId);
			}

			@Override
			public void onStallWarning(StallWarning warning) {
				ConsoleView.printlInConsoleln("Got stall warning:" + warning);
			}

			@Override
			public void onException(Exception ex) {
				ex.printStackTrace();
			}
		};

		//
		FilterQuery fq = new FilterQuery();
		if (!noWord)
			fq.track(keyWords);
		if (!noLocation)
			fq.locations(locations);
		twitterStream.addListener(listener);
		if (!noWord || !noLocation)
			twitterStream.filter(fq);
		else
			twitterStream.sample(); // in case there is no filter just sample
									// from all tweets

		try {
			synchronized (lock) {
				lock.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ConsoleView.printlInConsoleln("Crawling is completed");
		twitterStream.shutdown();
		monitor.worked(2);
		monitor.subTask("Writing Contents at " + fileName);
		jsonGenerator.writeEndArray();
		monitor.worked(2);
		jsonGenerator.close();
	}
}