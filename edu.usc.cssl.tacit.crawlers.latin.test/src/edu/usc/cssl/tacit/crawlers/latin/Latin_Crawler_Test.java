package edu.usc.cssl.tacit.crawlers.latin;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.usc.cssl.tacit.common.ui.views.ConsoleView;
import edu.usc.cssl.tacit.crawlers.latin.services.LatinCrawler;

public class Latin_Crawler_Test{

	final String directoryPath = new File("TestData").getAbsolutePath();
	
	@Test
	public void latinCrawlerTest(){
		LatinCrawler latinCrawler = new LatinCrawler();
		String outputDir = directoryPath + System.getProperty("file.separator") +  "Test";
		File temp = new File(outputDir);
		temp.mkdir();
		Exception exceptionObj = null;
		
			latinCrawler.initialize(outputDir);
			Iterator<String> authorItr;
			int totalWork = 1;
			List<String> authors = new ArrayList<String>();
			authors.add("Abelard");
			try {
				authorItr = authors.iterator();
				totalWork = authors.size();
				int totalFilesCreated = 0;
				while (authorItr.hasNext()) {
					String author = authorItr.next();
					ConsoleView.printlInConsoleln("Crawling " + author);
					totalFilesCreated += latinCrawler.getBooksByAuthor(author,
							latinCrawler.getAuthorNames().get(author), new NullProgressMonitor());
				}
				ConsoleView.printlInConsole("Total number of files downloaded : " + totalFilesCreated);

				FileUtils.deleteDirectory(new File(outputDir));
			}
		catch (Exception e){
			exceptionObj = e;
		}
		assertEquals("Checking if the crawling completed successfully", exceptionObj, null);
	}

}
