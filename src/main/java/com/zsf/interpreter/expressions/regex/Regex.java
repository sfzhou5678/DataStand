package com.zsf.interpreter.expressions.regex;

import com.zsf.flashextract.regex.DynimicRegexTools;
import com.zsf.interpreter.expressions.Score;
import com.zsf.interpreter.model.Match;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代表正则token
 * Created by hasee on 2017/1/22.
 */
public abstract class Regex implements Score,DynimicRegexTools {
    private String regexName;
    private String reg;
    private Pattern pattern;

    public Regex(String regexName, String reg) {
        this.regexName = regexName;
        this.reg = reg;

        this.pattern=Pattern.compile(reg);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Regex){
            return this.reg.equals(((Regex) obj).getReg());
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s",regexName);
    }

    public String getRegexName() {
        return regexName;
    }

    public void setRegexName(String regexName) {
        this.regexName = regexName;
    }

    public String getReg() {
        return reg;
    }

    public void setReg(String reg) {
        this.reg = reg;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * 在inputString中 利用this.pattern做match
     *
     * 例子：
     * inputString="hello-123-aaa"
     * this.reg="[a-z]+"
     * 那么matches分别为hello和aaa
     * @param inputString
     * @return
     */
    public List<Match> doMatch(String inputString) {
        if (pattern==null){
            return null;
        }
        List<Match> matches=new ArrayList<Match>();
        Matcher matcher=pattern.matcher(inputString);
        int count=1;
        while (matcher.find()){
            matches.add(new Match(inputString,matcher.start(),matcher.group(),this,count++));
        }
        count--;
        for (Match match:matches){
            match.setMaxCount(count);
        }
        return matches;
    }

    @Override
    public boolean needAddDynimicToken(String subStr) {
        if (pattern==null){
            return true;
        }
        Matcher matcher=pattern.matcher(subStr);
        while (matcher.find()){
            if (matcher.group().equals(subStr)){
                return false;
            }
        }
        return true;
    }
}
