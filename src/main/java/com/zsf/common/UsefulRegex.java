package com.zsf.common;

import com.zsf.interpreter.expressions.regex.EpicRegex;
import com.zsf.interpreter.expressions.regex.NormalRegex;
import com.zsf.interpreter.expressions.regex.RareRegex;
import com.zsf.interpreter.expressions.regex.Regex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsf on 2017/4/9.
 */
public class UsefulRegex {

    private static List<Regex> usefulRegex=null;

    public static List<Regex> getUsefulRegex(){
        if (usefulRegex==null){
            usefulRegex=initUsefulRegex();
        }
        return usefulRegex;
    }

    public static void init(){
        usefulRegex=initUsefulRegex();
    }

    /**
     * ������Ч��token����ǿ��ƥ������
     * <p>
     * ����ÿ���һ��token��������Ҫ�������token�ܲ����Ľ����
     * token����ᵼ�½����ը����(�������˼�ǧ��)
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
        regexList.add(new NormalRegex("AlphaDigitToken", "[a-zA-Z0-9]+"));
        regexList.add(new NormalRegex("ChineseToken", "[\\u4E00-\\u9FFF]+"));

//        regexList.add(new Regex("WordToken","[a-z\\sA-Z]+")); // ƥ�䵥�ʵ�token���ᵼ�½����ը������ʮ��

        // TimeToken��ƥ��[12:15 | 10:26:59 PM| 22:01:15 aM]��ʽ��ʱ������
        regexList.add(new RareRegex("TimeToken", "(([0-1]?[0-9])|([2][0-3])):([0-5]?[0-9])(:([0-5]?[0-9]))?([ ]*[aApP][mM])?"));
        // YMDToken��ƥ��[10/03/1979 | 1-1-02 | 01.1.2003]��ʽ������������
        regexList.add(new RareRegex("YMDToken", "([0]?[1-9]|[1|2][0-9]|[3][0|1])[./-]([0]?[1-9]|[1][0-2])[./-]([0-9]{4}|[0-9]{2})"));
        // YMDToken2��ƥ��[2004-04-30 | 2004-02-29],��ƥ��[2004-04-31 | 2004-02-30 | 2004-2-15 | 2004-5-7]
        regexList.add(new RareRegex("YMDToken2", "[0-9]{4}-(((0[13578]|(10|12))-(0[1-9]|[1-2][0-9]|3[0-1]))|(02-(0[1-9]|[1-2][0-9]))|((0[469]|11)-(0[1-9]|[1-2][0-9]|30)))"));
        // TextDate��ƥ��[Apr 03 | February 28 | November 02] (PS:�򻯰棬û�������ڵ��߼�����)
        regexList.add(new RareRegex("TextDate", "(Jan(uary)?|Feb(ruary)?|Ma(r(ch)?|y)|Apr(il)?|Jul(y)?|Ju((ly?)|(ne?))|Aug(ust)?|Oct(ober)?|(Sept|Nov|Dec)(ember)?)[ -]?(0[1-9]|[1-2][0-9]|3[01])"));
        regexList.add(new RareRegex("WhichDayToken", "(Mon|Tues|Fri|Sun)(day)?|Wed(nesday)?|(Thur|Tue)(sday)?|Sat(urday)?"));
//        regices.add(new Regex("AlphaNumToken", "[a-z A-Z 0-9]+"));

        // special tokens
        regexList.add(new EpicRegex("TestSymbolToken", "[-]+"));
        regexList.add(new EpicRegex("CommaToken", "[,]+"));
        regexList.add(new EpicRegex("<", "[<]+"));
        regexList.add(new EpicRegex(">", "[>]+"));
        regexList.add(new EpicRegex("/", "[/]+"));
        regexList.add(new EpicRegex("SpaceToken", "[ ]+"));
        regexList.add(new EpicRegex("\"", "[\"]+"));


//        regexList.add(new NormalRegex("SpecialTokens","[ -+()\\[\\],.:]+"));

        return regexList;
    }
}
