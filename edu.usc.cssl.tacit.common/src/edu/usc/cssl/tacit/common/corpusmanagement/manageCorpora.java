package edu.usc.cssl.tacit.common.corpusmanagement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.Corpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;

public class manageCorpora {

	static String rootDir = System.getProperty("user.dir")
			+ System.getProperty("file.separator") + "tacit_corpora"
			+ System.getProperty("file.separator");

	@SuppressWarnings("unchecked")
	public static void saveCorpuses(ArrayList<Corpus> corporaList) {
		for (Corpus corpus : corporaList) {
			saveCorpus(corpus);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void saveCorpus(Corpus corpus) {
		String corpusID = corpus.getCorpusId();
		String corpusLocation = rootDir + corpusID;
		if (!(new File(corpusLocation).exists())) {
			// Add code for everything
			new File(corpusLocation).mkdir();
			String metaFile = corpusLocation
					+ System.getProperty("file.separator") + "meta.txt";

			File metaFp = new File(metaFile);

			JSONObject jsonObj = new JSONObject();
			jsonObj.put("corpus_name", corpus.getCorpusId());
			jsonObj.put("data_type", corpus.getDatatype().toString());
			jsonObj.put("num_classes",
					Integer.toString(corpus.getClasses().size()));

			int numClasses = corpus.getClasses().size();
			ArrayList<ICorpusClass> corporaClasses = (ArrayList<ICorpusClass>) corpus
					.getClasses();

			JSONArray classArray = new JSONArray();

			for (int i = 0; i < numClasses; i++) {
				CorpusClass currClass = (CorpusClass) corporaClasses.get(i);
				JSONObject classObj = new JSONObject();
				classObj.put("class_name", currClass.getClassName());
				classObj.put("original_loc", currClass.getClassPath());

				String[] dirParts = currClass.getClassName().split(
						System.getProperty("file.separator"));
				String dirName = dirParts[dirParts.length - 1];
				classObj.put(
						"tacit_loc",
						corpusLocation
								+ System.getProperty("file.separator")
								+ dirName);

				classArray.add(classObj);
			}

			jsonObj.put("class_details", classArray);
			jsonObj.put("num_analysis", "0");
			JSONArray analysisArray = new JSONArray();
			jsonObj.put("prev_analysis", analysisArray);

			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(
						metaFp));
				bw.write(jsonObj.toString());
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			copyCorpus(jsonObj);

		} else {
			// corpus already exits, check what has changed

			String metaFile = corpusLocation
					+ System.getProperty("file.separator") + "meta.txt";

			File metaFp = new File(metaFile);

			JSONObject jsonObj = new JSONObject();
			jsonObj.put("corpus_name", corpus.getCorpusId());
			jsonObj.put("data_type", corpus.getDatatype());
			jsonObj.put("num_classes",
					Integer.toString(corpus.getClasses().size()));

			int numClasses = corpus.getClasses().size();
			ArrayList<ICorpusClass> corporaClasses = (ArrayList<ICorpusClass>) corpus
					.getClasses();

			JSONArray classArray = new JSONArray();
		}
	
	}

	private static void copyCorpus(JSONObject jsonObj) {
		int numClasses = (Integer) jsonObj.get("num_classes");
		JSONArray classArray = (JSONArray) jsonObj.get("class_details");

		for (int i = 0; i < numClasses; i++) {
			String originalLoc = (String) ((JSONObject) classArray.get(i))
					.get("original_loc");
			String tacitLoc = (String) ((JSONObject) classArray.get(i))
					.get("tacit_loc");

			new File(tacitLoc).mkdir();

			try {
				FileUtils.copyDirectory(new File(originalLoc), new File(
						tacitLoc));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
