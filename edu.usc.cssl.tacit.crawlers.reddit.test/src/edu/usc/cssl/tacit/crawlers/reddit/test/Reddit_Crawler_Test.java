package edu.usc.cssl.tacit.crawlers.reddit.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CMDataType;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.Corpus;
import edu.usc.cssl.tacit.crawlers.reddit.services.RedditCrawler;

public class Reddit_Crawler_Test{

	final String directoryPath = new File("TestData").getAbsolutePath();
	
	@Test
	public void redditTrendingTest() throws IOException {
		Exception exceptionObject = null;
		try {
			Corpus redditCorpus = new Corpus("Test", CMDataType.REDDIT_JSON);
			final RedditCrawler rc = new RedditCrawler(directoryPath, 2, 5, new NullProgressMonitor());
			rc.crawlTrendingData("hot", redditCorpus);
			FileUtils.deleteDirectory(new File(directoryPath + System.getProperty("file.separator") +  "hot"));
		}
		catch (Exception e){
			exceptionObject = e;
		}
		assertEquals("Checking if the crawling completed successfully", exceptionObject, null);
	}
	
	@Test
	public void redditSearchTest() throws IOException {
		Exception exceptionObject = null;
		try {
			Corpus redditCorpus = new Corpus("Test", CMDataType.REDDIT_JSON);
			File temp = new File(directoryPath+ System.getProperty("file.separator") +  "Test");
			temp.mkdir();
			final RedditCrawler rc = new RedditCrawler(directoryPath+ System.getProperty("file.separator") +  "Test", 2, 5, new NullProgressMonitor());
			ArrayList<String> content = new ArrayList<String>();
			rc.search("cats", "", "", "", "", "hour", "relevance", content, redditCorpus, "Test");
			FileUtils.deleteDirectory(new File(directoryPath + System.getProperty("file.separator") +  "Test"));
		}
		catch (Exception e) {
			exceptionObject = e;
		}
		assertEquals("Checking if the crawling completed successfully", null, exceptionObject);
	}
	@Test
	public void redditLabeledDataCrawlTest() throws IOException {
		Exception exceptionObject = null;
		try {
			Corpus redditCorpus = new Corpus("Test", CMDataType.REDDIT_JSON);
			File temp = new File(directoryPath+ System.getProperty("file.separator") +  "Test");
			temp.mkdir();
			final RedditCrawler rc = new RedditCrawler(directoryPath+ System.getProperty("file.separator") +  "Test", 2, 5, new NullProgressMonitor());
			rc.crawlLabeledData("top", "hour", redditCorpus);
			FileUtils.deleteDirectory(new File(directoryPath + System.getProperty("file.separator") +  "Test"));
		}
		catch (Exception e) {
			exceptionObject = e;
		}
		assertEquals("Checking if the crawling completed successfully", null, exceptionObject);
	}
}