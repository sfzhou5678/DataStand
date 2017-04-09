package com.zsf.flashextract.tools;

import com.zsf.flashextract.field.Field;
import com.zsf.flashextract.field.LineField;
import com.zsf.interpreter.expressions.regex.DynamicRegex;
import com.zsf.interpreter.expressions.regex.Regex;
import com.zsf.interpreter.model.Match;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hasee on 2017/3/16.
 */
public class RegexCommomTools {

    /**
     * 在每次有新的input时就调用此方法，可以返回 各个pos上所有能够和input匹配的集合
     * 当generatePosition()需要时，直接根据match的pos(index)去查找使用，避免重复计算
     */
    public static List<Match> buildStringMatches(String inputString, List<Regex> usefulRegex) {
        List<Match> matches = new ArrayList<Match>();
        for (int i = 0; i < usefulRegex.size(); i++) {
            Regex regex = usefulRegex.get(i);
            List<Match> curMatcher = regex.doMatch(inputString);
            matches.addAll(curMatcher);
        }
        return matches;
    }

    public static List<Regex> deDuplication(List<List<Regex>> regexs, boolean isStartWith) {
        List<Regex> deDuplicatedList = new ArrayList<Regex>();
        if (regexs.size() <= 0) {
            return deDuplicatedList;
        }
        List<Regex> baseRegexList = regexs.get(0);
        for (Regex baseRegex : baseRegexList) {
            boolean needAddIn = false;
            for (int j = 1; j < regexs.size(); j++) {
                List<Regex> regexList = regexs.get(j);

                for (Regex regex : regexList) {
                    if ((baseRegex.equals(regex))) {
                        needAddIn = true;
                        break;
                    }
                }
                if (!needAddIn) {
                    break;
                }
            }
            if (needAddIn) {
                if (isStartWith) {
                    baseRegex.setReg("^" + baseRegex.getReg());
                    baseRegex.setRegexName("startWith(" + baseRegex.getRegexName() + ")");
                } else {
                    baseRegex.setReg(baseRegex.getReg() + "$");
                    baseRegex.setRegexName("endWith(" + baseRegex.getRegexName() + ")");
                }
                deDuplicatedList.add(baseRegex);
            }
        }
        return deDuplicatedList;
    }

    /**
     * 获得符合所有examples的endWith语法
     *
     * @param curDeepth
     * @param maxDeepth
     * @param matches
     * @param endNode
     * @param lastRegex
     */
    public static List<Regex> buildEndWith(int curDeepth, int maxDeepth,
                                           List<Match> matches, int endNode, Regex lastRegex) {
        if (curDeepth > maxDeepth) {
            return null;
        }
        String connector = "+";
        if (curDeepth == 1) {
            connector = "";
        }
        List<Regex> regexList = new ArrayList<Regex>();
        for (int i = matches.size() - 1; i >= 0; i--) {
            Match match = matches.get(i);
            if ((match.getMatchedIndex() + match.getMatchedString().length()) == endNode) {

                Regex curRegex = new DynamicRegex(match.getRegex().getRegexName() + connector + lastRegex.getRegexName(),
                        match.getRegex().getReg() + lastRegex.getReg());
                regexList.add(curRegex);
                List<Regex> curList = buildEndWith(curDeepth + 1, maxDeepth,
                        matches, match.getMatchedIndex(),
                        curRegex);
                if (curList != null) {
                    regexList.addAll(curList);
                }
            }
        }
        return regexList;
    }

    /**
     * 获得符合所有examples的startWith语法
     *
     * @param curDeepth
     * @param maxDeepth
     * @param matches
     * @param beginNode
     * @param lastRegex
     */
    public static List<Regex> buildStartWith(int curDeepth, int maxDeepth,
                                             List<Match> matches, int beginNode, Regex lastRegex) {
        if (curDeepth > maxDeepth) {
            return null;
        }
        String connector = "+";
        if (curDeepth == 1) {
            connector = "";
        }
        List<Regex> regexList = new ArrayList<Regex>();

        for (int i = 0; i < matches.size(); i++) {
            Match match = matches.get(i);
            if (match.getMatchedIndex() == beginNode) {
                Regex curRegex = new DynamicRegex(lastRegex.getRegexName() + connector + match.getRegex().getRegexName(),
                        lastRegex.getReg() + match.getRegex().getReg());
                regexList.add(curRegex);

                List<Regex> curList = buildStartWith(curDeepth + 1, maxDeepth,
                        matches, match.getMatchedIndex() + match.getMatchedString().length(),
                        curRegex);
                if (curList != null) {
                    regexList.addAll(curList);
                }
            }
        }
        return regexList;
    }

    public static List<Regex> filterUsefulSelector(List<Regex> regices, List<LineField> lineFields,
                                                   List<Integer> positiveLineIndex, List<Integer> negataiveLineIndex) {
        List<Regex> usefulLineSelector = new ArrayList<Regex>();

        for (Regex regex : regices) {
            boolean needAddIn = true;
            for (int index : positiveLineIndex) {
                if (!lineFields.get(index).canMatch(regex)) {
                    needAddIn = false;
                    break;
                }
            }
            for (int index : negataiveLineIndex) {
                if (lineFields.get(index).canMatch(regex)) {
                    needAddIn = false;
                    break;
                }
            }
            if (needAddIn) {
                usefulLineSelector.add(regex);
            }
        }
        return usefulLineSelector;
    }

    /**
     * 从当前新选择的区域出发，分别向左&向右匹配相同str作为dynamicToken添加到usefulRegex中
     *
     * @param fieldsByUser
     * @param lineFields
     */
    public static void addDynamicToken(List<Field> fieldsByUser, List<Regex> usefulRegex, List<LineField> lineFields) {
        // TODO: 2017/3/28 对于末尾的数字，是否要处理？ 就是这种href="/p/4
        List<String> strings = new ArrayList<String>();
        for (Field field : fieldsByUser) {
            strings.add(field.getParentField().getText());
        }
        Map<String, Integer> dynamicStrMap = new HashMap<String, Integer>();
        List<Integer> timesList = new ArrayList<Integer>();
        String str0 = strings.get(0);
        int len = str0.length();
        for (int i = 0; i < len; i++) {
            for (int j = len; j > i; j--) {
                String subStr = str0.substring(i, j);
                boolean needAddIn = true;
                for (int k = 1; k < strings.size(); k++) {
                    if (!strings.get(k).contains(subStr)) {
                        needAddIn = false;
                        break;
                    }
                }
                if (needAddIn) {
                    for (Regex regex : usefulRegex) {
                        if (!regex.needAddDynimicToken(subStr)) {
                            needAddIn = false;
                            break;
                        }
                    }
                }
                if (needAddIn) {
                    int times = calculateAppearTimes(subStr, lineFields);
                    timesList.add(times);
                    dynamicStrMap.put(subStr, times);
//                    doAddDynamicToken(subStr,usefulRegex);
                    i = j - 1;
                    break;
                }
            }
        }
        // FIXME: 2017/4/9 tieba数据中 dynamicStrMap只有"</a"和"xxx/p" 但是startwith却又"</a>" 和"xxx/p/4"???
        int mediumTimes = calculateMedian(timesList);
        for (Map.Entry<String, Integer> entry : dynamicStrMap.entrySet()) {
            doAddDynamicToken(entry.getKey(), entry.getValue(), mediumTimes, usefulRegex);
        }
    }

    /**
     * 计算中位数
     *
     * @param scoreList
     * @return
     */
    private static int calculateMedian(List<Integer> scoreList) {
        Collections.sort(scoreList);
        int size = scoreList.size();
        if (size % 2 == 0) {
            return (scoreList.get(size / 2 - 1) + scoreList.get(size / 2)) / 2;
        } else {
            return scoreList.get(size / 2);
        }
    }

    /**
     * 计算字符串str在全文中出现的次数，用于辅助确定dynamicToken的权重
     *
     * @param str
     * @param lineFields
     * @return
     */
    private static int calculateAppearTimes(String str, List<LineField> lineFields) {
        int count = 0;
        Pattern pattern = Pattern.compile(str);
        for (LineField lineField : lineFields) {
            Matcher matcher = pattern.matcher(lineField.getText());
            while (matcher.find()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 若subStr不能被usefulRegex中的任何一个匹配，那么就将他加入作为dynamicToken
     *
     * @param subStr
     * @param times
     * @param medium
     * @param usefulRegex
     */
    private static void doAddDynamicToken(String subStr, Integer times, int medium, List<Regex> usefulRegex) {
        try {
            /**
             * 倍数
             */
            double beishu = times * 1.0 / medium;
            Regex rightRegex = new DynamicRegex("DynamicTok(" + subStr + ")", subStr, beishu > 1 ? Math.min(beishu, 3) : Math.max(1 / 3, beishu));
            if (!usefulRegex.contains(rightRegex)) {
                usefulRegex.add(rightRegex);
            }
        } catch (Exception e) {
        }
    }

    public static int indexNOf(String inputString, String target, int n) {
        Matcher matcher = Pattern.compile(target).matcher(inputString);
        int count = 1;
        while (matcher.find()) {
            if (count++ == n) {
                break;
            }
        }
        try {
            return matcher.start();
        } catch (Exception e) {
            return -1;
        }
    }
}
