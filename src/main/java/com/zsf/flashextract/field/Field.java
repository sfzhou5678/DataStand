package com.zsf.flashextract.field;

import com.zsf.flashextract.tools.Color;

/**
 * ÿһ��(Ӧ��)��ѡ�еĵ�С����Ͷ�Ӧһ��Field��Ҳ������lineSelectorѡ����LineField
 * Created by hasee on 2017/3/16.
 */
public interface Field {
    Color getColor();
    int getBeginPos();
    int getEndPos();
    String getText();
    Field getParentField();
}
