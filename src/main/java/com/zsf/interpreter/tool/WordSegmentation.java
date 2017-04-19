package com.zsf.interpreter.tool;

import com.zsf.common.UsefulRegex;
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

    private static List<Regex> wordSegmentationRegex= UsefulRegex.getUsefulRegex();

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
     * @param segmentation1
     * @param segmentation2
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

        List<String> segmentation1= getSegmentationByRMM(str1,maxWordLength);
        List<String> segmentation2= getSegmentationByRMM(str2,maxWordLength);

        StringBuilder sb1=new StringBuilder("[");
        for (String s:segmentation1){
            sb1.append(s+" ");
        }
        sb1.append("]");
        System.out.println(sb1.toString());

        StringBuilder sb2=new StringBuilder("[");
        for (String s:segmentation2){
            sb2.append(s+" ");
        }
        sb2.append("]");
        System.out.println(sb2.toString());

        int distance= calculateLevenshtein(segmentation1,segmentation2);

        double similarity=1 - (double) distance / Math.max(segmentation1.size(), segmentation2.size());
        return similarity;
    }

    public static void main(String[] args) {
        List<String> inputStringList=new ArrayList<String>();
        int maxWordLength=12;
        inputStringList.add("Bus Station,40.7572213,-73.99154663,Jun 27 13:14:26");
        inputStringList.add("Coffee Shop,40.72795461,-73.99326138,Oct 08 20:11:11");
        inputStringList.add("0,2015-05-10 23:59:00,Sunday,BAYVIEW,2000 Block of THOMAS AV");


        // 计算编辑距离
        for (int i = 0; i < inputStringList.size(); i++) {
            for (int j = i + 1; j < inputStringList.size(); j++) {
                System.out.println(i+" "+j);
                System.out.println(calculateSimilarity(inputStringList.get(i),inputStringList.get(j),maxWordLength));
            }
        }
    }
}
