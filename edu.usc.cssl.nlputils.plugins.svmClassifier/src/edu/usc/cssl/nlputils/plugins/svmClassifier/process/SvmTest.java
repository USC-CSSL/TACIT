package edu.usc.cssl.nlputils.plugins.svmClassifier.process;

import java.io.IOException;

public class SvmTest {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		SvmClassifier svm = new SvmClassifier("ham", "spam" , "c:\\liwc\\cross_output");
		
		CrossValidator cv = new CrossValidator();
		cv.doCross(svm, "ham","c:\\liwc\\folder1", "spam", "c:\\liwc\\folder2", 5, true);
		
		/*
		try {
		
			//svm.loadPretrainedModel("HAM","SPAM","c:\\singular\\pretrained.model","c:\\singular\\pretrained.hashmap");
			//svm.classify("HAM", "SPAM", "c:\\singular\\bigclassify");
			//svm.predict("HAM", "c:\\singular\\bigtest\\ham", "SPAM", "c:\\singular\\bigtest\\spam");
			//svm.outputPredictedOnly("HAM", "SPAM", "c:\\singular\\bigclassify", "c:\\singular\\bigcsv.csv");
			//svm.loadPretrainedModel("HAM", "SPAM", "C:\\Users\\45W1N\\NLPUtils-application\\edu.usc.pil.nlputils.plugins.svmClassifier\\HAM_SPAM_7-30-2014-1406758879611.model","C:\\Users\\45W1N\\NLPUtils-application\\edu.usc.pil.nlputils.plugins.svmClassifier\\HAM_SPAM_7-30-2014-1406758879611.hashmap");
			//svm.train("HAM", "c:\\singular\\train\\ham", "SPAM", "c:\\singular\\train\\spam",true);
			//svm.classify("HAM", "SPAM", "c:\\singular\\classify");
			//svm.outputPredictedOnly("HAM", "SPAM", "c:\\singular\\classify", "c:\\test\\svm\\csv.csv");
			//svm.predict("HAM", "c:\\singular\\test\\ham", "SPAM", "c:\\singular\\test\\spam");
			//svm.output("HAM", "c:\\singular\\test\\ham", "SPAM", "c:\\singular\\test\\spam", "c:\\test\\svm\\csv.csv");
			
			//svm.train("HAM", "c:\\spam_training\\ham", "SPAM", "c:\\spam_training\\spam",true);
			svm.train("HAM", "c:\\test\\svm\\small\\ham", "SPAM", "c:\\test\\svm\\small\\spam", true,false,"",true,false);
			//System.out.println(Math.log10(10));
			//svm.predict("HAM", "c:\\test\\svm\\small_test\\ham", "SPAM", "c:\\test\\svm\\small_test\\spam");
			//svm.output("HAM", "c:\\test\\svm\\small_test\\ham", "SPAM", "c:\\test\\svm\\small_test\\spam", "c:\\test\\svm\\csv.csv");
		//svm.predict("HAM", "c:\\test\\svm\\ham_test", "SPAM", "c:\\test\\svm\\spam_test","c:\\test\\svm\\hamspam.out");
		//svm.classify("C:\\Test\\svm\\a1a.t.test", "C:\\Test\\svm\\a1a.train.model", "C:\\Test\\svm\\geez.out");
		//svm.classify("C:\\Users\\45W1N\\RCPworkspace\\edu.usc.pil.nlputils.plugins.svmClassifier\\HAM_SPAM_7-28-2014-1406578668867.test", "C:\\Users\\45W1N\\RCPworkspace\\edu.usc.pil.nlputils.plugins.svmClassifier\\HAM_SPAM_7-28-2014-1406578668867.model", "C:\\Test\\svm\\geez2.out");
		} catch (Exception ie) {
			ie.printStackTrace();
		}
		*/
		
	}

}
