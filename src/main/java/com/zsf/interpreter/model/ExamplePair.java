package com.zsf.interpreter.model;

/**
 * Created by hasee on 2017/2/5.
 */
public class ExamplePair {
    private String inputString;
    private String outputString;

    public ExamplePair(String inputString, String outputString) {
        this.inputString = inputString;
        this.outputString = outputString;
    }

    public String getInputString() {
        return inputString;
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }

    public String getOutputString() {
        return outputString;
    }

    public void setOutputString(String outputString) {
        this.outputString = outputString;
    }
}
