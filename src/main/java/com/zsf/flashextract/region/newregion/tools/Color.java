package com.zsf.flashextract.region.newregion.tools;

import java.util.HashMap;

/**
 * һ�������� ���캯��Ϊprivate��
 * Created by hasee on 2017/3/16.
 */
public class Color {
    private static HashMap<Integer,Color> colorMap=new HashMap<Integer, Color>();
    // FIXME: 2017/3/18 ����color������α�ʾ�´ο���һ��
    public static final Color DEFAULT=new Color(0);
    public static final Color BLUE=new Color(1);
    public static final Color GREEN=new Color(2);
    public static final Color YELLOW=new Color(3);

    private int color;

    @Override
    public String toString() {
        return String.valueOf(color);
    }

    private Color(int color) {
        this.color = color;
        if (!colorMap.containsKey(color)){
            colorMap.put(color,this);
        }
    }

    public int getColor() {
        return color;
    }

    public int getColorId() {
        return color;
    }

    public static Color getColor(int color){
        if (!colorMap.containsKey(color)){
            colorMap.put(color,new Color(color));
        }
        return colorMap.get(color);
    }
}
