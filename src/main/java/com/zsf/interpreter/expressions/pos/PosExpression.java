package com.zsf.interpreter.expressions.pos;

import com.zsf.interpreter.expressions.TerminalExpression;

/**
 * Created by hasee on 2017/1/22.
 */
public abstract class PosExpression extends TerminalExpression {

    public static final int ILLEGAL_POS=Integer.MIN_VALUE;
    public static final int START_POS=-Integer.MAX_VALUE;
    public static final int END_POS=Integer.MAX_VALUE;

    @Override
    public abstract boolean equals(Object obj);

    public abstract int interpret(String inputString);
}
