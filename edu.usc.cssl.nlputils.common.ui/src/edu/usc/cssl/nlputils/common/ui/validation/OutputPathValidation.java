package edu.usc.cssl.nlputils.common.ui.validation;

import java.io.File;
import java.nio.file.Files;

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

	public String validateOutputDirectory(String location) {
		if (location.length() > 0) {
			File locationFile = new File(location);
			if (locationFile.exists()) { // check location exists
				if (Files.isWritable(locationFile.toPath())) {
					return null;
				} else {
					return "Permission Denied";
				}
			} else {
				return "Location doesn't exist";
			}

		} else {
			return "Location cannot be initial";
		}
	}

}
