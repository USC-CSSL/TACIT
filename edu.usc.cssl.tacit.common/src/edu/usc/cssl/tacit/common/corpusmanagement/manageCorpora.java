package edu.usc.cssl.tacit.common.corpusmanagement;

import java.io.File;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpus;

public class manageCorpora {

	static String rootDir = System.getProperty("user.dir")
			+ System.getProperty("file.separator") + "tacit_corpora"
			+ System.getProperty("file.separator");

	public static void saveCorpus(ArrayList<ICorpus> corporaList) {
		for (ICorpus corpus : corporaList) {
			String corpusID = corpus.getCorpusId();
			String corpusLocation = rootDir + corpusID;
			if ((new File(corpusLocation).exists())) {
				// Add code for everything
				new File(corpusLocation).mkdir();
				String metaFile = corpusLocation
						+ System.getProperty("file.separator") + "meta.txt";

				File metaFp = new File(metaFile);
				
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("corpus_name","abc");
				jsonObj.put("data_type",corpus.getDatatype());
				jsonObj.put("num_classes",corpus.getClasses().size());
				
			} else {
				// corpus already exits, check what has changed
			}
		}
	}
}
