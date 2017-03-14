package com.zsf.flashextract.regex;

import com.zsf.interpreter.expressions.regex.DynamicRegex;

/**
 * Created by hasee on 2017/2/27.
 */
public class EndWith extends DynamicRegex implements LineSelector {
    public EndWith(String regexName, String reg) {
        super(regexName, reg);
    }
}
