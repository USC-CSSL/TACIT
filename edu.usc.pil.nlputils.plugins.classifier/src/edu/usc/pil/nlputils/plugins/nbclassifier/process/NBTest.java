package edu.usc.pil.nlputils.plugins.nbclassifier.process;

import java.io.IOException;

import bsh.EvalError;

public class NBTest {

	public static void main(String[] args) {
		NBClassifier nb = new NBClassifier();
		try {
			nb.doClassification("c:\\test\\svm\\small\\ham", "c:\\test\\svm\\small\\spam", "c:\\test\\svm\\small_test\\ham", "c:\\test\\svm\\small_test\\spam", "c:\\test\\svm\\small_test", false, false);
			//nb.doValidation("c:\\test\\svm\\small\\ham", "c:\\test\\svm\\small\\spam", "c:\\test\\svm\\small_test\\ham", "c:\\test\\svm\\small_test");
		} catch (IOException | EvalError e) {
			e.printStackTrace();
		};

	}

}
