package com.zsf.interpreter.tool;

import com.zsf.interpreter.expressions.regex.NormalRegex;
import com.zsf.interpreter.expressions.regex.RareRegex;
import com.zsf.interpreter.expressions.regex.Regex;
import com.zsf.interpreter.model.Match;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by hasee on 2017/2/5.
 */
public class WordSegmentation {

    private final static String UNK_TOK = "UNK_TOK";

    private static List<Regex> wordSegmentationRegex=null;

    private static void initUsefulRegex() {
        if (wordSegmentationRegex==null){
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

            // FIXME: 2017/2/5 如果开启这个SpTok在当前算法下会导致解过于庞大
            regexList.add(new RareRegex("SpecialTokens", "[ -+()\\[\\],.:&|_/]+"));
            wordSegmentationRegex= regexList;
        }
    }

    /**
     * 逆向最大匹配分词算法测试
     * <p>
     * Electronics Store,40.74260751,-73.99270535,Tue Apr 03 18:08:57 +0800 2012
     * <p>
     * Wed Jul 11 11:17:44 +0800 2012,40.23213,German Restaurant
     * <p>
     * 注意：分词最关键的是delimter选定，暂定有[,. ]+
     *
     * @param string 待转换的字符串
     * @param maxWordLength 单词库中最长单词的长度,每次取词长度不超过2*maxWrodLength
     */
    private static List<String> getSegmentationByRMM(String string, int maxWordLength) {
        int step = 2*maxWordLength;

        // 逆向
        List<String> segmentation = new ArrayList<String>();
        int len = string.length();
        for (int i = len; i > 0; ) {
            boolean matched = false;
            for (int j = step; j > 0; j--) {
                if (i - j >= 0) {
                    String subString = string.substring(i - j, i);
                    String matchedToken = hasMatch(subString);
                    if (matchedToken != null) {
//                        System.out.println(String.format("Match %s subString=%s",matchedToken,subString));
                        i = i - j;
                        matched = true;
                        segmentation.add(matchedToken);
                    }
                }
            }
            if (!matched) {
                i--;
                segmentation.add(UNK_TOK);
            }
        }

        Collections.reverse(segmentation);
        return segmentation;
    }

    private static String hasMatch(String subString) {
        for (int i = 0; i < wordSegmentationRegex.size(); i++) {
            Regex regex = wordSegmentationRegex.get(i);
            List<Match> curMatcher = regex.doMatch(subString);
            for (Match match : curMatcher) {
                if (match.getMatchedString().equals(subString)) {
                    return match.getRegex().toString();
                }
            }
        }
        return null;
    }

    /**
     * 　　DNA分析 　　拼字检查 　　语音辨识 　　抄袭侦测
     *
     * @param segmentation1
     * @param segmentation2
     * @createTime 2012-1-12
     */
    private static int calculateLevenshtein(List<String> segmentation1, List<String> segmentation2) {
        //计算两个字符串的长度。
        int len1 = segmentation1.size();
        int len2 = segmentation2.size();

        //建立上面说的数组，比字符长度大一个空间
        int[][] dp = new int[len1 + 1][len2 + 1];
        //赋初值，步骤B。
        for (int a = 0; a <= len1; a++) {
            dp[a][0] = a;
        }
        for (int a = 0; a <= len2; a++) {
            dp[0][a] = a;
        }
        //计算两个字符是否一样，计算左上的值
        int temp;
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (isSame(segmentation1.get(i-1),segmentation2.get(j-1))) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                //取三个值中最小的
                dp[i][j] = findMin(dp[i - 1][j - 1] + temp, dp[i][j - 1] + 1,
                        dp[i - 1][j] + 1);
            }
        }
        //计算相似度
        return dp[len1][len2];
    }

    private static boolean isSame(String reg1, String reg2) {
        return reg1.equals(reg2);
    }

    private static int findMin(int... nums) {
        int min = Integer.MAX_VALUE;
        for (int i : nums) {
            if (min > i) {
                min = i;
            }
        }
        return min;
    }


    /**
     * 外部只能调用此方法
     * 计算两个字符串的相似度
     * @param str1
     * @param str2
     * @return
     */
    public static double calculateSimilarity(String str1, String str2,int maxWordLength){
        initUsefulRegex();

        List<String> segmentation1= getSegmentationByRMM(str1,maxWordLength);
        List<String> segmentation2= getSegmentationByRMM(str2,maxWordLength);

        int distance= calculateLevenshtein(segmentation1,segmentation2);

        double similarity=1 - (double) distance / Math.max(segmentation1.size(), segmentation2.size());
        return similarity;
    }

    public static void main(String[] args) {
        initUsefulRegex();

        List<String> inputStringList=new ArrayList<String>();
        int maxWordLength=12;
        // 第一类
        inputStringList.add("Electronics Store,40.74260751,-73.99270535,Tue Apr 03 18:08:57 +0800 2012");
        inputStringList.add("Bridge,Tue Apr 03 18:00:25 +0800 2012");
        inputStringList.add("Arts & Crafts Store,40.71981038,-74.00258103,Tue Apr 03 18:00:09 +0800 2012");
        inputStringList.add("Airport,40.77446436,-73.86970997,Sun Jul 15 14:51:15 +0800 2012");

//
//        // 第二类
        inputStringList.add("Wed Jul 11 11:17:44 +0800 2012,40.23213,German Restaurant");
        inputStringList.add("40.7451638,-73.98251878,Tue Apr 03 18:02:41 +0800 2012,Medical Center");

        // test Input
//        segmentationList.add(getSegmentationByRMM("Food & Drink Shop,40.69990191,-74.2342329,Sat Nov 17 20:36:26 +0800 2012", maxWordLength));
//        segmentationList.add(getSegmentationByRMM("Road,40.60847918,-74.12698746,Wed Jul 11 11:40:03 +0800 2012", maxWordLength));
        inputStringList.add("40.74218831,-73.98792419,Park,Wed Jul 11 11:42:00 +0800 2012");
//        segmentationList.add(getSegmentationByRMM(",40.88729679,Wed Jul 11 11:44:23 +0800 2012,Home (private)", maxWordLength));


//        segmentationList.add(getSegmentationByRMM("12-23-34"));
//        segmentationList.add(getSegmentationByRMM("12.3.4"));
//        segmentationList.add(getSegmentationByRMM("(123)-84-122"));
////        // 第二类
//        segmentationList.add(getSegmentationByRMM("74-12"));

//        segmentationList.add(getSegmentationByRMM("01/21/2001"));
//        segmentationList.add(getSegmentationByRMM("2003-03-23"));
//        segmentationList.add(getSegmentationByRMM("2003.03.23"));

        // 计算编辑距离
        for (int i = 0; i < inputStringList.size(); i++) {
            for (int j = i + 1; j < inputStringList.size(); j++) {
                System.out.println(i+" "+j);
                System.out.println(calculateSimilarity(inputStringList.get(i),inputStringList.get(j),maxWordLength));
            }
        }
    }
}
