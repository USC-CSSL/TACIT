package edu.usc.cssl.tacit.crawlers.wikipedia.services;
/**
* This class is used for crawling a certain item.  
* If the item name is valid and exists in Wikipedia's
*  database, the result would be stored to the path
*  where user specify through UI interface. 
*
* @author  Yongshun
* @version 1.0
* @since   2016-05-01 
*/


import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

public class Crawler {
	public Document doc=null;
	public boolean valid;
	
	public Crawler(String url) {
		// TODO Auto-generated constructor stub
		url=url.replace((char)8211, (char)45);
		crawl(url);
	}
	
	public void crawl(String url) {
		try {
			doc=Jsoup.connect(url).timeout(0).userAgent("Crawl Tools (yongshul@usc.edu)").get();
			valid=true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
//			try {
//				Thread.currentThread();
//				Thread.sleep(10);
//			} catch (InterruptedException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			System.out.println("Error when crawling the webpage.");
			valid=false;
		}
	}
	
	public String html() {
		if (valid)
			return doc.html();
		else
			return null;
	}
	
	public String text() {
		if (valid)
			return doc.text();
		else
			return null;
	}
	
	public String highValueText() {
		if (valid) {
			try {
				return ArticleExtractor.INSTANCE.getText(html()).replaceAll("\\n", " ");
			} catch (BoilerpipeProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;	
		}
		else
			return null;
	}
}
