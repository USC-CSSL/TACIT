package edu.uc.cssl.tacit.crawlers.supremecourt.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class CrawlerJob {

	private String outputDir;
	private boolean truncate;
	private boolean downloadAudio;
	private String baseUrl;
	private String url;
	private IProgressMonitor monitor;
	private FileWriter fileWriter;
	private BufferedWriter bw;
	private int limit;
	private int case_id;

	public CrawlerJob(String outputDir, String crawlUrl,
			IProgressMonitor monitor, boolean downloadAudio, boolean truncate,
			int limit) {

		this.outputDir = outputDir;
		this.truncate = truncate;
		this.downloadAudio = downloadAudio;
		this.baseUrl = crawlUrl;
		this.monitor = monitor;
		this.limit = limit;
		this.case_id = 0;
		openSummaryFile();
	}

	private void openSummaryFile() {
		DateFormat df = new SimpleDateFormat("MM-dd-yyyy-HH-mm-ss");
		Date dateobj = new Date();
		try {

			fileWriter = new FileWriter(this.outputDir + "/"
					+ "supremecourt-crawler-summary-" + df.format(dateobj)
					+ ".csv");
			this.bw = new BufferedWriter(fileWriter);

			addContentsToSummary("Case", "Location", "Docket No", "Argued",
					"Decided", "Majority Author", "Vote", "File Type",
					"File name");
		} catch (IOException e) {
		}

	}

	private void addContentsToSummary(String... contents) {

		try {
			for (String content : contents) {
				if (content.contains(",")) {
					content = content.replace(",", " ");
				}
				bw.write(content);
				bw.write(",");

			}
			bw.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void summaryFileClose() {
		try {
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run(String url, int noOfPages) throws IOException {
		crawl(url, noOfPages);

	}

	protected Document retrieveDocumentFromUrl(String url) throws IOException {
		Document doc = null;
		doc = Jsoup.connect(url).timeout(10 * 1000).get();
		return doc;
	}

	public void crawl(String url, int noOfPages) throws IOException {
		Document doc = retrieveDocumentFromUrl(url);
		Element table = doc.select("tbody").get(0);
		Elements rows = table.select("tr");
		int totalDone = 0;
		int remaining = 0;
		if (rows.size() > 0)
			remaining = 9900 / rows.size();
		if (noOfPages > 0)
			remaining = remaining / noOfPages;
		if (remaining == 0) {
			remaining = 1;
		}
		
		for (Element row : rows) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();

			}
			// ConsoleView.writeInConsole(row.select("a").get(0).attr("href"));
			String contenturl = baseUrl + row.select("a").get(0).attr("href");
			// ConsoleView.writeInConsole(row.select("td").get(1).text().trim());
			String date = row.select("td").get(2).text().trim();
			// Skip if no argument date
			if (date.equals("")) {
				ConsoleView.printlInConsoleln("No argument date found for "
						+ row.select("td").get(1).text().trim()
						+ ". Hence it will not be crawled ");
				continue;
			}
			String[] casesSplit = row.select("a").get(0).attr("href")
					.split("/");
			monitor.subTask("Crawling " + "Case : "
					+ row.select("a").get(0).text() + " year : "
					+ casesSplit[casesSplit.length - 2] + " url : " + url);
			ConsoleView.printlInConsole("Crawling " + "Case : "
					+ row.select("a").get(0).text() + " year : "
					+ casesSplit[casesSplit.length - 2]);
			if (limit == case_id)
				break;
			case_id++;
			String filename = row.select("td").get(1).text().trim() + "_"
					+ date.substring(6) + date.substring(0, 2)
					+ date.substring(3, 5);
			ConsoleView.printlInConsoleln(" url :" + contenturl);

			// Fixing the unhandled exception without cascading.
			try {
				CrawlerData crawlDetails = getFiles(contenturl, filename);
				if (crawlDetails.getFileLocation().length() > 1) {
					if (crawlDetails.getFileLocation().contains(",")) {
						addContentsToSummary(row.select("td").get(0).text(),
								row.select("td").get(1).text(),
								crawlDetails.getLocation(), row.select("td")
										.get(2).text(), row.select("td").get(3)
										.text(),
								row.select("td").get(4).text(), row
										.select("td").get(5).text(),
								"Transcript", crawlDetails.getFileLocation()
										.split(",")[0]);
						addContentsToSummary(row.select("td").get(0).text(),
								crawlDetails.getLocation(), row.select("td")
										.get(1).text(), row.select("td").get(2)
										.text(),
								row.select("td").get(3).text(), row
										.select("td").get(4).text(), row
										.select("td").get(5).text(), "Mp3",
								crawlDetails.getFileLocation().split(",")[1]);
					} else {
						addContentsToSummary(row.select("td").get(0).text(),
								crawlDetails.getLocation(), row.select("td")
										.get(1).text(), row.select("td").get(2)
										.text(),
								row.select("td").get(3).text(), row
										.select("td").get(4).text(), row
										.select("td").get(5).text(),
								"Transcript", crawlDetails.getFileLocation());
					}
				}
				totalDone += remaining;
				if (totalDone <= 9900)
					monitor.worked(remaining);
			} catch (IOException e) {
				ConsoleView.printlInConsoleln("Error Accessing the URL "
						+ contenturl);
				e.printStackTrace();
			} finally {

			}
			// break;
		}
	}

	private CrawlerData getFiles(String contenturl, String filename)
			throws IOException {
		File trans = new File(this.outputDir + "/" + filename
				+ "-transcript.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(trans));

		Document doc = retrieveDocumentFromUrl(contenturl);
		CrawlerData crawlData = new CrawlerData();

		Elements hidden = doc.select("div.hidden");
		if (hidden.size() == 0) {
			ConsoleView.printlInConsoleln("No data. Skipping page "
					+ contenturl);
			bw.close();
			return crawlData;
		}
		if (monitor.isCanceled()) {
			bw.close();
			throw new OperationCanceledException();
		}

		// "-transcript.txt"
		String outputDetail = "Writing " + outputDir + "/" + filename
				+ "-transcript.txt";
		ConsoleView.printlInConsoleln(outputDetail);
		this.monitor.subTask(outputDetail);

		// Element transcript = doc.select("div.hidden").get(0);
		Element transcript = hidden.get(0);
		Elements lines = transcript.select("p");
		if (lines.size() < 1) {
			lines = doc.select("div.content").select("p");
		}
		for (Element line : lines) {
			if (monitor.isCanceled()) {
				bw.close();
				throw new OperationCanceledException();

			}
			bw.write(line.text() + "\n");
		}
		bw.close();
		boolean exist = true;
		if (FileUtils.sizeOf(trans) <= 20) {
			trans.delete();
			exist = false;
		}
		String dwn = "";
		if (this.downloadAudio) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();

			}
			dwn = downloadAudioFilesFromWebPage(filename, doc);
		}
		if (dwn.length() > 1) {
			crawlData.setFileLocation(outputDir + "/" + filename
					+ "-transcript.txt" + "," + dwn);
		} else {
			if (exist)
				crawlData.setFileLocation(outputDir + "/" + filename
						+ "-transcript.txt");
		}
		if (doc.select("div.case-location") != null
				&& doc.select("div.case-location").select("a") != null
				&& doc.select("div.case-location").select("a").size() > 0
				&& doc.select("div.case-location").select("a").get(0).text() != "")
			crawlData.setLocation(doc.select("div.case-location").select("a")
					.get(0).text());
		else {
			crawlData.setLocation("");
		}
		return crawlData;

	}

	public String downloadAudioFilesFromWebPage(String filename, Document doc)
			throws IOException {
		// "-argument.mp3"
		Elements links = doc.select(".audio");
		for (Element mp3 : links) {
			if (mp3.attr("href").contains(".mp3")) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();

				}
				return downloadTranscriptMp3File(filename, mp3);
				// Once mp3 found, no need to continue for loop
			}
		}
		return "";
	}

	private String downloadTranscriptMp3File(String filename, Element mp3)
			throws IOException {
		ConsoleView.printlInConsoleln("Downloading " + baseUrl
				+ mp3.attr("href"));
		Response audio;
		FileOutputStream fos;
		audio = downloadAudio(mp3);
		File file = new File(outputDir + "/" + filename + "-argument.mp3");
		fos = new FileOutputStream(file);
		fos.write(audio.bodyAsBytes());
		fos.close();
		if (FileUtils.sizeOf(file) <= 0) {
			file.delete();
		}
		return file.getAbsolutePath();
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
