package edu.usc.cssl.tacit.common.ui.preprocessor;

import java.io.IOException;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class LatinStemFilter {

	public LatinStemFilter(String location) {
		
		System.setProperty("treetagger.home", location);
		tt = new TreeTaggerWrapper<String>();
		try {
			tt.setModel(FileLocator.toFileURL(
					Platform.getBundle(Activator.PLUGIN_ID).getEntry(
							"latin.par")).getPath());
		} catch (IOException e) {
			ConsoleView
					.printlInConsoleln("Error loading Latin POS tags model.");
		}
		tt.setHandler(new TokenHandler<String>() {
			public void token(String token, String pos, String lemma) {
				POS = pos;
			}
		});
		stemmer = new LatinStemmer();
	}

	/** flag that indicates if input should be incremented */
	private static boolean stemAsNoun = false;
	private static boolean stemAsVerb = false;
	private static LatinStemmer stemmer;
	static TreeTaggerWrapper<String> tt;
	/** token types */
	public static final String TYPE_NOUN = "LATIN_NOUN";
	public static final String TYPE_VERB = "LATIN_VERB";
	private static String POS = "";
	
	public void destroyTT() {
		tt.destroy();
	}

	public String doStemming(String input) throws IOException,
			TreeTaggerException {

		String stemmedToken;
		char[] currentTokenBuffer;
		int currentTokenLength;
		String[] words;

		StringBuilder newLine = new StringBuilder();

		if (input.isEmpty() || input.equals(""))
			return input;
		words = input.split(" ");
		for (String word : words) {
			stemAsNoun = false;
			stemAsVerb = false;
			word = word.replaceAll(
					"[.,;\"!-()\\[\\]{}\\:?'/\\`~$%#@&*_=+<>*$]", "");
			tt.process(new String[] { word });
			if (POS.charAt(0) == 'N') {
				stemAsNoun = true;
			} else if (POS.charAt(0) == 'V'
					|| (POS.length() >= 2 && POS.charAt(0) == 'A' && POS
							.charAt(1) == 'D')) {
				stemAsVerb = true;
			}
			word = word.toLowerCase();

			// System.out.println(word);
			currentTokenBuffer = word.toCharArray();

			currentTokenLength = word.length();
			/** step 1 - replace 'v' and 'j' (case sensitive) */
			replaceVJ(currentTokenBuffer, currentTokenLength);

			/** step 2 - check for words to stem ending with 'que' */
			int termLength = stemmer.stemQUE(currentTokenBuffer,
					currentTokenLength);
			if (termLength == -1) {
				// write original buffer as noun and verb
				stemmedToken = String.valueOf(currentTokenBuffer, 0,
						currentTokenLength);
			} else {
				/** step 3 - stem as noun or verb */
				stemmedToken = word;
				if (stemAsNoun) {
					stemmedToken = stemmer.stemAsNoun(currentTokenBuffer,
							termLength);
				} else if (stemAsVerb) {
					stemmedToken = stemmer.stemAsVerb(currentTokenBuffer,
							termLength);
				}
			}

			newLine.append(stemmedToken + " ");
		}

		return newLine.toString();
	}

	/**
	 * Replace replace 'v' with 'u' and 'j' with 'i' (case sensitive).
	 *
	 * @author markus klose
	 *
	 * @param termBuffer
	 *            term buffer containing token
	 * @param termLength
	 *            length of the token
	 */
	private void replaceVJ(char termBuffer[], int termLength) {
		for (int i = 0; i < termLength; i++) {
			switch (termBuffer[i]) {
			case 'V':
				termBuffer[i] = 'U';
				break;
			case 'v':
				termBuffer[i] = 'u';
				break;
			case 'J':
				termBuffer[i] = 'I';
				break;
			case 'j':
				termBuffer[i] = 'i';
				break;
			}
		}
	}
}