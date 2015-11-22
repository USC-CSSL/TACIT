package edu.usc.cssl.tacit.common.queryprocess;

import java.util.Arrays;
import java.util.List;

public enum QueryDataType {
	INTEGER, DOUBLE, STRING;
	
	public static List<QueryOperatorType> supportedOperations(QueryDataType dataType) {
		if(dataType == QueryDataType.INTEGER) return Arrays.asList(QueryOperatorType.INTEGER_GREATER_THAN, QueryOperatorType.INTEGER_LESS_THAN, QueryOperatorType.INTEGER_EQUALS);
		else if(dataType == QueryDataType.STRING) return Arrays.asList(QueryOperatorType.STRING_EQUALS, QueryOperatorType.STRING_CONTAINS, QueryOperatorType.STRING_STARS_WITH, QueryOperatorType.STRING_ENDS_WITH);
		else if(dataType == QueryDataType.DOUBLE) return Arrays.asList(QueryOperatorType.DOUBLE_GREATER_THAN, QueryOperatorType.DOUBLE_LESS_THAN, QueryOperatorType.DOUBLE_EQUALS);
		return null;
	}	
}
