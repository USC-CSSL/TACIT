package edu.usc.cssl.tacit.common.ui.corpusmanagement.services;

public enum CMDataType {
	JSON,REDDIT_JSON, TWITTER_JSON, STACKEXCHANGE_JSON, FRONTIER_JSON, TYPEPAD_JSON, CONGRESS_JSON, PLAIN_TEXT, XML, MICROSOFT_WORD, PRESIDENCY_JSON, HANSARD_JSON, IMPORTED_CSV, PLOSONE_JSON, PROPUBLICA_JSON, LATIN_JSON, GUTENBERG_JSON;

	public static CMDataType get(String dataType) {
		if(dataType.equals("PLAIN_TEXT")) return CMDataType.PLAIN_TEXT;
		else if(dataType.equals("JSON")) return CMDataType.JSON;
		else if(dataType.equals("REDDIT_JSON")) return CMDataType.REDDIT_JSON;
		else if(dataType.equals("TWITTER_JSON")) return CMDataType.TWITTER_JSON;
		else if(dataType.equals("CONGRESS_JSON")) return CMDataType.CONGRESS_JSON;
		else if(dataType.equals("STACKEXCHANGE_JSON")) return CMDataType.STACKEXCHANGE_JSON;
		else if(dataType.equals("FRONTIER_JSON")) return CMDataType.FRONTIER_JSON;
		else if(dataType.equals("TYPEPAD_JSON")) return CMDataType.TYPEPAD_JSON;
		else if(dataType.equals("XML")) return CMDataType.XML;
		else if(dataType.equals("IMPORTED_CSV")) return CMDataType.IMPORTED_CSV;
		else if(dataType.equals("MICROSOFT_WORD")) return CMDataType.MICROSOFT_WORD;
		else if(dataType.equals("PRESIDENCY_JSON")) return CMDataType.PRESIDENCY_JSON;
		else if(dataType.equals("HANSARD_JSON")) return CMDataType.HANSARD_JSON;
		else if(dataType.equals("PLOSONE_JSON")) return CMDataType.PLOSONE_JSON;
		else if(dataType.equals("PROPUBLICA_JSON")) return CMDataType.PROPUBLICA_JSON;
		else if(dataType.equals("LATIN_JSON")) return CMDataType.LATIN_JSON;
		else if(dataType.equals("GUTENBERG_JSON")) return CMDataType.GUTENBERG_JSON;
		return null;
	}
}
