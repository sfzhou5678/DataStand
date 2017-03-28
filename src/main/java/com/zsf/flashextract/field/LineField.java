package com.zsf.flashextract.field;

import com.zsf.flashextract.tools.Color;
import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.NonTerminalExpression;
import com.zsf.interpreter.expressions.regex.Regex;
import com.zsf.interpreter.expressions.string.SubStringExpression;
import com.zsf.interpreter.model.Match;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hasee on 2017/3/16.
 */
public class LineField implements Field {

    private Color color;
    private Field parentField;
    private int beginPos;
    private int endPos;
    private String text;

    public LineField(Field parentField, int beginPos, int endPos, String text) {
        this.parentField = parentField;
        this.beginPos = beginPos;
        this.endPos = endPos;
        this.text = text;

        this.color=Color.DEFAULT;
    }

    @Override
    public Field getParentField() {
        return parentField;
    }


    @Override
    public int getBeginPos() {
        return beginPos;
    }

    @Override
    public int getEndPos() {
        return endPos;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Color getColor() {
        return color;
    }

    public boolean canMatch(Regex curLineSelector) {
        List<Match> matches = curLineSelector.doMatch(text);
        return matches.size() > 0;
    }

    public List<PlainField> selectChildFieldByExp(Expression curExpression, Color color) {
        List<PlainField> plainFields = new ArrayList<PlainField>();
        if (curExpression instanceof NonTerminalExpression) {
            if (curExpression instanceof SubStringExpression) {
                // FIXME: 2017/3/27 有时会出现无法正确匹配的方式，导致 text="null"(可接受) pos=-无穷(不可接受)。
                String txt=((SubStringExpression) curExpression).interpret(text);
                if (txt!=null){
                    plainFields.add(new PlainField(this, color,
                            this.beginPos + ((SubStringExpression) curExpression).getPos1(),
                            this.beginPos + ((SubStringExpression) curExpression).getPos2(),
                            txt));
                }
            }
        }
        return plainFields;
    }
}
