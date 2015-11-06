package edu.usc.cssl.tacit.common.queryprocess;

import java.util.Arrays;
import java.util.List;

public enum SupportedDataTypes {
	INTEGER, DOUBLE, STRING;
	
	public static List<String> supportedOperations(SupportedDataTypes dataType) {
		if(dataType == SupportedDataTypes.INTEGER || dataType == SupportedDataTypes.DOUBLE) return Arrays.asList(">", "<", "==");
		else if(dataType == SupportedDataTypes.STRING) return Arrays.asList("equals", "startsWith", "endsWith");
		return null;
	}	
}
