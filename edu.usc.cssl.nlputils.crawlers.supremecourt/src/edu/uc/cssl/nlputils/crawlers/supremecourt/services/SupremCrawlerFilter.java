package edu.uc.cssl.nlputils.crawlers.supremecourt.services;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SupremCrawlerFilter {

	private String crawlerUrl;
	
	public SupremCrawlerFilter(String crawlUrl) {
		this.crawlerUrl = crawlUrl;
	}
	
	protected Document parseContentFromUrl(String crawlUrl) throws IOException {
		
		return Jsoup.connect(crawlUrl.toString()).get();

	}
	public List<String> filters(String segment) throws IOException{
		List<String> filterContents = new ArrayList<>();
			URI crawlUrl = URI.create(this.crawlerUrl+"/"+segment);
			Document doc = parseContentFromUrl(crawlUrl.toString());
			Element itemList = doc.select(".exmenu").get(0);
			Elements items = itemList.select("a");
			filterContents.add("All");
			for (Element element : items) {
				filterContents.add(element.attr("href").trim());
			}
		return filterContents;
	}
}
