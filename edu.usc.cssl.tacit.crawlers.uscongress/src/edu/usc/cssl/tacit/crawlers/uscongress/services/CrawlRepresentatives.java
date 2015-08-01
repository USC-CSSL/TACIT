package edu.usc.cssl.tacit.crawlers.uscongress.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawlRepresentatives {

	public static void crawlRepresentatives() throws IOException {
		HashMap<String, String> stateAbbr =  new HashMap<String, String>();
        stateAbbr.put("Alabama","AL");
        stateAbbr.put("Alaska","AK");
        stateAbbr.put("Arizona","AZ");
        stateAbbr.put("Arkansas","AR");
        stateAbbr.put("California","CA");
        stateAbbr.put("Colorado","CO");
        stateAbbr.put("Connecticut","CT");
        stateAbbr.put("Delaware","DE");
        stateAbbr.put("Florida","FL");
        stateAbbr.put("Georgia","GA");
        stateAbbr.put("Hawaii","HI");
        stateAbbr.put("Idaho","ID");
        stateAbbr.put("Illinois","IL");
        stateAbbr.put("Indiana","IN");
        stateAbbr.put("Iowa","IA");
        stateAbbr.put("Kansas","KS");
        stateAbbr.put("Kentucky","KY");
        stateAbbr.put("Louisiana","LA");
        stateAbbr.put("Maine","ME");
        stateAbbr.put("Maryland","MD");
        stateAbbr.put("Massachusetts","MA");
        stateAbbr.put("Michigan","MI");
        stateAbbr.put("Minnesota","MN");
        stateAbbr.put("Mississippi","MS");
        stateAbbr.put("Missouri","MO");
        stateAbbr.put("Montana","MT");
        stateAbbr.put("Nebraska","NE");
        stateAbbr.put("Nevada","NV");
        stateAbbr.put("New Hampshire","NH");
        stateAbbr.put("New Jersey","NJ");
        stateAbbr.put("New Mexico","NM");
        stateAbbr.put("New York","NY");
        stateAbbr.put("North Carolina","NC");
        stateAbbr.put("North Dakota","ND");
        stateAbbr.put("Ohio","OH");
        stateAbbr.put("Oklahoma","OK");
        stateAbbr.put("Oregon","OR");
        stateAbbr.put("Pennsylvania","PA");
        stateAbbr.put("Rhode Island","RI");
        stateAbbr.put("South Carolina","SC");
        stateAbbr.put("South Dakota","SD");
        stateAbbr.put("Tennessee","TN");
        stateAbbr.put("Texas","TX");
        stateAbbr.put("Utah","UT");
        stateAbbr.put("Vermont","VT");
        stateAbbr.put("Virginia","VA");
        stateAbbr.put("Washington","WA");
        stateAbbr.put("West Virginia","WV");
        stateAbbr.put("Wisconsin","WI");
        stateAbbr.put("Wyoming","WY");        
        stateAbbr.put("American Samoa","AS");
        stateAbbr.put("District of Columbia","DC");
        stateAbbr.put("Federated States of Micronesia","FM");
        stateAbbr.put("Guam","GU");
        stateAbbr.put("Marshall Islands","MH");
        stateAbbr.put("Northern Mariana Islands","MP");
        stateAbbr.put("Palau","PW");
        stateAbbr.put("Puerto Rico","PR");
        stateAbbr.put("Virgin Islands","VI"); 		
		Document doc = Jsoup.connect("http://www.house.gov/representatives").timeout(10*1000).get();
		Elements list = doc.getElementsByAttributeValue("id", "byState").select("h2");
		ArrayList<String> states = new ArrayList<String>();
		for(Element item : list) {
			states.add(item.text());
		}
		
		Elements repList = doc.getElementsByAttributeValue("id", "byState").select("table");
		int count = 0;
		for(Element item : repList) {
			for(Element eachItem : item.getElementsByTag("tr")) {
				String name = eachItem.getAllElements().get(2).text();
				String politicalAffiliation = eachItem.getAllElements().get(4).text();
				if(!name.equalsIgnoreCase("Name")) {
					//System.out.println(name + "(" + politicalAffiliation + "-" + stateAbbr.get(states.get(count)) + ")");
					System.out.println("representativeDet.put(\"" + name + "\", \"" + politicalAffiliation + "-" + stateAbbr.get(states.get(count)) +"\");");
				}
			}
			count++;
		}		
	}
	
	public static void main(String[] args) throws IOException {
		crawlRepresentatives();
	}	
}
