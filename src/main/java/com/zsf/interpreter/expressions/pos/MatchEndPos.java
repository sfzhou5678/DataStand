package com.zsf.interpreter.expressions.pos;

import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.regex.Regex;
import com.zsf.interpreter.model.Match;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hasee on 2017/2/28.
 */
public class MatchEndPos extends PosExpression {

    private Regex r;
    private int count;
    private int maxCount;


    public MatchEndPos(Regex r, int count,int maxCount) {
        this.r = r;
        this.count = count;
        this.maxCount=maxCount;
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
                return match.getMatchedIndex()+match.getMatchedString().length();
            }
            posList.add(match.getMatchedIndex()+match.getMatchedString().length());
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
        return String.format("eRegPos(%s,%d)", r.toString(), count);
    }

    @Override
    public Expression deepClone() {
        return new MatchEndPos(r, count,maxCount);
    }

    @Override
    public int deepth() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MatchEndPos) {
            return r.equals(((MatchEndPos) obj).getR()) && count == ((MatchEndPos) obj).getCount();
        }
        return false;
    }

    @Override
    public double score() {
        double score=0.3+r.score();
        return score;
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
