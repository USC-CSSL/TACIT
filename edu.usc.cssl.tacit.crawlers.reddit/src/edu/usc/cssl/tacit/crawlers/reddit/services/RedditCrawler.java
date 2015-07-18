package edu.usc.cssl.tacit.crawlers.reddit.services;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.github.jreddit.utils.restclient.HttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;

public class RedditCrawler {
	RestClient restClient;
	RedditPlugin rp;
	
	public RedditCrawler(String outputDir, int limitLinks, boolean limitComments) {
	    restClient = new HttpRestClient();
	    restClient.setUserAgent("bot/1.0 by name");	
	    rp = new RedditPlugin(restClient, outputDir, limitLinks, limitComments);
	}
	
	public void crawlTrendingData(String trendType) throws IOException, URISyntaxException {
		rp.crawlTrendingPosts(trendType);
	}
	
	public void search(String query, String title, String author, String url, String linkId, String timeFrame, String sortType, ArrayList<String> content) throws IOException, URISyntaxException {
		for(String subreddit : content) {
			String queryString = constructSearchQueryString(query, title, author, url, linkId, subreddit);
			String searchUrl = contructUrl(timeFrame, sortType, queryString);
			rp.crawlQueryResults(searchUrl, subreddit);
		}
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

	private String constructSearchQueryString(String query, String title, String author, String url, String linkId, String subreddit) throws UnsupportedEncodingException {
		String queryString = "";		
		if(!query.isEmpty())
			queryString="text:"+query;
		if(!title.isEmpty()) {
			if(queryString.isEmpty())
				queryString+="title:"+title;
			else
				queryString+=" title:"+title;
		}			
		if(!author.isEmpty()) {
			if(queryString.isEmpty())
				queryString+="author:"+author;
			else
				queryString+=" author:"+author;
		}
		if(!url.isEmpty()) {
			if(queryString.isEmpty())
				queryString+="url:"+url;
			else
				queryString+=" url:"+url;
		}
		if(!linkId.isEmpty()) {
			if(queryString.isEmpty())
				queryString+="fullname:"+linkId;
			else
				queryString+=" fullname:"+linkId;
		}
		if(!subreddit.isEmpty()) {
			if(queryString.isEmpty()) 
				queryString+="subreddit:"+subreddit;
			else
				queryString+=" subreddit:"+subreddit;
		}
		queryString = URLEncoder.encode(queryString, "UTF-8"); 
		return queryString;
	}
	
}
