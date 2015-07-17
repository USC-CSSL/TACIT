package edu.usc.cssl.tacit.crawlers.reddit.services;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import com.github.jreddit.utils.restclient.HttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;

public class RedditCrawler {
	RestClient restClient;
	RedditPlugin rp;
	
	public RedditCrawler(String outputDir, int limitLinks, boolean limitComments, ArrayList<String> subreddits) {
	    restClient = new HttpRestClient();
	    restClient.setUserAgent("bot/1.0 by name");	
	    rp = new RedditPlugin(restClient, outputDir, limitLinks, limitComments, subreddits);
	}
	
	public void crawlTrendingData(String trendType) throws IOException, URISyntaxException {
		rp.crawlTrendingPosts(trendType);
	}
	
	public void search(String query, String title, String author, String url, String linkId, String timeFrame, String sortType) throws IOException, URISyntaxException {
		String queryString = constructSearchQueryString(query, title, author, url, linkId);
		String searchUrl = contructUrl(timeFrame, sortType, queryString);
		System.out.println("Query :" + searchUrl);
		rp.crawlQueryResults(searchUrl);
	}

	public void crawlLabeledData(String label, String timeFrame) throws IOException, URISyntaxException {
		String url = "/".concat(label).concat("/.json?t=").concat(timeFrame);
		rp.crawlLabeledPosts(url, label);		
	}
	

	private String contructUrl(String timeFrame, String sortType, String queryString) {
		String url = "";
		if(null!= timeFrame && !timeFrame.isEmpty())
			url="t="+timeFrame;
		if(null!=sortType && !sortType.isEmpty())
			if(!url.isEmpty())
				url+="&sort="+sortType;
			else
				url="sort="+sortType;				
		if(null!= queryString && !queryString.isEmpty())
			if(!url.isEmpty())
				url+="&q="+queryString;
			else
				url="q="+queryString;		
		return url;
	}

	private String constructSearchQueryString(String query, String title, String author, String url, String linkId) {
		String queryString = "";		
		if(null != query && !query.isEmpty())
			queryString+="text:"+query;
		if(null != title && !title.isEmpty())
			queryString+="title:"+title;
		if(null != author && !author.isEmpty())
			queryString+="author:"+author;
		if(null != url && !url.isEmpty())
			queryString+="url:"+url;
		if(null != linkId && !linkId.isEmpty())
			queryString+="fullname:"+linkId;		
		return queryString;
	}
	
}
