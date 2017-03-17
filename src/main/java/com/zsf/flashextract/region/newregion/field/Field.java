package com.zsf.flashextract.region.newregion.field;

import com.zsf.flashextract.region.newregion.tools.Color;

/**
 * Created by hasee on 2017/3/16.
 */
public interface Field {
    Color getColor();
    int getBeginPos();
    int getEndPos();
    String getText();
    Field getParentField();
}
