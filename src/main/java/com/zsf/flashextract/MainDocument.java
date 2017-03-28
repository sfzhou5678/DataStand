package com.zsf.flashextract;

import com.zsf.flashextract.region.ColorRegion;
import com.zsf.flashextract.field.Field;
import com.zsf.flashextract.message.MessageSelectField;
import com.zsf.flashextract.tools.Color;
import com.zsf.flashextract.tools.FieldComparator;
import com.zsf.interpreter.expressions.regex.EpicRegex;
import com.zsf.interpreter.expressions.regex.NormalRegex;
import com.zsf.interpreter.expressions.regex.RareRegex;
import com.zsf.interpreter.expressions.regex.Regex;

import java.util.*;

/**
 * 最高层的FE容器，包含输入文本document和所有ColorReiongs
 * Created by hasee on 2017/3/16.
 */
public class MainDocument {
    private String document;
    private HashMap<Color, ColorRegion> colorRegionMap = new HashMap<Color, ColorRegion>();

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
//        regexList.add(new Regex("SpecialTokens","[ -+()\\[\\],.:]+"));

        return regexList;
    }

    public MainDocument(String document) {
        this.document = document;
    }

    public void selectField(Color color, int beginPos, int endPos, String text) {
        ColorRegion colorRegion = colorRegionMap.get(color);
        if (colorRegion == null) {
            colorRegion = new ColorRegion(color, document);
            colorRegionMap.put(color, colorRegion);
        }
        // TODO: 2017/3/16 判断当前区域是否在其他color的lineSelector之内！！
        // TODO: 2017/3/17 判断是否已经产生过selector了
        int lineIndex = colorRegion.calculateLineIndex(beginPos, endPos);
        for (ColorRegion region : colorRegionMap.values()) {
            if (region != colorRegion) {
                // FIXME: 2017/3/27 如果补充选择positiveExample 现在会变成重复选择多次(带来n行NULL数据)！
                if (region.getNeedSelectLineIndex().contains(lineIndex)) {
                    colorRegion.selectFieldByOuterSelector(lineIndex, beginPos, endPos, text, region.getCurLineSelector());
                    break;
                }
            }
        }
        colorRegion.selectField(lineIndex, beginPos, endPos, text);
    }

    private List<Field> fieldList;

    public MessageSelectField showSelectedFields() {
        fieldList = new ArrayList<Field>();
        int maxRow = 0;
        for (ColorRegion colorRegion : colorRegionMap.values()) {
            for (Field field : colorRegion.getFieldsGenerated()) {
                maxRow = Math.max(maxRow, colorRegion.getFieldsGenerated().size());
                if (!fieldList.contains(field)) {
                    fieldList.add(field);
                }
            }
            for (Field field : colorRegion.getFieldsByUser()) {
                maxRow = Math.max(maxRow, colorRegion.getFieldsByUser().size());
                if (!fieldList.contains(field)) {
                    fieldList.add(field);
                }
            }
        }
        // 按照beginPos从小到大sort
        Collections.sort(fieldList, new FieldComparator());

        // 产生color和title
        List<Color> colors = new ArrayList<Color>();
        List<String> titles = new ArrayList<String>();
        for (Field field : fieldList) {
            if (colors.contains(field.getColor())) {
                break;
            } else {
                colors.add(field.getColor());
                titles.add(getRegioinTitle(field.getColor()));
            }
        }

        // 产生tableDatas
        String[][] tableDatas = new String[maxRow][colors.size()];
        for (int i = 0; i < maxRow; i++) {
            for (int j = 0; j < colors.size(); j++) {
                tableDatas[i][j] = "NULL";
            }
        }
        int curRow = 0;
        int lastColIndex = -1;
        boolean needAddRow = false;
        for (Field field : fieldList) {
            int colIndex = colors.indexOf(field.getColor());

            if (colIndex <= lastColIndex && needAddRow) {
                curRow++;
            }
            lastColIndex = colIndex;

            tableDatas[curRow][colIndex] = field.getText();
            if ((colIndex == colors.size() - 1)) {
                curRow++;
                needAddRow = false;
            } else {
                needAddRow = true;
            }
        }
        MessageSelectField messageSelectField = new MessageSelectField(fieldList, colors, titles, tableDatas);
        return messageSelectField;
    }

    public String getRegioinTitle(Color color) {
        ColorRegion colorRegion = colorRegionMap.get(color);
        if (colorRegion != null) {
            return colorRegion.getRegionTitle();
        } else {
            return "";
        }
    }

    public void setRegionTitle(Color color, String title) {
        ColorRegion colorRegion = colorRegionMap.get(color);
        if (colorRegion != null) {
            colorRegion.setRegionTitle(title);
        }
    }
}
