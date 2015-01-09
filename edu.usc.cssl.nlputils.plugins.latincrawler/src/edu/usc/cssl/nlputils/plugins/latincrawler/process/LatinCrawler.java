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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	Set<String> authorNames;
	Set<String> skipBooks;
	
	public LatinCrawler() {
		authorNames = new HashSet<String>();
		//authorUrl = new HashSet<String>();
		skipBooks = new HashSet<String>();
		
		skipBooks.add("The Latin Library");
		skipBooks.add("The Classics Page");
		skipBooks.add("Christian Latin");
		skipBooks.add("The Miscellany");
		skipBooks.add("St. Thomas Aquinas");
		skipBooks.add("Isidore of Seville");
		skipBooks.add("Seneca the Younger");
		skipBooks.add("Seneca the Elder");
		skipBooks.add("Velleius");
		skipBooks.add("Neo-Latin");
		skipBooks.add("The Bible");
		skipBooks.add("Medieval Latin");
		skipBooks.add("Christian");
		skipBooks.add("Medieval");
		skipBooks.add("Ius Romanum");
		skipBooks.add("Miscellany");
	}
	
	public static void main(String[] args) throws IOException{
		LatinCrawler lc = new LatinCrawler();
		lc.outputDir = "/home/niki/Desktop/CSSL/latin/";
		lc.authorNames.add("Claudian");
		lc.getBooks("Medieval", "http://www.thelatinlibrary.com/medieval.html", "/home/niki/Desktop/CSSL/latin/Medieval", "Medieval");
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
		//System.out.println(authorsList);
		size = authorsList.size();
		int count = 0;
		
		for(i =0;i<size;i++)
		{
				name = authorsList.get(i).text();
				url = "http://www.thelatinlibrary.com/" + authorsList.get(i).attr("value");
				//authorUrl.add(url);
				authorNames.add(name);
				if(skipBooks.contains(name))
					continue;
				String aurl = outputDir + File.separator + name;
				File authorDir = new File(aurl);
				if(!authorDir.exists()){
					authorDir.mkdirs();
				}
				//System.out.println("Extracting Books of Author  "+ name +"...");
				appendLog("\nExtracting Books of Author  "+ name +"...");
				getBooks(name, url, aurl, name);
			 	//count++;
		}
		//String authorsString = " All|"+authorsList.text().split(":")[1];
		
		Element secondList = doc.getElementsByTag("table").get(1);
		Elements auth2List = secondList.getElementsByTag("td");
		for(Element auth : auth2List)
		{
			name = auth.text();
			url = auth.getElementsByTag("a").attr("abs:href");
			if(authorNames.contains(name))
				continue;
			//authorUrl.add(url);
			authorNames.add(name);
			if(skipBooks.contains(name))
				continue;
			String aurl = outputDir + File.separator + name;
			File authorDir = new File(aurl);
			if(!authorDir.exists()){
				authorDir.mkdirs();
			}
		//	System.out.println("Extracting Books of Author  "+ name +"...");
			appendLog("\nExtracting Books of Author  "+ name +"...");
		 	getBooks(name, url, aurl, name);
		}
		
		return;
	}
	
	private void getBooks(String author, String aurl, String apath, String mainAuthor) throws IOException {

		try{
		//	System.out.println(aurl);
			Document doc = Jsoup.connect(aurl).timeout(10*1000).get();
			Elements subLists = doc.select("h2.work");
			if(subLists != null && subLists.size() > 1){
				getBooksList(author, aurl, doc, apath, mainAuthor);
				return;
			}	

			Elements booksList = doc.getElementsByTag("td");
			//System.out.println(apath);
			int count = 0;
			String bookname = "";

			if(booksList != null){
				for (Element bookItem : booksList){
					String bookText = bookItem.getElementsByTag("a").attr("abs:href");
					if(bookText.contains("#"))
						continue;
					bookname  = bookItem.text();
					//System.out.println(bookname + " " + author);
					if(skipBooks.contains(bookname))
						continue;
					if(authorNames.contains(bookname) || (bookname.toLowerCase()).equals(mainAuthor.toLowerCase()))
						continue;
					getBooks(bookname, bookText, apath, mainAuthor);
					count++;

				}
			}
			if(count == 0) {
				if(doc.select("p.pagehead")!= null && doc.select("p.pagehead").size() > 0 )
					bookname = doc.select("p.pagehead").first().text();
				else if(doc.select("h1")!= null && doc.select("h1").size() >  0)
					bookname = doc.select("h1").first().text();
				else
					bookname = author;
				getContent(aurl, bookname, apath);
			}
		}catch(Exception e){
			System.out.println("Something went wrong when extracting books of Author " + author + e);
			appendLog("Something went wrong when extracting books of Author " + author);
		}
	}

	private void getBooksList(String author, String aurl, Document doc, String apath, String mainAuthor){
		Elements subLists = doc.select("div.work");
		//System.out.println(subLists);
		Elements subHeaders = doc.select("h2.work");
		int i = 0, size1 = subLists.size(), size2 = subHeaders.size(), j =0;
		String bookText ="";
		String bookname = "";
		Element head = null;

		//handle count of headers and div
		while(i< size1 || j<size2){
			try{
				if(j< size2){
					head = subHeaders.get(j);
					Elements bookLink = head.getElementsByTag("a");
					if(bookLink!= null && bookLink.size() > 0){
						//make for all links
						bookText = bookLink.get(0).attr("abs:href");
			//			System.out.println(bookText);
						bookname  = bookLink.get(0).text();
		//				System.out.println(bookname + " " + author);
						if(skipBooks.contains(bookname))
							continue;
						if(authorNames.contains(bookname)|| (bookname.toLowerCase()).equals(author.toLowerCase()))
							continue;
						File authorDir = new File(apath + File.separator + bookname);
						if(!authorDir.exists()){
							authorDir.mkdirs();
						}
						String apath1 = authorDir.toString();
						getBooks(bookname, bookText, apath1, mainAuthor);
						j++;
						continue;
					}
				}


				if(i< size1){
					Element list = subLists.get(i);
					Elements booksList = list.getElementsByTag("td");

					String authorNewDir = apath +  File.separator;
					if(j<size2)
						authorNewDir += head.text();
					else
						authorNewDir += "Others";
					File authorDir = new File(authorNewDir);
					if(!authorDir.exists()){
						authorDir.mkdirs();
					}
					String apath1 = authorDir.toString();
					//System.out.println(apath1);


					int count1 = 0;
					if(booksList != null){
						for (Element bookItem : booksList){
							bookText = bookItem.getElementsByTag("a").attr("abs:href");
							if(bookText.contains("#"))
								continue;
							bookname  = bookItem.text();
							//System.out.println(bookname + " " + author);
							if(skipBooks.contains(bookname))
								continue;
							if(authorNames.contains(bookname)|| (bookname.toLowerCase()).equals(author.toLowerCase()))
								continue;
							getBooks(bookname, bookText, apath1, mainAuthor);
							count1++;

						}
					}


					if(count1 == 0) {
						if(doc.select("p.pagehead")!= null && doc.select("p.pagehead").size() > 0 )
							bookname = doc.select("p.pagehead").first().text();
						else if(doc.select("h1")!= null && doc.select("h1").size() >  0)
							bookname = doc.select("h1").first().text();
						else
							bookname = author;
						//	System.out.println(bookname);
						getContent(aurl, bookname, apath1);
					}
					i++;
					
				}
				j++;
			}catch(Exception e){
				//System.out.println("Something went wrong when extracting books of Author " + author);
				appendLog("Something went wrong when extracting books of Author " + author);
			}
		}
	}
	
	private void getContent(String bookUri, String bookDir, String authorDir) throws IOException{
		BufferedWriter csvWriter= null;
		//System.out.println("Extracting Content of book  "+ bookDir +"...");
		appendLog("Extracting Content of book  "+ bookDir +"...");
		try{
			//System.out.println(bookUri);
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
			//System.out.println("Something went wrong when extracting book " + bookDir + e);
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
