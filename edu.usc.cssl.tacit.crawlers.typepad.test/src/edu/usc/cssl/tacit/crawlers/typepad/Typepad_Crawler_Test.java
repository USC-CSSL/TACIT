package edu.usc.cssl.tacit.crawlers.typepad;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.usc.cssl.tacit.crawlers.typepad.services.TypePadCrawler;

public class Typepad_Crawler_Test {
	final File testDirectory = new File("TestData");
	Exception exceptionObj = null;
	
	@Test
	public void testGenericCrawl() {
		
		if (!testDirectory.exists()){
			testDirectory.mkdir();
		}
		
		TypePadCrawler typePadCrawler = new TypePadCrawler();
		ArrayList<String> genericKeywords = new ArrayList<String>();
		genericKeywords.add("\"star wars\"");
		genericKeywords.add("galaxy");
	
		try{
			typePadCrawler.getQueryResults(genericKeywords, null, null, 100, 1, testDirectory.getAbsolutePath(), "GenericTestData", new NullProgressMonitor());	
			FileUtils.deleteDirectory(testDirectory);
		}
		catch(Exception e){
			exceptionObj = e;
		}
		
		assertEquals("Checking if any exception occured in generic typepad crawl.", exceptionObj, null);
	}
	
	@Test
	public void testConentAndTitleCrawl() {
		
		if (!testDirectory.exists()){
			testDirectory.mkdir();
		}
			
		TypePadCrawler typePadCrawler = new TypePadCrawler();
		ArrayList<String> titleKeywords = new ArrayList<String>();
		titleKeywords.add("\"star wars\"");
		ArrayList<String> contentKeywords = new ArrayList<String>();
		contentKeywords.add("galaxy");
		
		try{
			typePadCrawler.getQueryResults(null, contentKeywords, titleKeywords, 100, 1, testDirectory.getAbsolutePath(), "ContentTitleTestData", new NullProgressMonitor());	
			FileUtils.deleteDirectory(testDirectory);
		}
		catch(Exception e){
			exceptionObj = e;
		}
		
		assertEquals("Checking if any exception occured in content and title typepad crawl.", exceptionObj, null);
	}
}
