package edu.usc.pil.nlputils.plugins.svmClassifier.process;

import java.io.IOException;

public class SvmTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SvmClassifier svm = new SvmClassifier();
		try {
		//svm.train("HAM", "c:\\spam_training\\ham", "SPAM", "c:\\spam_training\\spam");
			svm.train("HAM", "c:\\test\\svm\\small\\ham", "SPAM", "c:\\test\\svm\\small\\spam");
			svm.predict("HAM", "c:\\test\\svm\\small_test\\ham", "SPAM", "c:\\test\\svm\\small_test\\spam", "c:\\test\\svm\\smallest.out");
		//svm.predict("HAM", "c:\\test\\svm\\ham_test", "SPAM", "c:\\test\\svm\\spam_test","c:\\test\\svm\\hamspam.out");
		//svm.classify("C:\\Test\\svm\\a1a.t.test", "C:\\Test\\svm\\a1a.train.model", "C:\\Test\\svm\\geez.out");
		//svm.classify("C:\\Users\\45W1N\\RCPworkspace\\edu.usc.pil.nlputils.plugins.svmClassifier\\HAM_SPAM_7-28-2014-1406578668867.test", "C:\\Users\\45W1N\\RCPworkspace\\edu.usc.pil.nlputils.plugins.svmClassifier\\HAM_SPAM_7-28-2014-1406578668867.model", "C:\\Test\\svm\\geez2.out");
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}

}
