package edu.usc.cssl.nlputils.plugins.latincrawler.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.IOException;
import java.util.Arrays;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;



/**
 * A {@link TokenFilter} that applies {@link LatinStemmer} to stem Latin words.
 * 
 * @author Markus Klose
 */
public final class LatinStemFilter {

	public static void main(String[] args) throws Exception{
		
		/* tagger */
		System.setProperty("treetagger.home", "C:/Users/carlosg/Desktop/CSSL/latin/TreeTagger");
		tt = new TreeTaggerWrapper<String>();
		tt.setModel("C:/Users/carlosg/Desktop/CSSL/latin/latin.par");
		  tt.setHandler(new TokenHandler<String>() {
		         public void token(String token, String pos, String lemma) {
		             //System.out.println(token+"\t"+pos+"\t"+lemma);
		        	 POS = pos;
		         }
		     });
		  stemmer = new LatinStemmer();
		//doStemming("");
		doStemming("C:/Users/carlosg/Desktop/CSSL/latin library/");
		tt.destroy();
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


	/**
	 * Entry point for latin stemming.<br/>
	 * step 1 - replace 'v' with 'u' and 'j' with 'i'<br/>
	 * step 2 - check for tokens ending with 'que' <br/>
	 * step 3 - stem nouns or verb
	 *
	 * @author markus klose
	 * @throws TreeTaggerException 
	 *
	 * @see org.apache.lucene.analysis.TokenStream#incrementToken()
	 */
	public static boolean doStemming(String input) throws IOException, TreeTaggerException {

		BufferedReader br = null;
		BufferedWriter bout = null;
		String currentLine = null;
		
		String stemmedToken;
		char[] currentTokenBuffer;
		int currentTokenLength;
		String[] words;
		
		
		File[] listOfFiles = (new File(input)).listFiles();
		for (File f : listOfFiles) {

			if (f.getAbsolutePath().contains("DS_Store"))
				continue;
			
			String name = f.getAbsolutePath();
			if(!f.exists() || f.isDirectory()){
				doStemming(name);
				continue;
			}
			System.out.println(name);
			br = new BufferedReader(new FileReader(f));
			StringBuilder newLine = new StringBuilder();
			while ((currentLine = br.readLine()) != null) {
				//System.out.println("Line" + currentLine);
				if(currentLine.isEmpty() || currentLine.equals(""))
					continue;
				words = currentLine.split(" ");
				for(String word:words){
					stemAsNoun = false;
					stemAsVerb = false;
					word = word.replaceAll("[.,;\"!-()\\[\\]{}\\:?'/\\`~$%#@&*_=+<>*$]", "");
					tt.process(new String[] {word });
					if(POS.charAt(0) == 'N'){
						stemAsNoun = true;
					}
					else if(POS.charAt(0) == 'V' || (POS.length() >= 2 && POS.charAt(0) == 'A' && POS.charAt(1) == 'D')){
						stemAsVerb = true;
					}
					word = word.toLowerCase();
					
				//	System.out.println(word);
					currentTokenBuffer = word.toCharArray();
			
					currentTokenLength = word.length();
					/** step 1 - replace 'v' and 'j' (case sensitive) */
					replaceVJ(currentTokenBuffer, currentTokenLength);

					/** step 2 - check for words to stem ending with 'que' */
					int termLength = stemmer.stemQUE(currentTokenBuffer, currentTokenLength);
					if (termLength == -1) {
						// write original buffer as noun and verb
						stemmedToken = String.valueOf(currentTokenBuffer, 0,currentTokenLength);
					} else {
						/** step 3 - stem as noun or verb */
						stemmedToken = word;
						if (stemAsNoun) {
							stemmedToken = stemmer.stemAsNoun(currentTokenBuffer,termLength);
						} else if(stemAsVerb) {
							stemmedToken = stemmer.stemAsVerb(currentTokenBuffer,termLength);
						}
					}
					
					newLine.append(stemmedToken + " ");
				}
				newLine.append('\n');
				// switch from noun to verb or vice versa
				/*String tokenType;
				if (stemAsNoun) {
					stemAsNoun = false;
					tokenType = TYPE_NOUN;

				} else {
					stemAsNoun = true;
					tokenType = TYPE_VERB;


				}*/
			}
			br.close();
			bout = new BufferedWriter(new FileWriter(new File(name)));
			bout.write(newLine.toString());
			bout.close();
			
		}
		
		return true;
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
	private static void replaceVJ(char termBuffer[], int termLength) {
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