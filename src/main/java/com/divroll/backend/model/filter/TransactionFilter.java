package com.divroll.backend.model.filter;

public class TransactionFilter {

    public BINARY_OP getOperator() {
        return operator;
    }

    public void setOperator(BINARY_OP operator) {
        this.operator = operator;
    }

    public EQUALITY_OP getEqualityOp() {
        return equalityOp;
    }

    public void setEqualityOp(EQUALITY_OP equalityOp) {
        this.equalityOp = equalityOp;
    }

    public enum BINARY_OP  { INTERSECT, UNION, MINUS, CONCAT }
    public enum EQUALITY_OP  { EQUAL, STARTS_WITH }

    private BINARY_OP operator;
    private EQUALITY_OP equalityOp;
    private String propertyName;
    private Comparable propertyValue;
    private Comparable minValue;
    private Comparable maxValue;

    private TransactionFilter() {}

    public TransactionFilter(BINARY_OP operator, String propertyName, Comparable propertyValue, EQUALITY_OP equalityOp) {
        setOperator(operator);
        setPropertyName(propertyName);
        setPropertyValue(propertyValue);
        setEqualityOp(equalityOp);
    }

    public TransactionFilter(BINARY_OP operator, String propertyName, Comparable minValue, Comparable maxValue, EQUALITY_OP equalityOp) {
        setOperator(operator);
        setPropertyName(propertyName);
        setMaxValue(maxValue);
        setMinValue(minValue);
        setEqualityOp(equalityOp);
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Comparable getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(Comparable propertyValue) {
        this.propertyValue = propertyValue;
    }

    public Comparable getMinValue() {
        return minValue;
    }

    public void setMinValue(Comparable minValue) {
        this.minValue = minValue;
    }

    public Comparable getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Comparable maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public String toString() {
        String s = "[";
        s = s + "operator=" + getOperator() + "\n";
        s = s + "equalityOp=" + getEqualityOp() + "\n";
        s = s + "propertyName=" + getPropertyName() + "\n";
        s = s + "propertyValue=" + getPropertyValue() + "\n";
        s = s + "maxValue=" + getMaxValue() + "\n";
        s = s + "minValue=" + getMinValue() + "\n";
        s = s + "]";
        return s;
    }
}
