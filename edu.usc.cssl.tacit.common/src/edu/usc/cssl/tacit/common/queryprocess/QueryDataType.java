package edu.usc.cssl.tacit.common.queryprocess;

import java.util.Arrays;
import java.util.List;

public enum QueryDataType {
	INTEGER, DOUBLE, STRING;
	
	public static List<String> supportedOperations(QueryDataType dataType) {
		if(dataType == QueryDataType.INTEGER || dataType == QueryDataType.DOUBLE) return Arrays.asList(">", "<", "==");
		else if(dataType == QueryDataType.STRING) return Arrays.asList("equals", "startsWith", "endsWith");
		return null;
	}	
}
