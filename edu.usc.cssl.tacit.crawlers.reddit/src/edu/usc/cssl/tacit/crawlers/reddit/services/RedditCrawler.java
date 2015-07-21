package edu.usc.cssl.tacit.crawlers.reddit.services;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;

import com.github.jreddit.utils.restclient.HttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;

public class RedditCrawler {
	RestClient restClient;
	RedditPlugin rp;
	IProgressMonitor monitor;
	
	/*
	 * create object for reddit plugin
	 */
	public RedditCrawler(String outputDir, int limitLinks, boolean limitComments, IProgressMonitor monitor) {
	    restClient = new HttpRestClient();
	    restClient.setUserAgent("bot/1.0 by name");	
	    this.monitor = monitor;
	    rp = new RedditPlugin(restClient, outputDir, limitLinks, limitComments, monitor);
	}
	
	public void crawlTrendingData(String trendType) throws IOException, URISyntaxException {
		monitor.worked(5);
		rp.crawlTrendingPosts(trendType);
	}
	
	public void search(String query, String title, String author, String url, String linkId, String timeFrame, String sortType, ArrayList<String> content) throws IOException, URISyntaxException {
		for(String subreddit : content) {			
			String queryString = constructSearchQueryString(query, title, author, url, linkId, subreddit);
			monitor.worked(1);
			String searchUrl = contructUrl(timeFrame, sortType, queryString);
			monitor.worked(1);
			rp.crawlQueryResults(searchUrl, subreddit);
			monitor.worked(10);
		}
		
		if(content.size() == 0) { // no subreddit specified
			String queryString = constructSearchQueryString(query, title, author, url, linkId, null);
			monitor.worked(1);
			String searchUrl = contructUrl(timeFrame, sortType, queryString);
			monitor.worked(1);
			rp.crawlQueryResults(searchUrl, null);
			monitor.worked(10);			
		}
	}

	public void crawlLabeledData(String label, String timeFrame) throws IOException, URISyntaxException {
		monitor.worked(5);
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
		if(null != query && !query.isEmpty())
			queryString="text:"+query;
		if(null != title && !title.isEmpty()) {
			if(queryString.isEmpty())
				queryString+="title:"+title;
			else
				queryString+=" title:"+title;
		}			
		if(null != author && !author.isEmpty()) {
			if(queryString.isEmpty())
				queryString+="author:"+author;
			else
				queryString+=" author:"+author;
		}
		if(null != url && !url.isEmpty()) {
			if(queryString.isEmpty())
				queryString+="url:"+url;
			else
				queryString+=" url:"+url;
		}
		if(null != linkId && !linkId.isEmpty()) {
			if(queryString.isEmpty())
				queryString+="fullname:"+linkId;
			else
				queryString+=" fullname:"+linkId;
		}
		if(null != subreddit && !subreddit.isEmpty()) {
			if(queryString.isEmpty()) 
				queryString+="subreddit:"+subreddit;
			else
				queryString+=" subreddit:"+subreddit;
		}
		queryString = URLEncoder.encode(queryString, "UTF-8"); 
		return queryString;
	}
	
}
