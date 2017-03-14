package com.zsf.interpreter.expressions.string;

import com.zsf.interpreter.expressions.Expression;

/**
 * Created by hasee on 2016/12/27.
 */
public class ConstStrExpression extends StringExpression {
    private String constStr;

    public ConstStrExpression(String constStr) {
        this.constStr = constStr;
    }

    @Override
    public String interpret(String inputString) {
        return constStr;
    }

    public String getConstStr() {
        return constStr;
    }

    public void setConstStr(String constStr) {
        this.constStr = constStr;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConstStrExpression){
            return constStr.equals(((ConstStrExpression) obj).getConstStr());
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("constStr(%s)",constStr);
    }

    @Override
    public Expression deepClone() {
        return new ConstStrExpression(constStr);
    }

    @Override
    public int deepth() {
        return 1;
    }

    @Override
    public double score() {
        return 0.2;
    }


}
