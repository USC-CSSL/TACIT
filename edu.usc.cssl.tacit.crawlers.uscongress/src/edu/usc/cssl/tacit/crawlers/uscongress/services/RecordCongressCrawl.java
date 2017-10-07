package edu.usc.cssl.tacit.crawlers.uscongress.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class RecordCongressCrawl {
	 JsonGenerator jsonGenerator;
	 JsonFactory jsonfactory;
	 int docCount; //this is used for parsing the web site links while crawling
	 int docsSavedCount; //this is used to maintain the counts of successful crawls of texts
	 int totalCount = 0; //same as docsSavedCount, but stores the total count
	 boolean first =true;
//	
//	public static void main(String args[]){
//	 try {
//		crawl(null, 10, "", "", "114", false);
//	} catch (IOException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
// }
// 
 public void crawl(String outputDir, int limit, String member, String search, String chamber, String congress, boolean random, IProgressMonitor monitor, boolean[] fields) throws IOException{
	 		first =true;
	 		File streamFile = new File(outputDir+File.separator+"crawl.json"); 
			jsonfactory = new JsonFactory();
			jsonGenerator = jsonfactory.createGenerator(streamFile, JsonEncoding.UTF8);
			jsonGenerator.useDefaultPrettyPrinter();
			jsonGenerator.writeStartArray();
			String houseRemark = "";
			String chamberTxt = "";
			if(!member.equals("") && !member.equals("All")){
				member = member.replaceAll(" ", "+");
				if(chamber.equals("House"))
					houseRemark = ",\"crHouseMemberRemarks\":\""+member+"\"";
				else
					houseRemark = ",\"crSenateMemberRemarks\":\""+member+"\"";
			}
			String query = "";
			if(!search.equals("")){
				query = ",\"search\":\""+search+"\"";
			}
			if(!chamber.equals("")){
				chamberTxt = ",\"chamber\":\""+chamber+"\"";
			}
			docCount= 0;
			docsSavedCount = 0;
			int page = 1;
			int totalPages = 0;//used only when random is selected
			int totalDatedFiles = 0;//used when sort by date is selected
			
			
			BufferedWriter bw;
			Document d = null;
			boolean connected = false;
			HashSet<String> set = new HashSet<String>();
			tag: while (true) {
				connected = false;
				if(random)
					page = (int) (Math.random()*25);
				String site = "https://www.congress.gov/search?q={\"source\":\"congrecord\",\"congress\":\"" + congress + "\""+ houseRemark + query + chamberTxt+"}&pageSize=25&page=" + page;

				System.out.println(site);
				while (!connected) {
					try {
						d = Jsoup.connect(site).timeout(50000).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
							     .get();
						connected = true;
					}
					catch (Exception e) {
						System.out.println(e);
						continue;
					}
				}
				
				if(first){
					
				Elements number = d.getElementsByClass("results-number");
				String num = Jsoup.parse(number.toString()).text();
				System.out.println(num);
				if(num.equals(""))
					return;
				System.out.println("This is "+ num.substring(num.lastIndexOf("of")+3).replaceAll(",", ""));
				int results = Integer.parseInt(num.substring(num.lastIndexOf("of")+3).replaceAll(",", ""));
				System.out.println(num+"------"+results);
				if(limit == -1)
					limit = results;
				first = false;
				}
				Element t = d.getElementsByClass("results-number").get(0);
				String s = t.text().trim();
				String st[] = s.split(" ");
				totalDatedFiles = Integer.parseInt(st[st.length-1].replaceAll(",",""));
				
				Elements title = d.getElementsByClass("result-heading");
				for (Element links : title) {
					Elements link = links.select("a");
					String data = link.toString();
					System.out.println(data);
					int start = data.indexOf("=\"");
					int end = data.indexOf("\">");
					Elements docJournalAbstract = null;
					System.out.print(data+"00000000");
					if(data.contains("house-bill"))
						break tag;
					String contentLink = data.substring(start + 2, end-1);
					if(set.contains(contentLink))
						continue;
					set.add(contentLink);
					System.out.println(contentLink);
					String dateText;
					Document doc;
						try {
							doc = Jsoup.connect(contentLink).timeout(4000).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
								     .get();
							docJournalAbstract =  doc.getElementsByClass("txt-box");
							Elements dates = doc.getElementsByClass("wrapper_std");
							jsonGenerator.writeStartObject();
							for(Element date: dates){
								dateText = Jsoup.parse(date.child(1).select("span").toString()).text();
								int endIndex = dateText.indexOf("-");
								if(fields[0])
									jsonGenerator.writeStringField("house", dateText.substring(1, endIndex-1));
								if(fields[1])
									jsonGenerator.writeStringField("date", dateText.substring(endIndex+1,dateText.length()-1));
								String title1 = (date.child(1)).toString().substring(4,date.child(1).toString().indexOf("<br"));
								if(fields[2])
									jsonGenerator.writeStringField("title", title1);
								ConsoleView.printlInConsoleln("Writing record "+title1+"count "+docCount);
								monitor.subTask("Writing record "+title1+", total count: "+totalCount);
							}
							System.out.println(Jsoup.parse(docJournalAbstract.toString()).text());
							if(fields[3])
								jsonGenerator.writeStringField("body", Jsoup.parse(docJournalAbstract.toString()).text());
							jsonGenerator.writeEndObject();
						} catch (SocketTimeoutException e) {
							docCount++;
							//totalCount++;
							continue;
						}catch (Exception e) {
							docCount++;
							//totalCount++;
							continue; 
						}
					docCount++;
					docsSavedCount++;
					totalCount++;
					monitor.worked(1);
					
					if (docCount >= limit){					
						break;
					}
				}
				if(random)
					page = (int) (Math.random()*totalPages+1);
				else {
					page++;
					if(totalCount>=totalDatedFiles-2)
						break;
				}
				if (docsSavedCount >= limit)
					break;
			}
			ConsoleView.printlInConsoleln(docCount+ "file(s) Downloaded ");
			jsonGenerator.writeEndArray();
			jsonGenerator.flush();
			jsonGenerator.close();
		
 }
}
