package edu.usc.cssl.tacit.crawlers.wikipedia.services;
/**
* This class use MediaWiki's API to get the categories of a item.  
* It uses official end point "https://en.wikipedia.org/w/api.php". 
* All information about MediaWiki could be found at 
* "https://www.mediawiki.org/wiki/MediaWiki"
*
* @author  Yongshun
* @version 1.0
* @since   2016-05-01 
*/

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Categorizer {
	public String title;
	public int limit;
	public int number;
	public ArrayList<String> category=new ArrayList<String>();	//the categories extracted from Json result.
	boolean valid;
	
	public Categorizer(String title,int limit) {
		this.title=title;
		this.limit=limit;
		categorize();
	}
	
	public Categorizer(String title) {
		this.title=title;
		this.limit=10;
		categorize();
	}
	
	public void categorize() {
		String endpoint="https://en.wikipedia.org/w/api.php";	//end-point for Wikipedia Api
		String query="action=query&prop=categories&redirects=1&titles="+title+"&cllimit="+Integer.toString(limit);
		String url=endpoint+"?"+query;
		Crawler c=new Crawler(url);
		String result=c.text();
//		System.out.println(result);
		valid=false;
		extract(result);
	}
	/*
	 * extract from Json format result.
	 */
	public void extract(String content) {
		if (!category.isEmpty())
			category.clear();
		Pattern p=Pattern.compile("\"categories\":\\s\\[[^]]+\\]");
		Matcher m=p.matcher(content);
		while (m.find()) {
			valid=true;
			String s=m.group();
			Pattern pp=Pattern.compile("\"title\":\\s\"[^\"]+\"");
			Matcher mm=pp.matcher(s);
			while (mm.find()) {
				String ss=mm.group();
				int k2=ss.length()-2,k1=ss.substring(0, k2).lastIndexOf('\"')+1;
				ss=ss.substring(k1, k2+1);
				category.add(ss);
//				System.out.println(ss);
			}
//			System.out.println(s);
		}
		number=category.size();
	}
	
	
}
