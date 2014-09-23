package edu.usc.pil.nlputils.plugins.preprocessor.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.cybozu.labs.langdetect.LangDetectException;

import edu.usc.pil.nlputils.plugins.preprocessor.process.Preprocess;

public class PreprocessTest {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		/*
		File test = new File("testis.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(test));
		String turk = "İnsan oğulları";
		System.out.println(turk);
		bw.write(turk+"\n");
		System.out.println(turk.toLowerCase());
		bw.write(turk.toLowerCase()+"\n");
		bw.close();
		*/
		
		String[] input = new String[1];
		input[0] = "C:\\LIWC\\Unicode\\turkish.txt";
		Preprocess pp = new Preprocess(input,null,"C:\\LIWC\\Unicode\\","_pre",".,",true,true,"TR");
		try {
			pp.doPreprocess();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LangDetectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
