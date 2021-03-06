package edu.usc.cssl.tacit.crawlers.uscongress.ui.internal;

import java.io.File;

public interface IUsCongressCrawlerViewConstants {
	public static final String IMAGE_LRUN_OBJ = "lrun_obj";
	public static final String IMAGE_HELP_CO = "help_co";
	public static final String IMAGE_US_CONGRESS_OBJ = "uscongress";
	String DEFAULT_CORPUS_LOCATION = System.getProperty("user.dir") + System.getProperty("file.separator") + "json_corpuses" + File.separator+ "congress";
}
