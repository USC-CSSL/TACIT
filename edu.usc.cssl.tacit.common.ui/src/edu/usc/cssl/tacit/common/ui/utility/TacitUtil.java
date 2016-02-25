package edu.usc.cssl.tacit.common.ui.utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import edu.usc.cssl.tacit.common.ui.composite.from.RedditJsonHandler;
import edu.usc.cssl.tacit.common.ui.composite.from.TwitterReadJsonData;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CMDataType;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;

public class TacitUtil {
	public String summaryFile = null;
	public Map<String, String[]> filenameCorpusMap = new HashMap<String, String[]>();

	public List<String> refineInput(List<Object> selectedInputs) {
		Set<String> refinedInputList = new HashSet<String>();
		Pattern corpusDetector = Pattern
				.compile(".* [(]Tacit Internal Class Path: (.*)[)]");
		TwitterReadJsonData twitterParser = new TwitterReadJsonData();
		boolean isTwitterParsed = false;
		for (Object input : selectedInputs) {
			String corpusName = "NIL", corpusClassName = "";
			Boolean isCorpus = false;
			if (input instanceof CorpusClass) {
				isCorpus = true;
				corpusName = ((CorpusClass) input).getParent().getCorpusName();
				corpusClassName = ((CorpusClass) input).getClassName();
				String corpusClassPath = ((CorpusClass) input).getClassPath();
				CMDataType corpusType = ((CorpusClass) input).getParent()
						.getDatatype();
				if (corpusType == null)
					continue;
				if (corpusType.equals(CMDataType.TWITTER_JSON)) {
					input = twitterParser.retrieveTwitterData(corpusClassPath);
					summaryFile = twitterParser.getSummaryFile();
					isTwitterParsed = true;
				} else if (corpusType.equals(CMDataType.REDDIT_JSON)) {
					input = new RedditJsonHandler()
							.retrieveRedditData(corpusClassPath);

				} else
					input = corpusClassPath;
			}
			File inputFile = new File((String) input);
			if (!inputFile.exists())
				continue;
			if (!inputFile.isDirectory()) {
				refinedInputList.add(inputFile.getAbsolutePath());
				if (isCorpus)
					filenameCorpusMap.put(inputFile.getAbsolutePath(),
							new String[] { corpusName, corpusClassName });

			} else {
				for (String filename : getFilesFromFolder(inputFile
						.getAbsolutePath())) {
					refinedInputList.add(filename);
					if(isCorpus) 
						filenameCorpusMap.put(filename, new String[] { corpusName,
								corpusClassName });

				}
			}
		}
		
		if (isTwitterParsed){
			twitterParser.summaryFileClose();
		}
		
		return new ArrayList<String>(refinedInputList);
	}

	public Map<String, String[]> getFileCorpusMembership() {
		return filenameCorpusMap;
	}
	
	public void writeSummaryFile(String outputPath) {
		if (summaryFile == null)
			return;
		try {
			FileUtils.copyFileToDirectory(new File(summaryFile), new File(
					outputPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<String> getFilesFromFolder(String folderPath) {
		File folder = new File(folderPath);
		List<String> subFiles = new ArrayList<String>();
		if (!folder.exists() || !folder.isDirectory())
			return subFiles;
		for (File f : folder.listFiles()) {
			if (f.getName().endsWith(".csv"))
				continue;
			if (!f.isDirectory())
				subFiles.add(f.getAbsolutePath());
			else
				subFiles.addAll(getFilesFromFolder(f.getAbsolutePath()));
		}
		return subFiles;
	}

}