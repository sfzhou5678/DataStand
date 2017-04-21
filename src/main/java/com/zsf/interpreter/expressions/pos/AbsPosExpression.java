package com.zsf.interpreter.expressions.pos;

import com.zsf.interpreter.expressions.Expression;

/**
 * Created by hasee on 2016/12/27.
 */
public class AbsPosExpression extends PosExpression {

    private int pos;

    public AbsPosExpression(int pos) {
        this.pos = pos;
    }

    @Override
    public String toString() {
        return String.format("absPos(%s)", pos == PosExpression.START_POS ? "START_POS" :
                (pos == PosExpression.END_POS ? "END_POS" : String.valueOf(pos)));
    }

    @Override
    public Expression deepClone() {
        return new AbsPosExpression(pos);
    }

    @Override
    public int deepth() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbsPosExpression) {
            return ((AbsPosExpression) obj).getPos() == this.getPos();
        }
        return false;
    }

    @Override
    public int interpret(String inputString) {
        return pos;
    }


    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    @Override
    public double score() {
        if (pos==PosExpression.START_POS||pos==PosExpression.END_POS){
            return 0.5;
        }
        return 0.1;
    }
}
