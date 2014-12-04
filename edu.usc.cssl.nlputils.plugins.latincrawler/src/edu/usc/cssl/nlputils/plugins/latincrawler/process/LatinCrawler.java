/**
 * @author Niki Parmar <nikijitp@usc.edu>
 */

package edu.usc.cssl.nlputils.plugins.latincrawler.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LatinCrawler {

	String outputDir;
	List<String> authorNames, authorUrl;
	
	public LatinCrawler() {
		authorNames = new ArrayList<String>();
		authorUrl = new ArrayList<String>();
	}
	
	public static void main(String[] args) throws IOException{
		LatinCrawler lc = new LatinCrawler();
		lc.outputDir = "C://Users//carlosg//Desktop//CSSL//svm";
		lc.getBooks("Vergil", "http://www.thelatinlibrary.com/verg.html");
	}
	
	public void initialize(String outputDir) throws IOException{
		this.outputDir = outputDir;
		
		checkPath(outputDir);
		appendLog("Loading Authors...");
		getAllAuthors();
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
			 	getBooks(name, url);
		}
		//String authorsString = " All|"+authorsList.text().split(":")[1];
		return;
	}
	
	private void getBooks(String author, String aurl) throws IOException {
		//System.out.println("Extracting Books of Author  "+ author +"...");
		appendLog("\nExtracting Books of Author  "+ author +"...");
		
		try{
		Document doc = Jsoup.connect(aurl).timeout(10*1000).get();
		Elements booksList = doc.getElementsByTag("td");
		
		File authorDir = new File(outputDir+"/"+author);
		if(!authorDir.exists()){
			authorDir.mkdirs();
		}
		String apath = authorDir.toString();
		System.out.println(apath);
		
		String bookname = "";
		for (Element bookItem : booksList){
			String bookText = bookItem.getElementsByTag("a").attr("abs:href");
			bookname  = bookItem.text();
			if(bookname.equals("The Latin Library") || bookname.equals("The Classics Page") || bookname.equals("Christian Latin"))
				continue;
			System.out.println(bookText);
			getContent(bookText, bookname, apath);
			
			
		}
		}catch(Exception e){
			System.out.println("Something went wrong when extracting books of Author " + author);
			appendLog("Something went wrong when extracting books of Author " + author);
		}
	}

	private void getContent(String bookUri, String bookDir, String authorDir) throws IOException{
		BufferedWriter csvWriter= null;
	//	System.out.println("Extracting Content of book  "+ bookDir +"...");
		appendLog("Extracting Content of book  "+ bookDir +"...");
		try{
			csvWriter  = new BufferedWriter(new FileWriter(new File(authorDir + System.getProperty("file.separator") + bookDir+".txt")));
			Document doc = Jsoup.connect(bookUri).timeout(10*1000).get();
			Elements content = doc.select("body").first().getElementsByTag("p");
			for (Element c : content){
				csvWriter.write(c.text()+"\n");
			}
		}catch(Exception e){
	//		System.out.println("Something went wrong when extracting book " + bookDir);
	//		appendLog("Something went wrong when extracting book " + bookDir);
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
		}
	}
}
