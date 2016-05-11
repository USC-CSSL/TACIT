package edu.usc.cssl.tacit.crawlers.uscongress;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import edu.usc.cssl.tacit.crawlers.uscongress.services.AvailableRecords;
import edu.usc.cssl.tacit.crawlers.uscongress.services.UsCongressCrawler;

public class Us_Congress_Crawler_Test {
	final String directoryPath = new File("TestData").getAbsolutePath();

	@Test
	public void usCongressCrawlerTest() {
		ArrayList<String> congressMemberDetails = new ArrayList<String>();
		congressMemberDetails.add("Alexander, Lamar (R-TN)");
		ArrayList<Integer> allCongresses = new ArrayList<Integer>();
		allCongresses.add(114);
		allCongresses.add(113);
		allCongresses.add(112);
		allCongresses.add(111);
		allCongresses.add(110);
		allCongresses.add(109);
		allCongresses.add(108);
		allCongresses.add(107);
		allCongresses.add(106);
		allCongresses.add(105);
		allCongresses.add(104);
		allCongresses.add(103);
		allCongresses.add(102);
		allCongresses.add(101);
		String corpusName = "Test";
		UsCongressCrawler sc = new UsCongressCrawler();
		String outputDir = directoryPath + System.getProperty("file.separator") + corpusName;
		File temp = new File(outputDir);
		temp.mkdir();
		Exception exceptionObj = null;
		try {
			String s[] = { "All", "114", "113", "112", "111", "110", "109", "108", "107", "106", "105", "104", "103",
					"102", "101" };
			AvailableRecords.getAllSenators(s);
			sc.initialize("Date", 1, 114, congressMemberDetails, "", "", outputDir, allCongresses,
					new NullProgressMonitor(), 40, true, true, false, true, true);
			sc.crawl();
			FileUtils.deleteDirectory(new File(outputDir));
		} catch (Exception e) {
			exceptionObj = e;
		}
		assertEquals("Checking if the crawling completed successfully", exceptionObj, null);
	}
}
