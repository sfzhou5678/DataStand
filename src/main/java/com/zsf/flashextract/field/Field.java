package com.zsf.flashextract.field;

import com.zsf.flashextract.tools.Color;

/**
 * 每一块(应该)被选中的的小区域就对应一个Field，也可能是lineSelector选出的LineField
 * Created by hasee on 2017/3/16.
 */
public interface Field {
    Color getColor();
    int getBeginPos();
    int getEndPos();
    String getText();
    Field getParentField();
}
