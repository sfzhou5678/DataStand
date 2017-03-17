package com.zsf.flashextract.region.newregion.tools;

import com.zsf.flashextract.region.newregion.field.Field;

import java.util.Comparator;

/**
 * Created by hasee on 2017/3/17.
 */
public class FieldComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        Field field1=(Field)o1;
        Field field2=(Field)o2;

        Integer field1Pos1=field1.getBeginPos();
        Integer field2Pos2=field2.getBeginPos();

        return field1Pos1.compareTo(field2Pos2);
    }
}