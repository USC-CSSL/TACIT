package edu.usc.nlputils.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import edu.usc.cssl.nlputils.common.ui.CommonUiActivator;
import edu.usc.cssl.nlputils.common.ui.IPreprocessorSettingsConstant;
import edu.usc.cssl.nlputils.common.ui.views.ConsoleView;
import edu.usc.nlputils.common.snowballstemmer.DanishStemmer;
import edu.usc.nlputils.common.snowballstemmer.DutchStemmer;
import edu.usc.nlputils.common.snowballstemmer.EnglishStemmer;
import edu.usc.nlputils.common.snowballstemmer.FinnishStemmer;
import edu.usc.nlputils.common.snowballstemmer.FrenchStemmer;
import edu.usc.nlputils.common.snowballstemmer.GermanStemmer;
import edu.usc.nlputils.common.snowballstemmer.HungarianStemmer;
import edu.usc.nlputils.common.snowballstemmer.ItalianStemmer;
import edu.usc.nlputils.common.snowballstemmer.NorwegianStemmer;
import edu.usc.nlputils.common.snowballstemmer.SnowballStemmer;
import edu.usc.nlputils.common.snowballstemmer.TurkishStemmer;

public class Preprocess {
	private boolean doLowercase = false;
	private boolean doStemming = false;
	private boolean doStopWords = false;
	private boolean doLangDetect = false;
	private boolean doCleanUp = false;
	private String delimiters = " .,;'\"!-()[]{}:?";
	// private String[] inputFiles;
	private String outputPath;
	private String stopwordsFile;
	private HashSet<String> stopWordsSet = new HashSet<String>();
	SnowballStemmer stemmer = null;
	private String stemLang;
	private String callingPlugin;
	private String currTime;
	private String preprocessingParentFolder;

	public Preprocess(String caller) {
		this.stopwordsFile = CommonUiActivator.getDefault()
				.getPreferenceStore().getString("stop_words_path");
		this.delimiters = CommonUiActivator.getDefault().getPreferenceStore()
				.getString("delimeters");
		this.stemLang = CommonUiActivator.getDefault().getPreferenceStore()
				.getString("language");
		this.doLowercase = Boolean.parseBoolean(CommonUiActivator.getDefault()
				.getPreferenceStore().getString("islower_case"));
		this.doStemming = Boolean.parseBoolean(CommonUiActivator.getDefault()
				.getPreferenceStore().getString("isStemming"));
		this.doCleanUp = Boolean.parseBoolean(CommonUiActivator.getDefault()
				.getPreferenceStore().getString("ispreprocessed"));
		this.outputPath = CommonUiActivator.getDefault().getPreferenceStore()
				.getString("pp_output_path");
		this.callingPlugin = caller;
		this.currTime = String.valueOf(System.currentTimeMillis());
	}

	// for File as well as Directory
	public String doPreprocessing(List<String> inputFiles, String subFolder)
			throws IOException {

		File[] files;
		files = new File[inputFiles.size()];
		int i = 0;
		boolean outputPathNotSet = false;
		for (String filepath : inputFiles) {
			if ((new File(filepath).isDirectory()))
				continue;
			if (new File(filepath).getAbsolutePath().contains("DS_Store"))
				continue;
			files[i] = new File(filepath);
			i = i + 1;
		}

		if (this.outputPath == null || this.outputPath.trim().length() == 0) {
			this.outputPath = System.getProperty("user.dir");
			// this.outputPath = (new File(inputFiles.get(0)).getParent());
			outputPathNotSet = true;
		}
		preprocessingParentFolder = this.outputPath + File.separator
				+ callingPlugin + "_" + currTime;
		if (outputPathNotSet)
			this.outputPath = "";
		if (!(new File(preprocessingParentFolder).exists())) {
			new File(preprocessingParentFolder).mkdir();
			ConsoleView.printlInConsoleln("Folder " + preprocessingParentFolder
					+ " created successfully.");
		}
		if (subFolder.trim().length() != 0) {
			preprocessingParentFolder = preprocessingParentFolder
					+ File.separator + subFolder;
			if (new File(preprocessingParentFolder).mkdir()) {
				ConsoleView.printlInConsoleln("Folder " + preprocessingParentFolder
						+ " created successfully.");
			}
		}

		if (stopwordsFile.trim().length() != 0) {
			doStopWords = true;
			String currentLine;
			BufferedReader br = new BufferedReader(new FileReader(new File(
					stopwordsFile)));
			while ((currentLine = br.readLine()) != null) {
				stopWordsSet.add(currentLine.trim().toLowerCase());
			}
			br.close();
		}

		if (doStemming) { // If stemming has to be performed, find the
							// appropriate stemmer.
			if (stemLang.equals("AUTODETECT")) {
				doLangDetect = true;
				Bundle bundle = Platform
						.getBundle("edu.usc.cssl.nlputils.common");
				URL url = FileLocator.find(bundle, new Path("profiles"), null);
				URL fileURL = FileLocator.toFileURL(url);
				ConsoleView.printlInConsoleln(fileURL.getPath());
				try {
					DetectorFactory.loadProfile(fileURL.getPath());
				} catch (com.cybozu.labs.langdetect.LangDetectException ex) {
					// ex.printStackTrace();
					ConsoleView.printlInConsoleln("Exception code - " + ex.getCode());
					// ex.getCode().toString() -> is not visible!
				}
			} else {
				doLangDetect = false;
				stemmer = stemSelect(stemLang);
			}
		}

		for (File f : files) {
			if (f == null)
				break;
			// Mac cache file filtering
			if (f.getAbsolutePath().contains("DS_Store"))
				continue;

			if ("_preprocessed".equals(f.getName()))
				continue;
			String inputFile = f.getAbsolutePath();
			ConsoleView.printlInConsoleln("Preprocessing " + inputFile);

			// doLangDetect only if doStemming is true
			if (doLangDetect) {
				try {
					stemmer = findLangStemmer(f);
				} catch (LangDetectException e) {
					e.printStackTrace();
				}
			}

			File iFile = new File(inputFile);
			if (!iFile.exists() || iFile.isDirectory()) {
				ConsoleView.printlInConsoleln("Error in input file path "
						+ iFile.getAbsolutePath());
				continue;
			}

			File oFile = new File(preprocessingParentFolder
					+ System.getProperty("file.separator") + f.getName());
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(iFile), "UTF8"));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(oFile), "UTF-8"));

			String linear;
			while ((linear = br.readLine()) != null) {
				if (linear != "") {
					if (doLowercase)
						linear = linear.toLowerCase();
					for (char c : delimiters.toCharArray())
						linear = linear.replace(c, ' ');
					if (doStopWords)
						linear = removeStopWords(linear);
					if (doStemming && stemmer != null)
						linear = stem(linear);
					bw.write(linear + "\n");
				}
			}
			ConsoleView.printlInConsoleln(preprocessingParentFolder
					+ System.getProperty("file.separator") + f.getName());

			br.close();
			bw.close();
		}
		ConsoleView.printlInConsoleln("Preprocessed files stored in "
				+ preprocessingParentFolder);
		return preprocessingParentFolder;
	}

	private SnowballStemmer findLangStemmer(File iFile) throws IOException,
			LangDetectException {
		BufferedReader br = new BufferedReader(new FileReader(iFile));
		String sampleText = "";
		for (int i = 0; i < 10; i++) {
			String currentLine = br.readLine();
			if (currentLine == null)
				break;
			sampleText = sampleText + currentLine.trim().replace('\n', ' ');
		}
		Detector detector = DetectorFactory.create();
		detector.append(sampleText);
		String lang = detector.detect();
		br.close();
		return stemSelect(lang);
	}

	private String stem(String linear) {
		if (linear.isEmpty())
			return "";
		StringBuilder returnString = new StringBuilder();
		String[] wordArray = linear.split("\\s+");
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

	private String removeStopWords(String linear) {
		StringBuilder returnString = new StringBuilder();
		String[] wordArray = linear.split("\\s+");
		for (String word : wordArray) {
			if (!stopWordsSet.contains(word.toLowerCase())) {
				returnString.append(word);
				returnString.append(' ');
			}
		}
		return returnString.toString();
	}

	public void clean() {
		final Boolean cleanUp = Boolean.valueOf(CommonUiActivator.getDefault()
				.getPreferenceStore().getString(IPreprocessorSettingsConstant.PRE_PROCESSED));
		if(!cleanUp){
			return;
		}
		File toDel = new File(this.preprocessingParentFolder);
		try {
			if (toDel.exists())
				FileUtils.deleteDirectory(toDel);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public boolean doCleanUp() {
		return doCleanUp;
	}

}
