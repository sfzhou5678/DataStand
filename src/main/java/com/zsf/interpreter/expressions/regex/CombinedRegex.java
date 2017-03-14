package com.zsf.interpreter.expressions.regex;

/**
 * Created by hasee on 2017/3/1.
 */
public class CombinedRegex extends Regex {
    public CombinedRegex(String regexName, String reg) {
        super(regexName, reg);
    }

    @Override
    public double score() {
        return 0.2+(this.getReg().length()/10)*0.1;
    }
}
