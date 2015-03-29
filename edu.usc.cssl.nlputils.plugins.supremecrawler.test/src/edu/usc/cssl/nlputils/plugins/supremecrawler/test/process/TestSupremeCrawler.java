package edu.usc.cssl.nlputils.plugins.supremecrawler.test.process;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.usc.cssl.nlputils.plugins.supremeCrawler.process.SupremeCrawler;

public class TestSupremeCrawler {
    SupremeCrawler sCrawler ;
    Document doc;
	@Before
	public void setUp() throws IOException{
		File input = new File("webpages/download_mp3.html");
		doc = Jsoup.parse(input, "UTF-8", "http://example.com/");
		sCrawler = new SupremeCrawler("", "outputdir",""){
			
			@Override
			protected Document retrieveDocumentFromUrl(String url) {
				File input = new File(url);
				try {
					doc = Jsoup.parse(input, "UTF-8", "http://example.com/");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return doc;
			}
			
			@Override
			protected Response downloadAudio(Element mp3) throws IOException {
				return new Response() {
					
					@Override
					public Response url(URL arg0) {
						return null;
					}
					
					@Override
					public URL url() {
						return null;
					}
					
					@Override
					public Response removeHeader(String arg0) {
						return null;
					}
					
					@Override
					public Response removeCookie(String arg0) {
						return null;
					}
					
					@Override
					public Response method(Method arg0) {
						return null;
					}
					
					@Override
					public Method method() {
						return null;
					}
					
					@Override
					public Map<String, String> headers() {
						return null;
					}
					
					@Override
					public Response header(String arg0, String arg1) {
						return null;
					}
					
					@Override
					public String header(String arg0) {
						return null;
					}
					
					@Override
					public boolean hasHeader(String arg0) {
						return false;
					}
					
					@Override
					public boolean hasCookie(String arg0) {
						return false;
					}
					
					@Override
					public Map<String, String> cookies() {
						return null;
					}
					
					@Override
					public Response cookie(String arg0, String arg1) {
						return null;
					}
					
					@Override
					public String cookie(String arg0) {
						return null;
					}
					
					@Override
					public String statusMessage() {
						return null;
					}
					
					@Override
					public int statusCode() {
						return 0;
					}
					
					@Override
					public Document parse() throws IOException {
						return null;
					}
					
					@Override
					public String contentType() {
						return null;
					}
					
					@Override
					public String charset() {
						return null;
					}
					
					@Override
					public byte[] bodyAsBytes() {
						try {
							return Files.readAllBytes(Paths.get("webpages/sample.mp3"));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null;
					}
					
					@Override
					public String body() {
						// TODO Auto-generated method stub
						return null;
					}
				};
			}
		};
		
	}
	
	@Test
	public void testDownloadAudioFileLocation() {
		sCrawler.downloadAudioFilesFromWebPage("tr", doc);
		assertTrue("Download Mp3 file Failed", new File("outputdir/tr-argument.mp3").exists());
	}
	
	@Test
	public void testGetFilesFromWeb() {
		 sCrawler.crawl("webpages/downloadcase.html");
		 assertTrue("Crawl Failed", new File("outputdir/07-581_20081201-transcript.txt").exists());

      
	}
	
   @After
   public void cleanUp(){
	   File testFile = new File("outputdir");
	   File[] files = testFile.listFiles();
	   for (File file : files) {
		   file.delete();
	}
   }
}
