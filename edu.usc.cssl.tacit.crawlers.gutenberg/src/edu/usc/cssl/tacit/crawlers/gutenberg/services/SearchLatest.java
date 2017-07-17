package edu.usc.cssl.tacit.crawlers.gutenberg.services;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.select.*;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;

public class SearchLatest {
	JsonFactory jsonFactory;
	JsonGenerator jsonGenerator;
	IProgressMonitor monitor;
	public int nextpageindex=26;
	public int lastbook = 26;

	public void latest(String dir,int limit,IProgressMonitor monitor) throws IOException {
		
		ArrayList<String> temp = new ArrayList<String>();
		int downloadCount = 0;
		int downloadedCount =0;
		this.monitor =monitor;
		jsonFactory = new JsonFactory();
		File streamFile = new File(dir+File.separator+ "Latest Search"+".json");
		try {
			jsonGenerator = jsonFactory.createGenerator(streamFile, JsonEncoding.UTF8);
			jsonGenerator.useDefaultPrettyPrinter();
			jsonGenerator.writeStartArray();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		File f=null;
		try
		{
		f = new File(dir+File.separator+  "Latest Search" +".txt");
		monitor.worked(1);
		String site = IGutenbergConstants.LATEST_SERACH;
		System.out.println("site-----" + site);
		Document d = Jsoup.connect(site).timeout(60*1000).get();
		Elements certainlinks = d.select("a[href*=/ebooks/]");
		//System.out.println(certainlinks);
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
		}
		if(limit>temp.size())
		{
			limit = temp.size();
		}
		//System.out.println("temp ka size=======" + temp.size());
		//System.out.println("Limit given========" + limit);
		//System.out.println(temp);
		}
		catch(HttpStatusException e1){
			if(e1.getStatusCode() == 412 || e1.getStatusCode() == 404)
			{
				System.out.println("Error Status Code is ----" + e1.getStatusCode());
			}
		}
		
		while(true){
			if(limit==downloadedCount)
				break;
			//System.out.println("******************I am in if***************************");
			for(int i = downloadCount;downloadedCount<limit;i++)
			{
				try
				{
				//System.out.println("i=======" + i);
				String numOfebook = temp.get(i);
				//System.out.println("Book-->" + numOfebook);
				String titleSite = IGutenbergConstants.TITLE_BASE_URL + numOfebook + "/" + numOfebook + "-h/" + numOfebook + "-h.htm";
				//System.out.println(titleSite);
				Document e = Jsoup.connect(titleSite).timeout(60*1000).get();
				Element title = e.select("title").first();
				//System.out.println(title);
				String contentSite = IGutenbergConstants.CONTENT_BASE_URL + numOfebook + "/pg" + numOfebook + ".txt";
				System.out.println("=============>>>>>>" + contentSite);
				Document g = Jsoup.connect(contentSite).timeout(60*1000).get();
				Response response = Jsoup.connect(contentSite).execute();
				ConsoleView.printlInConsoleln("Writing topic: "+ Jsoup.parse(title.toString()).text());
				jsonGenerator.writeStartObject();
				jsonGenerator.writeObjectField("title", Jsoup.parse(title.toString()).text());
				jsonGenerator.writeObjectField("abstract_body", Jsoup.parse(g.toString()).text());
				jsonGenerator.writeEndObject();
				downloadCount++;
				downloadedCount++;
				if(i==temp.size()-1 && downloadedCount!=limit)
				{
					//System.out.println("*************************************************");
					temp = searchNextPage(temp,limit);
				}
				//System.out.println("Download Count is=" + downloadCount);
				//System.out.println("Downloaded Count is=" + downloadedCount);
				}
				catch(HttpStatusException e1){
					if(e1.getStatusCode() == 412 || e1.getStatusCode() == 404)
					{
						System.out.println("Error Status Code is ----" + e1.getStatusCode());
						//System.out.println("Continuing after error");
						downloadCount++;
						//System.out.println("Download Count in Catch=" + downloadCount);
						//System.out.println("Downloaded Count in Catch=" + downloadedCount);
						//System.out.println("Last Book in Catch=" + lastbook);
						if(i==temp.size()-1 && downloadedCount!=limit)
						{
							//System.out.println("Searching nextttttttttttt paggeeeeeeeeeee");
							temp = searchNextPage(temp,limit);
						}
						
						continue;
					}
				}
				
			}
			jsonGenerator.writeEndArray();
		}
		
		//System.out.println("*****************I am out of if******************");
		 //System.out.println("I am out of extreme");
		// System.out.println(temp);
		 //System.out.println("temp ka size=======" + temp.size());
		 try {
				jsonGenerator.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Number of Downloads--" + downloadedCount);
		
}

	private ArrayList<String> searchNextPage(ArrayList<String> temp, int limit) throws IOException {
		//System.out.println("&&&&&&&&&&&&&&&&&|||||||||||||||||||||||||||||I am indise while||||||||||||||||||||||||||||||||&&&&&&&&&&&&&&&&&");
		int diff = limit-temp.size();
		//System.out.println("Difference is----" + diff);
		//System.out.println("nextpage index is---" + nextpageindex);
		String site2 = IGutenbergConstants.POPULAR_SEARCH + "&start_index=" + nextpageindex;
		//System.out.println("site2********************" + site2);
		Document h = Jsoup.connect(site2).timeout(60*1000).get();
		Elements certainlinks1 = h.select("a[href*=/ebooks/]");
		for (Element table : certainlinks1){
			Element a = table.select("a").first();
			String linkStr = a.attr("href");
			//System.out.println(linkStr);
			int lastIndex = linkStr.lastIndexOf('/');
			String s2 = linkStr.substring(lastIndex+1);
			if (s2.matches("[-+]?\\d*\\.?\\d+"))
			{	
				temp.add(s2);
			}
		}
		nextpageindex = nextpageindex + 25;
		lastbook = lastbook + 25;
		//System.out.println("Last Book in method=" + lastbook);
		//System.out.println(temp);
		//System.out.println("Size of temp----" + temp.size());
		// TODO Auto-generated method stub
		return temp;
	}
}
