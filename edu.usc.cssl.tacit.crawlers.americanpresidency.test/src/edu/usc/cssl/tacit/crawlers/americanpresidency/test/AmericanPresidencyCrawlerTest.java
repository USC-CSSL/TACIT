package edu.usc.cssl.tacit.crawlers.americanpresidency.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.usc.cssl.tacit.crawlers.americanpresidency.services.AmericanPresidencyCrawler;

public class AmericanPresidencyCrawlerTest {
	
	@Test
	public void SBPresidentialPapersCrawlerTest(){
		AmericanPresidencyCrawler crawler = new AmericanPresidencyCrawler();
		String outputDir = "Test";
		File temp = new File(outputDir);
		temp.mkdir();
		Exception exceptionObj = null;
		
		try {
			crawler.crawlSearch(outputDir, "Obama", "", "AND", null, null, "", "", 10, new NullProgressMonitor());
		} catch (IOException e) {
			e.printStackTrace();
			exceptionObj = e;
		}
		assertEquals("Checking if the crawling completed successfully", exceptionObj, null);
		try {
			FileUtils.deleteDirectory(temp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
