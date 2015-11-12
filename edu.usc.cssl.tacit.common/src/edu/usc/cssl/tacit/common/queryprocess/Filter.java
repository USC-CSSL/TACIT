package edu.usc.cssl.tacit.common.queryprocess;

public class Filter {
	QueryDataType targetType = null;
	String operator = null;
	String filterValue = null;
	
	public Filter() {
	}
	
	public Filter(QueryDataType targetType, String operator, String filterValue) {
		this.targetType = targetType;
		this.operator = operator;
		this.filterValue = filterValue;
	}
	
	public QueryDataType getTargetType() {
		return targetType;
	}
	public void setTargetType(QueryDataType targetType) {
		this.targetType = targetType;
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
