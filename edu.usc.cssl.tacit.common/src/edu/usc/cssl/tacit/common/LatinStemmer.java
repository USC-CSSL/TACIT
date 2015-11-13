package edu.usc.cssl.tacit.common;

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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Latin Stemmer. based on
 * http://snowball.tartarus.org/otherapps/schinke/intro.html
 * 
 * @author Markus Klose
 */
public class LatinStemmer {
	// TODO queList as txt file an property in schema.xml ???
	/** latin locale - no country specified */
	private static Locale locale = new Locale("la");
	/** list contains words ending with 'que' that should not be stemmed */
	private List<String> queList;

	public LatinStemmer() {
		// initialize the queList
		queList = Arrays.asList("atque", "quoque", "neque", "itaque", "absque",
				"apsque", "abusque", "adaeque", "adusque", "denique", "deque",
				"susque", "oblique", "peraeque", "plenisque", "quandoque",
				"quisque", "quaeque", "cuiusque", "cuique", "quemque",
				"quamque", "quaque", "quique", "quorumque", "quarumque",
				"quibusque", "quosque", "quasque", "quotusquisque", "quousque",
				"ubique", "undique", "usque", "uterque", "utique", "utroque",
				"utribique", "torque", "coque", "concoque", "contorque",
				"detorque", "decoque", "excoque", "extorque", "obtorque",
				"optorque", "retorque", "recoque", "attorque", "incoque",
				"intorque", "praetorque");
	}

	/**
	 * check if token ends with 'que' and if it should be stemmed
	 * 
	 * @author mk
	 *
	 * @param termBuffer
	 *            term buffer containing token
	 * @param termLength
	 *            length of the token
	 * @return current termLength (termLength - 3' if token ends with 'que'),<br/>
	 *         if token should not be stemmed return -1
	 */
	public int stemQUE(char[] termBuffer, int termLength) {
		// buffer to token
		String currentToken = String.valueOf(termBuffer, 0, termLength)
				.toLowerCase(locale);
		// check if token should be stemmed
		if (queList.contains(currentToken)) {
			// dont stem the token
			return -1;
		}
		// chekc if token ends with 'que'
		if (currentToken.endsWith("que")) {
			// cut of 'que'
			return termLength - 3;
		}
		return termLength;
	}

	/**
	 * removing known noun suffixe.<br/>
	 * changes to the snowball - additional suffixe: arum, erum, orum, ebus,
	 * uum, ium, ei, ui, im
	 * 
	 * @author mk
	 *
	 * @param termBuffer
	 *            term buffer containing token
	 * @param termLength
	 *            length of the token
	 * @return termLength after stemming
	 */
	public String stemAsNoun(char termBuffer[], int termLength) {
		// buffer to string
		String noun = String.valueOf(termBuffer, 0, termLength).toLowerCase(
				locale);
		String stemmed = noun;
		// check longest suffix
		if ((noun.endsWith("rimus") || noun.endsWith("limus"))
				&& noun.length() >= 7) {
			stemmed = String.valueOf(termBuffer, 0, termLength - 5);
		} else if ((noun.endsWith("orum") || noun.endsWith("arum")
				|| noun.endsWith("ibus") || noun.endsWith("erum") || noun
					.endsWith("ebus")) && noun.length() >= 6) {
			stemmed = String.valueOf(termBuffer, 0, termLength - 4);
		} else if ((noun.endsWith("ium") || noun.endsWith("uum")
				|| noun.endsWith("ius") || noun.endsWith("lis")
				|| noun.endsWith("eus") || noun.endsWith("ior"))
				&& noun.length() >= 5) {
			stemmed = String.valueOf(termBuffer, 0, termLength - 3);
		} else if ((noun.endsWith("us") || noun.endsWith("um")
				|| noun.endsWith("am") || noun.endsWith("ae")
				|| noun.endsWith("as") || noun.endsWith("os")
				|| noun.endsWith("is") || noun.endsWith("em")
				|| noun.endsWith("es") || noun.endsWith("ia")
				|| noun.endsWith("ei") || noun.endsWith("ua")
				|| noun.endsWith("ui") || noun.endsWith("er"))
				&& noun.length() >= 4) {
			stemmed = String.valueOf(termBuffer, 0, termLength - 2);
		} else if ((noun.endsWith("a") || noun.endsWith("e")
				|| noun.endsWith("i") || noun.endsWith("o") || noun
					.endsWith("u")) && noun.length() >= 3) {
			stemmed = String.valueOf(termBuffer, 0, termLength - 1);
		}
		// add nd in the end
		// stem nothing
		if (stemmed.endsWith("and") || stemmed.endsWith("ant")) {
			return stemmed.substring(0, stemmed.lastIndexOf("an"));
		} else if (stemmed.endsWith("end") || stemmed.endsWith("ent")) {
			return stemmed.substring(0, stemmed.lastIndexOf("en"));
		} else if (stemmed.endsWith("issim")) {
			return stemmed.substring(0, stemmed.lastIndexOf("issim"));
		}
		return stemmed;
	}

	/**
	 * removing / changing known verb suffixe.<br/>
	 * 
	 * @author mk
	 *
	 * @param termBuffer
	 *            term buffer containing token
	 * @param termLength
	 *            length of the token
	 * @return termLength after stemming
	 */
	public String stemAsVerb(char termBuffer[], int termLength) {
		// buffer to string
		String verb = String.valueOf(termBuffer, 0, termLength).toLowerCase(
				locale);
		String stemmed = verb;
		// check suffixe
		/*
		 * if (verb.endsWith("iuntur") || verb.endsWith("erunt") ||
		 * verb.endsWith("untur") || verb.endsWith("iunt") ||
		 * verb.endsWith("unt")) { // 'iuntur' 'erunt' 'untur' 'iunt' 'unt' ->
		 * 'i' return this.verbSuffixToI(termBuffer, termLength); } /*else if
		 * (verb.endsWith("beris") || verb.endsWith("bor") ||
		 * verb.endsWith("bo")) { // 'beris' 'bor' 'bo' -> 'bi' return
		 * this.verbSuffixToBI(termBuffer, termLength); } else if
		 * (verb.endsWith("ero") && termLength >= 5) { // 'ero' -> 'eri'
		 * termBuffer[termLength -1] = 'i'; return String.valueOf(termBuffer, 0,
		 * termLength); } else if
		 */
		if ((verb.endsWith("issemus") || verb.endsWith("issetis")
				|| verb.endsWith("assemus") || verb.endsWith("assetis"))
				&& termLength >= 9) {
			// 'iuntur' 'erunt' 'untur' 'iunt' 'unt' -> 'i'
			stemmed = String.valueOf(termBuffer, 0, termLength - 7);
		} else if ((verb.endsWith("bantur") || verb.endsWith("eramus")
				|| verb.endsWith("erimus") || verb.endsWith("eratis")
				|| verb.endsWith("eritis") || verb.endsWith("issent")
				|| verb.endsWith("assent") || verb.endsWith("bamini")
				|| verb.endsWith("bimini") || verb.endsWith("buntur"))
				&& termLength >= 8) {
			// 'iuntur' 'erunt' 'untur' 'iunt' 'unt' -> 'i'
			stemmed = String.valueOf(termBuffer, 0, termLength - 6);
		} else if ((verb.endsWith("bamus") || verb.endsWith("batis")
				|| verb.endsWith("bimus") || verb.endsWith("bitis")
				|| verb.endsWith("batur") || verb.endsWith("baris")
				|| verb.endsWith("beris") || verb.endsWith("erunt")
				|| verb.endsWith("issem") || verb.endsWith("isses")
				|| verb.endsWith("isset") || verb.endsWith("assem")
				|| verb.endsWith("asses") || verb.endsWith("asset")
				|| verb.endsWith("erint") || verb.endsWith("erant")
				|| verb.endsWith("imini") || verb.endsWith("istis")
				|| verb.endsWith("bamur") || verb.endsWith("bitur") || verb
					.endsWith("bimur")) && termLength >= 7) {
			// 'mini' 'ntur' 'stis' -> delete
			stemmed = String.valueOf(termBuffer, 0, termLength - 5);
		} else if ((verb.endsWith("mini") || verb.endsWith("ntur")
				|| verb.endsWith("isti") || verb.endsWith("bant")
				|| verb.endsWith("bunt") || verb.endsWith("asse")
				|| verb.endsWith("eram") || verb.endsWith("eras")
				|| verb.endsWith("erat") || verb.endsWith("isse")
				|| verb.endsWith("eris") || verb.endsWith("erim")
				|| verb.endsWith("erit") || verb.endsWith("orum")
				|| verb.endsWith("arum") || verb.endsWith("imus"))
				&& termLength >= 6) {
			// 'mini' 'ntur' 'stis' -> delete
			stemmed = String.valueOf(termBuffer, 0, termLength - 4);
		} else if ((verb.endsWith("mus") || verb.endsWith("mur")
				|| verb.endsWith("ris") || verb.endsWith("sti")
				|| verb.endsWith("tis") || verb.endsWith("bam")
				|| verb.endsWith("bas") || verb.endsWith("bat")
				|| verb.endsWith("bis") || verb.endsWith("bit")
				|| verb.endsWith("tur") || verb.endsWith("are")
				|| verb.endsWith("ire") || verb.endsWith("ere")
				|| verb.endsWith("ari") || verb.endsWith("ite")
				|| verb.endsWith("ero") || verb.endsWith("eri")
				|| verb.endsWith("iri") || verb.endsWith("bar") || verb
					.endsWith("bor")) && termLength >= 5) {
			// 'mus' 'ris' 'sti' 'tis' 'tur' -> delete
			stemmed = String.valueOf(termBuffer, 0, termLength - 3);
		} else if ((verb.endsWith("ns") || verb.endsWith("it")
				|| verb.endsWith("us") || verb.endsWith("um")
				|| verb.endsWith("te") || verb.endsWith("am")
				|| verb.endsWith("ae") || verb.endsWith("as")
				|| verb.endsWith("os") || verb.endsWith("is")
				|| verb.endsWith("nt") || verb.endsWith("bo") || verb
					.endsWith("or")) && termLength >= 4) {
			// 'ns' 'nt' 'ri' -> delete
			stemmed = String.valueOf(termBuffer, 0, termLength - 2);
		} else if ((verb.endsWith("m") || verb.endsWith("r")
				|| verb.endsWith("s") || verb.endsWith("t")
				|| verb.endsWith("a") || verb.endsWith("e")
				|| verb.endsWith("i") || verb.endsWith("o") || verb
					.endsWith("u")) && termLength >= 3) {
			// 'm' 'r' 's' 't' -> delete
			stemmed = String.valueOf(termBuffer, 0, termLength - 1);
		}
		if (stemmed.endsWith("and") || stemmed.endsWith("ant")) {
			return stemmed.substring(0, stemmed.lastIndexOf("an"));
		} else if (stemmed.endsWith("end") || stemmed.endsWith("ent")) {
			return stemmed.substring(0, stemmed.lastIndexOf("en"));
		} else if (stemmed.endsWith("ur")) {
			return stemmed.substring(0, stemmed.lastIndexOf("ur"));
		}
		return stemmed;
	}

	/**
	 * general verb suffixe praesens indikativ aktiv -> o, s, t, mus, tis,
	 * (u)nt, is, it, imus, itis praesens konjunktiv aktiv -> am, as, at, amus,
	 * atis, ant, iam, ias, iat, iamus, iatis, iant
	 *
	 * imperfekt indikativ aktiv -> bam,bas,bat,bamus,batis,bant,
	 * ebam,ebas,ebat,ebamus,ebatis,ebant imperfekt konjunktiv aktiv ->
	 * rem,res,ret,remus,retis,rent, erem,eres,eret,eremus,eretis,erent
	 *
	 * futur 1 indikativ aktiv -> bo,bis,bit,bimus,bitis,bunt,
	 * am,es,et,emus,etis,ent, iam,ies,iet,iemus,ietis,ient futur 2 indikativ
	 * aktiv ->
	 *
	 * perfekt indikativ aktiv -> i,isti,it,imus,istis,erunt, perfekt konjunktiv
	 * aktiv -> erim,eris,erit,erimus,eritis,erint
	 *
	 * plusquamperfekt indikativ aktiv -> eram,eras,erat,eramus,eratis,erant
	 * plusquamperfekt konjunktiv aktiv ->
	 * issem,isses,isset,issemus,issetis,issent
	 */
	// helper methods
	/**
	 * replacing suffix with 'i'
	 * 
	 * @param termBuffer
	 *            term buffer containing token
	 * @param termLength
	 *            length of the token
	 * @return stemmed verb
	 */
	private String verbSuffixToI(char termBuffer[], int termLength) {
		String verb = String.valueOf(termBuffer, 0, termLength).toLowerCase(
				locale);
		// 'iuntur' 'erunt' 'untur' 'iunt' 'unt' -> 'i'
		if (verb.endsWith("iuntur") && termLength >= 8) {
			return String.valueOf(termBuffer, 0, termLength - 5);
		} else if ((verb.endsWith("erunt") || verb.endsWith("untur"))
				&& termLength >= 7) {
			termBuffer[termLength - 5] = 'i';
			return String.valueOf(termBuffer, 0, termLength - 4);
		} else if (verb.endsWith("iunt") && termLength >= 6) {
			;
			return String.valueOf(termBuffer, 0, termLength - 3);
		} else if (verb.endsWith("unt") && termLength >= 5) {
			termBuffer[termLength - 3] = 'i';
			return String.valueOf(termBuffer, 0, termLength - 2);
		}
		return String.valueOf(termBuffer, 0, termLength);
	}

	/**
	 * replacing suffix with 'bi'
	 * 
	 * @param termBuffer
	 *            term buffer containing token
	 * @param termLength
	 *            length of the token
	 * @return stemmed verb
	 */
	private String verbSuffixToBI(char termBuffer[], int termLength) {
		String verb = String.valueOf(termBuffer, 0, termLength).toLowerCase(
				locale);
		// 'beris' 'bor' 'bo' -> 'bi'
		if (verb.endsWith("beris") && termLength >= 7) {
			termBuffer[termLength - 4] = 'i';
			return String.valueOf(termBuffer, 0, termLength - 3);
		} else if (verb.endsWith("bor") && termLength >= 5) {
			termBuffer[termLength - 2] = 'i';
			return String.valueOf(termBuffer, 0, termLength - 1);
		} else if (verb.endsWith("bo") && termLength >= 4) {
			;
			termBuffer[termLength - 1] = 'i';
			return String.valueOf(termBuffer, 0, termLength);
		}
		return String.valueOf(termBuffer, 0, termLength);
	}
}
