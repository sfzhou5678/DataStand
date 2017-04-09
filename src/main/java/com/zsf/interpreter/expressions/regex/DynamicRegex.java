package com.zsf.interpreter.expressions.regex;

/**
 * Created by hasee on 2017/3/1.
 */
public class DynamicRegex extends Regex {

    public DynamicRegex(String regexName, String reg) {
        super(regexName, reg);
    }

    public double score() {
        // TODO: 2017/3/1 dynamicToken的score应该要高一些
        // TODO: 2017/3/1 应该考虑信息量, dynamic的最高，特殊token(如<[{!@/等)次高，常见token(date、日期等)其次，最低是普通token(alpha等)
        return 0.35+Math.min(0.2,(this.getReg().length()/10)*0.1);
    }
}
