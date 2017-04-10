package com.zsf.flashextract.field;

import com.zsf.flashextract.tools.Color;

/**
 * Created by hasee on 2017/3/16.
 */
public class PlainField extends Field {
    private String editedText;

    public PlainField(Field parentField, Color color, String text, int beginPos, int endPos) {
        super(parentField, color, text, beginPos, endPos);
        this.editedText=text;
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
}
