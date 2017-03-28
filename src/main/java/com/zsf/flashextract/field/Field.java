package com.zsf.flashextract.field;

import com.zsf.flashextract.tools.Color;

/**
 * ÿһ��(Ӧ��)��ѡ�еĵ�С����Ͷ�Ӧһ��Field��Ҳ������lineSelectorѡ����LineField
 * Created by hasee on 2017/3/16.
 */
public abstract class Field {

    protected Field parentField;
    protected Color color;
    protected int beginPos;
    protected int endPos;

    public Field(Field parentField, Color color, int beginPos, int endPos) {
        this.parentField = parentField;
        this.color = color;
        this.beginPos = beginPos;
        this.endPos = endPos;
    }

    public abstract Color getColor();
    public abstract int getBeginPos();
    public abstract int getEndPos();
    public abstract String getText();
    public abstract Field getParentField();
}
