package edu.usc.cssl.tacit.common.ui.utility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.usc.cssl.tacit.common.ui.composite.from.RedditJsonHandler;
import edu.usc.cssl.tacit.common.ui.composite.from.TwitterReadJsonData;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CMDataType;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;

public class TacitUtil {
	public static List<String> refineInput(List<Object> selectedInputs) {
		Set<String> refinedInputList = new HashSet<String>();
		Pattern corpusDetector = Pattern
				.compile(".* [(]Tacit Internal Class Path: (.*)[)]");

		for (Object input : selectedInputs) {
			if (input instanceof CorpusClass) {
				String corpusClassPath = ((CorpusClass)input).getClassPath();
				CMDataType corpusType = ((CorpusClass)input).getParent().getDatatype();
				if (corpusType == null)
					continue;
				if (corpusType.equals(CMDataType.TWITTER_JSON))
					input = new TwitterReadJsonData()
							.retrieveTwitterData(corpusClassPath);
				else if (corpusType.equals(CMDataType.REDDIT_JSON))
					input = new RedditJsonHandler()
							.retrieveRedditData(corpusClassPath);
				else
					input = corpusClassPath;
			}
			File inputFile = new File((String) input);
			if (!inputFile.exists())
				continue;
			if (!inputFile.isDirectory())
				refinedInputList.add(inputFile.getAbsolutePath());
			else
				refinedInputList.addAll(getFilesFromFolder(inputFile
						.getAbsolutePath()));
		}
		return new ArrayList<String>(refinedInputList);
	}

	public static List<String> getFilesFromFolder(String folderPath) {
		File folder = new File(folderPath);
		List<String> subFiles = new ArrayList<String>();
		if (!folder.exists() || !folder.isDirectory())
			return subFiles;
		for (File f : folder.listFiles()) {
			if (!f.isDirectory())
				subFiles.add(f.getAbsolutePath());
			else
				subFiles.addAll(getFilesFromFolder(f.getAbsolutePath()));
		}
		return subFiles;
	}

}