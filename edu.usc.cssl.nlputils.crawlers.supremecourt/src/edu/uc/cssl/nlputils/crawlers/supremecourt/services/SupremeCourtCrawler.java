package edu.uc.cssl.nlputils.crawlers.supremecourt.services;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SupremeCourtCrawler {

	private String filter, url, outputDir;
	private boolean truncate, downloadAudio;
	private String baseUrl;
	private CrawlerJob crawler;

	public SupremeCourtCrawler(String filter, String outputDir, String crawlUrl) {
		this.filter = filter;
		this.outputDir = outputDir;
		this.truncate = false;
		this.downloadAudio = false;
		this.baseUrl = crawlUrl;
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

	public void looper(IProgressMonitor monitor) throws IOException {
		int noOfPages = 0;
		Document doc = Jsoup.connect(url).timeout(10 * 1000).get();
		Elements pages = doc.getElementsByClass("pager-last");
		monitor.subTask("Finding Total number of cases...");
		// Sometimes the pager element is absent
		if (!pages.isEmpty()) {
			Element pageList = pages.get(0);
			Pattern pattern = Pattern.compile("page=([0-9]+)");
			Matcher matcher = pattern.matcher(pageList.toString());
			if (matcher.find()) {
				noOfPages = Integer.parseInt(matcher.group(1));
			}
		}
		monitor.worked(10);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();

		}
		// ExecutorService executor = Executors.newFixedThreadPool(5);
		crawler = new CrawlerJob(this.filter, getOutputDir(), this.baseUrl,
				monitor, isDownloadAudio(), isTruncate());

		try {
			for (int i = 0; i <= noOfPages; i++) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();

				}
			//	monitor.subTask("crawling " + url);

				crawler.run(url + "&page=" + i,noOfPages);

			}
		} catch (IOException exc) {
			crawler.summaryFileClose();
			throw exc;
		}
		crawler.summaryFileClose();
		monitor.worked(100);
		// This will make the executor accept no new threads
		// and finish all existing threads in the queue

	}

}
