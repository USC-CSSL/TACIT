package edu.usc.cssl.tacit.common.queryprocess;

public class Filter {
	String targetName = null;
	String operator = null;
	String filterValue = null;
	QueryDataType targetType = null;
	
	public QueryDataType getTargetType() {
		return targetType;
	}

	public void setTargetType(QueryDataType targetType) {
		this.targetType = targetType;
	}

	public Filter() {
	}
	
	public Filter(String targetName, String operator, String filterValue, QueryDataType targetType) {
		this.targetName = targetName;
		this.operator = operator;
		this.filterValue = filterValue;
		this.targetType = targetType;
	}
	
	public String getTargetName() {
		return targetName;
	}
	public void setTargetType(String targetName) {
		this.targetName = targetName;
	}
	public String getOperationType() {
		return operator;
	}
	public void setOperationType(String operationType) {
		this.operator = operationType;
	}
	public String getFilterValue() {
		return filterValue;
	}
	public void setFilterValue(String filterValue) {
		this.filterValue = filterValue;
	}
}
