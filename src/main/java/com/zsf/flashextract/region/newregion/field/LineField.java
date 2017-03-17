package com.zsf.flashextract.region.newregion.field;

import com.zsf.flashextract.region.newregion.tools.Color;
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
        // TODO: 2017/3/16 不要sout，要new Field
        if (curExpression instanceof NonTerminalExpression) {
            if (curExpression instanceof SubStringExpression) {
                String txt=((SubStringExpression) curExpression).interpret(text);
                plainFields.add(new PlainField(this, color,
                        this.beginPos + ((SubStringExpression) curExpression).getPos1(),
                        this.beginPos + ((SubStringExpression) curExpression).getPos2(),
                        txt));
            }
        }
        return plainFields;
    }
}
