package edu.uc.cssl.nlputils.crawlers.supremecourt.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.usc.cssl.nlputils.common.ui.views.ConsoleView;
import edu.usc.nlputils.common.crawlers.ICrawler;

public class CrawlerJob implements Runnable,ICrawler {

	private String filter;
	private String outputDir;
	private boolean truncate;
	private boolean downloadAudio;
	private String baseUrl;
	private String url;
	private IProgressMonitor monitor;

	public CrawlerJob(String filter, String outputDir, String crawlUrl,String url, IProgressMonitor monitor) {

		this.filter = filter;
		this.outputDir = outputDir;
		this.truncate = false;
		this.downloadAudio = false;
		this.baseUrl = crawlUrl;
		this.url = url;
		this.monitor = monitor;
	}
	
	
	
	@Override
	public void run() {
		crawl(url);
    
	}
	
	protected Document retrieveDocumentFromUrl(String url) {
		Document doc = null;
		try {
			doc = Jsoup.connect(url).timeout(10 * 1000).get();
		} catch (IOException e) {
			// Error handling->will do later
		}
		return doc;
	}

	public void crawl(String url) {
		
		
		Document doc = retrieveDocumentFromUrl(url);
		Element table = doc.select("tbody").get(0);
		Elements rows = table.select("tr");
		for (Element row : rows) {
			
			 if (monitor.isCanceled()){
				 throw new OperationCanceledException(); 
				 
			}
			// ConsoleView.writeInConsole(row.select("a").get(0).attr("href"));
			String contenturl = baseUrl + row.select("a").get(0).attr("href");
			// ConsoleView.writeInConsole(row.select("td").get(1).text().trim());
			String date = row.select("td").get(2).text().trim();
			// Skip if no argument date
			if (date.equals(""))
				continue;
			String filename = row.select("td").get(1).text().trim() + "_"
					+ date.substring(6) + date.substring(0, 2)
					+ date.substring(3, 5);
			ConsoleView.writeInConsole(contenturl + ", " + filename);
			
			// Fixing the unhandled exception without cascading.
			try {
				getFiles(contenturl, filename);
			} catch (IOException e) {
				ConsoleView.writeInConsole("Error Accessing the URL " + contenturl);
				e.printStackTrace();
			}
			// break;
		}
	}	
	
	private void getFiles(String contenturl, String filename)
			throws IOException {
		File trans = new File(this.outputDir + "/" + filename
				+ "-transcript.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(trans));

		Document doc = retrieveDocumentFromUrl(contenturl);

		Elements hidden = doc.select("div.hidden");
		if (hidden.size() == 0) {
			ConsoleView.writeInConsole("No data. Skipping page " + contenturl);
			bw.close();
			return;
		}
		 if (monitor.isCanceled()){
			 throw new OperationCanceledException(); 
			 
		}

		// "-transcript.txt"
		String outputDetail = "Writing " + outputDir + "/" + filename
				+ "-transcript.txt";
		ConsoleView.writeInConsole(outputDetail);
		this.monitor.subTask(outputDetail);

		// Element transcript = doc.select("div.hidden").get(0);
		Element transcript = hidden.get(0);
		Elements lines = transcript.select("p");
		for (Element line : lines) {
			 if (monitor.isCanceled()){
				 throw new OperationCanceledException(); 
				 
			}
			// ConsoleView.writeInConsole(line.text());
			bw.write(line.text() + "\n");
		} 
		bw.close();
		

		if (this.downloadAudio) {
			 if (monitor.isCanceled()){
				 throw new OperationCanceledException(); 
				 
			}
			downloadAudioFilesFromWebPage(filename, doc);
		}

	}
	

	public void downloadAudioFilesFromWebPage(String filename, Document doc) {
		// "-argument.mp3"
		Elements links = doc.select(".audio");
		for (Element mp3 : links) {
			if (mp3.attr("href").contains(".mp3")) {
				 if (monitor.isCanceled()){
					 throw new OperationCanceledException(); 
					 
				}
				downloadTranscriptMp3File(filename, mp3);
				break; // Once mp3 found, no need to continue for loop
			}
		}
	}

	private void downloadTranscriptMp3File(String filename, Element mp3) {
		ConsoleView.writeInConsole("Downloading " + baseUrl + mp3.attr("href"));
		Response audio;
		FileOutputStream fos;
		try {
			audio = downloadAudio(mp3);
			fos = new FileOutputStream(new File(outputDir + "/" + filename
					+ "-argument.mp3"));
			fos.write(audio.bodyAsBytes());
			fos.close();
		} catch (IOException ioe) {
			// TODO Handle Exception
		}

	}

	protected Response downloadAudio(Element mp3) throws IOException {
		Response audio;
		if (!this.truncate)
			audio = Jsoup.connect(baseUrl + mp3.attr("href"))
					.cookie("oyez-tos", "1.0").maxBodySize(0)
					.ignoreContentType(true).execute();
		else
			audio = Jsoup.connect(baseUrl + mp3.attr("href"))
					.cookie("oyez-tos", "1.0").ignoreContentType(true)
					.execute();
		return audio;
	}

	protected Document parseContentFromUrl(String crawlUrl) throws IOException {

		return Jsoup.connect(crawlUrl.toString()).get();

	}

}
