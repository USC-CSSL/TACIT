package edu.usc.cssl.nlputils.plugins.latincrawler.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.AssertionFailedException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.usc.cssl.nlputils.plugins.latincrawler.process.LatinCrawler;

public class TestLatinCrawler {
	
	LatinCrawler lCrawler; 
	Document doc;
	@Before
	public void setUp() throws Exception {
		
		File input = new File("webpages/latinlibrary.html");
		doc = Jsoup.parse(input, "UTF-8", "http://example.com/");
		lCrawler = new LatinCrawler() {
			@Override
			protected Document retrieveDocumentFromUrl(String url) {
				File input = new File(url);
				try {
					doc = Jsoup.parse(input, null, "http://example.com/");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return doc;
			}
		};
		lCrawler.setOutputDir("outputdir");
	
	}

	@Test(expected = AssertionFailedException.class)
	public void testLatinCrawler() {
		assertEquals(24, lCrawler.getSkipBooks().size());
	}
	
	@Test
	public void testGetAuthorsList() { 
		assertNotNull(lCrawler.getAuthorNames()); 
	}
	
	@Test
	public void testGetBooksByAuthor() throws IOException {
		lCrawler.getBooksByAuthor("Abbo Floriacensis","webpages/abbofloracensis.html");
		assertTrue("Crawl Failed", new File("outputdir/ABBO FLORIACENSIS.txt").exists());
	}
	
	@After
    public void cleanUp() {
	   File testFile = new File("outputdir");
	   File[] files = testFile.listFiles();
	   for (File file : files) {
		   file.delete();
	   }
    }
	
}
