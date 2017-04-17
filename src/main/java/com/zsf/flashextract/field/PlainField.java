package com.zsf.flashextract.field;

import com.zsf.flashextract.tools.Color;
import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.NonTerminalExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hasee on 2017/3/16.
 */
public class PlainField extends Field {
    private String editedText;
    private List<Expression> extraEditExpressions = new ArrayList<Expression>();
    private Expression unConfirmedExtraExp;

    public PlainField(Field parentField, Color color, String text, int beginPos, int endPos) {
        super(parentField, color, text, beginPos, endPos);
        this.editedText = text;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlainField) {
            return ((PlainField) obj).getBeginPos() == beginPos
                    && ((PlainField) obj).getEndPos() == endPos
                    && ((PlainField) obj).color == color;
        }
        return false;
    }


    public Color getColor() {
        return color;
    }

    @Override
    public Field getParentField() {
        return parentField;
    }

    public int getBeginPos() {
        return beginPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public String getText() {
        return text;
    }

    public String getEditedText() {
        return editedText;
    }

    public void setEditedText(String editedText) {
        this.editedText = editedText;
    }

    public void setUnConfirmedExtraExp(Expression unConfirmedExtraExp) {
        this.unConfirmedExtraExp = unConfirmedExtraExp;
    }

    /**
     * ColorRegion的previewExp会调用setUnConfirmedExtraExp设置待定exp
     * 之后调用此方法确认这个待定exp有效，并添加到extraEditExpressions中
     * 然后更新editedText
     */
    public void confirmExtraExp() {
        if (this.unConfirmedExtraExp != null) {
            extraEditExpressions.add(unConfirmedExtraExp);
            this.editedText=((NonTerminalExpression) unConfirmedExtraExp).interpret(editedText);
            this.unConfirmedExtraExp = null;
        }
    }
}
