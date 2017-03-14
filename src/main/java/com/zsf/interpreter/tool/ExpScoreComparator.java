package com.zsf.interpreter.tool;

import com.zsf.interpreter.expressions.Expression;

import java.util.Comparator;

/**
 * Created by hasee on 2017/3/1.
 */
public class ExpScoreComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        Expression e1=(Expression)o1;
        Expression e2=(Expression)o2;

        Double score1=e1.score();
        Double score2=e2.score();
        return score2.compareTo(score1);
    }
}
