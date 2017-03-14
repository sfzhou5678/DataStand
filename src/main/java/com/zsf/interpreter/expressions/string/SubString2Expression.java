package com.zsf.interpreter.expressions.string;

import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.regex.Regex;
import com.zsf.interpreter.model.Match;

import java.util.List;

/**
 * Created by hasee on 2017/1/23.
 */
public class SubString2Expression extends StringExpression {
    private Regex regex;
    private int c;
    private int totalC;

    public SubString2Expression(Regex regex, int c) {
        this.regex = regex;
        this.c = c;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SubString2Expression){
            // FIXME: 2017/2/5 equals判定加上c可能会导致无法正常生成Loop
            return regex.equals(((SubString2Expression) obj).getRegex()) && c==((SubString2Expression) obj).getC();
        }
        return false;
    }
    public boolean loopEquals(Object obj) {
        if (obj instanceof SubString2Expression){
            // FIXME: 2017/2/5 equals判定加上c可能会导致无法正常生成Loop
            return regex.equals(((SubString2Expression) obj).getRegex());
        }
        return false;
    }

    @Override
    public Expression deepClone() {
        return new SubString2Expression(regex,c);
    }

    @Override
    public int deepth() {
        return 1;
    }

    @Override
    public String toString() {
        return String.format("subStr2(%s,%d)",regex.toString(),c);
    }

    @Override
    public String interpret(String inputString) {
        List<Match> matches=regex.doMatch(inputString);
        String ans="";
        if (c-1<matches.size()){
            ans=matches.get(c-1).getMatchedString();
        }else {
//            System.out.println("substr2超出索引范围");
            ans=null;
        }
        return ans;
    }

    public int getTotalC() {
        return totalC;
    }

    public void setTotalC(int totalC) {
        this.totalC = totalC;
    }

    @Override
    public double score() {
        return 0.5+regex.score();
    }

    public Regex getRegex() {
        return regex;
    }

    public void setRegex(Regex regex) {
        this.regex = regex;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }



}
