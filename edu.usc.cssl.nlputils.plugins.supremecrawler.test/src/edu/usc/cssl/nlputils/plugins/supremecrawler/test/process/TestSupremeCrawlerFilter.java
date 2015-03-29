package edu.usc.cssl.nlputils.plugins.supremecrawler.test.process;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import edu.usc.cssl.nlputils.plugins.supremeCrawler.process.ISupremeCrawlerConstants;
import edu.usc.cssl.nlputils.plugins.supremeCrawler.process.SupremCrawlerFilter;

public class TestSupremeCrawlerFilter {
    SupremCrawlerFilter supremeFilter;
	@Before
	public void setUp() {
		supremeFilter = new SupremCrawlerFilter(ISupremeCrawlerConstants.CRAWLER_URL){
			@Override
			protected Document parseContentFromUrl(
					String crawlUrl) throws IOException {
				File input = new File("webpages/cases.html");
				return Jsoup.parse(input, "UTF-8", "http://example.com/");
			}
		};
		
	}
	@Test
	public void testCasesFromWebPage() throws IOException {
		List<String> casesList = supremeFilter.filters("cases");
		assertEquals(151,casesList.size());
		
	}

}
