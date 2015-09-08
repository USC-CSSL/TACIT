package edu.usc.cssl.tacit.common.ui.corpusmanagement.services;

public enum DataType {
	REDDIT_JSON, TWITTER_JSON, PLAIN_TEXT, XML, MICROSOFT_WORD;

	public static DataType get(String dataType) {
		if(dataType.equals("PLAIN_TEXT")) return DataType.PLAIN_TEXT;
		else if(dataType.equals("REDDIT_JSON")) return DataType.REDDIT_JSON;
		else if(dataType.equals("TWITTER_JSON")) return DataType.TWITTER_JSON;
		else if(dataType.equals("XML")) return DataType.XML;
		else if(dataType.equals("MICROSOFT_WORD")) return DataType.MICROSOFT_WORD;		
		return null;
	}
}
