package com.zsf.model;

/**
 * 验证集
 * Created by hasee on 2017/2/6.
 */
public class ValidationPair {
    private String inputString;
    private String targetString;

    public ValidationPair(String inputString, String targetString) {
        this.inputString = inputString;
        this.targetString = targetString;
    }

    public String getInputString() {
        return inputString;
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }

    public String getTargetString() {
        return targetString;
    }

    public void setTargetString(String targetString) {
        this.targetString = targetString;
    }
}
