package edu.usc.pil.nlputils.plugins.senatecrawler.process;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SenateCrawler {
	ArrayList<Integer> congresses = new ArrayList<Integer>();
	
	public SenateCrawler() throws IOException{
		getCongresses();
		for (int congress : congresses){
			getSenators(congress);
		}
	}
	
	private void getSenators(int congress) throws IOException {
		System.out.println("Extracting Senators of Congress "+congress);
		Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&n=Record&c="+congress).timeout(10*1000).get();
		Elements senList = doc.getElementsByAttributeValue("name", "SSpeaker").select("option");
		
		for (Element senItem : senList){
			String senText = senItem.text().replace("\u00A0", " ");
			if (senText.contains("Any Senator"))		// We just need the senator names
				continue;
			String senatorName = senText.split("\\(")[0].trim();
			String senatorAttribs = senText.split("\\(")[1].replace(")", "").trim();
			//System.out.println(senatorName+" "+senatorAttribs);
			searchSenateRecord(congress,senText,senatorName,senatorAttribs);
			break;	// Remove to unleash
		}
	}

	private void searchSenateRecord(int congress, String senText,
			String senatorName, String senatorAttribs) throws IOException {
		System.out.println(congress+"-"+senatorName.split(",")[0]+"-"+senatorAttribs);
		doCrawl(congress,senText);
	}

	private void getCongresses() throws IOException {
		Document doc = Jsoup.connect("http://thomas.loc.gov/home/LegislativeData.php?&n=Record").timeout(10*1000).get();
		Elements congList = doc.select("p.nav");
		//System.out.println(congList);
		String[] congressNumbers = congList.text().split(":")[1].split("\\|");
		for (String cNum : congressNumbers){
			cNum = cNum.replaceAll("\u00A0", "");			// Invisible &nbsp; characters
			congresses.add(Integer.parseInt(cNum.trim()));
		}
		System.out.println(congresses);
	}

	public void doCrawl(int congress,String senText) throws IOException{
		Document doc = Jsoup.connect("http://thomas.loc.gov/cgi-bin/thomas2")
				.data("xss","query")		// Important. If removed, "301 Moved permanently" error
				.data("queryr"+congress,"")		// Important. 113 - congress number. Make this auto? If removed, "Database Missing" error
				.data("MaxDocs","2000")
				.data("Stemming","No")
				.data("HSpeaker","")
				.data("SSpeaker",senText)
				.data("member","speaking")	// speaking | all  -- all occurrences
				.data("relation","or")		// or | and  -- when there are multiple speakers in the query
				.data("SenateSection","1")
				//.data("HouseSection","2")
				//.data("ExSection","4")
				//.data("DigestSection","8")
				.data("LBDateSel","")		// "" | 1st | 2nd  -- all sessions, 1st session, 2nd session
				.data("DateFrom","")
				.data("DateTo","")
				.data("sort","Default")		// Default | Date
				.data("submit","SEARCH")
				.userAgent("Mozilla")
				.timeout(10*1000)
				.post();
		Elements links = doc.getElementById("content").getElementsByTag("a");
		
		// Removing unnecessary links
		links.remove(0);
		links.remove(0);
		links.remove(0);
		links.remove(links.size()-1);
		links.remove(links.size()-1);
		links.remove(links.size()-1);
		
		// Process each search result
		for (Element link : links){
			System.out.println("Processing "+link.text());
			Document record = Jsoup.connect("http://thomas.loc.gov"+link.attr("href")).timeout(10*1000).get();
			Elements tabLinks = record.getElementById("content").select("a[href]");
			
			String extractLink="";
			// Find Printer Friendly Display
			for (Element tabLink:tabLinks){
				if (tabLink.text().equals("Printer Friendly Display")){
					extractLink = tabLink.attr("href");
					break;
				}
			}
			
			extract(extractLink);
			//break;
		}
			
	}

	private void extract(String extractLink) throws IOException {
		Document page = Jsoup.connect("http://thomas.loc.gov"+extractLink).timeout(10*1000).get();
		//System.out.println(page.getElementById("container"));
		//System.out.println(page.getElementById("container").text());
		String title = page.getElementById("container").select("b").text();
		StringBuilder content = new StringBuilder();
		Elements lines = page.getElementById("container").select("p");
		for (Element line : lines) {
			content.append(line.text()+"\n");
		}
		System.out.println(extractLink.substring(extractLink.lastIndexOf('/')+2));
		System.out.println(title);
		System.out.println(content.toString());
	}
}
