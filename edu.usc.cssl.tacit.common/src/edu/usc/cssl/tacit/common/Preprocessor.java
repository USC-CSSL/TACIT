package edu.usc.cssl.tacit.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.annolab.tt4j.TreeTaggerException;
import org.apache.commons.io.FileUtils;

import edu.usc.cssl.tacit.common.snowballstemmer.DanishStemmer;
import edu.usc.cssl.tacit.common.snowballstemmer.DutchStemmer;
import edu.usc.cssl.tacit.common.snowballstemmer.EnglishStemmer;
import edu.usc.cssl.tacit.common.snowballstemmer.FinnishStemmer;
import edu.usc.cssl.tacit.common.snowballstemmer.FrenchStemmer;
import edu.usc.cssl.tacit.common.snowballstemmer.GermanStemmer;
import edu.usc.cssl.tacit.common.snowballstemmer.HungarianStemmer;
import edu.usc.cssl.tacit.common.snowballstemmer.ItalianStemmer;
import edu.usc.cssl.tacit.common.snowballstemmer.NorwegianStemmer;
import edu.usc.cssl.tacit.common.snowballstemmer.SnowballStemmer;
import edu.usc.cssl.tacit.common.snowballstemmer.TurkishStemmer;
import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CMDataType;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class Preprocessor {

	private String ppDir = "";
	private boolean doLowercase = false;
	private boolean doStemming = false;
	private boolean doStopWords = false;
	private boolean doCleanUp = true;
	private boolean isLatin = false;
	private String delimiters = " .,;'\"!-()[]{}:?";
	private String ppOutputPath;
	private HashSet<String> stopWordsSet = new HashSet<String>();
	private ArrayList<String> outputFiles = new ArrayList<String>();
	SnowballStemmer stemmer = null;
	LatinStemFilter latinStemmer = null;
	private String stemLang;
	private String currTime;
	private String latinStemLocation;
	private String tempPPFile = System.getProperty("user.dir")
			+ System.getProperty("file.separator") + "tacit_temp_files"
			+ System.getProperty("file.separator");

	public ArrayList<String> processData(String caller, List<Object> inData,
			boolean doPreprocessing) throws IOException {

		createppDir(caller);
		setupParams(doPreprocessing);

		for (Object obj : inData) {
			if (obj instanceof CorpusClass) {
				processCorpus((CorpusClass) obj, doPreprocessing);
			} else {
				File inputFile = new File((String) obj);
				if (inputFile.isDirectory()) {
					processDirectory(inputFile.getAbsolutePath(),
							doPreprocessing);
				} else {
					if (inputFile.getName().contains("DS_Store"))
						continue;

					if (doPreprocessing) {
						String ppFile = inputFile.getAbsolutePath();
						if (ppFile != "")
							outputFiles.add(processFile(ppFile, ""));
					} else {
						outputFiles.add(inputFile.getAbsolutePath());
					}
				}
			}
		}

		return outputFiles;
	}

	private void processDirectory(String dirpath, boolean doPreprocessing) {
		File[] files = new File(dirpath).listFiles();

		if (doPreprocessing) {
			for (File file : files) {
				if (file.isDirectory()) {
					processDirectory(file.getAbsolutePath(), doPreprocessing);
				} else {
					String ppFile = processFile(file.getAbsolutePath(), "");
					if (ppFile != "")
						outputFiles.add(ppFile);
				}
			}
		} else {
			for (File file : files) {
				if (file.getName().contains("DS_Store"))
					continue;

				if (file.isDirectory())
					processDirectory(file.getAbsolutePath(), doPreprocessing);
				else
					outputFiles.add(file.getAbsolutePath());
			}
		}
	}

	private String processFile(String inFile, String outName) {

		String outFile;
		if (outName == "" || outName == null) {
			outFile = ppDir + System.getProperty("file.separator")
					+ (new File(inFile).getName());
		} else {
			outFile = ppDir + System.getProperty("file.separator") + outName;
		}

		try {
			BufferedReader br = new BufferedReader(new FileReader(inFile));
			if (new File(outFile).exists()) {
				for (int i = 1; i < Integer.MAX_VALUE; i++) {
					if (new File(outFile + Integer.toString(i)).exists()) {
						continue;
					} else {
						outFile = outFile + Integer.toString(i);
						break;
					}
				}
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));

			String currLine = "";

			while ((currLine = br.readLine()) != null) {
				if (currLine != "") {
					if (doLowercase) {
						currLine.toLowerCase();
					}

					for (char c : delimiters.toCharArray()) {
						currLine = currLine.replace(c, ' ');
					}

					if (doStopWords) {
						currLine = removeStopWords(currLine);
					}

					if (doStemming) {
						if (isLatin) {
							try {
								currLine = latinStemmer.doStemming(currLine);
							} catch (TreeTaggerException e) {
								ConsoleView
										.printlInConsole("Error stemming the line: "
												+ currLine);
								ConsoleView
										.printlInConsole("Skipping the line and continuing.");
							}
							latinStemmer.destroyTT();
						} else {
							currLine = stemLine(currLine);
						}
					}
					bw.write(currLine + "\n");
				}
			}

			bw.close();
			br.close();
		} catch (FileNotFoundException e) {
			ConsoleView.printlInConsoleln("Error in input file path " + inFile);
			return "";
		} catch (IOException e) {
			ConsoleView.printlInConsoleln("I/O issues with file " + inFile);
		}
		return outFile;
	}

	private String stemLine(String line) {
		if (line.isEmpty())
			return "";
		StringBuilder returnString = new StringBuilder();
		String[] wordArray = line.split("\\s+");
		for (String word : wordArray) {
			stemmer.setCurrent(word);
			String stemmedWord = "";
			if (stemmer.stem())
				stemmedWord = stemmer.getCurrent();
			if (!stemmedWord.equals(""))
				word = stemmedWord;
			returnString.append(word);
			returnString.append(' ');
		}
		return returnString.toString();
	}

	private String removeStopWords(String line) {
		StringBuilder returnString = new StringBuilder();
		String[] wordArray = line.split("\\s+");
		for (String word : wordArray) {
			if (!stopWordsSet.contains(word.toLowerCase())) {
				returnString.append(word);
				returnString.append(' ');
			}
		}
		return returnString.toString();
	}

	/*
	 * CorpusType = Json, doPreprocessing = True: Traverse through all the data
	 * of the corpus. Check if the Json data satisfies query parameters. If it
	 * does, create a single temp file (store the name of this file in the
	 * tempPPFile variable, so that the same file can be reused. I have already
	 * written the code to delete this file once the analysis is over)
	 * corresponding to that unit of data. Pass this temp file to processFile to
	 * perform all preprocessing tasks. Add the output of processFile to
	 * outputFiles.
	 * 
	 * CorpusType = Json, doPreprocessing = False: Create temp files out of the
	 * Json data (Store these temp files the same way they were being stored
	 * earlier) if the query is satisfied and add them to outputFiles
	 */
	private void processCorpus(CorpusClass corpus, boolean doPreprocessing) {
		String corpusClassPath = ((CorpusClass) corpus).getClassPath();
		CMDataType corpusType = ((CorpusClass) corpus).getParent()
				.getDatatype();

		switch (corpusType) {
		case PLAIN_TEXT:
			processDirectory(corpusClassPath, doPreprocessing);
			break;

		case REDDIT_JSON:

			break;

		case TWITTER_JSON:

			break;

		default:
			break;
		}
	}

	private void setupParams(boolean doPreprocessing) throws IOException {
		if (doPreprocessing) {
			// Setup global parameters
			String stopwordsFile = CommonUiActivator.getDefault()
					.getPreferenceStore().getString("stop_words_path");
			delimiters = CommonUiActivator.getDefault().getPreferenceStore()
					.getString("delimeters");
			stemLang = CommonUiActivator.getDefault().getPreferenceStore()
					.getString("language");
			doLowercase = Boolean.parseBoolean(CommonUiActivator.getDefault()
					.getPreferenceStore().getString("islower_case"));
			doStemming = Boolean.parseBoolean(CommonUiActivator.getDefault()
					.getPreferenceStore().getString("isStemming"));
			doStopWords = Boolean.parseBoolean(CommonUiActivator.getDefault()
					.getPreferenceStore().getString("removeStopWords"));
			doCleanUp = Boolean.parseBoolean(CommonUiActivator.getDefault()
					.getPreferenceStore().getString("ispreprocessed"));
			ppOutputPath = CommonUiActivator.getDefault().getPreferenceStore()
					.getString("pp_output_path");
			latinStemLocation = CommonUiActivator.getDefault()
					.getPreferenceStore().getString("latin_stemmer");

			SimpleDateFormat sdfDate = new SimpleDateFormat(
					"yyyy-MM-dd-HH-mm-ss");
			Date now = new Date();
			this.currTime = sdfDate.format(now);

			// Setup stop words set
			if (doStopWords) {
				String currentLine;
				File sfile = new File(stopwordsFile);
				if (!sfile.exists() || sfile.isDirectory()) {
					ConsoleView
							.printlInConsoleln("Stop Words file is not valid. Please provide a correct file path");
					throw new IOException();
				}
				BufferedReader br = new BufferedReader(new FileReader(new File(
						stopwordsFile)));
				while ((currentLine = br.readLine()) != null) {
					stopWordsSet.add(currentLine.trim().toLowerCase());
				}
				br.close();
			}

			// Setup Stemmer
			if (doStemming) {
				if (stemLang.equals("LATIN")) {
					isLatin = true;
					latinStemmer = new LatinStemFilter(latinStemLocation);
				} else {
					stemmer = stemSelect(stemLang);
				}
			}
		}
	}

	private void createppDir(String caller) {
		tempPPFile = tempPPFile + caller + "_" + System.currentTimeMillis();
		if (ppOutputPath == null || ppOutputPath.trim().length() == 0) {
			String tempOutputPath = System.getProperty("user.dir")
					+ System.getProperty("file.separator") + "ppFiles";

			if (!(new File(tempOutputPath).exists())) {
				new File(tempOutputPath).mkdir();
			} else {
				ppDir = tempOutputPath + System.getProperty("file.separator")
						+ caller + "_" + currTime;
			}
		} else {
			ppDir = ppOutputPath + System.getProperty("file.separator")
					+ caller + "_" + currTime;
			new File(ppDir).mkdir();
		}
	}

	private SnowballStemmer stemSelect(String stemLang) {
		if (stemLang.toUpperCase().equals("EN")) {
			return new EnglishStemmer();
		} else if (stemLang.toUpperCase().equals("DE")) {
			return new GermanStemmer();
		} else if (stemLang.toUpperCase().equals("FR")) {
			return new FrenchStemmer();
		} else if (stemLang.toUpperCase().equals("IT")) {
			return new ItalianStemmer();
		} else if (stemLang.toUpperCase().equals("DA")) {
			return new DanishStemmer();
		} else if (stemLang.toUpperCase().equals("NL")) {
			return new DutchStemmer();
		} else if (stemLang.toUpperCase().equals("FI")) {
			return new FinnishStemmer();
		} else if (stemLang.toUpperCase().equals("HU")) {
			return new HungarianStemmer();
		} else if (stemLang.toUpperCase().equals("NO")) {
			return new NorwegianStemmer();
		} else if (stemLang.toUpperCase().equals("TR")) {
			return new TurkishStemmer();
		}
		return null;
	}

	public void clean() {

		if (new File(tempPPFile).exists()) {
			new File(tempPPFile).delete();
		}

		if (ppDir != "") {
			if (doCleanUp) {
				File toDel = new File(ppDir);
				try {
					if (toDel.exists())
						FileUtils.deleteDirectory(toDel);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}
}
