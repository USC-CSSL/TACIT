package edu.usc.cssl.tacit.crawlers.gutenberg.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

//import edu.usc.cssl.tacit.crawlers.frontier.services.FrontierConstants;


public class GutenbergMain {
	JsonFactory jsonFactory;
	JsonGenerator jsonGenerator;
	IProgressMonitor monitor;
	
	public void crawl(String dir, String domain, int limit,IProgressMonitor monitor) throws IOException{
		ConsoleView.printlInConsoleln("For Sub Domain: " + domain);
		//System.out.println("I am in crawl");
		this.monitor = monitor;
		//System.out.println("dir------" + dir);
		//System.out.println("domain---" + domain);
		//System.out.println("limit-----" + limit);
		ArrayList<String> temp = new ArrayList<String>();
		int downloadCount = 0;
		jsonFactory = new JsonFactory();
		//this.monitor = monitor;
		File streamFile = new File(dir+File.separator+domain+".json");
		int count =0;
		int downloadedCount =0;
		File f=null;
			System.out.println("I am in while---and count is ----" + count);
		try {
			jsonGenerator = jsonFactory.createGenerator(streamFile, JsonEncoding.UTF8);
			jsonGenerator.useDefaultPrettyPrinter();
			jsonGenerator.writeStartArray();
			
			f = new File(dir+File.separator+domain+".txt");
			//System.out.println("File Name--------" + dir+File.separator+domain+".txt");
			//String domain1= domain.replaceAll("\\s+", "_");
			//String domain2 = domain1.replace("'", "%27");
			String domain2 = GutenbergConstants.site2Link.get(domain);
			//String site = IGutenbergConstants.BASE_URL_DOMAIN + domain2 + "_(Bookshelf)";
			String site = IGutenbergConstants.BASE_URL_DOMAIN + domain2;
			//System.out.println("domain2-----" + domain2);
			System.out.println("site---------" + site);
			Document d = Jsoup.connect(site).timeout(60*1000).get();
			Elements certainlinks = d.select("a[href*=www.gutenberg.org/ebooks/]");
			for (Element table : certainlinks){
				Element a = table.select("a").first();
				String linkStr = a.attr("href");
				//System.out.println(linkStr);
				int lastIndex = linkStr.lastIndexOf('/');
				String s2 = linkStr.substring(lastIndex+1);
				if (s2.matches("[-+]?\\d*\\.?\\d+"))
				{	
					temp.add(s2);
				}	
				//System.out.println(s2);
				
			}
			if(limit>temp.size())
			{
				limit = temp.size();
			}
			//System.out.println("******************************************************");
			//System.out.println(temp);
			//System.out.println("******************************************************");
			jsonGenerator.writeStartObject();
			//System.out.println("Size of temp-------" + temp.size());
			int tempSize = temp.size();
			monitor.worked(1);
			
			
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		while(true){
			if(limit==downloadedCount)
				break;
			try{
				//System.out.println("I am in while-------------||||||||||||||||||||||||||------------------------");
				
				for (int i = downloadCount;downloadedCount<limit;i++)
				{
					System.out.println("i===" + i);
					//System.out.println(downloadCount);
					String numOfebook = temp.get(i);
					//System.out.println(numOfebook);
					String titleSite = IGutenbergConstants.TITLE_BASE_URL + numOfebook + "/" + numOfebook + "-h/" + numOfebook + "-h.htm";
					//System.out.println("TitleSite------" + titleSite);
					Document e = Jsoup.connect(titleSite).timeout(60*1000).get();
					Element title = e.select("title").first();
					//System.out.println("Title--------" + title);
					//ConsoleView.printlInConsoleln("Writing topic: "+ Jsoup.parse(title.toString()).text());
					//jsonGenerator.writeObjectField("title", Jsoup.parse(title.toString()).text());
					String contentSite = IGutenbergConstants.CONTENT_BASE_URL + numOfebook + "/pg" + numOfebook + ".txt";
					System.out.println("contentSite-------" + contentSite);
					Document g = Jsoup.connect(contentSite).timeout(60*1000).get();
					ConsoleView.printlInConsoleln("Writing topic: "+ Jsoup.parse(title.toString()).text());
					jsonGenerator.writeObjectField("title", Jsoup.parse(title.toString()).text());
					jsonGenerator.writeObjectField("abstract_body", Jsoup.parse(g.toString()).text());
					downloadCount++;
					downloadedCount++;
					//System.out.println("DownloadCount=" + downloadCount);
					//System.out.println("DownloadedCount=" + downloadedCount);
				}
				
				jsonGenerator.writeEndObject();
			}catch(HttpStatusException e1){
				if(e1.getStatusCode() == 412 || e1.getStatusCode() == 404)
				{
					//System.out.println("Error Status Code is ----" + e1.getStatusCode());
					downloadCount++;
					//System.out.println("Download Count in catch====" + downloadCount);
					//System.out.println("Downloaded Count in catch====" + downloadedCount );
					continue;
				}
			}catch (IOException e) {
				e.printStackTrace();
			} catch (IndexOutOfBoundsException e)
			{
				if(downloadedCount!=0)
				{
					System.out.println("Some books found according to your search");
				}
				else
				{
					ConsoleView.printlInConsoleln("No books found according to your search.Kindly change your search options or try again later.");
				}
				break;
			}
		}	
		try {
			jsonGenerator.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("Number of Downloads--" + downloadCount);
		ConsoleView.printlInConsoleln(downloadedCount + " book(s) downloaded according to specified search result.");
	}
	
	
	
	
	
	
	
	
	
	
	
	
}