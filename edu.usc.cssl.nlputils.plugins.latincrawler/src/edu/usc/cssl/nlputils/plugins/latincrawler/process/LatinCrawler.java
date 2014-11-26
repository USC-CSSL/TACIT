/**
 * @author Niki Parmar <nikijitp@usc.edu>
 */

package edu.usc.cssl.nlputils.plugins.latincrawler.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LatinCrawler {

	String outputDir;

	
	
	public static void main(String[] args) throws IOException{
		LatinCrawler lc = new LatinCrawler();
		lc.outputDir = "C://Users//carlosg//Desktop//CSSL//svm";
		lc.getBooks("cato");
	}
	
	public void initialize(String[] authors, String outputDir) throws IOException{
		this.outputDir = outputDir;
		
		checkPath(outputDir);
		
		for (String author: authors){
			
			getBooks(author);
			//break;	// Remove to unleash
		}
	}
	
	private void checkPath(String outputDir) {
		File outputPath = new File(outputDir);
		if (!outputPath.exists()){
			outputPath.mkdirs();
		}
	}	
	
	private void getBooks(String author) throws IOException {
		System.out.println("Extracting Books of Author  "+ author +"...");
		appendLog("Extracting Books of Author  "+ author +"...");
		Document doc = Jsoup.connect("http://www.thelatinlibrary.com/"+ author.toLowerCase()+".html").timeout(10*1000).get();
		Elements booksList = doc.getElementsByClass("work").first().getElementsByAttribute("href");
		
		File authorDir = new File(outputDir+"/"+author);
		if(!authorDir.exists()){
			authorDir.mkdirs();
		}
		String apath = authorDir.toString();
		System.out.println(apath);
		
		String bookname = "";
		for (Element bookItem : booksList){
			String bookText = bookItem.attr("abs:href");
			bookname  = bookItem.text();
			System.out.println(bookname);
			getContent(bookText, bookname, apath);
			
			
		}
	}

	private void getContent(String bookUri, String bookDir, String authorDir) throws IOException{
		BufferedWriter csvWriter= null;
		System.out.println("Extracting Content of book  "+ bookDir +"...");
		appendLog("Extracting Books of Author  "+ bookDir +"...");
		csvWriter  = new BufferedWriter(new FileWriter(new File(authorDir + System.getProperty("file.separator") + bookDir+".txt")));
		Document doc = Jsoup.connect(bookUri).timeout(10*1000).get();
		Elements content = doc.select("body").first().getElementsByTag("p");
		for (Element c : content){
			System.out.println(c.text());
			csvWriter.write(c.text()+"\n");
		}
		
		csvWriter.close();
		
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
