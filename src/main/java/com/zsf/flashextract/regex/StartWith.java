package com.zsf.flashextract.regex;

import com.zsf.interpreter.expressions.regex.DynamicRegex;

/**
 * Created by hasee on 2017/2/27.
 */
public class StartWith extends DynamicRegex implements LineSelector {
    public StartWith(String regexName, String reg) {
        super(regexName, reg);
    }
}
