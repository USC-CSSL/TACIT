package edu.usc.cssl.nlputils.plugins.supremeCrawler.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import edu.usc.cssl.nlputils.utilities.*;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.widgets.Display;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SupremeCrawler implements ISupremeCrawlerConstants {
	private String filter, url, outputDir;
	private boolean truncate, downloadAudio;
	private String baseUrl;

	public SupremeCrawler(String filter, String outputDir,String crawlUrl) {
		this.filter = filter;
		this.outputDir = outputDir;
		this.truncate = false;
		this.downloadAudio = false;
		this.baseUrl = crawlUrl;
		// http://www.oyez.org/cases/2009?page=1
		// http://www.oyez.org/cases/2009?order=field_argument_value&sort=asc&page=1&0=
		this.url = crawlUrl + filter + "?order=title&sort=asc";
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setTruncate(boolean truncate) {
		this.truncate = truncate;
	}

	public boolean isTruncate() {
		return truncate;
	}

	public void setDownloadAudio(boolean downloadAudio) {
		this.downloadAudio = downloadAudio;
	}

	public boolean isDownloadAudio() {
		return downloadAudio;
	}

	public void looper() throws IOException {
		int noOfPages = 0;
		System.out.println(url);
		appendLog("Crawling " + url);
		Document doc = Jsoup.connect(url).timeout(10 * 1000).get();
		Elements pages = doc.getElementsByClass("pager-last");
		// Sometimes the pager element is absent
		if (!pages.isEmpty()) {
			Element pageList = pages.get(0);
			Pattern pattern = Pattern.compile("page=([0-9]+)");
			Matcher matcher = pattern.matcher(pageList.toString());
			if (matcher.find()) {
				noOfPages = Integer.parseInt(matcher.group(1));
			}
		}

		for (int i = 0; i <= noOfPages; i++) {
			System.out.println("\nPage " + (i + 1));
			appendLog("\nPage" + (i + 1));
			crawl(url + "&page=" + i);
		}

	}

	public void crawl(String url) {
		Document doc = retrieveDocumentFromUrl(url);
		Element table = doc.select("tbody").get(0);
		Elements rows = table.select("tr");
		for (Element row : rows) {
			// System.out.println(row.select("a").get(0).attr("href"));
			String contenturl = baseUrl
					+ row.select("a").get(0).attr("href");
			// System.out.println(row.select("td").get(1).text().trim());
			String date = row.select("td").get(2).text().trim();
			// Skip if no argument date
			if (date.equals(""))
				continue;
			String filename = row.select("td").get(1).text().trim() + "_"
					+ date.substring(6) + date.substring(0, 2)
					+ date.substring(3, 5);
			System.out.println(contenturl + ", " + filename);
			appendLog(contenturl);
			// Fixing the unhandled exception without cascading.
			try {
				getFiles(contenturl, filename);
			} catch (IOException e) {
				System.out.println("Error Accessing the URL " + contenturl);
				appendLog("Error Accessing the URL " + contenturl);
				e.printStackTrace();
			}
			// break;
		}
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

	private void getFiles(String contenturl, String filename)
			throws IOException {
		File trans = new File(getOutputDir() + "/" + filename
				+ "-transcript.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(trans));

		Document doc = retrieveDocumentFromUrl(contenturl);

		Elements hidden = doc.select("div.hidden");
		if (hidden.size() == 0) {
			System.out.println("No data. Skipping page " + contenturl);
			appendLog("No data. Skipping page " + contenturl);
			bw.close();
			return;
		}

		// "-transcript.txt"
		System.out.println("Writing " + outputDir + "/" + filename
				+ "-transcript.txt");
		appendLog("Writing " + outputDir + "/" + filename + "-transcript.txt");

		// Element transcript = doc.select("div.hidden").get(0);
		Element transcript = hidden.get(0);
		Elements lines = transcript.select("p");
		for (Element line : lines) {
			// System.out.println(line.text());
			bw.write(line.text() + "\n");
		}
		bw.close();

		if (isDownloadAudio()) {
			downloadAudioFilesFromWebPage(filename, doc);
		}

	}

	public void downloadAudioFilesFromWebPage(String filename, Document doc) {
		// "-argument.mp3"
		Elements links = doc.select(".audio");
		for (Element mp3 : links) {
			if (mp3.attr("href").contains(".mp3")) {
				downloadTranscriptMp3File(filename, mp3);
				break; // Once mp3 found, no need to continue for loop
			}
		}
	}

	private void downloadTranscriptMp3File(String filename, Element mp3) {
		System.out.println("Downloading " + baseUrl + mp3.attr("href"));
		appendLog("Downloading " + baseUrl + mp3.attr("href"));
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
		if (!isTruncate())
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

	@Inject
	IEclipseContext context;

	private void appendLog(String message) {
		Log.append(context, message);
	}

}
