package edu.usc.cssl.tacit.crawlers.gutenberg.services;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

//import org.eclipse.core.runtime.IProgressMonitor;
//import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import edu.usc.cssl.tacit.common.ui.views.ConsoleView;


public class SearchPopular {
	JsonFactory jsonFactory;
	JsonGenerator jsonGenerator;
	//IProgressMonitor monitor;
	public int nextpageindex=26;
	ArrayList<String> temp = new ArrayList<String>();
	ArrayList<String> checktemp = new ArrayList<String>();
	IProgressMonitor monitor;
	
	
	//popular method will go the site and get the books in arraylist called temp
	public void popular(String dir, int limit,String query,IProgressMonitor monitor) throws IOException {
		int downloadCount = 0;
		int downloadedCount =0;
		this.monitor = monitor;
		jsonFactory = new JsonFactory();
		File streamFile = new File(dir+File.separator+ query +".json");
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
		f = new File(dir+File.separator+  query +".txt");
		monitor.worked(1);
		String site = IGutenbergConstants.POPULAR_SEARCH+query;
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
		//System.out.println("temp ka size=======" + temp.size());
		//System.out.println("Limit given========" + limit);
		//System.out.println(temp);
		monitor.worked(1);
		if(temp.size()==0)
		{
			//when search result returns nothing. i.e no books according to user search
			ConsoleView.printlInConsoleln("No books found according to your search.Kindly change your search options or try again later.");
		}
		if((limit==temp.size() || limit < temp.size() || limit>temp.size()) && temp.size()!=0)
		{
			//Two of the three cases i told you
			lessThanOrEqualTo(limit,query,temp);
		}
		}
		catch(HttpStatusException e1){
			if(e1.getStatusCode() == 412 || e1.getStatusCode() == 404)
			{
				System.out.println("Error Status Code is ----" + e1.getStatusCode());
			}
		}
		
		try {
			jsonGenerator.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("Number of Downloads-|||||||||||||||||||||||||||||||||||" + downloadCount);
		
	}

	
	/*Cases : 
	 * Case 0 : limit 5, temp 25, can get more in temp, 0 fails of first 25. 
	 * Case 1: limit 5, temp 25, can get more in temp, 22 fails of first 25
	 * Case 2: limit 27, temp 25, can get more in temp, 10 fails of first 25
	 * Case 3: limit 10, temp 25, can't get more in temp, 20 fails of first 25. 
	 * Case 4: limit 20, temp 15, can't get more in temp, 0 fails of first 25. 
	 * 
	*/
	//lessthanOrEqualTo method will now extract title and content from the books in temp
	private void lessThanOrEqualTo(int limit, String query, ArrayList<String> temp) throws IOException {
		int downloadedCount =0;
		//int downloadCount =0;
		while(true){
			if(downloadedCount==limit)
			{
				break;
			}
			for(int i = 0;i<temp.size();i++)
			{
				if(downloadedCount==limit)
				{
					break;
				}
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
				//System.out.println("=============>>>>>>" + contentSite);
				Document g = Jsoup.connect(contentSite).timeout(60*1000).get();
				Response response = Jsoup.connect(contentSite).execute();
				ConsoleView.printlInConsoleln("Writing topic: "+ Jsoup.parse(title.toString()).text());
				jsonGenerator.writeStartObject();
				jsonGenerator.writeObjectField("title", Jsoup.parse(title.toString()).text());
				jsonGenerator.writeObjectField("abstract_body", Jsoup.parse(g.toString()).text());
				jsonGenerator.writeEndObject();
				//downloadCount++;
				downloadedCount++;
				//System.out.println("Download Count is=" + downloadCount);
				//System.out.println("Downloaded Count is=" + downloadedCount);
				}
				catch(HttpStatusException e1){
					if(e1.getStatusCode() == 412 || e1.getStatusCode() == 404)
					{
						System.out.println("Error Status Code is ----" + e1.getStatusCode());
						//System.out.println("Continuing after error");
						//downloadCount++;
						//System.out.println("Download Count in Catch=" + downloadCount);
						//System.out.println("Downloaded Count in Catch=" + downloadedCount);
						continue;
					}
				}
			}
				if(downloadedCount!=limit)
				{
					//This is a check when end of temp is reached and also downloadedCount is not equal to limit so we need to search next page
					//System.out.println("I should search next page now probably");
					//downloadCount++;
					checktemp = searchnextpage(query,limit); //searchnextpage will search the nextpage of site and return next ebooks that will be stored in checktemp
					if(checktemp.isEmpty())
					{
						//means next page is empty
						//System.out.println("Next temp not found");
						break;
					}
					else
					{
						temp.clear();
						temp.addAll(checktemp);
					}
				}
			
		}
		jsonGenerator.writeEndArray();
		if(downloadedCount==0)
		{
			ConsoleView.printlInConsoleln("No books found according to your search.Kindly change your search options or try again later.");
		}
		ConsoleView.printlInConsoleln(downloadedCount + " book(s) downloaded according to specified search result.");
	}
	


	private ArrayList<String> searchnextpage(String query, int limit) throws IOException {
		System.out.println("&&&&&&&&&&&&&&&&&I am indise SEARCHNEXT&&&&&&&&&&&&&&&&&");
		ArrayList<String> newtemp = new ArrayList<String>();
		int diff = limit-temp.size();
		System.out.println("Difference is----" + diff);
		System.out.println("nextpage index is---" + nextpageindex);
		String site2 = IGutenbergConstants.POPULAR_SEARCH + query + "&start_index=" + nextpageindex;
		System.out.println("site2********************" + site2);
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
				newtemp.add(s2);
			}
		}
		nextpageindex = nextpageindex + 25;
		//System.out.println("Lets check new temp");
		//System.out.println("############################################");
		//System.out.println(newtemp);
		//System.out.println("############################################");
		return newtemp;
		
	}
	
	
	
	
}

