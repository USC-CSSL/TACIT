package edu.usc.cssl.tacit.crawlers.uscongress.services;

import java.io.IOException;
import java.util.LinkedHashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SearchSenators {
//	public static void main(String args[]) throws IOException {
//		crawl(true,"all","");
//	}

	public static LinkedHashSet<String> crawl(boolean house, String congress, String keyword){
		 LinkedHashSet<String> senatorList = new LinkedHashSet<String>();
		String[] letters = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
				"P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
		String chamberTxt ="";
		if(house){
			chamberTxt = "crHouseMemberRemarks";
		}else{
			chamberTxt = "crSenateMemberRemarks";
		}
		String search = "";
		if(!keyword.equals("")){
			search = ",\"search\":\""+keyword+"\"";
		}
		
		String site = "https://www.congress.gov/search/facets/"+chamberTxt+"?q={\"source\":\"congrecord\",\"congress\":\"" + congress + "\""+search+"}&letter=";
		for (int i = 0; i < 26; i++) {
			Document d = null;
			try {
				d = Jsoup.connect(site+letters[i]).get();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Elements senators = d.getElementsByClass("facetbox-shownrow");
			for (Element senator : senators) {
				String senatorTxt = Jsoup.parse(senator.toString()).text();
				int end = senatorTxt.indexOf("]")+1;
				System.out.println(senatorTxt.substring(0, end));
				senatorList.add(senatorTxt.substring(0, end));
			}
		}
		return senatorList;
	}
}
