package edu.usc.cssl.tacit.crawlers.stackexchange;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.usc.cssl.tacit.crawlers.stackexchange.services.StackConstants;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.StackExchangeApi;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.StackExchangeCrawler;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.StackExchangeSite;

public class Stackexchange_Crawler_Test {
	final String directoryPath = new File("TestData").getAbsolutePath();
	@Test
	public void test() {
		Exception exceptionObj = null;
		String outputDir = directoryPath + System.getProperty("file.separator") +  "Test";
		File temp = new File(outputDir);
		temp.mkdir();
		try{
			StackExchangeCrawler crawler = new StackExchangeCrawler("6Xk6jRz2SrEBLRBnOhhSIw((");
			StackExchangeSite scs = crawler.stackoverflow(new StackExchangeApi(), null);
			long from = 642478742;
			long to = 1463019542;
			boolean jsonFilter[] = {true, true, true, true, true, true, true, true};
			crawler.setDir(outputDir);
			crawler.search("NLP",1,"TestCorpus",scs,"stackoverflow", from, to, jsonFilter, 1, 1, "activity", new NullProgressMonitor());
			FileUtils.deleteDirectory(new File(outputDir));
		}
		catch (Exception e){
			exceptionObj = e;
		}
		assertEquals("Checking if any exception occurred",null, exceptionObj);
		
	}

}
