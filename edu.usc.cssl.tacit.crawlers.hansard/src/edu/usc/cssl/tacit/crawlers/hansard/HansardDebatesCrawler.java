package edu.usc.cssl.tacit.crawlers.hansard;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class HansardDebatesCrawler {
	static String strDate;
	static String strName;
	static String strTitle;
	static String strMetaData;
	static String strLink;
	static String link = "https://hansard.parliament.uk/search/Debates";
	static Map<String, Integer> map;
	JsonGenerator jsonGenerator;
	JsonFactory jsonfactory;
	String outputDir = "";
	
	
	private void setDir() {
		// Instantiate JSON writer
		String output = outputDir + File.separator + "hansard.json";
		File streamFile = new File(output);
		jsonfactory = new JsonFactory();
		try {
			jsonGenerator = jsonfactory.createGenerator(streamFile, JsonEncoding.UTF8);
			jsonGenerator.useDefaultPrettyPrinter();
			jsonGenerator.writeStartArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		map = new HashMap<String, Integer>();
		map.put("Both", 0);
		map.put("Commons", 1);
		map.put("Lords", 2);
	}

	
	void extractInfo() throws IOException{
		String url = "https://hansard.parliament.uk"+strLink;
		Element t = null;
		String body = "";
		try{
//Grand Committee
																						//110001001
			
		//	System.out.println("Extract Info 1 -------");
			Element ele = Jsoup.connect(url).timeout(120000).get().body().child(1).child(1).child(0).child(0).child(0).child(1);
			
		//	System.out.println("Extract Info 2 -------");
			strTitle = ele.child(1).child(1).child(0).text();
			strDate = ele.child(2).text();
			strMetaData = ele.child(3).text();
			if(ele.child(0).child(0).child(1).text().contains("General Committees"))
				t = ele.child(3).child(0).child(1);
			else if(!ele.child(0).child(0).child(1).text().contains("Public Bill Committees")) {
				t = ele.child(4).child(0).child(1);
			} else {
				t = ele.child(3).child(0).child(1);
			}
			body = "";
			boolean flag = false;
			

			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringField("Title", strTitle); 
			jsonGenerator.writeStringField("Date", strDate); 
			jsonGenerator.writeStringField("MetaData", strMetaData);
			jsonGenerator.writeArrayFieldStart("Body");
			
		//	System.out.println("li size "+t.select("li").size());
			if(t.select("li").size()==0) {
				System.out.println("Hello world ----------------------");
				body = t.text();
				
				jsonGenerator.writeStartObject();
				jsonGenerator.writeStringField("Speaker","NA/Various");
				jsonGenerator.writeStringField("Text", body);
				jsonGenerator.writeEndObject();
				body = "";
			}
			else {

				for(Element x : t.children()){
				/*	if(flag){			//In some cases, the first paragraph is present in a non <li> tag
						body = body + x.text();
						flag = false;
					}
					if(x.tagName().equals("li")) {
		
						String p = x.child(0).text() +"\n";
						for (Element  y : x.getElementsByClass("inner").get(0).child(0).getElementsByTag("p"))
						{
							p = p + y.text();
						}
						if(p.trim().compareTo("")==0){
							flag = true;
						} else{
						}
						body = body + p;
					}*/
					
					
					if(x.tagName().equals("li")) {
		
						String name = x.child(0).text();
						for (Element  y : x.getElementsByClass("inner").get(0).child(0).getElementsByTag("p"))
						{
							body += y.text();
						}
						jsonGenerator.writeStartObject();
						jsonGenerator.writeStringField("Speaker",name);
						jsonGenerator.writeStringField("Text", body);
						jsonGenerator.writeEndObject();
						body = "";
						
					}
					
					
					
				}
			}
			
		}
		catch(Exception e) {

			System.out.println("Catch entered for --- "+strTitle);
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.out.println("T is " + t);
			body = t.text();

			System.out.println("Catch leaving for --- "+strTitle);
		}
		
		System.out.println(strTitle+"---------------------");

		jsonGenerator.writeEndArray();

		jsonGenerator.writeEndObject();
		
	}
	
	public boolean crawlByKeywordSearch(String outputDir, String searchTerm, String house, boolean limitByDate, String startDate, String endDate, int limitRecords, IProgressMonitor monitor) throws IOException{
		System.out.println("start Date -- "+ startDate);
		System.out.println("end Date -- "+ endDate);
		int number = 0;
		this.outputDir = outputDir;
		setDir();
		Elements elements = null;
		Connection conn = Jsoup.connect("https://hansard.parliament.uk/search/Debates").data("SearchTerm",searchTerm).data("HouseFilter",map.get(house)+"");
		if(limitByDate){
			conn = conn.data("StartDate",startDate).data("EndDate",endDate);
		}
		Element e = conn.timeout(120000).post().body().child(1).child(1).child(0).child(0).child(2);
		String s = e.child(0).child(0).child(0).text();
		int numberOfPages = Integer.parseInt(s.substring(s.indexOf("(")+1, s.indexOf(")")).split(" ")[3]);
	//	System.out.println(s);
		int progressMonitorIncrement = 20000/numberOfPages;
		System.out.println("number of pages "+numberOfPages);
	//	System.out.println("Increment "+progressMonitorIncrement);
		elements = e.child(1).children();
		for(Element element:elements) {
	
			try{
				strTitle = element.child(0).child(0).child(0).text();
			//	System.out.println(strTitle);
				strMetaData = element.child(0).child(0).child(1).text();
			//	System.out.println(strMetaData);
				strLink = element.child(0).attr("href");
			//	System.out.println(strLink);
				extractInfo();
				ConsoleView.printlInConsoleln("Writing Debate: "+strTitle);
				number+=1;
				if (number==limitRecords)
					break;
			} catch(Exception exception){
				System.out.println("Exception occurred"+exception.getMessage());
				System.out.println("Exception occurred"+exception.getStackTrace());
				//return false;
			}
		}
		monitor.subTask("Page 1 of " + numberOfPages+" pages crawled");
		monitor.worked(progressMonitorIncrement);
		System.out.println(link);
		String updateLink = link;
		if(limitRecords==-1)
			limitRecords = Integer.MAX_VALUE;
		for(int i=2; i<=numberOfPages&&number<limitRecords; i++)
		{
			System.out.println("Page " + i);
			link = updateLink + "?";
			if(limitByDate){
				link = link + "startDate="+startDate.substring(6)+"-"+startDate.substring(3,5)+"-"+startDate.substring(0,2)+"&";
				link = link + "endDate="+endDate.substring(6)+"-"+endDate.substring(3,5)+"-"+endDate.substring(0,2)+"&";
			}
				if(!house.equals("Both"))
				link = link + "house="+house+"&";
			if(!searchTerm.equals(""))
				link = link + "searchTerm="+searchTerm+"&";
			link = link + "page="+i+"&";
			if(link.endsWith("&"))
			{
				link = link.substring(0, link.length()-1);
			}
			conn = Jsoup.connect(link).timeout(120000);
			e = conn.get().body().child(1).child(1).child(0).child(0).child(2);
			
			//System.out.println(e);
			System.out.println(link);
			for(Element element:e.child(1).children()) {
				try{
					strTitle = element.child(0).child(0).child(0).text();
					strMetaData = element.child(0).child(0).child(1).text();
					strLink = element.child(0).attr("href");
					extractInfo();
					ConsoleView.printlInConsoleln("Writing Debate: "+strTitle);
					number +=1;
				} catch(Exception exception){
					System.out.println("Exception occurred");
				}
			}
			monitor.subTask("Page "+i+" of " + numberOfPages+" pages crawled");
			monitor.worked(progressMonitorIncrement);
		}
		
		try {
			jsonGenerator.writeEndArray();
			jsonGenerator.flush();
			jsonGenerator.close();
		} catch (IOException exception) {
			exception.printStackTrace();
		}

		ConsoleView.printlInConsoleln(number + " debate(s) downloaded.");
		return true;
	}
	
	public boolean crawlByHouseMemberSearch(String outputDirectory, List<String> links, int limitRecords, IProgressMonitor monitor) throws IOException {
		
		System.out.println("Limit value -------- "+limitRecords);
		System.out.println("Links to pursue -------- "+links);
		this.outputDir = outputDirectory;
		setDir();
		for(String link:links) {
			//https://hansard.parliament.uk/search/MemberContributions?memberId=3898&type=Spoken
			//https://hansard.parliament.uk/search/MemberContributions?memberId=3468&type=Spoken
			int count = 0;
			
			Connection conn = Jsoup.connect("https://hansard.parliament.uk"+link);
			Element e = conn.timeout(120000).get().body().child(1).child(1).child(0).child(0).child(3).child(2).child(0);
	//		System.out.println(e);
			String s = e.child(1).child(0).child(0).text();
			int numberOfPages = Integer.parseInt(s.substring(s.indexOf("(")+1, s.indexOf(")")).split(" ")[3]);
			int progressMonitorIncrement = 20000/numberOfPages;
			
			for(Element xx: e.child(2).children()) {
				System.out.println("Entered --");
				System.out.println(xx);
				strTitle = xx.child(0).child(0).child(0).text();
				strDate = xx.child(0).child(0).child(1).child(0).text();
				String line = xx.child(0).attr("href");
				conn = Jsoup.connect("https://hansard.parliament.uk"+line);
				Element et = conn.timeout(12000).get().body();
				
				if(line.indexOf('#')!=-1) {
					Element t = et.getElementById(line.substring(line.indexOf('#')+1));
					strName = t.child(0).text();
					String body = t.child(3).child(0).text();
					strMetaData = et.child(1).child(1).child(0).child(0).child(0).child(1).child(3).text();
					if(strMetaData.length()>20)
						strMetaData = "NA";
					System.out.println("System.out.println(strMetaData);");
					System.out.println(strMetaData);

					ConsoleView.printlInConsoleln("Writing Member Dialogue: "+strTitle);
					
					jsonGenerator.writeStartObject();
					jsonGenerator.writeStringField("Title", strTitle); 
					jsonGenerator.writeStringField("Date", strDate); 
					jsonGenerator.writeStringField("MetaData", strMetaData);
					jsonGenerator.writeStringField("Body", strName+": "+body);
					jsonGenerator.writeEndObject();
					
					count+=1;
					if(count==limitRecords)
						break;
				}
			}
			
			monitor.subTask("Page 1 of " + numberOfPages+" pages crawled");
			monitor.worked(progressMonitorIncrement);
			
			String finalLink = link;

			for(int i = 2; i <= numberOfPages && count<limitRecords; i++) {

				finalLink = "https://hansard.parliament.uk" + link + "&page="+i;
				
				conn = Jsoup.connect(finalLink);
				e = conn.timeout(120000).get().body().child(1).child(1).child(0).child(0).child(3).child(2).child(0);
				
				for(Element xx: e.child(2).children()) {
					
					strTitle = xx.child(0).child(0).child(0).text();
					strDate = xx.child(2).text();
					String line = xx.child(0).attr("href");
					conn = Jsoup.connect("https://hansard.parliament.uk"+line);
					Element et = conn.timeout(12000).get().body();

					ConsoleView.printlInConsoleln("Writing Member Dialogue: "+strTitle);
					
					if(line.indexOf('#')!=-1) {
						Element t = et.getElementById(line.substring(line.indexOf('#')+1));
						strName = t.child(0).text();
						String body = t.child(3).child(0).text();
						strMetaData = et.child(1).child(1).child(0).child(0).child(0).child(1).child(3).text();
						if(strMetaData.length()>20)
							strMetaData = "NA";
						jsonGenerator.writeStartObject();
						jsonGenerator.writeStringField("Title", strTitle); 
						jsonGenerator.writeStringField("Date", strDate); 
						jsonGenerator.writeStringField("MetaData", strMetaData);
						jsonGenerator.writeStringField("Body", strName+": "+body);
						jsonGenerator.writeEndObject();
						
						count+=1;
					}
				}
				monitor.subTask("Page "+i+" of " + numberOfPages+" pages crawled");
				monitor.worked(progressMonitorIncrement);
			}
			try {
				jsonGenerator.writeEndArray();
				jsonGenerator.flush();
				jsonGenerator.close();
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
		return true;
	}
	public static HashMap<String, String> crawlMPs(String searchTerm, String house, String currentFormerFilter) throws IOException{
		
		HashMap<String, String> map = new HashMap<String, String>();
		if(house.contains("Both"))
			house = "0";
		else if(house.contains("Lords"))
			house = "2";
		else
			house = "1";
		Connection conn = Jsoup.connect("https://hansard.parliament.uk/search/Members").data("CurrentFormerFilter",currentFormerFilter).data("HouseFilter","0").data("SearchTerm",searchTerm);
		Element e = conn.timeout(120000).post().body().child(1).child(1).child(0).child(0);
		String s = e.child(2).child(0).child(0).child(0).text();
		System.out.println("Total Pages in string " + s);
		int numberOfPages = Integer.parseInt(s.substring(s.indexOf("(")+1, s.indexOf(")")).split(" ")[3]);
		System.out.println("Total Pages " + numberOfPages);
		for(Element x: e.child(2).child(1).children()){
			map.put(x.child(0).child(0).child(0).child(0).text(), x.child(0).attr("href"));
		}
		
		String link, updateLink = "https://hansard.parliament.uk/search/Members";

		for(int i=2; i<=numberOfPages; i++)
		{
			System.out.println("Page " + i);
			link = updateLink + "?" +"currentFormerFilter="+currentFormerFilter+"&";
			if(!house.equals("Both"))
				link = link + "house="+house+"&";
			if(!searchTerm.equals(""))
				link = link + "searchTerm="+searchTerm+"&";
			link = link + "page="+i+"&";
			if(link.endsWith("&"))
			{
				link = link.substring(0, link.length()-1);
			}
			
			conn = Jsoup.connect(link);
			e = conn.timeout(120000).get().body().child(1).child(1).child(0).child(0).child(2).child(1);
			
			for(Element x: e.children()){
				map.put(x.child(0).child(0).child(0).child(0).text(), x.child(0).attr("href"));
				
			}
		}
		
		return map;
	}
	
}
