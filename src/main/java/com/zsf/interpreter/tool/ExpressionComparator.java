package com.zsf.interpreter.tool;


import com.zsf.interpreter.expressions.Expression;

import java.util.Comparator;

/**
 * Created by hasee on 2017/2/16.
 */
public class ExpressionComparator implements Comparator{
    @Override
    public int compare(Object o1, Object o2) {
        Expression e1=(Expression)o1;
        Expression e2=(Expression)o2;

        Integer deepth1=e1.deepth();
        Integer deepth2=e2.deepth();
        return deepth1.compareTo(deepth2);
    }
}
