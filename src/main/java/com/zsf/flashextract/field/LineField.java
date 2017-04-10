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
public class LineField extends Field {

    public LineField(Field parentField, Color color, String text, int beginPos, int endPos) {
        super(parentField, color, text, beginPos, endPos);
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
                String txt = ((SubStringExpression) curExpression).interpret(text);
                if (txt != null) {
                    plainFields.add(new PlainField(this, color,
                            txt,
                            this.beginPos + ((SubStringExpression) curExpression).getPos1(),
                            this.beginPos + ((SubStringExpression) curExpression).getPos2()
                    ));
                }
            }
        }
        return plainFields;
    }
}
