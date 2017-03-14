package com.zsf.flashextract.model;

import com.zsf.flashextract.region.SelectedLineRegion;
import com.zsf.interpreter.expressions.regex.EpicRegex;
import com.zsf.interpreter.expressions.regex.NormalRegex;
import com.zsf.interpreter.expressions.regex.RareRegex;
import com.zsf.interpreter.expressions.regex.Regex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsf on 2017/3/13.
 */
public class FlashExtract {

    /**
     * 跟region、select相关的都放到document里了，FE相当于一个中介，配合doucment和GUI
     */
    private Document document;

    public static List<Regex> usefulRegex = initUsefulRegex();

    /**
     * 增加有效的token可以强化匹配能力
     * <p>
     * 但是每添加一个token，答案数就要乘以这个token能产生的结果数
     * token过多会导致结果爆炸增长(很容易伤几千万)
     *
     * @return
     */
    private static List<Regex> initUsefulRegex() {
        List<Regex> regexList = new ArrayList<Regex>();
        regexList.add(new NormalRegex("SimpleNumberTok", "[0-9]+"));
        regexList.add(new NormalRegex("DigitToken", "[-+]?(([0-9]+)([.]([0-9]+))?)"));
        regexList.add(new NormalRegex("LowerToken", "[a-z]+"));
        regexList.add(new NormalRegex("UpperToken", "[A-Z]+"));
        regexList.add(new NormalRegex("AlphaToken", "[a-zA-Z]+"));
//        regexList.add(new Regex("WordToken","[a-z\\sA-Z]+")); // 匹配单词的token，会导致结果爆炸增长几十万倍

        // TimeToken可匹配[12:15 | 10:26:59 PM| 22:01:15 aM]形式的时间数据
        regexList.add(new RareRegex("TimeToken", "(([0-1]?[0-9])|([2][0-3])):([0-5]?[0-9])(:([0-5]?[0-9]))?([ ]*[aApP][mM])?"));
        // YMDToken可匹配[10/03/1979 | 1-1-02 | 01.1.2003]形式的年月日数据
        regexList.add(new RareRegex("YMDToken", "([0]?[1-9]|[1|2][0-9]|[3][0|1])[./-]([0]?[1-9]|[1][0-2])[./-]([0-9]{4}|[0-9]{2})"));
        // YMDToken2可匹配[2004-04-30 | 2004-02-29],不匹配[2004-04-31 | 2004-02-30 | 2004-2-15 | 2004-5-7]
        regexList.add(new RareRegex("YMDToken2", "[0-9]{4}-(((0[13578]|(10|12))-(0[1-9]|[1-2][0-9]|3[0-1]))|(02-(0[1-9]|[1-2][0-9]))|((0[469]|11)-(0[1-9]|[1-2][0-9]|30)))"));
        // TextDate可匹配[Apr 03 | February 28 | November 02] (PS:简化版，没处理日期的逻辑错误)
        regexList.add(new RareRegex("TextDate", "(Jan(uary)?|Feb(ruary)?|Ma(r(ch)?|y)|Apr(il)?|Jul(y)?|Ju((ly?)|(ne?))|Aug(ust)?|Oct(ober)?|(Sept|Nov|Dec)(ember)?)[ -]?(0[1-9]|[1-2][0-9]|3[01])"));
        regexList.add(new RareRegex("WhichDayToken", "(Mon|Tues|Fri|Sun)(day)?|Wed(nesday)?|(Thur|Tue)(sday)?|Sat(urday)?"));
//        regices.add(new Regex("AlphaNumToken", "[a-z A-Z 0-9]+"));

        // special tokens
        regexList.add(new EpicRegex("TestSymbolToken", "[-]+"));
        regexList.add(new EpicRegex("CommaToken", "[,]+"));
        regexList.add(new EpicRegex("<", "[<]+"));
        regexList.add(new EpicRegex(">", "[>]+"));
        regexList.add(new EpicRegex("/", "[/]+"));
        regexList.add(new EpicRegex("SpaceToken", "[ ]+")); // 加上之后就出不了结果？？
        // FIXME: 2017/2/5 如果开启这个SpTok在当前算法下会导致解过于庞大
//        regexList.add(new Regex("SpecialTokens","[ -+()\\[\\],.:]+"));

        return regexList;
    }

    /**
     * 根据某个selector(Regex)选择符合条件的regions
     *
     * @param selector
     */
    public void selectRegionBySelector(Regex selector, int color) {
        List<SelectedLineRegion> selectedLineRegions = document.selectRegionsBySelector(selector, color);
        for (SelectedLineRegion region : selectedLineRegions) {
            System.out.println(region.getText());
        }
//        for (Region region:document.getDocumentRegions()){
//            if (region.canMatch(selector)){
//                System.out.println(region.getText());
//            }
//        }
    }

    public void setInputDocument(String inputDocument) {
        this.document = new Document(inputDocument, usefulRegex);
    }

    public void doSelectRegion(int color, int lineIndex, int beginPos, int endPos, String selectedText) {
        document.doSelectRegion(color, lineIndex, beginPos, endPos, selectedText);
    }

    public List<Regex> getLineSelector(int color) {
        return document.getLineSelector(color);
    }

    /**
     * 产生LineSelector之后，自动在LineRegion中根据提供的例子产生childRegion
     * @param color
     */
    public void generateChildRegionsInLineRegions(int color) {
        document.generateChildRegionsInLineRegions(color);
    }

    public void doSelectRegionInLineRegions(int color, int lineIndex, int beginPos, int endPos, String selectedText) {
        // FIXME: 2017/3/14 这个函数最终要个doSelectRegion合并
        document.doSelectRegionInLineRegions(color,lineIndex,beginPos,endPos,selectedText);
    }

    public boolean needGenerateLineReions(int color) {
        return document.needGenerateLineReions(color);
    }
}
