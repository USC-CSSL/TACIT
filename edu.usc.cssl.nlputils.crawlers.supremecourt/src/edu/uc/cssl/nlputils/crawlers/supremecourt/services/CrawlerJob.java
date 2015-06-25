package edu.uc.cssl.nlputils.crawlers.supremecourt.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.usc.cssl.nlputils.common.ui.views.ConsoleView;

public class CrawlerJob {

	private String filter;
	private String outputDir;
	private boolean truncate;
	private boolean downloadAudio;
	private String baseUrl;
	private String url;
	private IProgressMonitor monitor;
	private FileWriter fileWriter;
	private BufferedWriter bw;

	public CrawlerJob(String filter, String outputDir, String crawlUrl,
			IProgressMonitor monitor, boolean downloadAudio, boolean truncate) {

		this.filter = filter;
		this.outputDir = outputDir;
		this.truncate = truncate;
		this.downloadAudio = downloadAudio;
		this.baseUrl = crawlUrl;
		this.monitor = monitor;
		openSummaryFile();
	}

	private void openSummaryFile() {
		try {
			fileWriter = new FileWriter(this.outputDir + "/" + "summary_"
					+ System.currentTimeMillis() + ".csv");
			bw = new BufferedWriter(fileWriter);

			addContentsToSummary("Case", "Docket No", "Argued", "Decided",
					"Majority Author", "Vote", "File Type","File name");
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

	public void run(String url) throws IOException {
		crawl(url);

	}

	protected Document retrieveDocumentFromUrl(String url) throws IOException {
		Document doc = null;
		doc = Jsoup.connect(url).timeout(10 * 1000).get();
		return doc;
	}

	public void crawl(String url) throws IOException {

		Document doc = retrieveDocumentFromUrl(url);
		Element table = doc.select("tbody").get(0);
		Elements rows = table.select("tr");
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
			String filename = row.select("td").get(1).text().trim() + "_"
					+ date.substring(6) + date.substring(0, 2)
					+ date.substring(3, 5);
			ConsoleView.printlInConsoleln(contenturl + ", " + filename);

			// Fixing the unhandled exception without cascading.
			try {
				String fileName = getFiles(contenturl, filename);
				if (fileName.length() > 1){
					if(fileName.contains(",")){
						addContentsToSummary(row.select("td").get(0).text(), row
								.select("td").get(1).text(), row.select("td")
								.get(2).text(), row.select("td").get(3).text(), row
								.select("td").get(4).text(), row.select("td")
								.get(5).text(), "Transcript",fileName.split(",")[0]);
						addContentsToSummary(row.select("td").get(0).text(), row
								.select("td").get(1).text(), row.select("td")
								.get(2).text(), row.select("td").get(3).text(), row
								.select("td").get(4).text(), row.select("td")
								.get(5).text(), "Mp3",fileName.split(",")[1]);
					}
					else{
						addContentsToSummary(row.select("td").get(0).text(), row
								.select("td").get(1).text(), row.select("td")
								.get(2).text(), row.select("td").get(3).text(), row
								.select("td").get(4).text(), row.select("td")
								.get(5).text(), "Transcript",fileName);
					}
				}
					
			} catch (IOException e) {
				ConsoleView.printlInConsoleln("Error Accessing the URL "
						+ contenturl);
				e.printStackTrace();
			}
			// break;
		}
	}

	private String getFiles(String contenturl, String filename)
			throws IOException {
		File trans = new File(this.outputDir + "/" + filename
				+ "-transcript.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(trans));

		Document doc = retrieveDocumentFromUrl(contenturl);

		Elements hidden = doc.select("div.hidden");
		if (hidden.size() == 0) {
			ConsoleView.printlInConsoleln("No data. Skipping page "
					+ contenturl);
			bw.close();
			return "";
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
		for (Element line : lines) {
			if (monitor.isCanceled()) {
				bw.close();
				throw new OperationCanceledException();

			}
			bw.write(line.text() + "\n");
		}
		bw.close();

		if (FileUtils.sizeOf(trans) <= 0) {
			trans.delete();
		}
		String dwn = "";
		if (this.downloadAudio) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();

			}
			dwn = downloadAudioFilesFromWebPage(filename, doc);
		}
		if (dwn.length() > 1) {
			return outputDir + "/" + filename + "-transcript.txt" + "," + dwn;
		}
		return outputDir + "/" + filename + "-transcript.txt";

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
