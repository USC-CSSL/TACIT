package edu.usc.cssl.nlputils.common.ui.validation;

import java.io.File;

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

	public String validateOutputDirectory(String location,String label) {
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
			return label+ " location cannot be empty";
		}
	}

}
