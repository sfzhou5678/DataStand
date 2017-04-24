package com.zsf.interpreter.expressions.pos;

import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.regex.Regex;
import com.zsf.interpreter.model.Match;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hasee on 2017/2/28.
 */
public class StartRegPos extends PosExpression {

    private Regex r;
    private int count;
    private int maxCount;

    public StartRegPos(Regex r, int count, int maxCount) {
        this.r = r;
        this.count = count;
        this.maxCount=maxCount;

        // TODO: 2017/3/2 利用maxCount将count转换成maxCount(如果有必要的话)
    }

    @Override
    public int interpret(String inputString) {
        List<Match> matches = r.doMatch(inputString);
        int c = 0;
        int total = 0;
        List<Integer> posList = new ArrayList<Integer>();
        for (Match match : matches) {
            total++;
            c++;
            if (c == count) {
                return match.getMatchedIndex();
            }
            posList.add(match.getMatchedIndex());
        }
        if (count < 0) {
            int contraPos = count + (total);
            if (contraPos >= 0 && contraPos < posList.size()) {
                return posList.get(contraPos);
            }
        }
        return PosExpression.ILLEGAL_POS;
    }

    @Override
    public String toString() {
        return String.format("startRegPos(%s,%d)", r.toString(), count);
    }

    @Override
    public Expression deepClone() {
        return new StartRegPos(r, count,maxCount);
    }

    @Override
    public int deepth() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StartRegPos) {
            return r.equals(((StartRegPos) obj).getR()) && count == ((StartRegPos) obj).getCount();
        }
        return false;
    }

    @Override
    public double score() {
        return 0.25+r.score();
    }

    public Regex getR() {
        return r;
    }

    public void setR(Regex r) {
        this.r = r;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
