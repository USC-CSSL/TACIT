package edu.usc.cssl.tacit.common.ui.validation;

import java.io.File;
import java.util.List;

import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.ICorpus;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;

public class OutputPathValidation {

	private static OutputPathValidation eInstance = null;

	public static OutputPathValidation getInstance() {

		if (eInstance == null) {
			eInstance = new OutputPathValidation();
		}
		return eInstance;

	}

	private OutputPathValidation() {
	}

	public String validateOutputDirectory(String location, String label) {
		if (location.length() > 0) {
			File locationFile = new File(location);
			if (locationFile.exists()) { // check location exists
				if (locationFile.canWrite()) {
					return null;
				} else {
					return "Permission Denied";
				}
			} else {
				return "Location doesn't exist";
			}

		} else {
			return label + " location cannot be empty";
		}
	}

	public String validateOutputCorpus(String location) {
		if (location == null || location.length() == 0) {
			return "Corpus name must not be empty";
		} else if (corpusNameExists(location)) {
			return "Corpus name already exist";
		} else
			return null;
	}

	public boolean corpusNameExists(String corpusName) {
		List<ICorpus> corpuses = new ManageCorpora().getAllCorpusDetails();
		for (ICorpus corpus : corpuses) {
			if (corpus.getCorpusName().equals(corpusName))
				return true;
		}
		return false;
	}

}
