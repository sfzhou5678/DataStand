package com.zsf.flashextract.field;

import com.zsf.flashextract.tools.Color;
import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.NonTerminalExpression;
import com.zsf.interpreter.expressions.string.SubStringExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * 每一块(应该)被选中的的小区域就对应一个Field，也可能是lineSelector选出的LineField
 * Created by hasee on 2017/3/16.
 */
public abstract class Field {

    protected Field parentField;
    protected Color color;
    protected String text;
    protected int beginPos;
    protected int endPos;

    public Field(Field parentField, Color color, String text, int beginPos, int endPos) {
        this.parentField = parentField;
        this.color = color;
        this.text = text;
        this.beginPos = beginPos;
        this.endPos = endPos;
    }


    public abstract Color getColor();
    public abstract int getBeginPos();
    public abstract int getEndPos();
    public abstract String getText();
    public abstract Field getParentField();

}
