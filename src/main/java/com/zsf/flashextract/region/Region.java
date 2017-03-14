package com.zsf.flashextract.region;

import com.zsf.interpreter.expressions.regex.Regex;
import com.zsf.interpreter.model.Match;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hasee on 2017/2/27.
 */
public class Region {
    private Region parentRegion;
    /**
     * beginPos表示当前region在parentRegion中的起始位置
     */
    private int beginPos;
    /**
     * endPos表示当前region在parentRegion中的终止位置
     */
    private int endPos;
    private String text;

    /**
     * 在当前容器中被选中的regions()，每个region要求带有一个颜色
     */
    private List<SelectedLineRegion> selectedChildRegions= new ArrayList<SelectedLineRegion>();

    public Region(Region parentRegion, int beginPos, int endPos, String text) {
        this.parentRegion = parentRegion;
        this.beginPos = beginPos;
        this.endPos = endPos;
        this.text=text;
    }

    /**
     * 用于Filter，测试selector是否可以选出此行Region
     * @param selector
     * @return
     */
    public boolean canMatch(Regex selector){
        List<Match> matches=selector.doMatch(text);
        if (matches.size()>0){
            return true;
        }
        return false;
    }

    public Region getParentRegion() {
        return parentRegion;
    }

    public void setParentRegion(Region parentRegion) {
        this.parentRegion = parentRegion;
    }

    public int getBeginPos() {
        return beginPos;
    }

    public void setBeginPos(int beginPos) {
        this.beginPos = beginPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
