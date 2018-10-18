package com.divroll.backend.model;

import java.util.List;

public class JsFunction {

    private String functionName;
    private List<Comparable> arguments;
    private String expression;

    public JsFunction() {
    }

    public JsFunction(String functionName, List<Comparable> arguments) {
        setFunctionName(functionName);
        setArguments(arguments);
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public List<Comparable> getArguments() {
        return arguments;
    }

    public void setArguments(List<Comparable> arguments) {
        this.arguments = arguments;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
