package edu.usc.cssl.tacit.crawlers.plosone;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.usc.cssl.tacit.crawlers.plosone.services.PLOSOneCrawler;
import edu.usc.cssl.tacit.crawlers.plosone.services.PLOSOneWebConstants;

public class Plosone_Crawler_Test {
	final File testDirectory = new File("TestData");
	Exception exceptionObj = null;
	
	@Test
	public void testContentCrawl() {
		
		if (!testDirectory.exists()){
			testDirectory.mkdir();
		}
	
		PLOSOneCrawler plosOneCrawler = new PLOSOneCrawler();
		
		Map<String,String> urlFeatures = new HashMap<String,String>();
		
		urlFeatures.put(PLOSOneWebConstants.FEATURE_APIKEY, "f8529044-95lb-4d15-8d70bf8d9f86cd57");
		urlFeatures.put(PLOSOneWebConstants.FEATURE_DOCTYPE, "json");
		urlFeatures.put(PLOSOneWebConstants.FEATURE_FIELDS, "title,author,score");
		urlFeatures.put(PLOSOneWebConstants.FEATURE_FILTER_QUERY, "doc_type:full");
		urlFeatures.put(PLOSOneWebConstants.FEATURE_QUERY, plosOneCrawler.getModifiedQuery(PLOSOneWebConstants.FIELD_EVERYTHING ,"computational biology;dna"));
		urlFeatures.put(PLOSOneWebConstants.FEATURE_ROWS,"100"); 
		urlFeatures.put(PLOSOneWebConstants.FEATURE_START, "0");

		exceptionObj = null;
		try {
			plosOneCrawler.invokePlosOneCrawler(urlFeatures, 201, testDirectory.getAbsolutePath(), "TestContentData", new NullProgressMonitor());
			FileUtils.deleteDirectory(testDirectory);
		} catch (Exception e) {
			exceptionObj = e;
		}
		assertEquals("Checking if any exception occured in content plosone crawl." ,exceptionObj, null);
	}
	
	
	@Test
	public void testAuthorCrawl() {
		
		if (!testDirectory.exists()){
			testDirectory.mkdir();
		}
	
		PLOSOneCrawler plosOneCrawler = new PLOSOneCrawler();
		
		Map<String,String> urlFeatures = new HashMap<String,String>();
		
		urlFeatures.put(PLOSOneWebConstants.FEATURE_APIKEY, "f8529044-95lb-4d15-8d70bf8d9f86cd57");
		urlFeatures.put(PLOSOneWebConstants.FEATURE_DOCTYPE, "json");
		urlFeatures.put(PLOSOneWebConstants.FEATURE_FIELDS, "title,author,score");
		urlFeatures.put(PLOSOneWebConstants.FEATURE_FILTER_QUERY, "doc_type:full");
		urlFeatures.put(PLOSOneWebConstants.FEATURE_QUERY, plosOneCrawler.getModifiedQuery(PLOSOneWebConstants.FIELD_AUTHOR ,"paul;justin"));
		urlFeatures.put(PLOSOneWebConstants.FEATURE_ROWS,"100"); 
		urlFeatures.put(PLOSOneWebConstants.FEATURE_START, "0");

		exceptionObj = null;
		try {
			plosOneCrawler.invokePlosOneCrawler(urlFeatures, 201, testDirectory.getAbsolutePath(), "TestContentData", new NullProgressMonitor());
			FileUtils.deleteDirectory(testDirectory);
		} catch (Exception e) {
			exceptionObj = e;
		}
		assertEquals("Checking if any exception occured in author plosone crawl." ,exceptionObj, null);
	}
}
