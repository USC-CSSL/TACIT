/**
 * @author Niki Parmar <nikijitp@usc.edu>
 */

package edu.usc.cssl.nlputils.plugins.latincrawler.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LatinCrawler {
	private StringBuilder readMe = new StringBuilder();
	String outputDir;
	List<String> authorNames, authorUrl;
	
	public LatinCrawler() {
		authorNames = new ArrayList<String>();
		authorUrl = new ArrayList<String>();
	}
	
	public static void main(String[] args) throws IOException{
		LatinCrawler lc = new LatinCrawler();
		lc.outputDir = "/home/niki/Desktop/CSSL/latin/";
		lc.getBooks("Aquinas", "http://www.thelatinlibrary.com/aquinas.html", "/home/niki/Desktop/CSSL/latin/Aquinas", "St. Thomas Aquinas");
		//lc.getContent("http://www.thelatinlibrary.com/ammianus/17.shtml", "Liber XX", "/home/niki/Desktop/CSSL/latin/Ammianus Marcellinus");
	}
	
	public void initialize(String outputDir) throws IOException{
		this.outputDir = outputDir;
		
		checkPath(outputDir);
		appendLog("Loading Authors...");
		getAllAuthors();
		writeReadMe(outputDir);
	}
	
	private void checkPath(String outputDir) {
		File outputPath = new File(outputDir);
		if (!outputPath.exists()){
			outputPath.mkdirs();
		}
	}	
	
	public void getAllAuthors() throws IOException{
		int i, size = 0;
		String name , url;
		Document doc = Jsoup.connect("http://www.thelatinlibrary.com/").timeout(10*1000).get();
		Elements authorsList = doc.getElementsByTag("option");
		System.out.println(authorsList);
		size = authorsList.size();
		String[] authorString = new String[size];
		for(i =0;i<size;i++)
		{
				name = authorsList.get(i).text();
				url = "http://www.thelatinlibrary.com/" + authorsList.get(i).attr("value");
				authorUrl.add(url);
				authorNames.add(name);
				if(name.equals("Christian Latin") || url.equals("http://www.thelatinlibrary.com/christian.html"))
					continue;
				String aurl = outputDir + File.separator + name;
				File authorDir = new File(aurl);
				if(!authorDir.exists()){
					authorDir.mkdirs();
				}
				System.out.println("Extracting Books of Author  "+ name +"...");
				appendLog("\nExtracting Books of Author  "+ name +"...");
			 	getBooks(name, url, aurl, name);
		}
		//String authorsString = " All|"+authorsList.text().split(":")[1];
		return;
	}
	
	private void getBooks(String author, String aurl, String apath, String mainAuthor) throws IOException {

		try{
			System.out.println(aurl);
			Document doc = Jsoup.connect(aurl).timeout(10*1000).get();
			Elements subLists = doc.select("h2.work");
			if(subLists != null && subLists.size() > 1){
				getBooksList(author, aurl, doc, apath);
				return;
			}	

			Elements booksList = doc.getElementsByTag("td");
			System.out.println(apath);
			int count = 0;
			String bookname = "";

			if(booksList != null){


				for (Element bookItem : booksList){
					String bookText = bookItem.getElementsByTag("a").attr("abs:href");
					if(bookText.contains("#"))
						continue;
					bookname  = bookItem.text();
					System.out.println(bookname + " " + author);
					if(bookname.equals("The Latin Library") || bookname.equals("The Classics Page") || bookname.equals("Christian Latin") || 
							bookname.equals("The Miscellany") || (bookname.toLowerCase()).equals(mainAuthor.toLowerCase())
							|| bookname.equals("St. Thomas Aquinas") || bookname.equals("Neo-Latin") || bookname.equals("The Bible")
							|| bookname.equals("Medieval Latin") || bookname.equals("Ius Romanum"))
						continue;
					if (authorUrl.contains(bookText))
						continue;
					getBooks(bookname, bookText, apath, mainAuthor);
					count++;

				}
			}
			System.out.println(count);
			if(count == 0) {
				if(doc.select("p.pagehead")!= null && doc.select("p.pagehead").size() > 0 )
					bookname = doc.select("p.pagehead").first().text();
				else if(doc.select("h1")!= null && doc.select("h1").size() >  0)
					bookname = doc.select("h1").first().text();
				else
					bookname = author;
				//	System.out.println(bookname);
				getContent(aurl, bookname, apath);
			}
		}catch(Exception e){
			System.out.println("Something went wrong when extracting books of Author " + author + e);
			appendLog("Something went wrong when extracting books of Author " + author);
		}
	}

	private void getBooksList(String author, String aurl, Document doc, String apath){
		System.out.println("Contains sub ");
		Elements subLists = doc.select("div.work");
		//System.out.println(subLists);
		Elements subHeaders = doc.select("h2.work");
		int count = 0;
		for(Element list : subLists){
			try{
				//System.out.println(list);
				Elements booksList = list.getElementsByTag("td");
				
				File authorDir = new File(apath +  File.separator +  subHeaders.get(count).text());
				if(!authorDir.exists()){
					authorDir.mkdirs();
				}
				String apath1 = authorDir.toString();
				System.out.println(apath1);
				
				count++;
				String bookname = "";
				for (Element bookItem : booksList){
					String bookText = bookItem.getElementsByTag("a").attr("abs:href");
					bookname  = bookItem.text();

					System.out.println(bookText);
					getContent(bookText, bookname, apath1);
				}
				
			}catch(Exception e){
				System.out.println("Something went wrong when extracting books of Author " + author);
				appendLog("Something went wrong when extracting books of Author " + author);
			}
		}
	}
	
	private void getContent(String bookUri, String bookDir, String authorDir) throws IOException{
		BufferedWriter csvWriter= null;
		System.out.println("Extracting Content of book  "+ bookDir +"...");
		appendLog("Extracting Content of book  "+ bookDir +"...");
		try{
			System.out.println(bookUri);
			csvWriter  = new BufferedWriter(new FileWriter(new File(authorDir + System.getProperty("file.separator") + bookDir+".txt")));
			Document doc = Jsoup.connect(bookUri).timeout(10*10000).get();
			//Elements c1 = doc.select("div#page-wrapper");
			Elements content = doc.getElementsByTag("p"); 
		//	System.out.println(doc);
			for (Element c : content){
				csvWriter.write(c.text()+"\n");
			//	System.out.println(c.text());
			}
		}catch(Exception e){
			System.out.println("Something went wrong when extracting book " + bookDir + e);
			appendLog("Something went wrong when extracting book " + bookDir);
		}finally{
			if(csvWriter!=null)
				csvWriter.close();
		}
	}
	
	
	

	@Inject IEclipseContext context;	
	private void appendLog(String message){
		IEclipseContext parent = null;
		if (context==null)
			return;
		parent = context.getParent();
		//System.out.println(parent.get("consoleMessage"));
		String currentMessage = (String) parent.get("consoleMessage"); 
		if (currentMessage==null)
			parent.set("consoleMessage", message);
		else {
			if (currentMessage.equals(message)) {
				// Set the param to null before writing the message if it is the same as the previous message. 
				// Else, the change handler will not be called.
				parent.set("consoleMessage", null);
				parent.set("consoleMessage", message);
			}
			else
				parent.set("consoleMessage", message);
			readMe.append(message+"\n");
		}
	}
	public void writeReadMe(String location){
		File readme = new File(location+"/README.txt");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(readme));
			String plugV = Platform.getBundle("edu.usc.cssl.nlputils.plugins.latincrawler").getHeaders().get("Bundle-Version");
			String appV = Platform.getBundle("edu.usc.cssl.nlputils.application").getHeaders().get("Bundle-Version");
			Date date = new Date();
			bw.write("Latin Crawler Output\n--------------------\n\nApplication Version: "+appV+"\nPlugin Version: "+plugV+"\nDate: "+date.toString()+"\n\n");
			bw.write(readMe.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
