package edu.usc.cssl.tacit.crawlers.wikipedia.services;
/**
* This class creates a class for crawling a certain item. 
* It parses from the html file and extracts from it contents.
*
* @author  Yongshun
* @version 1.0
* @since   2016-05-01 
*/

import java.util.ArrayList;

public class Searcher {
	public String keyword;
	public int limit;
	public int number;
	public ArrayList<String> title=new ArrayList<String>();
	
	
	public Searcher(String keyword,int limit) {
		this.keyword=keyword;
		this.limit=limit;
		search();
	}
	
	public Searcher(String keyword) {
		this.keyword=keyword;
		this.limit=10;
		search();
	}
	
	public void search() {
		String endpoint="https://en.wikipedia.org/w/index.php";
		String query="title=Special:Search&limit="+limit+"&offset=0&profile=default&search="+keyword;
		String url=endpoint+"?"+query;
		Crawler c=new Crawler(url);
		String result=c.html();
//		System.out.println(result);
		extract(result);
	}
	
	/*
	 * Parse to get search result names.
	 */
	public void extract(String content) {
		if (!title.isEmpty())
			title.clear();
		int index=0;
		String heading="<div class=\"mw-search-result-heading\">";
		while (content.indexOf(heading, index)!=-1) {
			index=content.indexOf(heading, index);
			index=content.indexOf("title=\"", index);
			String temp=content.substring(index+7,content.indexOf("\"", index+7));
			title.add(temp);
//			System.out.println(temp);
		}
		number=title.size();
	}
	
}
