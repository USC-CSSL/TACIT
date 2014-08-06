package edu.usc.pil.nlputils.plugins.lda.process;

import java.io.FileNotFoundException;
import java.io.IOException;

public class LDATest {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		LDA lda = new LDA();
		lda.doLDA("c:\\mallet\\dirs\\ham", true, true, "10", "c:\\mallet\\dirs");
	}
}
