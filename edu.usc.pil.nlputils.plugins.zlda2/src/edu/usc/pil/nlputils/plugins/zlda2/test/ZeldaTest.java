package edu.usc.pil.nlputils.plugins.zlda2.test;

import edu.usc.pil.nlputils.plugins.zlda2.process.Zlda2;

public class ZeldaTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Zlda2 z2 = new Zlda2();
		z2.callPython("/Users/ashmaverick/Downloads/zlab-0.1/topics.txt","/Users/ashmaverick/Downloads/zlab-0.1/sampledocs/","10","/Users/ashmaverick/Downloads/zlab-0.1/outputFile.txt");
		//z2.callPython("topics.txt","sampledocs","10","/Users/ashmaverick/Downloads/zlab-0.1/outputFile.txt");
	}

}
