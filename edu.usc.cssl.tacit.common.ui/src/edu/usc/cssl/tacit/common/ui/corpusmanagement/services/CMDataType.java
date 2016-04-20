package edu.usc.cssl.tacit.common.ui.corpusmanagement.services;

public enum CMDataType {
	JSON,REDDIT_JSON, TWITTER_JSON, STACKEXCHANGE_JSON, PLAIN_TEXT, XML, MICROSOFT_WORD;

	public static CMDataType get(String dataType) {
		if(dataType.equals("PLAIN_TEXT")) return CMDataType.PLAIN_TEXT;
		else if(dataType.equals("JSON")) return CMDataType.JSON;
		else if(dataType.equals("REDDIT_JSON")) return CMDataType.REDDIT_JSON;
		else if(dataType.equals("TWITTER_JSON")) return CMDataType.TWITTER_JSON;
		else if(dataType.equals("STACKEXCHANGE_JSON")) return CMDataType.STACKEXCHANGE_JSON;
		else if(dataType.equals("XML")) return CMDataType.XML;
		else if(dataType.equals("MICROSOFT_WORD")) return CMDataType.MICROSOFT_WORD;		
		return null;
	}
}
