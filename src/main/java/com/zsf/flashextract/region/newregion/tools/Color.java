package com.zsf.flashextract.region.newregion.tools;

/**
 * Created by hasee on 2017/3/16.
 */
public class Color {
    public static final Color DEFAULT=new Color(0);
    public static final Color BLUE=new Color(1);
    public static final Color GREEN=new Color(2);
    public static final Color YELLOW=new Color(3);

    private int color;

    @Override
    public String toString() {
        return String.valueOf(color);
    }

    public Color(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
